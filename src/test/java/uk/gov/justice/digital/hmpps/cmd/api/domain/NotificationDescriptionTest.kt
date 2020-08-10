package uk.gov.justice.digital.hmpps.cmd.api.domain

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.cmd.api.domain.CommunicationPreference
import uk.gov.justice.digital.hmpps.cmd.api.model.ShiftNotification
import java.time.Clock
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId


class NotificationDescriptionTest {

    private val clock = Clock.fixed(LocalDate.of(2020, 5, 3).atStartOfDay(ZoneId.systemDefault()).toInstant(), ZoneId.systemDefault())
    private val now = LocalDate.now(clock)

    @Nested
    @DisplayName("Shift, Edit, This year tests")
    inner class ShiftEditThisYear {
        @Test
        fun `Should return shift changed None`() {
            val shiftNotification = ShiftNotification(1, "", now, LocalDateTime.MIN, 0, 0, "", "shift", "edit", false)
            val result = NotificationDescription.getNotificationDescription(shiftNotification, CommunicationPreference.NONE, clock)
            assertThat(result).isEqualTo("Your shift on Sunday, 3rd May has changed.")
        }

        @Test
        fun `Should return shift changed Sms`() {
            val shiftNotification = ShiftNotification(1, "", now, LocalDateTime.MIN, 0, 0, "", "shift", "edit", false)
            val result = NotificationDescription.getNotificationDescription(shiftNotification, CommunicationPreference.SMS, clock)
            assertThat(result).isEqualTo("Your shift on Sunday, 3rd May has changed.")
        }

        @Test
        fun `Should return shift changed Email`() {
            val shiftNotification = ShiftNotification(1, "", now, LocalDateTime.MIN, 0, 0, "", "shift", "edit", false)
            val result = NotificationDescription.getNotificationDescription(shiftNotification, CommunicationPreference.EMAIL, clock)
            assertThat(result).isEqualTo("* Your shift on Sunday, 3rd May has changed.")
        }
    }

    @Nested
    @DisplayName("Shift, Edit, Next year tests")
    inner class ShiftEditNextYear {
        @Test
        fun `Should return shift changed None`() {
            val shiftNotification = ShiftNotification(1, "", now.plusYears(1), LocalDateTime.MIN, 0, 0, "", "shift", "edit", false)
            val result = NotificationDescription.getNotificationDescription(shiftNotification, CommunicationPreference.NONE, clock)
            assertThat(result).isEqualTo("Your shift on Monday, 3rd May, 2021 has changed.")
        }

        @Test
        fun `Should return shift changed Sms`() {
            val shiftNotification = ShiftNotification(1, "", now.plusYears(1), LocalDateTime.MIN, 0, 0, "", "shift", "edit", false)
            val result = NotificationDescription.getNotificationDescription(shiftNotification, CommunicationPreference.SMS, clock)
            assertThat(result).isEqualTo("Your shift on Monday, 3rd May, 2021 has changed.")
        }

        @Test
        fun `Should return shift changed Email`() {
            val shiftNotification = ShiftNotification(1, "", now.plusYears(1), LocalDateTime.MIN, 0, 0, "", "shift", "edit", false)
            val result = NotificationDescription.getNotificationDescription(shiftNotification, CommunicationPreference.EMAIL, clock)
            assertThat(result).isEqualTo("* Your shift on Monday, 3rd May, 2021 has changed.")
        }
    }

    @Nested
    @DisplayName("Overtime Shift, Edit, This year tests")
    inner class OvertimeShiftEditThisYear {
        @Test
        fun `Should return overtime shift changed None`() {
            val shiftNotification = ShiftNotification(1, "", now, LocalDateTime.MIN, 0, 0, "", "overtime", "edit", false)
            val result = NotificationDescription.getNotificationDescription(shiftNotification, CommunicationPreference.NONE, clock)
            assertThat(result).isEqualTo("Your overtime shift on Sunday, 3rd May has changed.")
        }

        @Test
        fun `Should return overtime shift changed Sms`() {
            val shiftNotification = ShiftNotification(1, "", now, LocalDateTime.MIN, 0, 0, "", "overtime", "edit", false)
            val result = NotificationDescription.getNotificationDescription(shiftNotification, CommunicationPreference.SMS, clock)
            assertThat(result).isEqualTo("Your overtime shift on Sunday, 3rd May has changed.")
        }

        @Test
        fun `Should return overtime shift changed Email`() {
            val shiftNotification = ShiftNotification(1, "", now, LocalDateTime.MIN, 0, 0, "", "overtime", "edit", false)
            val result = NotificationDescription.getNotificationDescription(shiftNotification, CommunicationPreference.EMAIL, clock)
            assertThat(result).isEqualTo("* Your overtime shift on Sunday, 3rd May has changed.")
        }
    }

