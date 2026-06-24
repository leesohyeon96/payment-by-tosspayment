package com.shl.payment.order.presentation

import com.shl.payment.common.response.ApiResponse
import com.shl.payment.order.application.OrderService
import com.shl.payment.order.application.dto.CreateOrderRequest
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import java.util.UUID

@RestController
@RequestMapping("/orders")
class OrderController(
    private val orderService: OrderService,
) {
    @PostMapping
    fun createOrder(
        @AuthenticationPrincipal userId: String,
        @RequestBody @Valid request: CreateOrderRequest,
    ): ResponseEntity<ApiResponse<*>> {
        val result = orderService.createOrder(
            userId = UUID.fromString(userId),
            request = request,
        )
        return ResponseEntity.ok(ApiResponse.ok(result))
    }

    @GetMapping("/{orderId}")
    fun getOrder(
        @AuthenticationPrincipal userId: String,
        @PathVariable orderId: UUID,
    ): ResponseEntity<ApiResponse<*>> {
        val result = orderService.getOrder(orderId, UUID.fromString(userId))
        return ResponseEntity.ok(ApiResponse.ok(result))
    }
}
