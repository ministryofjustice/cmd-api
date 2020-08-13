package uk.gov.justice.digital.hmpps.cmd.api.domain

import java.util.*


enum class TaskDisplayType(val value: String) {
    DAY_START("day_start"),
    DAY_FINISH("day_finish"),
    NIGHT_START("night_start"),
    NIGHT_FINISH("night_finish"),
    OVERTIME_DAY_START("overtime_day_start"),
    OVERTIME_DAY_FINISH("overtime_day_finish"),
    OVERTIME_NIGHT_START("overtime_night_start"),
    OVERTIME_NIGHT_FINISH("overtime_night_finish");

    companion object {
        fun from(value: String): TaskDisplayType {
            return Arrays.stream(values())
                    .filter { type -> type.value == value }
                    .findFirst().orElseThrow { IllegalArgumentException() }
        }
    }
} 