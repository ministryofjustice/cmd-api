package uk.gov.justice.digital.hmpps.cmd.api.domain

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.cmd.api.uk.gov.justice.digital.hmpps.cmd.api.domain.NotificationDescription
import uk.gov.justice.digital.hmpps.cmd.api.uk.gov.justice.digital.hmpps.cmd.api.model.CommunicationPreference
import uk.gov.justice.digital.hmpps.cmd.api.uk.gov.justice.digital.hmpps.cmd.api.model.ShiftActionType
import uk.gov.justice.digital.hmpps.cmd.api.uk.gov.justice.digital.hmpps.cmd.api.model.ShiftNotificationType
import java.time.Clock
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId


class NotificationDescriptionTest {

    private val clock = Clock.fixed(LocalDate.of(2020, 5, 3).atStartOfDay(ZoneId.systemDefault()).toInstant(), ZoneId.systemDefault())
    private val now = LocalDateTime.now(clock)

    @Nested
    @DisplayName("Shift, Edit, This year tests")
    inner class ShiftEditThisYear {
        @Test
        fun `Should return shift changed None`() {
            val result = NotificationDescription.getNotificationDescription(ShiftNotificationType.SHIFT, ShiftActionType.EDIT, now, CommunicationPreference.NONE, clock)
            assertThat(result).isEqualTo("Your shift on Sunday, 3rd May has changed.")
        }

        @Test
        fun `Should return shift changed Sms`() {
            val result = NotificationDescription.getNotificationDescription(ShiftNotificationType.SHIFT, ShiftActionType.EDIT, now, CommunicationPreference.SMS, clock)
            assertThat(result).isEqualTo("Your shift on Sunday, 3rd May has changed.")
        }

        @Test
        fun `Should return shift changed Email`() {
            val result = NotificationDescription.getNotificationDescription(ShiftNotificationType.SHIFT, ShiftActionType.EDIT, now, CommunicationPreference.EMAIL, clock)
            assertThat(result).isEqualTo("* Your shift on Sunday, 3rd May has changed.")
        }
    }

    @Nested
    @DisplayName("Shift, Edit, Next year tests")
    inner class ShiftEditNextYear {
        @Test
        fun `Should return shift changed None`() {
            val result = NotificationDescription.getNotificationDescription(ShiftNotificationType.SHIFT, ShiftActionType.EDIT, now.plusYears(1), CommunicationPreference.NONE, clock)
            assertThat(result).isEqualTo("Your shift on Monday, 3rd May, 2021 has changed.")
        }

        @Test
        fun `Should return shift changed Sms`() {
            val result = NotificationDescription.getNotificationDescription(ShiftNotificationType.SHIFT, ShiftActionType.EDIT, now.plusYears(1), CommunicationPreference.SMS, clock)
            assertThat(result).isEqualTo("Your shift on Monday, 3rd May, 2021 has changed.")
        }

        @Test
        fun `Should return shift changed Email`() {
            val result = NotificationDescription.getNotificationDescription(ShiftNotificationType.SHIFT, ShiftActionType.EDIT, now.plusYears(1), CommunicationPreference.EMAIL, clock)
            assertThat(result).isEqualTo("* Your shift on Monday, 3rd May, 2021 has changed.")
        }
    }

    @Nested
    @DisplayName("Overtime Shift, Edit, This year tests")
    inner class OvertimeShiftEditThisYear {
        @Test
        fun `Should return overtime shift changed None`() {
            val result = NotificationDescription.getNotificationDescription(ShiftNotificationType.OVERTIME, ShiftActionType.EDIT, now, CommunicationPreference.NONE, clock)
            assertThat(result).isEqualTo("Your overtime shift on Sunday, 3rd May has changed.")
        }

        @Test
        fun `Should return overtime shift changed Sms`() {
            val result = NotificationDescription.getNotificationDescription(ShiftNotificationType.OVERTIME, ShiftActionType.EDIT, now, CommunicationPreference.SMS, clock)
            assertThat(result).isEqualTo("Your overtime shift on Sunday, 3rd May has changed.")
        }

        @Test
        fun `Should return overtime shift changed Email`() {
            val result = NotificationDescription.getNotificationDescription(ShiftNotificationType.OVERTIME, ShiftActionType.EDIT, now, CommunicationPreference.EMAIL, clock)
            assertThat(result).isEqualTo("* Your overtime shift on Sunday, 3rd May has changed.")
        }
    }

