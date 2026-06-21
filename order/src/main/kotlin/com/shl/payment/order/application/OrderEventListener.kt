package com.shl.payment.order.application

import com.shl.payment.common.event.PaymentCancelledEvent
import com.shl.payment.common.event.PaymentConfirmedEvent
import com.shl.payment.order.domain.OrderRepository
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import org.springframework.transaction.event.TransactionPhase
import org.springframework.transaction.event.TransactionalEventListener

@Component
class OrderEventListener(private val orderRepository: OrderRepository) {

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    @Transactional
    fun onPaymentConfirmed(event: PaymentConfirmedEvent) {
        orderRepository.findById(event.orderId).ifPresent { order ->
            order.markPaid()
            orderRepository.save(order)
        }
    }

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    @Transactional
    fun onPaymentCancelled(event: PaymentCancelledEvent) {
        orderRepository.findById(event.orderId).ifPresent { order ->
            order.markCancelled()
            orderRepository.save(order)
        }
    }
}
