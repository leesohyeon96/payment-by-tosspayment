# Payment System Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Kotlin + Spring Boot 결제 시스템 — JWT 인증, 주문 생성, 토스페이먼츠 결제 승인/환불, Webhook 안전망, 배치 스케줄러까지 실무 수준으로 완성

**Architecture:** Modular Monolith + DDD. `auth` / `order` / `payment` 도메인 각각 `domain / application / infrastructure / presentation` 4레이어로 분리. 도메인 레이어는 인프라를 모르고 인터페이스에만 의존.

**Tech Stack:** Kotlin 1.9, Spring Boot 3.3, Spring Security 6, Spring Data JPA, PostgreSQL (Neon), jjwt 0.12, Thymeleaf, WebClient, Render 배포

---

## 파일 구조

```
payment-by-tosspayments/
├── build.gradle.kts
├── settings.gradle.kts
├── src/
│   ├── main/
│   │   ├── kotlin/com/shl/payment/
│   │   │   ├── PaymentApplication.kt
│   │   │   ├── common/
│   │   │   │   ├── config/SecurityConfig.kt
│   │   │   │   ├── config/WebClientConfig.kt
│   │   │   │   ├── exception/BusinessException.kt
│   │   │   │   ├── exception/PaymentException.kt
│   │   │   │   ├── exception/GlobalExceptionHandler.kt
│   │   │   │   └── response/ApiResponse.kt
│   │   │   ├── auth/
│   │   │   │   ├── domain/User.kt
│   │   │   │   ├── domain/UserRepository.kt
│   │   │   │   ├── domain/RefreshToken.kt
│   │   │   │   ├── domain/RefreshTokenRepository.kt
│   │   │   │   ├── infrastructure/UserRepositoryImpl.kt
│   │   │   │   ├── infrastructure/RefreshTokenRepositoryImpl.kt
│   │   │   │   ├── infrastructure/JwtProvider.kt
│   │   │   │   ├── infrastructure/JwtAuthenticationFilter.kt
│   │   │   │   ├── application/AuthService.kt
│   │   │   │   ├── application/dto/SignupRequest.kt
│   │   │   │   ├── application/dto/LoginRequest.kt
│   │   │   │   ├── application/dto/TokenResponse.kt
│   │   │   │   └── presentation/AuthController.kt
│   │   │   ├── order/
│   │   │   │   ├── domain/Order.kt
│   │   │   │   ├── domain/OrderStatus.kt
│   │   │   │   ├── domain/OrderRepository.kt
│   │   │   │   ├── infrastructure/OrderRepositoryImpl.kt
│   │   │   │   ├── application/OrderService.kt
│   │   │   │   ├── application/dto/CreateOrderRequest.kt
│   │   │   │   ├── application/dto/OrderResponse.kt
│   │   │   │   └── presentation/OrderController.kt
│   │   │   └── payment/
│   │   │       ├── domain/Payment.kt
│   │   │       ├── domain/PaymentStatus.kt
│   │   │       ├── domain/PaymentMethod.kt
│   │   │       ├── domain/PaymentRepository.kt
│   │   │       ├── domain/vo/Money.kt
│   │   │       ├── infrastructure/PaymentRepositoryImpl.kt
│   │   │       ├── infrastructure/TossPaymentClient.kt
│   │   │       ├── infrastructure/dto/TossConfirmRequest.kt
│   │   │       ├── infrastructure/dto/TossConfirmResponse.kt
│   │   │       ├── infrastructure/dto/TossCancelRequest.kt
│   │   │       ├── infrastructure/dto/TossCancelResponse.kt
│   │   │       ├── application/PaymentService.kt
│   │   │       ├── application/PaymentScheduler.kt
│   │   │       ├── application/dto/ConfirmPaymentRequest.kt
│   │   │       ├── application/dto/CancelPaymentRequest.kt
│   │   │       ├── application/dto/PaymentResponse.kt
│   │   │       └── presentation/PaymentController.kt
│   │   │       └── presentation/WebhookController.kt
│   │   └── resources/
│   │       ├── application.yml
│   │       ├── application-local.yml
│   │       ├── application-prod.yml
│   │       └── templates/
│   │           ├── index.html
│   │           ├── auth/signup.html
│   │           ├── auth/login.html
│   │           ├── order/create.html
│   │           ├── payment/checkout.html
│   │           ├── payment/success.html
│   │           ├── payment/fail.html
│   │           └── payment/history.html
│   └── test/
│       ├── kotlin/com/shl/payment/
│       │   ├── auth/domain/UserTest.kt
│       │   ├── auth/application/AuthServiceTest.kt
│       │   ├── order/application/OrderServiceTest.kt
│       │   ├── payment/domain/PaymentTest.kt
│       │   └── payment/application/PaymentServiceTest.kt
│       └── resources/application.yml
```

---

### Task 1: 프로젝트 초기 설정

**Files:**
- Create: `build.gradle.kts`
- Create: `settings.gradle.kts`
- Create: `src/main/kotlin/com/shl/payment/PaymentApplication.kt`
- Create: `src/main/resources/application.yml`
- Create: `src/main/resources/application-local.yml`
- Create: `src/main/resources/application-prod.yml`
- Create: `src/test/resources/application.yml`

- [ ] **Step 1: IntelliJ에서 Spring Boot 프로젝트 생성**

IntelliJ → New Project → Spring Initializr:
- Language: Kotlin
- Type: Gradle - Kotlin
- Group: `com.shl`
- Artifact: `payment`
- Java: 21
- Dependencies: Spring Web, Spring Data JPA, Spring Security, Thymeleaf, Validation

- [ ] **Step 2: build.gradle.kts 전체 교체**

```kotlin
plugins {
    id("org.springframework.boot") version "3.3.0"
    id("io.spring.dependency-management") version "1.1.5"
    kotlin("jvm") version "1.9.24"
    kotlin("plugin.spring") version "1.9.24"
    kotlin("plugin.jpa") version "1.9.24"
}

group = "com.shl"
version = "0.0.1-SNAPSHOT"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-thymeleaf")
    implementation("org.thymeleaf.extras:thymeleaf-extras-springsecurity6")
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-reflect")

    // JWT
    implementation("io.jsonwebtoken:jjwt-api:0.12.6")
    runtimeOnly("io.jsonwebtoken:jjwt-impl:0.12.6")
    runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.12.6")

    // DB
    runtimeOnly("org.postgresql:postgresql")

    // Test
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.security:spring-security-test")
    testImplementation("com.h2database:h2")
    testImplementation("io.mockk:mockk:1.13.10")
    testImplementation("com.ninja-squad:springmockk:4.0.2")
}

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll("-Xjsr305=strict")
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}
```

- [ ] **Step 3: application.yml 작성**

```yaml
spring:
  profiles:
    active: local
  jpa:
    hibernate:
      ddl-auto: validate
    show-sql: false
    open-in-view: false
  datasource:
    url: ${DB_URL}
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}

jwt:
  secret: ${JWT_SECRET}
  access-expiration: 1800000
  refresh-expiration: 604800000

toss:
  secret-key: ${TOSS_SECRET_KEY}
  base-url: https://api.tosspayments.com
  webhook-secret: ${TOSS_WEBHOOK_SECRET}

logging:
  level:
    com.shl.payment: DEBUG
```

- [ ] **Step 4: application-local.yml 작성**

```yaml
spring:
  jpa:
    hibernate:
      ddl-auto: create-drop
    show-sql: true
  datasource:
    url: jdbc:postgresql://localhost:5432/payment_local
    username: postgres
    password: postgres
    driver-class-name: org.postgresql.Driver

jwt:
  secret: local-secret-key-must-be-at-least-32-characters-long-for-hs256

toss:
  secret-key: test_sk_zXLkKEypNArWmo50nX3lmeaxYG5R  # 토스 테스트 키 (예시, 실제 발급받은 키로 교체)
  webhook-secret: local_webhook_secret
```

- [ ] **Step 5: application-prod.yml 작성**

```yaml
spring:
  jpa:
    hibernate:
      ddl-auto: update
```

- [ ] **Step 6: 테스트용 application.yml 작성** (`src/test/resources/application.yml`)

```yaml
spring:
  jpa:
    hibernate:
      ddl-auto: create-drop
  datasource:
    url: jdbc:h2:mem:testdb;MODE=PostgreSQL;DB_CLOSE_DELAY=-1
    username: sa
    password:
    driver-class-name: org.h2.Driver

jwt:
  secret: test-secret-key-must-be-at-least-32-characters-long-for-hs256
  access-expiration: 1800000
  refresh-expiration: 604800000

toss:
  secret-key: test_sk_dummy
  base-url: http://localhost  # 테스트에서 MockWebServer로 교체
  webhook-secret: test_webhook_secret
```

- [ ] **Step 7: PaymentApplication.kt 작성**

```kotlin
package com.shl.payment

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@EnableScheduling
class PaymentApplication

fun main(args: Array<String>) {
    runApplication<PaymentApplication>(*args)
}
```

- [ ] **Step 8: 빌드 확인**

```bash
./gradlew build -x test
```

Expected: `BUILD SUCCESSFUL`

- [ ] **Step 9: Commit**

```bash
git add build.gradle.kts settings.gradle.kts src/main/kotlin/com/shl/payment/PaymentApplication.kt src/main/resources/ src/test/resources/
git commit -m "feat: initial project setup"
```

---

### Task 2: Common Layer

**Files:**
- Create: `src/main/kotlin/com/shl/payment/common/response/ApiResponse.kt`
- Create: `src/main/kotlin/com/shl/payment/common/exception/BusinessException.kt`
- Create: `src/main/kotlin/com/shl/payment/common/exception/PaymentException.kt`
- Create: `src/main/kotlin/com/shl/payment/common/exception/GlobalExceptionHandler.kt`

- [ ] **Step 1: ApiResponse.kt 작성**

```kotlin
package com.shl.payment.common.response

data class ApiResponse<T>(
    val success: Boolean,
    val data: T? = null,
    val error: ErrorResponse? = null,
) {
    companion object {
        fun <T> ok(data: T) = ApiResponse(success = true, data = data)
        fun ok() = ApiResponse<Unit>(success = true)
        fun fail(error: ErrorResponse) = ApiResponse<Unit>(success = false, error = error)
    }
}

data class ErrorResponse(
    val code: String,
    val message: String,
)
```

