package com.shl.payment.common.outbox

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.stereotype.Component

@Component
class OutboxEventStore(
    private val repository: OutboxEventRepository,
    private val objectMapper: ObjectMapper,
) {
    fun store(eventType: String, event: Any) {
        val payload = objectMapper.writeValueAsString(event)
        repository.save(OutboxEvent(eventType = eventType, payload = payload))
    }
}
