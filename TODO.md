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

## Phase 7 — 선택 사항
- [ ] Kafka로 이벤트 버스 교체 (내부 이벤트 → 외부 메시지)
- [ ] 포인트 도메인 추가
- [ ] 쿠폰 도메인 추가 (동시성 처리 포함)
