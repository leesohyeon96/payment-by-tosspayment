package com.shl.payment.auth.domain

import jakarta.persistence.*
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(name = "users")
@EntityListeners(AuditingEntityListener::class)
class User(
    email: String,
    password: String,
) {
    @Id
    val id: UUID = UUID.randomUUID()

    @Column(unique = true, nullable = false)
    var email: String = email
        private set

    @Column(nullable = false)
    var password: String = password
        private set

    @CreatedDate
    @Column(updatable = false)
    var createdAt: LocalDateTime = LocalDateTime.now()
        private set
}
