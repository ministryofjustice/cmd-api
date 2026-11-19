package uk.gov.justice.digital.hmpps.prisonertonomisupdate.wiremock

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.get
import com.github.tomakehurst.wiremock.client.WireMock.okJson
import org.junit.jupiter.api.extension.AfterAllCallback
import org.junit.jupiter.api.extension.BeforeAllCallback
import org.junit.jupiter.api.extension.BeforeEachCallback
import org.junit.jupiter.api.extension.ExtensionContext

class PrisonApiExtension :
  BeforeAllCallback,
  AfterAllCallback,
  BeforeEachCallback {
  companion object {
    @JvmField
    val api = PrisonApiMockServer()
  }

  override fun beforeAll(context: ExtensionContext) {
    api.start()
  }

  override fun beforeEach(context: ExtensionContext) {
    api.resetRequests()
  }

  override fun afterAll(context: ExtensionContext) {
    api.stop()
  }
}

class PrisonApiMockServer : WireMockServer(WIREMOCK_PORT) {
  companion object {
    private const val WIREMOCK_PORT = 8082
  }

  fun stubHealthPing(status: Int) {
    stubFor(
      get("/health/ping").willReturn(
        aResponse()
          .withHeader("Content-Type", "application/json")
          .withBody(if (status == 200) "pong" else "some error")
          .withStatus(status),
      ),
    )
  }

  fun stubUsersMe(prison: String = "WMI") {
    stubFor(
      get("/users/me/caseloads").willReturn(
        okJson(
          """
    { 
      "username": "TESTUSER1",
      "active": true,
      "accountType": "GENERAL",
      "activeCaseload": {
        "id": "$prison",
        "name": "HMP $prison",
        "function": "INST"
      },  
      "caseloads": []
    }    
          """.trimIndent(),
        ),
      ),
    )
  }

  fun getCountFor(url: String) = this.findAll(WireMock.getRequestedFor(WireMock.urlEqualTo(url))).count()
}
