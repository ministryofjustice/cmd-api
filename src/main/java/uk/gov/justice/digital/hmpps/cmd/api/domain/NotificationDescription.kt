package uk.gov.justice.digital.hmpps.cmd.api.uk.gov.justice.digital.hmpps.cmd.api.domain

import uk.gov.justice.digital.hmpps.cmd.api.uk.gov.justice.digital.hmpps.cmd.api.model.CommunicationPreference
import uk.gov.justice.digital.hmpps.cmd.api.uk.gov.justice.digital.hmpps.cmd.api.model.ShiftActionType
import uk.gov.justice.digital.hmpps.cmd.api.uk.gov.justice.digital.hmpps.cmd.api.model.ShiftNotificationType
import java.time.Clock
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class NotificationDescription {

    companion object {
        fun getNotificationDescription(shiftNotificationType: ShiftNotificationType, shiftActionType: ShiftActionType, shiftDate: LocalDateTime, communicationPreference: CommunicationPreference, clock: Clock, task: String? = null, from: Long = 0L, to: Long = 0L): String {
            val isEmail = isEmail(communicationPreference)
            val date = getDateTimeFormattedForTemplate(shiftDate, clock)
            val taskString = getTaskString(task, from, to)
            return "${isEmail}Your ${shiftNotificationType.prose} on $date$taskString has ${shiftActionType.prose}."
        }

        fun getDateTimeFormattedForTemplate(shiftDate: LocalDateTime, clock: Clock): String {
            val day = shiftDate.dayOfMonth
            val ordinal = if (day in 11..13) {
                "th"
            } else when (day % 10) {
                1 -> "st"
                2 -> "nd"
                3 -> "rd"
                else -> "th"
            }
            val year = if (shiftDate.year > LocalDate.now(clock).year) {
                ", yyyy"
            } else ""
            return DateTimeFormatter.ofPattern("EEEE, d'$ordinal' MMMM$year").format(shiftDate)
        }

        private fun getTaskString(task: String?, from: Long, to: Long): String {
            val fromTime = LocalTime.ofSecondOfDay(from)
            val toTime = LocalTime.ofSecondOfDay(to)
            return if (task != null) {
                " ($task, $fromTime - $toTime)"
            } else {
                ""
            }

        }

        private fun isEmail(communicationPreference: CommunicationPreference): String {
            return if (communicationPreference == CommunicationPreference.EMAIL) {
                "* "
            } else ""
        }
    }
}