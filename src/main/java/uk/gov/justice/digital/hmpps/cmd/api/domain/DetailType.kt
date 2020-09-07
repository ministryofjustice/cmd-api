package uk.gov.justice.digital.hmpps.cmd.api.uk.gov.justice.digital.hmpps.cmd.api.domain

import java.util.*


enum class DetailParentType(val description: String) {
    SHIFT("shift"),
    OVERTIME("overtime shift");

    companion object {
        fun from(value: String): DetailParentType {
            return Arrays.stream(values())
                    .filter { type -> type.name.equals(value,true) }
                    .findFirst().orElseThrow { IllegalArgumentException() }
        }
    }
}