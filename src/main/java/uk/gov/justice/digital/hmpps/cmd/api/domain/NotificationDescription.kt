package uk.gov.justice.digital.hmpps.cmd.api.uk.gov.justice.digital.hmpps.cmd.api.domain

import uk.gov.justice.digital.hmpps.cmd.api.model.ShiftNotification
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class NotificationDescription {

    companion object {
        fun ShiftNotification.getNotificationDescription(communicationPreference: CommunicationPreference): String {

            val bulletPoint = getOptionalBulletPoint(communicationPreference)
            val date = this.shiftDate.getDateTimeFormattedForTemplate()
            val taskTime = getOptionalTaskDescription(this.taskStart, this.taskEnd)
            val shiftActionType = ShiftActionType.from(this.actionType)
            val taskTo = getOptionalTaskTo(this.task, communicationPreference, shiftActionType)
            val shiftNotificationType = ShiftNotificationType.from(this.shiftType)

            return "${bulletPoint}Your ${shiftNotificationType.description} on $date ${taskTime}has ${shiftActionType.description}${taskTo}."
        }

        fun LocalDate.getDateTimeFormattedForTemplate(): String {
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

        private fun getOptionalTaskDescription(from: Long?, to: Long?): String {
            return if (from != null && from != 0L && to != null && to != 0L) {
                val fromTime = LocalTime.ofSecondOfDay(from)
                val toTime = LocalTime.ofSecondOfDay(to)
                "($fromTime - $toTime) "
            } else ""
        }

        private fun getOptionalTaskTo(task: String?, communicationPreference: CommunicationPreference, shiftActionType: ShiftActionType): String {
            return if (communicationPreference == CommunicationPreference.NONE && task != null && task.isNotEmpty()) {
                when(shiftActionType) {
                    ShiftActionType.ADD -> {
                        " as $task"
                    }
                    ShiftActionType.EDIT -> {
                        " to $task"
                    }
                    ShiftActionType.DELETE -> {
                        " (was $task)"
                    }
                    else -> {
                        ""
                    }
                }

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