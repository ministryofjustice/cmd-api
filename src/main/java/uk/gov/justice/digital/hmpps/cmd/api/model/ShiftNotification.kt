package uk.gov.justice.digital.hmpps.cmd.api.model

import uk.gov.justice.digital.hmpps.cmd.api.client.CsrModifiedDetailDto
import java.time.LocalDate
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
        }
}