    @Nested
    @DisplayName("Overtime Shift, Edit, This year tests")
    inner class OvertimeShiftEditNextYear {
        @Test
        fun `Should return overtime shift changed None`() {
            val result = NotificationDescription.getNotificationDescription(ShiftNotificationType.OVERTIME, ShiftActionType.EDIT, now.plusYears(1), CommunicationPreference.NONE, clock)
            assertThat(result).isEqualTo("Your overtime shift on Monday, 3rd May, 2021 has changed.")
        }

        @Test
        fun `Should return overtime shift changed Sms`() {
            val result = NotificationDescription.getNotificationDescription(ShiftNotificationType.OVERTIME, ShiftActionType.EDIT, now.plusYears(1), CommunicationPreference.SMS, clock)
            assertThat(result).isEqualTo("Your overtime shift on Monday, 3rd May, 2021 has changed.")
        }

        @Test
        fun `Should return overtime shift changed Email`() {
            val result = NotificationDescription.getNotificationDescription(ShiftNotificationType.OVERTIME, ShiftActionType.EDIT, now.plusYears(1), CommunicationPreference.EMAIL, clock)
            assertThat(result).isEqualTo("* Your overtime shift on Monday, 3rd May, 2021 has changed.")
        }
    }

    @Nested
    @DisplayName("Shift, Add, This year tests")
    inner class ShiftAddThisYear {
        @Test
        fun `Should return shift changed None`() {
            val result = NotificationDescription.getNotificationDescription(ShiftNotificationType.SHIFT, ShiftActionType.ADD, now, CommunicationPreference.NONE, clock)
            assertThat(result).isEqualTo("Your shift on Sunday, 3rd May has been added.")
        }

        @Test
        fun `Should return shift changed Sms`() {
            val result = NotificationDescription.getNotificationDescription(ShiftNotificationType.SHIFT, ShiftActionType.ADD, now, CommunicationPreference.SMS, clock)
            assertThat(result).isEqualTo("Your shift on Sunday, 3rd May has been added.")
        }

        @Test
        fun `Should return shift changed Email`() {
            val result = NotificationDescription.getNotificationDescription(ShiftNotificationType.SHIFT, ShiftActionType.ADD, now, CommunicationPreference.EMAIL, clock)
            assertThat(result).isEqualTo("* Your shift on Sunday, 3rd May has been added.")
        }
    }

    @Nested
    @DisplayName("Shift, Add, Next year tests")
    inner class ShiftAddNextYear {
        @Test
        fun `Should return shift changed None`() {
            val result = NotificationDescription.getNotificationDescription(ShiftNotificationType.SHIFT, ShiftActionType.ADD, now.plusYears(1), CommunicationPreference.NONE, clock)
            assertThat(result).isEqualTo("Your shift on Monday, 3rd May, 2021 has been added.")
        }

        @Test
        fun `Should return shift changed Sms`() {
            val result = NotificationDescription.getNotificationDescription(ShiftNotificationType.SHIFT, ShiftActionType.ADD, now.plusYears(1), CommunicationPreference.SMS, clock)
            assertThat(result).isEqualTo("Your shift on Monday, 3rd May, 2021 has been added.")
        }

        @Test
        fun `Should return shift changed Email`() {
            val result = NotificationDescription.getNotificationDescription(ShiftNotificationType.SHIFT, ShiftActionType.ADD, now.plusYears(1), CommunicationPreference.EMAIL, clock)
            assertThat(result).isEqualTo("* Your shift on Monday, 3rd May, 2021 has been added.")
        }
    }

