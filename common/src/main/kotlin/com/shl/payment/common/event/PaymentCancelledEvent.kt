package com.shl.payment.common.event

import java.util.UUID

data class PaymentCancelledEvent(
    val orderId: UUID,
)
