package com.shl.payment.inventory.application

import com.shl.payment.common.event.PaymentCancelledEvent
import com.shl.payment.common.event.PaymentConfirmedEvent
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import org.springframework.transaction.event.TransactionPhase
import org.springframework.transaction.event.TransactionalEventListener

@Component
class InventoryEventListener(private val stockService: StockService) {

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional
    fun onPaymentConfirmed(event: PaymentConfirmedEvent) {
        stockService.confirmReservation(event.orderId)
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional
    fun onPaymentCancelled(event: PaymentCancelledEvent) {
        stockService.cancelReservation(event.orderId)
    }
}
