package uk.gov.justice.digital.hmpps.cmd.api.uk.gov.justice.digital.hmpps.cmd.api.service

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.cmd.api.uk.gov.justice.digital.hmpps.cmd.api.client.CsrApiClient
import uk.gov.justice.digital.hmpps.cmd.api.uk.gov.justice.digital.hmpps.cmd.api.client.ShiftTaskDto
import uk.gov.justice.digital.hmpps.cmd.api.uk.gov.justice.digital.hmpps.cmd.api.domain.TaskType
import uk.gov.justice.digital.hmpps.cmd.api.uk.gov.justice.digital.hmpps.cmd.api.dto.DayModelDto
import uk.gov.justice.digital.hmpps.cmd.api.uk.gov.justice.digital.hmpps.cmd.api.dto.TaskModelDto
import java.time.*
import java.time.temporal.TemporalAmount
import java.util.*

@Service
@Transactional
class ShiftService(val csrClient: CsrApiClient, val clock: Clock) {

    fun getShiftsBetween(fromParam: Optional<LocalDate>, toParam: Optional<LocalDate>): Collection<DayModelDto> {
        val start = fromParam.orElse(LocalDate.now(clock))
        val end = fromParam.orElse(LocalDate.now(clock))
        val shifts = csrClient.getShiftTasks(start, end).groupBy { it.date }
        return getShiftDisplayData(shifts)
    }

    fun getShiftFor(dateParam: Optional<LocalDate>): Collection<TaskModelDto> {
        val date = dateParam.orElse(LocalDate.now(clock))
        val shifts = csrClient.getShiftTasks(date, date).groupBy { it.date }
        return getShiftDisplayData(shifts).filter { it.date == date }.flatMap { it.tasks }
    }

    fun getOvertimeShiftsBetween(fromParam: Optional<LocalDate>, toParam: Optional<LocalDate>): Collection<DayModelDto> {
        val start = fromParam.orElse(LocalDate.now(clock))
        val end = fromParam.orElse(LocalDate.now(clock))
        val shifts = csrClient.getOvertimeShiftTasks(start, end).groupBy { it.date }
        return getShiftDisplayData(shifts)
    }

    fun getOvertimeShiftFor(dateParam: Optional<LocalDate>): Collection<TaskModelDto> {
        val date = dateParam.orElse(LocalDate.now(clock))
        val shifts = csrClient.getOvertimeShiftTasks(date, date).groupBy { it.date }
        return getShiftDisplayData(shifts).filter { it.date == date }.flatMap { it.tasks }
    }

    /*
        Convert a list of shifts into details to show on each day.
        This is a port of the existing logic (with some small improvements)
        from the legacy prison-officer-diary. we're not happy with this code
        but there is high risk of regression because the legacy code didn't have
        adequate test coverage.
        We agreed to 1) lift and shift 2) improve, this represents phase one
        and not what we consider to be good work.
     */
    private fun getShiftDisplayData(shifts: Map<LocalDate, Collection<ShiftTaskDto>>): Collection<DayModelDto> {
        return shifts.map { shift ->
            // We need the previous shift and next shift to work out
            // the correct way to display the shift start and finish boxes
            val previousShift = shifts[shift.key.minusDays(1)]
            val nextShift = shifts[shift.key.plusDays(1)]

            // The start date for the overall shift is the earliest start date of the collection of tasks
            val shiftStart = shift.value.minBy(ShiftTaskDto::start)!!.start
            // The end date for the overall shift is the latest end date of the collection of tasks
            val shiftEnd = shift.value.maxBy(ShiftTaskDto::end)!!.end
            // The shift type can be determined by some combination of task types and whether or not it's an all day task.
            val shiftType = calculateShiftType(shift.value, shiftStart)

            DayModelDto(
                    date = shift.key,
                    dailyStartDateTime = calculateDisplayStartDateTime(nextShift, shiftStart, shiftEnd, shiftType),
                    dailyEndDateTime = calculateDisplayEndDateTime(previousShift, shiftStart, shiftEnd),
                    type = shiftType,
                    startDateTime = shiftStart,
                    endDateTime = shiftEnd,
                    durationInSeconds = calculateShiftDuration(shift.value),
                    tasks = getTaskDisplayData(shift.value, nextShift, previousShift)
            )
        }
    }

    private fun getTaskDisplayData(tasks: Collection<ShiftTaskDto>, nextShift: Collection<ShiftTaskDto>?, previousShift: Collection<ShiftTaskDto>?): Collection<TaskModelDto> {
        return tasks.map { task ->
            val allDayEvent = task.start.toLocalDate() == task.end.toLocalDate()
            TaskModelDto(
                    date = task.date,
                    dailyStartDateTime = if(!allDayEvent) calculateDisplayStartDateTime(nextShift, task.start, task.end, task.type) else null,
                    dailyEndDateTime = if(!allDayEvent) calculateDisplayEndDateTime(previousShift, task.start, task.end) else null,
                    label = task.activity,
                    type = task.type,
                    startDateTime = task.start,
                    endDateTime = task.end)
        }
    }

    private fun calculateShiftDuration(tasks: Collection<ShiftTaskDto>) =
            // We have to exclude unpaid breaks
            tasks.filter { task -> task.type.toLowerCase() != TaskType.BREAK.description.toLowerCase() }.map { task -> Duration.between(task.start, task.end).seconds }.sum()

