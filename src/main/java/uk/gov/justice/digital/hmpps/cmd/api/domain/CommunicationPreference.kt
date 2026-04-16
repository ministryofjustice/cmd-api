package uk.gov.justice.digital.hmpps.cmd.api.domain

enum class CommunicationPreference {
  EMAIL,
  SMS,
  NONE,
  ;

  companion object {
    fun from(value: String): CommunicationPreference = entries
      .firstOrNull { type -> type.name.equals(value, true) } ?: throw IllegalArgumentException()
  }
}
