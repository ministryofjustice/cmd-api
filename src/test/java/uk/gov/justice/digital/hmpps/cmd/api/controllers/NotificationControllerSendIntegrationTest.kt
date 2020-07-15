package uk.gov.justice.digital.hmpps.cmd.api.controllers

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.jdbc.Sql
import org.springframework.test.context.jdbc.SqlConfig
import org.springframework.test.context.jdbc.SqlGroup
import org.springframework.test.context.junit.jupiter.SpringExtension

@ExtendWith(SpringExtension::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@SqlGroup(
        Sql(scripts = ["classpath:notification-send/before-test.sql"], config = SqlConfig(transactionMode = SqlConfig.TransactionMode.ISOLATED)),
        Sql(scripts = ["classpath:notification-send/after-test.sql"], config = SqlConfig(transactionMode = SqlConfig.TransactionMode.ISOLATED), executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
)
@ActiveProfiles(value = ["test"])
@DisplayName("Integration Tests for Sending Notifications Controller")
class NotificationControllerSendIntegrationTest(
        @Autowired val testRestTemplate: TestRestTemplate,
        @Autowired val entityBuilder: EntityWithJwtAuthorisationBuilder
) {

    /*
     * These are fairly limited in use because the output is Notify not this response
     * Have a look at NotificationDescriptionTest for good output examples.
     */
    @Test
    fun `It sends notifications`() {
        val response = getNotificationPreference(A_USER, "/notifications/send")
        with(response) {
            assertThat(statusCode).isEqualTo(HttpStatus.OK)
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
        private val NO_ROLES = listOf<String>()
    }
}
