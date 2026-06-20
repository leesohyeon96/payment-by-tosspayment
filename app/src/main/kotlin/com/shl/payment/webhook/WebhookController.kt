package com.shl.payment.webhook

import com.shl.payment.order.domain.OrderRepository
import com.shl.payment.payment.domain.PaymentMethod
import com.shl.payment.payment.domain.PaymentRepository
import com.shl.payment.payment.domain.PaymentStatus
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.ResponseEntity
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.*
import java.util.UUID

@RestController
@RequestMapping("/webhook")
class WebhookController(
    private val paymentRepository: PaymentRepository,
    private val orderRepository: OrderRepository,
    @Value("\${toss.webhook-secret}") private val webhookSecret: String,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @PostMapping("/payment")
    @Transactional
    fun handlePaymentWebhook(@RequestBody payload: WebhookPayload): ResponseEntity<Void> {
        if (payload.secret != webhookSecret) {
            log.warn("Webhook secret mismatch")
            return ResponseEntity.ok().build()
        }

        log.info("Webhook received: paymentKey=${payload.paymentKey}, status=${payload.status}")

        if (payload.status != "DONE") return ResponseEntity.ok().build()

        val payment = paymentRepository.findByOrderId(UUID.fromString(payload.orderId)).orElse(null)
            ?: return ResponseEntity.ok().build()

        if (payment.status == PaymentStatus.READY) {
            runCatching {
                val method = runCatching { PaymentMethod.valueOf(payload.method?.uppercase() ?: "CARD") }
                    .getOrDefault(PaymentMethod.CARD)
                payment.confirm(payload.paymentKey, method)
                paymentRepository.save(payment)

                orderRepository.findById(payment.orderId).ifPresent { order ->
                    order.markPaid()
                    orderRepository.save(order)
                }
                log.info("Webhook으로 결제 보정 완료: paymentKey=${payload.paymentKey}")
            }.onFailure {
                log.error("Webhook 보정 실패", it)
            }
        }

        return ResponseEntity.ok().build()
    }
}

data class WebhookPayload(
    val secret: String,
    val status: String,
    val paymentKey: String,
    val orderId: String,
    val method: String? = null,
)
