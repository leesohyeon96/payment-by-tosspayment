package com.shl.payment.common.event

import java.util.UUID

data class PaymentFailedEvent(
    val orderId: UUID,
    val reason: String,
)
