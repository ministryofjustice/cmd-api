package uk.gov.justice.digital.hmpps.cmd.api.uk.gov.justice.digital.hmpps.cmd.api.domain

import java.util.*

enum class DetailType(val description: String) {
    UNSPECIFIC("Unspecific"),
    BREAK("Break"),

    REST_DAY("Rest Day"),
    HOLIDAY("Annual Leave"),
    ILLNESS("Illness"),
    ABSENCE("Absence"),
    TU_OFFICIALS_LEAVE_DAYS("TU Officials Leave Days"),
    TU_OFFICIALS_LEAVE_HOURS("TU Officials Leave Hours"),
    TRAINING_EXTERNAL("Training - External"),
    TRAINING_INTERNAL("Training - Internal"),
    NONE("None"),
    SHIFT("Shift"),
;

    companion object {
        fun from(value: String): DetailType {
            return Arrays.stream(values())
                    .filter { type -> value.contains(type.description, true) }
                    .findFirst().orElse(NONE)
        }
    }
} 