package uk.gov.justice.digital.hmpps.cmd.api.domain

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
