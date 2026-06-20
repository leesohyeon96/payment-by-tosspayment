package com.shl.payment.common.port

import java.util.UUID

interface PaymentCreationPort {
    fun createPayment(orderId: UUID, amount: Long)
}
