package uk.gov.justice.digital.hmpps.cmd.api.model

import uk.gov.justice.digital.hmpps.cmd.api.uk.gov.justice.digital.hmpps.cmd.api.client.dto.ShiftNotificationDto
import java.time.LocalDateTime
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
        var shiftDate: LocalDateTime,

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
        companion object {

                fun fromDto(dtos: Collection<ShiftNotificationDto>): Collection<ShiftNotification> {
                        return dtos.map { fromDto(it) }
                }

                fun fromDto(dto: ShiftNotificationDto): ShiftNotification {
                        return ShiftNotification(
                                dto.id,
                                dto.quantumId,
                                dto.shiftDate,
                                dto.shiftModified,
                                dto.taskStart,
                                dto.taskEnd,
                                dto.task,
                                dto.shiftType,
                                dto.actionType,
                                false
                        )
                }
        }
}
