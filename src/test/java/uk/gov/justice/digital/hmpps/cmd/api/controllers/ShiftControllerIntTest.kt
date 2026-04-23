package uk.gov.justice.digital.hmpps.cmd.api.controllers

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.groups.Tuple
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient
import org.springframework.test.web.servlet.client.RestTestClient
import uk.gov.justice.digital.hmpps.cmd.api.dto.DetailDto
import uk.gov.justice.digital.hmpps.prisonertonomisupdate.wiremock.HmppsAuthApiExtension
import uk.gov.justice.digital.hmpps.prisonertonomisupdate.wiremock.PrisonApiExtension

@ExtendWith(PrisonApiExtension::class, HmppsAuthApiExtension::class)
@DisplayName("Integration Tests for Shift Controller")
@AutoConfigureRestTestClient
class ShiftControllerIntTest(
  @Autowired private val restTestClient: RestTestClient,
  @Autowired private val entityBuilder: EntityWithJwtAuthorisationBuilder,
) : ResourceTest() {

  @Test
  fun `It returns shifts`() {
    PrisonApiExtension.api.stubUsersMe("WNI") // WNI is in the region 1

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
    PrisonApiExtension.api.stubUsersMe("WNI") // WNI is in the region 1

    restTestClient.get().uri("/user/details?from=2013-08-01&to=2013-08-30")
      .headers(entityBuilder.entityWithJwtAuthorisation(A_USER, PRISON_ROLE))
      .exchange()
      .expectStatus().isOk
      .expectBody()
      .jsonPath("$.length()").isEqualTo(30)
      .jsonPath("$[19].date").isEqualTo("2013-08-20")
      .jsonPath("$[19].fullDayTypeDescription").isEqualTo("None")
      .jsonPath("$[20].date").isEqualTo("2013-08-21")
      .jsonPath("$[20].fullDayType").isEqualTo("SHIFT")
      .jsonPath("$[20].details").value<List<DetailDto>> { details ->
        assertThat(details)
          .extracting("start", "end", "parentType")
          .containsExactly(
            Tuple("2013-08-21T00:00:00", "2013-08-21T00:00:00", "SHIFT"),
          )
      }
  }

  companion object {

    private const val A_USER = "TEST-USER"
    private val PRISON_ROLE = listOf("ROLE_PRISON")
  }
}
