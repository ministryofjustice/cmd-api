package uk.gov.justice.digital.hmpps.cmd.api.client

import org.slf4j.LoggerFactory
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.cmd.api.config.Regions
import uk.gov.justice.digital.hmpps.cmd.api.domain.DetailModificationType
import uk.gov.justice.digital.hmpps.cmd.api.domain.ShiftType
import uk.gov.justice.digital.hmpps.cmd.api.model.CmdNotification
import uk.gov.justice.digital.hmpps.cmd.api.model.Detail
import uk.gov.justice.hmpps.kotlin.auth.HmppsAuthenticationHolder
import java.time.LocalDate
import java.time.LocalDateTime

@Component
class CsrClient(
  private val csrRegionSelectorService: CsrRegionSelectorService,
  private val authenticationFacade: HmppsAuthenticationHolder,
  private val regionData: Regions,
) {

  @Cacheable(
    value = ["userDetails"],
    unless = "#result.size() == 0",
    key = "{ #from.toEpochDay().toString(), #to.toEpochDay().toString(), #quantumId }",
  )
  fun getDetailsForUser(from: LocalDate, to: LocalDate, region: Int?, quantumId: String): Collection<CsrDetailDto> {
    if (region != null) {
      return getDetails(from, to, region)
    } else {
      // Fallback to checking each region until we get some results.
      regionData.regions.forEach {
        val data = getDetails(from, to, it.name.toInt())
        if (!data.isEmpty()) {
          return data
        }
      }
    }
    return listOf()
  }

  fun getModified(region: Int): List<CsrModifiedDetailDto> = csrRegionSelectorService.getModified(region).also {
    log.info("getModified: found ${it.size}, Region $region")
  }

  fun deleteProcessed(region: Int, ids: List<Long>) {
    log.info("deleteProcessed: Region $region")
    csrRegionSelectorService.deleteProcessed(ids, region)

    log.info("deleteProcessed: end, Region $region")
  }

  private fun getDetails(from: LocalDate, to: LocalDate, region: Int): Collection<CsrDetailDto> {
    log.debug("User Details: finding User ${authenticationFacade.username}, Region $region")
    return csrRegionSelectorService.getStaffDetails(from, to, region).also {
      log.info("User Details: found ${it.size}, User ${authenticationFacade.username}, $from - $to, Region $region")
    }
  }

  companion object {
    private val log = LoggerFactory.getLogger(CsrClient::class.java)
  }
}

// if both start and end are this magic number then detail is a full day activity
private const val FULL_DAY_ACTIVITY = -2_147_483_648L

/*
CSR database uses positive or negative numbers to offset the shiftDate.
e.g. 04/09/2020T00:00:00 with a detail start of -10 is actually 03/09/2020T23:59:50
*/
private fun calculateDetailDateTime(shiftDate: LocalDate, detailTime: Long): LocalDateTime {
  val normalisedTime = if (detailTime == 86400L) {
    0
  } else {
    detailTime
  }

  return if (normalisedTime != FULL_DAY_ACTIVITY) {
    // plusSeconds allows negative numbers.
    shiftDate.atStartOfDay().plusSeconds(normalisedTime)
  } else {
    shiftDate.atStartOfDay()
  }
}

data class CsrDetailDto(
  val shiftType: ShiftType,
  val detailStart: LocalDateTime,
  val detailEnd: LocalDateTime,
  val activity: String? = null,
) {
  companion object {
    fun from(detail: Detail): CsrDetailDto = CsrDetailDto(
      shiftType = ShiftType.from(detail.shiftType),

      // We don't care about the shiftDate on its own
      // We want to include it in the detail's start/end values
      // So that our clients don't have to work it out themselves
      detailStart = calculateDetailDateTime(detail.shiftDate, detail.startTimeInSeconds ?: 0L),
      detailEnd = calculateDetailDateTime(detail.shiftDate, detail.endTimeInSeconds ?: 0L),

      activity = detail.activity,
    )
  }
}

data class CsrModifiedDetailDto(
  val id: Long? = null,
  val quantumId: String?,
  val shiftModified: LocalDateTime?,
  val shiftType: ShiftType,
  val detailStart: LocalDateTime,
  val detailEnd: LocalDateTime,
  val activity: String?,
  var actionType: DetailModificationType?,
) {
  companion object {
    fun from(detail: CmdNotification): CsrModifiedDetailDto = CsrModifiedDetailDto(
      id = detail.id,
      quantumId = detail.quantumId,
      shiftModified = detail.lastModified,
      shiftType = if (detail.levelId == 4000) ShiftType.OVERTIME else ShiftType.SHIFT,

      // We don't care about the shiftDate on its own
      // We want to include it in the detail's start/end values
      // So that our clients don't have to work it out themselves
      detailStart = calculateDetailDateTime(detail.onDate, detail.startTimeInSeconds ?: 0L),
      detailEnd = calculateDetailDateTime(detail.onDate, detail.endTimeInSeconds ?: 0L),

      activity = detail.activity,
      actionType = detail.actionType.let {
        when (it) {
          47012 -> DetailModificationType.DELETE
          0, 47001 -> DetailModificationType.EDIT
          47006, 47015 -> DetailModificationType.ADD
          else -> DetailModificationType.UNCHANGED
        }
      },
    )
  }
}
