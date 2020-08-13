package uk.gov.justice.digital.hmpps.cmd.api.client

import org.springframework.stereotype.Component
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId

@Component
class CsrApiClient {

    fun getShiftTasks(start : LocalDate, end : LocalDate ) : Collection<ShiftTaskDto> {
        return simpleDayShift
    }

    fun getOvertimeShiftTasks(start : LocalDate, end : LocalDate) : Collection<ShiftTaskDto> {
        return simpleDayShiftOvertime
    }

    companion object{
        val now = LocalDateTime.of(2020,8,3,0,0,0).atZone(ZoneId.systemDefault())
        val day1 = now.toLocalDate()
        val day2 = now.plusDays(1).toLocalDate()
        val day3 = now.plusDays(2).toLocalDate()
        val day4 = now.plusDays(3).toLocalDate()
        val day5 = now.plusDays(4).toLocalDate()

        val simpleDayShift = listOf(
                ShiftTaskDto(day1, "Unspecific", day1.atTime(7,15), day1.atTime(12,30), "Present"),
                ShiftTaskDto(day1, "Break", day1.atTime(12,30), day1.atTime(13,30), "Break (Unpaid)"),
                ShiftTaskDto(day1, "Unspecific", day1.atTime(13,30), day1.atTime(17,0), "Present"),

                ShiftTaskDto(day2, "Absence", day2.atTime(0,0), day2.atTime(23,59,59), "Rest Day"),

                ShiftTaskDto(day3, "Shift", day3.atTime(19,45), day4.atTime(6,45), "Night OSG"),

                ShiftTaskDto(day4, "Shift", day4.atTime(20,45), day5.atTime(7,45), "Night OSG"),

                ShiftTaskDto(day5, "Absence", day5.atTime(0,0), day5.atTime(23,59,59), "Rest Day")
                )

        val simpleDayShiftOvertime = listOf(
                ShiftTaskDto(day2, "Unspecific", day2.atTime(17,15), day2.atTime(18,30), "Present"),
                ShiftTaskDto(day2, "Break", day2.atTime(18,30), day2.atTime(19,30), "Break (Unpaid)"),
                ShiftTaskDto(day2, "Unspecific", day2.atTime(19,30), day2.atTime(22,0), "Present")
        )

        val simpleHolidayNightShift = listOf(
                ShiftTaskDto(day1, "Absence", day1.atTime(0,0), day1.atTime(23,59,59), "Rest Day"),
                ShiftTaskDto(day2, "Shift", day2.atTime(20,45), day3.atTime(7,45), "Night OSG"),
                ShiftTaskDto(day3, "Shift", day3.atTime(20,45), day4.atTime(7,45), "Night OSG")
        )

        val simpleHoliday = listOf(
                ShiftTaskDto(day1, "Absence", day1.atTime(7,45), day1.atTime(23,59,59), "Rest Day"),
                ShiftTaskDto(day2, "Holiday", day2.atTime(8,0), day2.atTime(12,30), "Annual Leave"),
                ShiftTaskDto(day2, "Break", day2.atTime(12,30), day2.atTime(13,30), "Break (Unpaid)"),
                ShiftTaskDto(day2, "Holiday", day2.atTime(13,30), day2.atTime(17,0), "Annual Leave"),
                ShiftTaskDto(day3, "Absence", day3.atTime(0,0), day4.atTime(23,59,59), "Rest Day")
        )

        val simpleNightShiftRestAfter = listOf(
                ShiftTaskDto(day1, "Shift", day1.atTime(20,45), day2.atTime(7,45), "Night OSG"),
                ShiftTaskDto(day2, "Shift", day2.atTime(20,45), day3.atTime(7,45), "Night OSG"),
                ShiftTaskDto(day3, "Absence", day3.atTime(0,0), day3.atTime(23,59,59), "Rest Day")
        )

        // This is broken, its the one Fareed is working on
        val longerSimpleNightShiftRestAfter = listOf(
                ShiftTaskDto(day1, "Unspecific", day1.atTime(7,15), day1.atTime(12,30), "Present"),
                ShiftTaskDto(day1, "Break", day1.atTime(12,30), day1.atTime(13,30), "Break (Unpaid)"),
                ShiftTaskDto(day1, "Unspecific", day1.atTime(13,30), day1.atTime(17,0), "Present"),
                ShiftTaskDto(day2, "Unspecific", day2.atTime(7,15), day2.atTime(12,30), "Present"),
                ShiftTaskDto(day2, "Break", day2.atTime(12,30), day2.atTime(13,30), "Break (Unpaid)"),
                ShiftTaskDto(day2, "Unspecific", day2.atTime(13,30), day2.atTime(17,0), "Present"),
                ShiftTaskDto(day2, "Shift", day2.atTime(20,45), day3.atTime(7,45), "Night OSG"),

                ShiftTaskDto(day3, "Absence", day3.atTime(7,45), day3.atTime(23,58,59), "Rest Day"),

                ShiftTaskDto(day4, "Unspecific", day4.atTime(7,15), day4.atTime(12,30), "Present"),
                ShiftTaskDto(day4, "Break", day4.atTime(12,30), day4.atTime(13,30), "Break (Unpaid)"),
                ShiftTaskDto(day4, "Unspecific", day4.atTime(13,30), day4.atTime(17,0), "Present")
        )

        val simpleNightShift = listOf(
                ShiftTaskDto(day1, "Shift", day1.atTime(20,45), day2.atTime(7,45), "Night OSG"),
                ShiftTaskDto(day2, "Shift", day2.atTime(20,45), day3.atTime(7,45), "Night OSG"),
                ShiftTaskDto(day3, "Shift", day3.atTime(20,45), day4.atTime(7,45), "Night OSG")
        )

        val longerNightShift = listOf(
                ShiftTaskDto(day1, "Shift", day1.atTime(20,45), day2.atTime(7,45), "Night OSG"),
                ShiftTaskDto(day2, "Shift", day2.atTime(20,45), day3.atTime(7,45), "Night OSG"),
                ShiftTaskDto(day3, "Shift", day3.atTime(20,45), day4.atTime(7,45), "Night OSG"),
                ShiftTaskDto(day4, "Shift", day4.atTime(20,45), day5.atTime(7,45), "Night OSG")

        )

        val restDayShift = listOf(
                ShiftTaskDto(day1, "Unspecific", day1.atTime(7,30), day1.atTime(12,0), "Present"),
                ShiftTaskDto(day1, "Break", day1.atTime(12,0), day1.atTime(13,0), "Break (Unpaid)"),
                ShiftTaskDto(day1, "Unspecific", day1.atTime(13,0), day1.atTime(17,0), "Present"),
                ShiftTaskDto(day2, "Absence", day2.atTime(0,0), day2.atTime(23,59,59), "Rest Day"),
                ShiftTaskDto(day3, "Unspecific", day3.atTime(7,30), day3.atTime(12,0), "Present"),
                ShiftTaskDto(day3, "Break", day3.atTime(12,0), day3.atTime(13,0), "Break (Unpaid)"),
                ShiftTaskDto(day3, "Unspecific", day3.atTime(13,0), day3.atTime(17,0), "Present"),
                ShiftTaskDto(day4, "Unspecific", day4.atTime(7,30), day4.atTime(12,0), "Present"),
                ShiftTaskDto(day4, "Break", day4.atTime(12,0), day4.atTime(13,0), "Break (Unpaid)"),
                ShiftTaskDto(day4, "Unspecific", day4.atTime(13,0), day4.atTime(17,0), "Present")
        )

    }
}

data class ShiftTaskDto(
        val date : LocalDate,
        val type : String,
        val start : LocalDateTime,
        val end : LocalDateTime,
        val activity: String)