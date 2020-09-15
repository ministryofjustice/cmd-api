package uk.gov.justice.digital.hmpps.cmd.api.domain

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.cmd.api.model.Notification
import uk.gov.justice.digital.hmpps.cmd.api.uk.gov.justice.digital.hmpps.cmd.api.domain.DetailParentType
import java.time.Clock
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId


class NotificationDescriptionTest {

    private val clock = Clock.fixed(LocalDate.of(2020, 5, 3).atStartOfDay(ZoneId.systemDefault()).toInstant(), ZoneId.systemDefault())
    private val now = LocalDate.now(clock)

    @Nested
    @DisplayName("Shift Task boundary checks")
    inner class ShitTaskBoundary {
        @Test
        fun `Should return full day for 0 0 `() {
            val shiftNotification = Notification(1, "", LocalDateTime.MIN, now.atStartOfDay(), now.atStartOfDay(), "Test Duty", DetailParentType.SHIFT, DetailModificationType.EDIT, false)
            val result = shiftNotification.getNotificationDescription(CommunicationPreference.NONE)
            assertThat(result).isEqualTo("Your detail on Sunday, 3rd May (full day) has changed to Test Duty.")
        }
    }

    @Nested
    @DisplayName("Shift, Edit, This year tests")
    inner class ShiftEditThisYear {
        @Test
        fun `Should return shift changed None`() {
            val shiftNotification = Notification(1, "", LocalDateTime.MIN, now.atStartOfDay(), now.atStartOfDay(), null, DetailParentType.SHIFT, DetailModificationType.EDIT, false)
            val result = shiftNotification.getNotificationDescription(CommunicationPreference.NONE)
            assertThat(result).isEqualTo("Your shift on Sunday, 3rd May has changed.")
        }

        @Test
        fun `Should return shift changed Sms`() {
            val shiftNotification = Notification(1, "", LocalDateTime.MIN, now.atStartOfDay(), now.atStartOfDay(), null, DetailParentType.SHIFT, DetailModificationType.EDIT, false)
            val result = shiftNotification.getNotificationDescription(CommunicationPreference.SMS)
            assertThat(result).isEqualTo("Your shift on Sunday, 3rd May has changed.")
        }

        @Test
        fun `Should return shift changed Email`() {
            val shiftNotification = Notification(1, "", LocalDateTime.MIN, now.atStartOfDay(), now.atStartOfDay(), null, DetailParentType.SHIFT, DetailModificationType.EDIT, false)
            val result = shiftNotification.getNotificationDescription(CommunicationPreference.EMAIL)
            assertThat(result).isEqualTo("* Your shift on Sunday, 3rd May has changed.")
        }
    }

    @Nested
    @DisplayName("Overtime Shift, Edit, This year tests")
    inner class OvertimeShiftEditThisYear {
        @Test
        fun `Should return overtime shift changed None`() {
            val shiftNotification = Notification(1, "", LocalDateTime.MIN, now.atStartOfDay(), now.atStartOfDay(), null, DetailParentType.OVERTIME, DetailModificationType.EDIT, false)
            val result = shiftNotification.getNotificationDescription(CommunicationPreference.NONE)
            assertThat(result).isEqualTo("Your overtime shift on Sunday, 3rd May has changed.")
        }

        @Test
        fun `Should return overtime shift changed Sms`() {
            val shiftNotification = Notification(1, "", LocalDateTime.MIN, now.atStartOfDay(), now.atStartOfDay(), null, DetailParentType.OVERTIME, DetailModificationType.EDIT, false)
            val result = shiftNotification.getNotificationDescription(CommunicationPreference.SMS)
            assertThat(result).isEqualTo("Your overtime shift on Sunday, 3rd May has changed.")
        }

        @Test
        fun `Should return overtime shift changed Email`() {
            val shiftNotification = Notification(1, "", LocalDateTime.MIN, now.atStartOfDay(), now.atStartOfDay(), null, DetailParentType.OVERTIME, DetailModificationType.EDIT, false)
            val result = shiftNotification.getNotificationDescription(CommunicationPreference.EMAIL)
            assertThat(result).isEqualTo("* Your overtime shift on Sunday, 3rd May has changed.")
        }
    }

