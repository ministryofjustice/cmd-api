package uk.gov.justice.digital.hmpps.cmd.api.dto

import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import java.time.LocalDate

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