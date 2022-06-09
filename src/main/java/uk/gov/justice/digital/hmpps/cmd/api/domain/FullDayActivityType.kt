package uk.gov.justice.digital.hmpps.cmd.api.domain

import java.util.Arrays

enum class FullDayActivityType(val description: String) {
  BREAK("Break"),

  REST_DAY("Rest Day"),
  HOLIDAY("Annual Leave"),
  ILLNESS("Sick"),
  ABSENCE("Absence"),
  TU_OFFICIALS_LEAVE_DAYS("TU Officials Leave Days"),
  TU_OFFICIALS_LEAVE_HOURS("TU Officials Leave Hours"),
  SECONDMENT("Secondment"),
  TOIL("TOIL"),
  TRAINING_EXTERNAL("Training - External"),
  TRAINING_INTERNAL("Training - Internal"),
  NONE("None"),
  SHIFT("Shift"),
  ;

  companion object {
    fun from(value: String): FullDayActivityType {
      return Arrays.stream(values())
        .filter { type -> value.contains(type.description, true) }
        .findFirst().orElse(SHIFT)
    }
  }
}
