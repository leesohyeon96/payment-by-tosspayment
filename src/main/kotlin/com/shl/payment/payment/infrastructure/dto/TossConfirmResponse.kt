package com.shl.payment.payment.infrastructure.dto

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class TossConfirmResponse(
    val paymentKey: String,
    val orderId: String,
    val status: String,
    val method: String?,
    val totalAmount: Long,
)
