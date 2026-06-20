package com.shl.payment.payment.application.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Positive

data class CancelPaymentRequest(
    @field:NotBlank val cancelReason: String,
    @field:Positive val cancelAmount: Long? = null,
)
