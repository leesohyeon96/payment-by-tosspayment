package com.shl.payment.common.outbox

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface OutboxJpaRepository : JpaRepository<OutboxEvent, UUID> {
    fun findByStatus(status: OutboxStatus): List<OutboxEvent>
}

@Repository
class OutboxEventRepositoryImpl(private val jpa: OutboxJpaRepository) : OutboxEventRepository {
    override fun save(event: OutboxEvent): OutboxEvent = jpa.save(event)
    override fun findByStatus(status: OutboxStatus): List<OutboxEvent> = jpa.findByStatus(status)
}
