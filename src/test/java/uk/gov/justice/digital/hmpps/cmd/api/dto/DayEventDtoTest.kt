package uk.gov.justice.digital.hmpps.cmd.api.dto

import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import java.time.LocalTime

class DayEventDtoTest {

    @Test
    fun `Create Task Dto `() {

        val time = LocalTime.of(5,6,1)
        val displayType = "displayType"
        val finish = "Five hours or so"

        val day = DayEventDto(
            time,
            displayType,
            finish
        )

        Assertions.assertThat(day.eventTime).isEqualTo(time)
        Assertions.assertThat(day.displayType).isEqualTo(displayType)
        Assertions.assertThat(day.finishDuration).isEqualTo(finish)
    }

} 