package com.shl.payment.auth.domain

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test

class UserTest {

    @Test
    fun `User 생성 시 UUID 자동 발급`() {
        val user = User(email = "test@test.com", password = "hashed_password")
        assertNotNull(user.id)
    }

    @Test
    fun `User 생성 시 이메일과 패스워드 저장`() {
        val user = User(email = "test@test.com", password = "hashed_password")
        assertEquals("test@test.com", user.email)
        assertEquals("hashed_password", user.password)
    }
}
