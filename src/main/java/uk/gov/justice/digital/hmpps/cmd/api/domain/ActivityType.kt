package uk.gov.justice.digital.hmpps.cmd.api.domain

import java.util.*


enum class ActivityType(val description: String) {
    NONE("None"),
    SHIFT("Shift"),
    REST_DAY("Rest Day"),
    TRAINING_EXTERNAL("Training - External"),
    TRAINING_INTERNAL("Training - Internal");

    companion object {
        fun fromDescription(value: String): ActivityType {
            return Arrays.stream(values())
                    .filter { type -> type.description.equals(value, true) }
                    .findFirst().orElse(NONE)
        }
    }
} 