    @Nested
    @DisplayName("Overtime Shift, Add, This year tests")
    inner class OvertimeShiftAddThisYear {
        @Test
        fun `Should return overtime shift changed None`() {
            val result = NotificationDescription.getNotificationDescription(ShiftNotificationType.OVERTIME, ShiftActionType.ADD, now, CommunicationPreference.NONE, clock)
            assertThat(result).isEqualTo("Your overtime shift on Sunday, 3rd May has been added.")
        }

        @Test
        fun `Should return overtime shift changed Sms`() {
            val result = NotificationDescription.getNotificationDescription(ShiftNotificationType.OVERTIME, ShiftActionType.ADD, now, CommunicationPreference.SMS, clock)
            assertThat(result).isEqualTo("Your overtime shift on Sunday, 3rd May has been added.")
        }

        @Test
        fun `Should return overtime shift changed Email`() {
            val result = NotificationDescription.getNotificationDescription(ShiftNotificationType.OVERTIME, ShiftActionType.ADD, now, CommunicationPreference.EMAIL, clock)
            assertThat(result).isEqualTo("* Your overtime shift on Sunday, 3rd May has been added.")
        }
    }

    @Nested
    @DisplayName("Overtime Shift, Add, This year tests")
    inner class OvertimeShiftAddNextYear {
        @Test
        fun `Should return overtime shift changed None`() {
            val result = NotificationDescription.getNotificationDescription(ShiftNotificationType.OVERTIME, ShiftActionType.ADD, now.plusYears(1), CommunicationPreference.NONE, clock)
            assertThat(result).isEqualTo("Your overtime shift on Monday, 3rd May, 2021 has been added.")
        }

        @Test
        fun `Should return overtime shift changed Sms`() {
            val result = NotificationDescription.getNotificationDescription(ShiftNotificationType.OVERTIME, ShiftActionType.ADD, now.plusYears(1), CommunicationPreference.SMS, clock)
            assertThat(result).isEqualTo("Your overtime shift on Monday, 3rd May, 2021 has been added.")
        }

        @Test
        fun `Should return overtime shift changed Email`() {
            val result = NotificationDescription.getNotificationDescription(ShiftNotificationType.OVERTIME, ShiftActionType.ADD, now.plusYears(1), CommunicationPreference.EMAIL, clock)
            assertThat(result).isEqualTo("* Your overtime shift on Monday, 3rd May, 2021 has been added.")
        }
    }

    @Nested
    @DisplayName("Shift, Remove, This year tests")
    inner class ShiftRemoveThisYear {
        @Test
        fun `Should return shift changed None`() {
            val result = NotificationDescription.getNotificationDescription(ShiftNotificationType.SHIFT, ShiftActionType.DELETE, now, CommunicationPreference.NONE, clock)
            assertThat(result).isEqualTo("Your shift on Sunday, 3rd May has been removed.")
        }

        @Test
        fun `Should return shift changed Sms`() {
            val result = NotificationDescription.getNotificationDescription(ShiftNotificationType.SHIFT, ShiftActionType.DELETE, now, CommunicationPreference.SMS, clock)
            assertThat(result).isEqualTo("Your shift on Sunday, 3rd May has been removed.")
        }

        @Test
        fun `Should return shift changed Email`() {
            val result = NotificationDescription.getNotificationDescription(ShiftNotificationType.SHIFT, ShiftActionType.DELETE, now, CommunicationPreference.EMAIL, clock)
            assertThat(result).isEqualTo("* Your shift on Sunday, 3rd May has been removed.")
        }
    }

    @Nested
    @DisplayName("Shift, Remove, Next year tests")
    inner class ShiftRemoveNextYear {
        @Test
        fun `Should return shift changed None`() {
            val result = NotificationDescription.getNotificationDescription(ShiftNotificationType.SHIFT, ShiftActionType.DELETE, now.plusYears(1), CommunicationPreference.NONE, clock)
            assertThat(result).isEqualTo("Your shift on Monday, 3rd May, 2021 has been removed.")
        }

        @Test
        fun `Should return shift changed Sms`() {
            val result = NotificationDescription.getNotificationDescription(ShiftNotificationType.SHIFT, ShiftActionType.DELETE, now.plusYears(1), CommunicationPreference.SMS, clock)
            assertThat(result).isEqualTo("Your shift on Monday, 3rd May, 2021 has been removed.")
        }

        @Test
        fun `Should return shift changed Email`() {
            val result = NotificationDescription.getNotificationDescription(ShiftNotificationType.SHIFT, ShiftActionType.DELETE, now.plusYears(1), CommunicationPreference.EMAIL, clock)
            assertThat(result).isEqualTo("* Your shift on Monday, 3rd May, 2021 has been removed.")
        }
    }

