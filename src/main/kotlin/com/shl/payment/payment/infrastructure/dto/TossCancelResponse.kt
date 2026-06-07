package com.shl.payment.payment.infrastructure.dto

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class TossCancelResponse(
    val paymentKey: String,
    val status: String,
)
