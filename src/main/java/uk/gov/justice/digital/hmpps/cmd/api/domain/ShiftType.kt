package uk.gov.justice.digital.hmpps.cmd.api.domain

import java.util.Arrays

enum class ShiftType(val description: String) {
  SHIFT("shift"),
  OVERTIME("overtime shift");

  companion object {
    fun from(value: String): ShiftType {
      return Arrays.stream(values())
        .filter { type -> type.name.equals(value, true) }
        .findFirst().orElseThrow { IllegalArgumentException() }
    }
  }
}