- [ ] **Step 2: BusinessException.kt 작성**

```kotlin
package com.shl.payment.common.exception

open class BusinessException(
    val code: String,
    override val message: String,
) : RuntimeException(message)
```

- [ ] **Step 3: PaymentException.kt 작성**

```kotlin
package com.shl.payment.common.exception

class PaymentException(
    code: String,
    message: String,
) : BusinessException(code, message) {

    companion object {
        fun alreadyDone() = PaymentException("PAYMENT_ALREADY_DONE", "이미 완료된 결제입니다")
        fun invalidStatus(status: String) = PaymentException("PAYMENT_INVALID_STATUS", "결제 상태가 올바르지 않습니다: $status")
        fun notFound() = PaymentException("PAYMENT_NOT_FOUND", "결제 정보를 찾을 수 없습니다")
        fun cancelAmountExceeded() = PaymentException("PAYMENT_CANCEL_AMOUNT_EXCEEDED", "취소 금액이 결제 금액을 초과합니다")
        fun tossFailed(message: String) = PaymentException("TOSS_API_FAILED", "토스 API 오류: $message")
    }
}
```

- [ ] **Step 4: GlobalExceptionHandler.kt 작성**

```kotlin
package com.shl.payment.common.exception

import com.shl.payment.common.response.ApiResponse
import com.shl.payment.common.response.ErrorResponse
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalExceptionHandler {

    private val log = LoggerFactory.getLogger(javaClass)

    @ExceptionHandler(BusinessException::class)
    fun handleBusinessException(e: BusinessException): ResponseEntity<ApiResponse<Unit>> {
        log.warn("BusinessException: code=${e.code}, message=${e.message}")
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(ApiResponse.fail(ErrorResponse(e.code, e.message)))
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidation(e: MethodArgumentNotValidException): ResponseEntity<ApiResponse<Unit>> {
        val message = e.bindingResult.fieldErrors.joinToString { "${it.field}: ${it.defaultMessage}" }
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(ApiResponse.fail(ErrorResponse("VALIDATION_FAILED", message)))
    }

    @ExceptionHandler(Exception::class)
    fun handleException(e: Exception): ResponseEntity<ApiResponse<Unit>> {
        log.error("Unhandled exception", e)
        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ApiResponse.fail(ErrorResponse("INTERNAL_ERROR", "서버 오류가 발생했습니다")))
    }
}
```

- [ ] **Step 5: 테스트 작성** (`src/test/kotlin/com/shl/payment/common/exception/GlobalExceptionHandlerTest.kt`)

```kotlin
package com.shl.payment.common.exception

import com.ninjasquad.springmockk.MockkBean
import com.shl.payment.auth.infrastructure.JwtProvider
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@WebMvcTest(GlobalExceptionHandlerTest.TestController::class)
@Import(GlobalExceptionHandler::class)
class GlobalExceptionHandlerTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @MockkBean
    lateinit var jwtProvider: JwtProvider

    @RestController
    class TestController {
        @GetMapping("/test/error")
        fun error(): Nothing = throw BusinessException("TEST_CODE", "테스트 에러")
    }

    @Test
    fun `BusinessException은 400 반환`() {
        mockMvc.get("/test/error")
            .andExpect {
                status { isBadRequest() }
                jsonPath("$.success") { value(false) }
                jsonPath("$.error.code") { value("TEST_CODE") }
            }
    }
}
```

- [ ] **Step 6: Commit**

```bash
git add src/main/kotlin/com/shl/payment/common/ src/test/kotlin/com/shl/payment/common/
git commit -m "feat: add common layer (ApiResponse, exceptions, handler)"
```

---

### Task 3: Auth Domain

**Files:**
- Create: `src/main/kotlin/com/shl/payment/auth/domain/User.kt`
- Create: `src/main/kotlin/com/shl/payment/auth/domain/UserRepository.kt`
- Create: `src/main/kotlin/com/shl/payment/auth/domain/RefreshToken.kt`
- Create: `src/main/kotlin/com/shl/payment/auth/domain/RefreshTokenRepository.kt`
- Test: `src/test/kotlin/com/shl/payment/auth/domain/UserTest.kt`

- [ ] **Step 1: User.kt 작성**

```kotlin
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
```

- [ ] **Step 2: UserRepository.kt 작성 (도메인 인터페이스)**

```kotlin
package com.shl.payment.auth.domain

import java.util.Optional
import java.util.UUID

interface UserRepository {
    fun save(user: User): User
    fun findByEmail(email: String): Optional<User>
    fun existsByEmail(email: String): Boolean
    fun findById(id: UUID): Optional<User>
}
```

- [ ] **Step 3: RefreshToken.kt 작성**

```kotlin
package com.shl.payment.auth.domain

import jakarta.persistence.*
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(name = "refresh_tokens")
class RefreshToken(
    userId: UUID,
    token: String,
    expiresAt: LocalDateTime,
) {
    @Id
    val id: UUID = UUID.randomUUID()

    @Column(nullable = false)
    val userId: UUID = userId

    @Column(unique = true, nullable = false)
    var token: String = token
        private set

    @Column(nullable = false)
    val expiresAt: LocalDateTime = expiresAt

    @Column(nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now()

    fun isExpired(): Boolean = LocalDateTime.now().isAfter(expiresAt)
}
```

- [ ] **Step 4: RefreshTokenRepository.kt 작성**

```kotlin
package com.shl.payment.auth.domain

import java.util.Optional
import java.util.UUID

interface RefreshTokenRepository {
    fun save(token: RefreshToken): RefreshToken
    fun findByToken(token: String): Optional<RefreshToken>
    fun deleteByUserId(userId: UUID)
}
```

- [ ] **Step 5: UserTest.kt 작성**

```kotlin
package com.shl.payment.auth.domain

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

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
```

- [ ] **Step 6: 테스트 실행**

```bash
./gradlew test --tests "com.shl.payment.auth.domain.UserTest"
```

Expected: 2 tests PASSED

- [ ] **Step 7: Commit**

```bash
git add src/main/kotlin/com/shl/payment/auth/domain/ src/test/kotlin/com/shl/payment/auth/domain/
git commit -m "feat: add auth domain (User, RefreshToken)"
```

---

### Task 4: JWT Infrastructure + Spring Security

**Files:**
- Create: `src/main/kotlin/com/shl/payment/auth/infrastructure/JwtProvider.kt`
- Create: `src/main/kotlin/com/shl/payment/auth/infrastructure/JwtAuthenticationFilter.kt`
- Create: `src/main/kotlin/com/shl/payment/auth/infrastructure/UserRepositoryImpl.kt`
- Create: `src/main/kotlin/com/shl/payment/auth/infrastructure/RefreshTokenRepositoryImpl.kt`
- Create: `src/main/kotlin/com/shl/payment/common/config/SecurityConfig.kt`

- [ ] **Step 1: UserRepositoryImpl.kt 작성**

```kotlin
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
```

- [ ] **Step 2: RefreshTokenRepositoryImpl.kt 작성**

```kotlin
package com.shl.payment.auth.infrastructure

import com.shl.payment.auth.domain.RefreshToken
import com.shl.payment.auth.domain.RefreshTokenRepository
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.Optional
import java.util.UUID

interface RefreshTokenJpaRepository : JpaRepository<RefreshToken, UUID> {
    fun findByToken(token: String): Optional<RefreshToken>
    fun deleteByUserId(userId: UUID)
}

@Repository
class RefreshTokenRepositoryImpl(
    private val jpaRepository: RefreshTokenJpaRepository,
) : RefreshTokenRepository {
    override fun save(token: RefreshToken) = jpaRepository.save(token)
    override fun findByToken(token: String) = jpaRepository.findByToken(token)
    override fun deleteByUserId(userId: UUID) = jpaRepository.deleteByUserId(userId)
}
```

- [ ] **Step 3: JwtProvider.kt 작성**

```kotlin
package com.shl.payment.auth.infrastructure

import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.util.Date
import java.util.UUID

@Component
class JwtProvider(
    @Value("\${jwt.secret}") private val secret: String,
    @Value("\${jwt.access-expiration}") private val accessExpiration: Long,
    @Value("\${jwt.refresh-expiration}") private val refreshExpiration: Long,
) {
    private val key = Keys.hmacShaKeyFor(secret.toByteArray())

    fun generateAccessToken(userId: UUID): String = Jwts.builder()
        .subject(userId.toString())
        .claim("type", "access")
        .issuedAt(Date())
        .expiration(Date(System.currentTimeMillis() + accessExpiration))
        .signWith(key)
        .compact()

    fun generateRefreshToken(userId: UUID): String = Jwts.builder()
        .subject(userId.toString())
        .claim("type", "refresh")
        .issuedAt(Date())
        .expiration(Date(System.currentTimeMillis() + refreshExpiration))
        .signWith(key)
        .compact()

    fun getUserId(token: String): UUID {
        val subject = Jwts.parser()
            .verifyWith(key)
            .build()
            .parseSignedClaims(token)
            .payload
            .subject
        return UUID.fromString(subject)
    }

    fun isValid(token: String): Boolean = runCatching {
        Jwts.parser().verifyWith(key).build().parseSignedClaims(token)
        true
    }.getOrDefault(false)
}
```

- [ ] **Step 4: JwtAuthenticationFilter.kt 작성**

```kotlin
package com.shl.payment.auth.infrastructure

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.filter.OncePerRequestFilter

class JwtAuthenticationFilter(
    private val jwtProvider: JwtProvider,
) : OncePerRequestFilter() {

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain,
    ) {
        val token = resolveToken(request)
        if (token != null && jwtProvider.isValid(token)) {
            val userId = jwtProvider.getUserId(token)
            val auth = UsernamePasswordAuthenticationToken(
                userId.toString(),
                null,
                listOf(SimpleGrantedAuthority("ROLE_USER")),
            )
            SecurityContextHolder.getContext().authentication = auth
        }
        filterChain.doFilter(request, response)
    }

    private fun resolveToken(request: HttpServletRequest): String? {
        val bearer = request.getHeader("Authorization") ?: return null
        return if (bearer.startsWith("Bearer ")) bearer.substring(7) else null
    }
}
```

