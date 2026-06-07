package com.shl.payment.order.domain

import java.util.Optional
import java.util.UUID

interface OrderRepository {
    fun save(order: Order): Order
    fun findById(id: UUID): Optional<Order>
    fun findByUserId(userId: UUID): List<Order>
}
