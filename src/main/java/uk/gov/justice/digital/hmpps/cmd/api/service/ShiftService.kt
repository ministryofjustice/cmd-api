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
import java.time.*
import java.util.*

@Service
@Transactional
class ShiftService(val csrClient: CsrApiClient, val clock: Clock) {

    fun getShiftsBetween(fromParam: Optional<LocalDate>, toParam: Optional<LocalDate>): Collection<DayModelDto> {
        val start = fromParam.orElse(LocalDate.now(clock))
        val end = fromParam.orElse(LocalDate.now(clock))
        val shifts = csrClient.getShiftTasks(start, end)

        //val overtime = csrClient.getOvertimeShiftTasks(start, end)

        return getShiftDisplayData(shifts)
    }

    fun getShiftFor(dateParam: Optional<LocalDate>): Collection<DayEventDto> {
        return getShiftsBetween(dateParam,dateParam).flatMap { it.tasks }
    }

    private fun getShiftDisplayData(tasks: Collection<ShiftTaskDto>): Collection<DayModelDto> {

        /*
        * We don't want to group by shift date,
        * we want things to be grouped by start & end date instead
        * as one task might be need to shown on multiple days
        */
        val dayStartTasks = tasks.groupBy { it.start.toLocalDate() }
        val dayEndTasks = tasks.groupBy { it.end.toLocalDate() }
        val taskListGroups: Map<LocalDate, List<ShiftTaskDto>> = (dayStartTasks.keys + dayEndTasks.keys).associateWith {
            ((dayStartTasks[it]?.toList() ?: listOf()) + (dayEndTasks[it]?.toList() ?: listOf())).distinct()
        }

        /*
        * We now have a map of days with a list of all events that have either start times or finish times on that day.
        * We need to work out which ones we need to show on the main calendar page.
        * For each day we are looking for :
        * 1) the earliest finish date, if the start date is a different day we have a night shift end.
        * 2) the latest start date, if the end date is on a different day we have a night shift start.
        * 3) the earliest start date with a finish date on the same day, this is a day shift start
        * 4) the latest finish date with a start date on the same day, this is a day shift end
        *  Then to calculate the duration line we look for finish types, and add up everything in that shift
        */
        val taskMap = taskListGroups.mapValues { taskEntry ->
            val shiftTypes = taskEntry.value.partition{ it.start.toLocalDate() == it.end.toLocalDate() }

            val nightShiftEnd = shiftTypes.second.filter { it.end.toLocalDate() == taskEntry.key }.minBy { it.end }?.let{
                DayEventDto (it.date, it.activity, it.type, it.end, TaskDisplayType.NIGHT_FINISH.value,
                        calculateShiftDuration(shiftTypes.second.filter {s -> s.start.isBefore(it.end) }))
            }
            val nightShiftStart = shiftTypes.second.filter { it.start.toLocalDate() == taskEntry.key }.maxBy { it.start }?.let{
                DayEventDto (it.date, it.activity, it.type, it.start, TaskDisplayType.NIGHT_START.value)
            }
            val shiftStart = shiftTypes.first.minBy{ it.start }?.let{
                DayEventDto (it.date, it.activity, it.type, it.start, TaskDisplayType.DAY_START.value)
            }
            val shiftEnd = shiftTypes.first.maxBy { it.end }?.let{
                DayEventDto (it.date, it.activity, it.type, it.end, TaskDisplayType.DAY_FINISH.value,
                        calculateShiftDuration(shiftTypes.first))
            }



            listOfNotNull(nightShiftEnd, shiftStart, shiftEnd, nightShiftStart)
        }

        return taskMap.map { tasks ->
            //val fullDayShift = getFullDayShift(task.value)
            DayModelDto(
                    date = tasks.key,
                    fullDayType = "",//fullDayType = fullDayShift?.type ?: TaskType.SHIFT.value,
                    fullDayDescription = "",//fullDayDescription = fullDayShift?.activity ?: TaskType.SHIFT.description,
                    tasks = tasks.value
            )
        }
    }

    //private fun calculateTypes(tasks: Collection<ShiftTaskDto>): Collection<DayEventDto> {
         //return            tasks.map { task ->
          //   DayEventDto(
         //            date = task.date,
          //           label = task.activity,
          //           taskType = task.type,
           //          time = task.start) }
    //}

    private fun calculateShiftDuration(tasks: Collection<ShiftTaskDto>) : String {
        // We have to exclude unpaid breaks
        val sum = tasks.filter { task -> task.type.toLowerCase() != TaskType.BREAK.description.toLowerCase() }.map { task -> Duration.between(task.start, task.end).seconds }.sum()
        return String.format("%dh:%02dm", sum / 3600, (sum % 3600) / 60)
    }


    private fun getFullDayShift(tasks: Collection<ShiftTaskDto>): ShiftTaskDto? {
        return tasks.firstOrNull { it.type != TaskType.SHIFT.description }
    }

    companion object {
        private val log = LoggerFactory.getLogger(ShiftService::class.java)
    }
}