- [ ] **Step 5: SecurityConfig.kt 작성**

```kotlin
package com.shl.payment.common.config

import com.shl.payment.auth.infrastructure.JwtAuthenticationFilter
import com.shl.payment.auth.infrastructure.JwtProvider
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter

@Configuration
@EnableWebSecurity
class SecurityConfig(
    private val jwtProvider: JwtProvider,
) {
    @Bean
    fun filterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .csrf { it.disable() }
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .authorizeHttpRequests {
                it
                    .requestMatchers("/auth/**", "/webhook/**").permitAll()
                    .requestMatchers(HttpMethod.GET, "/", "/payment/success", "/payment/fail").permitAll()
                    .anyRequest().authenticated()
            }
            .addFilterBefore(
                JwtAuthenticationFilter(jwtProvider),
                UsernamePasswordAuthenticationFilter::class.java,
            )
        return http.build()
    }

    @Bean
    fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder()
}
```

- [ ] **Step 6: WebClientConfig.kt 작성**

```kotlin
package com.shl.payment.common.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.client.WebClient

@Configuration
class WebClientConfig {
    @Bean
    fun webClient(): WebClient = WebClient.builder().build()
}
```

- [ ] **Step 7: `@EnableJpaAuditing` 추가** (PaymentApplication.kt 수정)

```kotlin
package com.shl.payment

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.data.jpa.repository.config.EnableJpaAuditing
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@EnableScheduling
@EnableJpaAuditing
class PaymentApplication

fun main(args: Array<String>) {
    runApplication<PaymentApplication>(*args)
}
```

- [ ] **Step 8: 빌드 확인**

```bash
./gradlew build -x test
```

Expected: `BUILD SUCCESSFUL`

- [ ] **Step 9: Commit**

```bash
git add src/main/kotlin/com/shl/payment/auth/infrastructure/ src/main/kotlin/com/shl/payment/common/config/ src/main/kotlin/com/shl/payment/PaymentApplication.kt
git commit -m "feat: add JWT provider, security config"
```

---

### Task 5: Auth Application + Controller

**Files:**
- Create: `src/main/kotlin/com/shl/payment/auth/application/dto/SignupRequest.kt`
- Create: `src/main/kotlin/com/shl/payment/auth/application/dto/LoginRequest.kt`
- Create: `src/main/kotlin/com/shl/payment/auth/application/dto/TokenResponse.kt`
- Create: `src/main/kotlin/com/shl/payment/auth/application/AuthService.kt`
- Create: `src/main/kotlin/com/shl/payment/auth/presentation/AuthController.kt`
- Test: `src/test/kotlin/com/shl/payment/auth/application/AuthServiceTest.kt`

- [ ] **Step 1: DTO 작성**

```kotlin
// SignupRequest.kt
package com.shl.payment.auth.application.dto

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class SignupRequest(
    @field:Email @field:NotBlank val email: String,
    @field:NotBlank @field:Size(min = 8) val password: String,
)

// LoginRequest.kt
package com.shl.payment.auth.application.dto

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank

data class LoginRequest(
    @field:Email @field:NotBlank val email: String,
    @field:NotBlank val password: String,
)

// TokenResponse.kt
package com.shl.payment.auth.application.dto

data class TokenResponse(
    val accessToken: String,
    val refreshToken: String,
)
```

- [ ] **Step 2: AuthService 테스트 작성**

```kotlin
package com.shl.payment.auth.application

import com.shl.payment.auth.domain.User
import com.shl.payment.auth.domain.UserRepository
import com.shl.payment.auth.domain.RefreshTokenRepository
import com.shl.payment.auth.infrastructure.JwtProvider
import com.shl.payment.common.exception.BusinessException
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import java.util.Optional
import kotlin.test.assertNotNull

class AuthServiceTest {

    private val userRepository = mockk<UserRepository>()
    private val refreshTokenRepository = mockk<RefreshTokenRepository>()
    private val jwtProvider = mockk<JwtProvider>()
    private val passwordEncoder = BCryptPasswordEncoder()
    private val authService = AuthService(userRepository, refreshTokenRepository, jwtProvider, passwordEncoder)

    @Test
    fun `회원가입 - 정상`() {
        every { userRepository.existsByEmail("test@test.com") } returns false
        every { userRepository.save(any()) } answers { firstArg() }
        every { refreshTokenRepository.save(any()) } answers { firstArg() }
        every { jwtProvider.generateAccessToken(any()) } returns "access_token"
        every { jwtProvider.generateRefreshToken(any()) } returns "refresh_token"

        val result = authService.signup("test@test.com", "password123")

        assertNotNull(result.accessToken)
        verify { userRepository.save(any()) }
    }

    @Test
    fun `회원가입 - 이미 존재하는 이메일이면 예외`() {
        every { userRepository.existsByEmail("test@test.com") } returns true

        assertThrows<BusinessException> {
            authService.signup("test@test.com", "password123")
        }
    }

    @Test
    fun `로그인 - 이메일 없으면 예외`() {
        every { userRepository.findByEmail("unknown@test.com") } returns Optional.empty()

        assertThrows<BusinessException> {
            authService.login("unknown@test.com", "password123")
        }
    }

    @Test
    fun `로그인 - 비밀번호 틀리면 예외`() {
        val user = User(email = "test@test.com", password = passwordEncoder.encode("correct_password"))
        every { userRepository.findByEmail("test@test.com") } returns Optional.of(user)

        assertThrows<BusinessException> {
            authService.login("test@test.com", "wrong_password")
        }
    }
}
```

- [ ] **Step 3: 테스트 실행 — 실패 확인**

```bash
./gradlew test --tests "com.shl.payment.auth.application.AuthServiceTest"
```

Expected: FAIL (AuthService 없음)

- [ ] **Step 4: AuthService.kt 작성**

```kotlin
package com.shl.payment.auth.application

import com.shl.payment.auth.domain.RefreshToken
import com.shl.payment.auth.domain.RefreshTokenRepository
import com.shl.payment.auth.domain.User
import com.shl.payment.auth.domain.UserRepository
import com.shl.payment.auth.application.dto.TokenResponse
import com.shl.payment.auth.infrastructure.JwtProvider
import com.shl.payment.common.exception.BusinessException
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.UUID

@Service
@Transactional
class AuthService(
    private val userRepository: UserRepository,
    private val refreshTokenRepository: RefreshTokenRepository,
    private val jwtProvider: JwtProvider,
    private val passwordEncoder: PasswordEncoder,
    @Value("\${jwt.refresh-expiration:604800000}") private val refreshExpiration: Long = 604800000L,
) {
    fun signup(email: String, password: String): TokenResponse {
        if (userRepository.existsByEmail(email)) {
            throw BusinessException("DUPLICATE_EMAIL", "이미 사용 중인 이메일입니다")
        }
        val user = User(email = email, password = passwordEncoder.encode(password))
        userRepository.save(user)
        return issueTokens(user.id)
    }

    fun login(email: String, password: String): TokenResponse {
        val user = userRepository.findByEmail(email)
            .orElseThrow { BusinessException("INVALID_CREDENTIALS", "이메일 또는 비밀번호가 올바르지 않습니다") }
        if (!passwordEncoder.matches(password, user.password)) {
            throw BusinessException("INVALID_CREDENTIALS", "이메일 또는 비밀번호가 올바르지 않습니다")
        }
        refreshTokenRepository.deleteByUserId(user.id)
        return issueTokens(user.id)
    }

    fun refresh(refreshTokenStr: String): TokenResponse {
        val refreshToken = refreshTokenRepository.findByToken(refreshTokenStr)
            .orElseThrow { BusinessException("INVALID_REFRESH_TOKEN", "유효하지 않은 리프레시 토큰입니다") }
        if (refreshToken.isExpired()) {
            throw BusinessException("EXPIRED_REFRESH_TOKEN", "만료된 리프레시 토큰입니다")
        }
        refreshTokenRepository.deleteByUserId(refreshToken.userId)
        return issueTokens(refreshToken.userId)
    }

    private fun issueTokens(userId: UUID): TokenResponse {
        val accessToken = jwtProvider.generateAccessToken(userId)
        val refreshTokenStr = jwtProvider.generateRefreshToken(userId)
        val expiresAt = LocalDateTime.now().plusSeconds(refreshExpiration / 1000)
        refreshTokenRepository.save(RefreshToken(userId, refreshTokenStr, expiresAt))
        return TokenResponse(accessToken, refreshTokenStr)
    }
}
```

- [ ] **Step 5: 테스트 실행 — 통과 확인**

```bash
./gradlew test --tests "com.shl.payment.auth.application.AuthServiceTest"
```

Expected: 4 tests PASSED

- [ ] **Step 6: AuthController.kt 작성**

```kotlin
package com.shl.payment.auth.presentation

import com.shl.payment.auth.application.AuthService
import com.shl.payment.auth.application.dto.LoginRequest
import com.shl.payment.auth.application.dto.SignupRequest
import com.shl.payment.common.response.ApiResponse
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/auth")
class AuthController(
    private val authService: AuthService,
) {
    @PostMapping("/signup")
    fun signup(@RequestBody @Valid request: SignupRequest): ResponseEntity<ApiResponse<*>> {
        val tokens = authService.signup(request.email, request.password)
        return ResponseEntity.ok(ApiResponse.ok(tokens))
    }

    @PostMapping("/login")
    fun login(@RequestBody @Valid request: LoginRequest): ResponseEntity<ApiResponse<*>> {
        val tokens = authService.login(request.email, request.password)
        return ResponseEntity.ok(ApiResponse.ok(tokens))
    }

    @PostMapping("/refresh")
    fun refresh(@RequestHeader("Refresh-Token") refreshToken: String): ResponseEntity<ApiResponse<*>> {
        val tokens = authService.refresh(refreshToken)
        return ResponseEntity.ok(ApiResponse.ok(tokens))
    }
}
```

- [ ] **Step 7: Commit**

```bash
git add src/main/kotlin/com/shl/payment/auth/ src/test/kotlin/com/shl/payment/auth/
git commit -m "feat: add auth service and controller with JWT"
```

