package com.shl.payment.payment.application

import com.shl.payment.common.event.OrderCreatedEvent
import com.shl.payment.payment.domain.Payment
import com.shl.payment.payment.domain.PaymentRepository
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import org.springframework.transaction.event.TransactionPhase
import org.springframework.transaction.event.TransactionalEventListener

@Component
class PaymentEventListener(private val paymentRepository: PaymentRepository) {

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    @Transactional
    fun onOrderCreated(event: OrderCreatedEvent) {
        paymentRepository.save(Payment(orderId = event.orderId, amount = event.totalAmount))
    }
}
