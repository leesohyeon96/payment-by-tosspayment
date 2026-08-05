# 전체 로드맵

## Phase 1 — 재고 도메인 추가 ✅
- [x] Stock, StockReservation 엔티티 구현
- [x] 낙관적 락(@Version) 적용
- [x] StockService (예약/확정/취소)

## Phase 2 — 내부 이벤트 버스 (Spring Application Events) ✅
- [x] Spring ApplicationEventPublisher 기반 이벤트 발행
- [x] @TransactionalEventListener 리스너 구현
- [x] order ↔ payment ↔ inventory 모듈 간 이벤트 연결

## Phase 3 — Outbox Pattern ✅
- [x] OutboxEvent 엔티티 및 테이블 생성
- [x] 이벤트 발행을 DB 트랜잭션과 같은 원자적 단위로 처리
- [x] Outbox 폴링 릴레이 스케줄러 구현 (5초 주기)

## Phase 4 — Saga (보상 트랜잭션) ✅
- [x] Choreography 기반 Saga 구현
- [x] Payment 생성 실패 → 주문 취소 보상 트랜잭션
- [x] 결제 시간 초과 → 주문 취소 + 재고 해제 보상 트랜잭션
- [x] REQUIRES_NEW 트랜잭션 격리 (PaymentCreationService 분리)

## Phase 5 — Inbox Pattern + 재고 연결 ✅
- [x] 모든 이벤트에 eventId 추가
- [x] ProcessedEvent 엔티티 + 리포지토리 구현
- [x] 모든 리스너에 중복 처리 방지 체크 적용
- [x] OrderCreatedEvent에 productId/quantity 추가
- [x] 주문 생성 시 재고 자동 예약 (InventoryEventListener.onOrderCreated)

## Phase 6 — DDD 강화 (Value Object + Aggregate) ✅
- [x] Money Value Object 구현 (금액 도메인 개념화, JPA AttributeConverter)
- [x] Quantity Value Object 구현 (수량 도메인 개념화, JPA AttributeConverter)
- [x] OrderItem 엔티티 구현 (Order Aggregate Root 완성)
- [x] Order에서 단일 productId/quantity 제거 → OrderItems 리스트로 교체
- [x] 다중 상품 주문 지원 (CreateOrderRequest items 리스트)

## Phase 6.5 — 배포 크래시 수정 + 매핑/정합성 보정 ✅
- [x] `order_items.order_id` 중복 매핑 제거 (DuplicateMappingException으로 기동 실패)
- [x] `@TransactionalEventListener` + `@Transactional` 충돌 해소
      (BEFORE_COMMIT은 어노테이션 제거, AFTER_COMMIT은 REQUIRES_NEW)
- [x] Order↔OrderItem 양방향 전환 — 아이템당 불필요한 UPDATE 제거 (INSERT 2/UPDATE 2 → INSERT 2/UPDATE 0)
- [x] `ProcessedEventRepositoryImpl`을 `em.persist`로 변경
      (할당식 @Id라 `save()`가 merge로 동작해 중복이 조용히 무시되던 문제)
- [x] 웹훅 secret 검증 제거 → Toss API 재조회로 검증 (토스는 결제별 secret만 제공)
- [x] 알 수 없는 결제수단 수신 시 경고 로그
- [x] 컨텍스트 로딩 스모크 테스트 추가 (`PaymentApplicationTests`) — 위 매핑/트랜잭션 버그를 잡아냄
- [x] 배포 대상 Railway → Render 전환 (트라이얼 만료)

## Phase 7 — 선택 사항
- [ ] Kafka로 이벤트 버스 교체 (내부 이벤트 → 외부 메시지) — 아래 "알려진 한계" 해결
- [ ] 포인트 도메인 추가
- [ ] 쿠폰 도메인 추가 (동시성 처리 포함)

---

## 알려진 한계 — AFTER_COMMIT 리스너 실패 시 복구 불가

**현상**

`InventoryEventListener`는 `AFTER_COMMIT` + `REQUIRES_NEW`로 동작한다. 원본 트랜잭션이
커밋된 뒤 별도 트랜잭션으로 재고를 다루므로, 여기서 예외가 나면 결제·주문은 이미 커밋된
상태로 남고 재고만 어긋난다. 롤백 대상이 아니고 재시도 장치도 없어 그대로 유실된다.

```
[결제 확정 트랜잭션]
  ├─ Payment 상태 변경
  ├─ BEFORE_COMMIT → OrderEventListener: order.markPaid()   ← 원본 트랜잭션에 포함
  └─ COMMIT ────────────────────────────────────────────┐
                                                         │
     AFTER_COMMIT → InventoryEventListener (REQUIRES_NEW)┘  ← 실패해도 아무도 모름
```

`OrderEventListener`는 `BEFORE_COMMIT`이라 원본과 원자적으로 커밋된다. 두 리스너의
phase가 달라 같은 이벤트인데 정합성 보장 수준이 다르다.

**이미 갖춰진 것**

- `OutboxEvent` — 이벤트 유실 방지
- `ProcessedEvent` — 중복 처리 방지 (`em.persist`로 PK 위반을 일으켜 중복 트랜잭션 롤백)
- 보상 트랜잭션 — `PaymentFailedEvent` → `cancelReservation()`

즉 Saga 골격은 있고, **빠진 건 리스너 실패 시의 재시도 계층**이다.

**해결 방향 (택1)**

1. Kafka 도입 — 컨슈머 재시도 + DLQ가 이 역할을 대신한다 (Phase 7 항목)
2. `OutboxEventRelay`를 재시도 큐로 사용 — 리스너 처리 완료까지 확인하고 미완료 건 재발행
3. 재고 정합성 점검 스케줄러 — 예약됐지만 주문이 취소된 건 등을 주기적으로 보정

**미결정** — 파급이 커서 설계 확정 후 진행.
