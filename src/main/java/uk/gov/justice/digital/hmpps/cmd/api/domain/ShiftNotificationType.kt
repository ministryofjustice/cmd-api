package uk.gov.justice.digital.hmpps.cmd.api.domain

import java.util.*


enum class ShiftNotificationType(val value: String, val description: String) {
    SHIFT("shift", "shift"),
    OVERTIME("overtime", "overtime shift");

    fun equalsValue(value: String): Boolean {
        return this == from(value)
    }

    companion object {
        fun from(value: String): ShiftNotificationType {
            return Arrays.stream(values())
                    .filter { type -> type.value.equals(value,true) }
                    .findFirst().orElseThrow { IllegalArgumentException() }
        }
    }
}