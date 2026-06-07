package com.shl.payment.auth.domain

import jakarta.persistence.*
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(name = "refresh_tokens")
class RefreshToken(
    userId: UUID,
    token: String,
    expiresAt: LocalDateTime,
) {
    @Id
    val id: UUID = UUID.randomUUID()

    @Column(nullable = false)
    val userId: UUID = userId

    @Column(unique = true, nullable = false)
    var token: String = token
        private set

    @Column(nullable = false)
    val expiresAt: LocalDateTime = expiresAt

    @Column(nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now()

    fun isExpired(): Boolean = LocalDateTime.now().isAfter(expiresAt)
}
