package uk.gov.justice.digital.hmpps.cmd.api.uk.gov.justice.digital.hmpps.cmd.api.domain

import java.util.*


enum class EntityType(val value: String, val description: String) {
    SHIFT("shift", "shift"),
    OVERTIME("overtime", "overtime shift");

    companion object {
        fun from(value: String): EntityType {
            return Arrays.stream(values())
                    .filter { type -> type.value.equals(value,true) }
                    .findFirst().orElseThrow { IllegalArgumentException() }
        }
    }
}