    @Nested
    @DisplayName("Overtime Shift, Edit, Next year tests")
    inner class OvertimeShiftEditNextYear {
        @Test
        fun `Should return overtime shift changed None`() {
            val shiftNotification = ShiftNotification(1, "", now.plusYears(1), LocalDateTime.MIN, 0, 0, "", "overtime", "edit", false)
            val result = NotificationDescription.getNotificationDescription(shiftNotification, CommunicationPreference.NONE, clock)
            assertThat(result).isEqualTo("Your overtime shift on Monday, 3rd May, 2021 has changed.")
        }

        @Test
        fun `Should return overtime shift changed Sms`() {
            val shiftNotification = ShiftNotification(1, "", now.plusYears(1), LocalDateTime.MIN, 0, 0, "", "overtime", "edit", false)
            val result = NotificationDescription.getNotificationDescription(shiftNotification, CommunicationPreference.SMS, clock)
            assertThat(result).isEqualTo("Your overtime shift on Monday, 3rd May, 2021 has changed.")
        }

        @Test
        fun `Should return overtime shift changed Email`() {
            val shiftNotification = ShiftNotification(1, "", now.plusYears(1), LocalDateTime.MIN, 0, 0, "", "overtime", "edit", false)
            val result = NotificationDescription.getNotificationDescription(shiftNotification, CommunicationPreference.EMAIL, clock)
            assertThat(result).isEqualTo("* Your overtime shift on Monday, 3rd May, 2021 has changed.")
        }
    }

    @Nested
    @DisplayName("Shift, Add, This year tests")
    inner class ShiftAddThisYear {
        @Test
        fun `Should return shift changed None`() {
            val shiftNotification = ShiftNotification(1, "", now, LocalDateTime.MIN, 0, 0, "", "shift", "add", false)
            val result = NotificationDescription.getNotificationDescription(shiftNotification, CommunicationPreference.NONE, clock)
            assertThat(result).isEqualTo("Your shift on Sunday, 3rd May has been added.")
        }

        @Test
        fun `Should return shift changed Sms`() {
            val shiftNotification = ShiftNotification(1, "", now, LocalDateTime.MIN, 0, 0, "", "shift", "add", false)
            val result = NotificationDescription.getNotificationDescription(shiftNotification, CommunicationPreference.SMS, clock)
            assertThat(result).isEqualTo("Your shift on Sunday, 3rd May has been added.")
        }

        @Test
        fun `Should return shift changed Email`() {
            val shiftNotification = ShiftNotification(1, "", now, LocalDateTime.MIN, 0, 0, "", "shift", "add", false)
            val result = NotificationDescription.getNotificationDescription(shiftNotification, CommunicationPreference.EMAIL, clock)
            assertThat(result).isEqualTo("* Your shift on Sunday, 3rd May has been added.")
        }
    }

    @Nested
    @DisplayName("Shift, Add, Next year tests")
    inner class ShiftAddNextYear {
        @Test
        fun `Should return shift changed None`() {
            val shiftNotification = ShiftNotification(1, "", now.plusYears(1), LocalDateTime.MIN, 0, 0, "", "shift", "add", false)
            val result = NotificationDescription.getNotificationDescription(shiftNotification, CommunicationPreference.NONE, clock)
            assertThat(result).isEqualTo("Your shift on Monday, 3rd May, 2021 has been added.")
        }

        @Test
        fun `Should return shift changed Sms`() {
            val shiftNotification = ShiftNotification(1, "", now.plusYears(1), LocalDateTime.MIN, 0, 0, "", "shift", "add", false)
            val result = NotificationDescription.getNotificationDescription(shiftNotification, CommunicationPreference.SMS, clock)
            assertThat(result).isEqualTo("Your shift on Monday, 3rd May, 2021 has been added.")
        }

        @Test
        fun `Should return shift changed Email`() {
            val shiftNotification = ShiftNotification(1, "", now.plusYears(1), LocalDateTime.MIN, 0, 0, "", "shift", "add", false)
            val result = NotificationDescription.getNotificationDescription(shiftNotification, CommunicationPreference.EMAIL, clock)
            assertThat(result).isEqualTo("* Your shift on Monday, 3rd May, 2021 has been added.")
        }
    }

