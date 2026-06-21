package com.shl.payment.outbox

import com.fasterxml.jackson.databind.ObjectMapper
import com.shl.payment.common.event.OrderCreatedEvent
import com.shl.payment.common.event.PaymentCancelledEvent
import com.shl.payment.common.event.PaymentConfirmedEvent
import com.shl.payment.common.outbox.OutboxEventRepository
import com.shl.payment.common.outbox.OutboxStatus
import org.slf4j.LoggerFactory
import org.springframework.context.ApplicationEventPublisher
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class OutboxEventRelay(
    private val outboxRepository: OutboxEventRepository,
    private val eventPublisher: ApplicationEventPublisher,
    private val objectMapper: ObjectMapper,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @Scheduled(fixedDelay = 5000)
    @Transactional
    fun relay() {
        val pending = outboxRepository.findByStatus(OutboxStatus.PENDING)
        if (pending.isEmpty()) return

        log.debug("Outbox relay: ${pending.size}개 이벤트 처리")

        pending.forEach { outboxEvent ->
            runCatching {
                val domainEvent = deserialize(outboxEvent.eventType, outboxEvent.payload)
                eventPublisher.publishEvent(domainEvent)
                outboxEvent.markPublished()
            }.onFailure {
                log.error("Outbox 이벤트 처리 실패: id=${outboxEvent.id}, type=${outboxEvent.eventType}", it)
                outboxEvent.markFailed()
            }
            outboxRepository.save(outboxEvent)
        }
    }

    private fun deserialize(eventType: String, payload: String): Any = when (eventType) {
        "ORDER_CREATED" -> objectMapper.readValue(payload, OrderCreatedEvent::class.java)
        "PAYMENT_CONFIRMED" -> objectMapper.readValue(payload, PaymentConfirmedEvent::class.java)
        "PAYMENT_CANCELLED" -> objectMapper.readValue(payload, PaymentCancelledEvent::class.java)
        else -> throw IllegalArgumentException("Unknown event type: $eventType")
    }
}
