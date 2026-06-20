# order 모듈

주문 생성 및 조회 담당.

## 패키지 구조

```
order/
├── domain/
│   ├── Order.kt              # 엔티티
│   ├── OrderStatus.kt        # PENDING → PAID → CANCELLED
│   └── OrderRepository.kt
├── application/
│   ├── OrderService.kt       # createOrder, getOrder
│   └── dto/
│       ├── CreateOrderRequest.kt
│       └── OrderResponse.kt
├── infrastructure/
│   └── OrderRepositoryImpl.kt
└── presentation/
    └── OrderController.kt    # POST /orders, GET /orders/{id}
```

## 도메인 규칙

- `Order` 생성 시 `Payment(READY)` 레코드를 **같은 트랜잭션**에서 함께 생성
- Phase 1 완료 후: `StockReservation`도 같은 트랜잭션에서 생성
- `OrderStatus`: PENDING → PAID → CANCELLED (역방향 불가)

## 모듈 금지 사항

- `InventoryService` 직접 호출 금지 → Phase 2 이후 이벤트로만 통신
- `PaymentRepository` 직접 주입은 Phase 2에서 이벤트로 교체 예정 — 그 전까지는 허용

## 할 일

→ `TODO.md` 참고
