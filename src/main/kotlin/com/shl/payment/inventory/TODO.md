# inventory 모듈 TODO

## Phase 1 — 재고 도메인 구현
- [ ] `Product` 엔티티 생성
- [ ] `Stock` 엔티티 생성 (`@Version` 낙관적 락)
- [ ] `StockReservation` 엔티티 생성
- [ ] `StockReservationStatus` enum (RESERVED / CONFIRMED / CANCELLED)
- [ ] `InventoryRepository` 인터페이스 + Impl
- [ ] `InventoryService.reserveStock(orderId, productId, quantity)`
- [ ] `InventoryService.confirmStock(orderId)` — 예약 → 차감 확정
- [ ] `InventoryService.cancelReservation(orderId)` — 예약 해제
- [ ] `OrderService`에서 주문 생성 시 `reserveStock` 연동
- [ ] 재고 부족 시 주문 실패 처리 (예외 정의)
- [ ] 단위 테스트 작성

## Phase 2
- [ ] `PaymentConfirmedEvent` 수신 → `confirmStock` 호출
- [ ] `PaymentCancelledEvent` / `PaymentFailedEvent` 수신 → `cancelReservation` 호출
- [ ] `OrderService`의 직접 호출 제거

## Phase 4
- [ ] 재고 확정 실패 시 `PaymentCancelEvent` 발행 (Saga 보상 트랜잭션)
