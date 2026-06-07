package com.shl.payment.payment.infrastructure.dto

data class TossConfirmRequest(
    val paymentKey: String,
    val orderId: String,
    val amount: Long,
)
