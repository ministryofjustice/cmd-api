package uk.gov.justice.digital.hmpps.cmd.api.model

import uk.gov.justice.digital.hmpps.cmd.api.uk.gov.justice.digital.hmpps.cmd.api.client.dto.ShiftNotificationDto
import uk.gov.justice.digital.hmpps.cmd.api.uk.gov.justice.digital.hmpps.cmd.api.domain.CommunicationPreference
import uk.gov.justice.digital.hmpps.cmd.api.uk.gov.justice.digital.hmpps.cmd.api.domain.ShiftActionType
import uk.gov.justice.digital.hmpps.cmd.api.uk.gov.justice.digital.hmpps.cmd.api.domain.ShiftNotificationType
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
                val taskTime = getOptionalTaskDescription(this.taskStart, this.taskEnd)
                val shiftActionType = ShiftActionType.from(this.actionType)
                val taskTo = getOptionalTaskTo(this.task, communicationPreference, shiftActionType)
                val shiftNotificationType = ShiftNotificationType.from(this.shiftType)

                return "${bulletPoint}Your ${shiftNotificationType.description} on $date ${taskTime}has ${shiftActionType.description}${taskTo}."
        }

        companion object {

                fun fromDto(dtos: Collection<ShiftNotificationDto>): Collection<ShiftNotification> {
                        return dtos.map { fromDto(it) }
                }

                fun fromDto(dto: ShiftNotificationDto): ShiftNotification {
                        return ShiftNotification(
                                quantumId = dto.quantumId.toUpperCase(),
                                shiftDate = dto.actionDate,
                                shiftModified = dto.shiftModified,
                                taskStart = dto.taskStart,
                                taskEnd = dto.taskEnd,
                                task = dto.task,
                                shiftType = dto.shiftType,
                                actionType = dto.actionType,
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
