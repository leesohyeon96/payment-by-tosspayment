package com.shl.payment.payment.application.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Positive
import java.util.UUID

data class ConfirmPaymentRequest(
    @field:NotBlank val paymentKey: String,
    val orderId: UUID,
    @field:Positive val amount: Long,
)
