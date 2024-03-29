package uk.gov.justice.digital.hmpps.cmd.api.domain

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class ShiftTypeTest {

  @Nested
  @DisplayName("Case Insensitive From")
  inner class CaseInsensitiveFrom {
    @Test
    fun `It should match case insensitive lower`() {
      assertThat(ShiftType.from("shift")).isEqualTo(ShiftType.SHIFT)
    }

    @Test
    fun `It should match case insensitive upper`() {
      assertThat(ShiftType.from("SHIFT")).isEqualTo(ShiftType.SHIFT)
    }

    @Test
    fun `It should match case insensitive mixed`() {
      assertThat(ShiftType.from("sHIFt")).isEqualTo(ShiftType.SHIFT)
    }
  }
}
