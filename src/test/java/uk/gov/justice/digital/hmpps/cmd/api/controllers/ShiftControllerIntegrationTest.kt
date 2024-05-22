package uk.gov.justice.digital.hmpps.cmd.api.controllers

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.groups.Tuple
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import uk.gov.justice.digital.hmpps.cmd.api.dto.DetailDto
import uk.gov.justice.digital.hmpps.cmd.api.dto.ShiftDto
import uk.gov.justice.digital.hmpps.prisonertonomisupdate.wiremock.CsrApiExtension
import uk.gov.justice.digital.hmpps.prisonertonomisupdate.wiremock.HmppsAuthApiExtension
import uk.gov.justice.digital.hmpps.prisonertonomisupdate.wiremock.PrisonApiExtension

@ExtendWith(PrisonApiExtension::class, CsrApiExtension::class, HmppsAuthApiExtension::class)
@DisplayName("Integration Tests for Shift Controller")
class ShiftControllerIntegrationTest(
  @Autowired val testRestTemplate: TestRestTemplate,
  @Autowired val entityBuilder: EntityWithJwtAuthorisationBuilder,
) : ResourceTest() {

  @Test
  fun `It returns shifts`() {
    PrisonApiExtension.api.stubUsersMe()
    CsrApiExtension.api.stubUserDetails(
      3,
      "2022-04-06",
      // WMI is region 3
      "2022-04-06",
      """
      [
        {
        "id" : "1",
        "quantumId" : "$A_USER",
        "shiftModified" : "2022-03-25T15:00:00",
        "shiftType" : "SHIFT",
        "detailStart" : "2022-03-31T10:00:00",
        "detailEnd" : "2022-03-31T11:00:00",
        "activity" : "CCTV monitoring",
        "actionType" : "ADD"
        }
      ]
      """.trimIndent(),
    )

    val response = testRestTemplate.exchange(
      "/user/details?from=2022-04-06&to=2022-04-06",
      HttpMethod.GET,
      entityBuilder.entityWithJwtAuthorisation(A_USER, PRISON_ROLE),
      String::class.java,
    )
    with(response) {
      assertThat(statusCode).isEqualTo(HttpStatus.OK)
      assertThat(jsonTester.from(body)).extractingJsonPathArrayValue<ShiftDto>("$").hasSize(1)
      assertThat(jsonTester.from(body)).extractingJsonPathStringValue("$[0].date").isEqualTo("2022-04-06")
    }
  }

  @Test
  fun `It returns shifts with missing activity`() {
    PrisonApiExtension.api.stubUsersMe()
    CsrApiExtension.api.stubUserDetails(
      3,
      "2022-04-01",
      // WMI is region 3
      "2022-04-30",
      """
      [
        {
        "id" : "1",
        "quantumId" : "$A_USER",
        "shiftModified" : "2022-04-06T15:00:00",
        "shiftType" : "SHIFT",
        "detailStart" : "2022-04-06T10:00:00",
        "detailEnd" : "2022-04-06T11:00:00",
        "actionType" : "ADD"
        }
      ]
      """.trimIndent(),
    )

    val response = testRestTemplate.exchange(
      "/user/details?from=2022-04-01&to=2022-04-30",
      HttpMethod.GET,
      entityBuilder.entityWithJwtAuthorisation(A_USER, PRISON_ROLE),
      String::class.java,
    )
    with(response) {
      assertThat(statusCode).isEqualTo(HttpStatus.OK)
      val content = jsonTester.from(body)
      assertThat(content).extractingJsonPathArrayValue<ShiftDto>("$").hasSize(30)
      assertThat(content).extractingJsonPathStringValue("$[4].date").isEqualTo("2022-04-05")
      assertThat(content).extractingJsonPathStringValue("$[4].fullDayTypeDescription").isEqualTo("None")
      assertThat(content).extractingJsonPathStringValue("$[5].date").isEqualTo("2022-04-06")
      assertThat(content).extractingJsonPathStringValue("$[5].fullDayType").isEqualTo("SHIFT")
      assertThat(content).extractingJsonPathArrayValue<DetailDto>("$[5].details")
        .extracting("activity", "displayType", "displayTypeTime", "start", "end", "parentType", "finishDuration")
        .containsExactly(
          Tuple(null, "DAY_START", "2022-04-06T10:00:00", "2022-04-06T10:00:00", "2022-04-06T11:00:00", "SHIFT", null),
          Tuple(null, "DAY_FINISH", "2022-04-06T11:00:00", "2022-04-06T10:00:00", "2022-04-06T11:00:00", "SHIFT", 3600),
        )
    }
  }

  companion object {

    private const val A_USER = "API_TEST_USER"
    private val PRISON_ROLE = listOf("ROLE_PRISON")
  }
}
