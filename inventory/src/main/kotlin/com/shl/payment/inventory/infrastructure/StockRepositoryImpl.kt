package com.shl.payment.inventory.infrastructure

import com.shl.payment.inventory.domain.Stock
import com.shl.payment.inventory.domain.StockRepository
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.Optional
import java.util.UUID

@Repository
interface StockJpaRepository : JpaRepository<Stock, UUID> {
    fun findByProductId(productId: UUID): Optional<Stock>
}

@Repository
class StockRepositoryImpl(private val jpa: StockJpaRepository) : StockRepository {
    override fun save(stock: Stock): Stock = jpa.save(stock)
    override fun findByProductId(productId: UUID): Optional<Stock> = jpa.findByProductId(productId)
}
