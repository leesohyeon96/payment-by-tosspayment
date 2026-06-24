package com.shl.payment.payment.application

import com.shl.payment.common.event.OrderCreatedEvent
import com.shl.payment.common.event.PaymentCreationFailedEvent
import com.shl.payment.common.outbox.OutboxEventStore
import com.shl.payment.common.outbox.ProcessedEvent
import com.shl.payment.common.outbox.ProcessedEventRepository
import com.shl.payment.payment.domain.Payment
import com.shl.payment.payment.domain.PaymentRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional

@Service
class PaymentCreationService(
    private val paymentRepository: PaymentRepository,
    private val outboxEventStore: OutboxEventStore,
    private val processedEventRepository: ProcessedEventRepository,
) {
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    fun createPayment(event: OrderCreatedEvent) {
        val key = "${event.eventId}:payment:create"
        if (processedEventRepository.existsById(key)) return
        paymentRepository.save(Payment(orderId = event.orderId, amount = event.totalAmount))
        processedEventRepository.save(ProcessedEvent(key))
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    fun storeCompensatingEvent(event: OrderCreatedEvent, reason: String) {
        outboxEventStore.store(
            "PAYMENT_CREATION_FAILED",
            PaymentCreationFailedEvent(orderId = event.orderId, reason = reason),
        )
    }
}