    @Nested
    @DisplayName("Overtime Shift, Add, This year tests")
    inner class OvertimeShiftAddThisYear {
        @Test
        fun `Should return overtime shift changed None`() {
            val shiftNotification = ShiftNotification(1, "", now, LocalDateTime.MIN, 0, 0, "", "overtime", "add", false)
            val result = NotificationDescription.getNotificationDescription(shiftNotification, CommunicationPreference.NONE, clock)
            assertThat(result).isEqualTo("Your overtime shift on Sunday, 3rd May has been added.")
        }

        @Test
        fun `Should return overtime shift changed Sms`() {
            val shiftNotification = ShiftNotification(1, "", now, LocalDateTime.MIN, 0, 0, "", "overtime", "add", false)
            val result = NotificationDescription.getNotificationDescription(shiftNotification, CommunicationPreference.SMS, clock)
            assertThat(result).isEqualTo("Your overtime shift on Sunday, 3rd May has been added.")
        }

        @Test
        fun `Should return overtime shift changed Email`() {
            val shiftNotification = ShiftNotification(1, "", now, LocalDateTime.MIN, 0, 0, "", "overtime", "add", false)
            val result = NotificationDescription.getNotificationDescription(shiftNotification, CommunicationPreference.EMAIL, clock)
            assertThat(result).isEqualTo("* Your overtime shift on Sunday, 3rd May has been added.")
        }
    }

    @Nested
    @DisplayName("Overtime Shift, Add, Next year tests")
    inner class OvertimeShiftAddNextYear {
        @Test
        fun `Should return overtime shift changed None`() {
            val shiftNotification = ShiftNotification(1, "", now.plusYears(1), LocalDateTime.MIN, 0, 0, "", "overtime", "add", false)
            val result = NotificationDescription.getNotificationDescription(shiftNotification, CommunicationPreference.NONE, clock)
            assertThat(result).isEqualTo("Your overtime shift on Monday, 3rd May, 2021 has been added.")
        }

        @Test
        fun `Should return overtime shift changed Sms`() {
            val shiftNotification = ShiftNotification(1, "", now.plusYears(1), LocalDateTime.MIN, 0, 0, "", "overtime", "add", false)
            val result = NotificationDescription.getNotificationDescription(shiftNotification, CommunicationPreference.SMS, clock)
            assertThat(result).isEqualTo("Your overtime shift on Monday, 3rd May, 2021 has been added.")
        }

        @Test
        fun `Should return overtime shift changed Email`() {
            val shiftNotification = ShiftNotification(1, "", now.plusYears(1), LocalDateTime.MIN, 0, 0, "", "overtime", "add", false)
            val result = NotificationDescription.getNotificationDescription(shiftNotification, CommunicationPreference.EMAIL, clock)
            assertThat(result).isEqualTo("* Your overtime shift on Monday, 3rd May, 2021 has been added.")
        }
    }

    @Nested
    @DisplayName("Shift, Remove, This year tests")
    inner class ShiftRemoveThisYear {
        @Test
        fun `Should return shift changed None`() {
            val shiftNotification = ShiftNotification(1, "", now, LocalDateTime.MIN, 0, 0, "", "shift", "delete", false)
            val result = NotificationDescription.getNotificationDescription(shiftNotification, CommunicationPreference.NONE, clock)
            assertThat(result).isEqualTo("Your shift on Sunday, 3rd May has been removed.")
        }

        @Test
        fun `Should return shift changed Sms`() {
            val shiftNotification = ShiftNotification(1, "", now, LocalDateTime.MIN, 0, 0, "", "shift", "delete", false)
            val result = NotificationDescription.getNotificationDescription(shiftNotification, CommunicationPreference.SMS, clock)
            assertThat(result).isEqualTo("Your shift on Sunday, 3rd May has been removed.")
        }

        @Test
        fun `Should return shift changed Email`() {
            val shiftNotification = ShiftNotification(1, "", now, LocalDateTime.MIN, 0, 0, "", "shift", "delete", false)
            val result = NotificationDescription.getNotificationDescription(shiftNotification, CommunicationPreference.EMAIL, clock)
            assertThat(result).isEqualTo("* Your shift on Sunday, 3rd May has been removed.")
        }
    }

