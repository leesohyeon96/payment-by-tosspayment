package com.shl.payment.common.port

import java.util.UUID

interface StockPort {
    fun reserve(orderId: UUID, productId: UUID, quantity: Long)
    fun confirmReservation(orderId: UUID)
    fun cancelReservation(orderId: UUID)
}
