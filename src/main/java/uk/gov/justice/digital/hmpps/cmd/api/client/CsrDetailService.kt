package uk.gov.justice.digital.hmpps.cmd.api.client

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.cmd.api.model.CmdNotification
import uk.gov.justice.digital.hmpps.cmd.api.model.Detail
import uk.gov.justice.digital.hmpps.cmd.api.model.DetailTemplate
import uk.gov.justice.digital.hmpps.cmd.api.repository.CsrSqlRepository
import uk.gov.justice.hmpps.kotlin.auth.HmppsAuthenticationHolder
import java.time.LocalDate

private const val DELETECHUNKSIZE = 1000

@Service
class CsrDetailService(
  private val sqlRepository: CsrSqlRepository,
  private val authenticationFacade: HmppsAuthenticationHolder,
) {
  fun getStaffDetails(
    from: LocalDate,
    to: LocalDate,
  ): Collection<CsrDetailDto> {
    val quantumId = authenticationFacade.username!!
    log.debug("Fetching shift details for $quantumId")
    // We must pad the 'from' so that we don't miss night shift ends that start the day before our from-to range.
    val details = sqlRepository.getDetails(from.minusDays(1), to, quantumId)
    log.debug("Found {} shift details for {}", details.size, quantumId)

    // Some details are created from a template and we don't get the complete time or activity data
    // so we need to get a list of templates in our results and fetch the template data separately.
    val templates = getTemplates(details.mapNotNull { it.templateName }.distinct())

    // We merge details that refer to templates with the data from the template.
    val mergedDetails = mergeTemplatesIntoDetails(details, templates)
    log.info("Returning {} shift details for {}", mergedDetails.size, quantumId)

    return mapToDetailsDto(mergedDetails)
  }

  fun getModified(): List<CsrModifiedDetailDto> {
    val startTime = System.currentTimeMillis()

    val modified = mapCmdNotificationToDetailsDto(sqlRepository.getModified())

    log.debug("getModified: Found {}, time taken {}s", modified.size, elapsed(startTime))
    return modified
  }

  // Intentionally not transactional: we want chunks to get deleted even if one fails
  fun deleteProcessed(ids: List<Long>) {
    val startTime = System.currentTimeMillis()

    ids.chunked(DELETECHUNKSIZE).forEach {
      try {
        val deleted = sqlRepository.deleteProcessed(it)
        log.info("deleteProcessed: deleted {} rows", deleted)
      } catch (e: Exception) {
        log.error("Unexpected exception", e)
      }
    }

    log.info("deleteProcessed: Received {} ids, time taken {}s", ids.size, elapsed(startTime))
  }

  @Transactional(transactionManager = "regionTransactionManager")
  fun deleteOld(date: LocalDate): String {
    val startTime = System.currentTimeMillis()

    val deleted = sqlRepository.deleteOld(date)

    return "Deleted $deleted rows up to $date, time taken ${elapsed(startTime)}s".also {
      log.info("deleteOld: $it")
    }
  }

  private fun elapsed(startTime: Long) = (System.currentTimeMillis() - startTime) / 1000.0

  private fun getTemplates(templateNames: Collection<String>): Collection<DetailTemplate> {
    log.debug("Fetching templates: {}", templateNames)
    val templates = if (templateNames.any()) {
      sqlRepository.getDetailTemplates(templateNames)
    } else {
      setOf()
    }
    log.debug("Found {}: templates", templates.size)
    return templates
  }

  private fun mapToDetailsDto(details: Collection<Detail>): Collection<CsrDetailDto> = details.map(CsrDetailDto::from)

  private fun mapCmdNotificationToDetailsDto(notifications: List<CmdNotification>): List<CsrModifiedDetailDto> = notifications.map(CsrModifiedDetailDto::from)

  private fun mergeTemplatesIntoDetails(
    details: Collection<Detail>,
    templates: Collection<DetailTemplate>,
  ): Collection<Detail> {
    val groupedTemplates = templates.groupBy { it.templateName }

    return details
      .fold(listOf()) { acc: Collection<Detail>, el: Detail ->
        // If the detail doesn't refer to a template, add it and continue
        if (el.templateName == null) {
          acc.plus(el)
        } else {
          /*
           Details that refer to templates still have a start and end time
           One detail might be replaced by multiple template rows
           Some row's times are relative to the detail start time
           and some are not (usually Breaks).
           If the times are relative we need to change them to absolute values
           before constructing each replacement detail
           */
          val newDetails = groupedTemplates[el.templateName]?.map {
            var start = it.detailStart
            var end = it.detailEnd
            if (it.isRelative && el.startTimeInSeconds != null) {
              start += el.startTimeInSeconds
              end += el.startTimeInSeconds
            }
            Detail(
              el.quantumId,
              el.shiftModified,
              el.shiftDate,
              el.shiftType,
              start,
              end,
              it.activity,
              el.actionType,
              el.templateName,
            )
          }

          // Add the new details instead of the detail that references the template
          if (newDetails != null) {
            acc.plus(newDetails)
          } else {
            log.warn("Detail template could not be merged: ${el.templateName}")
            acc.plus(el)
          }
        }
      }
  }

  companion object {
    private val log = LoggerFactory.getLogger(CsrDetailService::class.java)
  }
}
