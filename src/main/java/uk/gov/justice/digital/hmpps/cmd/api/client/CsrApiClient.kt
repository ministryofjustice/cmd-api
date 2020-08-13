package uk.gov.justice.digital.hmpps.cmd.api.client

import org.springframework.stereotype.Component
import java.time.LocalDate
import java.time.LocalDateTime

@Component
class CsrApiClient {

    fun getShiftTasks(start : LocalDate, end : LocalDate ) : Collection<ShiftTaskDto> {
        return listOf()
    }

    fun getOvertimeShiftTasks(start : LocalDate, end : LocalDate) : Collection<ShiftTaskDto> {
        return listOf()
    }

}

data class ShiftTaskDto(
        val date : LocalDate,
        val type : String,
        val start : LocalDateTime,
        val end : LocalDateTime,
        val activity: String) 