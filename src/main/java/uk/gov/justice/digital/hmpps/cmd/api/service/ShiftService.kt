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
import uk.gov.justice.digital.hmpps.cmd.api.uk.gov.justice.digital.hmpps.cmd.api.domain.EntityType
import java.time.Clock
import java.time.Duration
import java.time.LocalDate
import java.time.LocalTime
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
                    getAllEvents(date, detailsForDate)
            )
        }.collect(Collectors.toList())
    }

    private fun groupDetailsByDate(details : Collection<CsrDetailDto>): Map<LocalDate, Collection<CsrDetailDto>> {
        val detailStartGroup = details.groupBy { it.shiftDate }
        // a detail with an end earlier than start means it finishes on the next day (night shift)
        val detailEndGroup = details.filter { it.end < it.start }.groupBy { it.shiftDate.plusDays(1) }
        return (detailStartGroup.keys + detailEndGroup.keys)
                .associateWith {
                    detailStartGroup.getOrDefault(it, listOf()) +
                            detailEndGroup.getOrDefault(it, listOf())
                }
    }

    private fun calculateFullDayType(tasks: Collection<CsrDetailDto>): String {
        if (tasks.any()) {
            val isFullDay = tasks.minBy { it.start }?.start == 0L
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
        val (shiftDetails, overtimeDetails) = csrDetails.partition { it.entityType == EntityType.SHIFT }

        val shiftStartAndFinishEvents = getStartAndFinishEvents(date, shiftDetails)
        val overtimeStartAndFinishEvents = getStartAndFinishEvents(date, overtimeDetails)
        val startAndFinishEvents = (shiftStartAndFinishEvents + overtimeStartAndFinishEvents)

        val middleEvents = csrDetails
                .map {
                    DetailEventDto(
                            it.activity,
                            LocalTime.ofSecondOfDay(it.start),
                            LocalTime.ofSecondOfDay(it.end),
                            it.entityType,
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
                .filter { it.start != 0L }
                .partition { it.start < it.end }

        // Identify a Day Shift Starting
        val dayShiftStart = dayShiftDetails.minBy { it.start }?.let {
            DetailEventDto(
                    it.activity,
                    LocalTime.ofSecondOfDay(it.start),
                    LocalTime.ofSecondOfDay(it.end),
                    it.entityType,
                    when (it.entityType) {
                        EntityType.OVERTIME -> {
                            TaskDisplayType.OVERTIME_DAY_START
                        }
                        else -> {
                            TaskDisplayType.DAY_START
                        }
                    },
                    LocalTime.ofSecondOfDay(it.start))
        }

        // Identify a Day Shift Finishing
        val dayShiftEnd = dayShiftDetails.maxBy { it.end }?.let {
            DetailEventDto(
                    it.activity,
                    LocalTime.ofSecondOfDay(it.start),
                    LocalTime.ofSecondOfDay(it.end),
                    it.entityType,
                    when (it.entityType) {
                        EntityType.OVERTIME -> {
                            TaskDisplayType.OVERTIME_DAY_FINISH
                        }
                        else -> {
                            TaskDisplayType.DAY_FINISH
                        }
                    },
                    LocalTime.ofSecondOfDay(it.end),
                    calculateShiftDuration(it.shiftDate, dayShiftDetails))
        }

        // Identify a Night Shift Starting
        val nightShiftStart = nightShiftDetails.filter { it.shiftDate == date }.maxBy { it.start }?.let {
            DetailEventDto(
                    it.activity,
                    LocalTime.ofSecondOfDay(it.start),
                    LocalTime.ofSecondOfDay(it.end),
                    it.entityType,
                    when (it.entityType) {
                        EntityType.OVERTIME -> {
                            TaskDisplayType.OVERTIME_NIGHT_START
                        }
                        else -> {
                            TaskDisplayType.NIGHT_START
                        }
                    },
                    LocalTime.ofSecondOfDay(it.start))
        }

        // Identify a Night Shift Finishing
        val nightShiftEnd = nightShiftDetails.filter { it.shiftDate.plusDays(1) == date }.minBy { it.end }?.let {
            DetailEventDto(
                    it.activity,
                    LocalTime.ofSecondOfDay(it.start),
                    LocalTime.ofSecondOfDay(it.end),
                    it.entityType,
                    when (it.entityType) {
                        EntityType.OVERTIME -> {
                            TaskDisplayType.OVERTIME_NIGHT_FINISH
                        }
                        else -> {
                            TaskDisplayType.NIGHT_FINISH
                        }
                    },
                    LocalTime.ofSecondOfDay(it.end),
                    //TODO: Do we need to filter this here? why not the day shift too?
                    calculateShiftDuration(it.shiftDate, nightShiftDetails))
        }

        return listOfNotNull(nightShiftEnd, nightShiftStart, dayShiftStart, dayShiftEnd)
    }

    private fun calculateShiftDuration(startDate: LocalDate, details: Collection<CsrDetailDto>): String {
        // We have to exclude unpaid breaks
        val sum = details.filter { detail -> detail.detailType != DetailType.BREAK }.map {
            detail ->
            if(detail.start < detail.end) {
                detail.end - detail.start
            } else {
                Duration.between(
                startDate.atTime(LocalTime.ofSecondOfDay(detail.start)),
                startDate.plusDays(1).atTime(LocalTime.ofSecondOfDay(detail.end))).toSeconds()
            }
        }.sum()
        return String.format("%dh %02dm", sum / 3600, (sum % 3600) / 60)
    }

    companion object {

        private val log = LoggerFactory.getLogger(ShiftService::class.java)
    }
}