package com.shl.payment.payment.presentation

import com.shl.payment.common.response.ApiResponse
import com.shl.payment.payment.application.PaymentService
import com.shl.payment.payment.application.dto.CancelPaymentRequest
import com.shl.payment.payment.application.dto.ConfirmPaymentRequest
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import java.util.UUID

@RestController
@RequestMapping("/payments")
class PaymentController(
    private val paymentService: PaymentService,
) {
    @PostMapping("/confirm")
    fun confirm(
        @AuthenticationPrincipal userId: String,
        @RequestBody @Valid request: ConfirmPaymentRequest,
    ): ResponseEntity<ApiResponse<*>> {
        val result = paymentService.confirm(
            paymentKey = request.paymentKey,
            orderId = request.orderId,
            amount = request.amount,
            userId = UUID.fromString(userId),
        )
        return ResponseEntity.ok(ApiResponse.ok(result))
    }

    @PostMapping("/{paymentId}/cancel")
    fun cancel(
        @AuthenticationPrincipal userId: String,
        @PathVariable paymentId: UUID,
        @RequestBody @Valid request: CancelPaymentRequest,
    ): ResponseEntity<ApiResponse<*>> {
        val result = paymentService.cancel(paymentId, UUID.fromString(userId), request)
        return ResponseEntity.ok(ApiResponse.ok(result))
    }

    @GetMapping("/{paymentId}")
    fun getPayment(
        @AuthenticationPrincipal userId: String,
        @PathVariable paymentId: UUID,
    ): ResponseEntity<ApiResponse<*>> {
        val result = paymentService.getPayment(paymentId, UUID.fromString(userId))
        return ResponseEntity.ok(ApiResponse.ok(result))
    }

    @GetMapping
    fun getHistory(
        @AuthenticationPrincipal userId: String,
    ): ResponseEntity<ApiResponse<*>> {
        val result = paymentService.getHistory(UUID.fromString(userId))
        return ResponseEntity.ok(ApiResponse.ok(result))
    }
}
