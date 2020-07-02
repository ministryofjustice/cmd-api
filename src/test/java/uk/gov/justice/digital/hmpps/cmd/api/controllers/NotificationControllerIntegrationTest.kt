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
import java.time.LocalDate

@ExtendWith(SpringExtension::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@SqlGroup(
        Sql(scripts = ["classpath:notification/before-test.sql"], config = SqlConfig(transactionMode = SqlConfig.TransactionMode.ISOLATED)),
        Sql(scripts = ["classpath:notification/after-test.sql"], config = SqlConfig(transactionMode = SqlConfig.TransactionMode.ISOLATED), executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
)
@ActiveProfiles(value = ["test"])
@DisplayName("Integration Tests for Notification Controller")
class NotificationControllerIntegrationTest(
        @Autowired val testRestTemplate: TestRestTemplate,
        @Autowired val entityBuilder: EntityWithJwtAuthorisationBuilder
) {
    val jsonTester = BasicJsonTester(this.javaClass)

    @Test
    fun `It returns notifications`() {
        val response = getNotificationPreference(A_USER, "/notifications")
        with(response) {
            assertThat(statusCode).isEqualTo(HttpStatus.OK)
            // we use an insert of CURRENT_DATE in the test data. so there should be 4 returned
            assertThat(jsonTester.from(body)).extractingJsonPathValue("$").asList().hasSize(4)
        }
    }

    @Test
    fun `It returns notifications with an unread only preference of false`() {
        val response = getNotificationPreference(A_USER, "/notifications?unreadOnly=false")
        with(response) {
            assertThat(statusCode).isEqualTo(HttpStatus.OK)
            // we have one read and one not for each type so there should be 4 returned
            assertThat(jsonTester.from(body)).extractingJsonPathValue("$").asList().hasSize(4)
        }
    }

    @Test
    fun `It returns notifications with an unread preference of true`() {
        val response = getNotificationPreference(A_USER, "/notifications?unreadOnly=true")
        with(response) {
            assertThat(statusCode).isEqualTo(HttpStatus.OK)
            // we have one read and one not for each type so there should be 2 returned
            assertThat(jsonTester.from(body)).extractingJsonPathValue("$").asList().hasSize(2)
        }
    }

    @Test
    fun `It returns no notifications if there are none for a user`() {
        val response = getNotificationPreference(A_USER_NO_DATA, "/notifications")
        with(response) {
            assertThat(statusCode).isEqualTo(HttpStatus.OK)

            assertThat(jsonTester.from(body)).extractingJsonPathValue("$").asList().hasSize(0)
        }
    }

    @Test
    fun `It returns notifications between two dates with data for after the 'to'`() {
        val from = LocalDate.now()
        val to = LocalDate.now().plusDays(2)
        val response = getNotificationPreference(A_USER, "/notifications?from=$from&to=$to")
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
        val response = getNotificationPreference(A_USER, "/notifications?from=$from&to=$to")
        with(response) {
            assertThat(statusCode).isEqualTo(HttpStatus.OK)
            // we use an insert of CURRENT_DATE+7 for 2/4 in the test data. so there should be 2 returned
            assertThat(jsonTester.from(body)).extractingJsonPathValue("$").asList().hasSize(2)
        }
    }

    @Test
    fun `It returns notifications applying both dates and unread filters`() {
        val from = LocalDate.now().plusDays(2)
        val to = LocalDate.now().plusDays(8)
        val response = getNotificationPreference(A_USER, "/notifications?from=$from&to=$to&unreadOnly=true")
        with(response) {
            assertThat(statusCode).isEqualTo(HttpStatus.OK)
            // we use an insert of CURRENT_DATE+7 for 2/4 in the test data with one of those unread. so there should be 1 returned
            assertThat(jsonTester.from(body)).extractingJsonPathValue("$").asList().hasSize(1)
        }
    }

    @Test
    fun `It returns notifications and marks them as read`() {
        val responseOne = getNotificationPreference(A_USER, "/notifications")
        with(responseOne) {
            assertThat(statusCode).isEqualTo(HttpStatus.OK)
            // there are two unread notifications
            assertThat(jsonTester.from(body)).extractingJsonPathValue("$").asList().hasSize(4)
        }

        Thread.sleep(2000L)

        val responseTwo = getNotificationPreference(A_USER, "/notifications?unreadOnly=true")
        with(responseTwo) {
            assertThat(statusCode).isEqualTo(HttpStatus.OK)
            // there should now be 0 unread notifications
            assertThat(jsonTester.from(body)).extractingJsonPathValue("$").asList().hasSize(0)
        }
    }

    fun getNotificationPreference(user: String, url: String): ResponseEntity<String> =
            testRestTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entityBuilder.entityWithJwtAuthorisation(user, NO_ROLES),
                    String::class.java)

    companion object {

        private const val A_USER = "API_TEST_USER"
        private const val A_USER_NO_DATA = "API_TEST_USER_NO_DATA"
        private val NO_ROLES = listOf<String>()
    }
}