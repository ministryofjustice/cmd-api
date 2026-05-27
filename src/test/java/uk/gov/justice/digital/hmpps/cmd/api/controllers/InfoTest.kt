package uk.gov.justice.digital.hmpps.cmd.api.controllers

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.info.BuildProperties
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient
import org.springframework.test.web.servlet.client.RestTestClient

@AutoConfigureRestTestClient
class InfoTest(
  @Autowired private val buildProperties: BuildProperties,
  @Autowired val restTestClient: RestTestClient,
) : ResourceTest() {

  @Test
  fun `Info page is accessible`() {
    restTestClient.get()
      .uri("/info")
      .exchange()
      .expectStatus()
      .isOk
      .expectBody()
      .jsonPath("build.name").isEqualTo("cmd-api")
  }

  @Test
  fun `Info page reports version`() {
    restTestClient.get().uri("/info")
      .exchange()
      .expectStatus().isOk
      .expectBody().jsonPath("build.version").isEqualTo(buildProperties.version)
  }
}
