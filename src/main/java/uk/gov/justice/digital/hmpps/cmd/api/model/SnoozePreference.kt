package uk.gov.justice.digital.hmpps.cmd.api.uk.gov.justice.digital.hmpps.cmd.api.model

import org.hibernate.annotations.GenericGenerator
import java.time.LocalDate
import java.util.*
import javax.persistence.*

@Entity
@Table(name = "notification_snooze_preference")
data class SnoozePreference (
        @Column(nullable = false)
        var quantumId: String,

        @Column(nullable = false)
        var snooze: LocalDate?
) {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "uuid", updatable = false, nullable = false)
    var id: UUID? = null
}