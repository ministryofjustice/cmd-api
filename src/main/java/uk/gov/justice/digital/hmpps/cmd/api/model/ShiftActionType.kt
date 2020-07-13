package uk.gov.justice.digital.hmpps.cmd.api.uk.gov.justice.digital.hmpps.cmd.api.model

import java.util.*

enum class ShiftActionType(val value: String, val prose: String) {
    ADD("add", "been added"),
    EDIT("edit", "changed"),
    DELETE("delete", "been removed");

    companion object {
        fun from(value: String): ShiftActionType {
            return Arrays.stream(values())
                    .filter { type -> type.value == value }
                    .findFirst().orElseThrow { IllegalArgumentException() }
        }
    }
}