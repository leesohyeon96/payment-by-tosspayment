package com.shl.payment.order.domain

import com.shl.payment.common.domain.Money
import com.shl.payment.common.domain.MoneyConverter
import com.shl.payment.common.domain.Quantity
import com.shl.payment.common.domain.QuantityConverter
import jakarta.persistence.*
import java.util.UUID

@Entity
@Table(name = "order_items")
class OrderItem(
    productId: UUID,
    quantity: Quantity,
    unitPrice: Money,
) {
    @Id
    val id: UUID = UUID.randomUUID()

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    lateinit var order: Order
        private set

    @Column(nullable = false)
    val productId: UUID = productId

    @Convert(converter = QuantityConverter::class)
    @Column(nullable = false)
    val quantity: Quantity = quantity

    @Convert(converter = MoneyConverter::class)
    @Column(name = "unit_price", nullable = false)
    val unitPrice: Money = unitPrice

    val subtotal: Money get() = unitPrice * quantity.value

    internal fun assignTo(order: Order) {
        this.order = order
    }
}
