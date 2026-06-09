package com.shl.payment.payment.application

import com.shl.payment.common.exception.BusinessException
import com.shl.payment.common.exception.PaymentException
import com.shl.payment.order.domain.OrderRepository
import com.shl.payment.payment.application.dto.CancelPaymentRequest
import com.shl.payment.payment.application.dto.PaymentResponse
import com.shl.payment.payment.domain.PaymentMethod
import com.shl.payment.payment.domain.PaymentRepository
import com.shl.payment.payment.domain.PaymentStatus
import com.shl.payment.payment.infrastructure.TossPaymentClient
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
@Transactional
class PaymentService(
    private val paymentRepository: PaymentRepository,
    private val orderRepository: OrderRepository,
    private val tossClient: TossPaymentClient,
    private val maxRetry: Int = 3,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    fun confirm(paymentKey: String, orderId: UUID, amount: Long, userId: UUID): PaymentResponse {
        val order = orderRepository.findById(orderId)
            .orElseThrow { BusinessException("ORDER_NOT_FOUND", "주문을 찾을 수 없습니다") }

        if (order.userId != userId) throw BusinessException("ORDER_ACCESS_DENIED", "접근 권한이 없습니다")

        val payment = paymentRepository.findByOrderId(orderId)
            .orElseThrow { PaymentException.notFound() }

        if (payment.status == PaymentStatus.DONE) {
            return PaymentResponse.from(payment)
        }

        if (payment.status != PaymentStatus.READY) {
            throw PaymentException.invalidStatus(payment.status.name)
        }

        if (payment.amount != amount) {
            throw PaymentException("AMOUNT_MISMATCH", "결제 금액이 주문 금액과 다릅니다")
        }

        return confirmWithRetry(payment, paymentKey, orderId, amount, order)
    }

    private fun confirmWithRetry(
        payment: com.shl.payment.payment.domain.Payment,
        paymentKey: String,
        orderId: UUID,
        amount: Long,
        order: com.shl.payment.order.domain.Order,
    ): PaymentResponse {
        var lastException: Exception? = null

        repeat(maxRetry) { attempt ->
            try {
                val response = tossClient.confirm(paymentKey, orderId.toString(), amount)
                val method = runCatching { PaymentMethod.valueOf(response.method?.uppercase() ?: "CARD") }
                    .getOrDefault(PaymentMethod.CARD)
                payment.confirm(paymentKey, method)
                paymentRepository.save(payment)
                order.markPaid()
                orderRepository.save(order)
                return PaymentResponse.from(payment)
            } catch (e: PaymentException) {
                throw e
            } catch (e: Exception) {
                lastException = e
                val delay = (1L shl attempt) * 500L
                log.warn("결제 승인 재시도 ${attempt + 1}/$maxRetry, delay=${delay}ms", e)
                Thread.sleep(delay)
            }
        }

        log.error("결제 승인 재시도 전부 실패, 토스 취소 요청")
        runCatching { tossClient.cancel(paymentKey, "DB 저장 실패로 인한 자동 취소") }
            .onFailure { log.error("토스 취소 요청도 실패", it) }
        payment.fail("재시도 초과: ${lastException?.message}")
        paymentRepository.save(payment)
        throw PaymentException("PAYMENT_CONFIRM_FAILED", "결제 처리 중 오류가 발생했습니다")
    }

    fun cancel(paymentId: UUID, userId: UUID, request: CancelPaymentRequest): PaymentResponse {
        val payment = paymentRepository.findById(paymentId)
            .orElseThrow { PaymentException.notFound() }

        val order = orderRepository.findById(payment.orderId)
            .orElseThrow { BusinessException("ORDER_NOT_FOUND", "주문을 찾을 수 없습니다") }

        if (order.userId != userId) throw BusinessException("ORDER_ACCESS_DENIED", "접근 권한이 없습니다")

        val cancelAmount = request.cancelAmount ?: payment.amount
        if (payment.cancelledAmount + cancelAmount > payment.amount) {
            throw PaymentException.cancelAmountExceeded()
        }

        tossClient.cancel(
            paymentKey = payment.paymentKey!!,
            cancelReason = request.cancelReason,
            cancelAmount = request.cancelAmount,
        )

        payment.cancel(cancelAmount)
        if (payment.status == PaymentStatus.CANCELLED) {
            order.markCancelled()
            orderRepository.save(order)
        }
        paymentRepository.save(payment)
        return PaymentResponse.from(payment)
    }

    @Transactional(readOnly = true)
    fun getPayment(paymentId: UUID, userId: UUID): PaymentResponse {
        val payment = paymentRepository.findById(paymentId)
            .orElseThrow { PaymentException.notFound() }
        val order = orderRepository.findById(payment.orderId)
            .orElseThrow { BusinessException("ORDER_NOT_FOUND", "주문 없음") }
        if (order.userId != userId) throw BusinessException("ORDER_ACCESS_DENIED", "접근 권한 없음")
        return PaymentResponse.from(payment)
    }

    @Transactional(readOnly = true)
    fun getHistory(userId: UUID): List<PaymentResponse> {
        val orderIds = orderRepository.findByUserId(userId).map { it.id }
        if (orderIds.isEmpty()) return emptyList()
        return paymentRepository.findByOrderIdIn(orderIds)
            .sortedByDescending { it.requestedAt }
            .map { PaymentResponse.from(it) }
    }
}
