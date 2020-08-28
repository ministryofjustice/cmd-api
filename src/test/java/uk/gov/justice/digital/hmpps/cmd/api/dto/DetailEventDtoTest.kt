package uk.gov.justice.digital.hmpps.cmd.api.dto

import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import java.time.LocalTime

class DetailEventDtoTest {

    @Test
    fun `Create Task Dto `() {

        val label = "anylabel"
        val start = LocalTime.of(5,6,1)
        val end = LocalTime.of(6,7,8)
        val displayType = "displayType"
        val finish = "Five hours or so"

        val day = DetailEventDto(
            label,
            start,
            end,
            displayType,
            finish
        )

        Assertions.assertThat(day.label).isEqualTo(label)
        Assertions.assertThat(day.start).isEqualTo(start)
        Assertions.assertThat(day.end).isEqualTo(end)
        Assertions.assertThat(day.displayType).isEqualTo(displayType)
        Assertions.assertThat(day.finishDuration).isEqualTo(finish)
    }

} 