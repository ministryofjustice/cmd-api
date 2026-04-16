package uk.gov.justice.digital.hmpps.cmd.api.domain

enum class ShiftType(val value: Int, val description: String) {
  SHIFT(0, "shift"),
  OVERTIME(1, "overtime shift"),
  ;

  companion object {
    private val map = entries.associateBy(ShiftType::value)

    fun from(type: Int) = map.getOrDefault(type, SHIFT)

    fun from(value: String): ShiftType = entries
      .firstOrNull { it.name.equals(value, true) }
      ?: throw IllegalArgumentException()
  }
}