    @Nested
    @DisplayName("Shift, Remove, Next year tests")
    inner class ShiftRemoveNextYear {
        @Test
        fun `Should return shift changed None`() {
            val shiftNotification = ShiftNotification(1, "", now.plusYears(1), LocalDateTime.MIN, 0, 0, "", "shift", "delete", false)
            val result = NotificationDescription.getNotificationDescription(shiftNotification, CommunicationPreference.NONE, clock)
            assertThat(result).isEqualTo("Your shift on Monday, 3rd May, 2021 has been removed.")
        }

        @Test
        fun `Should return shift changed Sms`() {
            val shiftNotification = ShiftNotification(1, "", now.plusYears(1), LocalDateTime.MIN, 0, 0, "", "shift", "delete", false)
            val result = NotificationDescription.getNotificationDescription(shiftNotification, CommunicationPreference.SMS, clock)
            assertThat(result).isEqualTo("Your shift on Monday, 3rd May, 2021 has been removed.")
        }

        @Test
        fun `Should return shift changed Email`() {
            val shiftNotification = ShiftNotification(1, "", now.plusYears(1), LocalDateTime.MIN, 0, 0, "", "shift", "delete", false)
            val result = NotificationDescription.getNotificationDescription(shiftNotification, CommunicationPreference.EMAIL, clock)
            assertThat(result).isEqualTo("* Your shift on Monday, 3rd May, 2021 has been removed.")
        }
    }

    @Nested
    @DisplayName("Overtime Shift, Remove, This year tests")
    inner class OvertimeShiftRemoveThisYear {
        @Test
        fun `Should return overtime shift changed None`() {
            val shiftNotification = ShiftNotification(1, "", now, LocalDateTime.MIN, 0, 0, "", "overtime", "delete", false)
            val result = NotificationDescription.getNotificationDescription(shiftNotification, CommunicationPreference.NONE, clock)
            assertThat(result).isEqualTo("Your overtime shift on Sunday, 3rd May has been removed.")
        }

        @Test
        fun `Should return overtime shift changed Sms`() {
            val shiftNotification = ShiftNotification(1, "", now, LocalDateTime.MIN, 0, 0, "", "overtime", "delete", false)
            val result = NotificationDescription.getNotificationDescription(shiftNotification, CommunicationPreference.SMS, clock)
            assertThat(result).isEqualTo("Your overtime shift on Sunday, 3rd May has been removed.")
        }

        @Test
        fun `Should return overtime shift changed Email`() {
            val shiftNotification = ShiftNotification(1, "", now, LocalDateTime.MIN, 0, 0, "", "overtime", "delete", false)
            val result = NotificationDescription.getNotificationDescription(shiftNotification, CommunicationPreference.EMAIL, clock)
            assertThat(result).isEqualTo("* Your overtime shift on Sunday, 3rd May has been removed.")
        }
    }

    @Nested
    @DisplayName("Overtime Shift, Remove, Next year tests")
    inner class OvertimeShiftRemoveNextYear {
        @Test
        fun `Should return overtime shift changed None`() {
            val shiftNotification = ShiftNotification(1, "", now.plusYears(1), LocalDateTime.MIN, 0, 0, "", "overtime", "delete", false)
            val result = NotificationDescription.getNotificationDescription(shiftNotification, CommunicationPreference.NONE, clock)
            assertThat(result).isEqualTo("Your overtime shift on Monday, 3rd May, 2021 has been removed.")
        }

        @Test
        fun `Should return overtime shift changed Sms`() {
            val shiftNotification = ShiftNotification(1, "", now.plusYears(1), LocalDateTime.MIN, 0, 0, "", "overtime", "delete", false)
            val result = NotificationDescription.getNotificationDescription(shiftNotification, CommunicationPreference.SMS, clock)
            assertThat(result).isEqualTo("Your overtime shift on Monday, 3rd May, 2021 has been removed.")
        }

        @Test
        fun `Should return overtime shift changed Email`() {
            val shiftNotification = ShiftNotification(1, "", now.plusYears(1), LocalDateTime.MIN, 0, 0, "", "overtime", "delete", false)
            val result = NotificationDescription.getNotificationDescription(shiftNotification, CommunicationPreference.EMAIL, clock)
            assertThat(result).isEqualTo("* Your overtime shift on Monday, 3rd May, 2021 has been removed.")
        }
    }

    @Nested
    @DisplayName("Shift Task, Edit, This year tests")
    inner class ShiftTaskEditThisYear {
        @Test
        fun `Should return shift task changed None`() {
            val shiftNotification = ShiftNotification(1, "", now, LocalDateTime.MIN, 1234L, 12345L, "Test Duty", "shift_task", "edit", false)
            val result = NotificationDescription.getNotificationDescription(shiftNotification, CommunicationPreference.NONE, clock)
            assertThat(result).isEqualTo("Your activity on Sunday, 3rd May (00:20:34 - 03:25:45) has changed to Test Duty.")
        }

        @Test
        fun `Should return shift changed Sms`() {
            val shiftNotification = ShiftNotification(1, "", now, LocalDateTime.MIN, 1234L, 12345L, "Test Duty", "shift_task", "edit", false)
            val result = NotificationDescription.getNotificationDescription(shiftNotification, CommunicationPreference.SMS, clock)
            assertThat(result).isEqualTo("Your activity on Sunday, 3rd May (00:20:34 - 03:25:45) has changed.")
        }

        @Test
        fun `Should return shift changed Email`() {
            val shiftNotification = ShiftNotification(1, "", now, LocalDateTime.MIN, 1234L, 12345L, "Test Duty", "shift_task", "edit", false)
            val result = NotificationDescription.getNotificationDescription(shiftNotification, CommunicationPreference.EMAIL, clock)
            assertThat(result).isEqualTo("* Your activity on Sunday, 3rd May (00:20:34 - 03:25:45) has changed.")
        }
    }

