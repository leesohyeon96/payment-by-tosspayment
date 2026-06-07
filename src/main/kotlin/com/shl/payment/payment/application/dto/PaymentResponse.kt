package com.shl.payment.payment.application.dto

import com.shl.payment.payment.domain.Payment
import com.shl.payment.payment.domain.PaymentMethod
import com.shl.payment.payment.domain.PaymentStatus
import java.time.LocalDateTime
import java.util.UUID

data class PaymentResponse(
    val paymentId: UUID,
    val orderId: UUID,
    val paymentKey: String?,
    val method: PaymentMethod?,
    val amount: Long,
    val cancelledAmount: Long,
    val status: PaymentStatus,
    val requestedAt: LocalDateTime,
    val approvedAt: LocalDateTime?,
) {
    companion object {
        fun from(payment: Payment) = PaymentResponse(
            paymentId = payment.id,
            orderId = payment.orderId,
            paymentKey = payment.paymentKey,
            method = payment.method,
            amount = payment.amount,
            cancelledAmount = payment.cancelledAmount,
            status = payment.status,
            requestedAt = payment.requestedAt,
            approvedAt = payment.approvedAt,
        )
    }
}
