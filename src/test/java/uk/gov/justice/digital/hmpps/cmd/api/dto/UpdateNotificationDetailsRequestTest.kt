package uk.gov.justice.digital.hmpps.cmd.api.dto

import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.cmd.api.domain.CommunicationPreference
import uk.gov.justice.digital.hmpps.cmd.api.dto.UpdateNotificationDetailsRequest

class UpdateNotificationDetailsRequestTest {

    @Test
    fun `Create Update Notification Details Request`() {
        val email = "Any Email"
        val sms = "any sms"
        val pref = CommunicationPreference.EMAIL
        val updateNotificationDetailsRequest = UpdateNotificationDetailsRequest(email, sms, pref)

        Assertions.assertThat(updateNotificationDetailsRequest.email).isEqualTo(email)
        Assertions.assertThat(updateNotificationDetailsRequest.sms).isEqualTo(sms)
        Assertions.assertThat(updateNotificationDetailsRequest.commPref).isEqualTo(pref)
    }
}