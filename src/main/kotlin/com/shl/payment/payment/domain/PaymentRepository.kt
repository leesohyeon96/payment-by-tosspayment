package com.shl.payment.payment.domain

import java.time.LocalDateTime
import java.util.Optional
import java.util.UUID

interface PaymentRepository {
    fun save(payment: Payment): Payment
    fun findById(id: UUID): Optional<Payment>
    fun findByOrderId(orderId: UUID): Optional<Payment>
    fun findByPaymentKey(paymentKey: String): Optional<Payment>
    fun findByStatusAndRequestedAtBefore(status: PaymentStatus, before: LocalDateTime): List<Payment>
    fun findByOrderIdIn(orderIds: List<UUID>): List<Payment>
}
