package com.shl.payment.inventory.application

import com.shl.payment.common.exception.BusinessException
import com.shl.payment.inventory.domain.Stock
import com.shl.payment.inventory.domain.StockRepository
import com.shl.payment.inventory.domain.StockReservation
import com.shl.payment.inventory.domain.StockReservationRepository
import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.Optional
import java.util.UUID

class StockServiceTest {

    private val stockRepository = mockk<StockRepository>()
    private val reservationRepository = mockk<StockReservationRepository>()
    private val stockService = StockService(stockRepository, reservationRepository)

    private val productId = UUID.randomUUID()
    private val orderId = UUID.randomUUID()

    @Test
    fun `재고 예약 - 정상`() {
        val stock = Stock(productId, 10L)
        every { stockRepository.findByProductId(productId) } returns Optional.of(stock)
        every { stockRepository.save(any()) } answers { firstArg() }
        every { reservationRepository.save(any()) } answers { firstArg() }

        stockService.reserve(orderId, productId, 3L)

        assertEquals(7L, stock.quantity)
        verify { reservationRepository.save(any()) }
    }

    @Test
    fun `재고 예약 - 재고 부족이면 예외`() {
        val stock = Stock(productId, 2L)
        every { stockRepository.findByProductId(productId) } returns Optional.of(stock)

        assertThrows<IllegalStateException> {
            stockService.reserve(orderId, productId, 5L)
        }
    }

    @Test
    fun `재고 예약 취소 - 재고 복원`() {
        val stock = Stock(productId, 7L)
        val reservation = StockReservation(orderId, productId, 3L)

        every { reservationRepository.findByOrderId(orderId) } returns Optional.of(reservation)
        every { reservationRepository.save(any()) } answers { firstArg() }
        every { stockRepository.findByProductId(productId) } returns Optional.of(stock)
        every { stockRepository.save(any()) } answers { firstArg() }

        stockService.cancelReservation(orderId)

        assertEquals(10L, stock.quantity)
    }

    @Test
    fun `재고 없는 상품 예약 - 예외`() {
        every { stockRepository.findByProductId(any()) } returns Optional.empty()

        assertThrows<BusinessException> {
            stockService.reserve(orderId, productId, 1L)
        }
    }
}
