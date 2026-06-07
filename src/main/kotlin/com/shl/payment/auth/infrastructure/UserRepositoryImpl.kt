package com.shl.payment.auth.infrastructure

import com.shl.payment.auth.domain.User
import com.shl.payment.auth.domain.UserRepository
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.Optional
import java.util.UUID

interface UserJpaRepository : JpaRepository<User, UUID> {
    fun findByEmail(email: String): Optional<User>
    fun existsByEmail(email: String): Boolean
}

@Repository
class UserRepositoryImpl(
    private val jpaRepository: UserJpaRepository,
) : UserRepository {
    override fun save(user: User) = jpaRepository.save(user)
    override fun findByEmail(email: String) = jpaRepository.findByEmail(email)
    override fun existsByEmail(email: String) = jpaRepository.existsByEmail(email)
    override fun findById(id: UUID) = jpaRepository.findById(id)
}
