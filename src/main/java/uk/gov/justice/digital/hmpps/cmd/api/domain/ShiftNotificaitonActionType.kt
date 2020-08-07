package uk.gov.justice.digital.hmpps.cmd.api.domain

import java.util.*

enum class ShiftNotificaitonActionType(val value: String, val description: String) {
    ADD("add", "been added"),
    EDIT("edit", "changed"),
    DELETE("delete", "been removed"),
    UNCHANGED("unchanged", "not changed");

    companion object {
        fun from(value: String): ShiftNotificaitonActionType {
            return Arrays.stream(values())
                    .filter { type -> type.value.equals(value,true) }
                    .findFirst().orElseThrow { IllegalArgumentException() }
        }
    }
}