    @Nested
    @DisplayName("Overtime Shift, Remove, This year tests")
    inner class OvertimeShiftRemoveThisYear {
        @Test
        fun `Should return overtime shift changed None`() {
            val result = NotificationDescription.getNotificationDescription(ShiftNotificationType.OVERTIME, ShiftActionType.DELETE, now, CommunicationPreference.NONE, clock)
            assertThat(result).isEqualTo("Your overtime shift on Sunday, 3rd May has been removed.")
        }

        @Test
        fun `Should return overtime shift changed Sms`() {
            val result = NotificationDescription.getNotificationDescription(ShiftNotificationType.OVERTIME, ShiftActionType.DELETE, now, CommunicationPreference.SMS, clock)
            assertThat(result).isEqualTo("Your overtime shift on Sunday, 3rd May has been removed.")
        }

        @Test
        fun `Should return overtime shift changed Email`() {
            val result = NotificationDescription.getNotificationDescription(ShiftNotificationType.OVERTIME, ShiftActionType.DELETE, now, CommunicationPreference.EMAIL, clock)
            assertThat(result).isEqualTo("* Your overtime shift on Sunday, 3rd May has been removed.")
        }
    }

    @Nested
    @DisplayName("Overtime Shift, Remove, This year tests")
    inner class OvertimeShiftRemoveNextYear {
        @Test
        fun `Should return overtime shift changed None`() {
            val result = NotificationDescription.getNotificationDescription(ShiftNotificationType.OVERTIME, ShiftActionType.DELETE, now.plusYears(1), CommunicationPreference.NONE, clock)
            assertThat(result).isEqualTo("Your overtime shift on Monday, 3rd May, 2021 has been removed.")
        }

        @Test
        fun `Should return overtime shift changed Sms`() {
            val result = NotificationDescription.getNotificationDescription(ShiftNotificationType.OVERTIME, ShiftActionType.DELETE, now.plusYears(1), CommunicationPreference.SMS, clock)
            assertThat(result).isEqualTo("Your overtime shift on Monday, 3rd May, 2021 has been removed.")
        }

        @Test
        fun `Should return overtime shift changed Email`() {
            val result = NotificationDescription.getNotificationDescription(ShiftNotificationType.OVERTIME, ShiftActionType.DELETE, now.plusYears(1), CommunicationPreference.EMAIL, clock)
            assertThat(result).isEqualTo("* Your overtime shift on Monday, 3rd May, 2021 has been removed.")
        }
    }

    @Nested
    @DisplayName("Shift Task, Edit, This year tests")
    inner class ShiftTaskEditThisYear {
        @Test
        fun `Should return shift task changed None`() {
            val result = NotificationDescription.getNotificationDescription(ShiftNotificationType.SHIFT_TASK, ShiftActionType.EDIT, now, CommunicationPreference.NONE, clock, "Test Duty", 1234L, 12345L)
            assertThat(result).isEqualTo("Your activity on Sunday, 3rd May (Test Duty, 00:20:34 - 03:25:45) has changed.")
        }

        @Test
        fun `Should return shift changed Sms`() {
            val result = NotificationDescription.getNotificationDescription(ShiftNotificationType.SHIFT_TASK, ShiftActionType.EDIT, now, CommunicationPreference.SMS, clock, "Test Duty", 1234L, 12345L)
            assertThat(result).isEqualTo("Your activity on Sunday, 3rd May (Test Duty, 00:20:34 - 03:25:45) has changed.")
        }

        @Test
        fun `Should return shift changed Email`() {
            val result = NotificationDescription.getNotificationDescription(ShiftNotificationType.SHIFT_TASK, ShiftActionType.EDIT, now, CommunicationPreference.EMAIL, clock, "Test Duty", 1234L, 12345L)
            assertThat(result).isEqualTo("* Your activity on Sunday, 3rd May (Test Duty, 00:20:34 - 03:25:45) has changed.")
        }
    }

