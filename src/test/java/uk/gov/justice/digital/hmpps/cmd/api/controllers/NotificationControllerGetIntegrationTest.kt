package uk.gov.justice.digital.hmpps.cmd.api.controllers

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient
import org.springframework.http.HttpStatus
import org.springframework.test.context.jdbc.Sql
import org.springframework.test.context.jdbc.SqlConfig
import org.springframework.test.context.jdbc.SqlGroup
import org.springframework.test.web.servlet.client.RestTestClient
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@SqlGroup(
  Sql(scripts = ["classpath:notification/before-test.sql"], config = SqlConfig(transactionMode = SqlConfig.TransactionMode.ISOLATED)),
  Sql(scripts = ["classpath:notification/after-test.sql"], config = SqlConfig(transactionMode = SqlConfig.TransactionMode.ISOLATED), executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD),
)
@DisplayName("Integration Tests for Notification Controller")
@AutoConfigureRestTestClient
class NotificationControllerGetIntegrationTest(
  @Autowired val restTestClient: RestTestClient,
  @Autowired val entityBuilder: EntityWithJwtAuthorisationBuilder,
) : ResourceTest() {
  @Test
  fun `It returns notifications`() {
    val date = LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE d MMMM"))
    getNotifications(A_USER, "/notifications")
      .expectStatus().isEqualTo(HttpStatus.OK)
      .expectBody()
      // we use an insert of CURRENT_DATE in the test data. so there should be 4 returned
      .jsonPath("$.length()").isEqualTo(4)
      .jsonPath("$[0].description").isEqualTo("Your shift on $date has been added.")
  }

  @Test
  fun `It returns notifications deep inspection Email`() {
    getNotifications(A_USER, "/notifications")
      .expectStatus().isEqualTo(HttpStatus.OK)
      .expectBody()
      // we use an insert of CURRENT_DATE in the test data. so there should be 4 returned
      .jsonPath("$.length()").isEqualTo(4)
      .jsonPath("$").value<List<LinkedHashMap<String, Any>>> { notificationList ->
        assertThat(notificationList).hasSize(4)

        val notification = notificationList.findLast { it["description"].toString().contains("has been added.") }
        assertThat(notification).isNotNull
        assertThat(notification!!["processed"]).isEqualTo(false)
        assertThat(notification["shiftModified"]).isEqualTo("${LocalDate.now().atStartOfDay()}:00")
      }
  }

  @Test
  fun `It returns notifications with an unread only preference of false`() {
    getNotifications(A_USER, "/notifications?unprocessedOnly=false")
      .expectStatus().isEqualTo(HttpStatus.OK)
      .expectBody()
      // we have one read and one not for each type so there should be 4 returned
      .jsonPath("$.length()").isEqualTo(4)
  }

  @Test
  fun `It returns notifications with an unread preference of true`() {
    getNotifications(A_USER, "/notifications?unprocessedOnly=true")
      .expectStatus().isEqualTo(HttpStatus.OK)
      .expectBody()
      // we have one read and one not for each type so there should be 2 returned
      .jsonPath("$.length()").isEqualTo(2)
  }

  @Test
  fun `It returns no notifications if there are none for a user`() {
    getNotifications(A_USER_NO_DATA, "/notifications")
      .expectStatus().isEqualTo(HttpStatus.OK)
      .expectBody()
      .jsonPath("$.length()").isEqualTo(0)
  }

  @Test
  fun `It returns notifications between two dates with data for after the 'to'`() {
    val from = LocalDate.now()
    val to = LocalDate.now().plusDays(2)
    getNotifications(A_USER, "/notifications?from=$from&to=$to")
      .expectStatus().isEqualTo(HttpStatus.OK)
      .expectBody()
      // we use an insert of CURRENT_DATE+7 for 2/4 in the test data. so there should be 2 returned
      .jsonPath("$.length()").isEqualTo(2)
  }

  @Test
  fun `It returns notifications between two dates with data for before the 'from'`() {
    val from = LocalDate.now().plusDays(2)
    val to = LocalDate.now().plusDays(8)
    getNotifications(A_USER, "/notifications?from=$from&to=$to")
      .expectStatus().isEqualTo(HttpStatus.OK)
      .expectBody()
      // we use an insert of CURRENT_DATE+7 for 2/4 in the test data. so there should be 2 returned
      .jsonPath("$.length()").isEqualTo(2)
  }

  @Test
  fun `It returns empty when 'from' is after 'to'`() {
    val from = LocalDate.now().plusDays(2)
    val to = LocalDate.now().plusDays(8)
    getNotifications(A_USER, "/notifications?from=$to&to=$from")
      .expectStatus().isEqualTo(HttpStatus.OK)
      .expectBody()
      // we use an insert of CURRENT_DATE+7 for 2/4 in the test data. so there should be 2 returned
      .jsonPath("$.length()").isEqualTo(0)
  }

  @Test
  fun `It returns notifications applying both dates and unread filters`() {
    val from = LocalDate.now().plusDays(2)
    val to = LocalDate.now().plusDays(8)
    getNotifications(A_USER, "/notifications?from=$from&to=$to&unprocessedOnly=true")
      .expectStatus().isEqualTo(HttpStatus.OK)
      .expectBody()
      // we use an insert of CURRENT_DATE+7 for 2/4 in the test data with one of those unread. so there should be 1 returned
      .jsonPath("$.length()").isEqualTo(1)
  }

  @Test
  fun `It returns notifications and marks them as read`() {
    getNotifications(A_USER, "/notifications")
      .expectStatus().isEqualTo(HttpStatus.OK)
      .expectBody()
      // there are two unprocessed notifications and two processed
      .jsonPath("$.length()").isEqualTo(4)

    getNotifications(A_USER, "/notifications?unprocessedOnly=true")
      .expectStatus().isEqualTo(HttpStatus.OK)
      .expectBody()
      // there should now be 0 unprocessed notifications
      .jsonPath("$.length()").isEqualTo(0)
  }

  @Test
  fun `It returns notifications and doesn't mark them as read`() {
    getNotifications(A_USER, "/notifications?processOnRead=false")
      .expectStatus().isEqualTo(HttpStatus.OK)
      .expectBody()
      // there are two unprocessed notifications and two processed
      .jsonPath("$.length()").isEqualTo(4)

    getNotifications(A_USER, "/notifications")
      .expectStatus().isEqualTo(HttpStatus.OK)
      .expectBody()
      // there should now be 0 unprocessed notifications
      .jsonPath("$.length()").isEqualTo(4)
  }

  fun getNotifications(user: String, url: String): RestTestClient.ResponseSpec = restTestClient.get()
    .uri(url)
    .headers(entityBuilder.entityWithJwtAuthorisation(user, PRISON_ROLE))
    .exchange()

  companion object {

    private const val A_USER = "API_TEST_USER"
    private const val A_USER_NO_DATA = "API_TEST_USER_NO_DATA"
    private val PRISON_ROLE = listOf("ROLE_PRISON")
  }
}
