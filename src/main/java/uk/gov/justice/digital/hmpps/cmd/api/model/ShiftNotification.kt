package uk.gov.justice.digital.hmpps.cmd.api.model

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
)
