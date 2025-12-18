package uk.gov.justice.digital.hmpps.cmd.api.controllers

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.groups.Tuple
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient
import org.springframework.http.HttpStatus
import org.springframework.test.web.servlet.client.RestTestClient
import uk.gov.justice.digital.hmpps.cmd.api.dto.DetailDto
import uk.gov.justice.digital.hmpps.prisonertonomisupdate.wiremock.CsrApiExtension
import uk.gov.justice.digital.hmpps.prisonertonomisupdate.wiremock.HmppsAuthApiExtension
import uk.gov.justice.digital.hmpps.prisonertonomisupdate.wiremock.PrisonApiExtension

@ExtendWith(PrisonApiExtension::class, CsrApiExtension::class, HmppsAuthApiExtension::class)
@DisplayName("Integration Tests for Shift Controller")
@AutoConfigureRestTestClient
class ShiftControllerIntegrationTest(
  @Autowired val restTestClient: RestTestClient,
  @Autowired val entityBuilder: EntityWithJwtAuthorisationBuilder,
) : ResourceTest() {

  @Test
  fun `It returns shifts`() {
    PrisonApiExtension.api.stubUsersMe()
    CsrApiExtension.api.stubUserDetails(
      // WMI is region 3
      3,
      "2022-04-06",
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

    restTestClient.get()
      .uri("/user/details?from=2022-04-06&to=2022-04-06")
      .headers(entityBuilder.entityWithJwtAuthorisation(A_USER, PRISON_ROLE))
      .exchange()
      .expectStatus().isOk
      .expectBody()
      .jsonPath("$.length()").isEqualTo(1)
      .jsonPath("$[0].date").isEqualTo("2022-04-06")
  }

  @Test
  fun `It returns shifts with missing activity`() {
    PrisonApiExtension.api.stubUsersMe()
    CsrApiExtension.api.stubUserDetails(
      // WMI is region 3
      3,
      "2022-04-01",
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

    restTestClient.get().uri("/user/details?from=2022-04-01&to=2022-04-30")
      .headers(entityBuilder.entityWithJwtAuthorisation(A_USER, PRISON_ROLE))
      .exchange()
      .expectStatus().isEqualTo(HttpStatus.OK)
      .expectBody()
      .jsonPath("$.length()").isEqualTo(30)
      .jsonPath("$[4].date").isEqualTo("2022-04-05")
      .jsonPath("$[4].fullDayTypeDescription").isEqualTo("None")
      .jsonPath("$[5].date").isEqualTo("2022-04-06")
      .jsonPath("$[5].fullDayType").isEqualTo("SHIFT")
      .jsonPath("$[5].details").value<List<DetailDto>> { details ->
        assertThat(details)
          .extracting("activity", "displayType", "displayTypeTime", "start", "end", "parentType", "finishDuration")
          .containsExactly(
            Tuple(null, "DAY_START", "2022-04-06T10:00:00", "2022-04-06T10:00:00", "2022-04-06T11:00:00", "SHIFT", null),
            Tuple(null, "DAY_FINISH", "2022-04-06T11:00:00", "2022-04-06T10:00:00", "2022-04-06T11:00:00", "SHIFT", 3600),
          )
      }
  }

  @Test
  fun `It calls csr api with authentication`() {
    PrisonApiExtension.api.stubUsersMe()
    CsrApiExtension.api.stubUserDetails(
      // WMI is region 3
      3,
      "2022-04-06",
      "2022-04-06",
      "[]",
    )

    restTestClient.get()
      .uri("/user/details?from=2022-04-06&to=2022-04-06")
      .headers(entityBuilder.entityWithJwtAuthorisation(A_USER, PRISON_ROLE))
      .exchange()
      .expectStatus().isOk

    val requests = CsrApiExtension.api.getFor("/user/details/3?from=2022-04-06&to=2022-04-06")
    assertThat(requests).hasSize(1)
    assertThat(requests[0].getHeader("Authorization")).isEqualTo("Bearer ABCDE")
  }

  companion object {

    private const val A_USER = "API_TEST_USER"
    private val PRISON_ROLE = listOf("ROLE_PRISON")
  }
}