    @Nested
    @DisplayName("Overtime Shift Task, Edit, This year tests")
    inner class OvertimeShiftTaskEditThisYear {
        @Test
        fun `Should return shift task changed None`() {
            val result = NotificationDescription.getNotificationDescription(ShiftNotificationType.OVERTIME_TASK, ShiftActionType.EDIT, now, CommunicationPreference.NONE, clock, "Test Duty", 1234L, 12345L)
            assertThat(result).isEqualTo("Your overtime activity on Sunday, 3rd May (Test Duty, 00:20:34 - 03:25:45) has changed.")
        }

        @Test
        fun `Should return shift changed Sms`() {
            val result = NotificationDescription.getNotificationDescription(ShiftNotificationType.OVERTIME_TASK, ShiftActionType.EDIT, now, CommunicationPreference.SMS, clock, "Test Duty", 1234L, 12345L)
            assertThat(result).isEqualTo("Your overtime activity on Sunday, 3rd May (Test Duty, 00:20:34 - 03:25:45) has changed.")
        }

        @Test
        fun `Should return shift changed Email`() {
            val result = NotificationDescription.getNotificationDescription(ShiftNotificationType.OVERTIME_TASK, ShiftActionType.EDIT, now, CommunicationPreference.EMAIL, clock, "Test Duty", 1234L, 12345L)
            assertThat(result).isEqualTo("* Your overtime activity on Sunday, 3rd May (Test Duty, 00:20:34 - 03:25:45) has changed.")
        }
    }

    @Nested
    @DisplayName("Shift Task, Edit, next year tests")
    inner class ShiftTaskEditNextYear {
        @Test
        fun `Should return shift task changed None`() {
            val result = NotificationDescription.getNotificationDescription(ShiftNotificationType.SHIFT_TASK, ShiftActionType.EDIT, now.plusYears(1), CommunicationPreference.NONE, clock, "Test Duty", 1234L, 12345L)
            assertThat(result).isEqualTo("Your activity on Monday, 3rd May, 2021 (Test Duty, 00:20:34 - 03:25:45) has changed.")
        }

        @Test
        fun `Should return shift changed Sms`() {
            val result = NotificationDescription.getNotificationDescription(ShiftNotificationType.SHIFT_TASK, ShiftActionType.EDIT, now.plusYears(1), CommunicationPreference.SMS, clock, "Test Duty", 1234L, 12345L)
            assertThat(result).isEqualTo("Your activity on Monday, 3rd May, 2021 (Test Duty, 00:20:34 - 03:25:45) has changed.")
        }

        @Test
        fun `Should return shift changed Email`() {
            val result = NotificationDescription.getNotificationDescription(ShiftNotificationType.SHIFT_TASK, ShiftActionType.EDIT, now.plusYears(1), CommunicationPreference.EMAIL, clock, "Test Duty", 1234L, 12345L)
            assertThat(result).isEqualTo("* Your activity on Monday, 3rd May, 2021 (Test Duty, 00:20:34 - 03:25:45) has changed.")
        }
    }

    @Nested
    @DisplayName("Overtime Shift Task, Edit, next year tests")
    inner class OvertimeShiftTaskEditNextYear {
        @Test
        fun `Should return shift task changed None`() {
            val result = NotificationDescription.getNotificationDescription(ShiftNotificationType.OVERTIME_TASK, ShiftActionType.EDIT, now.plusYears(1), CommunicationPreference.NONE, clock, "Test Duty", 1234L, 12345L)
            assertThat(result).isEqualTo("Your overtime activity on Monday, 3rd May, 2021 (Test Duty, 00:20:34 - 03:25:45) has changed.")
        }

        @Test
        fun `Should return shift changed Sms`() {
            val result = NotificationDescription.getNotificationDescription(ShiftNotificationType.OVERTIME_TASK, ShiftActionType.EDIT, now.plusYears(1), CommunicationPreference.SMS, clock, "Test Duty", 1234L, 12345L)
            assertThat(result).isEqualTo("Your overtime activity on Monday, 3rd May, 2021 (Test Duty, 00:20:34 - 03:25:45) has changed.")
        }

        @Test
        fun `Should return shift changed Email`() {
            val result = NotificationDescription.getNotificationDescription(ShiftNotificationType.OVERTIME_TASK, ShiftActionType.EDIT, now.plusYears(1), CommunicationPreference.EMAIL, clock, "Test Duty", 1234L, 12345L)
            assertThat(result).isEqualTo("* Your overtime activity on Monday, 3rd May, 2021 (Test Duty, 00:20:34 - 03:25:45) has changed.")
        }
    }

