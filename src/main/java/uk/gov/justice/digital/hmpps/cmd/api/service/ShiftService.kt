package uk.gov.justice.digital.hmpps.cmd.api.service

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.cmd.api.client.CsrClient
import uk.gov.justice.digital.hmpps.cmd.api.client.CsrDetailDto
import uk.gov.justice.digital.hmpps.cmd.api.domain.DetailDisplayType
import uk.gov.justice.digital.hmpps.cmd.api.dto.DetailDto
import uk.gov.justice.digital.hmpps.cmd.api.dto.ShiftDto
import uk.gov.justice.digital.hmpps.cmd.api.security.AuthenticationFacade
import uk.gov.justice.digital.hmpps.cmd.api.uk.gov.justice.digital.hmpps.cmd.api.domain.DetailParentType
import uk.gov.justice.digital.hmpps.cmd.api.uk.gov.justice.digital.hmpps.cmd.api.domain.DetailType
import java.time.*
import java.util.*
import java.util.stream.Collectors

@Service
@Transactional
class ShiftService(private val prisonService: PrisonService,
                   private val csrClient: CsrClient,
                   private val clock: Clock,
                   private val authenticationFacade : AuthenticationFacade) {

    fun getDetailsForUser(fromParam: Optional<LocalDate>, toParam: Optional<LocalDate>): Collection<ShiftDto> {
        val start = fromParam.orElse(LocalDate.now(clock))
        val end = toParam.orElse(LocalDate.now(clock))
        val region = prisonService.getPrisonForUser()?.region
        val details = csrClient.getDetailsForUser(start, end, region, authenticationFacade.currentUsername)
        val detailsByDate = groupDetailsByDate(details)

        return start.datesUntil(end.plusDays(1)).map { date ->
            val detailsForDate = detailsByDate.getOrDefault(date, listOf())
            ShiftDto(
                    date,
                    calculateFullDayType(detailsForDate),
                    getAllEvents(date, detailsForDate.distinct())
            )
        }.collect(Collectors.toList())
    }

    private fun groupDetailsByDate(details : Collection<CsrDetailDto>): Map<LocalDate, Collection<CsrDetailDto>> {
        val detailStartGroup = details.groupBy { it.detailStart.toLocalDate() }
        val detailEndGroup = details.groupBy { it.detailEnd.toLocalDate() }
        return (detailStartGroup.keys + detailEndGroup.keys)
                .associateWith {
                    detailStartGroup.getOrDefault(it, listOf()) +
                            detailEndGroup.getOrDefault(it, listOf())
                }
    }

    private fun calculateFullDayType(tasks: Collection<CsrDetailDto>): DetailType {
        if (tasks.any()) {
            val isFullDay = tasks.minBy { it.detailStart }?.detailStart?.toLocalTime() == LocalTime.MIDNIGHT
            tasks.forEach {
                val activity = DetailType.from(it.activity)

                if ((activity == DetailType.UNSPECIFIC && isFullDay) ||
                   (activity == DetailType.REST_DAY && isFullDay)  ||
                        //TODO: Partial Rest day
                   (activity == DetailType.ABSENCE && isFullDay) ||
                   (activity == DetailType.TRAINING_INTERNAL) ||
                   (activity == DetailType.TRAINING_EXTERNAL) ||
                   (activity == DetailType.ILLNESS) ||
                   (activity == DetailType.HOLIDAY && isFullDay) ||
                   (activity == DetailType.HOLIDAY && tasks.none { task -> DetailType.from(task.activity) != DetailType.HOLIDAY })
                   ) {
                    return activity
                }
            }
            return DetailType.SHIFT

        } else {
            return DetailType.NONE
        }
    }

    private fun getAllEvents(date: LocalDate, csrDetails: Collection<CsrDetailDto>): Collection<DetailDto> {
        val (shiftDetails, overtimeDetails) = csrDetails.partition { it.shiftType == DetailParentType.SHIFT }

        val shiftStartAndFinishEvents = getStartAndFinishEvents(date, shiftDetails)
        val overtimeStartAndFinishEvents = getStartAndFinishEvents(date, overtimeDetails)
        val startAndFinishEvents = (shiftStartAndFinishEvents + overtimeStartAndFinishEvents)

        val middleEvents = csrDetails
                .map {
                    DetailDto(
                            it.activity,
                            it.detailStart,
                            it.detailEnd,
                            it.shiftType,
                            null
                    )
                }.filter { event ->
                    startAndFinishEvents
                            .all { sfe -> event.start != sfe.start && event.end != sfe.end }
                }
        return startAndFinishEvents + middleEvents
    }

    /* For each day we are looking for :
     * 1) the earliest start date with a finish date on the same day, this is a day shift start
     * 2) the latest finish date with a start date on the same day, this is a day shift end
     * 3) the latest start date, if the end date is on a different day we have a night shift start.
     * 4) the earliest finish date, if the start date is a different day we have a night shift end.
     */
    private fun getStartAndFinishEvents(date: LocalDate, csrDetails: Collection<CsrDetailDto>): Collection<DetailDto> {

        val (dayShiftDetails, nightShiftDetails) = csrDetails
                .filter { it.detailStart.toLocalTime() != LocalTime.MIDNIGHT }
                .partition { it.detailStart.toLocalTime() < it.detailEnd.toLocalTime()}

        // Identify a Day Shift Starting
        val dayShiftStart = dayShiftDetails.filter { it.detailStart.toLocalDate() == date }.minBy { it.detailStart }?.let {
            DetailDto(
                    it.activity,
                    it.detailStart,
                    it.detailEnd,
                    it.shiftType,
                    when (it.shiftType) {
                        DetailParentType.OVERTIME -> {
                            DetailDisplayType.OVERTIME_DAY_START
                        }
                        else -> {
                            DetailDisplayType.DAY_START
                        }
                    },
                    it.detailStart)
        }

        // Identify a Day Shift Finishing
        val dayShiftEnd = dayShiftDetails.filter { it.detailEnd.toLocalDate() == date }.maxBy { it.detailEnd }?.let {
            DetailDto(
                    it.activity,
                    it.detailStart,
                    it.detailEnd,
                    it.shiftType,
                    when (it.shiftType) {
                        DetailParentType.OVERTIME -> {
                            DetailDisplayType.OVERTIME_DAY_FINISH
                        }
                        else -> {
                            DetailDisplayType.DAY_FINISH
                        }
                    },
                    it.detailEnd,
                    calculateShiftDuration(dayShiftDetails))
        }

        // Identify a Night Shift Starting
        val nightShiftStart = nightShiftDetails.filter { it.detailStart.toLocalDate() == date }.maxBy { it.detailStart }?.let {
            DetailDto(
                    it.activity,
                    it.detailStart,
                    it.detailEnd,
                    it.shiftType,
                    when (it.shiftType) {
                        DetailParentType.OVERTIME -> {
                            DetailDisplayType.OVERTIME_NIGHT_START
                        }
                        else -> {
                            DetailDisplayType.NIGHT_START
                        }
                    },
                    it.detailStart)
        }

        // Identify a Night Shift Finishing
        val nightShiftEnd = nightShiftDetails.filter { it.detailEnd.toLocalDate() == date }.minBy { it.detailEnd }?.let {
            DetailDto(
                    it.activity,
                    it.detailStart,
                    it.detailEnd,
                    it.shiftType,
                    when (it.shiftType) {
                        DetailParentType.OVERTIME -> {
                            DetailDisplayType.OVERTIME_NIGHT_FINISH
                        }
                        else -> {
                            DetailDisplayType.NIGHT_FINISH
                        }
                    },
                    it.detailEnd,
                    /*
                     Nightshift finishes work differently,
                     we need to calculate the duration between
                     nightshift_start and nightshift_finish only.
                     because the collection has multiple day's data in it.
                     */
                    calculateSingleDuration(it.detailStart, it.detailEnd))
        }

        return listOfNotNull(nightShiftEnd, nightShiftStart, dayShiftStart, dayShiftEnd)
    }

    private fun calculateShiftDuration(details: Collection<CsrDetailDto>): String {
        // We have to exclude unpaid breaks
        val sum = details.filter { detail -> DetailType.from(detail.activity) != DetailType.BREAK }.map {
            detail ->
                Duration.between(
                detail.detailStart,
                detail.detailEnd).toSeconds()
        }.sum()
        return formatToHoursMins(sum)
    }

    private fun calculateSingleDuration(start: LocalDateTime, end: LocalDateTime) : String {
        val sum = Duration.between(
                start,
                end).toSeconds()
        return formatToHoursMins(sum)
    }

    private fun formatToHoursMins(sum : Long) : String {
        return String.format("%dh %02dm", sum / 3600, (sum % 3600) / 60)
    }

    companion object {

        private val log = LoggerFactory.getLogger(ShiftService::class.java)
    }
}