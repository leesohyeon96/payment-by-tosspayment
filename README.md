# 토스페이먼츠 결제 데모

토스페이먼츠 API를 연동한 결제 시스템 데모 프로젝트입니다.  
회원가입/로그인부터 주문 생성, 결제, 취소까지 전체 결제 플로우를 경험할 수 있습니다.

**[→ 데모 사이트](#)** _(배포 후 URL 추가 예정)_

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

## 주요 기능

- 회원가입 / 로그인 (JWT Access + Refresh Token)
- 주문 생성
- 토스페이먼츠 결제 위젯 연동
- 결제 승인 / 취소
- 결제 내역 조회
- 미완료 결제 자동 만료 처리 (스케줄러)

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

`src/main/resources/application-local.yml` 파일 생성:

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
./gradlew bootRun -Dspring.profiles.active=local
```

브라우저에서 `http://localhost:8080` 접속

---

# Toss Payments Demo

A payment system demo project integrating the Toss Payments API.  
Experience the full payment flow: sign up, log in, create an order, pay, and cancel.

**[→ Live Demo](#)** _(URL will be added after deployment)_

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

## Features

- Sign up / Login (JWT Access + Refresh Token)
- Order creation
- Toss Payments widget integration
- Payment confirmation / cancellation
- Payment history
- Auto-expiry of incomplete payments (scheduler)

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

Create `src/main/resources/application-local.yml`:

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
./gradlew bootRun -Dspring.profiles.active=local
```

Open `http://localhost:8080` in your browser
