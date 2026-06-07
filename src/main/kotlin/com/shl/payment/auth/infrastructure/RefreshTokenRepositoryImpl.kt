package com.shl.payment.auth.infrastructure

import com.shl.payment.auth.domain.RefreshToken
import com.shl.payment.auth.domain.RefreshTokenRepository
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.Optional
import java.util.UUID

interface RefreshTokenJpaRepository : JpaRepository<RefreshToken, UUID> {
    fun findByToken(token: String): Optional<RefreshToken>
    fun deleteByUserId(userId: UUID)
}

@Repository
class RefreshTokenRepositoryImpl(
    private val jpaRepository: RefreshTokenJpaRepository,
) : RefreshTokenRepository {
    override fun save(token: RefreshToken) = jpaRepository.save(token)
    override fun findByToken(token: String) = jpaRepository.findByToken(token)
    override fun deleteByUserId(userId: UUID) = jpaRepository.deleteByUserId(userId)
}
