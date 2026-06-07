# 결제 시스템 설계 스펙

## 개요

토스페이먼츠를 사용한 결제 시스템 사이드 프로젝트.
목적: 실무 수준의 결제 플로우(요청 → 승인 → 환불), 에러 처리, 후처리 학습.

---

## 기술 스택

| 항목 | 결정 | 선택 이유 |
|------|------|-----------|
| 언어/프레임워크 | Kotlin + Spring Boot + JPA | 실무에서 가장 많이 쓰는 조합. 학습 목적으로 실무 스택 그대로 경험 |
| 결제 PG | 토스페이먼츠 | 국내 최고 수준의 개발자 문서, 테스트 모드 완비, 국내 카드 전부 지원 |
| 인증 | JWT (Access + Refresh Token) | 현재 실무 트렌드, 면접 빈출 주제. 세션 기반 대비 stateless로 확장 용이 |
| DB | PostgreSQL (Neon 무료) | 결제는 트랜잭션/정합성 필수 → RDB. Neon은 무료 + 데이터 영구 보존 (Render 내장 DB는 90일 후 삭제) |
| 프론트 | Thymeleaf | 토스 SDK가 프론트에서 호출되므로 UI 필요. React 오버킬, Spring 내장으로 별도 배포 불필요 |
| 배포 | Render 무료 플랜 | 추가 과금 없음 (한도 초과 시 정지, 청구 아님). 신용카드 불필요 |
| 구조 | Modular Monolith + DDD | 단일 배포로 운영 단순화. 도메인별 bounded context 분리로 나중에 MSA 전환 시 서비스로 그대로 분리 가능 |

---

## 아키텍처

### 패키지 구조

```
com.shl.payment/
├── auth/
│   ├── domain/
│   │   ├── User.kt
│   │   ├── UserRepository.kt       # 인터페이스
│   │   └── vo/
│   │       └── Email.kt
│   ├── application/
│   │   ├── AuthService.kt
│   │   └── dto/
│   ├── infrastructure/
│   │   ├── UserRepositoryImpl.kt
│   │   └── JwtProvider.kt
│   └── presentation/
│       └── AuthController.kt
│
├── order/
│   ├── domain/
│   │   ├── Order.kt                # AggregateRoot
│   │   ├── OrderStatus.kt
│   │   └── OrderRepository.kt
│   ├── application/
│   ├── infrastructure/
│   └── presentation/
│
├── payment/
│   ├── domain/
│   │   ├── Payment.kt              # AggregateRoot
│   │   ├── PaymentStatus.kt
│   │   ├── PaymentMethod.kt
│   │   ├── PaymentRepository.kt
│   │   └── vo/
│   │       └── Money.kt
│   ├── application/
│   │   └── PaymentService.kt
│   ├── infrastructure/
│   │   ├── PaymentRepositoryImpl.kt
│   │   └── TossPaymentClient.kt    # WebClient
│   └── presentation/
│       └── PaymentController.kt
│
└── common/
    ├── exception/
    │   ├── GlobalExceptionHandler.kt
    │   └── BusinessException.kt
    └── response/
        └── ApiResponse.kt
```

---

## 데이터 모델

### User
```
id          UUID        PK
email       VARCHAR     UNIQUE NOT NULL
password    VARCHAR     NOT NULL (bcrypt)
createdAt   TIMESTAMP
```

### Order
```
id          UUID        PK
userId      UUID        FK → User
totalAmount BIGINT      NOT NULL
status      ENUM        PENDING | PAID | CANCELLED
createdAt   TIMESTAMP
```

### Payment (Order 1:N)
```
id              UUID        PK
orderId         UUID        FK → Order
paymentKey      VARCHAR     UNIQUE (토스 발급) - 멱등성 키
method          ENUM        CARD | VIRTUAL_ACCOUNT | 등
amount          BIGINT      NOT NULL
cancelledAmount BIGINT      DEFAULT 0
status          ENUM        READY | DONE | CANCELLED | FAILED
failReason      VARCHAR     NULLABLE
requestedAt     TIMESTAMP
approvedAt      TIMESTAMP   NULLABLE
cancelledAt     TIMESTAMP   NULLABLE
```

---

## 결제 플로우

