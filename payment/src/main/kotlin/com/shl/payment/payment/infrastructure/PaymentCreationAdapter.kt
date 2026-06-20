package com.shl.payment.payment.infrastructure

import com.shl.payment.common.port.PaymentCreationPort
import com.shl.payment.payment.domain.Payment
import com.shl.payment.payment.domain.PaymentRepository
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class PaymentCreationAdapter(private val paymentRepository: PaymentRepository) : PaymentCreationPort {
    override fun createPayment(orderId: UUID, amount: Long) {
        paymentRepository.save(Payment(orderId = orderId, amount = amount))
    }
}
