package com.shl.payment.order.application

import com.shl.payment.common.exception.BusinessException
import com.shl.payment.common.port.PaymentCreationPort
import com.shl.payment.order.application.dto.OrderResponse
import com.shl.payment.order.domain.Order
import com.shl.payment.order.domain.OrderRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
@Transactional
class OrderService(
    private val orderRepository: OrderRepository,
    private val paymentCreationPort: PaymentCreationPort,
) {
    fun createOrder(userId: UUID, orderName: String, totalAmount: Long): OrderResponse {
        val order = Order(userId = userId, totalAmount = totalAmount, orderName = orderName)
        orderRepository.save(order)
        paymentCreationPort.createPayment(order.id, totalAmount)
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
