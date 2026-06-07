package com.shl.payment.order.application.dto

import com.shl.payment.order.domain.Order
import com.shl.payment.order.domain.OrderStatus
import java.time.LocalDateTime
import java.util.UUID

data class OrderResponse(
    val orderId: UUID,
    val orderName: String,
    val totalAmount: Long,
    val status: OrderStatus,
    val createdAt: LocalDateTime,
) {
    companion object {
        fun from(order: Order) = OrderResponse(
            orderId = order.id,
            orderName = order.orderName,
            totalAmount = order.totalAmount,
            status = order.status,
            createdAt = order.createdAt,
        )
    }
}
