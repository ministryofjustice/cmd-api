package uk.gov.justice.digital.hmpps.cmd.api.dto

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty
import uk.gov.justice.digital.hmpps.cmd.api.model.ShiftNotification
import uk.gov.justice.digital.hmpps.cmd.api.model.ShiftTaskNotification
import java.time.LocalDateTime

@ApiModel(description = "Notification")
data class NotificationDto @JsonCreator constructor(
        @ApiModelProperty(required = true, value = "Description of notification", position = 1, example = "Your shift on 2020-04-20 has changed.")
        @JsonProperty("description")
        val description: String,
        @ApiModelProperty(required = true, value = "When the shift was modified", position = 2, example = "2020-04-20T17:45:55")
        @JsonProperty("lastModified")
        val lastModified: LocalDateTime,
        @ApiModelProperty(required = true, value = "Whether the notification has been processed", position = 3, example = "true")
        @JsonProperty("processed")
        val processed: Boolean
) {

    companion object {
        fun fromShifts(shiftNotifications: Collection<ShiftNotification>): List<NotificationDto> {
            return shiftNotifications.map { from(it) }
        }

        fun fromTasks(shiftTaskNotifications: Collection<ShiftTaskNotification>): List<NotificationDto> {
            return shiftTaskNotifications.map { from(it) }
        }

        private fun from(shiftNotification: ShiftNotification): NotificationDto {
            return NotificationDto(shiftNotification.description,
                    shiftNotification.lastModified,
                    shiftNotification.processed)
        }

        private fun from(shiftTaskNotification: ShiftTaskNotification): NotificationDto {
            return NotificationDto(shiftTaskNotification.description,
                    shiftTaskNotification.lastModified,
                    shiftTaskNotification.processed)
        }
    }
}
