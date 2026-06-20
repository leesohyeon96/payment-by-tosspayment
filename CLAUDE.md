# payment-by-tosspayments

토스페이먼츠 API를 연동한 결제 시스템. 현재 고도화 진행 중 — 이벤트 기반 Saga 패턴 + Outbox 패턴 도입 목표.

## 프로젝트 구조

```
com.shl.payment
├── auth/           # 회원가입·로그인·JWT (Access+Refresh 토큰)
├── order/          # 주문 생성·조회
├── payment/        # 결제 승인·취소·내역·웹훅·스케줄러
├── inventory/      # 재고 관리 (WIP)
└── common/         # 공통 응답·예외·설정·페이지 컨트롤러
```

각 도메인은 `domain` / `application` / `infrastructure` / `presentation` 4레이어로 분리.

## 핵심 도메인 규칙

- **결제 상태 머신**: READY → DONE → CANCELLED, READY → FAILED. 역방향 전이 불가
- **OrderService**: 주문 생성 시 `Payment(READY)` 레코드를 같은 트랜잭션 내 함께 생성
- **PaymentScheduler**: 10분 이상 READY 상태인 결제 → FAILED (5분 주기)
- **멱등성**: `paymentKey` DB unique 제약으로 중복 승인 방지
- **낙관적 락**: `Payment.version(@Version)` 으로 동시 상태 변경 충돌 감지

## URL 규칙

- 페이지 라우트 (Thymeleaf): `/order/**`, `/payment/**`, `/auth/**` — 단수형
- REST API: `/orders/**`, `/payments/**`, `/auth/**` — 복수형

## 금지 사항

- `application-local.yml` 파일 수정·생성·내용 출력 금지 (실제 키 포함, gitignore)
- DB 스키마 직접 변경 금지 — 엔티티 수정 후 JPA가 처리
- `ddl-auto: create-drop` (로컬) / `ddl-auto: update` (운영) 설정 변경 금지
- Security permitAll 범위 무분별 확대 금지 — 페이지 GET만 허용, API는 인증 필수
- 기존 결제 상태 전이 로직(`confirm`, `cancel`, `fail`) 임의 변경 금지

## 구현·리팩토링 시 주의사항

- 새 API 엔드포인트 추가 시 SecurityConfig permitAll 여부 명시적으로 확인
- 서비스 계층은 `@Transactional` 적용
- 예외는 `BusinessException` 또는 `PaymentException` 사용, 새 예외 클래스 남발 금지
- 응답은 반드시 `ApiResponse.ok()` / `ApiResponse.fail()` 래퍼 사용
- 테스트 시 MockK 사용 (Mockito 아님)

## 환경변수 (Railway 운영)

`DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `JWT_SECRET`, `TOSS_SECRET_KEY`, `TOSS_CLIENT_KEY`, `TOSS_WEBHOOK_SECRET`

## 배포

- 운영: Railway + Neon PostgreSQL
- 운영 URL: https://payment-by-tosspayments-production-6d2b.up.railway.app
- 브랜치: `develop` → Railway 자동 배포

---

## TODO (고도화 로드맵)

### Phase 1 — 재고 도메인 추가
- [ ] `inventory` 모듈 생성 (Product, Stock 엔티티)
- [ ] 주문 생성 시 재고 예약 (StockReservation)
- [ ] 결제 완료 이벤트 수신 → 재고 차감 확정
- [ ] 결제 취소/실패 이벤트 수신 → 재고 예약 해제

### Phase 2 — 내부 이벤트 버스 (Spring Application Event)
- [ ] `OrderCreatedEvent`, `PaymentConfirmedEvent`, `PaymentCancelledEvent` 정의
- [ ] 도메인 간 직접 의존 제거 → 이벤트로 통신
- [ ] `@TransactionalEventListener` 적용

### Phase 3 — Outbox Pattern
- [ ] `OutboxEvent` 엔티티 및 테이블 생성
- [ ] 이벤트 발행을 DB 트랜잭션과 같은 원자적 단위로 처리
- [ ] Outbox 폴링 스케줄러 구현

### Phase 4 — Saga (보상 트랜잭션)
- [ ] 결제 성공 + 재고 차감 실패 → 결제 취소 보상 트랜잭션
- [ ] Saga 상태 추적 (SagaLog)
- [ ] 실패 시나리오 테스트 작성

### Phase 5 — 선택 사항
- [ ] Kafka로 이벤트 버스 교체 (내부 이벤트 → 외부 메시지)
- [ ] 포인트 도메인 추가
- [ ] 쿠폰 도메인 추가 (동시성 처리 포함)
