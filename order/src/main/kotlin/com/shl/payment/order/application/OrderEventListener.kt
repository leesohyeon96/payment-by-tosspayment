package com.shl.payment.order.application

import com.shl.payment.common.event.PaymentCancelledEvent
import com.shl.payment.common.event.PaymentConfirmedEvent
import com.shl.payment.common.event.PaymentCreationFailedEvent
import com.shl.payment.common.event.PaymentFailedEvent
import com.shl.payment.common.outbox.ProcessedEvent
import com.shl.payment.common.outbox.ProcessedEventRepository
import com.shl.payment.order.domain.OrderRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.transaction.event.TransactionPhase
import org.springframework.transaction.event.TransactionalEventListener

@Component
class OrderEventListener(
    private val orderRepository: OrderRepository,
    private val processedEventRepository: ProcessedEventRepository,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    fun onPaymentConfirmed(event: PaymentConfirmedEvent) {
        val key = "${event.eventId}:order:paid"
        if (processedEventRepository.existsById(key)) return
        orderRepository.findById(event.orderId).ifPresent { order ->
            order.markPaid()
            orderRepository.save(order)
        }
        processedEventRepository.save(ProcessedEvent(key))
    }

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    fun onPaymentCancelled(event: PaymentCancelledEvent) {
        val key = "${event.eventId}:order:cancel"
        if (processedEventRepository.existsById(key)) return
        orderRepository.findById(event.orderId).ifPresent { order ->
            order.markCancelled()
            orderRepository.save(order)
        }
        processedEventRepository.save(ProcessedEvent(key))
    }

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    fun onPaymentCreationFailed(event: PaymentCreationFailedEvent) {
        val key = "${event.eventId}:order:cancel"
        if (processedEventRepository.existsById(key)) return
        log.warn("보상 트랜잭션 실행 — Payment 생성 실패로 주문 취소: orderId=${event.orderId}")
        orderRepository.findById(event.orderId).ifPresent { order ->
            order.markCancelled()
            orderRepository.save(order)
        }
        processedEventRepository.save(ProcessedEvent(key))
    }

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    fun onPaymentFailed(event: PaymentFailedEvent) {
        val key = "${event.eventId}:order:cancel"
        if (processedEventRepository.existsById(key)) return
        log.warn("보상 트랜잭션 실행 — 결제 시간 초과로 주문 취소: orderId=${event.orderId}")
        orderRepository.findById(event.orderId).ifPresent { order ->
            order.markCancelled()
            orderRepository.save(order)
        }
        processedEventRepository.save(ProcessedEvent(key))
    }
}
