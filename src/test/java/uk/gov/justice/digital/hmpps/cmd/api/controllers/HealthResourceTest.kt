package uk.gov.justice.digital.hmpps.cmd.api.controllers

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.json.BasicJsonTester
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.http.HttpStatus
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension

@ExtendWith(SpringExtension::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles(value = ["test"])
class HealthResourceTest(@Autowired val testRestTemplate: TestRestTemplate) {

  val jsonTester = BasicJsonTester(this.javaClass)

  @Test
  fun `Ping test`() {
    val response = testRestTemplate.getForEntity(PING_URL, String::class.java)
    assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
    assertThat(jsonTester.from(response.body)).extractingJsonPathStringValue("$.status").isEqualTo("UP")
  }

  @Test
  fun `Health test`() {
    val response = testRestTemplate.getForEntity(HEALTH_URL, String::class.java)
    assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
    assertThat(jsonTester.from(response.body)).extractingJsonPathStringValue("$.status").isEqualTo("UP")
    assertThat(jsonTester.from(response.body)).extractingJsonPathStringValue("$.components.ping.status").isEqualTo("UP")
    // TODO: db status
    assertThat(jsonTester.from(response.body)).extractingJsonPathStringValue("$.components.diskSpace.status").isEqualTo("UP")
  }

  @Test
  fun `Info test`() {
    val response = testRestTemplate.getForEntity(INFO_URL, String::class.java)
    assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
    assertThat(jsonTester.from(response.body)).extractingJsonPathStringValue("$.build.name").isEqualTo("cmd-api")
  }

  companion object {
    private const val PING_URL = "/health/ping"
    private const val HEALTH_URL = "/health"
    private const val INFO_URL = "/info"
  }
}
