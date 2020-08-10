package uk.gov.justice.digital.hmpps.cmd.api.uk.gov.justice.digital.hmpps.cmd.api.service

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.cmd.api.uk.gov.justice.digital.hmpps.cmd.api.client.CsrApiClient
import uk.gov.justice.digital.hmpps.cmd.api.uk.gov.justice.digital.hmpps.cmd.api.client.ShiftTaskDto
import uk.gov.justice.digital.hmpps.cmd.api.uk.gov.justice.digital.hmpps.cmd.api.domain.TaskDisplayType
import uk.gov.justice.digital.hmpps.cmd.api.uk.gov.justice.digital.hmpps.cmd.api.domain.TaskType
import uk.gov.justice.digital.hmpps.cmd.api.uk.gov.justice.digital.hmpps.cmd.api.dto.DayModelDto
import uk.gov.justice.digital.hmpps.cmd.api.uk.gov.justice.digital.hmpps.cmd.api.dto.DayEventDto
import uk.gov.justice.digital.hmpps.cmd.api.uk.gov.justice.digital.hmpps.cmd.api.dto.TaskEventDto
import uk.gov.justice.digital.hmpps.cmd.api.uk.gov.justice.digital.hmpps.cmd.api.dto.TaskModelDto
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

        /*
         * We don't want to group by shift date,
         * we want things to be grouped by start & end date instead
         * as one task might be need to shown on multiple days
         */
        return (start.datesUntil(end.plusDays(1))).map { date ->
            val shiftTasks = getShiftBoundaries(date, taskData.filter { it.start.toLocalDate() == date || it.end.toLocalDate() == date })
            val overtimeTasks = getOvertimeBoundaries(date, overtimeData.filter { it.start.toLocalDate() == date || it.end.toLocalDate() == date })

            val fullDayType = calculateShiftType(taskData, taskData.minBy { it.start }!!.start)

            DayModelDto(
                    date = date,
                    fullDayType = fullDayType,
                    tasks = (shiftTasks + overtimeTasks)
            )

        }.collect(Collectors.toList())
    }

    fun getShiftFor(dateParam: Optional<LocalDate>): TaskModelDto {
        val date = dateParam.orElse(LocalDate.now(clock))

        val taskData = csrClient.getShiftTasks(date, date)
        val overtimeData = csrClient.getOvertimeShiftTasks(date, date)


        /*
         * We don't want to group by shift date,
         * we want things to be grouped by start & end date instead
         * as one task might be need to shown on multiple days
         */
        val shiftTasks = getShiftTasks(date, taskData.filter { it.start.toLocalDate() == date || it.end.toLocalDate() == date })
        val overtimeTasks = getOvertimeTasks(date, overtimeData.filter { it.start.toLocalDate() == date || it.end.toLocalDate() == date })

        val fullDayType = calculateShiftType(taskData, taskData.minBy { it.start }!!.start)

        return TaskModelDto(
                date = date,
                fullDayType = fullDayType,
                tasks = (shiftTasks + overtimeTasks)
        )

    }

    /* For each day we are looking for :
     * 1) the earliest finish date, if the start date is a different day we have a night shift end.
     * 2) the latest start date, if the end date is on a different day we have a night shift start.
     * 3) the earliest start date with a finish date on the same day, this is a day shift start
     * 4) the latest finish date with a start date on the same day, this is a day shift end
     *  Then to calculate the duration line we look for finish types, and add up everything in that shift
     */
    private fun getShiftBoundaries(date: LocalDate, tasks: Collection<ShiftTaskDto>) : Collection<DayEventDto> {
        getShiftBoundaries(date, tasks)
        getOvertimeBoundaries(date, tasks)
        // first is day shifts, second is night shifts
        val shiftTypes = tasks.filter { it.start.toLocalTime() != LocalTime.MIN }
                .partition { it.start.toLocalDate() == it.end.toLocalDate() }

        val nightShiftEnd = shiftTypes.second.filter { it.end.toLocalDate() == date }.minBy { it.end }?.let {
            DayEventDto(it.date, it.activity, it.type, it.end, TaskDisplayType.NIGHT_FINISH.value,
                    calculateShiftDuration(shiftTypes.second.filter { s -> s.start.isBefore(it.end) }))
        }
        val nightShiftStart = shiftTypes.second.filter { it.start.toLocalDate() == date }.maxBy { it.start }?.let {
            DayEventDto(it.date, it.activity, it.type, it.start, TaskDisplayType.NIGHT_START.value)
        }
        val shiftStart = shiftTypes.first.minBy { it.start }?.let {
            DayEventDto(it.date, it.activity, it.type, it.start, TaskDisplayType.DAY_START.value)
        }
        val shiftEnd = shiftTypes.first.maxBy { it.end }?.let {
            DayEventDto(it.date, it.activity, it.type, it.end, TaskDisplayType.DAY_FINISH.value,
                    calculateShiftDuration(shiftTypes.first))
        }
        return listOfNotNull(nightShiftEnd, nightShiftStart, shiftStart, shiftEnd)
    }

    private fun getOvertimeBoundaries(date: LocalDate, tasks: Collection<ShiftTaskDto>) : Collection<DayEventDto> {
        return getShiftBoundaries(date, tasks).map {
            val displayType = TaskDisplayType.from(it.displayType)
            it.displayType = when (displayType) {
                TaskDisplayType.DAY_START -> {
                    TaskDisplayType.OVERTIME_DAY_START.value
                }
                TaskDisplayType.DAY_FINISH -> {
                    TaskDisplayType.OVERTIME_DAY_FINISH.value
                }
                TaskDisplayType.OVERTIME_NIGHT_START -> {
                    TaskDisplayType.OVERTIME_NIGHT_START.value
                }
                TaskDisplayType.OVERTIME_DAY_FINISH -> {
                    TaskDisplayType.OVERTIME_DAY_FINISH.value
                }
                else -> { it.displayType}
            }
            it
        }
    }

    private fun getShiftTasks(date: LocalDate, tasks: Collection<ShiftTaskDto>) : Collection<TaskEventDto> {

        // first is day shifts, second is night shifts
        val shiftTypes = tasks.filter { it.start.toLocalTime() != LocalTime.MIN }
                .partition { it.start.toLocalDate() == it.end.toLocalDate() }

        val nightShiftEnd = shiftTypes.second.filter { it.end.toLocalDate() == date }.minBy { it.end }?.let {
            TaskEventDto(it.date, it.activity, it.type, it.end, TaskDisplayType.NIGHT_FINISH.value,
                    calculateShiftDuration(shiftTypes.second.filter { s -> s.start.isBefore(it.end) }))
        }
        val nightShiftStart = shiftTypes.second.filter { it.start.toLocalDate() == date }.maxBy { it.start }?.let {
            DayEventDto(it.date, it.activity, it.type, it.start, TaskDisplayType.NIGHT_START.value)
        }
        val shiftStart = shiftTypes.first.minBy { it.start }?.let {
            DayEventDto(it.date, it.activity, it.type, it.start, TaskDisplayType.DAY_START.value)
        }
        val shiftEnd = shiftTypes.first.maxBy { it.end }?.let {
            DayEventDto(it.date, it.activity, it.type, it.end, TaskDisplayType.DAY_FINISH.value,
                    calculateShiftDuration(shiftTypes.first))
        }
        return listOfNotNull(nightShiftEnd, nightShiftStart, shiftStart, shiftEnd)
    }

    private fun calculateShiftDuration(tasks: Collection<ShiftTaskDto>) : String {
        // We have to exclude unpaid breaks
        val sum = tasks.filter { task -> task.type.toLowerCase() != TaskType.BREAK.description.toLowerCase() }.map { task -> Duration.between(task.start, task.end).seconds }.sum()
        return String.format("%dh %02dm", sum / 3600, (sum % 3600) / 60)
    }

    private fun calculateShiftType(tasks: Collection<ShiftTaskDto>, shiftStart: LocalDateTime): String {
        val fullDay = shiftStart.toLocalTime() == LocalTime.of(0, 0, 0)

        tasks.forEach {
            if ((TaskType.UNSPECIFIC.descriptionEquals(it.type) && fullDay) ||
                    TaskType.TRAINING_INTERNAL.descriptionEquals(it.activity) ||
                    TaskType.TRAINING_EXTERNAL.descriptionEquals(it.activity)) {
                 return it.activity
            } else if(TaskType.ABSENCE.descriptionEquals(it.type)) {
                return if(TaskType.REST_DAY.descriptionEquals(it.activity) && fullDay) {
                    TaskType.REST_DAY.description
                } else {
                    it.activity
                }
            } else if(TaskType.HOLIDAY.descriptionEquals(it.type) && (fullDay || !tasks.any { task -> TaskType.UNSPECIFIC.descriptionEquals(task.type) } )) {
                return it.type
            } else if(TaskType.ILLNESS.descriptionEquals(it.type)) {
                return it.type
            }

        }

        return TaskType.SHIFT.description
    }

    companion object {
        private val log = LoggerFactory.getLogger(ShiftService::class.java)
    }
}
