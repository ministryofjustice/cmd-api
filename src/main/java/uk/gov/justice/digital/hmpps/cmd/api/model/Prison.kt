package uk.gov.justice.digital.hmpps.cmd.api.model

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "prison")
data class Prison(
  @Id
  @Column(nullable = false)
  var prisonId: String,

  @Column(nullable = false)
  var csrPlanUnit: String,

  @Column(nullable = false)
  var prisonName: String,

  @Column(nullable = false)
  var region: Int,
)
