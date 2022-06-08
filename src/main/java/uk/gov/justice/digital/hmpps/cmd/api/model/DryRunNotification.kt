package uk.gov.justice.digital.hmpps.cmd.api.model

import uk.gov.justice.digital.hmpps.cmd.api.client.CsrModifiedDetailDto
import uk.gov.justice.digital.hmpps.cmd.api.domain.CommunicationPreference
import uk.gov.justice.digital.hmpps.cmd.api.domain.DetailModificationType
import uk.gov.justice.digital.hmpps.cmd.api.domain.DetailModificationType.ADD
import uk.gov.justice.digital.hmpps.cmd.api.domain.DetailModificationType.DELETE
import uk.gov.justice.digital.hmpps.cmd.api.domain.DetailModificationType.EDIT
import uk.gov.justice.digital.hmpps.cmd.api.domain.ShiftType
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.EnumType
import javax.persistence.Enumerated
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.Table

@Entity
@Table(name = "dry_run_notification")
data class DryRunNotification(
  @Id
  @Column(name = "ID")
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  var id: Long = 0,

  @Column(nullable = false)
  var quantumId: String,

  @Column(nullable = false)
  var shiftModified: LocalDateTime,

  @Column(nullable = false)
  var detailStart: LocalDateTime,

  @Column(nullable = false)
  var detailEnd: LocalDateTime,

  @Column
  var activity: String?,

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  var parentType: ShiftType,

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  var actionType: DetailModificationType,

  @Column(nullable = false)
  var processed: Boolean
) {
  fun getNotificationDescription(communicationPreference: CommunicationPreference): String {

    val bulletPoint = getOptionalBulletPoint(communicationPreference)
    val date = this.detailStart.getDateTimeFormattedForTemplate()
    val taskTime = getOptionalTaskDescription(this.detailStart, this.detailEnd, this.activity)
    val shiftActionType = this.actionType
    val taskTo = getOptionalTaskTo(this.activity, communicationPreference, shiftActionType)
    val shiftNotificationType = getNotificationType(this.parentType, this.activity)

    return "${bulletPoint}Your $shiftNotificationType on $date ${taskTime}has ${shiftActionType.description}$taskTo."
  }

  companion object {

    fun fromDto(dtoCsrs: Collection<CsrModifiedDetailDto>): Collection<DryRunNotification> {
      return dtoCsrs.map { fromDto(it) }
    }

    fun fromDto(dtoCsr: CsrModifiedDetailDto): DryRunNotification {
      return DryRunNotification(
        quantumId = dtoCsr.quantumId!!,
        shiftModified = dtoCsr.shiftModified!!,
        detailStart = dtoCsr.detailStart,
        detailEnd = dtoCsr.detailEnd,
        activity = dtoCsr.activity,
        parentType = dtoCsr.shiftType,
        actionType = dtoCsr.actionType!!,
        processed = false
      )
    }

    fun LocalDateTime.getDateTimeFormattedForTemplate(): String {
      val day = this.dayOfMonth
      val ordinal = if (day in 11..13) {
        "th"
      } else when (day % 10) {
        1 -> "st"
        2 -> "nd"
        3 -> "rd"
        else -> "th"
      }

      return DateTimeFormatter.ofPattern("EEEE, d'$ordinal' MMMM").format(this)
    }

    private fun getNotificationType(shiftNotificationType: ShiftType, activity: String?): String {
      return if (shiftNotificationType == ShiftType.SHIFT) {
        if (activity == null) {
          "shift"
        } else {
          "detail"
        }
      } else {
        if (activity == null) {
          "overtime shift"
        } else {
          "overtime detail"
        }
      }
    }

    private fun getOptionalTaskDescription(from: LocalDateTime, to: LocalDateTime, task: String?): String {
      return if (!task.isNullOrEmpty()) {
        if (from.toLocalTime().isAfter(LocalTime.MIN) && to.toLocalTime().isAfter(LocalTime.MIN)) {
          "(${from.toLocalTime()} - ${to.toLocalTime()}) "
        } else {
          "(full day) "
        }
      } else ""
    }

    private fun getOptionalTaskTo(
      task: String?,
      communicationPreference: CommunicationPreference,
      shiftActionType: DetailModificationType
    ): String =
      if (communicationPreference == CommunicationPreference.NONE && !task.isNullOrEmpty())
        when (shiftActionType) {
          ADD -> " as $task"
          EDIT -> " to $task"
          DELETE -> " (was $task)"
          else -> ""
        }
      else ""

    // Notify supports bullet points for Email but not Sms
    private fun getOptionalBulletPoint(communicationPreference: CommunicationPreference): String {
      return if (communicationPreference == CommunicationPreference.EMAIL) {
        "* "
      } else ""
    }
  }
}
