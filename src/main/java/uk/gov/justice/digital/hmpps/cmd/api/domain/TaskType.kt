package uk.gov.justice.digital.hmpps.cmd.api.uk.gov.justice.digital.hmpps.cmd.api.domain

import java.util.*


enum class TaskType(val value: String, val description: String) {
    UNSPECIFIC("unspecific", "Unspecific"),
    SHIFT("shift", "Shift"),
    BREAK("break", "Break"),
    REST_DAY("rest_day", "Rest Day"),
    ILLNESS("illness", "Illness"),
    HOLIDAY("holiday", "Holiday"),
    ABSENCE("absence", "Absence"),
    MEETING("meeting", "Meeting"),
    ONCALL("oncall", "On Call"),
    TRAINING_EXTERNAL("training_external", "Training - External"),
    TRAINING_INTERNAL("training_internal", "Training - Internal");

    companion object {
        fun from(value: String): TaskType {
            return Arrays.stream(values())
                    .filter { type -> type.value == value }
                    .findFirst().orElseThrow { IllegalArgumentException() }
        }
    }
}