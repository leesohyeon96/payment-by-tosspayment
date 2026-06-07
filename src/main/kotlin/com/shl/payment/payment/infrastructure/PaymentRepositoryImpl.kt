package com.shl.payment.payment.infrastructure

import com.shl.payment.payment.domain.Payment
import com.shl.payment.payment.domain.PaymentRepository
import com.shl.payment.payment.domain.PaymentStatus
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.time.LocalDateTime
import java.util.Optional
import java.util.UUID

interface PaymentJpaRepository : JpaRepository<Payment, UUID> {
    fun findByOrderId(orderId: UUID): Optional<Payment>
    fun findByPaymentKey(paymentKey: String): Optional<Payment>
    fun findByStatusAndRequestedAtBefore(status: PaymentStatus, before: LocalDateTime): List<Payment>
}

@Repository
class PaymentRepositoryImpl(
    private val jpaRepository: PaymentJpaRepository,
) : PaymentRepository {
    override fun save(payment: Payment) = jpaRepository.save(payment)
    override fun findById(id: UUID) = jpaRepository.findById(id)
    override fun findByOrderId(orderId: UUID) = jpaRepository.findByOrderId(orderId)
    override fun findByPaymentKey(paymentKey: String) = jpaRepository.findByPaymentKey(paymentKey)
    override fun findByStatusAndRequestedAtBefore(status: PaymentStatus, before: LocalDateTime) =
        jpaRepository.findByStatusAndRequestedAtBefore(status, before)
}
