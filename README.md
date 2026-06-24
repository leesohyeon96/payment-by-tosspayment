# 토스페이먼츠 결제 시스템

토스페이먼츠 API를 연동한 결제 시스템 데모 프로젝트입니다.  
회원가입/로그인부터 주문 생성, 재고 예약, 결제, 취소까지 전체 결제 플로우를 구현했습니다.

**[→ 데모 사이트](https://payment-by-tosspayments-production-6d2b.up.railway.app)**

---

## 기술 스택

| 분류 | 기술 |
|------|------|
| Language | Kotlin |
| Framework | Spring Boot 3.3 |
| ORM | Spring Data JPA (Hibernate) |
| Security | Spring Security + JWT |
| DB (운영) | PostgreSQL (Neon) |
| DB (로컬) | PostgreSQL (Docker) |
| 배포 | Railway |
| 결제 | 토스페이먼츠 API |
| View | Thymeleaf + Vanilla JS |

---

## 아키텍처

### 멀티모듈 구성

```
payment-by-tosspayments/
├── app/          # 애플리케이션 진입점 (Spring Boot main, Outbox relay)
├── common/       # 공통 모듈 (이벤트, 예외, Outbox/Inbox 엔티티, 포트 인터페이스)
├── auth/         # 인증 모듈 (회원가입, 로그인, JWT)
├── order/        # 주문 모듈 (주문 생성/조회)
├── payment/      # 결제 모듈 (토스페이먼츠 연동, 결제 승인/취소)
└── inventory/    # 재고 모듈 (재고 예약/확정/취소)
```

모듈 간 직접 의존 없이 `common` 모듈의 이벤트와 포트 인터페이스를 통해 통신합니다.

### 이벤트 기반 아키텍처

```
주문 생성
  └─▶ [outbox_events: ORDER_CREATED]
         └─▶ OutboxEventRelay (5초 폴링)
               ├─▶ PaymentEventListener    → Payment 생성 (REQUIRES_NEW)
               └─▶ InventoryEventListener  → 재고 예약 (AFTER_COMMIT)

결제 승인
  └─▶ [outbox_events: PAYMENT_CONFIRMED]
         └─▶ OutboxEventRelay
               ├─▶ OrderEventListener      → 주문 PAID 처리
               └─▶ InventoryEventListener  → 재고 예약 확정

결제 실패/취소 (보상 트랜잭션)
  └─▶ [outbox_events: PAYMENT_CANCELLED / PAYMENT_FAILED / PAYMENT_CREATION_FAILED]
         └─▶ OutboxEventRelay
               ├─▶ OrderEventListener      → 주문 CANCELLED 처리
               └─▶ InventoryEventListener  → 재고 예약 취소
```

---

## 주요 기능

- 회원가입 / 로그인 (JWT Access + Refresh Token)
- 주문 생성 (상품 ID, 수량 포함)
- 재고 예약 (주문 생성 시 자동 예약, 결제 완료 시 확정)
- 토스페이먼츠 결제 위젯 연동
- 결제 승인 / 취소
- 결제 내역 조회
- 미완료 결제 자동 만료 처리 (5분 주기 스케줄러)

---

## 기술적 고려사항

### Outbox Pattern
비즈니스 트랜잭션과 이벤트 발행의 원자성 보장.  
주문/결제 저장과 이벤트 저장을 동일 트랜잭션으로 처리하고, 별도 릴레이 스케줄러가 발행.

### Inbox Pattern
이벤트 중복 처리 방지. 각 리스너가 이벤트를 처리하기 전 `processed_events` 테이블을 확인하여 멱등성 보장.  
처리 키: `{eventId}:{모듈}:{액션}` (예: `uuid:inventory:reserve`)

### Choreography-based Saga (보상 트랜잭션)
분산 트랜잭션 없이 이벤트로 보상 처리:
- Payment 생성 실패 → `PAYMENT_CREATION_FAILED` 이벤트 → 주문 취소 + 재고 예약 취소
- 결제 시간 초과 → `PAYMENT_FAILED` 이벤트 → 주문 취소 + 재고 예약 취소

### 낙관적 락 (Optimistic Locking)
`Stock` / `Payment` 엔티티에 `@Version` 적용, 동시 재고/결제 변경 충돌 감지.

### REQUIRES_NEW 트랜잭션 격리
이벤트 리스너에서 실패 시 원본 트랜잭션 오염 방지.  
`PaymentCreationService`를 별도 빈으로 분리하여 Spring AOP 프록시를 통한 `REQUIRES_NEW` 적용.

### 결제 상태 머신
단방향 전이만 허용: `READY → DONE → CANCELLED`, `READY → FAILED`

### 재시도 + 자동 취소
결제 승인 실패 시 지수 백오프(최대 3회) 재시도. 10분 이상 READY 상태 결제는 토스페이먼츠에도 취소 요청 후 FAILED 처리.

---

## 로컬 실행

### 사전 준비

- Java 21
- Docker (PostgreSQL 실행용)
- 토스페이먼츠 테스트 키 ([개발자센터](https://developers.tosspayments.com) 발급)

### 1. PostgreSQL 실행

```bash
docker run -d \
  --name payment-db \
  -e POSTGRES_DB=payment \
  -e POSTGRES_USER=sa \
  -e POSTGRES_PASSWORD=password \
  -p 5432:5432 \
  postgres:15
```

### 2. application-local.yml 생성

`app/src/main/resources/application-local.yml` 파일 생성:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/payment
    username: sa
    password: password
  jpa:
    hibernate:
      ddl-auto: create-drop
    show-sql: true

jwt:
  secret: local-secret-key-must-be-at-least-32-characters-long
  access-expiration: 1800000
  refresh-expiration: 604800000

toss:
  secret-key: test_sk_여기에_시크릿키_입력
  client-key: test_ck_여기에_클라이언트키_입력
  base-url: https://api.tosspayments.com
  webhook-secret: local-webhook-secret
```

### 3. 실행

```bash
./gradlew :app:bootRun -Dspring.profiles.active=local
```

브라우저에서 `http://localhost:8080` 접속

---

# Toss Payments System

A payment system demo integrating the Toss Payments API.  
Covers the full payment flow: sign up, create an order, reserve stock, pay, and cancel.

**[→ Live Demo](https://payment-by-tosspayments-production-6d2b.up.railway.app)**

---

## Tech Stack

| Category | Technology |
|----------|------------|
| Language | Kotlin |
| Framework | Spring Boot 3.3 |
| ORM | Spring Data JPA (Hibernate) |
| Security | Spring Security + JWT |
| DB (Production) | PostgreSQL (Neon) |
| DB (Local) | PostgreSQL (Docker) |
| Deployment | Railway |
| Payment | Toss Payments API |
| View | Thymeleaf + Vanilla JS |

---

## Architecture

### Multi-Module Structure

```
payment-by-tosspayments/
├── app/          # Application entry point (Spring Boot main, Outbox relay)
├── common/       # Shared module (events, exceptions, Outbox/Inbox entities, port interfaces)
├── auth/         # Authentication (sign up, login, JWT)
├── order/        # Order module (create/query orders)
├── payment/      # Payment module (Toss Payments integration, confirm/cancel)
└── inventory/    # Inventory module (reserve/confirm/cancel stock)
```

Modules communicate through events and port interfaces defined in `common` — no direct cross-module dependencies.

### Event-Driven Flow

```
Order Created
  └─▶ [outbox_events: ORDER_CREATED]
         └─▶ OutboxEventRelay (5s polling)
               ├─▶ PaymentEventListener    → Create Payment (REQUIRES_NEW)
               └─▶ InventoryEventListener  → Reserve stock (AFTER_COMMIT)

Payment Confirmed
  └─▶ [outbox_events: PAYMENT_CONFIRMED]
         └─▶ OutboxEventRelay
               ├─▶ OrderEventListener      → Mark order PAID
               └─▶ InventoryEventListener  → Confirm stock reservation

Payment Failed/Cancelled (Compensating Transactions)
  └─▶ [outbox_events: PAYMENT_CANCELLED / PAYMENT_FAILED / PAYMENT_CREATION_FAILED]
         └─▶ OutboxEventRelay
               ├─▶ OrderEventListener      → Mark order CANCELLED
               └─▶ InventoryEventListener  → Cancel stock reservation
```

---

## Key Technical Decisions

### Outbox Pattern
Guarantees atomicity between business transactions and event publishing.  
Business data and events are saved in the same transaction; a relay scheduler handles publishing separately.

### Inbox Pattern
Prevents duplicate event processing. Each listener checks `processed_events` before acting.  
Key format: `{eventId}:{module}:{action}` (e.g. `uuid:inventory:reserve`)

### Choreography-based Saga
Compensating transactions via events, no distributed locks:
- Payment creation failure → `PAYMENT_CREATION_FAILED` → cancel order + release stock
- Payment timeout → `PAYMENT_FAILED` → cancel order + release stock

### Optimistic Locking
`@Version` on `Stock` and `Payment` entities detects concurrent modification conflicts.

### REQUIRES_NEW Transaction Isolation
Prevents a failed listener from poisoning the relay's transaction.  
`PaymentCreationService` extracted as a separate bean so Spring AOP proxy applies `REQUIRES_NEW` correctly.

### Payment State Machine
One-way transitions only: `READY → DONE → CANCELLED`, `READY → FAILED`

### Retry + Auto-cancel
Exponential backoff retry (up to 3x) on payment confirmation failure.  
Payments stuck in READY for 10+ minutes are cancelled on Toss and marked FAILED every 5 minutes.

---

## Local Setup

### Prerequisites

- Java 21
- Docker (for PostgreSQL)
- Toss Payments test keys ([Developer Console](https://developers.tosspayments.com))

### 1. Run PostgreSQL

```bash
docker run -d \
  --name payment-db \
  -e POSTGRES_DB=payment \
  -e POSTGRES_USER=sa \
  -e POSTGRES_PASSWORD=password \
  -p 5432:5432 \
  postgres:15
```

### 2. Create application-local.yml

Create `app/src/main/resources/application-local.yml`:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/payment
    username: sa
    password: password
  jpa:
    hibernate:
      ddl-auto: create-drop
    show-sql: true

jwt:
  secret: local-secret-key-must-be-at-least-32-characters-long
  access-expiration: 1800000
  refresh-expiration: 604800000

toss:
  secret-key: test_sk_your_secret_key_here
  client-key: test_ck_your_client_key_here
  base-url: https://api.tosspayments.com
  webhook-secret: local-webhook-secret
```

### 3. Run

```bash
./gradlew :app:bootRun -Dspring.profiles.active=local
```

Open `http://localhost:8080` in your browser
