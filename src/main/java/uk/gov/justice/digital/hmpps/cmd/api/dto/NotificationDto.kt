package uk.gov.justice.digital.hmpps.cmd.api.dto

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema
import uk.gov.justice.digital.hmpps.cmd.api.domain.CommunicationPreference
import uk.gov.justice.digital.hmpps.cmd.api.model.Notification
import java.time.LocalDateTime

@Schema(description = "Notification")
data class NotificationDto @JsonCreator constructor(
  @Schema(required = true, description = "Description of notification", example = "Your shift on 2020-04-20 has changed.")
  @JsonProperty("description")
  val description: String,
  @Schema(required = true, description = "When the shift was modified", example = "2020-04-20T17:45:55")
  @JsonProperty("shiftModified")
  val shiftModified: LocalDateTime,
  @Schema(required = true, description = "Whether the notification has been processed", example = "true")
  @JsonProperty("processed")
  val processed: Boolean
) {

  companion object {

    fun from(notification: Notification, communicationPreference: CommunicationPreference): NotificationDto {
      return NotificationDto(
        notification.getNotificationDescription(communicationPreference),
        notification.shiftModified,
        notification.processed
      )
    }
  }
}
