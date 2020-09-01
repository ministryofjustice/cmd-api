package uk.gov.justice.digital.hmpps.cmd.api.dto

import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import java.time.LocalDate

class DetailDtoTest {

    @Test
    fun `Create Day Dto `() {

        val date = LocalDate.now()
        val type = "anytype"
        val details : Collection<DetailEventDto> = listOf()

        val day = DetailDto(
            date,
            type,
            details
        )

        Assertions.assertThat(day.date).isEqualTo(date)
        Assertions.assertThat(day.fullDayType).isEqualTo(type)
        Assertions.assertThat(day.details).isEqualTo(details)
    }

} 