    @Nested
    @DisplayName("Shift Task, Edit, next year tests")
    inner class ShiftTaskEditNextYear {
        @Test
        fun `Should return shift task changed None`() {
            val shiftNotification = ShiftNotification(1, "", now.plusYears(1), LocalDateTime.MIN, 1234L, 12345L, "Test Duty", "shift_task", "edit", false)
            val result = NotificationDescription.getNotificationDescription(shiftNotification, CommunicationPreference.NONE, clock)
            assertThat(result).isEqualTo("Your activity on Monday, 3rd May, 2021 (00:20:34 - 03:25:45) has changed to Test Duty.")
        }

        @Test
        fun `Should return shift changed Sms`() {
            val shiftNotification = ShiftNotification(1, "", now.plusYears(1), LocalDateTime.MIN, 1234L, 12345L, "Test Duty", "shift_task", "edit", false)
            val result = NotificationDescription.getNotificationDescription(shiftNotification, CommunicationPreference.SMS, clock)
            assertThat(result).isEqualTo("Your activity on Monday, 3rd May, 2021 (00:20:34 - 03:25:45) has changed.")
        }

        @Test
        fun `Should return shift changed Email`() {
            val shiftNotification = ShiftNotification(1, "", now.plusYears(1), LocalDateTime.MIN, 1234L, 12345L, "Test Duty", "shift_task", "edit", false)
            val result = NotificationDescription.getNotificationDescription(shiftNotification, CommunicationPreference.EMAIL, clock)
            assertThat(result).isEqualTo("* Your activity on Monday, 3rd May, 2021 (00:20:34 - 03:25:45) has changed.")
        }
    }

    @Nested
    @DisplayName("Overtime Shift Task, Edit, This year tests")
    inner class OvertimeShiftTaskEditThisYear {
        @Test
        fun `Should return shift task changed None`() {
            val shiftNotification = ShiftNotification(1, "", now, LocalDateTime.MIN, 1234L, 12345L, "Test Duty", "overtime_task", "edit", false)
            val result = NotificationDescription.getNotificationDescription(shiftNotification, CommunicationPreference.NONE, clock)
            assertThat(result).isEqualTo("Your overtime activity on Sunday, 3rd May (00:20:34 - 03:25:45) has changed to Test Duty.")
        }

        @Test
        fun `Should return shift changed Sms`() {
            val shiftNotification = ShiftNotification(1, "", now, LocalDateTime.MIN, 1234L, 12345L, "Test Duty", "overtime_task", "edit", false)
            val result = NotificationDescription.getNotificationDescription(shiftNotification, CommunicationPreference.SMS, clock)
            assertThat(result).isEqualTo("Your overtime activity on Sunday, 3rd May (00:20:34 - 03:25:45) has changed.")
        }

        @Test
        fun `Should return shift changed Email`() {
            val shiftNotification = ShiftNotification(1, "", now, LocalDateTime.MIN, 1234L, 12345L, "Test Duty", "overtime_task", "edit", false)
            val result = NotificationDescription.getNotificationDescription(shiftNotification, CommunicationPreference.EMAIL, clock)
            assertThat(result).isEqualTo("* Your overtime activity on Sunday, 3rd May (00:20:34 - 03:25:45) has changed.")
        }
    }

