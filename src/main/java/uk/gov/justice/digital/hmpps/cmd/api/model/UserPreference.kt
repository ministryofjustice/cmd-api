package uk.gov.justice.digital.hmpps.cmd.api.model

import org.hibernate.annotations.GenericGenerator
import java.time.LocalDate
import java.util.*
import javax.persistence.*

@Entity
@Table(name = "user_preference")
data class UserPreference(
        @Column(nullable = false)
        var quantumId: String,

        @Column(nullable = false)
        var snoozeUntil: LocalDate,

        @Id
        @GeneratedValue(generator = "UUID")
        @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
        @Column(name = "uuid", updatable = false, nullable = false)
        var id: UUID? = null
)