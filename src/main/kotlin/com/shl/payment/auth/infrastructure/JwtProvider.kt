package com.shl.payment.auth.infrastructure

import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.util.Date
import java.util.UUID

@Component
class JwtProvider(
    @Value("\${jwt.secret}") private val secret: String,
    @Value("\${jwt.access-expiration}") private val accessExpiration: Long,
    @Value("\${jwt.refresh-expiration}") private val refreshExpiration: Long,
) {
    private val key = Keys.hmacShaKeyFor(secret.toByteArray())

    fun generateAccessToken(userId: UUID): String = Jwts.builder()
        .subject(userId.toString())
        .claim("type", "access")
        .issuedAt(Date())
        .expiration(Date(System.currentTimeMillis() + accessExpiration))
        .signWith(key)
        .compact()

    fun generateRefreshToken(userId: UUID): String = Jwts.builder()
        .subject(userId.toString())
        .claim("type", "refresh")
        .issuedAt(Date())
        .expiration(Date(System.currentTimeMillis() + refreshExpiration))
        .signWith(key)
        .compact()

    fun getUserId(token: String): UUID {
        val subject = Jwts.parser()
            .verifyWith(key)
            .build()
            .parseSignedClaims(token)
            .payload
            .subject
        return UUID.fromString(subject)
    }

    fun isValid(token: String): Boolean = runCatching {
        Jwts.parser().verifyWith(key).build().parseSignedClaims(token)
        true
    }.getOrDefault(false)
}
