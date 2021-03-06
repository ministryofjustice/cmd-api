package uk.gov.justice.digital.hmpps.cmd.api.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.cmd.api.client.CsrClient
import uk.gov.justice.digital.hmpps.cmd.api.client.CsrDetailDto
import uk.gov.justice.digital.hmpps.cmd.api.domain.DetailDisplayType
import uk.gov.justice.digital.hmpps.cmd.api.dto.DetailDto
import uk.gov.justice.digital.hmpps.cmd.api.dto.ShiftDto
import uk.gov.justice.digital.hmpps.cmd.api.security.AuthenticationFacade
import uk.gov.justice.digital.hmpps.cmd.api.uk.gov.justice.digital.hmpps.cmd.api.domain.DetailParentType
import uk.gov.justice.digital.hmpps.cmd.api.uk.gov.justice.digital.hmpps.cmd.api.domain.FullDayActivityType
import java.time.Clock
import java.time.Duration
import java.time.LocalDate
import java.time.LocalTime
import java.util.*
import java.util.stream.Collectors

@Service
class ShiftService(private val prisonService: PrisonService,
                   private val csrClient: CsrClient,
                   private val clock: Clock,
                   private val authenticationFacade : AuthenticationFacade) {

    fun getDetailsForUser(fromParam: Optional<LocalDate>, toParam: Optional<LocalDate>): Collection<ShiftDto> {
        val start = fromParam.orElse(LocalDate.now(clock))
        val end = toParam.orElse(LocalDate.now(clock))

        val detailsByDate: Map<LocalDate, Collection<CsrDetailDto>> = getDetailsGroupedByDate(start, end)

        // DatesUntil is exclusive of the end date, so we have to do plusDays(1)
        return start.datesUntil(end.plusDays(1)).map { date ->
            val details = detailsByDate.getOrDefault(date, listOf())
            val startAndFinishDetails = getStartAndFinishDetails(date, details)
            val middleDetails = getMiddleDetails(details, startAndFinishDetails)
            val fullDay = startAndFinishDetails.plus(middleDetails)
            val fullDayActivity = calculateFullDayActivity(fullDay)
            ShiftDto(
                    date,
                    FullDayActivityType.from(fullDayActivity), // This value can be different from the value below where we don't care about the activity e.g DetailType.SHIFT & "Secondment" rather than REST_DAY & "rest day"
                    fullDayActivity,
                    fullDay
            )
        }.collect(Collectors.toList())
    }

    /*
    Group the details by date, both by start and end date.
    A night shift will be in two buckets.
     */
    private fun getDetailsGroupedByDate(start: LocalDate, end: LocalDate): Map<LocalDate, Collection<CsrDetailDto>> {
        val region = prisonService.getPrisonForUser()?.region
        val details = csrClient.getDetailsForUser(start, end, region, authenticationFacade.currentUsername)
        val detailStartGroup = details.groupBy { it.detailStart.toLocalDate() }
        val detailEndGroup = details.groupBy { it.detailEnd.toLocalDate() }
        return (detailStartGroup.keys + detailEndGroup.keys)
                .associateWith {
                    detailStartGroup.getOrDefault(it, listOf())
                            .plus(detailEndGroup.getOrDefault(it, listOf()))
                            .distinct()
                }
    }

    /*
    We're not convinced this logic is correct, but it replicates the behaviour of the legacy service.
     */
    private fun calculateFullDayActivity(tasks: Collection<DetailDto>): String {
        // tasks here is on purpose,
        // we want to discount overtime,
        // but if there are only overtime values the activity isn't 'NONE'
        if (tasks.any()) {
            val shiftTasks = tasks.filter { it.detail == DetailParentType.SHIFT }
            shiftTasks.forEach {
                val activity = FullDayActivityType.from(it.activity!!)
                if (
                   (activity == FullDayActivityType.REST_DAY && onlyHasBreaksOrNightShiftFinish(shiftTasks, FullDayActivityType.REST_DAY)) ||
                   (activity == FullDayActivityType.HOLIDAY && onlyHasBreaksOrNightShiftFinish(shiftTasks, FullDayActivityType.HOLIDAY)) ||
                   (activity == FullDayActivityType.ILLNESS) ||
                   (activity == FullDayActivityType.ABSENCE && onlyHasBreaksOrNightShiftFinish(shiftTasks, FullDayActivityType.ABSENCE)) ||
                   (activity == FullDayActivityType.TU_OFFICIALS_LEAVE_DAYS) ||
                   (activity == FullDayActivityType.TU_OFFICIALS_LEAVE_HOURS) ||
                   (activity == FullDayActivityType.TOIL && isStartType(it.displayType)) ||
                   (activity == FullDayActivityType.SECONDMENT && isStartType(it.displayType)) ||
                   (activity == FullDayActivityType.TRAINING_INTERNAL && isStartType(it.displayType)) ||
                   (activity == FullDayActivityType.TRAINING_EXTERNAL && isStartType(it.displayType))
                ) {
                    return activity.description
                }
            }
            return FullDayActivityType.SHIFT.description

        } else {
            return FullDayActivityType.NONE.description
        }
    }

    private fun isStartType(displayType: DetailDisplayType?) : Boolean {
        return (displayType == DetailDisplayType.DAY_START || displayType == DetailDisplayType.NIGHT_START)
    }

    private fun onlyHasBreaksOrNightShiftFinish(tasks: Collection<DetailDto>, type : FullDayActivityType) : Boolean {
        return tasks.none {
            it.displayType != DetailDisplayType.NIGHT_FINISH &&
            FullDayActivityType.from(it.activity!!) != type &&
            FullDayActivityType.from(it.activity) != FullDayActivityType.BREAK
        }
    }

    private fun getMiddleDetails(details : Collection<CsrDetailDto>, startAndFinishDetails : Collection<DetailDto>): Collection<DetailDto> {

        // The middle details are the fullDetails - the start and finish details
        return details
                .filter { detail ->
                    startAndFinishDetails.all { sfd -> detail.detailStart != sfd.start && detail.detailEnd != sfd.end }
                }
                .map {
                    DetailDto(
                            it.activity,
                            it.detailStart,
                            it.detailEnd,
                            it.shiftType,
                            null
                    )
                }
    }

    /* For each day we are looking for :
     * 1) the earliest start date with a finish date on the same day, this is a day shift start
     * 2) the latest finish date with a start date on the same day, this is a day shift end
     * 3) the latest start date, if the end date is on a different day we have a night shift start.
     * 4) the earliest finish date, if the start date is a different day we have a night shift end.
     */
    private fun getStartAndFinishDetails(date: LocalDate, details: Collection<CsrDetailDto>): Collection<DetailDto> {
        // We need to separate out overtime and shift details because we need to identify the start and finish of each
        val (shiftDetails, overtimeDetails) = details.filter {
            // Remove full day details because we won't be showing the start and end
            it.detailStart.toLocalTime() != LocalTime.MIDNIGHT && it.detailEnd.toLocalTime() != LocalTime.MIDNIGHT
        }.partition { it.shiftType == DetailParentType.SHIFT }

        return listOf(shiftDetails, overtimeDetails).map { csrDetails ->
            val (dayShiftDetails, nightShiftDetails) = csrDetails
                    .partition { it.detailStart.toLocalTime() < it.detailEnd.toLocalTime() && it.detailStart.toLocalDate() == it.detailEnd.toLocalDate() }

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
                         Nightshift finish works differently,
                         we need to calculate the duration between
                         nightshift_start and nightshift_finish only.
                         because the collection has multiple day's data in it
                         if we use calculateShiftDuration() it will be wrong.
                        */
                        Duration.between(it.detailStart, it.detailEnd).toSeconds())
            }

            listOfNotNull(nightShiftEnd, nightShiftStart, dayShiftStart, dayShiftEnd)
        }.flatten()
    }

    private fun calculateShiftDuration(details: Collection<CsrDetailDto>): Long {
        // We have to exclude unpaid breaks
        return details.filter { detail -> FullDayActivityType.from(detail.activity) != FullDayActivityType.BREAK }.map {
            detail ->
                Duration.between(
                detail.detailStart,
                detail.detailEnd).toSeconds()
        }.sum()
    }

}