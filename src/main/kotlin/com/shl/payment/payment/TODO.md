# payment 모듈 TODO

## Phase 2
- [ ] `PaymentConfirmedEvent` 발행 (결제 승인 완료 시)
- [ ] `PaymentCancelledEvent` 발행 (결제 취소 완료 시)
- [ ] `PaymentFailedEvent` 발행 (결제 실패/만료 시)
- [ ] `@TransactionalEventListener` 적용

## Phase 3
- [ ] Outbox 패턴 적용 — 이벤트 발행을 DB 트랜잭션과 원자적으로 처리
