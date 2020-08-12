package uk.gov.justice.digital.hmpps.cmd.api.domain

import java.util.*

enum class ShiftNotificaitonActionType(val value: String, val description: String) {
    ADD("add", "been added"),
    EDIT("edit", "changed"),
    DELETE("delete", "been removed");

    companion object {
        fun from(value: String): ShiftNotificaitonActionType {
            return Arrays.stream(values())
                    .filter { type -> type.value == value }
                    .findFirst().orElseThrow { IllegalArgumentException() }
        }
    }
}