    @Nested
    @DisplayName("Shift, Add, This year tests")
    inner class ShiftAddThisYear {
        @Test
        fun `Should return shift changed None`() {
            val shiftNotification = Notification(1, "", LocalDateTime.MIN, now.atStartOfDay(), now.atStartOfDay(), null, DetailParentType.SHIFT, DetailModificationType.ADD, false)
            val result = shiftNotification.getNotificationDescription(CommunicationPreference.NONE)
            assertThat(result).isEqualTo("Your shift on Sunday, 3rd May has been added.")
        }

        @Test
        fun `Should return shift changed Sms`() {
            val shiftNotification = Notification(1, "", LocalDateTime.MIN, now.atStartOfDay(), now.atStartOfDay(), null, DetailParentType.SHIFT, DetailModificationType.ADD, false)
            val result = shiftNotification.getNotificationDescription(CommunicationPreference.SMS)
            assertThat(result).isEqualTo("Your shift on Sunday, 3rd May has been added.")
        }

        @Test
        fun `Should return shift changed Email`() {
            val shiftNotification = Notification(1, "", LocalDateTime.MIN, now.atStartOfDay(), now.atStartOfDay(), null, DetailParentType.SHIFT, DetailModificationType.ADD, false)
            val result = shiftNotification.getNotificationDescription(CommunicationPreference.EMAIL)
            assertThat(result).isEqualTo("* Your shift on Sunday, 3rd May has been added.")
        }
    }

    @Nested
    @DisplayName("Overtime Shift, Add, This year tests")
    inner class OvertimeShiftAddThisYear {
        @Test
        fun `Should return overtime shift changed None`() {
            val shiftNotification = Notification(1, "", LocalDateTime.MIN, now.atStartOfDay(), now.atStartOfDay(), null, DetailParentType.OVERTIME, DetailModificationType.ADD, false)
            val result = shiftNotification.getNotificationDescription(CommunicationPreference.NONE)
            assertThat(result).isEqualTo("Your overtime shift on Sunday, 3rd May has been added.")
        }

        @Test
        fun `Should return overtime shift changed Sms`() {
            val shiftNotification = Notification(1, "", LocalDateTime.MIN, now.atStartOfDay(), now.atStartOfDay(), null, DetailParentType.OVERTIME, DetailModificationType.ADD, false)
            val result = shiftNotification.getNotificationDescription(CommunicationPreference.SMS)
            assertThat(result).isEqualTo("Your overtime shift on Sunday, 3rd May has been added.")
        }

        @Test
        fun `Should return overtime shift changed Email`() {
            val shiftNotification = Notification(1, "", LocalDateTime.MIN, now.atStartOfDay(), now.atStartOfDay(), null, DetailParentType.OVERTIME, DetailModificationType.ADD, false)
            val result = shiftNotification.getNotificationDescription(CommunicationPreference.EMAIL)
            assertThat(result).isEqualTo("* Your overtime shift on Sunday, 3rd May has been added.")
        }
    }

    @Nested
    @DisplayName("Shift, Remove, This year tests")
    inner class ShiftRemoveThisYear {
        @Test
        fun `Should return shift changed None`() {
            val shiftNotification = Notification(1, "", LocalDateTime.MIN, now.atStartOfDay(), now.atStartOfDay(), null, DetailParentType.SHIFT, DetailModificationType.DELETE, false)
            val result = shiftNotification.getNotificationDescription(CommunicationPreference.NONE)
            assertThat(result).isEqualTo("Your shift on Sunday, 3rd May has been removed.")
        }

        @Test
        fun `Should return shift changed Sms`() {
            val shiftNotification = Notification(1, "", LocalDateTime.MIN, now.atStartOfDay(), now.atStartOfDay(), null, DetailParentType.SHIFT, DetailModificationType.DELETE, false)
            val result = shiftNotification.getNotificationDescription(CommunicationPreference.SMS)
            assertThat(result).isEqualTo("Your shift on Sunday, 3rd May has been removed.")
        }

        @Test
        fun `Should return shift changed Email`() {
            val shiftNotification = Notification(1, "", LocalDateTime.MIN, now.atStartOfDay(), now.atStartOfDay(), null, DetailParentType.SHIFT, DetailModificationType.DELETE, false)
            val result = shiftNotification.getNotificationDescription(CommunicationPreference.EMAIL)
            assertThat(result).isEqualTo("* Your shift on Sunday, 3rd May has been removed.")
        }
    }

