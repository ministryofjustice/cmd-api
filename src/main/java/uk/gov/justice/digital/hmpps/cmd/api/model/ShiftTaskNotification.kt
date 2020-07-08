package uk.gov.justice.digital.hmpps.cmd.api.model

import java.time.LocalDateTime
import javax.persistence.*

@Entity
@Table(name = "shift_task_notification")
data class ShiftTaskNotification(
        @Column(nullable = false)
        var quantumId: String,

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
        var acknowledged: Boolean,

        @Id
        @Column(name = "ID")
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        val id: Long? = null
)