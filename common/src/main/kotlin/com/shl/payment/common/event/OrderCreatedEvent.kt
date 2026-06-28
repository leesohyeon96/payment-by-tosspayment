package com.shl.payment.common.event

import java.util.UUID

data class OrderCreatedEvent(
    val orderId: UUID,
    val totalAmount: Long,
)
