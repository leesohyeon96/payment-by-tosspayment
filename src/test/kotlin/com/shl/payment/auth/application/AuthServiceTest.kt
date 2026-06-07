package com.shl.payment.auth.application

import com.shl.payment.auth.domain.User
import com.shl.payment.auth.domain.UserRepository
import com.shl.payment.auth.domain.RefreshTokenRepository
import com.shl.payment.auth.infrastructure.JwtProvider
import com.shl.payment.common.exception.BusinessException
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import java.util.Optional
import org.junit.jupiter.api.Assertions.assertNotNull

class AuthServiceTest {

    private val userRepository = mockk<UserRepository>()
    private val refreshTokenRepository = mockk<RefreshTokenRepository>()
    private val jwtProvider = mockk<JwtProvider>()
    private val passwordEncoder = BCryptPasswordEncoder()
    private val authService = AuthService(userRepository, refreshTokenRepository, jwtProvider, passwordEncoder)

    @Test
    fun `회원가입 - 정상`() {
        every { userRepository.existsByEmail("test@test.com") } returns false
        every { userRepository.save(any()) } answers { firstArg() }
        every { refreshTokenRepository.save(any()) } answers { firstArg() }
        every { jwtProvider.generateAccessToken(any()) } returns "access_token"
        every { jwtProvider.generateRefreshToken(any()) } returns "refresh_token"

        val result = authService.signup("test@test.com", "password123")

        assertNotNull(result.accessToken)
        verify { userRepository.save(any()) }
    }

    @Test
    fun `회원가입 - 이미 존재하는 이메일이면 예외`() {
        every { userRepository.existsByEmail("test@test.com") } returns true

        assertThrows<BusinessException> {
            authService.signup("test@test.com", "password123")
        }
    }

    @Test
    fun `로그인 - 이메일 없으면 예외`() {
        every { userRepository.findByEmail("unknown@test.com") } returns Optional.empty()

        assertThrows<BusinessException> {
            authService.login("unknown@test.com", "password123")
        }
    }

    @Test
    fun `로그인 - 비밀번호 틀리면 예외`() {
        val user = User(email = "test@test.com", password = passwordEncoder.encode("correct_password"))
        every { userRepository.findByEmail("test@test.com") } returns Optional.of(user)

        assertThrows<BusinessException> {
            authService.login("test@test.com", "wrong_password")
        }
    }
}
