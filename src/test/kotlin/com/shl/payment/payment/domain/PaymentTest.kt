package com.shl.payment.payment.domain

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.UUID
import org.junit.jupiter.api.Assertions.assertEquals

class PaymentTest {

    private fun readyPayment() = Payment(orderId = UUID.randomUUID(), amount = 10000L)

    private fun donePayment(): Payment {
        val p = readyPayment()
        p.confirm("pay_key", PaymentMethod.CARD)
        return p
    }

    private fun failedPayment(): Payment {
        val p = readyPayment()
        p.fail("실패 이유")
        return p
    }

    @Test
    fun `READY 상태에서 confirm 호출 시 DONE으로 전이`() {
        val p = readyPayment()
        p.confirm("pay_key_123", PaymentMethod.CARD)
        assertEquals(PaymentStatus.DONE, p.status)
        assertEquals("pay_key_123", p.paymentKey)
    }

    @Test
    fun `DONE 상태에서 confirm 호출 시 예외`() {
        val p = donePayment()
        assertThrows<IllegalStateException> { p.confirm("another_key", PaymentMethod.CARD) }
    }

    @Test
    fun `FAILED 상태에서 confirm 호출 시 예외`() {
        val p = failedPayment()
        assertThrows<IllegalStateException> { p.confirm("key", PaymentMethod.CARD) }
    }

    @Test
    fun `DONE 상태에서 전체 cancel 시 CANCELLED로 전이`() {
        val p = donePayment()
        p.cancel(10000L)
        assertEquals(PaymentStatus.CANCELLED, p.status)
        assertEquals(10000L, p.cancelledAmount)
    }

    @Test
    fun `DONE 상태에서 부분 cancel 시 DONE 유지`() {
        val p = donePayment()
        p.cancel(3000L)
        assertEquals(PaymentStatus.DONE, p.status)
        assertEquals(3000L, p.cancelledAmount)
    }

    @Test
    fun `취소 금액 초과 시 예외`() {
        val p = donePayment()
        assertThrows<IllegalStateException> { p.cancel(20000L) }
    }

    @Test
    fun `CANCELLED 상태에서 cancel 시 예외`() {
        val p = donePayment()
        p.cancel(10000L)
        assertThrows<IllegalStateException> { p.cancel(1000L) }
    }

    @Test
    fun `READY 상태에서 fail 시 FAILED로 전이`() {
        val p = readyPayment()
        p.fail("카드 한도 초과")
        assertEquals(PaymentStatus.FAILED, p.status)
        assertEquals("카드 한도 초과", p.failReason)
    }

    @Test
    fun `DONE 상태에서 fail 시 예외`() {
        val p = donePayment()
        assertThrows<IllegalStateException> { p.fail("이유") }
    }
}