    @Nested
    @DisplayName("Overtime Shift, Remove, This year tests")
    inner class OvertimeShiftRemoveThisYear {
        @Test
        fun `Should return overtime shift changed None`() {
            val shiftNotification = Notification(1, "", LocalDateTime.MIN, now.atStartOfDay(), now.atStartOfDay(), null, DetailParentType.OVERTIME, DetailModificationType.DELETE, false)
            val result = shiftNotification.getNotificationDescription(CommunicationPreference.NONE)
            assertThat(result).isEqualTo("Your overtime shift on Sunday, 3rd May has been removed.")
        }

        @Test
        fun `Should return overtime shift changed Sms`() {
            val shiftNotification = Notification(1, "", LocalDateTime.MIN, now.atStartOfDay(), now.atStartOfDay(), null, DetailParentType.OVERTIME, DetailModificationType.DELETE, false)
            val result = shiftNotification.getNotificationDescription(CommunicationPreference.SMS)
            assertThat(result).isEqualTo("Your overtime shift on Sunday, 3rd May has been removed.")
        }

        @Test
        fun `Should return overtime shift changed Email`() {
            val shiftNotification = Notification(1, "", LocalDateTime.MIN, now.atStartOfDay(), now.atStartOfDay(), null, DetailParentType.OVERTIME, DetailModificationType.DELETE, false)
            val result = shiftNotification.getNotificationDescription(CommunicationPreference.EMAIL)
            assertThat(result).isEqualTo("* Your overtime shift on Sunday, 3rd May has been removed.")
        }
    }

    @Nested
    @DisplayName("Shift Task, Edit, This year tests")
    inner class ShiftTaskEditThisYear {
        @Test
        fun `Should return shift task changed None`() {
            val shiftNotification = Notification(1, "", LocalDateTime.MIN, now.atStartOfDay().plusSeconds(1234L), now.atStartOfDay().plusSeconds(12345L), "Test Duty", DetailParentType.SHIFT, DetailModificationType.EDIT, false)
            val result = shiftNotification.getNotificationDescription(CommunicationPreference.NONE)
            assertThat(result).isEqualTo("Your detail on Sunday, 3rd May (00:20:34 - 03:25:45) has changed to Test Duty.")
        }

        @Test
        fun `Should return shift changed Sms`() {
            val shiftNotification = Notification(1, "", LocalDateTime.MIN, now.atStartOfDay().plusSeconds(1234L), now.atStartOfDay().plusSeconds(12345L), "Test Duty", DetailParentType.SHIFT, DetailModificationType.EDIT, false)
            val result = shiftNotification.getNotificationDescription(CommunicationPreference.SMS)
            assertThat(result).isEqualTo("Your detail on Sunday, 3rd May (00:20:34 - 03:25:45) has changed.")
        }

        @Test
        fun `Should return shift changed Email`() {
            val shiftNotification = Notification(1, "", LocalDateTime.MIN, now.atStartOfDay().plusSeconds(1234L), now.atStartOfDay().plusSeconds(12345L), "Test Duty", DetailParentType.SHIFT, DetailModificationType.EDIT, false)
            val result = shiftNotification.getNotificationDescription(CommunicationPreference.EMAIL)
            assertThat(result).isEqualTo("* Your detail on Sunday, 3rd May (00:20:34 - 03:25:45) has changed.")
        }
    }

