package uk.gov.justice.digital.hmpps.cmd.api.scheduler

import com.github.tomakehurst.wiremock.client.WireMock.equalTo
import com.github.tomakehurst.wiremock.client.WireMock.putRequestedFor
import com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import uk.gov.justice.digital.hmpps.cmd.api.domain.DetailModificationType
import uk.gov.justice.digital.hmpps.cmd.api.domain.ShiftType
import uk.gov.justice.digital.hmpps.cmd.api.model.DryRunNotification
import uk.gov.justice.digital.hmpps.cmd.api.repository.DryRunNotificationRepository
import uk.gov.justice.digital.hmpps.prisonertonomisupdate.wiremock.CsrApiExtension
import uk.gov.justice.digital.hmpps.prisonertonomisupdate.wiremock.HmppsAuthApiExtension
import uk.gov.justice.digital.hmpps.prisonertonomisupdate.wiremock.PrisonApiExtension
import java.time.Clock
import java.time.LocalDateTime

@ExtendWith(PrisonApiExtension::class, CsrApiExtension::class, HmppsAuthApiExtension::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = ["csr.timeout=1s"])
@ActiveProfiles(value = ["test"])
@DisplayName("Integration Tests for Shift Controller")
class PollingSchedulerIntegrationTest(
  @Autowired val pollingScheduler: PollingScheduler,
  @Autowired val dryRunNotificationRepository: DryRunNotificationRepository,
  @Autowired private val clock: Clock,
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
        "id" : "103",
        "shiftModified" : "2022-03-25T15:30:00",
        "shiftType" : "SHIFT",
        "detailStart" : "2022-03-31T10:00:00",
        "detailEnd" : "2022-03-31T11:00:00",
        "activity" : "CCTV monitoring",
        "actionType" : "ADD"
        }
      ]
      """.trimIndent()
    )
    CsrApiExtension.api.stubGetUpdates(
      2,
      """[{
        "id" : "102",
        "quantumId" : "other-user",
        "shiftModified" : "2022-03-25T15:40:00",
        "shiftType" : "SHIFT",
        "detailStart" : "2022-03-31T10:00:00",
        "detailEnd" : "2022-03-31T11:00:00",
        "activity" : "CCTV monitoring",
        "actionType" : "ADD"
        }]"""
    )
    CsrApiExtension.api.stubGetUpdates(3, "[]")
    CsrApiExtension.api.stubGetUpdates(4, "[]")
    CsrApiExtension.api.stubGetUpdates(5, "[]")
    CsrApiExtension.api.stubGetUpdates(6, "[]")
    CsrApiExtension.api.stubDeleteProcessed(1, "[101,103]")
    CsrApiExtension.api.stubDeleteProcessed(2, "[102]")

    pollingScheduler.pollNotifications()

    assertThat(CsrApiExtension.api.putCountFor("/updates/1")).isEqualTo(1)
    assertThat(CsrApiExtension.api.putCountFor("/updates/2")).isEqualTo(1)
    assertThat(CsrApiExtension.api.putCountFor("/updates/3")).isEqualTo(0)
    assertThat(CsrApiExtension.api.putCountFor("/updates/4")).isEqualTo(0)
    assertThat(CsrApiExtension.api.putCountFor("/updates/5")).isEqualTo(0)
    assertThat(CsrApiExtension.api.putCountFor("/updates/6")).isEqualTo(0)

    val saved = dryRunNotificationRepository.findAll()
    assertThat(saved).asList().containsExactly(
      DryRunNotification(
        id = saved.first().id, // generated
        quantumId = A_USER,
        shiftModified = LocalDateTime.parse("2022-03-25T15:00:00"),
        detailStart = LocalDateTime.parse("2022-03-31T10:00:00"),
        detailEnd = LocalDateTime.parse("2022-03-31T11:00:00"),
        activity = "CCTV monitoring",
        actionType = DetailModificationType.ADD,
        parentType = ShiftType.SHIFT,
        processed = true,
      ),
      DryRunNotification(
        id = saved.last().id,
        quantumId = "other-user",
        shiftModified = LocalDateTime.parse("2022-03-25T15:40:00"),
        detailStart = LocalDateTime.parse("2022-03-31T10:00:00"),
        detailEnd = LocalDateTime.parse("2022-03-31T11:00:00"),
        activity = "CCTV monitoring",
        actionType = DetailModificationType.ADD,
        parentType = ShiftType.SHIFT,
        processed = true,
      ),
    )
  }

  @Test
  fun `tidy job functions`() {

    dryRunNotificationRepository.save(
      DryRunNotification(
        quantumId = "user1",
        shiftModified = LocalDateTime.now().minusMonths(4),
        detailStart = LocalDateTime.parse("2022-03-31T10:00:00"),
        detailEnd = LocalDateTime.parse("2022-03-31T11:00:00"),
        activity = "CCTV monitoring",
        actionType = DetailModificationType.ADD,
        parentType = ShiftType.SHIFT,
        processed = true,
      )
    )
    dryRunNotificationRepository.save(
      DryRunNotification(
        quantumId = "user2",
        shiftModified = LocalDateTime.now().minusMonths(2),
        detailStart = LocalDateTime.parse("2022-03-31T10:00:00"),
        detailEnd = LocalDateTime.parse("2022-03-31T11:00:00"),
        activity = "CCTV monitoring",
        actionType = DetailModificationType.ADD,
        parentType = ShiftType.SHIFT,
        processed = true,
      )
    )

    pollingScheduler.tidyNotifications()

    val all = dryRunNotificationRepository.findAll()
    assertThat(all).asList().hasSize(1)
    assertThat(all.first().quantumId).isEqualTo("user2")
  }

  @Test
  fun `handles connection reset by peer errors`() {
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
        "id" : "103",
        "shiftModified" : "2022-03-25T15:30:00",
        "shiftType" : "SHIFT",
        "detailStart" : "2022-03-31T10:00:00",
        "detailEnd" : "2022-03-31T11:00:00",
        "activity" : "CCTV monitoring",
        "actionType" : "ADD"
        }
      ]
      """.trimIndent()
    )
    CsrApiExtension.api.stubConnectionResetByPeer(2)
    CsrApiExtension.api.stubGetUpdates(3, """[{
        "id" : "102",
        "quantumId" : "other-user",
        "shiftModified" : "2022-03-25T15:40:00",
        "shiftType" : "SHIFT",
        "detailStart" : "2022-03-31T10:00:00",
        "detailEnd" : "2022-03-31T11:00:00",
        "activity" : "CCTV monitoring",
        "actionType" : "ADD"
        }]""")
    CsrApiExtension.api.stubGetUpdates(4, "[]")
    CsrApiExtension.api.stubGetUpdates(5, "[]")
    CsrApiExtension.api.stubGetUpdates(6, "[]")
    CsrApiExtension.api.stubDeleteProcessed(1, "[101,103]")
    CsrApiExtension.api.stubDeleteProcessed(3, "[102]")

    pollingScheduler.pollNotifications()

    assertThat(CsrApiExtension.api.putCountFor("/updates/1")).isEqualTo(1)
    assertThat(CsrApiExtension.api.putCountFor("/updates/2")).isEqualTo(0)
    assertThat(CsrApiExtension.api.putCountFor("/updates/3")).isEqualTo(1)
    assertThat(CsrApiExtension.api.putCountFor("/updates/4")).isEqualTo(0)
    assertThat(CsrApiExtension.api.putCountFor("/updates/5")).isEqualTo(0)
    assertThat(CsrApiExtension.api.putCountFor("/updates/6")).isEqualTo(0)
    CsrApiExtension.api.verify(putRequestedFor(urlEqualTo("/updates/1")).withRequestBody(equalTo("[101,103]")))
    CsrApiExtension.api.verify(putRequestedFor(urlEqualTo("/updates/3")).withRequestBody(equalTo("[102]")))


    val saved = dryRunNotificationRepository.findAll()
    assertThat(saved).asList().containsExactly(
      DryRunNotification(
        id = saved.first().id, // generated
        quantumId = A_USER,
        shiftModified = LocalDateTime.parse("2022-03-25T15:00:00"),
        detailStart = LocalDateTime.parse("2022-03-31T10:00:00"),
        detailEnd = LocalDateTime.parse("2022-03-31T11:00:00"),
        activity = "CCTV monitoring",
        actionType = DetailModificationType.ADD,
        parentType = ShiftType.SHIFT,
        processed = true,
      ),
      DryRunNotification(
        id = saved.last().id,
        quantumId = "other-user",
        shiftModified = LocalDateTime.parse("2022-03-25T15:40:00"),
        detailStart = LocalDateTime.parse("2022-03-31T10:00:00"),
        detailEnd = LocalDateTime.parse("2022-03-31T11:00:00"),
        activity = "CCTV monitoring",
        actionType = DetailModificationType.ADD,
        parentType = ShiftType.SHIFT,
        processed = true,
      ),
    )
  }

  companion object {
    private const val A_USER = "API_TEST_USER"
  }
}
