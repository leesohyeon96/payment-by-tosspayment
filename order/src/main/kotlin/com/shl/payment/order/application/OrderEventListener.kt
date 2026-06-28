package com.shl.payment.order.application

import com.shl.payment.common.event.PaymentCancelledEvent
import com.shl.payment.common.event.PaymentConfirmedEvent
import com.shl.payment.common.event.PaymentCreationFailedEvent
import com.shl.payment.common.event.PaymentFailedEvent
import com.shl.payment.order.domain.OrderRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import org.springframework.transaction.event.TransactionPhase
import org.springframework.transaction.event.TransactionalEventListener

@Component
class OrderEventListener(private val orderRepository: OrderRepository) {
    private val log = LoggerFactory.getLogger(javaClass)

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

    // 보상 트랜잭션: Payment 생성 실패 → 주문 취소
    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    @Transactional
    fun onPaymentCreationFailed(event: PaymentCreationFailedEvent) {
        log.warn("보상 트랜잭션 실행 — Payment 생성 실패로 주문 취소: orderId=${event.orderId}")
        orderRepository.findById(event.orderId).ifPresent { order ->
            order.markCancelled()
            orderRepository.save(order)
        }
    }

    // 보상 트랜잭션: 결제 시간 초과 → 주문 취소
    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    @Transactional
    fun onPaymentFailed(event: PaymentFailedEvent) {
        log.warn("보상 트랜잭션 실행 — 결제 시간 초과로 주문 취소: orderId=${event.orderId}")
        orderRepository.findById(event.orderId).ifPresent { order ->
            order.markCancelled()
            orderRepository.save(order)
        }
    }
}
