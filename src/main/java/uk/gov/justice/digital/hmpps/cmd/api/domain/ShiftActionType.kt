package uk.gov.justice.digital.hmpps.cmd.api.uk.gov.justice.digital.hmpps.cmd.api.domain

import java.util.*

enum class ShiftActionType(val value: String, val description: String) {
    ADD("add", "been added"),
    EDIT("edit", "changed"),
    DELETE("delete", "been removed"),
    UNCHANGED("unchanged", "not changed");

    fun equalsValue(value: String): Boolean {
        return this == from(value)
    }

    companion object {
        fun from(value: String): ShiftActionType {
            return Arrays.stream(values())
                    .filter { type -> type.value.equals(value,true) }
                    .findFirst().orElseThrow { IllegalArgumentException() }
        }
    }
}