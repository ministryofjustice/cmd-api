package uk.gov.justice.digital.hmpps.cmd.api.scheduler

import org.assertj.core.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.json.BasicJsonTester
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.test.context.ActiveProfiles
import uk.gov.justice.digital.hmpps.cmd.api.controllers.EntityWithJwtAuthorisationBuilder
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
  @Autowired val testRestTemplate: TestRestTemplate,
  @Autowired val entityBuilder: EntityWithJwtAuthorisationBuilder,
  @Autowired val pollingScheduler: PollingScheduler,
  @Autowired val dryRunNotificationRepository: DryRunNotificationRepository,
) {
  val jsonTester = BasicJsonTester(this.javaClass)

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
        }
      ]
      """.trimIndent()
    )
    CsrApiExtension.api.stubGetUpdates(2, "[]")
    CsrApiExtension.api.stubGetUpdates(3, "[]")
    CsrApiExtension.api.stubGetUpdates(4, "[]")
    CsrApiExtension.api.stubGetUpdates(5, "[]")
    CsrApiExtension.api.stubGetUpdates(6, "[]")
    CsrApiExtension.api.stubDeleteProcessed(1, "[101]")

    pollingScheduler.pollNotifications()

    Assertions.assertThat(CsrApiExtension.api.putCountFor("/updates/1")).isEqualTo(1)
    Assertions.assertThat(CsrApiExtension.api.putCountFor("/updates/2")).isEqualTo(0)
    Assertions.assertThat(CsrApiExtension.api.putCountFor("/updates/3")).isEqualTo(0)
    Assertions.assertThat(CsrApiExtension.api.putCountFor("/updates/4")).isEqualTo(0)
    Assertions.assertThat(CsrApiExtension.api.putCountFor("/updates/5")).isEqualTo(0)
    Assertions.assertThat(CsrApiExtension.api.putCountFor("/updates/6")).isEqualTo(0)

    val notification = dryRunNotificationRepository.findAll().first()
    Assertions.assertThat(notification.quantumId).isEqualTo(A_USER)
    Assertions.assertThat(notification.processed).isTrue()
  }

  companion object {

    private const val A_USER = "API_TEST_USER"
    private const val A_USER_NO_DATA = "API_TEST_USER_NO_DATA"
    private val NO_ROLES = listOf<String>()
  }
}
