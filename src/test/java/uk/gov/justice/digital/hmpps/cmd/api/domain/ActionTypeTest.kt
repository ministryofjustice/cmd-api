package uk.gov.justice.digital.hmpps.cmd.api.domain

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class DetailModificationTypeTest {

    @Nested
    @DisplayName("Case Insensitive From")
    inner class CaseInsensitiveFrom {
        @Test
        fun `It should match case insensitive lower`() {
            assertThat(DetailModificationType.from("edit")).isEqualTo(DetailModificationType.EDIT)
        }

        @Test
        fun `It should match case insensitive upper`() {
            assertThat(DetailModificationType.from("EDIT")).isEqualTo(DetailModificationType.EDIT)
        }

        @Test
        fun `It should match case insensitive mixed`() {
            assertThat(DetailModificationType.from("eDIt")).isEqualTo(DetailModificationType.EDIT)
        }
    }

    @Nested
    @DisplayName("Case Insensitive Equals")
    inner class CaseInsensitiveEquals {
        @Test
        fun `It should match case insensitive lower`() {
            assertThat(DetailModificationType.EDIT.equalsValue("edit")).isTrue()
        }

        @Test
        fun `It should match case insensitive upper`() {
            assertThat(DetailModificationType.EDIT.equalsValue("EDIT")).isTrue()
        }

        @Test
        fun `It should match case insensitive mixed`() {
            assertThat(DetailModificationType.EDIT.equalsValue("eDIt")).isTrue()        }
    }

}