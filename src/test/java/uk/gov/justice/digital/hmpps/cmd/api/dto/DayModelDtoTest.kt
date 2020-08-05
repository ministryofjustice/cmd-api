package uk.gov.justice.digital.hmpps.cmd.api.dto

import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.cmd.api.uk.gov.justice.digital.hmpps.cmd.api.dto.DayModelDto
import uk.gov.justice.digital.hmpps.cmd.api.uk.gov.justice.digital.hmpps.cmd.api.dto.TaskModelDto
import java.time.LocalDate
import java.time.LocalDateTime

class DayModelDtoTest {

    @Test
    fun `Create Day Dto `() {

        val date = LocalDate.now()
        val dailyStartDatTime = LocalDateTime.of(12,3,4,5,6,1)
        val dailyEndDateTime = LocalDateTime.of(12,3,4,8,6,2)
        val type = "anytype"
        val startDateTime = LocalDateTime.of(12,3,4,6,6,3)
        val endDateTime = LocalDateTime.of(12,3,4,7,6,4)
        val durationInSeconds = 5L
        val tasks : Collection<TaskModelDto> = listOf()

        val day = DayModelDto(
            date,
            dailyStartDatTime,
            dailyEndDateTime,
            type,
            startDateTime,
            endDateTime,
            durationInSeconds,
            tasks
        )

        Assertions.assertThat(day.date).isEqualTo(date)
        Assertions.assertThat(day.dailyStartDateTime).isEqualTo(dailyStartDatTime)
        Assertions.assertThat(day.dailyEndDateTime).isEqualTo(dailyEndDateTime)
        Assertions.assertThat(day.type).isEqualTo(type)
        Assertions.assertThat(day.startDateTime).isEqualTo(startDateTime)
        Assertions.assertThat(day.endDateTime).isEqualTo(endDateTime)
        Assertions.assertThat(day.durationInSeconds).isEqualTo(durationInSeconds)
        Assertions.assertThat(day.tasks).isEqualTo(tasks)
    }

}