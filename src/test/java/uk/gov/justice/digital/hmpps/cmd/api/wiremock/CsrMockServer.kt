package uk.gov.justice.digital.hmpps.prisonertonomisupdate.wiremock

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.equalTo
import com.github.tomakehurst.wiremock.client.WireMock.get
import com.github.tomakehurst.wiremock.client.WireMock.matching
import org.junit.jupiter.api.extension.AfterAllCallback
import org.junit.jupiter.api.extension.BeforeAllCallback
import org.junit.jupiter.api.extension.BeforeEachCallback
import org.junit.jupiter.api.extension.ExtensionContext

class CsrApiExtension : BeforeAllCallback, AfterAllCallback, BeforeEachCallback {
  companion object {
    @JvmField
    val api = CsrMockServer()
  }

  override fun beforeAll(context: ExtensionContext) {
    api.start()
  }

  override fun beforeEach(context: ExtensionContext) {
    api.resetAll()
  }

  override fun afterAll(context: ExtensionContext) {
    api.stop()
  }
}

class CsrMockServer : WireMockServer(WIREMOCK_PORT) {
  companion object {
    private const val WIREMOCK_PORT = 8081
  }

  fun stubHealthPing(status: Int) {
    stubFor(
      get("/health/ping").willReturn(
        aResponse()
          .withHeader("Content-Type", "application/json")
          .withBody(if (status == 200) "pong" else "some error")
          .withStatus(status)
      )
    )
  }

  fun stubUserDetails(region: Int, from: String, to: String, response: String) {
    stubFor(
      get("/user/details?from=$from&to=$to")
        .withHeader("X-Region", matching(region.toString()))
        .willReturn(
          aResponse()
            .withHeader("Content-Type", "application/json")
            .withBody(response)
            .withStatus(200)
        )
    )
  }

  fun stubGetUpdates(region: Int, response: String) {
    stubFor(
      get("/updates/$region").willReturn(
        aResponse()
          .withHeader("Content-Type", "application/json")
          .withBody(response)
          .withStatus(200)
      )
    )
  }

  fun stubGetUpdatesWithError(region: Int, status: Int = 500) {
    stubFor(
      get("/updates/$region").willReturn(
        aResponse()
          .withHeader("Content-Type", "application/json")
          .withBody("""{ "status": $status, "userMessage": "id does not exist" }""")
          .withStatus(status)
      )
    )
  }

  fun stubDeleteProcessed(region: Int, expectedRequestBody: String) {
    stubFor(
      WireMock.put("/updates/$region")
        .withRequestBody(equalTo(expectedRequestBody))
        .willReturn(
          aResponse()
            .withHeader("Content-Type", "application/json")
            .withStatus(200)
        )
    )
  }

  fun putCountFor(url: String) = this.findAll(WireMock.putRequestedFor(WireMock.urlEqualTo(url))).count()
  fun getCountFor(url: String) = this.findAll(WireMock.getRequestedFor(WireMock.urlEqualTo(url))).count()
}
