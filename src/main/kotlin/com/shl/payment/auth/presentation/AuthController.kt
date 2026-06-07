package com.shl.payment.auth.presentation

import com.shl.payment.auth.application.AuthService
import com.shl.payment.auth.application.dto.LoginRequest
import com.shl.payment.auth.application.dto.SignupRequest
import com.shl.payment.common.response.ApiResponse
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/auth")
class AuthController(
    private val authService: AuthService,
) {
    @PostMapping("/signup")
    fun signup(@RequestBody @Valid request: SignupRequest): ResponseEntity<ApiResponse<*>> {
        val tokens = authService.signup(request.email, request.password)
        return ResponseEntity.ok(ApiResponse.ok(tokens))
    }

    @PostMapping("/login")
    fun login(@RequestBody @Valid request: LoginRequest): ResponseEntity<ApiResponse<*>> {
        val tokens = authService.login(request.email, request.password)
        return ResponseEntity.ok(ApiResponse.ok(tokens))
    }

    @PostMapping("/refresh")
    fun refresh(@RequestHeader("Refresh-Token") refreshToken: String): ResponseEntity<ApiResponse<*>> {
        val tokens = authService.refresh(refreshToken)
        return ResponseEntity.ok(ApiResponse.ok(tokens))
    }
}