    @Nested
    @DisplayName("Overtime Shift Task, Edit, next year tests")
    inner class OvertimeShiftTaskEditNextYear {
        @Test
        fun `Should return shift task changed None`() {
            val shiftNotification = ShiftNotification(1, "", now.plusYears(1), LocalDateTime.MIN, 1234L, 12345L, "Test Duty", "overtime_task", "edit", false)
            val result = NotificationDescription.getNotificationDescription(shiftNotification, CommunicationPreference.NONE, clock)
            assertThat(result).isEqualTo("Your overtime activity on Monday, 3rd May, 2021 (00:20:34 - 03:25:45) has changed to Test Duty.")
        }

        @Test
        fun `Should return shift changed Sms`() {
            val shiftNotification = ShiftNotification(1, "", now.plusYears(1), LocalDateTime.MIN, 1234L, 12345L, "Test Duty", "overtime_task", "edit", false)
            val result = NotificationDescription.getNotificationDescription(shiftNotification, CommunicationPreference.SMS, clock)
            assertThat(result).isEqualTo("Your overtime activity on Monday, 3rd May, 2021 (00:20:34 - 03:25:45) has changed.")
        }

        @Test
        fun `Should return shift changed Email`() {
            val shiftNotification = ShiftNotification(1, "", now.plusYears(1), LocalDateTime.MIN, 1234L, 12345L, "Test Duty", "overtime_task", "edit", false)
            val result = NotificationDescription.getNotificationDescription(shiftNotification, CommunicationPreference.EMAIL, clock)
            assertThat(result).isEqualTo("* Your overtime activity on Monday, 3rd May, 2021 (00:20:34 - 03:25:45) has changed.")
        }
    }

    @Nested
    @DisplayName("Shift Task, Add, This year tests")
    inner class ShiftTaskAddThisYear {
        @Test
        fun `Should return shift task changed None`() {
            val shiftNotification = ShiftNotification(1, "", now, LocalDateTime.MIN, 1234L, 12345L, "Test Duty", "shift_task", "add", false)
            val result = NotificationDescription.getNotificationDescription(shiftNotification, CommunicationPreference.NONE, clock)
            assertThat(result).isEqualTo("Your activity on Sunday, 3rd May (00:20:34 - 03:25:45) has been added as Test Duty.")
        }

        @Test
        fun `Should return shift changed Sms`() {
            val shiftNotification = ShiftNotification(1, "", now, LocalDateTime.MIN, 1234L, 12345L, "Test Duty", "shift_task", "add", false)
            val result = NotificationDescription.getNotificationDescription(shiftNotification, CommunicationPreference.SMS, clock)
            assertThat(result).isEqualTo("Your activity on Sunday, 3rd May (00:20:34 - 03:25:45) has been added.")
        }

        @Test
        fun `Should return shift changed Email`() {
            val shiftNotification = ShiftNotification(1, "", now, LocalDateTime.MIN, 1234L, 12345L, "Test Duty", "shift_task", "add", false)
            val result = NotificationDescription.getNotificationDescription(shiftNotification, CommunicationPreference.EMAIL, clock)
            assertThat(result).isEqualTo("* Your activity on Sunday, 3rd May (00:20:34 - 03:25:45) has been added.")
        }
    }

    @Nested
    @DisplayName("Shift Task, Add, next year tests")
    inner class ShiftTaskAddNextYear {
        @Test
        fun `Should return shift task changed None`() {
            val shiftNotification = ShiftNotification(1, "", now.plusYears(1), LocalDateTime.MIN, 1234L, 12345L, "Test Duty", "shift_task", "add", false)
            val result = NotificationDescription.getNotificationDescription(shiftNotification, CommunicationPreference.NONE, clock)
            assertThat(result).isEqualTo("Your activity on Monday, 3rd May, 2021 (00:20:34 - 03:25:45) has been added as Test Duty.")
        }

        @Test
        fun `Should return shift changed Sms`() {
            val shiftNotification = ShiftNotification(1, "", now.plusYears(1), LocalDateTime.MIN, 1234L, 12345L, "Test Duty", "shift_task", "add", false)
            val result = NotificationDescription.getNotificationDescription(shiftNotification, CommunicationPreference.SMS, clock)
            assertThat(result).isEqualTo("Your activity on Monday, 3rd May, 2021 (00:20:34 - 03:25:45) has been added.")
        }

        @Test
        fun `Should return shift changed Email`() {
            val shiftNotification = ShiftNotification(1, "", now.plusYears(1), LocalDateTime.MIN, 1234L, 12345L, "Test Duty", "shift_task", "add", false)
            val result = NotificationDescription.getNotificationDescription(shiftNotification, CommunicationPreference.EMAIL, clock)
            assertThat(result).isEqualTo("* Your activity on Monday, 3rd May, 2021 (00:20:34 - 03:25:45) has been added.")
        }
    }