---

### Task 6: Order Domain + Service + Controller

**Files:**
- Create: `src/main/kotlin/com/shl/payment/order/domain/Order.kt`
- Create: `src/main/kotlin/com/shl/payment/order/domain/OrderStatus.kt`
- Create: `src/main/kotlin/com/shl/payment/order/domain/OrderRepository.kt`
- Create: `src/main/kotlin/com/shl/payment/order/infrastructure/OrderRepositoryImpl.kt`
- Create: `src/main/kotlin/com/shl/payment/order/application/OrderService.kt`
- Create: `src/main/kotlin/com/shl/payment/order/application/dto/*.kt`
- Create: `src/main/kotlin/com/shl/payment/order/presentation/OrderController.kt`
- Test: `src/test/kotlin/com/shl/payment/order/application/OrderServiceTest.kt`

- [ ] **Step 1: OrderStatus.kt 작성**

```kotlin
package com.shl.payment.order.domain

enum class OrderStatus {
    PENDING,
    PAID,
    CANCELLED,
}
```

- [ ] **Step 2: Order.kt 작성**

```kotlin
package com.shl.payment.order.domain

import jakarta.persistence.*
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(name = "orders")
@EntityListeners(AuditingEntityListener::class)
class Order(
    userId: UUID,
    totalAmount: Long,
    orderName: String,
) {
    @Id
    val id: UUID = UUID.randomUUID()

    @Column(nullable = false)
    val userId: UUID = userId

    @Column(nullable = false)
    val totalAmount: Long = totalAmount

    @Column(nullable = false)
    val orderName: String = orderName

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: OrderStatus = OrderStatus.PENDING
        private set

    @CreatedDate
    @Column(updatable = false)
    var createdAt: LocalDateTime = LocalDateTime.now()
        private set

    fun markPaid() {
        check(status == OrderStatus.PENDING) { "PENDING 상태만 PAID로 전이 가능" }
        status = OrderStatus.PAID
    }

    fun markCancelled() {
        check(status != OrderStatus.CANCELLED) { "이미 취소된 주문입니다" }
        status = OrderStatus.CANCELLED
    }
}
```

- [ ] **Step 3: OrderRepository.kt 작성**

```kotlin
package com.shl.payment.order.domain

import java.util.Optional
import java.util.UUID

interface OrderRepository {
    fun save(order: Order): Order
    fun findById(id: UUID): Optional<Order>
    fun findByUserId(userId: UUID): List<Order>
}
```

- [ ] **Step 4: OrderRepositoryImpl.kt 작성**

```kotlin
package com.shl.payment.order.infrastructure

import com.shl.payment.order.domain.Order
import com.shl.payment.order.domain.OrderRepository
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.Optional
import java.util.UUID

interface OrderJpaRepository : JpaRepository<Order, UUID> {
    fun findByUserId(userId: UUID): List<Order>
}

@Repository
class OrderRepositoryImpl(
    private val jpaRepository: OrderJpaRepository,
) : OrderRepository {
    override fun save(order: Order) = jpaRepository.save(order)
    override fun findById(id: UUID) = jpaRepository.findById(id)
    override fun findByUserId(userId: UUID) = jpaRepository.findByUserId(userId)
}
```

- [ ] **Step 5: DTO 작성**

```kotlin
// CreateOrderRequest.kt
package com.shl.payment.order.application.dto

import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Positive

data class CreateOrderRequest(
    @field:NotBlank val orderName: String,
    @field:Positive val totalAmount: Long,
)

// OrderResponse.kt
package com.shl.payment.order.application.dto

import com.shl.payment.order.domain.Order
import com.shl.payment.order.domain.OrderStatus
import java.time.LocalDateTime
import java.util.UUID

data class OrderResponse(
    val orderId: UUID,
    val orderName: String,
    val totalAmount: Long,
    val status: OrderStatus,
    val createdAt: LocalDateTime,
) {
    companion object {
        fun from(order: Order) = OrderResponse(
            orderId = order.id,
            orderName = order.orderName,
            totalAmount = order.totalAmount,
            status = order.status,
            createdAt = order.createdAt,
        )
    }
}
```

- [ ] **Step 6: OrderService 테스트 작성**

```kotlin
package com.shl.payment.order.application

import com.shl.payment.common.exception.BusinessException
import com.shl.payment.order.domain.Order
import com.shl.payment.order.domain.OrderRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.Optional
import java.util.UUID
import kotlin.test.assertEquals

class OrderServiceTest {

    private val orderRepository = mockk<OrderRepository>()
    private val orderService = OrderService(orderRepository)
    private val userId = UUID.randomUUID()

    @Test
    fun `주문 생성 - 정상`() {
        every { orderRepository.save(any()) } answers { firstArg() }

        val result = orderService.createOrder(userId, "테스트 상품", 10000L)

        assertEquals("테스트 상품", result.orderName)
        assertEquals(10000L, result.totalAmount)
        verify { orderRepository.save(any()) }
    }

    @Test
    fun `주문 조회 - 없으면 예외`() {
        every { orderRepository.findById(any()) } returns Optional.empty()

        assertThrows<BusinessException> {
            orderService.getOrder(UUID.randomUUID(), userId)
        }
    }

    @Test
    fun `주문 조회 - 다른 사용자 주문이면 예외`() {
        val order = Order(userId = UUID.randomUUID(), totalAmount = 10000L, orderName = "상품")
        every { orderRepository.findById(order.id) } returns Optional.of(order)

        assertThrows<BusinessException> {
            orderService.getOrder(order.id, userId)
        }
    }
}
```

- [ ] **Step 7: 테스트 실행 — 실패 확인**

```bash
./gradlew test --tests "com.shl.payment.order.application.OrderServiceTest"
```

Expected: FAIL

- [ ] **Step 8: OrderService.kt 작성**

```kotlin
package com.shl.payment.order.application

import com.shl.payment.common.exception.BusinessException
import com.shl.payment.order.application.dto.OrderResponse
import com.shl.payment.order.domain.Order
import com.shl.payment.order.domain.OrderRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
@Transactional
class OrderService(
    private val orderRepository: OrderRepository,
) {
    fun createOrder(userId: UUID, orderName: String, totalAmount: Long): OrderResponse {
        val order = Order(userId = userId, totalAmount = totalAmount, orderName = orderName)
        orderRepository.save(order)
        return OrderResponse.from(order)
    }

    @Transactional(readOnly = true)
    fun getOrder(orderId: UUID, userId: UUID): OrderResponse {
        val order = orderRepository.findById(orderId)
            .orElseThrow { BusinessException("ORDER_NOT_FOUND", "주문을 찾을 수 없습니다") }
        if (order.userId != userId) {
            throw BusinessException("ORDER_ACCESS_DENIED", "접근 권한이 없습니다")
        }
        return OrderResponse.from(order)
    }
}
```

- [ ] **Step 9: 테스트 실행 — 통과 확인**

```bash
./gradlew test --tests "com.shl.payment.order.application.OrderServiceTest"
```

Expected: 3 tests PASSED

- [ ] **Step 10: OrderController.kt 작성**

```kotlin
package com.shl.payment.order.presentation

import com.shl.payment.common.response.ApiResponse
import com.shl.payment.order.application.OrderService
import com.shl.payment.order.application.dto.CreateOrderRequest
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import java.util.UUID

@RestController
@RequestMapping("/orders")
class OrderController(
    private val orderService: OrderService,
) {
    @PostMapping
    fun createOrder(
        @AuthenticationPrincipal userId: String,
        @RequestBody @Valid request: CreateOrderRequest,
    ): ResponseEntity<ApiResponse<*>> {
        val result = orderService.createOrder(
            userId = UUID.fromString(userId),
            orderName = request.orderName,
            totalAmount = request.totalAmount,
        )
        return ResponseEntity.ok(ApiResponse.ok(result))
    }

    @GetMapping("/{orderId}")
    fun getOrder(
        @AuthenticationPrincipal userId: String,
        @PathVariable orderId: UUID,
    ): ResponseEntity<ApiResponse<*>> {
        val result = orderService.getOrder(orderId, UUID.fromString(userId))
        return ResponseEntity.ok(ApiResponse.ok(result))
    }
}
```

- [ ] **Step 11: Commit**

```bash
git add src/main/kotlin/com/shl/payment/order/ src/test/kotlin/com/shl/payment/order/
git commit -m "feat: add order domain, service, controller"
```

---

### Task 7: Payment Domain

**Files:**
- Create: `src/main/kotlin/com/shl/payment/payment/domain/PaymentStatus.kt`
- Create: `src/main/kotlin/com/shl/payment/payment/domain/PaymentMethod.kt`
- Create: `src/main/kotlin/com/shl/payment/payment/domain/vo/Money.kt`
- Create: `src/main/kotlin/com/shl/payment/payment/domain/Payment.kt`
- Create: `src/main/kotlin/com/shl/payment/payment/domain/PaymentRepository.kt`
- Test: `src/test/kotlin/com/shl/payment/payment/domain/PaymentTest.kt`

- [ ] **Step 1: Enum 및 VO 작성**

```kotlin
// PaymentStatus.kt
package com.shl.payment.payment.domain

enum class PaymentStatus {
    READY,
    DONE,
    CANCELLED,
    FAILED,
}

// PaymentMethod.kt
package com.shl.payment.payment.domain

enum class PaymentMethod {
    CARD,
    VIRTUAL_ACCOUNT,
    TRANSFER,
    MOBILE_PHONE,
    GIFT_CERTIFICATE,
    EASY_PAY,
}

// vo/Money.kt
package com.shl.payment.payment.domain.vo

data class Money(val amount: Long) {
    init {
        require(amount >= 0) { "금액은 0 이상이어야 합니다" }
    }
    operator fun plus(other: Money) = Money(amount + other.amount)
    operator fun minus(other: Money) = Money(amount - other.amount)
    operator fun compareTo(other: Money) = amount.compareTo(other.amount)
}
```

- [ ] **Step 2: PaymentTest.kt 작성 (상태 전이 검증)**

