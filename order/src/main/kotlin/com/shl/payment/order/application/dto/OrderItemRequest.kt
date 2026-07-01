package com.shl.payment.order.application.dto

import jakarta.validation.constraints.Positive
import java.util.UUID

data class OrderItemRequest(
    val productId: UUID,
    @field:Positive val quantity: Long,
    @field:Positive val unitPrice: Long,
)
