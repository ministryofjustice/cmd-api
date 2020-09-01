package uk.gov.justice.digital.hmpps.cmd.api.dto

import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.cmd.api.domain.TaskDisplayType
import uk.gov.justice.digital.hmpps.cmd.api.uk.gov.justice.digital.hmpps.cmd.api.domain.EntityType
import java.time.LocalTime

class DetailEventDtoTest {

    @Test
    fun `Create Task Dto `() {

        val label = "anylabel"
        val start = LocalTime.of(5,6,1)
        val end = LocalTime.of(6,7,8)
        val entityType = EntityType.SHIFT
        val displayType = TaskDisplayType.DAY_START
        val eventTime = LocalTime.of(6,4,5)
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
        Assertions.assertThat(day.entityType).isEqualTo(entityType)
        Assertions.assertThat(day.displayType).isEqualTo(displayType)
        Assertions.assertThat(day.eventTime).isEqualTo(eventTime)
        Assertions.assertThat(day.finishDuration).isEqualTo(finish)
    }

} 