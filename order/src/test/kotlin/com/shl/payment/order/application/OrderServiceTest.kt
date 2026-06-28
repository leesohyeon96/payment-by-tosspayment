package com.shl.payment.order.application

import com.shl.payment.common.event.OrderCreatedEvent
import com.shl.payment.common.exception.BusinessException
import com.shl.payment.common.outbox.OutboxEventStore
import com.shl.payment.order.domain.Order
import com.shl.payment.order.domain.OrderRepository
import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.Optional
import java.util.UUID

class OrderServiceTest {

    private val orderRepository = mockk<OrderRepository>()
    private val outboxEventStore = mockk<OutboxEventStore>()
    private val orderService = OrderService(orderRepository, outboxEventStore)
    private val userId = UUID.randomUUID()

    @Test
    fun `주문 생성 - 정상`() {
        every { orderRepository.save(any()) } answers { firstArg() }
        justRun { outboxEventStore.store(any(), any()) }

        val result = orderService.createOrder(userId, "테스트 상품", 10000L)

        assertEquals("테스트 상품", result.orderName)
        assertEquals(10000L, result.totalAmount)
        verify { outboxEventStore.store("ORDER_CREATED", any<OrderCreatedEvent>()) }
    }

    @Test
    fun `주문 조회 - 없으면 예외`() {
        every { orderRepository.findById(any()) } returns Optional.empty()

        assertThrows<BusinessException> {
            orderService.getOrder(UUID.randomUUID(), userId)
        }
    }

    @Test
    fun `주문 조회 - 다른 사용자 주문이면 예외`() {
        val order = Order(userId = UUID.randomUUID(), totalAmount = 10000L, orderName = "상품")
        every { orderRepository.findById(order.id) } returns Optional.of(order)

        assertThrows<BusinessException> {
            orderService.getOrder(order.id, userId)
        }
    }
}