    @Nested
    @DisplayName("Shift Task, Add, This year tests")
    inner class ShiftTaskAddThisYear {
        @Test
        fun `Should return shift task changed None`() {
            val result = NotificationDescription.getNotificationDescription(ShiftNotificationType.SHIFT_TASK, ShiftActionType.ADD, now, CommunicationPreference.NONE, clock, "Test Duty", 1234L, 12345L)
            assertThat(result).isEqualTo("Your activity on Sunday, 3rd May (Test Duty, 00:20:34 - 03:25:45) has been added.")
        }

        @Test
        fun `Should return shift changed Sms`() {
            val result = NotificationDescription.getNotificationDescription(ShiftNotificationType.SHIFT_TASK, ShiftActionType.ADD, now, CommunicationPreference.NONE, clock, "Test Duty", 1234L, 12345L)
            assertThat(result).isEqualTo("Your activity on Sunday, 3rd May (Test Duty, 00:20:34 - 03:25:45) has been added.")
        }

        @Test
        fun `Should return shift changed Email`() {
            val result = NotificationDescription.getNotificationDescription(ShiftNotificationType.SHIFT_TASK, ShiftActionType.ADD, now, CommunicationPreference.EMAIL, clock, "Test Duty", 1234L, 12345L)
            assertThat(result).isEqualTo("* Your activity on Sunday, 3rd May (Test Duty, 00:20:34 - 03:25:45) has been added.")
        }
    }

    @Nested
    @DisplayName("Overtime Shift Task, Add, This year tests")
    inner class OvertimeShiftTaskAddThisYear {
        @Test
        fun `Should return shift task changed None`() {
            val result = NotificationDescription.getNotificationDescription(ShiftNotificationType.OVERTIME_TASK, ShiftActionType.ADD, now, CommunicationPreference.NONE, clock, "Test Duty", 1234L, 12345L)
            assertThat(result).isEqualTo("Your overtime activity on Sunday, 3rd May (Test Duty, 00:20:34 - 03:25:45) has been added.")
        }

        @Test
        fun `Should return shift changed Sms`() {
            val result = NotificationDescription.getNotificationDescription(ShiftNotificationType.OVERTIME_TASK, ShiftActionType.ADD, now, CommunicationPreference.NONE, clock, "Test Duty", 1234L, 12345L)
            assertThat(result).isEqualTo("Your overtime activity on Sunday, 3rd May (Test Duty, 00:20:34 - 03:25:45) has been added.")
        }

        @Test
        fun `Should return shift changed Email`() {
            val result = NotificationDescription.getNotificationDescription(ShiftNotificationType.OVERTIME_TASK, ShiftActionType.ADD, now, CommunicationPreference.EMAIL, clock, "Test Duty", 1234L, 12345L)
            assertThat(result).isEqualTo("* Your overtime activity on Sunday, 3rd May (Test Duty, 00:20:34 - 03:25:45) has been added.")
        }
    }

    @Nested
    @DisplayName("Shift Task, Add, next year tests")
    inner class ShiftTaskAddNextYear {
        @Test
        fun `Should return shift task changed None`() {
            val result = NotificationDescription.getNotificationDescription(ShiftNotificationType.SHIFT_TASK, ShiftActionType.ADD, now.plusYears(1), CommunicationPreference.NONE, clock, "Test Duty", 1234L, 12345L)
            assertThat(result).isEqualTo("Your activity on Monday, 3rd May, 2021 (Test Duty, 00:20:34 - 03:25:45) has been added.")
        }

        @Test
        fun `Should return shift changed Sms`() {
            val result = NotificationDescription.getNotificationDescription(ShiftNotificationType.SHIFT_TASK, ShiftActionType.ADD, now.plusYears(1), CommunicationPreference.NONE, clock, "Test Duty", 1234L, 12345L)
            assertThat(result).isEqualTo("Your activity on Monday, 3rd May, 2021 (Test Duty, 00:20:34 - 03:25:45) has been added.")
        }

        @Test
        fun `Should return shift changed Email`() {
            val result = NotificationDescription.getNotificationDescription(ShiftNotificationType.SHIFT_TASK, ShiftActionType.ADD, now.plusYears(1), CommunicationPreference.EMAIL, clock, "Test Duty", 1234L, 12345L)
            assertThat(result).isEqualTo("* Your activity on Monday, 3rd May, 2021 (Test Duty, 00:20:34 - 03:25:45) has been added.")
        }
    }

