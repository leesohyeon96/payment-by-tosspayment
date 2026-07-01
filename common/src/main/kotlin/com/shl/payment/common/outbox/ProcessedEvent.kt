package com.shl.payment.common.outbox

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.LocalDateTime

@Entity
@Table(name = "processed_events")
class ProcessedEvent(
    @Id val id: String,
    @Column(updatable = false) val processedAt: LocalDateTime = LocalDateTime.now(),
)
