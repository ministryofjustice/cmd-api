package uk.gov.justice.digital.hmpps.cmd.api.dto

import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.cmd.api.domain.DetailDisplayType
import uk.gov.justice.digital.hmpps.cmd.api.uk.gov.justice.digital.hmpps.cmd.api.domain.DetailParentType
import java.time.LocalDateTime

class DetailDtoTest {

    @Test
    fun `Create Task Dto `() {

        val label = "anylabel"
        val start = LocalDateTime.of(1,2,3,5,6,1)
        val end = LocalDateTime.of(2,3,4,6,7,8)
        val entityType = DetailParentType.SHIFT
        val displayType = DetailDisplayType.DAY_START
        val eventTime = LocalDateTime.of(4,5,6,6,4,5)
        val finish = "Five hours or so"

        val day = DetailDto(
            label,
            start,
            end,
            entityType,
            displayType,
            eventTime,
            finish
        )

        Assertions.assertThat(day.activity).isEqualTo(label)
        Assertions.assertThat(day.start).isEqualTo(start)
        Assertions.assertThat(day.end).isEqualTo(end)
        Assertions.assertThat(day.detail).isEqualTo(entityType)
        Assertions.assertThat(day.displayType).isEqualTo(displayType)
        Assertions.assertThat(day.displayTypeTime).isEqualTo(eventTime)
        Assertions.assertThat(day.finishDuration).isEqualTo(finish)
    }

} 