package com.shl.payment.common.outbox

import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ProcessedEventJpaRepository : JpaRepository<ProcessedEvent, String>

@Repository
class ProcessedEventRepositoryImpl(
    private val jpa: ProcessedEventJpaRepository,
    @PersistenceContext private val em: EntityManager,
) : ProcessedEventRepository {
    override fun existsById(id: String): Boolean = jpa.existsById(id)

    // save()는 할당식 @Id를 merge로 처리해 중복을 조용히 무시한다.
    // persist로 PK 위반을 일으켜 중복 처리 트랜잭션이 롤백되게 한다.
    override fun save(event: ProcessedEvent) {
        em.persist(event)
    }
}