    @Nested
    @DisplayName("Overtime Shift Task, Add, next year tests")
    inner class OvertimeShiftTaskAddNextYear {
        @Test
        fun `Should return shift task changed None`() {
            val result = NotificationDescription.getNotificationDescription(ShiftNotificationType.OVERTIME_TASK, ShiftActionType.ADD, now.plusYears(1), CommunicationPreference.NONE, clock, "Test Duty", 1234L, 12345L)
            assertThat(result).isEqualTo("Your overtime activity on Monday, 3rd May, 2021 (Test Duty, 00:20:34 - 03:25:45) has been added.")
        }

        @Test
        fun `Should return shift changed Sms`() {
            val result = NotificationDescription.getNotificationDescription(ShiftNotificationType.OVERTIME_TASK, ShiftActionType.ADD, now.plusYears(1), CommunicationPreference.NONE, clock, "Test Duty", 1234L, 12345L)
            assertThat(result).isEqualTo("Your overtime activity on Monday, 3rd May, 2021 (Test Duty, 00:20:34 - 03:25:45) has been added.")
        }

        @Test
        fun `Should return shift changed Email`() {
            val result = NotificationDescription.getNotificationDescription(ShiftNotificationType.OVERTIME_TASK, ShiftActionType.ADD, now.plusYears(1), CommunicationPreference.EMAIL, clock, "Test Duty", 1234L, 12345L)
            assertThat(result).isEqualTo("* Your overtime activity on Monday, 3rd May, 2021 (Test Duty, 00:20:34 - 03:25:45) has been added.")
        }
    }

    @Nested
    @DisplayName("Shift Task, Remove, This year tests")
    inner class ShiftTaskRemoveThisYear {
        @Test
        fun `Should return shift task changed None`() {
            val result = NotificationDescription.getNotificationDescription(ShiftNotificationType.SHIFT_TASK, ShiftActionType.DELETE, now, CommunicationPreference.NONE, clock, "Test Duty", 1234L, 12345L)
            assertThat(result).isEqualTo("Your activity on Sunday, 3rd May (Test Duty, 00:20:34 - 03:25:45) has been removed.")
        }

        @Test
        fun `Should return shift changed Sms`() {
            val result = NotificationDescription.getNotificationDescription(ShiftNotificationType.SHIFT_TASK, ShiftActionType.DELETE, now, CommunicationPreference.NONE, clock, "Test Duty", 1234L, 12345L)
            assertThat(result).isEqualTo("Your activity on Sunday, 3rd May (Test Duty, 00:20:34 - 03:25:45) has been removed.")
        }

        @Test
        fun `Should return shift changed Email`() {
            val result = NotificationDescription.getNotificationDescription(ShiftNotificationType.SHIFT_TASK, ShiftActionType.DELETE, now, CommunicationPreference.EMAIL, clock, "Test Duty", 1234L, 12345L)
            assertThat(result).isEqualTo("* Your activity on Sunday, 3rd May (Test Duty, 00:20:34 - 03:25:45) has been removed.")
        }
    }

