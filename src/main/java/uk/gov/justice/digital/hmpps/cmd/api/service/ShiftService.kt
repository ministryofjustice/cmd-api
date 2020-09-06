package uk.gov.justice.digital.hmpps.cmd.api.service

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.cmd.api.client.CsrClient
import uk.gov.justice.digital.hmpps.cmd.api.client.CsrDetailDto
import uk.gov.justice.digital.hmpps.cmd.api.domain.ActivityType
import uk.gov.justice.digital.hmpps.cmd.api.domain.TaskDisplayType
import uk.gov.justice.digital.hmpps.cmd.api.dto.DetailDto
import uk.gov.justice.digital.hmpps.cmd.api.dto.DetailEventDto
import uk.gov.justice.digital.hmpps.cmd.api.uk.gov.justice.digital.hmpps.cmd.api.domain.DetailType
import uk.gov.justice.digital.hmpps.cmd.api.uk.gov.justice.digital.hmpps.cmd.api.domain.ShiftType
import java.time.*
import java.util.*
import java.util.stream.Collectors

@Service
@Transactional
class ShiftService(private val prisonService: PrisonService,
                   private val csrClient: CsrClient,
                   private val clock: Clock) {

    fun getDetailsForUser(fromParam: Optional<LocalDate>, toParam: Optional<LocalDate>): Collection<DetailDto> {
        val start = fromParam.orElse(LocalDate.now(clock))
        val end = toParam.orElse(LocalDate.now(clock))
        val region = prisonService.getPrisonForUser()?.region

        val details = csrClient.getDetailsForUser(start, end, region)
        val detailsByDate = groupDetailsByDate(details)

        return start.datesUntil(end.plusDays(1)).map { date ->
            val detailsForDate = detailsByDate.getOrDefault(date, listOf())
            DetailDto(
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

    private fun calculateFullDayType(tasks: Collection<CsrDetailDto>): String {
        if (tasks.any()) {
            val isFullDay = tasks.minBy { it.detailStart }?.detailStart?.toLocalTime() == LocalTime.MIDNIGHT
            tasks.forEach {
                val detailType = it.detailType
                val activityType = ActivityType.fromDescription(it.activity)

                if ((detailType == DetailType.UNSPECIFIC && isFullDay) ||
                        detailType == DetailType.ABSENCE ||
                        activityType == ActivityType.TRAINING_INTERNAL ||
                        activityType == ActivityType.TRAINING_EXTERNAL) {
                    return it.activity
                } else if (detailType == DetailType.ILLNESS ||
                        detailType == DetailType.HOLIDAY && isFullDay ||
                        detailType == DetailType.HOLIDAY && tasks.none { task -> task.detailType == DetailType.UNSPECIFIC }) {
                    return detailType.description
                }
            }
            return ActivityType.SHIFT.description

        } else {
            return ActivityType.NONE.description
        }
    }

    private fun getAllEvents(date: LocalDate, csrDetails: Collection<CsrDetailDto>): Collection<DetailEventDto> {
        val (shiftDetails, overtimeDetails) = csrDetails.partition { it.shiftType == ShiftType.SHIFT }

        val shiftStartAndFinishEvents = getStartAndFinishEvents(date, shiftDetails)
        val overtimeStartAndFinishEvents = getStartAndFinishEvents(date, overtimeDetails)
        val startAndFinishEvents = (shiftStartAndFinishEvents + overtimeStartAndFinishEvents)

        val middleEvents = csrDetails
                .map {
                    DetailEventDto(
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
    private fun getStartAndFinishEvents(date: LocalDate, csrDetails: Collection<CsrDetailDto>): Collection<DetailEventDto> {

        val (dayShiftDetails, nightShiftDetails) = csrDetails
                .filter { it.detailStart != LocalDateTime.MIN }
                .partition { it.detailStart.toLocalTime() < it.detailEnd.toLocalTime()}

        // Identify a Day Shift Starting
        val dayShiftStart = dayShiftDetails.filter { it.detailStart.toLocalDate() == date }.minBy { it.detailStart }?.let {
            DetailEventDto(
                    it.activity,
                    it.detailStart,
                    it.detailEnd,
                    it.shiftType,
                    when (it.shiftType) {
                        ShiftType.OVERTIME -> {
                            TaskDisplayType.OVERTIME_DAY_START
                        }
                        else -> {
                            TaskDisplayType.DAY_START
                        }
                    },
                    it.detailStart)
        }

        // Identify a Day Shift Finishing
        val dayShiftEnd = dayShiftDetails.filter { it.detailEnd.toLocalDate() == date }.maxBy { it.detailEnd }?.let {
            DetailEventDto(
                    it.activity,
                    it.detailStart,
                    it.detailEnd,
                    it.shiftType,
                    when (it.shiftType) {
                        ShiftType.OVERTIME -> {
                            TaskDisplayType.OVERTIME_DAY_FINISH
                        }
                        else -> {
                            TaskDisplayType.DAY_FINISH
                        }
                    },
                    it.detailEnd,
                    calculateShiftDuration(dayShiftDetails))
        }

        // Identify a Night Shift Starting
        val nightShiftStart = nightShiftDetails.filter { it.detailStart.toLocalDate() == date }.maxBy { it.detailStart }?.let {
            DetailEventDto(
                    it.activity,
                    it.detailStart,
                    it.detailEnd,
                    it.shiftType,
                    when (it.shiftType) {
                        ShiftType.OVERTIME -> {
                            TaskDisplayType.OVERTIME_NIGHT_START
                        }
                        else -> {
                            TaskDisplayType.NIGHT_START
                        }
                    },
                    it.detailStart)
        }

        // Identify a Night Shift Finishing
        val nightShiftEnd = nightShiftDetails.filter { it.detailEnd.toLocalDate() == date }.minBy { it.detailEnd }?.let {
            DetailEventDto(
                    it.activity,
                    it.detailStart,
                    it.detailEnd,
                    it.shiftType,
                    when (it.shiftType) {
                        ShiftType.OVERTIME -> {
                            TaskDisplayType.OVERTIME_NIGHT_FINISH
                        }
                        else -> {
                            TaskDisplayType.NIGHT_FINISH
                        }
                    },
                    it.detailEnd,
                    calculateShiftDuration(nightShiftDetails))
        }

        return listOfNotNull(nightShiftEnd, nightShiftStart, dayShiftStart, dayShiftEnd)
    }

    private fun calculateShiftDuration(details: Collection<CsrDetailDto>): String {
        // We have to exclude unpaid breaks
        val sum = details.filter { detail -> detail.detailType != DetailType.BREAK }.map {
            detail ->
                Duration.between(
                detail.detailStart,
                detail.detailEnd).toSeconds()
        }.sum()
        return String.format("%dh %02dm", sum / 3600, (sum % 3600) / 60)
    }

    companion object {

        private val log = LoggerFactory.getLogger(ShiftService::class.java)
    }
}