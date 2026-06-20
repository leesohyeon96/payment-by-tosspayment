package com.shl.payment.auth.application.dto

data class TokenResponse(
    val accessToken: String,
    val refreshToken: String,
)
