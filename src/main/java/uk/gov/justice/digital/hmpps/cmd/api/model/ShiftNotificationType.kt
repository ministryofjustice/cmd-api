package uk.gov.justice.digital.hmpps.cmd.api.uk.gov.justice.digital.hmpps.cmd.api.model

enum class ShiftNotificationType(val value: String) {
    SHIFT("shift"),
    OVERTIME("overtime"),
    SHIFT_TASK("activity"),
    OVERTIME_TASK("overtime activity")
}