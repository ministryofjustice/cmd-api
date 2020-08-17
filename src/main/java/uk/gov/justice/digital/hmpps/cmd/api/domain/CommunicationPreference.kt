package uk.gov.justice.digital.hmpps.cmd.api.domain

import java.util.*

enum class CommunicationPreference(val value: String) {
    EMAIL("EMAIL"),
    SMS("SMS"),
    NONE("NONE");

    companion object {
        fun from(value: String): CommunicationPreference {
            return Arrays.stream(values())
                    .filter { type -> type.value.equals(value,true) }
                    .findFirst().orElseThrow { IllegalArgumentException() }
        }
    }
}