package uk.gov.justice.digital.hmpps.cmd.api.uk.gov.justice.digital.hmpps.cmd.api.client.dto

import com.fasterxml.jackson.annotation.JsonCreator
import io.swagger.annotations.ApiModel
import java.time.LocalDateTime

@ApiModel(description = "CSR Notification")
data class ShiftNotificationDto @JsonCreator constructor(

        var id: Long? = null,


        var quantumId: String,


        var shiftDate: LocalDateTime,


        var shiftModified: LocalDateTime,


        var taskStart: Long?,


        var taskEnd: Long?,


        var task: String?,


        var shiftType: String,


        var actionType: String

)
