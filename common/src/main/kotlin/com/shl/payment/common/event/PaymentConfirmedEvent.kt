package com.shl.payment.common.event

import java.util.UUID

data class PaymentConfirmedEvent(
    val eventId: UUID = UUID.randomUUID(),
    val orderId: UUID,
    val paymentKey: String,
)
