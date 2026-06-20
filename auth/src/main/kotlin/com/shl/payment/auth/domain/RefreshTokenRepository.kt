package com.shl.payment.auth.domain

import java.util.Optional
import java.util.UUID

interface RefreshTokenRepository {
    fun save(token: RefreshToken): RefreshToken
    fun findByToken(token: String): Optional<RefreshToken>
    fun deleteByUserId(userId: UUID)
}
