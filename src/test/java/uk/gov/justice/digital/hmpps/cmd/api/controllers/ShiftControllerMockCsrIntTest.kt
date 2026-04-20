package uk.gov.justice.digital.hmpps.cmd.api.controllers

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.groups.Tuple
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.client.RestTestClient
import uk.gov.justice.digital.hmpps.cmd.api.client.CsrDetailDto
import uk.gov.justice.digital.hmpps.cmd.api.client.CsrRegionSelectorService
import uk.gov.justice.digital.hmpps.cmd.api.domain.ShiftType
import uk.gov.justice.digital.hmpps.cmd.api.dto.DetailDto
import uk.gov.justice.digital.hmpps.prisonertonomisupdate.wiremock.HmppsAuthApiExtension
import uk.gov.justice.digital.hmpps.prisonertonomisupdate.wiremock.PrisonApiExtension
import java.time.LocalDate
import java.time.LocalDateTime

@ExtendWith(PrisonApiExtension::class, HmppsAuthApiExtension::class)
@DisplayName("Integration Tests for Shift Controller")
@AutoConfigureRestTestClient
class ShiftControllerMockCsrIntTest(
  @Autowired private val restTestClient: RestTestClient,
  @Autowired private val entityBuilder: EntityWithJwtAuthorisationBuilder,
  @Autowired @MockitoBean private val csrRegionSelectorService: CsrRegionSelectorService,
) : ResourceTest() {

  @Test
  fun `It returns shifts`() {
    PrisonApiExtension.api.stubUsersMe()
    whenever(csrRegionSelectorService.getStaffDetails(any(), any(), eq(3))).thenReturn(
      listOf(
        CsrDetailDto(
          shiftType = ShiftType.SHIFT,
          detailStart = LocalDateTime.parse("2022-03-31T10:00:00"),
          detailEnd = LocalDateTime.parse("2022-03-31T11:00:00"),
          activity = "CCTV monitoring",
        ),
      ),
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
    whenever(csrRegionSelectorService.getStaffDetails(any(), any(), eq(3))).thenReturn(
      listOf(
        CsrDetailDto(
          shiftType = ShiftType.SHIFT,
          detailStart = LocalDateTime.parse("2022-04-06T10:00:00"),
          detailEnd = LocalDateTime.parse("2022-04-06T11:00:00"),
          activity = null,
        ),
      ),
    )

    restTestClient.get().uri("/user/details?from=2022-04-01&to=2022-04-30")
      .headers(entityBuilder.entityWithJwtAuthorisation(A_USER, PRISON_ROLE))
      .exchange()
      .expectStatus().isOk
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

    verify(csrRegionSelectorService).getStaffDetails(LocalDate.parse("2022-04-01"), LocalDate.parse("2022-04-30"), 3)
  }

  @Test
  fun `It calls csr api correctly`() {
    PrisonApiExtension.api.stubUsersMe()
    whenever(csrRegionSelectorService.getStaffDetails(any(), any(), eq(3))).thenReturn(emptyList())

    restTestClient.get()
      .uri("/user/details?from=2022-04-03&to=2022-04-06")
      .headers(entityBuilder.entityWithJwtAuthorisation(A_USER, PRISON_ROLE))
      .exchange()
      .expectStatus().isOk

    verify(csrRegionSelectorService).getStaffDetails(LocalDate.parse("2022-04-03"), LocalDate.parse("2022-04-06"), 3)
  }

  @Nested
  inner class Security {
    @Test
    fun `access forbidden when no role`() {
      restTestClient.get()
        .uri("/user/details?from=2022-04-06&to=2022-04-06")
        .headers(entityBuilder.entityWithJwtAuthorisation(A_USER, emptyList()))
        .exchange()
        .expectStatus().isForbidden
    }

    @Test
    fun `access forbidden with wrong role`() {
      restTestClient.get()
        .uri("/user/details?from=2022-04-06&to=2022-04-06")
        .headers(entityBuilder.entityWithJwtAuthorisation(A_USER, listOf("BANANAS")))
        .exchange()
        .expectStatus().isForbidden
    }

    @Test
    fun `access unauthorised with no auth token`() {
      restTestClient.get()
        .uri("/user/details?from=2022-04-06&to=2022-04-06")
        .exchange()
        .expectStatus().isUnauthorized
    }
  }

  companion object {

    private const val A_USER = "API_TEST_USER"
    private val PRISON_ROLE = listOf("ROLE_PRISON")
  }
}