```
[사용자]
  │
  ├── 1. POST /orders          → Order 생성 (PENDING)
  │                               Payment READY 선저장
  │
  ├── 2. 프론트 토스 SDK 호출   → 사용자가 카드 정보 입력
  │
  ├── 3. 토스 결제 완료         → 프론트가 paymentKey + orderId 받음
  │
  └── 4. POST /payments/confirm → 백엔드 토스 승인 API 호출

[백엔드 승인 처리]
  ├── 성공                     → Payment DONE, Order PAID
  ├── 일시 장애               → 재시도 3회 (exponential backoff)
  │   ├── 성공               → Payment DONE, Order PAID
  │   └── 전부 실패          → 즉시 토스 취소 API 호출 → FAILED → 사용자에게 실패 응답
  └── 복구 불가 오류          → 즉시 토스 취소 API 호출 → FAILED

[Webhook 안전망] - POST /webhook/payment (토스 서버 → 우리 백엔드, 프론트 무관)
  └── 서버 다운 후 복구 시 토스가 재전송
      ├── DB READY → DONE 보정 시도
      │   └── 보정 실패 → 토스 취소 API 호출
      └── DB 이미 DONE → 무시 (멱등성)
      * 인증: JWT 아님, 토스 HMAC 서명 검증

[배치 스케줄러] - 5~10분 주기
  └── READY 상태 + 10분 이상 경과한 Payment 탐색
      → 전부 토스 취소 API 호출 → FAILED
      (사용자는 이미 에러 보고 이탈한 상태)
```

---

## 핵심 구현 포인트

### 1. 멱등성

```kotlin
// orderId unique 제약으로 중복 요청 차단
// 승인 전 상태 선체크
fun confirm(orderId: UUID) {
    val payment = paymentRepository.findByOrderId(orderId)
    if (payment.status == PaymentStatus.DONE) return  // 이미 완료
    if (payment.status != PaymentStatus.READY) throw BusinessException("승인 불가 상태")
    // ...
}
```

### 2. 상태 전이 검증 (도메인 메서드)

```kotlin
class Payment : AggregateRoot() {

    fun confirm() {
        check(status == PaymentStatus.READY) { "READY 상태만 승인 가능" }
        status = PaymentStatus.DONE
        approvedAt = LocalDateTime.now()
    }

    fun cancel(amount: Long) {
        check(status == PaymentStatus.DONE || status == PaymentStatus.READY) {
            "취소 불가 상태: $status"
        }
        check(cancelledAmount + amount <= this.amount) { "취소 금액 초과" }
        cancelledAmount += amount
        if (cancelledAmount == this.amount) status = PaymentStatus.CANCELLED
        cancelledAt = LocalDateTime.now()
    }

    fun fail(reason: String) {
        check(status == PaymentStatus.READY) { "READY 상태만 실패 처리 가능" }
        status = PaymentStatus.FAILED
        failReason = reason
    }
}
```

불가능한 전이:
- `FAILED → DONE` : 차단
- `CANCELLED → DONE` : 차단
- `DONE → DONE` : 멱등성으로 무시

### 3. 보상 트랜잭션

```
토스 승인 성공
  └── DB 저장 시도
        ├── 성공 → 완료
        └── 실패 (일시적) → 재시도 3회
              └── 전부 실패 → 토스 취소 API 강제 호출
```

### 4. 환불

- 전체 환불: `payment.cancel(payment.amount)`
- 부분 환불: `payment.cancel(일부금액)`, `cancelledAmount` 누적 추적
- 토스 취소 API 호출 후 도메인 상태 업데이트

---

## 인증

- 회원가입: `POST /auth/signup`
- 로그인: `POST /auth/login` → Access Token + Refresh Token 발급
- Access Token: 30분, Authorization Bearer 헤더
- Refresh Token: 7일, HttpOnly Cookie
- 토큰 갱신: `POST /auth/refresh`
- Spring Security + JWT Filter

---

## API 엔드포인트

```
# 인증
POST /auth/signup
POST /auth/login
POST /auth/refresh

# 주문
POST /orders                  # 주문 생성
GET  /orders/{orderId}        # 주문 조회

# 결제
POST /payments/confirm        # 결제 승인 (토스 paymentKey + orderId)
POST /payments/{paymentId}/cancel  # 환불 (전체/부분)
GET  /payments/{paymentId}    # 결제 조회
GET  /payments                # 내 결제 목록

# Webhook
POST /webhook/payment         # 토스 웹훅 수신
```

---

## 에러 처리

```kotlin
@RestControllerAdvice
class GlobalExceptionHandler {
    // BusinessException → 400
    // PaymentException  → 결제 전용 에러 코드
    // 토스 API 오류    → 상태별 분기 처리
}

data class ApiResponse<T>(
    val success: Boolean,
    val data: T?,
    val error: ErrorResponse?
)
```

---

## 확장 고려사항 (현재 미구현)

- **Outbox Pattern**: PaymentConfirmed 이벤트를 DB에 저장 후 발행 → MSA 전환 시 필요
- **포인트 시스템**: Order 1:N Payment 구조로 이미 복합결제 대응 가능
- **알림**: 결제 완료 이메일/SMS

---

## 배포

- **백엔드 + 프론트**: Render 무료 (Web Service, 슬립 후 콜드스타트 ~30초)
- **DB**: Neon PostgreSQL 무료 (삭제 없음)
- **환경변수**: 토스 시크릿키, JWT 시크릿, DB URL
