package com.shl.payment.order.application.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty

data class CreateOrderRequest(
    @field:NotBlank val orderName: String,
    @field:NotEmpty val items: List<OrderItemRequest>,
)
