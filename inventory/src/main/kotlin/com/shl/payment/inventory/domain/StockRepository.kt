package com.shl.payment.inventory.domain

import java.util.Optional
import java.util.UUID

interface StockRepository {
    fun save(stock: Stock): Stock
    fun findByProductId(productId: UUID): Optional<Stock>
}
