package uk.gov.justice.digital.hmpps.cmd.api.dto

import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.cmd.api.domain.TaskDisplayType
import uk.gov.justice.digital.hmpps.cmd.api.uk.gov.justice.digital.hmpps.cmd.api.domain.ShiftType
import java.time.LocalDateTime

class DetailEventDtoTest {

    @Test
    fun `Create Task Dto `() {

        val label = "anylabel"
        val start = LocalDateTime.of(1,2,3,5,6,1)
        val end = LocalDateTime.of(2,3,4,6,7,8)
        val entityType = ShiftType.SHIFT
        val displayType = TaskDisplayType.DAY_START
        val eventTime = LocalDateTime.of(4,5,6,6,4,5)
        val finish = "Five hours or so"

        val day = DetailEventDto(
            label,
            start,
            end,
            entityType,
            displayType,
            eventTime,
            finish
        )

        Assertions.assertThat(day.label).isEqualTo(label)
        Assertions.assertThat(day.start).isEqualTo(start)
        Assertions.assertThat(day.end).isEqualTo(end)
        Assertions.assertThat(day.shiftType).isEqualTo(entityType)
        Assertions.assertThat(day.displayType).isEqualTo(displayType)
        Assertions.assertThat(day.eventDateTime).isEqualTo(eventTime)
        Assertions.assertThat(day.finishDuration).isEqualTo(finish)
    }

} 