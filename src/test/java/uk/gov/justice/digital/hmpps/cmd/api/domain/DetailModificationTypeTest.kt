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
  @DisplayName("Convert from Int")
  inner class CaseInsensitiveFromInt {
    @Test
    fun `It should match 0 and Unchanged`() {
      assertThat(DetailModificationType.from(0)).isEqualTo(DetailModificationType.UNCHANGED)
    }

    @Test
    fun `It should match 1 and Add`() {
      assertThat(DetailModificationType.from(1)).isEqualTo(DetailModificationType.ADD)
    }

    @Test
    fun `It should match 2 and Edit`() {
      assertThat(DetailModificationType.from(2)).isEqualTo(DetailModificationType.EDIT)
    }

    @Test
    fun `It should match 3 and Delete`() {
      assertThat(DetailModificationType.from(3)).isEqualTo(DetailModificationType.DELETE)
    }
  }
}
