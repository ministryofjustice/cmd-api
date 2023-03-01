package uk.gov.justice.digital.hmpps.cmd.api.controllers

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
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
import uk.gov.justice.digital.hmpps.cmd.api.dto.NotificationDto
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@ExtendWith(SpringExtension::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@SqlGroup(
  Sql(scripts = ["classpath:notification/before-test.sql"], config = SqlConfig(transactionMode = SqlConfig.TransactionMode.ISOLATED)),
  Sql(scripts = ["classpath:notification/after-test.sql"], config = SqlConfig(transactionMode = SqlConfig.TransactionMode.ISOLATED), executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD),
)
@ActiveProfiles(value = ["test"])
@DisplayName("Integration Tests for Notification Controller")
class NotificationControllerGetIntegrationTest(
  @Autowired val testRestTemplate: TestRestTemplate,
  @Autowired val entityBuilder: EntityWithJwtAuthorisationBuilder,
  @Autowired val objectMapper: ObjectMapper,
) {
  val jsonTester = BasicJsonTester(this.javaClass)

  @Test
  fun `It returns notifications`() {
    val response = getNotifications(A_USER, "/notifications")
    with(response) {
      assertThat(statusCode).isEqualTo(HttpStatus.OK)
      // we use an insert of CURRENT_DATE in the test data. so there should be 4 returned
      val content = jsonTester.from(body)
      val date = LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE d MMMM"))
      assertThat(content).extractingJsonPathValue("$").asList().hasSize(4)
      assertThat(content).extractingJsonPathValue("$[0].description").isEqualTo("Your shift on $date has been added.")
    }
  }

  @Test
  fun `It returns notifications deep inspection Email`() {
    val response = getNotifications(A_USER, "/notifications")
    with(response) {
      assertThat(statusCode).isEqualTo(HttpStatus.OK)
      // we use an insert of CURRENT_DATE in the test data. so there should be 4 returned
      val notificationList: List<NotificationDto> = objectMapper.readValue(body, object : TypeReference<List<NotificationDto>>() {})
      assertThat(notificationList).hasSize(4)

      val notification = notificationList.findLast { it.description.contains("has been added.") }
      assertThat(notification).isNotNull
      assertThat(notification?.processed).isFalse()
      assertThat(notification?.shiftModified).isEqualTo(LocalDate.now().atStartOfDay())
    }
  }

  @Test
  fun `It returns notifications with an unread only preference of false`() {
    val response = getNotifications(A_USER, "/notifications?unprocessedOnly=false")
    with(response) {
      assertThat(statusCode).isEqualTo(HttpStatus.OK)
      // we have one read and one not for each type so there should be 4 returned
      assertThat(jsonTester.from(body)).extractingJsonPathValue("$").asList().hasSize(4)
    }
  }

  @Test
  fun `It returns notifications with an unread preference of true`() {
    val response = getNotifications(A_USER, "/notifications?unprocessedOnly=true")
    with(response) {
      assertThat(statusCode).isEqualTo(HttpStatus.OK)
      // we have one read and one not for each type so there should be 2 returned
      assertThat(jsonTester.from(body)).extractingJsonPathValue("$").asList().hasSize(2)
    }
  }

  @Test
  fun `It returns no notifications if there are none for a user`() {
    val response = getNotifications(A_USER_NO_DATA, "/notifications")
    with(response) {
      assertThat(statusCode).isEqualTo(HttpStatus.OK)

      assertThat(jsonTester.from(body)).extractingJsonPathValue("$").asList().hasSize(0)
    }
  }

  @Test
  fun `It returns notifications between two dates with data for after the 'to'`() {
    val from = LocalDate.now()
    val to = LocalDate.now().plusDays(2)
    val response = getNotifications(A_USER, "/notifications?from=$from&to=$to")
    with(response) {
      assertThat(statusCode).isEqualTo(HttpStatus.OK)
      // we use an insert of CURRENT_DATE+7 for 2/4 in the test data. so there should be 2 returned
      assertThat(jsonTester.from(body)).extractingJsonPathValue("$").asList().hasSize(2)
    }
  }

  @Test
  fun `It returns notifications between two dates with data for before the 'from'`() {
    val from = LocalDate.now().plusDays(2)
    val to = LocalDate.now().plusDays(8)
    val response = getNotifications(A_USER, "/notifications?from=$from&to=$to")
    with(response) {
      assertThat(statusCode).isEqualTo(HttpStatus.OK)
      // we use an insert of CURRENT_DATE+7 for 2/4 in the test data. so there should be 2 returned
      assertThat(jsonTester.from(body)).extractingJsonPathValue("$").asList().hasSize(2)
    }
  }

  @Test
  fun `It returns empty when 'from' is after 'to'`() {
    val from = LocalDate.now().plusDays(2)
    val to = LocalDate.now().plusDays(8)
    val response = getNotifications(A_USER, "/notifications?from=$to&to=$from")
    with(response) {
      assertThat(statusCode).isEqualTo(HttpStatus.OK)
      // we use an insert of CURRENT_DATE+7 for 2/4 in the test data. so there should be 2 returned
      assertThat(jsonTester.from(body)).extractingJsonPathValue("$").asList().hasSize(0)
    }
  }

  @Test
  fun `It returns notifications applying both dates and unread filters`() {
    val from = LocalDate.now().plusDays(2)
    val to = LocalDate.now().plusDays(8)
    val response = getNotifications(A_USER, "/notifications?from=$from&to=$to&unprocessedOnly=true")
    with(response) {
      assertThat(statusCode).isEqualTo(HttpStatus.OK)
      // we use an insert of CURRENT_DATE+7 for 2/4 in the test data with one of those unread. so there should be 1 returned
      assertThat(jsonTester.from(body)).extractingJsonPathValue("$").asList().hasSize(1)
    }
  }

  @Test
  fun `It returns notifications and marks them as read`() {
    val responseOne = getNotifications(A_USER, "/notifications")
    with(responseOne) {
      assertThat(statusCode).isEqualTo(HttpStatus.OK)
      // there are two unprocessed notifications and two processed
      assertThat(jsonTester.from(body)).extractingJsonPathValue("$").asList().hasSize(4)
    }

    val responseTwo = getNotifications(A_USER, "/notifications?unprocessedOnly=true")
    with(responseTwo) {
      assertThat(statusCode).isEqualTo(HttpStatus.OK)
      // there should now be 0 unprocessed notifications
      assertThat(jsonTester.from(body)).extractingJsonPathValue("$").asList().hasSize(0)
    }
  }

  @Test
  fun `It returns notifications and doesn't mark them as read`() {
    val responseOne = getNotifications(A_USER, "/notifications?processOnRead=false")
    with(responseOne) {
      assertThat(statusCode).isEqualTo(HttpStatus.OK)
      // there are two unprocessed notifications and two processed
      assertThat(jsonTester.from(body)).extractingJsonPathValue("$").asList().hasSize(4)
    }

    val responseTwo = getNotifications(A_USER, "/notifications")
    with(responseTwo) {
      assertThat(statusCode).isEqualTo(HttpStatus.OK)
      // there should now be 0 unprocessed notifications
      assertThat(jsonTester.from(body)).extractingJsonPathValue("$").asList().hasSize(4)
    }
  }

  fun getNotifications(user: String, url: String): ResponseEntity<String> =
    testRestTemplate.exchange(
      url,
      HttpMethod.GET,
      entityBuilder.entityWithJwtAuthorisation(user, NO_ROLES),
      String::class.java,
    )

  companion object {

    private const val A_USER = "API_TEST_USER"
    private const val A_USER_NO_DATA = "API_TEST_USER_NO_DATA"
    private val NO_ROLES = listOf<String>()
  }
}
