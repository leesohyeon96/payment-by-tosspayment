package com.shl.payment.order.infrastructure

import com.shl.payment.common.port.OrderInfo
import com.shl.payment.common.port.OrderPort
import com.shl.payment.order.domain.OrderRepository
import org.springframework.stereotype.Component
import java.util.Optional
import java.util.UUID

@Component
class OrderPortAdapter(private val orderRepository: OrderRepository) : OrderPort {

    override fun findById(orderId: UUID): Optional<OrderInfo> =
        orderRepository.findById(orderId).map { OrderInfo(it.id, it.userId) }

    override fun findOrderIdsByUserId(userId: UUID): List<UUID> =
        orderRepository.findByUserId(userId).map { it.id }
}
