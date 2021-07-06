package uk.gov.justice.digital.hmpps.cmd.api.domain

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.cmd.api.uk.gov.justice.digital.hmpps.cmd.api.domain.FullDayActivityType

class FullDayActivityTypeTest {

  @Nested
  @DisplayName("Case Insensitive")
  inner class CaseInsensitive {
    @Test
    fun `It should match case insensitive lower`() {
      assertThat(FullDayActivityType.from("break")).isEqualTo(FullDayActivityType.BREAK)
    }

    @Test
    fun `It should match case insensitive upper`() {
      assertThat(FullDayActivityType.from("BREAK")).isEqualTo(FullDayActivityType.BREAK)
    }

    @Test
    fun `It should match case insensitive mixed`() {
      assertThat(FullDayActivityType.from("BrEaK")).isEqualTo(FullDayActivityType.BREAK)
    }

    @Test
    fun `It should match case insensitive with extra after`() {
      assertThat(FullDayActivityType.from("BrEaK (UNPAID)")).isEqualTo(FullDayActivityType.BREAK)
    }

    @Test
    fun `It should match case insensitive with extra before`() {
      assertThat(FullDayActivityType.from("Unpaid BrEaK")).isEqualTo(FullDayActivityType.BREAK)
    }
  }

  @Nested
  @DisplayName("'from' tests")
  inner class fromValueTests {

    @Test
    fun `It should match BREAK`() {
      assertThat(FullDayActivityType.from("break")).isEqualTo(FullDayActivityType.BREAK)
    }

    @Test
    fun `It should match REST_DAY`() {
      assertThat(FullDayActivityType.from("Rest Day")).isEqualTo(FullDayActivityType.REST_DAY)
    }

    @Test
    fun `It should match HOLIDAY`() {
      assertThat(FullDayActivityType.from("Annual Leave")).isEqualTo(FullDayActivityType.HOLIDAY)
    }

    @Test
    fun `It should match ILNESS`() {
      assertThat(FullDayActivityType.from("Sick")).isEqualTo(FullDayActivityType.ILLNESS)
    }

    @Test
    fun `It should match ABSENCE`() {
      assertThat(FullDayActivityType.from("Absence")).isEqualTo(FullDayActivityType.ABSENCE)
    }

    @Test
    fun `It should match TU_OFFICIALS_LEAVE_DAYS`() {
      assertThat(FullDayActivityType.from("TU Officials Leave Days")).isEqualTo(FullDayActivityType.TU_OFFICIALS_LEAVE_DAYS)
    }

    @Test
    fun `It should match TU_OFFICIALS_LEAVE_HOURS`() {
      assertThat(FullDayActivityType.from("TU Officials Leave Hours")).isEqualTo(FullDayActivityType.TU_OFFICIALS_LEAVE_HOURS)
    }

    @Test
    fun `It should match SECONDMENT`() {
      assertThat(FullDayActivityType.from("Secondment")).isEqualTo(FullDayActivityType.SECONDMENT)
    }

    @Test
    fun `It should match TOIL`() {
      assertThat(FullDayActivityType.from("Toil")).isEqualTo(FullDayActivityType.TOIL)
    }
    @Test
    fun `It should match TRAINING_EXTERNAL`() {
      assertThat(FullDayActivityType.from("Training - External")).isEqualTo(FullDayActivityType.TRAINING_EXTERNAL)
    }

    @Test
    fun `It should match TRAINING_INTERNAL`() {
      assertThat(FullDayActivityType.from("Training - Internal")).isEqualTo(FullDayActivityType.TRAINING_INTERNAL)
    }

    @Test
    fun `It should match NONE`() {
      assertThat(FullDayActivityType.from("None")).isEqualTo(FullDayActivityType.NONE)
    }

    @Test
    fun `It should match SHIFT`() {
      assertThat(FullDayActivityType.from("Shift")).isEqualTo(FullDayActivityType.SHIFT)
    }
  }
}
