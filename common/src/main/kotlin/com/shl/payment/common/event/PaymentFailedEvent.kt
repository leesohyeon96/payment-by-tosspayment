package com.shl.payment.common.event

import java.util.UUID

data class PaymentFailedEvent(
    val eventId: UUID = UUID.randomUUID(),
    val orderId: UUID,
    val reason: String,
)
