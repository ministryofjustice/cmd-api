package uk.gov.justice.digital.hmpps.cmd.api.dto

import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.cmd.api.dto.DayModelDto
import uk.gov.justice.digital.hmpps.cmd.api.dto.DayEventDto
import java.time.LocalDate
import java.time.LocalDateTime

class DayModelDtoTest {

    @Test
    fun `Create Day Dto `() {

        val date = LocalDate.now()
        val type = "anytype"
        val tasks : Collection<DayEventDto> = listOf()

        val day = DayModelDto(
            date,
            type,
            tasks
        )

        Assertions.assertThat(day.date).isEqualTo(date)
        Assertions.assertThat(day.fullDayType).isEqualTo(type)
        Assertions.assertThat(day.tasks).isEqualTo(tasks)
    }

}