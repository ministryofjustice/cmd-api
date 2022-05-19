package uk.gov.justice.digital.hmpps.cmd.api.scheduler

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import uk.gov.justice.digital.hmpps.cmd.api.repository.DryRunNotificationRepository
import uk.gov.justice.digital.hmpps.cmd.api.uk.gov.justice.digital.hmpps.cmd.api.scheduler.PollingScheduler
import uk.gov.justice.digital.hmpps.prisonertonomisupdate.wiremock.CsrApiExtension
import uk.gov.justice.digital.hmpps.prisonertonomisupdate.wiremock.HmppsAuthApiExtension
import uk.gov.justice.digital.hmpps.prisonertonomisupdate.wiremock.PrisonApiExtension

@ExtendWith(PrisonApiExtension::class, CsrApiExtension::class, HmppsAuthApiExtension::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles(value = ["test"])
@DisplayName("Integration Tests for Shift Controller")
class PollingSchedulerIntegrationTest(
  @Autowired val pollingScheduler: PollingScheduler,
  @Autowired val dryRunNotificationRepository: DryRunNotificationRepository,
) {

  @BeforeEach
  fun init() {
    dryRunNotificationRepository.deleteAll()
  }

  @Test
  fun `updates are processed`() {
    CsrApiExtension.api.stubGetUpdates(
      1,
      """
      [
        {
        "id" : "101",
        "quantumId" : "$A_USER",
        "shiftModified" : "2022-03-25T15:00:00",
        "shiftType" : "SHIFT",
        "detailStart" : "2022-03-31T10:00:00",
        "detailEnd" : "2022-03-31T11:00:00",
        "activity" : "CCTV monitoring",
        "actionType" : "ADD"
        },
        {
        "id" : "102",
        "shiftType" : "SHIFT",
        "detailStart" : "2022-03-31T10:00:00",
        "detailEnd" : "2022-03-31T11:00:00",
        "activity" : "CCTV monitoring",
        "actionType" : "ADD"
        }
      ]
      """.trimIndent()
    )
    CsrApiExtension.api.stubGetUpdates(2, "[]")
    CsrApiExtension.api.stubGetUpdates(3, "[]")
    CsrApiExtension.api.stubGetUpdates(4, "[]")
    CsrApiExtension.api.stubGetUpdates(5, "[]")
    CsrApiExtension.api.stubGetUpdates(6, "[]")
    CsrApiExtension.api.stubDeleteProcessed(1, "[101,102]")

    pollingScheduler.pollNotifications()

    assertThat(CsrApiExtension.api.putCountFor("/updates/1")).isEqualTo(1)
    assertThat(CsrApiExtension.api.putCountFor("/updates/2")).isEqualTo(0)
    assertThat(CsrApiExtension.api.putCountFor("/updates/3")).isEqualTo(0)
    assertThat(CsrApiExtension.api.putCountFor("/updates/4")).isEqualTo(0)
    assertThat(CsrApiExtension.api.putCountFor("/updates/5")).isEqualTo(0)
    assertThat(CsrApiExtension.api.putCountFor("/updates/6")).isEqualTo(0)

    val saved = dryRunNotificationRepository.findAll()
    val notification = saved.first()
    assertThat(notification.quantumId).isEqualTo(A_USER)
    assertThat(notification.processed).isTrue
    assertThat(saved).hasSize(1)
  }

  companion object {
    private const val A_USER = "API_TEST_USER"
  }
}
