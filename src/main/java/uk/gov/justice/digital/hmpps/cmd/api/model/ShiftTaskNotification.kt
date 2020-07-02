package uk.gov.justice.digital.hmpps.cmd.api.model

import org.hibernate.annotations.GenericGenerator
import java.time.LocalDateTime
import java.util.*
import javax.persistence.*

@Entity
@Table(name = "shift_task_notification")
data class ShiftTaskNotification(
        @Column(nullable = false)
        var quantumId: String,

        @Column(nullable = false)
        var dateTime: LocalDateTime,

        @Column(nullable = false)
        var description: String,

        @Column(nullable = false)
        var taskDate: LocalDateTime,

        @Column(nullable = false)
        var taskStartTimeInSeconds: Int,

        @Column(nullable = false)
        var taskEndTimeInSeconds: Int,

        @Column(nullable = false)
        var activity: String,

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

        @Id
        @GeneratedValue(generator = "UUID")
        @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
        @Column(name = "uuid", updatable = false, nullable = false)
        var id: UUID? = null
)