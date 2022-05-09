package uk.gov.justice.digital.hmpps.cmd.api.controllers

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.json.BasicJsonTester
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.test.context.ActiveProfiles
import uk.gov.justice.digital.hmpps.cmd.api.dto.ShiftDto
import uk.gov.justice.digital.hmpps.prisonertonomisupdate.wiremock.CsrApiExtension
import uk.gov.justice.digital.hmpps.prisonertonomisupdate.wiremock.HmppsAuthApiExtension
import uk.gov.justice.digital.hmpps.prisonertonomisupdate.wiremock.PrisonApiExtension

@ExtendWith(PrisonApiExtension::class, CsrApiExtension::class, HmppsAuthApiExtension::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles(value = ["test"])
@DisplayName("Integration Tests for Shift Controller")
class ShiftControllerIntegrationTest(
  @Autowired val testRestTemplate: TestRestTemplate,
  @Autowired val entityBuilder: EntityWithJwtAuthorisationBuilder,
) {
  val jsonTester = BasicJsonTester(this.javaClass)

  @Test
  fun `It returns shifts`() {
    PrisonApiExtension.api.stubUsersMe()
    CsrApiExtension.api.stubUserDetails(
      1, "2022-04-06", "2022-04-06",
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
      """.trimIndent()
    )

    val response = testRestTemplate.exchange(
      "/user/details?from=2022-04-06&to=2022-04-06",
      HttpMethod.GET,
      entityBuilder.entityWithJwtAuthorisation(A_USER, NO_ROLES),
      String::class.java
    )
    with(response) {
      assertThat(statusCode).isEqualTo(HttpStatus.OK)
      assertThat(jsonTester.from(body)).extractingJsonPathArrayValue<ShiftDto>("$").hasSize(1)
      assertThat(jsonTester.from(body)).extractingJsonPathStringValue("$[0].date").isEqualTo("2022-04-06")
    }
  }

  companion object {

    private const val A_USER = "API_TEST_USER"
    private const val A_USER_NO_DATA = "API_TEST_USER_NO_DATA"
    private val NO_ROLES = listOf<String>()
  }
}