    // This is suspicious, why does this work for both shifts and tasks?!?!
    private fun calculateDisplayEndDateTime(previousShift: Collection<ShiftTaskDto>?, eventStart: LocalDateTime, eventEnd: LocalDateTime) : LocalDateTime? {
        return previousShift?.let {
            val previousShiftStart = previousShift.minBy(ShiftTaskDto::start)!!.start
            val previousShiftEnd = previousShift.maxBy(ShiftTaskDto::end)!!.end
            val previousShiftType = calculateShiftType(previousShift, previousShiftStart)

            return when {
                eventStart.toLocalDate() == eventEnd.toLocalDate() && previousShiftEnd.toLocalDate() == eventStart.toLocalDate() -> {
                    previousShiftEnd
                }
                eventStart.toLocalDate() == eventEnd.toLocalDate() -> {
                    eventEnd
                }
                eventEnd.toLocalDate() != previousShiftEnd.toLocalDate() && Period.between(previousShiftEnd.toLocalDate(), eventEnd.toLocalDate()).days > 2 -> {
                    null
                }
                eventEnd.toLocalDate() != previousShiftEnd.toLocalDate() && previousShiftType.toLowerCase() == TaskType.SHIFT.description.toLowerCase() -> {
                    previousShiftEnd
                }
                else -> {
                    null
                }
            }
        }
    }

    // This is suspicious, why does this work for both shifts and tasks?!?!
    private fun calculateDisplayStartDateTime(nextShift: Collection<ShiftTaskDto>?, eventStart: LocalDateTime, eventEnd: LocalDateTime, eventType: String) : LocalDateTime? {
        return nextShift?.let {
            val nextShiftStart = nextShift.minBy(ShiftTaskDto::start)!!.start
            val nextShiftEnd = nextShift.maxBy(ShiftTaskDto::end)!!.end
            val nextShiftType = calculateShiftType(nextShift, nextShiftStart)

            // Some weird logic, no tests in the legacy code
            // They feel like specific bug fixes...
            return when {
                eventEnd.toLocalDate().isAfter(eventStart.toLocalDate()) && eventType.toLowerCase() == TaskType.SHIFT.description.toLowerCase() -> {
                    return eventStart
                }
                nextShiftType.toLowerCase() == TaskType.REST_DAY.description.toLowerCase() &&
                        eventStart.toLocalDate() == eventEnd.toLocalDate() &&
                        eventEnd.toLocalTime() == LocalTime.of(23, 59, 59) &&
                        eventStart.toLocalTime() != LocalTime.of(0, 0, 0) -> {
                    null
                }
                eventStart.toLocalDate() == eventEnd.toLocalDate() -> {
                    eventStart
                }
                eventStart.toLocalDate() != nextShiftStart.toLocalDate() && nextShiftStart == nextShiftEnd -> {
                    null
                }
                eventStart.toLocalDate() != eventEnd.toLocalDate() && nextShiftType.toLowerCase() != TaskType.SHIFT.description.toLowerCase() -> {
                    null
                }
                else -> {
                    eventStart
                }
            }
        }
    }

    // This is copied from the old code and not refactored yet.
    private fun calculateShiftType(tasks: Collection<ShiftTaskDto>, shiftStart: LocalDateTime): String {

        tasks.forEach {
            when (it.type.toLowerCase()) {
                TaskType.ILLNESS.description.toLowerCase() -> {
                    return TaskType.ILLNESS.description
                }
                TaskType.HOLIDAY.description.toLowerCase() -> {
                    return if (it.start.toLocalTime() != LocalTime.of(0, 0, 0) && it.end.toLocalTime() != LocalTime.of(23, 59, 59)) {
                        TaskType.SHIFT.description
                    } else {
                        TaskType.HOLIDAY.description
                    }
                }
                TaskType.ABSENCE.description.toLowerCase() -> {
                    return if(it.activity.toLowerCase() == TaskType.REST_DAY.description.toLowerCase() && it.start.toLocalTime() != LocalTime.of(0, 0, 0) && it.end.toLocalTime() == LocalTime.of(23, 59, 59)) {
                        TaskType.REST_DAY.description
                    } else if(it.activity.toLowerCase() == TaskType.REST_DAY.description.toLowerCase() && it.start.toLocalTime() != LocalTime.of(0, 0, 0))  {
                        TaskType.SHIFT.description
                    } else {
                        it.activity
                    }
                }
                TaskType.UNSPECIFIC.description.toLowerCase() -> {
                    return if(tasks.any {task -> task.type.toLowerCase() == TaskType.HOLIDAY.description.toLowerCase() }) {
                        return TaskType.HOLIDAY.description
                    } else if (it.activity.toLowerCase() == TaskType.TRAINING_INTERNAL.description.toLowerCase() ||
                            it.activity.toLowerCase() == TaskType.TRAINING_EXTERNAL.description.toLowerCase() ||
                            shiftStart.toLocalTime() == LocalTime.of(0, 0, 0)) {
                        it.activity
                    } else {
                        TaskType.SHIFT.description
                    }
                }
            }
        }
        return TaskType.SHIFT.description
    }

    companion object {
        private val log = LoggerFactory.getLogger(ShiftService::class.java)
    }
}
