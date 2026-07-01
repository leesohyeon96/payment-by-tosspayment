package com.shl.payment.inventory.application

import com.shl.payment.common.exception.BusinessException
import com.shl.payment.inventory.domain.Stock
import com.shl.payment.inventory.domain.StockRepository
import com.shl.payment.inventory.domain.StockReservation
import com.shl.payment.inventory.domain.StockReservationRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
@Transactional
class StockService(
    private val stockRepository: StockRepository,
    private val reservationRepository: StockReservationRepository,
) {
    fun reserve(orderId: UUID, productId: UUID, quantity: Long) {
        val stock = stockRepository.findByProductId(productId)
            .orElseThrow { BusinessException("STOCK_NOT_FOUND", "상품 재고를 찾을 수 없습니다") }

        stock.reserve(quantity)
        stockRepository.save(stock)

        reservationRepository.save(StockReservation(orderId, productId, quantity))
    }

    fun confirmReservation(orderId: UUID) {
        val reservation = reservationRepository.findByOrderId(orderId)
            .orElseThrow { BusinessException("RESERVATION_NOT_FOUND", "재고 예약을 찾을 수 없습니다") }
        reservation.confirm()
        reservationRepository.save(reservation)
    }

    fun cancelReservation(orderId: UUID) {
        val reservation = reservationRepository.findByOrderId(orderId)
            .orElse(null) ?: return

        reservation.cancel()
        reservationRepository.save(reservation)

        val stock = stockRepository.findByProductId(reservation.productId)
            .orElse(null) ?: return
        stock.restore(reservation.quantity)
        stockRepository.save(stock)
    }

    fun addStock(productId: UUID, quantity: Long): Stock {
        val stock = stockRepository.findByProductId(productId)
            .orElse(Stock(productId, 0L))
        stock.restore(quantity)
        return stockRepository.save(stock)
    }
}
