package com.shl.payment.order.application

import com.shl.payment.common.domain.Money
import com.shl.payment.common.domain.Quantity
import com.shl.payment.common.event.OrderCreatedEvent
import com.shl.payment.common.event.OrderItemDto
import com.shl.payment.common.exception.BusinessException
import com.shl.payment.common.outbox.OutboxEventStore
import com.shl.payment.order.application.dto.CreateOrderRequest
import com.shl.payment.order.application.dto.OrderResponse
import com.shl.payment.order.domain.Order
import com.shl.payment.order.domain.OrderItem
import com.shl.payment.order.domain.OrderRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
@Transactional
class OrderService(
    private val orderRepository: OrderRepository,
    private val outboxEventStore: OutboxEventStore,
) {
    fun createOrder(userId: UUID, request: CreateOrderRequest): OrderResponse {
        val totalAmount = Money(request.items.sumOf { it.unitPrice * it.quantity })
        val order = Order(userId = userId, totalAmount = totalAmount, orderName = request.orderName)
        request.items.forEach { item ->
            order.addItem(OrderItem(
                orderId = order.id,
                productId = item.productId,
                quantity = Quantity(item.quantity),
                unitPrice = Money(item.unitPrice),
            ))
        }
        orderRepository.save(order)
        outboxEventStore.store("ORDER_CREATED", OrderCreatedEvent(
            orderId = order.id,
            totalAmount = totalAmount.amount,
            items = request.items.map { OrderItemDto(it.productId, it.quantity) },
        ))
        return OrderResponse.from(order)
    }

    @Transactional(readOnly = true)
    fun getOrder(orderId: UUID, userId: UUID): OrderResponse {
        val order = orderRepository.findById(orderId)
            .orElseThrow { BusinessException("ORDER_NOT_FOUND", "주문을 찾을 수 없습니다") }
        if (order.userId != userId) {
            throw BusinessException("ORDER_ACCESS_DENIED", "접근 권한이 없습니다")
        }
        return OrderResponse.from(order)
    }
}
