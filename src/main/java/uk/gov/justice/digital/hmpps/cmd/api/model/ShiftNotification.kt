package uk.gov.justice.digital.hmpps.cmd.api.model

import uk.gov.justice.digital.hmpps.cmd.api.uk.gov.justice.digital.hmpps.cmd.api.client.dto.ShiftNotificationDto
import uk.gov.justice.digital.hmpps.cmd.api.client.CsrModifiedDetailDto
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import javax.persistence.*

@Entity
@Table(name = "shift_notification")
data class ShiftNotification(
        @Id
        @Column(name = "ID")
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        var id: Long? = null,

        @Column(nullable = false)
        var quantumId: String,

        @Column(nullable = false)
        var shiftDate: LocalDate,

        @Column(nullable = false)
        var shiftModified: LocalDateTime,

        @Column
        var taskStart: Long?,

        @Column
        var taskEnd: Long?,

        @Column
        var task: String?,

        @Column(nullable = false)
        var shiftType: String,

        @Column(nullable = false)
        var actionType: String,

        @Column(nullable = false)
        var processed: Boolean
) {
        fun getNotificationDescription(communicationPreference: CommunicationPreference): String {

                val bulletPoint = getOptionalBulletPoint(communicationPreference)
                val date = this.shiftDate.getDateTimeFormattedForTemplate()
                val taskTime = getOptionalTaskDescription(this.taskStart, this.taskEnd, this.task)
                val shiftActionType = ShiftActionType.from(this.actionType)
                val taskTo = getOptionalTaskTo(this.task, communicationPreference, shiftActionType)
                val shiftNotificationType = ShiftNotificationType.from(this.shiftType)

                return "${bulletPoint}Your ${shiftNotificationType.description} on $date ${taskTime}has ${shiftActionType.description}${taskTo}."
        }

        companion object {

                fun fromDto(dtoCsrs: Collection<CsrModifiedDetailDto>): Collection<ShiftNotification> {
                        return dtoCsrs.map { fromDto(it) }
                }

                fun fromDto(dtoCsr: CsrModifiedDetailDto): ShiftNotification {
                        return ShiftNotification(
                                quantumId = dtoCsr.quantumId,
                                shiftDate = dtoCsr.shiftDate,
                                shiftModified = dtoCsr.shiftModified,
                                taskStart = dtoCsr.detailStart,
                                taskEnd = dtoCsr.detailEnd,
                                task = dtoCsr.activity,
                                shiftType = dtoCsr.shiftType,
                                actionType = dtoCsr.actionType,
                                processed = false)
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

                private fun getOptionalTaskDescription(from: Long?, to: Long?, task: String?): String {
                        return if ((from != null && to != null) && !task.isNullOrEmpty())
                        {
                                if(from > 1000L && to > 1000L) {
                                        "(${getTimeWithoutDayOffset(from)} - ${getTimeWithoutDayOffset(to)}) "
                                } else {
                                        "(full day) "
                                }
                        } else ""
                }

                private fun getTimeWithoutDayOffset(seconds: Long): LocalTime {
                        val fullDay = 86_400L
                        return LocalTime.ofSecondOfDay(
                                if (seconds >= fullDay) {
                                        seconds - fullDay
                                } else {
                                        seconds
                                }
                        )
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
