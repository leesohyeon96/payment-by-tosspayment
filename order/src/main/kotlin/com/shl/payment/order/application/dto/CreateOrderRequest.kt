package com.shl.payment.order.application.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Positive
import java.util.UUID

data class CreateOrderRequest(
    @field:NotBlank val orderName: String,
    @field:Positive val totalAmount: Long,
    val productId: UUID? = null,
    @field:Positive val quantity: Long = 1L,
)
