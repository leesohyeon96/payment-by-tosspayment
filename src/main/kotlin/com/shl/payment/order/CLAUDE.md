# order 모듈

주문 생성 및 조회 담당.

## 도메인 규칙

- `Order` 생성 시 반드시 `Payment(READY)` 레코드도 같은 트랜잭션에서 생성 (`OrderService.createOrder`)
- 재고 예약도 같은 트랜잭션 내에서 처리 예정 (Phase 1 TODO)
- `OrderStatus`: PENDING → PAID → CANCELLED

## 이벤트 (Phase 2 예정)

- **발행**: `OrderCreatedEvent` — 주문 생성 시
- **수신**: `PaymentConfirmedEvent` → 주문 상태 PAID 전환
- **수신**: `PaymentCancelledEvent` → 주문 상태 CANCELLED 전환

## 파일 구조

```
order/
├── domain/
│   ├── Order.kt              # 엔티티
│   ├── OrderStatus.kt        # 상태 enum
│   └── OrderRepository.kt    # 레포지토리 인터페이스
├── application/
│   ├── OrderService.kt       # 비즈니스 로직
│   └── dto/
│       ├── CreateOrderRequest.kt
│       └── OrderResponse.kt
├── infrastructure/
│   └── OrderRepositoryImpl.kt
└── presentation/
    └── OrderController.kt    # POST /orders, GET /orders/{id}
```

## 주의

- `OrderService`는 `PaymentRepository`에 직접 의존 중 (Phase 2에서 이벤트로 교체 예정)
- 재고 예약 추가 시 `InventoryService` 직접 호출 금지 → 이벤트 방식으로
