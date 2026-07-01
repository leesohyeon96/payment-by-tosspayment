package com.shl.payment.inventory.domain

import jakarta.persistence.*
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(name = "stock_reservations")
@EntityListeners(AuditingEntityListener::class)
class StockReservation(
    orderId: UUID,
    productId: UUID,
    quantity: Long,
) {
    @Id
    val id: UUID = UUID.randomUUID()

    @Column(nullable = false)
    val orderId: UUID = orderId

    @Column(nullable = false)
    val productId: UUID = productId

    @Column(nullable = false)
    val quantity: Long = quantity

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: ReservationStatus = ReservationStatus.RESERVED
        private set

    @CreatedDate
    @Column(updatable = false)
    var createdAt: LocalDateTime = LocalDateTime.now()
        private set

    fun confirm() {
        check(status == ReservationStatus.RESERVED) { "RESERVED 상태만 확정 가능" }
        status = ReservationStatus.CONFIRMED
    }

    fun cancel() {
        check(status == ReservationStatus.RESERVED) { "RESERVED 상태만 취소 가능" }
        status = ReservationStatus.CANCELLED
    }
}
