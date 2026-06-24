package com.shl.payment.order.domain

import jakarta.persistence.*
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(name = "orders")
@EntityListeners(AuditingEntityListener::class)
class Order(
    userId: UUID,
    totalAmount: Long,
    orderName: String,
    productId: UUID? = null,
    quantity: Long = 1L,
) {
    @Id
    val id: UUID = UUID.randomUUID()

    @Column(nullable = false)
    val userId: UUID = userId

    @Column(nullable = false)
    val totalAmount: Long = totalAmount

    @Column(nullable = false)
    val orderName: String = orderName

    @Column(nullable = true)
    val productId: UUID? = productId

    @Column(nullable = false)
    val quantity: Long = quantity

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: OrderStatus = OrderStatus.PENDING
        private set

    @CreatedDate
    @Column(updatable = false)
    var createdAt: LocalDateTime = LocalDateTime.now()
        private set

    fun markPaid() {
        check(status == OrderStatus.PENDING) { "PENDING 상태만 PAID로 전이 가능" }
        status = OrderStatus.PAID
    }

    fun markCancelled() {
        check(status != OrderStatus.CANCELLED) { "이미 취소된 주문입니다" }
        status = OrderStatus.CANCELLED
    }
}
