package com.shl.payment.order.application.dto

import com.shl.payment.order.domain.Order
import com.shl.payment.order.domain.OrderStatus
import java.time.LocalDateTime
import java.util.UUID

data class OrderItemResponse(
    val productId: UUID,
    val quantity: Long,
    val unitPrice: Long,
    val subtotal: Long,
)

data class OrderResponse(
    val orderId: UUID,
    val orderName: String,
    val totalAmount: Long,
    val status: OrderStatus,
    val items: List<OrderItemResponse>,
    val createdAt: LocalDateTime,
) {
    companion object {
        fun from(order: Order) = OrderResponse(
            orderId = order.id,
            orderName = order.orderName,
            totalAmount = order.totalAmount.amount,
            status = order.status,
            items = order.items.map {
                OrderItemResponse(
                    productId = it.productId,
                    quantity = it.quantity.value,
                    unitPrice = it.unitPrice.amount,
                    subtotal = it.subtotal.amount,
                )
            },
            createdAt = order.createdAt,
        )
    }
}
