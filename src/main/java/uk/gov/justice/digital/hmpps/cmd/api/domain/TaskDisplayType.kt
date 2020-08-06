package uk.gov.justice.digital.hmpps.cmd.api.uk.gov.justice.digital.hmpps.cmd.api.domain

import java.util.*


enum class TaskDisplayType(val value: String) {
    DAY_START("day_start"),
    DAY_FINISH("day_finish"),
    NIGHT_START("night_start"),
    NIGHT_FINISH("night_finish");

    companion object {
        fun from(value: String): TaskDisplayType {
            return Arrays.stream(values())
                    .filter { type -> type.value == value }
                    .findFirst().orElseThrow { IllegalArgumentException() }
        }
    }
}