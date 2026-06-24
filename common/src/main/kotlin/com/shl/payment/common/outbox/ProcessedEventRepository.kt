package com.shl.payment.common.outbox

interface ProcessedEventRepository {
    fun existsById(id: String): Boolean
    fun save(event: ProcessedEvent)
}
