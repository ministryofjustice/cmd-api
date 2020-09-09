package uk.gov.justice.digital.hmpps.cmd.api.domain

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.cmd.api.uk.gov.justice.digital.hmpps.cmd.api.domain.DetailParentType

class DetailParentTypeTest {

    @Nested
    @DisplayName("Case Insensitive From")
    inner class CaseInsensitiveFrom {
        @Test
        fun `It should match case insensitive lower`() {
            assertThat(DetailParentType.from("shift")).isEqualTo(DetailParentType.SHIFT)
        }

        @Test
        fun `It should match case insensitive upper`() {
            assertThat(DetailParentType.from("SHIFT")).isEqualTo(DetailParentType.SHIFT)
        }

        @Test
        fun `It should match case insensitive mixed`() {
            assertThat(DetailParentType.from("sHIFt")).isEqualTo(DetailParentType.SHIFT)
        }
    }
}