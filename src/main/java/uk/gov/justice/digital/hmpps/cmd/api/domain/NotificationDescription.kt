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
            val taskTime = getOptionalTaskDescription(shiftNotification.taskStart, shiftNotification.taskEnd)
            val shiftActionType = ShiftActionType.from(shiftNotification.actionType)
            val taskTo = getOptionalTaskTo(shiftNotification.task, communicationPreference, shiftActionType)
            val shiftNotificationType = ShiftNotificationType.from(shiftNotification.shiftType)

            return "${bulletPoint}Your ${shiftNotificationType.description} on $date ${taskTime}has ${shiftActionType.description}${taskTo}."
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

        private fun getOptionalTaskDescription(from: Long?, to: Long?): String {
            return if (from != null && from != 0L && to != null && to != 0L) {
                if((from > LocalTime.MAX.toSecondOfDay() || from < LocalTime.MIN.toSecondOfDay()) ||
                    (to > LocalTime.MAX.toSecondOfDay() || to < LocalTime.MIN.toSecondOfDay())) {
                    "(full day) "
                } else {
                    val fromTime = LocalTime.ofSecondOfDay(from)
                    val toTime = LocalTime.ofSecondOfDay(to)
                    "($fromTime - $toTime) "
                }
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