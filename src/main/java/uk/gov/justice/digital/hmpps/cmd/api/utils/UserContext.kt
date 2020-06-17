package uk.gov.justice.digital.hmpps.cmd.api.utils

object UserContext {
    private val authToken = ThreadLocal<String>()
    fun getAuthToken(): String {
        return authToken.get()
    }

    fun setAuthToken(aToken: String?) {
        authToken.set(aToken)
    }
}