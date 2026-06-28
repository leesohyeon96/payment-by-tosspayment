package com.shl.payment.order.application.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Positive

data class CreateOrderRequest(
    @field:NotBlank val orderName: String,
    @field:Positive val totalAmount: Long,
)
