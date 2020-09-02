package uk.gov.justice.digital.hmpps.cmd.api.domain

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class ShiftActionTypeTest {

    @Nested
    @DisplayName("Case Insensitive From")
    inner class CaseInsensitiveFrom {
        @Test
        fun `It should match case insensitive lower`() {
            assertThat(ShiftActionType.from("edit")).isEqualTo(ShiftActionType.EDIT)
        }

        @Test
        fun `It should match case insensitive upper`() {
            assertThat(ShiftActionType.from("EDIT")).isEqualTo(ShiftActionType.EDIT)
        }

        @Test
        fun `It should match case insensitive mixed`() {
            assertThat(ShiftActionType.from("eDIt")).isEqualTo(ShiftActionType.EDIT)
        }
    }

    @Nested
    @DisplayName("Case Insensitive Equals")
    inner class CaseInsensitiveEquals {
        @Test
        fun `It should match case insensitive lower`() {
            assertThat(ShiftActionType.EDIT.equalsValue("edit")).isTrue()
        }

        @Test
        fun `It should match case insensitive upper`() {
            assertThat(ShiftActionType.EDIT.equalsValue("EDIT")).isTrue()
        }

        @Test
        fun `It should match case insensitive mixed`() {
            assertThat(ShiftActionType.EDIT.equalsValue("eDIt")).isTrue()        }
    }

}