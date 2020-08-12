package uk.gov.justice.digital.hmpps.cmd.api.service

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.cmd.api.client.CsrApiClient
import uk.gov.justice.digital.hmpps.cmd.api.client.ShiftTaskDto
import uk.gov.justice.digital.hmpps.cmd.api.domain.TaskDisplayType
import uk.gov.justice.digital.hmpps.cmd.api.domain.TaskType
import uk.gov.justice.digital.hmpps.cmd.api.dto.DayModelDto
import uk.gov.justice.digital.hmpps.cmd.api.dto.DayEventDto
import uk.gov.justice.digital.hmpps.cmd.api.dto.TaskEventDto
import uk.gov.justice.digital.hmpps.cmd.api.dto.TaskModelDto
import java.time.*
import java.util.*
import java.util.stream.Collectors

@Service
@Transactional
class ShiftService(val csrClient: CsrApiClient, val clock: Clock) {

    fun getShiftsBetween(fromParam: Optional<LocalDate>, toParam: Optional<LocalDate>): Collection<DayModelDto> {
        val start = fromParam.orElse(LocalDate.now(clock))
        val end = toParam.orElse(LocalDate.now(clock))

        val taskData = csrClient.getShiftTasks(start, end)
        val overtimeData = csrClient.getOvertimeShiftTasks(start, end)

        return (start.datesUntil(end.plusDays(1))).map { date ->
            DayModelDto(
                date,
                calculateShiftType(date, taskData + overtimeData),
                getShiftBoundaries(date, taskData) + getOvertimeBoundaries(date, overtimeData)
            )

        }.collect(Collectors.toList())
    }

    fun getTaskDetailFor(dateParam: Optional<LocalDate>): TaskModelDto {
        val date = dateParam.orElse(LocalDate.now(clock))

        val taskData = csrClient.getShiftTasks(date, date)
        val overtimeData = csrClient.getOvertimeShiftTasks(date, date)

        return TaskModelDto(
                date,
                calculateShiftType(date, taskData),
                getShiftTasks(date, taskData) + getOvertimeTasks(date, overtimeData)
        )

    }

    /* For each day we are looking for :
     * 1) the earliest start date with a finish date on the same day, this is a day shift start
     * 2) the latest finish date with a start date on the same day, this is a day shift end
     * 3) the latest start date, if the end date is on a different day we have a night shift start.
     * 4) the earliest finish date, if the start date is a different day we have a night shift end.
     *  Then to calculate the duration line we look for finish types, and add up everything in that shift
     */
    private fun getShiftBoundaries(date: LocalDate, tasks: Collection<ShiftTaskDto>) : Collection<DayEventDto> {

        // first is day shifts, second is night shifts
        val shiftTypes = tasks
                .filter { eventHappensOnDate(it, date) }
                .filter { it.start.toLocalTime() != LocalTime.MIN }
                .partition { it.start.toLocalDate() == it.end.toLocalDate() }

        val shiftStart = shiftTypes.first.minBy { it.start }?.let {
            DayEventDto(it.start.toLocalTime(), TaskDisplayType.DAY_START.value)
        }
        val shiftEnd = shiftTypes.first.maxBy { it.end }?.let {
            DayEventDto(it.end.toLocalTime(), TaskDisplayType.DAY_FINISH.value,
                    calculateShiftDuration(shiftTypes.first))
        }
        val nightShiftStart = shiftTypes.second.filter { it.start.toLocalDate() == date }.maxBy { it.start }?.let {
            DayEventDto(it.start.toLocalTime(), TaskDisplayType.NIGHT_START.value)
        }
        val nightShiftEnd = shiftTypes.second.filter { it.end.toLocalDate() == date }.minBy { it.end }?.let {
            DayEventDto(it.end.toLocalTime(), TaskDisplayType.NIGHT_FINISH.value,
                    calculateShiftDuration(shiftTypes.second.filter { s -> s.start.isBefore(it.end) }))
        }

        return listOfNotNull(nightShiftEnd, nightShiftStart, shiftStart, shiftEnd)
    }

    private fun getOvertimeBoundaries(date: LocalDate, tasks: Collection<ShiftTaskDto>) : Collection<DayEventDto> {
        return getShiftBoundaries(date, tasks).map {
            it.displayType = translateToOvertimeTypes(it.displayType)!!
            it
        }

    }