```kotlin
package com.shl.payment.payment.domain

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.UUID
import kotlin.test.assertEquals

class PaymentTest {

    private fun payment(status: PaymentStatus = PaymentStatus.READY) =
        Payment(orderId = UUID.randomUUID(), amount = 10000L).also {
            if (status == PaymentStatus.DONE) it.confirm("pay_key", PaymentMethod.CARD)
            if (status == PaymentStatus.FAILED) it.fail("실패 이유")
        }

    @Test
    fun `READY 상태에서 confirm 호출 시 DONE으로 전이`() {
        val p = payment(PaymentStatus.READY)
        p.confirm("pay_key_123", PaymentMethod.CARD)
        assertEquals(PaymentStatus.DONE, p.status)
        assertEquals("pay_key_123", p.paymentKey)
    }

    @Test
    fun `DONE 상태에서 confirm 호출 시 예외`() {
        val p = payment(PaymentStatus.DONE)
        assertThrows<IllegalStateException> { p.confirm("another_key", PaymentMethod.CARD) }
    }

    @Test
    fun `FAILED 상태에서 confirm 호출 시 예외`() {
        val p = payment(PaymentStatus.FAILED)
        assertThrows<IllegalStateException> { p.confirm("key", PaymentMethod.CARD) }
    }

    @Test
    fun `DONE 상태에서 전체 cancel 시 CANCELLED로 전이`() {
        val p = payment(PaymentStatus.DONE)
        p.cancel(10000L)
        assertEquals(PaymentStatus.CANCELLED, p.status)
        assertEquals(10000L, p.cancelledAmount)
    }

    @Test
    fun `DONE 상태에서 부분 cancel 시 DONE 유지`() {
        val p = payment(PaymentStatus.DONE)
        p.cancel(3000L)
        assertEquals(PaymentStatus.DONE, p.status)
        assertEquals(3000L, p.cancelledAmount)
    }

    @Test
    fun `취소 금액 초과 시 예외`() {
        val p = payment(PaymentStatus.DONE)
        assertThrows<IllegalStateException> { p.cancel(20000L) }
    }

    @Test
    fun `CANCELLED 상태에서 cancel 시 예외`() {
        val p = payment(PaymentStatus.DONE)
        p.cancel(10000L)
        assertThrows<IllegalStateException> { p.cancel(1000L) }
    }

    @Test
    fun `READY 상태에서 fail 시 FAILED로 전이`() {
        val p = payment(PaymentStatus.READY)
        p.fail("카드 한도 초과")
        assertEquals(PaymentStatus.FAILED, p.status)
        assertEquals("카드 한도 초과", p.failReason)
    }

    @Test
    fun `DONE 상태에서 fail 시 예외`() {
        val p = payment(PaymentStatus.DONE)
        assertThrows<IllegalStateException> { p.fail("이유") }
    }
}
```

- [ ] **Step 3: 테스트 실행 — 실패 확인**

```bash
./gradlew test --tests "com.shl.payment.payment.domain.PaymentTest"
```

Expected: FAIL (Payment 없음)

- [ ] **Step 4: Payment.kt 작성**

```kotlin
package com.shl.payment.payment.domain

import jakarta.persistence.*
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(name = "payments")
@EntityListeners(AuditingEntityListener::class)
class Payment(
    orderId: UUID,
    amount: Long,
) {
    @Id
    val id: UUID = UUID.randomUUID()

    @Column(nullable = false)
    val orderId: UUID = orderId

    @Column(unique = true)
    var paymentKey: String? = null
        private set

    @Enumerated(EnumType.STRING)
    var method: PaymentMethod? = null
        private set

    @Column(nullable = false)
    val amount: Long = amount

    @Column(nullable = false)
    var cancelledAmount: Long = 0L
        private set

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: PaymentStatus = PaymentStatus.READY
        private set

    var failReason: String? = null
        private set

    @CreatedDate
    @Column(updatable = false)
    var requestedAt: LocalDateTime = LocalDateTime.now()
        private set

    var approvedAt: LocalDateTime? = null
        private set

    var cancelledAt: LocalDateTime? = null
        private set

    fun confirm(paymentKey: String, method: PaymentMethod) {
        check(status == PaymentStatus.READY) { "READY 상태만 승인 가능, 현재: $status" }
        this.paymentKey = paymentKey
        this.method = method
        this.status = PaymentStatus.DONE
        this.approvedAt = LocalDateTime.now()
    }

    fun cancel(amount: Long) {
        check(status == PaymentStatus.DONE) { "DONE 상태만 취소 가능, 현재: $status" }
        check(cancelledAmount + amount <= this.amount) { "취소 금액이 결제 금액을 초과합니다" }
        cancelledAmount += amount
        cancelledAt = LocalDateTime.now()
        if (cancelledAmount == this.amount) {
            status = PaymentStatus.CANCELLED
        }
    }

    fun fail(reason: String) {
        check(status == PaymentStatus.READY) { "READY 상태만 실패 처리 가능, 현재: $status" }
        status = PaymentStatus.FAILED
        failReason = reason
    }
}
```

- [ ] **Step 5: 테스트 실행 — 통과 확인**

```bash
./gradlew test --tests "com.shl.payment.payment.domain.PaymentTest"
```

Expected: 9 tests PASSED

- [ ] **Step 6: PaymentRepository.kt 작성**

```kotlin
package com.shl.payment.payment.domain

import java.time.LocalDateTime
import java.util.Optional
import java.util.UUID

interface PaymentRepository {
    fun save(payment: Payment): Payment
    fun findById(id: UUID): Optional<Payment>
    fun findByOrderId(orderId: UUID): Optional<Payment>
    fun findByPaymentKey(paymentKey: String): Optional<Payment>
    fun findByStatusAndRequestedAtBefore(status: PaymentStatus, before: LocalDateTime): List<Payment>
}
```

- [ ] **Step 7: PaymentRepositoryImpl.kt 작성**

```kotlin
package com.shl.payment.payment.infrastructure

import com.shl.payment.payment.domain.Payment
import com.shl.payment.payment.domain.PaymentRepository
import com.shl.payment.payment.domain.PaymentStatus
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.time.LocalDateTime
import java.util.Optional
import java.util.UUID

interface PaymentJpaRepository : JpaRepository<Payment, UUID> {
    fun findByOrderId(orderId: UUID): Optional<Payment>
    fun findByPaymentKey(paymentKey: String): Optional<Payment>
    fun findByStatusAndRequestedAtBefore(status: PaymentStatus, before: LocalDateTime): List<Payment>
}

@Repository
class PaymentRepositoryImpl(
    private val jpaRepository: PaymentJpaRepository,
) : PaymentRepository {
    override fun save(payment: Payment) = jpaRepository.save(payment)
    override fun findById(id: UUID) = jpaRepository.findById(id)
    override fun findByOrderId(orderId: UUID) = jpaRepository.findByOrderId(orderId)
    override fun findByPaymentKey(paymentKey: String) = jpaRepository.findByPaymentKey(paymentKey)
    override fun findByStatusAndRequestedAtBefore(status: PaymentStatus, before: LocalDateTime) =
        jpaRepository.findByStatusAndRequestedAtBefore(status, before)
}
```

- [ ] **Step 8: Commit**

```bash
git add src/main/kotlin/com/shl/payment/payment/domain/ src/main/kotlin/com/shl/payment/payment/infrastructure/PaymentRepositoryImpl.kt src/test/kotlin/com/shl/payment/payment/domain/
git commit -m "feat: add payment domain with state machine"
```

---

### Task 8: Toss Payment Client

**Files:**
- Create: `src/main/kotlin/com/shl/payment/payment/infrastructure/dto/TossConfirmRequest.kt`
- Create: `src/main/kotlin/com/shl/payment/payment/infrastructure/dto/TossConfirmResponse.kt`
- Create: `src/main/kotlin/com/shl/payment/payment/infrastructure/dto/TossCancelRequest.kt`
- Create: `src/main/kotlin/com/shl/payment/payment/infrastructure/dto/TossCancelResponse.kt`
- Create: `src/main/kotlin/com/shl/payment/payment/infrastructure/TossPaymentClient.kt`

- [ ] **Step 1: Toss DTO 작성**

```kotlin
// TossConfirmRequest.kt
package com.shl.payment.payment.infrastructure.dto

data class TossConfirmRequest(
    val paymentKey: String,
    val orderId: String,
    val amount: Long,
)

// TossConfirmResponse.kt
package com.shl.payment.payment.infrastructure.dto

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class TossConfirmResponse(
    val paymentKey: String,
    val orderId: String,
    val status: String,
    val method: String?,
    val totalAmount: Long,
)

// TossCancelRequest.kt
package com.shl.payment.payment.infrastructure.dto

data class TossCancelRequest(
    val cancelReason: String,
    val cancelAmount: Long? = null,  // null이면 전체 취소
)

// TossCancelResponse.kt
package com.shl.payment.payment.infrastructure.dto

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class TossCancelResponse(
    val paymentKey: String,
    val status: String,
)
```

- [ ] **Step 2: TossPaymentClient.kt 작성**

```kotlin
package com.shl.payment.payment.infrastructure

import com.shl.payment.common.exception.PaymentException
import com.shl.payment.payment.infrastructure.dto.TossCancelRequest
import com.shl.payment.payment.infrastructure.dto.TossCancelResponse
import com.shl.payment.payment.infrastructure.dto.TossConfirmRequest
import com.shl.payment.payment.infrastructure.dto.TossConfirmResponse
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import java.util.Base64

@Component
class TossPaymentClient(
    private val webClient: WebClient,
    @Value("\${toss.secret-key}") private val secretKey: String,
    @Value("\${toss.base-url}") private val baseUrl: String,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    private fun authHeader(): String {
        val encoded = Base64.getEncoder().encodeToString("$secretKey:".toByteArray())
        return "Basic $encoded"
    }

    fun confirm(paymentKey: String, orderId: String, amount: Long): TossConfirmResponse {
        return try {
            webClient.post()
                .uri("$baseUrl/v1/payments/confirm")
                .header(HttpHeaders.AUTHORIZATION, authHeader())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(TossConfirmRequest(paymentKey, orderId, amount))
                .retrieve()
                .bodyToMono(TossConfirmResponse::class.java)
                .block()!!
        } catch (e: WebClientResponseException) {
            log.error("Toss confirm failed: status=${e.statusCode}, body=${e.responseBodyAsString}")
            throw PaymentException.tossFailed(e.responseBodyAsString)
        }
    }

    fun cancel(paymentKey: String, cancelReason: String, cancelAmount: Long? = null): TossCancelResponse {
        return try {
            webClient.post()
                .uri("$baseUrl/v1/payments/$paymentKey/cancel")
                .header(HttpHeaders.AUTHORIZATION, authHeader())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(TossCancelRequest(cancelReason, cancelAmount))
                .retrieve()
                .bodyToMono(TossCancelResponse::class.java)
                .block()!!
        } catch (e: WebClientResponseException) {
            log.error("Toss cancel failed: paymentKey=$paymentKey, body=${e.responseBodyAsString}")
            throw PaymentException.tossFailed(e.responseBodyAsString)
        }
    }
}
```

