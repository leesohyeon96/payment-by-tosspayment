package com.shl.payment.payment.application

import com.shl.payment.common.event.PaymentFailedEvent
import com.shl.payment.common.outbox.OutboxEventStore
import com.shl.payment.payment.domain.PaymentRepository
import com.shl.payment.payment.domain.PaymentStatus
import com.shl.payment.payment.infrastructure.TossPaymentClient
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Component
class PaymentScheduler(
    private val paymentRepository: PaymentRepository,
    private val tossClient: TossPaymentClient,
    private val outboxEventStore: OutboxEventStore,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @Scheduled(fixedDelay = 300_000)
    @Transactional
    fun cancelStalePayments() {
        val threshold = LocalDateTime.now().minusMinutes(10)
        val stalePayments = paymentRepository.findByStatusAndRequestedAtBefore(
            status = PaymentStatus.READY,
            before = threshold,
        )

        if (stalePayments.isEmpty()) return

        log.info("미완료 결제 ${stalePayments.size}건 취소 처리 시작")

        stalePayments.forEach { payment ->
            runCatching {
                payment.paymentKey?.let { key ->
                    tossClient.cancel(key, "시간 초과로 인한 자동 취소")
                }
                val reason = "10분 초과 미완료"
                payment.fail(reason)
                paymentRepository.save(payment)
                outboxEventStore.store("PAYMENT_FAILED", PaymentFailedEvent(orderId = payment.orderId, reason = reason))
                log.info("미완료 결제 취소: id=${payment.id}")
            }.onFailure {
                log.error("미완료 결제 취소 실패: id=${payment.id}", it)
            }
        }
    }
}
