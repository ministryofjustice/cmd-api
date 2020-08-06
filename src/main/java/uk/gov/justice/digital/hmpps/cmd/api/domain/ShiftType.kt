package uk.gov.justice.digital.hmpps.cmd.api.uk.gov.justice.digital.hmpps.cmd.api.domain

import java.util.*


enum class ShiftType(val value: String) {
    NIGHT_FINISH("night_finish"),
    NIGHT_START("night_start"),


    companion object {
        fun from(value: String): ShiftType {
            return Arrays.stream(values())
                    .filter { type -> type.value == value }
                    .findFirst().orElseThrow { IllegalArgumentException() }
        }
    }
}