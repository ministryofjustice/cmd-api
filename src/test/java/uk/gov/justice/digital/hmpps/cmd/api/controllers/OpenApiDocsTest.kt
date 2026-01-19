package uk.gov.justice.digital.hmpps.cmd.api.controllers

import io.swagger.v3.parser.OpenAPIV3Parser
import net.minidev.json.JSONArray
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.client.RestTestClient
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@AutoConfigureRestTestClient
class OpenApiDocsTest(@Autowired val restTestClient: RestTestClient) : ResourceTest() {
  @LocalServerPort
  private val port: Int = 0

  @Test
  fun `open api docs are available`() {
    restTestClient.get()
      .uri("/swagger-ui/index.html?configUrl=/v3/api-docs")
      .accept(MediaType.APPLICATION_JSON)
      .exchange()
      .expectStatus().isOk
  }

  @Test
  fun `open api docs redirect to correct page`() {
    restTestClient.get()
      .uri("/swagger-ui.html")
      .accept(MediaType.APPLICATION_JSON)
      .exchange()
      .expectStatus().is3xxRedirection
      .expectHeader().value("Location") { it.contains("/swagger-ui/index.html?configUrl=/v3/api-docs/swagger-config") }
  }

  @Test
  fun `the open api json contains documentation`() {
    restTestClient.get()
      .uri("/v3/api-docs")
      .accept(MediaType.APPLICATION_JSON)
      .exchange()
      .expectStatus().isOk
      .expectBody().jsonPath("paths").isNotEmpty
  }

  @Test
  fun `the open api json is valid and contains documentation`() {
    val result = OpenAPIV3Parser().readLocation("http://localhost:$port/v3/api-docs", null, null)
    assertThat(result.messages).isEmpty()
    assertThat(result.openAPI.paths).isNotEmpty
  }

  @Test
  fun `the open api json contains the version number`() {
    restTestClient.get()
      .uri("/v3/api-docs")
      .accept(MediaType.APPLICATION_JSON)
      .exchange()
      .expectStatus().isOk
      .expectBody().jsonPath("info.version").value<String> {
        assertThat(it).startsWith(DateTimeFormatter.ISO_DATE.format(LocalDate.now()))
      }
  }

  @Test
  fun `the generated open api for date times hasn't got the time zone`() {
    restTestClient.get()
      .uri("/v3/api-docs")
      .accept(MediaType.APPLICATION_JSON)
      .exchange()
      .expectStatus().isOk
      .expectBody()
      .jsonPath("$.components.schemas.DetailDto.properties.start.example").isEqualTo("2020-04-20T17:00:00")
      .jsonPath("$.components.schemas.DetailDto.properties.start.description")
      .isEqualTo("The start of the detail")
      .jsonPath("$.components.schemas.DetailDto.properties.start.type").isEqualTo("string")
      .jsonPath("$.components.schemas.DetailDto.properties.start.format").isEqualTo("date-time")
  }

  @ParameterizedTest
  @CsvSource(value = ["bearer-jwt"])
  fun `the security scheme is setup for bearer tokens`(key: String) {
    restTestClient.get()
      .uri("/v3/api-docs")
      .accept(MediaType.APPLICATION_JSON)
      .exchange()
      .expectStatus().isOk
      .expectBody()
      .jsonPath("$.components.securitySchemes.$key.type").isEqualTo("http")
      .jsonPath("$.components.securitySchemes.$key.scheme").isEqualTo("bearer")
      .jsonPath("$.components.securitySchemes.$key.description").value<String> {
        assertThat(it).contains("An HMPPS Auth access token.")
      }
      .jsonPath("$.components.securitySchemes.$key.bearerFormat").isEqualTo("JWT")
      .jsonPath("$.security[0].$key").isEqualTo(
        JSONArray().apply { addAll(listOf("read", "write")) },
      )
  }

  @Test
  fun `the open api json doesn't include LocalTime`() {
    restTestClient.get()
      .uri("/v3/api-docs")
      .accept(MediaType.APPLICATION_JSON)
      .exchange()
      .expectStatus().isOk
      .expectBody()
      .jsonPath("components.schemas.LocalTime").doesNotExist()
  }

  @Test
  fun `the response contains required fields`() {
    restTestClient.get()
      .uri("/v3/api-docs")
      .accept(MediaType.APPLICATION_JSON)
      .exchange()
      .expectStatus().isOk
      .expectBody()
      .jsonPath("$.components.schemas.ShiftDto.required").value<List<String>> {
        assertThat(it).containsExactlyInAnyOrder("date", "details", "fullDayType", "fullDayTypeDescription")
      }
  }
}
