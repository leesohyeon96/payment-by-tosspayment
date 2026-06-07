package com.shl.payment.order.infrastructure

import com.shl.payment.order.domain.Order
import com.shl.payment.order.domain.OrderRepository
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.Optional
import java.util.UUID

interface OrderJpaRepository : JpaRepository<Order, UUID> {
    fun findByUserId(userId: UUID): List<Order>
}

@Repository
class OrderRepositoryImpl(
    private val jpaRepository: OrderJpaRepository,
) : OrderRepository {
    override fun save(order: Order) = jpaRepository.save(order)
    override fun findById(id: UUID) = jpaRepository.findById(id)
    override fun findByUserId(userId: UUID) = jpaRepository.findByUserId(userId)
}
