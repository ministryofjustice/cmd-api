package uk.gov.justice.digital.hmpps.cmd.api.domain

import java.util.*

enum class ShiftActionType(val description: String) {
    ADD("been added"),
    EDIT("changed"),
    DELETE("been removed"),
    UNCHANGED("not changed");

    fun equalsValue(value: String): Boolean {
        return this == from(value)
    }

    companion object {
        fun from(value: String): ShiftActionType {
            return Arrays.stream(values())
                    .filter { type -> type.name.equals(value,true) }
                    .findFirst().orElseThrow { IllegalArgumentException() }
        }
    }
}