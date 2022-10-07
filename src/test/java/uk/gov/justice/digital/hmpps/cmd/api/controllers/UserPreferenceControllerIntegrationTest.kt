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
import org.springframework.http.ResponseEntity
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.jdbc.Sql
import org.springframework.test.context.jdbc.SqlConfig
import org.springframework.test.context.jdbc.SqlGroup
import org.springframework.test.context.junit.jupiter.SpringExtension
import uk.gov.justice.digital.hmpps.cmd.api.domain.CommunicationPreference
import uk.gov.justice.digital.hmpps.cmd.api.dto.UpdateNotificationDetailsRequest
import uk.gov.justice.digital.hmpps.cmd.api.dto.UpdateSnoozeUntilRequest
import java.time.LocalDate

@ExtendWith(SpringExtension::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@SqlGroup(
  Sql(scripts = ["classpath:preference/before-test.sql"], config = SqlConfig(transactionMode = SqlConfig.TransactionMode.ISOLATED)),
  Sql(scripts = ["classpath:preference/after-test.sql"], config = SqlConfig(transactionMode = SqlConfig.TransactionMode.ISOLATED), executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
)
@ActiveProfiles(value = ["test"])
@DisplayName("Integration Tests for userPreferencesController")
class UserPreferenceControllerIntegrationTest(
  @Autowired val testRestTemplate: TestRestTemplate,
  @Autowired val entityBuilder: EntityWithJwtAuthorisationBuilder
) {
  val jsonTester = BasicJsonTester(this.javaClass)

  @Test
  fun `It returns an existing notification preference`() {
    val response = getNotificationPreference(A_USER)
    with(response) {
      assertThat(statusCode).isEqualTo(HttpStatus.OK)
      assertThat(jsonTester.from(body)).extractingJsonPathStringValue("$.snoozeUntil").isEqualTo(LocalDate.now().plusDays(1).toString())
      assertThat(jsonTester.from(body)).extractingJsonPathStringValue("$.email").isEqualTo("me@test.com")
      assertThat(jsonTester.from(body)).extractingJsonPathStringValue("$.sms").isEqualTo("01234567890")
      assertThat(jsonTester.from(body)).extractingJsonPathStringValue("$.preference").isEqualTo("EMAIL")
    }
  }



  @Test
  fun `It returns 404 when there isn't a notification preference `() {
    val response = getNotificationPreference2(A_USER_NO_PREFERENCE)
    with(response) {
      assertThat(statusCode).isEqualTo(HttpStatus.NOT_FOUND)
    }
  }

  @Test
  fun `It updates a notification preference with valid values`() {
    val response = putNotificationPreference(A_USER, "a@b.com", "07234567890", CommunicationPreference.EMAIL)
    with(response) {
      assertThat(statusCode).isEqualTo(HttpStatus.OK)
    }
  }

  @Test
  fun `It rejects an invalid email`() {
    val response = putNotificationPreference(A_USER, "a.com", "07234567890", CommunicationPreference.EMAIL)
    with(response) {
      assertThat(statusCode).isEqualTo(HttpStatus.BAD_REQUEST)
    }
  }

  @Test
  fun `It accepts a blank email`() {
    val response = putNotificationPreference(A_USER, "", "07234567890", CommunicationPreference.EMAIL)
    with(response) {
      assertThat(statusCode).isEqualTo(HttpStatus.OK)
    }
  }

  @Test
  fun `It rejects an invalid sms`() {
    val response = putNotificationPreference(A_USER, "a@b.com", "0a234567890", CommunicationPreference.EMAIL)
    with(response) {
      assertThat(statusCode).isEqualTo(HttpStatus.BAD_REQUEST)
    }
  }

  @Test
  fun `It accepts a blank sms`() {
    val response = putNotificationPreference(A_USER, "a@b.com", "", CommunicationPreference.EMAIL)
    with(response) {
      assertThat(statusCode).isEqualTo(HttpStatus.OK)
    }
  }

  @Test
  fun `It accepts a null sms`() {
    val response = putNotificationPreference(A_USER, "a@b.com", null, CommunicationPreference.EMAIL)
    with(response) {
      assertThat(statusCode).isEqualTo(HttpStatus.OK)
    }
  }

  @Test
  fun `It updates an existing snooze preference`() {
    val response = putSnoozeUntilPreference(A_USER, LocalDate.now())
    with(response) {
      assertThat(statusCode).isEqualTo(HttpStatus.OK)
    }
  }

  @Test
  fun `It updates an existing snooze preference when the existing preference is in the past`() {
    val response = putSnoozeUntilPreference(A_USER_OLD, LocalDate.now())
    with(response) {
      assertThat(statusCode).isEqualTo(HttpStatus.OK)
    }
  }

  @Test
  fun `It creates a new preference when updating a snooze preference`() {
    val response = putSnoozeUntilPreference(A_USER_NO_PREFERENCE, LocalDate.now())
    with(response) {
      assertThat(statusCode).isEqualTo(HttpStatus.OK)
    }
  }

  @Test
  fun `It creates a new preference even if the snooze date is older than today`() {
    val response = putSnoozeUntilPreference(A_USER_NO_PREFERENCE, LocalDate.now().minusDays(45))
    with(response) {
      assertThat(statusCode).isEqualTo(HttpStatus.OK)
    }
  }

  fun putSnoozeUntilPreference(user: String, date: LocalDate): ResponseEntity<Void> =
    testRestTemplate.exchange(
      PUT_SNOOZE_PREFERENCES_TEMPLATE,
      HttpMethod.PUT,
      entityBuilder.entityWithJwtAuthorisation(user, NO_ROLES, UpdateSnoozeUntilRequest(date)),
      Void::class.java
    )

  fun putNotificationPreference(user: String, email: String, sms: String?, pref: CommunicationPreference): ResponseEntity<Void> =
    testRestTemplate.exchange(
      PUT_NOTIFICATION_PREFERENCES_TEMPLATE,
      HttpMethod.PUT,
      entityBuilder.entityWithJwtAuthorisation(user, NO_ROLES, UpdateNotificationDetailsRequest(email, sms, pref)),
      Void::class.java
    )

  fun getNotificationPreference(user: String): ResponseEntity<String> =
    testRestTemplate.exchange(
      NOTIFICATION_PREFERENCES_TEMPLATE,
      HttpMethod.GET,
      entityBuilder.entityWithJwtAuthorisation(user, NO_ROLES),
      String::class.java
    )

  fun getNotificationPreference2(user: String): ResponseEntity<String> =
    testRestTemplate.exchange(
      NOTIFICATION_PREFERENCES_TEMPLATE2,
      HttpMethod.GET,
      entityBuilder.entityWithJwtAuthorisation(user, NO_ROLES),
      String::class.java
    )

  companion object {
    private const val NOTIFICATION_PREFERENCES_TEMPLATE = "/preferences/notifications"
    private const val NOTIFICATION_PREFERENCES_TEMPLATE2 = "/preferences/notifications2"
    private const val PUT_SNOOZE_PREFERENCES_TEMPLATE = "/preferences/notifications/snooze"
    private const val PUT_NOTIFICATION_PREFERENCES_TEMPLATE = "/preferences/notifications/details"

    private const val A_USER = "API_TEST_USER"
    private const val A_USER_OLD = "API_TEST_USER_OLD_P"
    private const val A_USER_NO_PREFERENCE = "API_TEST_USER_NP"
    private val NO_ROLES = listOf<String>()
  }
}
