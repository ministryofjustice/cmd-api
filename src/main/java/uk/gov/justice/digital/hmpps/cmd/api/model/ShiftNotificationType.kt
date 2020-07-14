package uk.gov.justice.digital.hmpps.cmd.api.uk.gov.justice.digital.hmpps.cmd.api.model

import java.util.*


enum class ShiftNotificationType(val value: String, val prose: String) {
    SHIFT("shift", "shift"),
    OVERTIME("overtime", "overtime shift"),
    SHIFT_TASK("shift_task", "activity"),
    OVERTIME_TASK("overtime_task", "overtime activity");

    companion object {
        fun from(value: String): ShiftNotificationType {
            return Arrays.stream(values())
                    .filter { type -> type.value == value }
                    .findFirst().orElseThrow { IllegalArgumentException() }
        }
    }
}