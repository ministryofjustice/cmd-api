package uk.gov.justice.digital.hmpps.cmd.api.domain

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.cmd.api.uk.gov.justice.digital.hmpps.cmd.api.domain.ShiftActionType

class ShiftActionTypeTest {

    @Nested
    @DisplayName("Case Insensitive")
    inner class CaseInsensitive {
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

}