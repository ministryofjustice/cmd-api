package uk.gov.justice.digital.hmpps.cmd.api.model

data class DetailTemplate(
  val detailStart: Long,
  val detailEnd: Long,
  val isRelative: Boolean,
  val activity: String,
  val templateName: String,
)
