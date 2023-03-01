package uk.gov.justice.digital.hmpps.cmd.api.domain

import java.util.Arrays

enum class DetailModificationType(val description: String) {
  ADD("been added"),
  EDIT("changed"),
  DELETE("been removed"),
  UNCHANGED("not changed"),
  ;

  companion object {
    fun from(value: String): DetailModificationType {
      return Arrays.stream(values())
        .filter { type -> type.name.equals(value, true) }
        .findFirst().orElseThrow { IllegalArgumentException() }
    }
  }
}
