package com.shl.payment.payment.application

import com.shl.payment.common.event.OrderCreatedEvent
import com.shl.payment.common.event.PaymentCreationFailedEvent
import com.shl.payment.common.outbox.OutboxEventStore
import com.shl.payment.payment.domain.Payment
import com.shl.payment.payment.domain.PaymentRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import org.springframework.transaction.event.TransactionPhase
import org.springframework.transaction.event.TransactionalEventListener

@Component
class PaymentEventListener(
    private val paymentRepository: PaymentRepository,
    private val outboxEventStore: OutboxEventStore,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    @Transactional
    fun onOrderCreated(event: OrderCreatedEvent) {
        runCatching {
            paymentRepository.save(Payment(orderId = event.orderId, amount = event.totalAmount))
        }.onFailure { ex ->
            log.error("Payment 생성 실패 — 주문 취소 보상 트랜잭션 시작: orderId=${event.orderId}", ex)
            outboxEventStore.store(
                "PAYMENT_CREATION_FAILED",
                PaymentCreationFailedEvent(event.orderId, ex.message ?: "Payment 생성 실패"),
            )
        }
    }
}
