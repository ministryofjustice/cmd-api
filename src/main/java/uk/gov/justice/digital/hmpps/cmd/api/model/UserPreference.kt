package uk.gov.justice.digital.hmpps.cmd.api.model

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.Table
import uk.gov.justice.digital.hmpps.cmd.api.domain.CommunicationPreference
import java.time.LocalDate

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
  var commPref: CommunicationPreference = CommunicationPreference.NONE,
)
