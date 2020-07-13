package uk.gov.justice.digital.hmpps.cmd.api.uk.gov.justice.digital.hmpps.cmd.api.model

import java.util.*

enum class CommunicationPreference(val stringValue: String) {
    EMAIL("EMAIL"),
    SMS("SMS"),
    NONE("NONE");

    companion object {
        fun from(value: String): CommunicationPreference {
            return Arrays.stream(values())
                    .filter { type -> type.stringValue == value }
                    .findFirst().orElseThrow { IllegalArgumentException() }
        }
    }
}