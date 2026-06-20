package com.shl.payment.auth.domain

import java.util.Optional
import java.util.UUID

interface UserRepository {
    fun save(user: User): User
    fun findByEmail(email: String): Optional<User>
    fun existsByEmail(email: String): Boolean
    fun findById(id: UUID): Optional<User>
}