    @Nested
    @DisplayName("Overtime Shift Task, Edit, This year tests")
    inner class OvertimeShiftTaskEditThisYear {
        @Test
        fun `Should return shift task changed None`() {
            val shiftNotification = Notification(1, "", LocalDateTime.MIN, now.atStartOfDay().plusSeconds(1234L), now.atStartOfDay().plusSeconds(12345L), "Test Duty", DetailParentType.OVERTIME, DetailModificationType.EDIT, false)
            val result = shiftNotification.getNotificationDescription(CommunicationPreference.NONE)
            assertThat(result).isEqualTo("Your overtime detail on Sunday, 3rd May (00:20:34 - 03:25:45) has changed to Test Duty.")
        }

        @Test
        fun `Should return shift changed Sms`() {
            val shiftNotification = Notification(1, "", LocalDateTime.MIN, now.atStartOfDay().plusSeconds(1234L), now.atStartOfDay().plusSeconds(12345L), "Test Duty", DetailParentType.OVERTIME, DetailModificationType.EDIT, false)
            val result = shiftNotification.getNotificationDescription(CommunicationPreference.SMS)
            assertThat(result).isEqualTo("Your overtime detail on Sunday, 3rd May (00:20:34 - 03:25:45) has changed.")
        }

        @Test
        fun `Should return shift changed Email`() {
            val shiftNotification = Notification(1, "", LocalDateTime.MIN, now.atStartOfDay().plusSeconds(1234L), now.atStartOfDay().plusSeconds(12345L), "Test Duty", DetailParentType.OVERTIME, DetailModificationType.EDIT, false)
            val result = shiftNotification.getNotificationDescription(CommunicationPreference.EMAIL)
            assertThat(result).isEqualTo("* Your overtime detail on Sunday, 3rd May (00:20:34 - 03:25:45) has changed.")
        }
    }

    @Nested
    @DisplayName("Shift Task, Add, This year tests")
    inner class ShiftTaskAddThisYear {
        @Test
        fun `Should return shift task changed None`() {
            val shiftNotification = Notification(1, "", LocalDateTime.MIN, now.atStartOfDay().plusSeconds(1234L), now.atStartOfDay().plusSeconds(12345L), "Test Duty", DetailParentType.SHIFT, DetailModificationType.ADD, false)
            val result = shiftNotification.getNotificationDescription(CommunicationPreference.NONE)
            assertThat(result).isEqualTo("Your detail on Sunday, 3rd May (00:20:34 - 03:25:45) has been added as Test Duty.")
        }

        @Test
        fun `Should return shift changed Sms`() {
            val shiftNotification = Notification(1, "", LocalDateTime.MIN, now.atStartOfDay().plusSeconds(1234L), now.atStartOfDay().plusSeconds(12345L), "Test Duty", DetailParentType.SHIFT, DetailModificationType.ADD, false)
            val result = shiftNotification.getNotificationDescription(CommunicationPreference.SMS)
            assertThat(result).isEqualTo("Your detail on Sunday, 3rd May (00:20:34 - 03:25:45) has been added.")
        }

        @Test
        fun `Should return shift changed Email`() {
            val shiftNotification = Notification(1, "", LocalDateTime.MIN, now.atStartOfDay().plusSeconds(1234L), now.atStartOfDay().plusSeconds(12345L), "Test Duty", DetailParentType.SHIFT, DetailModificationType.ADD, false)
            val result = shiftNotification.getNotificationDescription(CommunicationPreference.EMAIL)
            assertThat(result).isEqualTo("* Your detail on Sunday, 3rd May (00:20:34 - 03:25:45) has been added.")
        }
    }

    @Nested
    @DisplayName("Overtime Shift Task, Add, This year tests")
    inner class OvertimeShiftTaskAddThisYear {
        @Test
        fun `Should return shift task changed None`() {
            val shiftNotification = Notification(1, "", LocalDateTime.MIN, now.atStartOfDay().plusSeconds(1234L), now.atStartOfDay().plusSeconds(12345L), "Test Duty", DetailParentType.OVERTIME, DetailModificationType.ADD, false)
            val result = shiftNotification.getNotificationDescription(CommunicationPreference.NONE)
            assertThat(result).isEqualTo("Your overtime detail on Sunday, 3rd May (00:20:34 - 03:25:45) has been added as Test Duty.")
        }

        @Test
        fun `Should return shift changed Sms`() {
            val shiftNotification = Notification(1, "", LocalDateTime.MIN, now.atStartOfDay().plusSeconds(1234L), now.atStartOfDay().plusSeconds(12345L), "Test Duty", DetailParentType.OVERTIME, DetailModificationType.ADD, false)
            val result = shiftNotification.getNotificationDescription(CommunicationPreference.SMS)
            assertThat(result).isEqualTo("Your overtime detail on Sunday, 3rd May (00:20:34 - 03:25:45) has been added.")
        }

        @Test
        fun `Should return shift changed Email`() {
            val shiftNotification = Notification(1, "", LocalDateTime.MIN, now.atStartOfDay().plusSeconds(1234L), now.atStartOfDay().plusSeconds(12345L), "Test Duty", DetailParentType.OVERTIME, DetailModificationType.ADD, false)
            val result = shiftNotification.getNotificationDescription(CommunicationPreference.EMAIL)
            assertThat(result).isEqualTo("* Your overtime detail on Sunday, 3rd May (00:20:34 - 03:25:45) has been added.")
        }
    }

