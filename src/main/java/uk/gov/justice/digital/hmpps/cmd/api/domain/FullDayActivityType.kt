package uk.gov.justice.digital.hmpps.cmd.api.domain

import org.slf4j.LoggerFactory

enum class FullDayActivityType(val description: String) {
  BREAK("Break"),

  REST_DAY("Rest Day"),
  HOLIDAY("Annual Leave"),
  ILLNESS("Sick"),
  ABSENCE("Absence"),
  TU_OFFICIALS_LEAVE("Union Duties"),
  TU_OFFICIALS_LEAVE_2("Union Facility Time"),
  SECONDMENT("Detached Duty"),
  TOIL("TOIL"),
  TRAINING_EXTERNAL("Training - External"),
  TRAINING_INTERNAL("Training - Internal"),
  NONE("None"),
  SHIFT("Shift"),
  ;

  companion object {
    fun from(value: String?): FullDayActivityType = if (value == null) {
      log.warn("FullDayActivityType: Activity is missing - possibly using deleted Day Model")
      SHIFT
    } else {
      entries
        .find { type -> value.contains(type.description, true) }
        ?: SHIFT
    }

    private val log = LoggerFactory.getLogger(FullDayActivityType::class.java)
  }
}
