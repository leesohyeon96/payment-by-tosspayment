package com.shl.payment.inventory.domain

import jakarta.persistence.*
import java.util.UUID

@Entity
@Table(name = "stocks")
class Stock(
    productId: UUID,
    quantity: Long,
) {
    @Id
    val id: UUID = UUID.randomUUID()

    @Column(nullable = false, unique = true)
    val productId: UUID = productId

    @Column(nullable = false)
    var quantity: Long = quantity
        private set

    @Version
    var version: Long = 0
        private set

    fun reserve(amount: Long) {
        check(quantity >= amount) { "재고 부족: 현재 $quantity, 요청 $amount" }
        quantity -= amount
    }

    fun restore(amount: Long) {
        quantity += amount
    }
}
