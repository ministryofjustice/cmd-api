package uk.gov.justice.digital.hmpps.cmd.api.model

import java.time.LocalDateTime
import javax.persistence.*

@Entity
@Table(name = "shift_notification")
data class ShiftNotification(
        @Column(nullable = false)
        var quantumId: String,

        @Column(nullable = false)
        var description: String,

        @Column(nullable = false)
        var shiftDate: LocalDateTime,

        @Column(nullable = false)
        var lastModified: LocalDateTime,

        @Column
        var notificationType: Long,

        @Column(nullable = false)
        var processed: Boolean,

        @Id
        @Column(name = "ID")
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        val id: Long? = null
)
