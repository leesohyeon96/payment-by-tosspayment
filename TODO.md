# 전체 로드맵

## Phase 1 — 재고 도메인 추가
→ `src/main/kotlin/com/shl/payment/inventory/TODO.md` 참고

## Phase 2 — 내부 이벤트 버스 (Spring Application Event)
→ order / payment / inventory 모듈 TODO 참고

## Phase 3 — Outbox Pattern
- [ ] `OutboxEvent` 엔티티 및 테이블 생성
- [ ] 이벤트 발행을 DB 트랜잭션과 같은 원자적 단위로 처리
- [ ] Outbox 폴링 스케줄러 구현

## Phase 4 — Saga (보상 트랜잭션)
- [ ] 결제 성공 + 재고 확정 실패 → 결제 취소 보상 트랜잭션
- [ ] Saga 상태 추적 (SagaLog)
- [ ] 실패 시나리오 테스트 작성

## Phase 5 — 선택 사항
- [ ] Kafka로 이벤트 버스 교체 (내부 이벤트 → 외부 메시지)
- [ ] 포인트 도메인 추가
- [ ] 쿠폰 도메인 추가 (동시성 처리 포함)
