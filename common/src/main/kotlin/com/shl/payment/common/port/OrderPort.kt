package com.shl.payment.common.port

import java.util.Optional
import java.util.UUID

data class OrderInfo(val id: UUID, val userId: UUID)

interface OrderPort {
    fun findById(orderId: UUID): Optional<OrderInfo>
    fun markPaid(orderId: UUID)
    fun markCancelled(orderId: UUID)
    fun findOrderIdsByUserId(userId: UUID): List<UUID>
}
