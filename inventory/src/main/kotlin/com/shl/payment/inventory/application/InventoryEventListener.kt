package com.shl.payment.inventory.application

import com.shl.payment.common.event.OrderCreatedEvent
import com.shl.payment.common.event.PaymentCancelledEvent
import com.shl.payment.common.event.PaymentConfirmedEvent
import com.shl.payment.common.event.PaymentCreationFailedEvent
import com.shl.payment.common.event.PaymentFailedEvent
import com.shl.payment.common.outbox.ProcessedEvent
import com.shl.payment.common.outbox.ProcessedEventRepository
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import org.springframework.transaction.event.TransactionPhase
import org.springframework.transaction.event.TransactionalEventListener

@Component
class InventoryEventListener(
    private val stockService: StockService,
    private val processedEventRepository: ProcessedEventRepository,
) {
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    fun onOrderCreated(event: OrderCreatedEvent) {
        val key = "${event.eventId}:inventory:reserve"
        if (processedEventRepository.existsById(key)) return
        event.items.forEach { item ->
            stockService.reserve(event.orderId, item.productId, item.quantity)
        }
        processedEventRepository.save(ProcessedEvent(key))
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    fun onPaymentConfirmed(event: PaymentConfirmedEvent) {
        val key = "${event.eventId}:inventory:confirm"
        if (processedEventRepository.existsById(key)) return
        stockService.confirmReservation(event.orderId)
        processedEventRepository.save(ProcessedEvent(key))
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    fun onPaymentCancelled(event: PaymentCancelledEvent) {
        val key = "${event.eventId}:inventory:cancel"
        if (processedEventRepository.existsById(key)) return
        stockService.cancelReservation(event.orderId)
        processedEventRepository.save(ProcessedEvent(key))
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    fun onPaymentCreationFailed(event: PaymentCreationFailedEvent) {
        val key = "${event.eventId}:inventory:cancel"
        if (processedEventRepository.existsById(key)) return
        stockService.cancelReservation(event.orderId)
        processedEventRepository.save(ProcessedEvent(key))
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    fun onPaymentFailed(event: PaymentFailedEvent) {
        val key = "${event.eventId}:inventory:cancel"
        if (processedEventRepository.existsById(key)) return
        stockService.cancelReservation(event.orderId)
        processedEventRepository.save(ProcessedEvent(key))
    }
}
