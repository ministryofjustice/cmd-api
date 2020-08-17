package uk.gov.justice.digital.hmpps.cmd.api.domain

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.cmd.api.uk.gov.justice.digital.hmpps.cmd.api.domain.ShiftNotificationType

class ShiftNotificationTypeTest {

    @Nested
    @DisplayName("Case Insensitive")
    inner class CaseInsensitive {
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

}