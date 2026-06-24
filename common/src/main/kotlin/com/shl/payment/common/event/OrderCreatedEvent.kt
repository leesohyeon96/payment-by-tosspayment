package com.shl.payment.common.event

import java.util.UUID

data class OrderCreatedEvent(
    val eventId: UUID = UUID.randomUUID(),
    val orderId: UUID,
    val totalAmount: Long,
    val productId: UUID? = null,
    val quantity: Long = 1L,
)
