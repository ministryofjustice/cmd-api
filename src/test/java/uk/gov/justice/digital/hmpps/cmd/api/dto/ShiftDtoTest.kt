package uk.gov.justice.digital.hmpps.cmd.api.dto

import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import java.time.LocalDate

class ShiftDtoTest {

    @Test
    fun `Create Day Dto `() {

        val date = LocalDate.now()
        val type = "anytype"
        val details : Collection<DetailDto> = listOf()

        val day = ShiftDto(
            date,
            type,
            details
        )

        Assertions.assertThat(day.date).isEqualTo(date)
        Assertions.assertThat(day.shiftType).isEqualTo(type)
        Assertions.assertThat(day.details).isEqualTo(details)
    }

} 