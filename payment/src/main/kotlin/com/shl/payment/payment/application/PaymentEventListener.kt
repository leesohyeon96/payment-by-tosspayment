package com.shl.payment.payment.application

import com.shl.payment.common.event.OrderCreatedEvent
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.transaction.event.TransactionPhase
import org.springframework.transaction.event.TransactionalEventListener

@Component
class PaymentEventListener(private val paymentCreationService: PaymentCreationService) {
    private val log = LoggerFactory.getLogger(javaClass)

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    fun onOrderCreated(event: OrderCreatedEvent) {
        runCatching {
            paymentCreationService.createPayment(event)
        }.onFailure { ex ->
            log.error("Payment 생성 실패 — 주문 취소 보상 트랜잭션 시작: orderId=${event.orderId}", ex)
            paymentCreationService.storeCompensatingEvent(event, ex.message ?: "Payment 생성 실패")
        }
    }
}
