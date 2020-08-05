package uk.gov.justice.digital.hmpps.cmd.api.dto

import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.cmd.api.uk.gov.justice.digital.hmpps.cmd.api.dto.TaskModelDto
import java.time.LocalDate
import java.time.LocalDateTime

class TaskModelDtoTest {

    @Test
    fun `Create Task Dto `() {

        val date = LocalDate.now()
        val dailyStartDatTime = LocalDateTime.of(12,3,4,5,6,1)
        val dailyEndDateTime = LocalDateTime.of(12,3,4,8,6,2)
        val label = "anylabel"
        val type = "anyType"
        val startDateTime = LocalDateTime.of(12,3,4,6,6,3)
        val endDateTime = LocalDateTime.of(12,3,4,7,6,4)

        val day = TaskModelDto(
            date,
            dailyStartDatTime,
            dailyEndDateTime,
            label,
            type,
            startDateTime,
            endDateTime
        )

        Assertions.assertThat(day.date).isEqualTo(date)
        Assertions.assertThat(day.dailyStartDateTime).isEqualTo(dailyStartDatTime)
        Assertions.assertThat(day.dailyEndDateTime).isEqualTo(dailyEndDateTime)
        Assertions.assertThat(day.label).isEqualTo(label)
        Assertions.assertThat(day.type).isEqualTo(type)
        Assertions.assertThat(day.startDateTime).isEqualTo(startDateTime)
        Assertions.assertThat(day.endDateTime).isEqualTo(endDateTime)
    }

}