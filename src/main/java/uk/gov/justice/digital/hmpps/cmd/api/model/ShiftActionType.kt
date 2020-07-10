package uk.gov.justice.digital.hmpps.cmd.api.uk.gov.justice.digital.hmpps.cmd.api.model

enum class ShiftActionType(val value: String) {
    ADD("been added"),
    EDIT("changed"),
    DELETE("been removed")
}