    private fun getShiftTasks(date: LocalDate, tasks: Collection<ShiftTaskDto>) : Collection<TaskEventDto> {

        // first is day shifts, second is night shifts
        val shiftCandidates = tasks
                .filter { eventHappensOnDate(it, date) }
                .filter { it.start.toLocalTime() != LocalTime.MIN }

        val shiftTypes = shiftCandidates.partition { it.start.toLocalDate() == it.end.toLocalDate() }

        val shiftStart = shiftTypes.first.minBy { it.start }?.let {
            TaskEventDto(it.activity, it.type, it.start, it.end, TaskDisplayType.DAY_START.value)
        }
        val shiftEnd = shiftTypes.first.maxBy { it.end }?.let {
            TaskEventDto(it.activity, it.type, it.start, it.end, TaskDisplayType.DAY_FINISH.value,
                    calculateShiftDuration(shiftTypes.first))
        }
        val nightShiftStart = shiftTypes.second.filter { it.start.toLocalDate() == date }.maxBy { it.start }?.let {
            TaskEventDto(it.activity, it.type, it.start, null, TaskDisplayType.NIGHT_START.value)
        }
        val nightShiftEnd = shiftTypes.second.filter { it.end.toLocalDate() == date }.minBy { it.end }?.let {
            TaskEventDto(it.activity, it.type, null, it.end, TaskDisplayType.NIGHT_FINISH.value,
                    calculateShiftDuration(shiftTypes.second.filter { s -> s.start.isBefore(it.end) }))
        }
        val shiftEvents = shiftCandidates.map {
            TaskEventDto(it.activity, it.type, it.start, it.end, null)
        }

        // We have the boundaries identified, so only add in the extras
        val boundaryEvents = listOfNotNull(nightShiftEnd, nightShiftStart, shiftStart, shiftEnd)
        val middleEvents = shiftEvents.filter { event ->
            boundaryEvents.all { event.start != it.start && event.end != it.end }
        }

        return boundaryEvents + middleEvents
    }

    private fun getOvertimeTasks(date: LocalDate, tasks: Collection<ShiftTaskDto>) : Collection<TaskEventDto> {
        return getShiftTasks(date, tasks).map {
            it.displayType = translateToOvertimeTypes(it.displayType)
            it
        }
    }

    private fun translateToOvertimeTypes(displayType: String?) : String? {
        return displayType?.let {
            when (TaskDisplayType.from(displayType)) {
                TaskDisplayType.DAY_START -> {
                    TaskDisplayType.OVERTIME_DAY_START.value
                }
                TaskDisplayType.DAY_FINISH -> {
                    TaskDisplayType.OVERTIME_DAY_FINISH.value
                }
                TaskDisplayType.NIGHT_START -> {
                    TaskDisplayType.OVERTIME_NIGHT_START.value
                }
                TaskDisplayType.NIGHT_FINISH -> {
                    TaskDisplayType.OVERTIME_NIGHT_FINISH.value
                }
                else -> {
                    displayType
                }
            }
        } ?: displayType
    }

    private fun calculateShiftType(date : LocalDate,tasks: Collection<ShiftTaskDto>): String {

        val todayTasks = tasks.filter { eventHappensOnDate(it, date) }
        if(todayTasks.any()) {
            val shiftStart = tasks.minBy { it.start }!!.start
            val isFullDay = shiftStart.toLocalTime() == LocalTime.of(0, 0, 0)
            todayTasks.forEach {
                if ((TaskType.UNSPECIFIC.descriptionEquals(it.type) && isFullDay) ||
                        TaskType.TRAINING_INTERNAL.descriptionEquals(it.activity) ||
                        TaskType.TRAINING_EXTERNAL.descriptionEquals(it.activity) ||
                        TaskType.ABSENCE.descriptionEquals(it.type)) {
                    return it.activity
                } else if (TaskType.HOLIDAY.descriptionEquals(it.type) && (isFullDay || !tasks.any { task -> TaskType.UNSPECIFIC.descriptionEquals(task.type) })) {
                    return TaskType.HOLIDAY.description
                } else if (TaskType.ILLNESS.descriptionEquals(it.type)) {
                    return TaskType.ILLNESS.description
                }
            }
            return TaskType.SHIFT.description

        } else {
            return TaskType.NONE.description

        }

    }

    companion object {

        private fun calculateShiftDuration(tasks: Collection<ShiftTaskDto>) : String {
            // We have to exclude unpaid breaks
            val sum = tasks.filter { task -> task.type.toLowerCase() != TaskType.BREAK.description.toLowerCase() }.map { task -> Duration.between(task.start, task.end).seconds }.sum()
            return String.format("%dh %02dm", sum / 3600, (sum % 3600) / 60)
        }

        private fun eventHappensOnDate(task: ShiftTaskDto, date : LocalDate) : Boolean { return task.start.toLocalDate() == date || task.end.toLocalDate() == date }

        private val log = LoggerFactory.getLogger(ShiftService::class.java)
    }
}
