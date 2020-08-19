package uk.gov.justice.digital.hmpps.cmd.api.domain

import java.util.*


enum class ShiftNotificationType(val value: String, val description: String) {
    SHIFT("shift", "shift"),
    OVERTIME("overtime", "overtime shift"),
    SHIFT_TASK("shift_task", "activity"),
    TASK("task", "activity"), // The legacy code returns 'task' not 'shift_task'
    OVERTIME_TASK("overtime_task", "overtime activity");

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