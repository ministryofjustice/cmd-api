package uk.gov.justice.digital.hmpps.cmd.api.dto

import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.cmd.api.uk.gov.justice.digital.hmpps.cmd.api.dto.TaskEventDto
import java.time.LocalDateTime

class TaskEventDtoTest {

    @Test
    fun `Create Task Dto `() {

        val label = "anylabel"
        val type = "anyType"
        val start = LocalDateTime.of(12,3,4,5,6,1)
        val end = LocalDateTime.of(12,3,4,6,7,8)
        val displayType = "displayType"
        val finish = "Five hours or so"

        val day = TaskEventDto(
            label,
            type,
            start,
            end,
            displayType,
            finish
        )

        Assertions.assertThat(day.label).isEqualTo(label)
        Assertions.assertThat(day.taskType).isEqualTo(type)
        Assertions.assertThat(day.start).isEqualTo(start)
        Assertions.assertThat(day.end).isEqualTo(end)
        Assertions.assertThat(day.displayType).isEqualTo(displayType)
        Assertions.assertThat(day.finishDuration).isEqualTo(finish)
    }

}