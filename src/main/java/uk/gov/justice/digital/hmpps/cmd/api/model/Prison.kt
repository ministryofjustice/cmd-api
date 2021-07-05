package uk.gov.justice.digital.hmpps.cmd.api.model

import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Table

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
  var region: Int
)
