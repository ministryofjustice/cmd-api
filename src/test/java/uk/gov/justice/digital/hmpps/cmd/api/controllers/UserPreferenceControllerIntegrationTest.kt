package uk.gov.justice.digital.hmpps.cmd.api.controllers

import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient
import org.springframework.http.HttpStatus
import org.springframework.test.context.jdbc.Sql
import org.springframework.test.context.jdbc.SqlConfig
import org.springframework.test.context.jdbc.SqlGroup
import org.springframework.test.web.servlet.client.RestTestClient
import uk.gov.justice.digital.hmpps.cmd.api.domain.CommunicationPreference
import uk.gov.justice.digital.hmpps.cmd.api.dto.UpdateNotificationDetailsRequest
import uk.gov.justice.digital.hmpps.cmd.api.dto.UpdateSnoozeUntilRequest
import java.time.LocalDate

@SqlGroup(
  Sql(scripts = ["classpath:preference/before-test.sql"], config = SqlConfig(transactionMode = SqlConfig.TransactionMode.ISOLATED)),
  Sql(scripts = ["classpath:preference/after-test.sql"], config = SqlConfig(transactionMode = SqlConfig.TransactionMode.ISOLATED), executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD),
)
@DisplayName("Integration Tests for userPreferencesController")
@AutoConfigureRestTestClient
class UserPreferenceControllerIntegrationTest(
  @Autowired val restTestClient: RestTestClient,
  @Autowired val entityBuilder: EntityWithJwtAuthorisationBuilder,
) : ResourceTest() {
  @Test
  fun `It returns an existing notification preference`() {
    getNotificationPreference(A_USER)
      .expectStatus().isEqualTo(HttpStatus.OK)
      .expectBody()
      .jsonPath("$.snoozeUntil").isEqualTo(LocalDate.now().plusDays(1).toString())
      .jsonPath("$.email").isEqualTo("me@test.com")
      .jsonPath("$.sms").isEqualTo("01234567890")
      .jsonPath("$.preference").isEqualTo("EMAIL")
  }

  @Test
  fun `It returns 404 when there isn't a notification preference `() {
    getNotificationPreference(A_USER_NO_PREFERENCE)
      .expectStatus().isEqualTo(HttpStatus.NOT_FOUND)
      .expectBody()
  }

  @Test
  fun `It updates a notification preference with valid values`() {
    putNotificationPreference(A_USER, "a@b.com", "07234567890", CommunicationPreference.EMAIL)
      .expectStatus().isEqualTo(HttpStatus.OK)
  }

  @Test
  fun `It rejects an invalid email`() {
    putNotificationPreference(A_USER, "a.com", "07234567890", CommunicationPreference.EMAIL)
      .expectStatus().isEqualTo(HttpStatus.BAD_REQUEST)
  }

  @Test
  fun `It accepts a blank email`() {
    putNotificationPreference(A_USER, "", "07234567890", CommunicationPreference.EMAIL)
      .expectStatus().isEqualTo(HttpStatus.OK)
  }

  @Test
  fun `It rejects an invalid sms`() {
    putNotificationPreference(A_USER, "a@b.com", "0a234567890", CommunicationPreference.EMAIL)
      .expectStatus().isEqualTo(HttpStatus.BAD_REQUEST)
  }

  @Test
  fun `It accepts a blank sms`() {
    putNotificationPreference(A_USER, "a@b.com", "", CommunicationPreference.EMAIL)
      .expectStatus().isEqualTo(HttpStatus.OK)
  }

  @Test
  fun `It accepts a null sms`() {
    putNotificationPreference(A_USER, "a@b.com", null, CommunicationPreference.EMAIL)
      .expectStatus().isEqualTo(HttpStatus.OK)
  }

  @Test
  fun `It updates an existing snooze preference`() {
    putSnoozeUntilPreference(A_USER, LocalDate.now())
      .expectStatus().isEqualTo(HttpStatus.OK)
  }

  @Test
  fun `It updates an existing snooze preference when the existing preference is in the past`() {
    putSnoozeUntilPreference(A_USER_OLD, LocalDate.now())
      .expectStatus().isEqualTo(HttpStatus.OK)
  }

  @Test
  fun `It creates a new preference when updating a snooze preference`() {
    putSnoozeUntilPreference(A_USER_NO_PREFERENCE, LocalDate.now())
      .expectStatus().isEqualTo(HttpStatus.OK)
  }

  @Test
  fun `It creates a new preference even if the snooze date is older than today`() {
    putSnoozeUntilPreference(A_USER_NO_PREFERENCE, LocalDate.now().minusDays(45))
      .expectStatus().isEqualTo(HttpStatus.OK)
  }

  fun putSnoozeUntilPreference(user: String, date: LocalDate): RestTestClient.ResponseSpec = restTestClient.put()
    .uri(PUT_SNOOZE_PREFERENCES_TEMPLATE)
    .headers(entityBuilder.entityWithJwtAuthorisation(user, PRISON_ROLE))
    .body(UpdateSnoozeUntilRequest(date))
    .exchange()

  fun putNotificationPreference(user: String, email: String, sms: String?, pref: CommunicationPreference): RestTestClient.ResponseSpec = restTestClient.put()
    .uri(PUT_NOTIFICATION_PREFERENCES_TEMPLATE)
    .headers(entityBuilder.entityWithJwtAuthorisation(user, PRISON_ROLE))
    .body(UpdateNotificationDetailsRequest(email, sms, pref))
    .exchange()

  fun getNotificationPreference(user: String): RestTestClient.ResponseSpec = restTestClient.get()
    .uri(NOTIFICATION_PREFERENCES_TEMPLATE)
    .headers(entityBuilder.entityWithJwtAuthorisation(user, PRISON_ROLE))
    .exchange()

  companion object {
    private const val NOTIFICATION_PREFERENCES_TEMPLATE = "/preferences/notifications"
    private const val PUT_SNOOZE_PREFERENCES_TEMPLATE = "/preferences/notifications/snooze"
    private const val PUT_NOTIFICATION_PREFERENCES_TEMPLATE = "/preferences/notifications/details"

    private const val A_USER = "API_TEST_USER"
    private const val A_USER_OLD = "API_TEST_USER_OLD_P"
    private const val A_USER_NO_PREFERENCE = "API_TEST_USER_NP"
    private val PRISON_ROLE = listOf("ROLE_PRISON")
  }
}
