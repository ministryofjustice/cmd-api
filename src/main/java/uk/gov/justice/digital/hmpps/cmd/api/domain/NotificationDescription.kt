package uk.gov.justice.digital.hmpps.cmd.api.uk.gov.justice.digital.hmpps.cmd.api.domain

import uk.gov.justice.digital.hmpps.cmd.api.model.ShiftNotification
import java.time.Clock
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class NotificationDescription {

    companion object {
        fun getNotificationDescription(shiftNotification: ShiftNotification, communicationPreference: CommunicationPreference, clock: Clock): String {
            val bulletPoint = getOptionalBulletPoint(communicationPreference)
            val date = getDateTimeFormattedForTemplate(shiftNotification.shiftDate, clock)
            val taskDescription = getOptionalTaskDescription(shiftNotification.task, shiftNotification.taskStart, shiftNotification.taskEnd)
            val shiftNotificationType = ShiftNotificationType.from(shiftNotification.shiftType)
            val shiftActionType = ShiftActionType.from(shiftNotification.actionType)
            return "${bulletPoint}Your ${shiftNotificationType.description} on $date ${taskDescription}has ${shiftActionType.description}."
        }

        fun getDateTimeFormattedForTemplate(shiftDate: LocalDate, clock: Clock): String {
            val day = shiftDate.dayOfMonth
            val ordinal = if (day in 11..13) {
                "th"
            } else when (day % 10) {
                1 -> "st"
                2 -> "nd"
                3 -> "rd"
                else -> "th"
            }
            val year = if (shiftDate.year != LocalDate.now(clock).year) {
                ", yyyy"
            } else ""

            return DateTimeFormatter.ofPattern("EEEE, d'$ordinal' MMMM$year").format(shiftDate)
        }

        private fun getOptionalTaskDescription(task: String?, from: Long?, to: Long?): String {
            return if (task != null && task.isNotEmpty() && from != null && to != null) {
                val fromTime = LocalTime.ofSecondOfDay(from)
                val toTime = LocalTime.ofSecondOfDay(to)
                "($task, $fromTime - $toTime) "
            } else ""
        }

        // Notify supports bullet points for Email but not Sms
        private fun getOptionalBulletPoint(communicationPreference: CommunicationPreference): String {
            return if (communicationPreference == CommunicationPreference.EMAIL) {
                "* "
            } else ""
        }
    }
}