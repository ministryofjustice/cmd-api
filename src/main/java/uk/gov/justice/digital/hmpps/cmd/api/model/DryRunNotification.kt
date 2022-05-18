package uk.gov.justice.digital.hmpps.cmd.api.model

import uk.gov.justice.digital.hmpps.cmd.api.client.CsrModifiedDetailDto
import uk.gov.justice.digital.hmpps.cmd.api.domain.DetailModificationType
import uk.gov.justice.digital.hmpps.cmd.api.uk.gov.justice.digital.hmpps.cmd.api.domain.ShiftType
import java.time.LocalDateTime
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.EnumType
import javax.persistence.Enumerated
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.Table

@Entity
@Table(name = "dry_run_notification")
data class DryRunNotification(
  @Id
  @Column(name = "ID")
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  var id: Long = 0,

  @Column(nullable = false)
  var quantumId: String,

  @Column(nullable = false)
  var shiftModified: LocalDateTime,

  @Column(nullable = false)
  var detailStart: LocalDateTime,

  @Column(nullable = false)
  var detailEnd: LocalDateTime,

  @Column
  var activity: String?,

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  var parentType: ShiftType,

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  var actionType: DetailModificationType,

  @Column(nullable = false)
  var processed: Boolean
) {

  companion object {

    fun fromDto(dtoCsrs: Collection<CsrModifiedDetailDto>): Collection<DryRunNotification> {
      return dtoCsrs.map { fromDto(it) }
    }

    fun fromDto(dtoCsr: CsrModifiedDetailDto): DryRunNotification {
      return DryRunNotification(
        quantumId = dtoCsr.quantumId!!,
        shiftModified = dtoCsr.shiftModified!!,
        detailStart = dtoCsr.detailStart,
        detailEnd = dtoCsr.detailEnd,
        activity = dtoCsr.activity,
        parentType = dtoCsr.shiftType,
        actionType = dtoCsr.actionType!!,
        processed = false
      )
    }
  }
}
