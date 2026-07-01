package com.shl.payment.order.domain

import com.shl.payment.common.domain.Money
import com.shl.payment.common.domain.MoneyConverter
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
    totalAmount: Money,
    orderName: String,
) {
    @Id
    val id: UUID = UUID.randomUUID()

    @Column(nullable = false)
    val userId: UUID = userId

    @Convert(converter = MoneyConverter::class)
    @Column(nullable = false)
    val totalAmount: Money = totalAmount

    @Column(nullable = false)
    val orderName: String = orderName

    @OneToMany(cascade = [CascadeType.ALL], orphanRemoval = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    val items: MutableList<OrderItem> = mutableListOf()

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: OrderStatus = OrderStatus.PENDING
        private set

    @CreatedDate
    @Column(updatable = false)
    var createdAt: LocalDateTime = LocalDateTime.now()
        private set

    fun addItem(item: OrderItem) {
        items.add(item)
    }

    fun markPaid() {
        check(status == OrderStatus.PENDING) { "PENDING 상태만 PAID로 전이 가능" }
        status = OrderStatus.PAID
    }

    fun markCancelled() {
        check(status != OrderStatus.CANCELLED) { "이미 취소된 주문입니다" }
        status = OrderStatus.CANCELLED
    }
}
