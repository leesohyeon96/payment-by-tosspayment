package com.shl.payment.common.outbox

import jakarta.persistence.*
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(name = "outbox_events")
@EntityListeners(AuditingEntityListener::class)
class OutboxEvent(
    eventType: String,
    payload: String,
) {
    @Id
    val id: UUID = UUID.randomUUID()

    @Column(nullable = false)
    val eventType: String = eventType

    @Column(nullable = false, columnDefinition = "TEXT")
    val payload: String = payload

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: OutboxStatus = OutboxStatus.PENDING
        private set

    @CreatedDate
    @Column(updatable = false)
    var createdAt: LocalDateTime = LocalDateTime.now()
        private set

    var publishedAt: LocalDateTime? = null
        private set

    fun markPublished() {
        status = OutboxStatus.PUBLISHED
        publishedAt = LocalDateTime.now()
    }

    fun markFailed() {
        status = OutboxStatus.FAILED
    }
}

enum class OutboxStatus { PENDING, PUBLISHED, FAILED }
