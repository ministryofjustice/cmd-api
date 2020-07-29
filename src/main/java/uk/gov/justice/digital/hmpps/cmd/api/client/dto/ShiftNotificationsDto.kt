package uk.gov.justice.digital.hmpps.cmd.api.uk.gov.justice.digital.hmpps.cmd.api.client.dto

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty


data class ShiftNotificationsDto @JsonCreator constructor(
        @JsonProperty("shiftNotifications")
        var shiftNotifications: List<ShiftNotificationDto>
)
