package com.shl.payment.common.event

import java.util.UUID

data class OrderItemDto(
    val productId: UUID,
    val quantity: Long,
)

data class OrderCreatedEvent(
    val eventId: UUID = UUID.randomUUID(),
    val orderId: UUID,
    val totalAmount: Long,
    val items: List<OrderItemDto> = emptyList(),
)