- [ ] **Step 3: Commit**

```bash
git add src/main/kotlin/com/shl/payment/payment/infrastructure/
git commit -m "feat: add Toss payment client"
```

---

### Task 9: Payment Service (결제 승인 + 재시도)

**Files:**
- Create: `src/main/kotlin/com/shl/payment/payment/application/dto/*.kt`
- Create: `src/main/kotlin/com/shl/payment/payment/application/PaymentService.kt`
- Test: `src/test/kotlin/com/shl/payment/payment/application/PaymentServiceTest.kt`

- [ ] **Step 1: DTO 작성**

```kotlin
// ConfirmPaymentRequest.kt
package com.shl.payment.payment.application.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Positive
import java.util.UUID

data class ConfirmPaymentRequest(
    @field:NotBlank val paymentKey: String,
    val orderId: UUID,
    @field:Positive val amount: Long,
)

// CancelPaymentRequest.kt
package com.shl.payment.payment.application.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Positive

data class CancelPaymentRequest(
    @field:NotBlank val cancelReason: String,
    @field:Positive val cancelAmount: Long? = null,
)

// PaymentResponse.kt
package com.shl.payment.payment.application.dto

import com.shl.payment.payment.domain.Payment
import com.shl.payment.payment.domain.PaymentMethod
import com.shl.payment.payment.domain.PaymentStatus
import java.time.LocalDateTime
import java.util.UUID

data class PaymentResponse(
    val paymentId: UUID,
    val orderId: UUID,
    val paymentKey: String?,
    val method: PaymentMethod?,
    val amount: Long,
    val cancelledAmount: Long,
    val status: PaymentStatus,
    val requestedAt: LocalDateTime,
    val approvedAt: LocalDateTime?,
) {
    companion object {
        fun from(payment: Payment) = PaymentResponse(
            paymentId = payment.id,
            orderId = payment.orderId,
            paymentKey = payment.paymentKey,
            method = payment.method,
            amount = payment.amount,
            cancelledAmount = payment.cancelledAmount,
            status = payment.status,
            requestedAt = payment.requestedAt,
            approvedAt = payment.approvedAt,
        )
    }
}
```

- [ ] **Step 2: PaymentServiceTest.kt 작성**

```kotlin
package com.shl.payment.payment.application

import com.shl.payment.common.exception.PaymentException
import com.shl.payment.order.domain.Order
import com.shl.payment.order.domain.OrderRepository
import com.shl.payment.payment.domain.Payment
import com.shl.payment.payment.domain.PaymentRepository
import com.shl.payment.payment.domain.PaymentStatus
import com.shl.payment.payment.infrastructure.TossPaymentClient
import com.shl.payment.payment.infrastructure.dto.TossConfirmResponse
import io.mockk.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.Optional
import java.util.UUID
import kotlin.test.assertEquals

class PaymentServiceTest {

    private val paymentRepository = mockk<PaymentRepository>()
    private val orderRepository = mockk<OrderRepository>()
    private val tossClient = mockk<TossPaymentClient>()
    private val paymentService = PaymentService(paymentRepository, orderRepository, tossClient)

    private val userId = UUID.randomUUID()
    private val orderId = UUID.randomUUID()

    @Test
    fun `결제 승인 - 정상 플로우`() {
        val order = Order(userId = userId, totalAmount = 10000L, orderName = "테스트")
        val payment = Payment(orderId = orderId, amount = 10000L)

        every { orderRepository.findById(orderId) } returns Optional.of(order)
        every { paymentRepository.findByOrderId(orderId) } returns Optional.of(payment)
        every { paymentRepository.save(any()) } answers { firstArg() }
        every { tossClient.confirm("pay_key", orderId.toString(), 10000L) } returns
            TossConfirmResponse("pay_key", orderId.toString(), "DONE", "카드", 10000L)

        val result = paymentService.confirm("pay_key", orderId, 10000L, userId)

        assertEquals(PaymentStatus.DONE, result.status)
        verify { tossClient.confirm("pay_key", orderId.toString(), 10000L) }
    }

    @Test
    fun `결제 승인 - 이미 DONE이면 멱등 처리`() {
        val order = Order(userId = userId, totalAmount = 10000L, orderName = "테스트")
        val payment = Payment(orderId = orderId, amount = 10000L).also {
            it.confirm("pay_key", com.shl.payment.payment.domain.PaymentMethod.CARD)
        }

        every { orderRepository.findById(orderId) } returns Optional.of(order)
        every { paymentRepository.findByOrderId(orderId) } returns Optional.of(payment)

        val result = paymentService.confirm("pay_key", orderId, 10000L, userId)

        assertEquals(PaymentStatus.DONE, result.status)
        verify(exactly = 0) { tossClient.confirm(any(), any(), any()) }
    }

    @Test
    fun `결제 승인 - 금액 불일치 시 예외`() {
        val order = Order(userId = userId, totalAmount = 10000L, orderName = "테스트")
        val payment = Payment(orderId = orderId, amount = 10000L)

        every { orderRepository.findById(orderId) } returns Optional.of(order)
        every { paymentRepository.findByOrderId(orderId) } returns Optional.of(payment)

        assertThrows<PaymentException> {
            paymentService.confirm("pay_key", orderId, 5000L, userId)
        }
        verify(exactly = 0) { tossClient.confirm(any(), any(), any()) }
    }
}
```

- [ ] **Step 3: 테스트 실행 — 실패 확인**

```bash
./gradlew test --tests "com.shl.payment.payment.application.PaymentServiceTest"
```

Expected: FAIL

- [ ] **Step 4: PaymentService.kt 작성**

```kotlin
package com.shl.payment.payment.application

import com.shl.payment.common.exception.BusinessException
import com.shl.payment.common.exception.PaymentException
import com.shl.payment.order.domain.OrderRepository
import com.shl.payment.payment.application.dto.CancelPaymentRequest
import com.shl.payment.payment.application.dto.PaymentResponse
import com.shl.payment.payment.domain.Payment
import com.shl.payment.payment.domain.PaymentMethod
import com.shl.payment.payment.domain.PaymentRepository
import com.shl.payment.payment.domain.PaymentStatus
import com.shl.payment.payment.infrastructure.TossPaymentClient
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
@Transactional
class PaymentService(
    private val paymentRepository: PaymentRepository,
    private val orderRepository: OrderRepository,
    private val tossClient: TossPaymentClient,
    private val maxRetry: Int = 3,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    fun confirm(paymentKey: String, orderId: UUID, amount: Long, userId: UUID): PaymentResponse {
        val order = orderRepository.findById(orderId)
            .orElseThrow { BusinessException("ORDER_NOT_FOUND", "주문을 찾을 수 없습니다") }

        if (order.userId != userId) throw BusinessException("ORDER_ACCESS_DENIED", "접근 권한이 없습니다")

        val payment = paymentRepository.findByOrderId(orderId)
            .orElseThrow { PaymentException.notFound() }

        // 멱등성: 이미 완료된 결제면 바로 반환
        if (payment.status == PaymentStatus.DONE) {
            return PaymentResponse.from(payment)
        }

        if (payment.status != PaymentStatus.READY) {
            throw PaymentException.invalidStatus(payment.status.name)
        }

        // 금액 검증
        if (payment.amount != amount) {
            throw PaymentException("AMOUNT_MISMATCH", "결제 금액이 주문 금액과 다릅니다")
        }

        // 토스 승인 API 호출 (재시도 포함)
        return confirmWithRetry(payment, paymentKey, orderId, amount, order)
    }

    private fun confirmWithRetry(
        payment: Payment,
        paymentKey: String,
        orderId: UUID,
        amount: Long,
        order: com.shl.payment.order.domain.Order,
    ): PaymentResponse {
        var lastException: Exception? = null

        repeat(maxRetry) { attempt ->
            try {
                val response = tossClient.confirm(paymentKey, orderId.toString(), amount)
                val method = runCatching { PaymentMethod.valueOf(response.method ?: "CARD") }
                    .getOrDefault(PaymentMethod.CARD)
                payment.confirm(paymentKey, method)
                paymentRepository.save(payment)
                order.markPaid()
                orderRepository.save(order)
                return PaymentResponse.from(payment)
            } catch (e: PaymentException) {
                throw e  // 토스 API 명시적 오류는 재시도 불필요
            } catch (e: Exception) {
                lastException = e
                val delay = (1L shl attempt) * 500L  // 500ms, 1000ms, 2000ms
                log.warn("결제 승인 재시도 ${attempt + 1}/$maxRetry, delay=${delay}ms", e)
                Thread.sleep(delay)
            }
        }

        // 재시도 전부 실패 → 토스 취소
        log.error("결제 승인 재시도 전부 실패, 토스 취소 요청")
        runCatching { tossClient.cancel(paymentKey, "DB 저장 실패로 인한 자동 취소") }
            .onFailure { log.error("토스 취소 요청도 실패", it) }
        payment.fail("재시도 초과: ${lastException?.message}")
        paymentRepository.save(payment)
        throw PaymentException("PAYMENT_CONFIRM_FAILED", "결제 처리 중 오류가 발생했습니다")
    }

    fun cancel(paymentId: UUID, userId: UUID, request: CancelPaymentRequest): PaymentResponse {
        val payment = paymentRepository.findById(paymentId)
            .orElseThrow { PaymentException.notFound() }

        val order = orderRepository.findById(payment.orderId)
            .orElseThrow { BusinessException("ORDER_NOT_FOUND", "주문을 찾을 수 없습니다") }

        if (order.userId != userId) throw BusinessException("ORDER_ACCESS_DENIED", "접근 권한이 없습니다")

        val cancelAmount = request.cancelAmount ?: payment.amount
        if (payment.cancelledAmount + cancelAmount > payment.amount) {
            throw PaymentException.cancelAmountExceeded()
        }

        tossClient.cancel(
            paymentKey = payment.paymentKey!!,
            cancelReason = request.cancelReason,
            cancelAmount = request.cancelAmount,
        )

        payment.cancel(cancelAmount)
        if (payment.status == PaymentStatus.CANCELLED) {
            order.markCancelled()
            orderRepository.save(order)
        }
        paymentRepository.save(payment)
        return PaymentResponse.from(payment)
    }

    @Transactional(readOnly = true)
    fun getPayment(paymentId: UUID, userId: UUID): PaymentResponse {
        val payment = paymentRepository.findById(paymentId)
            .orElseThrow { PaymentException.notFound() }
        val order = orderRepository.findById(payment.orderId)
            .orElseThrow { BusinessException("ORDER_NOT_FOUND", "주문 없음") }
        if (order.userId != userId) throw BusinessException("ORDER_ACCESS_DENIED", "접근 권한 없음")
        return PaymentResponse.from(payment)
    }
}
```

