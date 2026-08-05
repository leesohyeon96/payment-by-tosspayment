package com.shl.payment.webhook

import com.shl.payment.order.domain.OrderRepository
import com.shl.payment.payment.domain.PaymentMethod
import com.shl.payment.payment.domain.PaymentRepository
import com.shl.payment.payment.domain.PaymentStatus
import com.shl.payment.payment.infrastructure.TossPaymentClient
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.*
import java.util.UUID

@RestController
@RequestMapping("/webhook")
class WebhookController(
    private val paymentRepository: PaymentRepository,
    private val orderRepository: OrderRepository,
    private val tossPaymentClient: TossPaymentClient,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @PostMapping("/payment")
    @Transactional
    fun handlePaymentWebhook(@RequestBody payload: WebhookPayload): ResponseEntity<Void> {
        log.info("Webhook received: eventType=${payload.eventType}, orderId=${payload.data.orderId}")

        val confirmed = runCatching {
            tossPaymentClient.getPayment(payload.data.paymentKey)
        }.getOrElse {
            log.error("Toss API 재확인 실패: paymentKey=${payload.data.paymentKey}", it)
            return ResponseEntity.ok().build()
        }

        if (confirmed.status != "DONE") return ResponseEntity.ok().build()

        val payment = paymentRepository.findByOrderId(UUID.fromString(confirmed.orderId)).orElse(null)
            ?: return ResponseEntity.ok().build()

        if (payment.status == PaymentStatus.READY) {
            runCatching {
                val method = when (confirmed.method) {
                    "카드" -> PaymentMethod.CARD
                    "가상계좌" -> PaymentMethod.VIRTUAL_ACCOUNT
                    "계좌이체" -> PaymentMethod.TRANSFER
                    "휴대폰" -> PaymentMethod.MOBILE_PHONE
                    "문화상품권", "도서문화상품권", "게임문화상품권" -> PaymentMethod.GIFT_CERTIFICATE
                    "간편결제" -> PaymentMethod.EASY_PAY
                    else -> {
                        log.warn("알 수 없는 결제수단: ${confirmed.method}, orderId=${confirmed.orderId}")
                        PaymentMethod.CARD
                    }
                }
                payment.confirm(confirmed.paymentKey, method)
                paymentRepository.save(payment)

                orderRepository.findById(payment.orderId).ifPresent { order ->
                    order.markPaid()
                    orderRepository.save(order)
                }
                log.info("Webhook 보정 완료: paymentKey=${confirmed.paymentKey}")
            }.onFailure {
                log.error("Webhook 보정 실패", it)
            }
        }

        return ResponseEntity.ok().build()
    }
}

data class WebhookPayload(
    val eventType: String,
    val createdAt: String,
    val data: WebhookData,
)

data class WebhookData(
    val paymentKey: String,
    val orderId: String,
    val status: String,
)