    @Nested
    @DisplayName("Overtime Shift Task, Add, This year tests")
    inner class OvertimeShiftTaskAddThisYear {
        @Test
        fun `Should return shift task changed None`() {
            val shiftNotification = ShiftNotification(1, "", now, LocalDateTime.MIN, 1234L, 12345L, "Test Duty", "overtime_task", "add", false)
            val result = NotificationDescription.getNotificationDescription(shiftNotification, CommunicationPreference.NONE, clock)
            assertThat(result).isEqualTo("Your overtime activity on Sunday, 3rd May (00:20:34 - 03:25:45) has been added as Test Duty.")
        }

        @Test
        fun `Should return shift changed Sms`() {
            val shiftNotification = ShiftNotification(1, "", now, LocalDateTime.MIN, 1234L, 12345L, "Test Duty", "overtime_task", "add", false)
            val result = NotificationDescription.getNotificationDescription(shiftNotification, CommunicationPreference.SMS, clock)
            assertThat(result).isEqualTo("Your overtime activity on Sunday, 3rd May (00:20:34 - 03:25:45) has been added.")
        }

        @Test
        fun `Should return shift changed Email`() {
            val shiftNotification = ShiftNotification(1, "", now, LocalDateTime.MIN, 1234L, 12345L, "Test Duty", "overtime_task", "add", false)
            val result = NotificationDescription.getNotificationDescription(shiftNotification, CommunicationPreference.EMAIL, clock)
            assertThat(result).isEqualTo("* Your overtime activity on Sunday, 3rd May (00:20:34 - 03:25:45) has been added.")
        }
    }

    @Nested
    @DisplayName("Overtime Shift Task, Add, next year tests")
    inner class OvertimeShiftTaskAddNextYear {
        @Test
        fun `Should return shift task changed None`() {
            val shiftNotification = ShiftNotification(1, "", now.plusYears(1), LocalDateTime.MIN, 1234L, 12345L, "Test Duty", "overtime_task", "add", false)
            val result = NotificationDescription.getNotificationDescription(shiftNotification, CommunicationPreference.NONE, clock)
            assertThat(result).isEqualTo("Your overtime activity on Monday, 3rd May, 2021 (00:20:34 - 03:25:45) has been added as Test Duty.")
        }

        @Test
        fun `Should return shift changed Sms`() {
            val shiftNotification = ShiftNotification(1, "", now.plusYears(1), LocalDateTime.MIN, 1234L, 12345L, "Test Duty", "overtime_task", "add", false)
            val result = NotificationDescription.getNotificationDescription(shiftNotification, CommunicationPreference.SMS, clock)
            assertThat(result).isEqualTo("Your overtime activity on Monday, 3rd May, 2021 (00:20:34 - 03:25:45) has been added.")
        }

        @Test
        fun `Should return shift changed Email`() {
            val shiftNotification = ShiftNotification(1, "", now.plusYears(1), LocalDateTime.MIN, 1234L, 12345L, "Test Duty", "overtime_task", "add", false)
            val result = NotificationDescription.getNotificationDescription(shiftNotification, CommunicationPreference.EMAIL, clock)
            assertThat(result).isEqualTo("* Your overtime activity on Monday, 3rd May, 2021 (00:20:34 - 03:25:45) has been added.")
        }
    }

    @Nested
    @DisplayName("Shift Task, Remove, This year tests")
    inner class ShiftTaskRemoveThisYear {
        @Test
        fun `Should return shift task changed None`() {
            val shiftNotification = ShiftNotification(1, "", now, LocalDateTime.MIN, 1234L, 12345L, "Test Duty", "shift_task", "delete", false)
            val result = NotificationDescription.getNotificationDescription(shiftNotification, CommunicationPreference.NONE, clock)
            assertThat(result).isEqualTo("Your activity on Sunday, 3rd May (00:20:34 - 03:25:45) has been removed (was Test Duty).")
        }

        @Test
        fun `Should return shift changed Sms`() {
            val shiftNotification = ShiftNotification(1, "", now, LocalDateTime.MIN, 1234L, 12345L, "Test Duty", "shift_task", "delete", false)
            val result = NotificationDescription.getNotificationDescription(shiftNotification, CommunicationPreference.SMS, clock)
            assertThat(result).isEqualTo("Your activity on Sunday, 3rd May (00:20:34 - 03:25:45) has been removed.")
        }

        @Test
        fun `Should return shift changed Email`() {
            val shiftNotification = ShiftNotification(1, "", now, LocalDateTime.MIN, 1234L, 12345L, "Test Duty", "shift_task", "delete", false)
            val result = NotificationDescription.getNotificationDescription(shiftNotification, CommunicationPreference.EMAIL, clock)
            assertThat(result).isEqualTo("* Your activity on Sunday, 3rd May (00:20:34 - 03:25:45) has been removed.")
        }
    }

