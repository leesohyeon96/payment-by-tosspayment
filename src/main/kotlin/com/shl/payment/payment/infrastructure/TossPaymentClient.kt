package com.shl.payment.payment.infrastructure

import com.shl.payment.common.exception.PaymentException
import com.shl.payment.payment.infrastructure.dto.TossCancelRequest
import com.shl.payment.payment.infrastructure.dto.TossCancelResponse
import com.shl.payment.payment.infrastructure.dto.TossConfirmRequest
import com.shl.payment.payment.infrastructure.dto.TossConfirmResponse
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import java.util.Base64

@Component
class TossPaymentClient(
    private val webClient: WebClient,
    @Value("\${toss.secret-key}") private val secretKey: String,
    @Value("\${toss.base-url}") private val baseUrl: String,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    private fun authHeader(): String {
        val encoded = Base64.getEncoder().encodeToString("$secretKey:".toByteArray())
        return "Basic $encoded"
    }

    fun confirm(paymentKey: String, orderId: String, amount: Long): TossConfirmResponse {
        return try {
            webClient.post()
                .uri("$baseUrl/v1/payments/confirm")
                .header(HttpHeaders.AUTHORIZATION, authHeader())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(TossConfirmRequest(paymentKey, orderId, amount))
                .retrieve()
                .bodyToMono(TossConfirmResponse::class.java)
                .block()!!
        } catch (e: WebClientResponseException) {
            log.error("Toss confirm failed: status=${e.statusCode}, body=${e.responseBodyAsString}")
            throw PaymentException.tossFailed(e.responseBodyAsString)
        }
    }

    fun cancel(paymentKey: String, cancelReason: String, cancelAmount: Long? = null): TossCancelResponse {
        return try {
            webClient.post()
                .uri("$baseUrl/v1/payments/$paymentKey/cancel")
                .header(HttpHeaders.AUTHORIZATION, authHeader())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(TossCancelRequest(cancelReason, cancelAmount))
                .retrieve()
                .bodyToMono(TossCancelResponse::class.java)
                .block()!!
        } catch (e: WebClientResponseException) {
            log.error("Toss cancel failed: paymentKey=$paymentKey, body=${e.responseBodyAsString}")
            throw PaymentException.tossFailed(e.responseBodyAsString)
        }
    }
}
