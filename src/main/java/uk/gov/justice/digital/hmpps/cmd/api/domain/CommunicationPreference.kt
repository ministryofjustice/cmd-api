package uk.gov.justice.digital.hmpps.cmd.api.domain

import java.util.Arrays

enum class CommunicationPreference {
  EMAIL,
  SMS,
  NONE,
  ;

  companion object {
    fun from(value: String): CommunicationPreference {
      return Arrays.stream(values())
        .filter { type -> type.name.equals(value, true) }
        .findFirst().orElseThrow { IllegalArgumentException() }
    }
  }
}