    @Nested
    @DisplayName("Shift Task, Remove, This year tests")
    inner class ShiftTaskRemoveThisYear {
        @Test
        fun `Should return shift task changed None`() {
            val shiftNotification = Notification(1, "", LocalDateTime.MIN, now.atStartOfDay().plusSeconds(1234L), now.atStartOfDay().plusSeconds(12345L), "Test Duty", DetailParentType.SHIFT, DetailModificationType.DELETE, false)
            val result = shiftNotification.getNotificationDescription(CommunicationPreference.NONE)
            assertThat(result).isEqualTo("Your detail on Sunday, 3rd May (00:20:34 - 03:25:45) has been removed (was Test Duty).")
        }

        @Test
        fun `Should return shift changed Sms`() {
            val shiftNotification = Notification(1, "", LocalDateTime.MIN, now.atStartOfDay().plusSeconds(1234L), now.atStartOfDay().plusSeconds(12345L), "Test Duty", DetailParentType.SHIFT, DetailModificationType.DELETE, false)
            val result = shiftNotification.getNotificationDescription(CommunicationPreference.SMS)
            assertThat(result).isEqualTo("Your detail on Sunday, 3rd May (00:20:34 - 03:25:45) has been removed.")
        }

        @Test
        fun `Should return shift changed Email`() {
            val shiftNotification = Notification(1, "", LocalDateTime.MIN, now.atStartOfDay().plusSeconds(1234L), now.atStartOfDay().plusSeconds(12345L), "Test Duty", DetailParentType.SHIFT, DetailModificationType.DELETE, false)
            val result = shiftNotification.getNotificationDescription(CommunicationPreference.EMAIL)
            assertThat(result).isEqualTo("* Your detail on Sunday, 3rd May (00:20:34 - 03:25:45) has been removed.")
        }
    }

    @Nested
    @DisplayName("Overtime Shift Task, Remove, This year tests")
    inner class OvertimeShiftTaskRemoveThisYear {
        @Test
        fun `Should return shift task changed None`() {
            val shiftNotification = Notification(1, "", LocalDateTime.MIN, now.atStartOfDay().plusSeconds(1234L), now.atStartOfDay().plusSeconds(12345L), "Test Duty", DetailParentType.OVERTIME, DetailModificationType.DELETE, false)
            val result = shiftNotification.getNotificationDescription(CommunicationPreference.NONE)
            assertThat(result).isEqualTo("Your overtime detail on Sunday, 3rd May (00:20:34 - 03:25:45) has been removed (was Test Duty).")
        }

        @Test
        fun `Should return shift changed Sms`() {
            val shiftNotification = Notification(1, "", LocalDateTime.MIN, now.atStartOfDay().plusSeconds(1234L), now.atStartOfDay().plusSeconds(12345L), "Test Duty", DetailParentType.OVERTIME, DetailModificationType.DELETE, false)
            val result = shiftNotification.getNotificationDescription(CommunicationPreference.SMS)
            assertThat(result).isEqualTo("Your overtime detail on Sunday, 3rd May (00:20:34 - 03:25:45) has been removed.")
        }

        @Test
        fun `Should return shift changed Email`() {
            val shiftNotification = Notification(1, "", LocalDateTime.MIN, now.atStartOfDay().plusSeconds(1234L), now.atStartOfDay().plusSeconds(12345L), "Test Duty", DetailParentType.OVERTIME, DetailModificationType.DELETE, false)
            val result = shiftNotification.getNotificationDescription(CommunicationPreference.EMAIL)
            assertThat(result).isEqualTo("* Your overtime detail on Sunday, 3rd May (00:20:34 - 03:25:45) has been removed.")
        }
    }

}