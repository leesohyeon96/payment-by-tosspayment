package com.shl.payment.common.event

import java.util.UUID

data class PaymentCreationFailedEvent(
    val orderId: UUID,
    val reason: String,
)
