package uk.gov.justice.digital.hmpps.cmd.api.domain

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class CommunicationPreferenceTest {

    @Nested
    @DisplayName("Case Insensitive")
    inner class CaseInsensitive {
        @Test
        fun `It should match case insensitive lower`() {
            assertThat(CommunicationPreference.from("email")).isEqualTo(CommunicationPreference.EMAIL)
        }

        @Test
        fun `It should match case insensitive upper`() {
            assertThat(CommunicationPreference.from("EMAIL")).isEqualTo(CommunicationPreference.EMAIL)
        }

        @Test
        fun `It should match case insensitive mixed`() {
            assertThat(CommunicationPreference.from("eMAIl")).isEqualTo(CommunicationPreference.EMAIL)
        }
    }

}