    @Nested
    @DisplayName("Shift Task, Remove, next year tests")
    inner class ShiftTaskRemoveNextYear {
        @Test
        fun `Should return shift task changed None`() {
            val shiftNotification = ShiftNotification(1, "", now.plusYears(1), LocalDateTime.MIN, 1234L, 12345L, "Test Duty", "shift_task", "delete", false)
            val result = NotificationDescription.getNotificationDescription(shiftNotification, CommunicationPreference.NONE, clock)
            assertThat(result).isEqualTo("Your activity on Monday, 3rd May, 2021 (00:20:34 - 03:25:45) has been removed (was Test Duty).")
        }

        @Test
        fun `Should return shift changed Sms`() {
            val shiftNotification = ShiftNotification(1, "", now.plusYears(1), LocalDateTime.MIN, 1234L, 12345L, "Test Duty", "shift_task", "delete", false)
            val result = NotificationDescription.getNotificationDescription(shiftNotification, CommunicationPreference.SMS, clock)
            assertThat(result).isEqualTo("Your activity on Monday, 3rd May, 2021 (00:20:34 - 03:25:45) has been removed.")
        }

        @Test
        fun `Should return shift changed Email`() {
            val shiftNotification = ShiftNotification(1, "", now.plusYears(1), LocalDateTime.MIN, 1234L, 12345L, "Test Duty", "shift_task", "delete", false)
            val result = NotificationDescription.getNotificationDescription(shiftNotification, CommunicationPreference.EMAIL, clock)
            assertThat(result).isEqualTo("* Your activity on Monday, 3rd May, 2021 (00:20:34 - 03:25:45) has been removed.")
        }
    }

    @Nested
    @DisplayName("Overtime Shift Task, Remove, This year tests")
    inner class OvertimeShiftTaskRemoveThisYear {
        @Test
        fun `Should return shift task changed None`() {
            val shiftNotification = ShiftNotification(1, "", now, LocalDateTime.MIN, 1234L, 12345L, "Test Duty", "overtime_task", "delete", false)
            val result = NotificationDescription.getNotificationDescription(shiftNotification, CommunicationPreference.NONE, clock)
            assertThat(result).isEqualTo("Your overtime activity on Sunday, 3rd May (00:20:34 - 03:25:45) has been removed (was Test Duty).")
        }

        @Test
        fun `Should return shift changed Sms`() {
            val shiftNotification = ShiftNotification(1, "", now, LocalDateTime.MIN, 1234L, 12345L, "Test Duty", "overtime_task", "delete", false)
            val result = NotificationDescription.getNotificationDescription(shiftNotification, CommunicationPreference.SMS, clock)
            assertThat(result).isEqualTo("Your overtime activity on Sunday, 3rd May (00:20:34 - 03:25:45) has been removed.")
        }

        @Test
        fun `Should return shift changed Email`() {
            val shiftNotification = ShiftNotification(1, "", now, LocalDateTime.MIN, 1234L, 12345L, "Test Duty", "overtime_task", "delete", false)
            val result = NotificationDescription.getNotificationDescription(shiftNotification, CommunicationPreference.EMAIL, clock)
            assertThat(result).isEqualTo("* Your overtime activity on Sunday, 3rd May (00:20:34 - 03:25:45) has been removed.")
        }
    }

    @Nested
    @DisplayName("Overtime Shift Task, Remove, next year tests")
    inner class OvertimeShiftTaskRemoveNextYear {
        @Test
        fun `Should return shift task changed None`() {
            val shiftNotification = ShiftNotification(1, "", now.plusYears(1), LocalDateTime.MIN, 1234L, 12345L, "Test Duty", "overtime_task", "delete", false)
            val result = NotificationDescription.getNotificationDescription(shiftNotification, CommunicationPreference.NONE, clock)
            assertThat(result).isEqualTo("Your overtime activity on Monday, 3rd May, 2021 (00:20:34 - 03:25:45) has been removed (was Test Duty).")
        }

        @Test
        fun `Should return shift changed Sms`() {
            val shiftNotification = ShiftNotification(1, "", now.plusYears(1), LocalDateTime.MIN, 1234L, 12345L, "Test Duty", "overtime_task", "delete", false)
            val result = NotificationDescription.getNotificationDescription(shiftNotification, CommunicationPreference.SMS, clock)
            assertThat(result).isEqualTo("Your overtime activity on Monday, 3rd May, 2021 (00:20:34 - 03:25:45) has been removed.")
        }

        @Test
        fun `Should return shift changed Email`() {
            val shiftNotification = ShiftNotification(1, "", now.plusYears(1), LocalDateTime.MIN, 1234L, 12345L, "Test Duty", "overtime_task", "delete", false)
            val result = NotificationDescription.getNotificationDescription(shiftNotification, CommunicationPreference.EMAIL, clock)
            assertThat(result).isEqualTo("* Your overtime activity on Monday, 3rd May, 2021 (00:20:34 - 03:25:45) has been removed.")
        }
    }

}