package uk.gov.justice.digital.hmpps.cmd.api.dto

import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.cmd.api.dto.TaskEventDto
import uk.gov.justice.digital.hmpps.cmd.api.dto.TaskModelDto
import java.time.LocalDate

class TaskModelDtoTest {

    @Test
    fun `Create Day Dto `() {

        val date = LocalDate.now()
        val type = "anytype"
        val tasks : Collection<TaskEventDto> = listOf()

        val day = TaskModelDto(
            date,
            type,
            tasks
        )

        Assertions.assertThat(day.date).isEqualTo(date)
        Assertions.assertThat(day.fullDayType).isEqualTo(type)
        Assertions.assertThat(day.tasks).isEqualTo(tasks)
    }

}