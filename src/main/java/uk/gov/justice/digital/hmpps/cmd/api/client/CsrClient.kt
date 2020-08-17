package uk.gov.justice.digital.hmpps.cmd.api.uk.gov.justice.digital.hmpps.cmd.api.client

import io.jsonwebtoken.JwtBuilder
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import io.netty.channel.ChannelOption
import io.netty.handler.timeout.ReadTimeoutHandler
import io.netty.handler.timeout.WriteTimeoutHandler
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpHeaders
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import reactor.netty.http.client.HttpClient
import reactor.netty.tcp.TcpClient
import uk.gov.justice.digital.hmpps.cmd.api.uk.gov.justice.digital.hmpps.cmd.api.client.dto.ShiftNotificationDto
import uk.gov.justice.digital.hmpps.cmd.api.uk.gov.justice.digital.hmpps.cmd.api.client.dto.ShiftNotificationsDto
import uk.gov.justice.digital.hmpps.cmd.api.uk.gov.justice.digital.hmpps.cmd.api.client.dto.ShiftTaskNotificationsDto
import uk.gov.justice.digital.hmpps.cmd.api.uk.gov.justice.digital.hmpps.cmd.api.domain.ShiftNotificationType
import uk.gov.justice.digital.hmpps.cmd.api.uk.gov.justice.digital.hmpps.cmd.api.utils.region.Regions
import java.nio.charset.Charset
import java.security.Key
import java.util.*
import javax.crypto.spec.SecretKeySpec

@Component
class CsrClient(val regionData: Regions, @Value("\${jwt.secret}") val secret: String) {

    fun getShiftNotifications(planUnit: String, region: Int): Collection<ShiftNotificationDto> {
        val notifications : ShiftNotificationsDto
        log.info("Finding shift notifications, PlanUnit $planUnit, Region $region")
        try {
             notifications = getAuthorisedWebClient(region)
                    .get()
                    .uri("/notifications/shifts/${planUnit}?interval=24&intervaltype=1")
                    .retrieve()
                    .bodyToMono(ShiftNotificationsDto::class.java)
                    .block()
            log.info("Found ${notifications.shiftNotifications.size} shift notifications, PlanUnit $planUnit, Region $region")

        } catch (e : Exception) {
            // 💩💩💩 The Legacy API returns 404 when there are no results.
            log.info("Found 0 shift notifications, PlanUnit $planUnit, Region $region")
            return listOf()
        }
        return notifications.shiftNotifications
    }

    fun getShiftTaskNotifications(planUnit: String, region: Int): Collection<ShiftNotificationDto> {
        val notifications : ShiftTaskNotificationsDto
        log.info("Finding shift task notifications, PlanUnit $planUnit, Region $region")
        try {
            notifications = getAuthorisedWebClient(region)
                    .get()
                    .uri("/notifications/shifts/${planUnit}/tasks?interval=24&intervaltype=1")
                    .retrieve()
                    .bodyToMono(ShiftTaskNotificationsDto::class.java)
                    .block()
            log.info("Found ${notifications.shiftTaskNotifications.size} shift task notifications, PlanUnit $planUnit, Region $region")

        } catch (e : Exception) {
            // 💩💩💩 The Legacy API returns 404 when there are no results.
            log.info("Found 0 shift task notifications, PlanUnit $planUnit, Region $region")
            return listOf()
        }
        notifications.shiftTaskNotifications
                .forEach { notification ->
                    notification.shiftType = if(ShiftNotificationType.from(notification.shiftType) == ShiftNotificationType.SHIFT ||
                            notification.shiftType.equals("task",true)) {
                        ShiftNotificationType.SHIFT_TASK.value
                    } else {
                        ShiftNotificationType.OVERTIME_TASK.value
                    }
                }

        return notifications.shiftTaskNotifications
    }


    /* 💩💩💩 This should move to bean definition */
    private fun getAuthorisedWebClient(region : Int) : WebClient {
        val tcpClient = TcpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 360_000)
                .doOnConnected { connection ->
                    connection.addHandlerLast(ReadTimeoutHandler(360))
                            .addHandlerLast(WriteTimeoutHandler(360))
                }

        val token = generateSelfSignedJwt()
        return WebClient.builder()
                .clientConnector(ReactorClientHttpConnector(HttpClient.from(tcpClient)))
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer $token")
                .baseUrl(getCorrectRegionUrl(region))
                .build()
    }

    /* 💩💩💩 This Is only needed because we're calling the
    *  multiple legacy services
    */
    private fun getCorrectRegionUrl(region : Int) : String {
        return regionData.regions.map {
            it.name to it.url
        }.toMap().getOrDefault(region.toString(), regionData.regions[0].url)
    }

    /* 💩💩💩 We need to self sign a JWT because the legacy
    * service doesn't interact with HMPPS Auth correctly,
    * until we move over to the proper service.
    */
    private fun generateSelfSignedJwt() : String {
        val signingKey: Key = SecretKeySpec(secret.toByteArray(Charset.defaultCharset()),SignatureAlgorithm.HS256.jcaName)
        val builder: JwtBuilder = Jwts.builder().setId(UUID.randomUUID().toString())
                .setIssuedAt(Date(System.currentTimeMillis()))
                .setIssuer("check-my-diary-notification-service")
                .setAudience("PrisonOfficer.Diary.Api")
                .setSubject("notifications")
                .claim("email", "checkmydiary@digital.justice.gov.uk")
                .signWith(SignatureAlgorithm.HS256,signingKey)
                .setExpiration(Date(System.currentTimeMillis() + 3_600_000))

        return builder.compact()
    }

    companion object {

        private val log = LoggerFactory.getLogger(CsrClient::class.java)

    }
}