package uk.gov.justice.digital.hmpps.cmd.api.dto

import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.cmd.api.uk.gov.justice.digital.hmpps.cmd.api.domain.DetailType
import java.time.LocalDate

class ShiftDtoTest {

    @Test
    fun `Create Day Dto `() {

        val date = LocalDate.now()
        val type = DetailType.REST_DAY
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