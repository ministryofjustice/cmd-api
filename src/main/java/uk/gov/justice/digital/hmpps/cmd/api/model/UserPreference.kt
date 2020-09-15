package uk.gov.justice.digital.hmpps.cmd.api.model

import uk.gov.justice.digital.hmpps.cmd.api.domain.CommunicationPreference
import java.time.LocalDate
import javax.persistence.*

@Entity
@Table(name = "user_preference")
data class UserPreference(
        @Id
        @Column(nullable = false)
        var quantumId: String,

        @Column
        var snoozeUntil: LocalDate? = null,

        @Column
        var email: String? = null,

        @Column
        var sms: String? = null,

        @Enumerated(EnumType.STRING)
        @Column
        var commPref: CommunicationPreference = CommunicationPreference.NONE
)