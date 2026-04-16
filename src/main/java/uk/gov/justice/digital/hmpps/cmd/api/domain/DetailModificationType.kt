package uk.gov.justice.digital.hmpps.cmd.api.domain

enum class DetailModificationType(val value: Int, val description: String) {
  ADD(1, "been added"),
  EDIT(2, "changed"),
  DELETE(3, "been removed"),
  UNCHANGED(0, "not changed"),
  ;

  companion object {
    private val map = entries.toTypedArray().associateBy(DetailModificationType::value)
    fun from(type: Int) = map.getOrDefault(type, EDIT)

    fun from(value: String): DetailModificationType = entries
      .firstOrNull { it.name.equals(value, true) } ?: throw IllegalArgumentException()
  }
}
