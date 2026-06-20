package com.shl.payment.inventory.infrastructure

import com.shl.payment.common.port.StockPort
import com.shl.payment.inventory.application.StockService
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class StockPortAdapter(private val stockService: StockService) : StockPort {
    override fun reserve(orderId: UUID, productId: UUID, quantity: Long) =
        stockService.reserve(orderId, productId, quantity)

    override fun confirmReservation(orderId: UUID) =
        stockService.confirmReservation(orderId)

    override fun cancelReservation(orderId: UUID) =
        stockService.cancelReservation(orderId)
}
