package com.shl.payment.payment.infrastructure.dto

data class TossCancelRequest(
    val cancelReason: String,
    val cancelAmount: Long? = null,
)
