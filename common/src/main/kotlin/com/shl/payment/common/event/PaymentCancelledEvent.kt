package com.shl.payment.common.event

import java.util.UUID

data class PaymentCancelledEvent(
    val eventId: UUID = UUID.randomUUID(),
    val orderId: UUID,
)
