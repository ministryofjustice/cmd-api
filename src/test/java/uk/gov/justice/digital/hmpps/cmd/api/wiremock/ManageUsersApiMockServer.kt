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

class ManageUsersApiExtension :
  BeforeAllCallback,
  AfterAllCallback,
  BeforeEachCallback {
  companion object {
    @JvmField
    val api = ManageUsersApiMockServer()
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

class ManageUsersApiMockServer : WireMockServer(WIREMOCK_PORT) {
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

  fun stubUsersMe(user: String, prison: String) {
    stubFor(
      get("/prisonusers/$user/details").willReturn(
        okJson(
          """
    {
      "username": "testuser1",
      "staffId": 324323,
      "firstName": "John",
      "lastName": "Smith",
      "activeCaseloadId": "$prison",
      "accountStatus": "OPEN",
      "accountType": "GENERAL",
      "primaryEmail": "test@test.com",
      "dpsRoleCodes": [],
      "administratorOfUserGroups": [],
      "accountNonLocked": true,
      "credentialsNonExpired": true,
      "enabled": true,
      "admin": true,
      "active": true,
      "staffStatus": "ACTIVE",
      "lastLogonDate": "2023-01-01T12:13:14.123",
      "userId": 0,
      "name": "string",
      "authSource": "auth"
    } 
          """.trimIndent(),
        ),
      ),
    )
  }

  fun getCountFor(url: String) = this.findAll(WireMock.getRequestedFor(WireMock.urlEqualTo(url))).count()
}
