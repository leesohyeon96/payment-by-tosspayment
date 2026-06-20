package com.shl.payment.inventory.domain

import java.util.Optional
import java.util.UUID

interface StockReservationRepository {
    fun save(reservation: StockReservation): StockReservation
    fun findByOrderId(orderId: UUID): Optional<StockReservation>
}
