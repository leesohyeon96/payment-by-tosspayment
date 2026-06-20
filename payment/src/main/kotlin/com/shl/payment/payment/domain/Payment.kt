package com.shl.payment.payment.domain

import jakarta.persistence.*
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(name = "payments")
@EntityListeners(AuditingEntityListener::class)
class Payment(
    orderId: UUID,
    amount: Long,
) {
    @Id
    val id: UUID = UUID.randomUUID()

    @Version
    var version: Long = 0
        private set

    @Column(nullable = false)
    val orderId: UUID = orderId

    @Column(unique = true)
    var paymentKey: String? = null
        private set

    @Enumerated(EnumType.STRING)
    var method: PaymentMethod? = null
        private set

    @Column(nullable = false)
    val amount: Long = amount

    @Column(nullable = false)
    var cancelledAmount: Long = 0L
        private set

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: PaymentStatus = PaymentStatus.READY
        private set

    var failReason: String? = null
        private set

    @CreatedDate
    @Column(updatable = false)
    var requestedAt: LocalDateTime = LocalDateTime.now()
        private set

    var approvedAt: LocalDateTime? = null
        private set

    var cancelledAt: LocalDateTime? = null
        private set

    fun confirm(paymentKey: String, method: PaymentMethod) {
        check(status == PaymentStatus.READY) { "READY 상태만 승인 가능, 현재: $status" }
        this.paymentKey = paymentKey
        this.method = method
        this.status = PaymentStatus.DONE
        this.approvedAt = LocalDateTime.now()
    }

    fun cancel(amount: Long) {
        check(status == PaymentStatus.DONE) { "DONE 상태만 취소 가능, 현재: $status" }
        check(cancelledAmount + amount <= this.amount) { "취소 금액이 결제 금액을 초과합니다" }
        cancelledAmount += amount
        cancelledAt = LocalDateTime.now()
        if (cancelledAmount == this.amount) {
            status = PaymentStatus.CANCELLED
        }
    }

    fun fail(reason: String) {
        check(status == PaymentStatus.READY) { "READY 상태만 실패 처리 가능, 현재: $status" }
        status = PaymentStatus.FAILED
        failReason = reason
    }
}
