package uk.gov.justice.digital.hmpps.cmd.api.controllers

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient
import org.springframework.http.HttpStatus
import org.springframework.test.web.servlet.client.RestTestClient

@AutoConfigureRestTestClient
class HealthResourceTest(@Autowired val restTestClient: RestTestClient) : ResourceTest() {

  @Test
  fun `Ping test`() {
    restTestClient.get().uri(PING_URL)
      .exchange()
      .expectStatus().isEqualTo(HttpStatus.OK)
      .expectBody().jsonPath("$.status").isEqualTo("UP")
  }

  @Test
  fun `Health test`() {
    restTestClient.get().uri(HEALTH_URL)
      .exchange()
      .expectStatus().isEqualTo(HttpStatus.OK)
      .expectBody()
      .jsonPath("$.status").isEqualTo("UP")
      .jsonPath("$.components.ping.status").isEqualTo("UP")
      .jsonPath("$.components.diskSpace.status").isEqualTo("UP")
  }

  @Test
  fun `Info test`() {
    restTestClient.get().uri(INFO_URL)
      .exchange()
      .expectStatus().isEqualTo(HttpStatus.OK)
      .expectBody()
      .jsonPath("$.build.name").isEqualTo("cmd-api")
      .jsonPath($$"$.cache-userDetails").value<String> {
        assertThat(it).contains("hitCount")
      }
  }

  companion object {
    private const val PING_URL = "/health/ping"
    private const val HEALTH_URL = "/health"
    private const val INFO_URL = "/info"
  }
}
