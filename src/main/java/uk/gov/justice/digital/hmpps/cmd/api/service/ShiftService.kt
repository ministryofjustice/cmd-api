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
import java.time.LocalDate
import java.time.LocalTime
import java.util.*
import java.util.stream.Collectors

@Service
@Transactional
class ShiftService(private val prisonService: PrisonService,
                   private val csrClient: CsrClient,
                   private val clock: Clock) {

    fun getShiftsForUserBetween(fromParam: Optional<LocalDate>, toParam: Optional<LocalDate>): Collection<DetailDto> {
        val start = fromParam.orElse(LocalDate.now(clock))
        val end = toParam.orElse(LocalDate.now(clock))
        val region = prisonService.getPrisonForUser()?.region

        val detailsData = csrClient.getDetailsForUser(start, end, region)

        return start.datesUntil(end.plusDays(1)).map { date ->
            val detailData = detailsData.filter { detailHappensOnDate(it, date) }
            DetailDto(
                    date,
                    calculateFullDayType(detailData),
                    getSignificantEvents(date, detailData)
            )
        }.collect(Collectors.toList())
    }

    fun getTaskDetailFor(dateParam: Optional<LocalDate>): DetailDto {
        val date = dateParam.orElse(LocalDate.now(clock))
        val region = prisonService.getPrisonForUser()?.region

        val detailData = csrClient.getDetailsForUser(date, date, region)

        return DetailDto(
                date,
                calculateFullDayType(detailData),
                getAllEvents(date, detailData)
        )
    }

    /* For each day we are looking for :
     * 1) the earliest start date with a finish date on the same day, this is a day shift start
     * 2) the latest finish date with a start date on the same day, this is a day shift end
     * 3) the latest start date, if the end date is on a different day we have a night shift start.
     * 4) the earliest finish date, if the start date is a different day we have a night shift end.
     */
    private fun getSignificantEvents(date: LocalDate, csrDetails: Collection<CsrDetailDto>): Collection<DetailEventDto> {

        val (dayShiftDetails, nightShiftDetails) = csrDetails
                .filter { it.start != 0L }
                .partition { it.start > it.end }

        // Identify a Day Shift Starting
        val dayShiftStart = dayShiftDetails.minBy { it.start }?.let {
            DetailEventDto(
                    it.activity,
                    LocalTime.ofSecondOfDay(it.start),
                    LocalTime.ofSecondOfDay(it.end),
                    when (EntityType.from(it.entityType)) {
                        EntityType.OVERTIME -> {
                            TaskDisplayType.OVERTIME_DAY_START
                        }
                        else -> {
                            TaskDisplayType.DAY_START
                        }
                    })
        }

        // Identify a Day Shift Finishing
        val dayShiftEnd = dayShiftDetails.maxBy { it.end }?.let {
            DetailEventDto(
                    it.activity,
                    LocalTime.ofSecondOfDay(it.start),
                    LocalTime.ofSecondOfDay(it.end),
                    when (EntityType.from(it.entityType)) {
                        EntityType.OVERTIME -> {
                            TaskDisplayType.OVERTIME_DAY_FINISH
                        }
                        else -> {
                            TaskDisplayType.DAY_FINISH
                        }
                    },
                    calculateShiftDuration(dayShiftDetails))
        }

        // Identify a Night Shift Starting
        val nightShiftStart = nightShiftDetails.filter { it.shiftDate == date }.maxBy { it.start }?.let {
            DetailEventDto(
                    it.activity,
                    LocalTime.ofSecondOfDay(it.start),
                    null,
                    when (EntityType.from(it.entityType)) {
                        EntityType.OVERTIME -> {
                            TaskDisplayType.OVERTIME_NIGHT_START
                        }
                        else -> {
                            TaskDisplayType.NIGHT_START
                        }
                    })
        }

        // Identify a Night Shift Finishing
        val nightShiftEnd = nightShiftDetails.filter { it.shiftDate.plusDays(1) == date }.minBy { it.end }?.let {
            DetailEventDto(
                    it.activity,
                    null,
                    LocalTime.ofSecondOfDay(it.end),
                    when (EntityType.from(it.entityType)) {
                        EntityType.OVERTIME -> {
                            TaskDisplayType.OVERTIME_NIGHT_FINISH
                        }
                        else -> {
                            TaskDisplayType.NIGHT_FINISH
                        }
                    },
                    //TODO: Do we need to filter this here? why not the day shift too?
                    calculateShiftDuration(nightShiftDetails.filter { s -> s.start < it.end }))
        }

        return listOfNotNull(nightShiftEnd, nightShiftStart, dayShiftStart, dayShiftEnd)
    }

    private fun getAllEvents(date: LocalDate, csrDetails: Collection<CsrDetailDto>): Collection<DetailEventDto> {
        val boundaryEvents = getSignificantEvents(date, csrDetails)
        // We have the boundaries identified, so only add in the extras
        val middleEvents = csrDetails
                .map {
                    DetailEventDto(
                            it.activity,
                            LocalTime.ofSecondOfDay(it.start),
                            LocalTime.ofSecondOfDay(it.end),
                            null)
                }.filter { event ->
                    boundaryEvents.all { event.start != it.start && event.end != it.end }
                }
        return boundaryEvents + middleEvents
    }

    private fun calculateFullDayType(tasks: Collection<CsrDetailDto>): String {

        if (tasks.any()) {
            val isFullDay = tasks.map { it.start }.minBy { it } == 0L
            tasks.forEach {

                val detailType = DetailType.from(it.detailType)
                val activityType = ActivityType.fromDescription(it.activity)

                if ((detailType == DetailType.UNSPECIFIC && isFullDay) ||
                        detailType == DetailType.ABSENCE ||
                        activityType == ActivityType.TRAINING_INTERNAL ||
                        activityType == ActivityType.TRAINING_EXTERNAL) {
                    return activityType.description
                } else if (detailType == DetailType.ILLNESS ||
                        (detailType == DetailType.HOLIDAY && (!isFullDay || !tasks.any { task -> DetailType.from(task.detailType) == DetailType.UNSPECIFIC }))) {
                    return detailType.description
                }
            }
            return ActivityType.SHIFT.description

        } else {
            return ActivityType.NONE.description
        }
    }

    private fun calculateShiftDuration(tasks: Collection<CsrDetailDto>): String {
        // We have to exclude unpaid breaks
        val sum = tasks.filter { task -> DetailType.from(task.entityType) != DetailType.BREAK }.map { task -> task.end - task.start }.sum()
        return String.format("%dh %02dm", sum / 3600, (sum % 3600) / 60)
    }

    private fun detailHappensOnDate(task: CsrDetailDto, date: LocalDate): Boolean {
        // Shift Date is always tied to start time.
        return when {
            task.shiftDate == date -> {
                true
            }
            task.end < task.start -> {
                task.shiftDate.plusDays(1) == date
            }
            else -> {
                false
            }
        }
    }

    companion object {

        private val log = LoggerFactory.getLogger(ShiftService::class.java)
    }
}