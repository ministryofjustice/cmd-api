package uk.gov.justice.digital.hmpps.cmd.api.domain

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class ShiftNotificationTypeTest {

    @Nested
    @DisplayName("Case Insensitive From")
    inner class CaseInsensitiveFrom {
        @Test
        fun `It should match case insensitive lower`() {
            assertThat(ShiftNotificationType.from("shift")).isEqualTo(ShiftNotificationType.SHIFT)
        }

        @Test
        fun `It should match case insensitive upper`() {
            assertThat(ShiftNotificationType.from("SHIFT")).isEqualTo(ShiftNotificationType.SHIFT)
        }

        @Test
        fun `It should match case insensitive mixed`() {
            assertThat(ShiftNotificationType.from("sHIFt")).isEqualTo(ShiftNotificationType.SHIFT)
        }
    }

    @Nested
    @DisplayName("Case Insensitive Equals")
    inner class CaseInsensitiveEquals {
        @Test
        fun `It should match case insensitive lower`() {

            assertThat(ShiftNotificationType.SHIFT.equalsValue("shift")).isTrue()
        }

        @Test
        fun `It should match case insensitive upper`() {
            assertThat(ShiftNotificationType.SHIFT.equalsValue("SHIFT")).isTrue()
        }

        @Test
        fun `It should match case insensitive mixed`() {
            assertThat(ShiftNotificationType.SHIFT.equalsValue("sHIFt")).isTrue()
        }
    }

}