- [ ] **Step 5: 테스트 실행 — 통과 확인**

```bash
./gradlew test --tests "com.shl.payment.payment.application.PaymentServiceTest"
```

Expected: 3 tests PASSED

- [ ] **Step 6: Commit**

```bash
git add src/main/kotlin/com/shl/payment/payment/application/ src/test/kotlin/com/shl/payment/payment/application/
git commit -m "feat: add payment service with confirm, cancel, retry logic"
```

---

### Task 10: Payment Controller + Webhook + Batch

**Files:**
- Create: `src/main/kotlin/com/shl/payment/payment/presentation/PaymentController.kt`
- Create: `src/main/kotlin/com/shl/payment/payment/presentation/WebhookController.kt`
- Create: `src/main/kotlin/com/shl/payment/payment/application/PaymentScheduler.kt`

- [ ] **Step 1: PaymentController.kt 작성**

```kotlin
package com.shl.payment.payment.presentation

import com.shl.payment.common.response.ApiResponse
import com.shl.payment.payment.application.PaymentService
import com.shl.payment.payment.application.dto.CancelPaymentRequest
import com.shl.payment.payment.application.dto.ConfirmPaymentRequest
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import java.util.UUID

@RestController
@RequestMapping("/payments")
class PaymentController(
    private val paymentService: PaymentService,
) {
    @PostMapping("/confirm")
    fun confirm(
        @AuthenticationPrincipal userId: String,
        @RequestBody @Valid request: ConfirmPaymentRequest,
    ): ResponseEntity<ApiResponse<*>> {
        val result = paymentService.confirm(
            paymentKey = request.paymentKey,
            orderId = request.orderId,
            amount = request.amount,
            userId = UUID.fromString(userId),
        )
        return ResponseEntity.ok(ApiResponse.ok(result))
    }

    @PostMapping("/{paymentId}/cancel")
    fun cancel(
        @AuthenticationPrincipal userId: String,
        @PathVariable paymentId: UUID,
        @RequestBody @Valid request: CancelPaymentRequest,
    ): ResponseEntity<ApiResponse<*>> {
        val result = paymentService.cancel(paymentId, UUID.fromString(userId), request)
        return ResponseEntity.ok(ApiResponse.ok(result))
    }

    @GetMapping("/{paymentId}")
    fun getPayment(
        @AuthenticationPrincipal userId: String,
        @PathVariable paymentId: UUID,
    ): ResponseEntity<ApiResponse<*>> {
        val result = paymentService.getPayment(paymentId, UUID.fromString(userId))
        return ResponseEntity.ok(ApiResponse.ok(result))
    }
}
```

- [ ] **Step 2: WebhookController.kt 작성**

```kotlin
package com.shl.payment.payment.presentation

import com.shl.payment.payment.application.PaymentService
import com.shl.payment.payment.domain.PaymentMethod
import com.shl.payment.payment.domain.PaymentRepository
import com.shl.payment.payment.domain.PaymentStatus
import com.shl.payment.order.domain.OrderRepository
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.UUID

@RestController
@RequestMapping("/webhook")
class WebhookController(
    private val paymentRepository: PaymentRepository,
    private val orderRepository: OrderRepository,
    @Value("\${toss.webhook-secret}") private val webhookSecret: String,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @PostMapping("/payment")
    fun handlePaymentWebhook(@RequestBody payload: WebhookPayload): ResponseEntity<Void> {
        // 토스 Webhook 시크릿 검증
        if (payload.secret != webhookSecret) {
            log.warn("Webhook secret mismatch")
            return ResponseEntity.ok().build()  // 200 반환 (토스 재전송 방지)
        }

        log.info("Webhook received: paymentKey=${payload.paymentKey}, status=${payload.status}")

        if (payload.status != "DONE") return ResponseEntity.ok().build()

        val payment = paymentRepository.findByOrderId(UUID.fromString(payload.orderId))
            .orElse(null) ?: return ResponseEntity.ok().build()

        if (payment.status == PaymentStatus.READY) {
            // 서버 다운 후 복구 시나리오: DB 보정
            runCatching {
                val method = runCatching { PaymentMethod.valueOf(payload.method ?: "CARD") }
                    .getOrDefault(PaymentMethod.CARD)
                payment.confirm(payload.paymentKey, method)
                paymentRepository.save(payment)

                val order = orderRepository.findById(payment.orderId).orElse(null)
                order?.let {
                    it.markPaid()
                    orderRepository.save(it)
                }
                log.info("Webhook으로 결제 보정 완료: paymentKey=${payload.paymentKey}")
            }.onFailure {
                log.error("Webhook 보정 실패, 추후 배치에서 처리됨", it)
            }
        }

        return ResponseEntity.ok().build()
    }
}

data class WebhookPayload(
    val secret: String,
    val status: String,
    val paymentKey: String,
    val orderId: String,
    val method: String? = null,
)
```

- [ ] **Step 3: PaymentScheduler.kt 작성**

```kotlin
package com.shl.payment.payment.application

import com.shl.payment.payment.domain.PaymentRepository
import com.shl.payment.payment.domain.PaymentStatus
import com.shl.payment.payment.infrastructure.TossPaymentClient
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Component
class PaymentScheduler(
    private val paymentRepository: PaymentRepository,
    private val tossClient: TossPaymentClient,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @Scheduled(fixedDelay = 300_000)  // 5분마다
    @Transactional
    fun cancelStalePayments() {
        val threshold = LocalDateTime.now().minusMinutes(10)
        val stalePayments = paymentRepository.findByStatusAndRequestedAtBefore(
            status = PaymentStatus.READY,
            before = threshold,
        )

        if (stalePayments.isEmpty()) return

        log.info("미완료 결제 ${stalePayments.size}건 취소 처리 시작")

        stalePayments.forEach { payment ->
            runCatching {
                payment.paymentKey?.let { key ->
                    tossClient.cancel(key, "시간 초과로 인한 자동 취소")
                }
                payment.fail("10분 초과 미완료")
                paymentRepository.save(payment)
                log.info("미완료 결제 취소: id=${payment.id}")
            }.onFailure {
                log.error("미완료 결제 취소 실패: id=${payment.id}", it)
            }
        }
    }
}
```

- [ ] **Step 4: Commit**

```bash
git add src/main/kotlin/com/shl/payment/payment/presentation/ src/main/kotlin/com/shl/payment/payment/application/PaymentScheduler.kt
git commit -m "feat: add payment controller, webhook handler, batch scheduler"
```

---

### Task 11: Thymeleaf 프론트엔드

**Files:**
- Create: `src/main/resources/templates/index.html`
- Create: `src/main/resources/templates/auth/signup.html`
- Create: `src/main/resources/templates/auth/login.html`
- Create: `src/main/resources/templates/order/create.html`
- Create: `src/main/resources/templates/payment/checkout.html`
- Create: `src/main/resources/templates/payment/success.html`
- Create: `src/main/resources/templates/payment/fail.html`
- Create: `src/main/resources/templates/payment/history.html`
- Create: `src/main/kotlin/com/shl/payment/common/presentation/PageController.kt`

- [ ] **Step 1: PageController.kt 작성 (Thymeleaf 페이지 라우팅)**

```kotlin
package com.shl.payment.common.presentation

import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping

@Controller
class PageController {
    @GetMapping("/") fun index() = "index"
    @GetMapping("/auth/signup") fun signup() = "auth/signup"
    @GetMapping("/auth/login") fun login() = "auth/login"
    @GetMapping("/order/create") fun createOrder() = "order/create"
    @GetMapping("/payment/checkout") fun checkout() = "payment/checkout"
    @GetMapping("/payment/success") fun success() = "payment/success"
    @GetMapping("/payment/fail") fun fail() = "payment/fail"
    @GetMapping("/payment/history") fun history() = "payment/history"
}
```

- [ ] **Step 2: index.html 작성**

```html
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <title>결제 데모</title>
</head>
<body>
<h1>토스페이먼츠 결제 데모</h1>
<nav>
    <a href="/auth/signup">회원가입</a> |
    <a href="/auth/login">로그인</a> |
    <a href="/order/create">주문하기</a> |
    <a href="/payment/history">결제 내역</a>
</nav>
</body>
</html>
```

- [ ] **Step 3: auth/signup.html 작성**

