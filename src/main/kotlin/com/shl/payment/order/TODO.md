# order 모듈 TODO

## Phase 1
- [ ] `OrderCreatedEvent` 정의 (재고 예약 트리거용, Phase 2 준비)
- [ ] 주문 생성 시 재고 예약 연동 (`InventoryService.reserveStock` 호출 — Phase 2 전까지 임시 직접 호출)

## Phase 2
- [ ] `OrderCreatedEvent` 발행 (`ApplicationEventPublisher`)
- [ ] `PaymentConfirmedEvent` 수신 → `Order.status = PAID`
- [ ] `PaymentCancelledEvent` / `PaymentFailedEvent` 수신 → `Order.status = CANCELLED`
- [ ] `PaymentRepository` 직접 의존 제거
