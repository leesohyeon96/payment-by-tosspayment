package com.shl.payment.auth.application

import com.shl.payment.auth.domain.RefreshToken
import com.shl.payment.auth.domain.RefreshTokenRepository
import com.shl.payment.auth.domain.User
import com.shl.payment.auth.domain.UserRepository
import com.shl.payment.auth.application.dto.TokenResponse
import com.shl.payment.auth.infrastructure.JwtProvider
import com.shl.payment.common.exception.BusinessException
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.UUID

@Service
@Transactional
class AuthService(
    private val userRepository: UserRepository,
    private val refreshTokenRepository: RefreshTokenRepository,
    private val jwtProvider: JwtProvider,
    private val passwordEncoder: PasswordEncoder,
    @Value("\${jwt.refresh-expiration:604800000}") private val refreshExpiration: Long = 604800000L,
) {
    fun signup(email: String, password: String): TokenResponse {
        if (userRepository.existsByEmail(email)) {
            throw BusinessException("DUPLICATE_EMAIL", "이미 사용 중인 이메일입니다")
        }
        val user = User(email = email, password = passwordEncoder.encode(password))
        userRepository.save(user)
        return issueTokens(user.id)
    }

    fun login(email: String, password: String): TokenResponse {
        val user = userRepository.findByEmail(email)
            .orElseThrow { BusinessException("INVALID_CREDENTIALS", "이메일 또는 비밀번호가 올바르지 않습니다") }
        if (!passwordEncoder.matches(password, user.password)) {
            throw BusinessException("INVALID_CREDENTIALS", "이메일 또는 비밀번호가 올바르지 않습니다")
        }
        refreshTokenRepository.deleteByUserId(user.id)
        return issueTokens(user.id)
    }

    fun refresh(refreshTokenStr: String): TokenResponse {
        val refreshToken = refreshTokenRepository.findByToken(refreshTokenStr)
            .orElseThrow { BusinessException("INVALID_REFRESH_TOKEN", "유효하지 않은 리프레시 토큰입니다") }
        if (refreshToken.isExpired()) {
            throw BusinessException("EXPIRED_REFRESH_TOKEN", "만료된 리프레시 토큰입니다")
        }
        refreshTokenRepository.deleteByUserId(refreshToken.userId)
        return issueTokens(refreshToken.userId)
    }

    @Transactional(readOnly = true)
    fun getEmail(userId: UUID): String {
        return userRepository.findById(userId)
            .orElseThrow { BusinessException("USER_NOT_FOUND", "존재하지 않는 사용자입니다") }
            .email
    }

    private fun issueTokens(userId: UUID): TokenResponse {
        val accessToken = jwtProvider.generateAccessToken(userId)
        val refreshTokenStr = jwtProvider.generateRefreshToken(userId)
        val expiresAt = LocalDateTime.now().plusSeconds(refreshExpiration / 1000)
        refreshTokenRepository.save(RefreshToken(userId, refreshTokenStr, expiresAt))
        return TokenResponse(accessToken, refreshTokenStr)
    }
}
