package com.shl.payment.common.outbox

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ProcessedEventJpaRepository : JpaRepository<ProcessedEvent, String>

@Repository
class ProcessedEventRepositoryImpl(private val jpa: ProcessedEventJpaRepository) : ProcessedEventRepository {
    override fun existsById(id: String): Boolean = jpa.existsById(id)
    override fun save(event: ProcessedEvent) { jpa.save(event) }
}