```html
<!DOCTYPE html>
<html lang="ko">
<head><meta charset="UTF-8"><title>회원가입</title></head>
<body>
<h2>회원가입</h2>
<form id="signupForm">
    <input type="email" id="email" placeholder="이메일" required><br>
    <input type="password" id="password" placeholder="비밀번호 (8자 이상)" required><br>
    <button type="submit">회원가입</button>
</form>
<p id="message"></p>
<script>
document.getElementById('signupForm').addEventListener('submit', async (e) => {
    e.preventDefault();
    const res = await fetch('/auth/signup', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
            email: document.getElementById('email').value,
            password: document.getElementById('password').value
        })
    });
    const data = await res.json();
    if (data.success) {
        localStorage.setItem('accessToken', data.data.accessToken);
        localStorage.setItem('refreshToken', data.data.refreshToken);
        window.location.href = '/order/create';
    } else {
        document.getElementById('message').textContent = data.error.message;
    }
});
</script>
</body>
</html>
```

- [ ] **Step 4: auth/login.html 작성**

```html
<!DOCTYPE html>
<html lang="ko">
<head><meta charset="UTF-8"><title>로그인</title></head>
<body>
<h2>로그인</h2>
<form id="loginForm">
    <input type="email" id="email" placeholder="이메일" required><br>
    <input type="password" id="password" placeholder="비밀번호" required><br>
    <button type="submit">로그인</button>
</form>
<p id="message"></p>
<script>
document.getElementById('loginForm').addEventListener('submit', async (e) => {
    e.preventDefault();
    const res = await fetch('/auth/login', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
            email: document.getElementById('email').value,
            password: document.getElementById('password').value
        })
    });
    const data = await res.json();
    if (data.success) {
        localStorage.setItem('accessToken', data.data.accessToken);
        localStorage.setItem('refreshToken', data.data.refreshToken);
        window.location.href = '/order/create';
    } else {
        document.getElementById('message').textContent = data.error.message;
    }
});
</script>
</body>
</html>
```

- [ ] **Step 5: order/create.html 작성**

```html
<!DOCTYPE html>
<html lang="ko">
<head><meta charset="UTF-8"><title>주문 생성</title></head>
<body>
<h2>주문하기</h2>
<form id="orderForm">
    <input type="text" id="orderName" placeholder="상품명" value="테스트 상품" required><br>
    <input type="number" id="amount" placeholder="금액" value="10000" required><br>
    <button type="submit">주문 생성 및 결제</button>
</form>
<p id="message"></p>
<script>
document.getElementById('orderForm').addEventListener('submit', async (e) => {
    e.preventDefault();
    const token = localStorage.getItem('accessToken');
    if (!token) { window.location.href = '/auth/login'; return; }

    const res = await fetch('/orders', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json', 'Authorization': `Bearer ${token}` },
        body: JSON.stringify({
            orderName: document.getElementById('orderName').value,
            totalAmount: parseInt(document.getElementById('amount').value)
        })
    });
    const data = await res.json();
    if (data.success) {
        const order = data.data;
        window.location.href = `/payment/checkout?orderId=${order.orderId}&amount=${order.totalAmount}&orderName=${encodeURIComponent(order.orderName)}`;
    } else {
        document.getElementById('message').textContent = data.error.message;
    }
});
</script>
</body>
</html>
```

- [ ] **Step 6: payment/checkout.html 작성 (토스 SDK 연동)**

```html
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <title>결제</title>
    <script src="https://js.tosspayments.com/v2/standard"></script>
</head>
<body>
<h2>결제하기</h2>
<div id="payment-method"></div>
<div id="agreement"></div>
<button id="payment-button">결제하기</button>
<script>
const params = new URLSearchParams(window.location.search);
const orderId = params.get('orderId');
const amount = parseInt(params.get('amount'));
const orderName = params.get('orderName');

const tossPayments = TossPayments('토스페이먼츠_클라이언트_키_여기에');  // 클라이언트 키 교체 필요
const payment = tossPayments.payment({ customerKey: 'ANONYMOUS_' + orderId });

async function renderPaymentWidget() {
    await payment.renderPaymentMethods({
        selector: '#payment-method',
        variantKey: 'DEFAULT',
        amount: { currency: 'KRW', value: amount },
    });
    await payment.renderAgreement({ selector: '#agreement', variantKey: 'AGREEMENT' });
}

document.getElementById('payment-button').addEventListener('click', async () => {
    await payment.requestPayment({
        method: 'CARD',
        amount: { currency: 'KRW', value: amount },
        orderId: orderId,
        orderName: orderName,
        successUrl: window.location.origin + '/payment/success',
        failUrl: window.location.origin + '/payment/fail',
    });
});

renderPaymentWidget();
</script>
</body>
</html>
```

> **주의:** `토스페이먼츠_클라이언트_키_여기에` 를 토스 대시보드에서 발급받은 클라이언트 키로 교체.

- [ ] **Step 7: payment/success.html 작성 (결제 확인 API 호출)**

```html
<!DOCTYPE html>
<html lang="ko">
<head><meta charset="UTF-8"><title>결제 완료</title></head>
<body>
<h2>결제 처리 중...</h2>
<p id="message"></p>
<script>
const params = new URLSearchParams(window.location.search);
const paymentKey = params.get('paymentKey');
const orderId = params.get('orderId');
const amount = parseInt(params.get('amount'));
const token = localStorage.getItem('accessToken');

async function confirmPayment() {
    const res = await fetch('/payments/confirm', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json', 'Authorization': `Bearer ${token}` },
        body: JSON.stringify({ paymentKey, orderId, amount })
    });
    const data = await res.json();
    if (data.success) {
        document.querySelector('h2').textContent = '결제 완료!';
        document.getElementById('message').textContent =
            `결제 금액: ${data.data.amount.toLocaleString()}원`;
    } else {
        document.querySelector('h2').textContent = '결제 실패';
        document.getElementById('message').textContent = data.error.message;
    }
}

confirmPayment();
</script>
</body>
</html>
```

- [ ] **Step 8: payment/fail.html 작성**

```html
<!DOCTYPE html>
<html lang="ko">
<head><meta charset="UTF-8"><title>결제 실패</title></head>
<body>
<h2>결제 실패</h2>
<p id="message"></p>
<a href="/order/create">다시 시도</a>
<script>
const params = new URLSearchParams(window.location.search);
document.getElementById('message').textContent =
    params.get('message') || '결제 중 오류가 발생했습니다';
</script>
</body>
</html>
```

- [ ] **Step 9: payment/history.html 작성**

```html
<!DOCTYPE html>
<html lang="ko">
<head><meta charset="UTF-8"><title>결제 내역</title></head>
<body>
<h2>결제 내역</h2>
<div id="payment-list">로딩 중...</div>
<script>
// 결제 내역은 주문 목록에서 가져옴
// (GET /orders 구현 후 연동)
document.getElementById('payment-list').textContent = '준비 중';
</script>
</body>
</html>
```

- [ ] **Step 10: Commit**

```bash
git add src/main/kotlin/com/shl/payment/common/presentation/ src/main/resources/templates/
git commit -m "feat: add Thymeleaf frontend pages with Toss SDK integration"
```

---

### Task 12: 배포 설정

**Files:**
- Create: `Dockerfile`
- Create: `render.yaml`
- Modify: `src/main/resources/application-prod.yml`

- [ ] **Step 1: Dockerfile 작성**

```dockerfile
FROM gradle:8.7-jdk21-alpine AS build
WORKDIR /app
COPY build.gradle.kts settings.gradle.kts ./
COPY src ./src
RUN gradle bootJar --no-daemon

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

- [ ] **Step 2: render.yaml 작성**

```yaml
services:
  - type: web
    name: payment-by-tosspayments
    runtime: docker
    dockerfilePath: ./Dockerfile
    envVars:
      - key: SPRING_PROFILES_ACTIVE
        value: prod
      - key: DB_URL
        sync: false
      - key: DB_USERNAME
        sync: false
      - key: DB_PASSWORD
        sync: false
      - key: JWT_SECRET
        generateValue: true
      - key: TOSS_SECRET_KEY
        sync: false
      - key: TOSS_WEBHOOK_SECRET
        sync: false
```

- [ ] **Step 3: application-prod.yml 업데이트**

```yaml
spring:
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: false

server:
  port: 8080

logging:
  level:
    com.shl.payment: INFO
```

- [ ] **Step 4: .gitignore 확인 — 시크릿 키 미포함 여부 확인**

```bash
cat .gitignore
# 아래 항목들이 있는지 확인:
# *.env
# application-local.yml (포함 여부 선택)
```

application-local.yml은 실제 테스트 키가 들어가므로 .gitignore에 추가:

```bash
echo "src/main/resources/application-local.yml" >> .gitignore
```

- [ ] **Step 5: 전체 테스트 실행**

```bash
./gradlew test
```

Expected: All tests PASSED

- [ ] **Step 6: 최종 빌드 확인**

```bash
./gradlew bootJar
```

Expected: `BUILD SUCCESSFUL`, `build/libs/payment-0.0.1-SNAPSHOT.jar` 생성

- [ ] **Step 7: Commit**

```bash
git add Dockerfile render.yaml src/main/resources/application-prod.yml .gitignore
git commit -m "feat: add Dockerfile and Render deploy config"
```

---

## 배포 체크리스트

- [ ] Neon 계정 생성 → PostgreSQL DB 생성 → 연결 문자열 복사
- [ ] 토스페이먼츠 계정 → 테스트 시크릿 키 / 클라이언트 키 발급
- [ ] `checkout.html`의 클라이언트 키 교체
- [ ] Render 계정 생성 → GitHub 연동 → 환경변수 설정 (DB_URL, TOSS_SECRET_KEY 등)
- [ ] 토스 대시보드 → Webhook URL 등록: `https://<render-url>/webhook/payment`
- [ ] 토스 대시보드 → 성공 URL: `https://<render-url>/payment/success`

---

## 스펙 커버리지

| 스펙 항목 | 구현 태스크 |
|-----------|------------|
| JWT 인증 | Task 4, 5 |
| Order 1:N Payment | Task 6, 7 |
| 결제 승인 + 멱등성 | Task 9 |
| 재시도 (exponential backoff) | Task 9 |
| 상태 전이 검증 (도메인 메서드) | Task 7 |
| 보상 트랜잭션 (즉시 취소) | Task 9 |
| Webhook 안전망 | Task 10 |
| 배치 스케줄러 (10분 초과 취소) | Task 10 |
| 부분/전체 환불 | Task 9 |
| Thymeleaf UI | Task 11 |
| Render 배포 | Task 12 |