    @Nested
    @DisplayName("Overtime Shift Task, Remove, This year tests")
    inner class OvertimeShiftTaskRemoveThisYear {
        @Test
        fun `Should return shift task changed None`() {
            val result = NotificationDescription.getNotificationDescription(ShiftNotificationType.OVERTIME_TASK, ShiftActionType.DELETE, now, CommunicationPreference.NONE, clock, "Test Duty", 1234L, 12345L)
            assertThat(result).isEqualTo("Your overtime activity on Sunday, 3rd May (Test Duty, 00:20:34 - 03:25:45) has been removed.")
        }

        @Test
        fun `Should return shift changed Sms`() {
            val result = NotificationDescription.getNotificationDescription(ShiftNotificationType.OVERTIME_TASK, ShiftActionType.DELETE, now, CommunicationPreference.NONE, clock, "Test Duty", 1234L, 12345L)
            assertThat(result).isEqualTo("Your overtime activity on Sunday, 3rd May (Test Duty, 00:20:34 - 03:25:45) has been removed.")
        }

        @Test
        fun `Should return shift changed Email`() {
            val result = NotificationDescription.getNotificationDescription(ShiftNotificationType.OVERTIME_TASK, ShiftActionType.DELETE, now, CommunicationPreference.EMAIL, clock, "Test Duty", 1234L, 12345L)
            assertThat(result).isEqualTo("* Your overtime activity on Sunday, 3rd May (Test Duty, 00:20:34 - 03:25:45) has been removed.")
        }
    }

    @Nested
    @DisplayName("Shift Task, Remove, next year tests")
    inner class ShiftTaskRemoveNextYear {
        @Test
        fun `Should return shift task changed None`() {
            val result = NotificationDescription.getNotificationDescription(ShiftNotificationType.SHIFT_TASK, ShiftActionType.DELETE, now.plusYears(1), CommunicationPreference.NONE, clock, "Test Duty", 1234L, 12345L)
            assertThat(result).isEqualTo("Your activity on Monday, 3rd May, 2021 (Test Duty, 00:20:34 - 03:25:45) has been removed.")
        }

        @Test
        fun `Should return shift changed Sms`() {
            val result = NotificationDescription.getNotificationDescription(ShiftNotificationType.SHIFT_TASK, ShiftActionType.DELETE, now.plusYears(1), CommunicationPreference.NONE, clock, "Test Duty", 1234L, 12345L)
            assertThat(result).isEqualTo("Your activity on Monday, 3rd May, 2021 (Test Duty, 00:20:34 - 03:25:45) has been removed.")
        }

        @Test
        fun `Should return shift changed Email`() {
            val result = NotificationDescription.getNotificationDescription(ShiftNotificationType.SHIFT_TASK, ShiftActionType.DELETE, now.plusYears(1), CommunicationPreference.EMAIL, clock, "Test Duty", 1234L, 12345L)
            assertThat(result).isEqualTo("* Your activity on Monday, 3rd May, 2021 (Test Duty, 00:20:34 - 03:25:45) has been removed.")
        }
    }

    @Nested
    @DisplayName("Overtime Shift Task, Remove, next year tests")
    inner class OvertimeShiftTaskRemoveNextYear {
        @Test
        fun `Should return shift task changed None`() {
            val result = NotificationDescription.getNotificationDescription(ShiftNotificationType.OVERTIME_TASK, ShiftActionType.DELETE, now.plusYears(1), CommunicationPreference.NONE, clock, "Test Duty", 1234L, 12345L)
            assertThat(result).isEqualTo("Your overtime activity on Monday, 3rd May, 2021 (Test Duty, 00:20:34 - 03:25:45) has been removed.")
        }

        @Test
        fun `Should return shift changed Sms`() {
            val result = NotificationDescription.getNotificationDescription(ShiftNotificationType.OVERTIME_TASK, ShiftActionType.DELETE, now.plusYears(1), CommunicationPreference.NONE, clock, "Test Duty", 1234L, 12345L)
            assertThat(result).isEqualTo("Your overtime activity on Monday, 3rd May, 2021 (Test Duty, 00:20:34 - 03:25:45) has been removed.")
        }

        @Test
        fun `Should return shift changed Email`() {
            val result = NotificationDescription.getNotificationDescription(ShiftNotificationType.OVERTIME_TASK, ShiftActionType.DELETE, now.plusYears(1), CommunicationPreference.EMAIL, clock, "Test Duty", 1234L, 12345L)
            assertThat(result).isEqualTo("* Your overtime activity on Monday, 3rd May, 2021 (Test Duty, 00:20:34 - 03:25:45) has been removed.")
        }
    }

}