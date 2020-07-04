package uk.gov.justice.digital.hmpps.cmd.api.model

import java.time.LocalDateTime
import javax.persistence.*

@Entity
@Table(name = "shift_notification")
data class ShiftNotification(
        @Column(nullable = false)
        var quantumId: String,

        @Column(nullable = false)
        var dateTime: LocalDateTime,

        @Column(nullable = false)
        var description: String,

        @Column(nullable = false)
        var shiftDate: LocalDateTime,

        @Column(nullable = false)
        var lastModifiedDateTime: LocalDateTime,

        @Column(nullable = false)
        var read: Boolean,

        @Column(nullable = false)
        var sentSms: Boolean,

        @Column(nullable = false)
        var sentEmail: Boolean,

        @Column(nullable = false)
        var lastModifiedDateTimeInSeconds: Long,

        @Column
        var notificationType: Long,

        @Id
        @Column(name = "ID")
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        val id: Long? = null
)
