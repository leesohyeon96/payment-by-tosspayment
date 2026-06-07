package com.shl.payment.payment.application

import com.shl.payment.common.exception.PaymentException
import com.shl.payment.order.domain.Order
import com.shl.payment.order.domain.OrderRepository
import com.shl.payment.payment.domain.Payment
import com.shl.payment.payment.domain.PaymentMethod
import com.shl.payment.payment.domain.PaymentRepository
import com.shl.payment.payment.domain.PaymentStatus
import com.shl.payment.payment.infrastructure.TossPaymentClient
import com.shl.payment.payment.infrastructure.dto.TossConfirmResponse
import io.mockk.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.Optional
import java.util.UUID
import org.junit.jupiter.api.Assertions.assertEquals

class PaymentServiceTest {

    private val paymentRepository = mockk<PaymentRepository>()
    private val orderRepository = mockk<OrderRepository>()
    private val tossClient = mockk<TossPaymentClient>()
    private val paymentService = PaymentService(paymentRepository, orderRepository, tossClient, maxRetry = 1)

    private val userId = UUID.randomUUID()
    private val orderId = UUID.randomUUID()

    private fun orderOf(userId: UUID = this.userId) =
        Order(userId = userId, totalAmount = 10000L, orderName = "테스트")

    private fun readyPayment() = Payment(orderId = orderId, amount = 10000L)

    @Test
    fun `결제 승인 - 정상 플로우`() {
        val order = orderOf()
        val payment = readyPayment()

        every { orderRepository.findById(orderId) } returns Optional.of(order)
        every { paymentRepository.findByOrderId(orderId) } returns Optional.of(payment)
        every { paymentRepository.save(any()) } answers { firstArg() }
        every { orderRepository.save(any()) } answers { firstArg() }
        every { tossClient.confirm("pay_key", orderId.toString(), 10000L) } returns
            TossConfirmResponse("pay_key", orderId.toString(), "DONE", "카드", 10000L)

        val result = paymentService.confirm("pay_key", orderId, 10000L, userId)

        assertEquals(PaymentStatus.DONE, result.status)
        verify { tossClient.confirm("pay_key", orderId.toString(), 10000L) }
    }

    @Test
    fun `결제 승인 - 이미 DONE이면 멱등 처리 (토스 API 호출 없음)`() {
        val order = orderOf()
        val payment = readyPayment().also { it.confirm("pay_key", PaymentMethod.CARD) }

        every { orderRepository.findById(orderId) } returns Optional.of(order)
        every { paymentRepository.findByOrderId(orderId) } returns Optional.of(payment)

        val result = paymentService.confirm("pay_key", orderId, 10000L, userId)

        assertEquals(PaymentStatus.DONE, result.status)
        verify(exactly = 0) { tossClient.confirm(any(), any(), any()) }
    }

    @Test
    fun `결제 승인 - 금액 불일치 시 예외 (토스 API 호출 없음)`() {
        val order = orderOf()
        val payment = readyPayment()

        every { orderRepository.findById(orderId) } returns Optional.of(order)
        every { paymentRepository.findByOrderId(orderId) } returns Optional.of(payment)

        assertThrows<PaymentException> {
            paymentService.confirm("pay_key", orderId, 5000L, userId)
        }
        verify(exactly = 0) { tossClient.confirm(any(), any(), any()) }
    }

    @Test
    fun `결제 승인 - 다른 사용자 주문이면 예외`() {
        val order = orderOf(userId = UUID.randomUUID())
        val payment = readyPayment()

        every { orderRepository.findById(orderId) } returns Optional.of(order)
        every { paymentRepository.findByOrderId(orderId) } returns Optional.of(payment)

        assertThrows<Exception> {
            paymentService.confirm("pay_key", orderId, 10000L, userId)
        }
    }
}
