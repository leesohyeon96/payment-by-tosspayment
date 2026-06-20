package com.shl.payment.inventory.infrastructure

import com.shl.payment.inventory.domain.StockReservation
import com.shl.payment.inventory.domain.StockReservationRepository
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.Optional
import java.util.UUID

@Repository
interface StockReservationJpaRepository : JpaRepository<StockReservation, UUID> {
    fun findByOrderId(orderId: UUID): Optional<StockReservation>
}

@Repository
class StockReservationRepositoryImpl(private val jpa: StockReservationJpaRepository) : StockReservationRepository {
    override fun save(reservation: StockReservation): StockReservation = jpa.save(reservation)
    override fun findByOrderId(orderId: UUID): Optional<StockReservation> = jpa.findByOrderId(orderId)
}
