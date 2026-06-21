package com.shl.payment.common.outbox

interface OutboxEventRepository {
    fun save(event: OutboxEvent): OutboxEvent
    fun findByStatus(status: OutboxStatus): List<OutboxEvent>
}
