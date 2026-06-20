# inventory 모듈

재고 관리 담당. **현재 미구현 (Phase 1 진행 예정)**

## 설계 방향

### 핵심 엔티티

```
Product         — 상품 (id, name, price)
Stock           — 재고 수량 (productId, quantity, @Version으로 낙관적 락)
StockReservation — 재고 예약 (orderId, productId, quantity, status)
```

### StockReservation 상태

```
RESERVED → CONFIRMED (결제 완료)
RESERVED → CANCELLED (결제 실패/취소)
```

### 흐름

```
주문 생성
  → StockReservation 생성 (RESERVED)
  → 결제 승인 완료 이벤트 수신
    → Stock.quantity -= reservedQty
    → StockReservation.status = CONFIRMED
  → 결제 실패/취소 이벤트 수신
    → StockReservation.status = CANCELLED
    → (Stock은 이미 차감 안 했으므로 복구 불필요)
```

### Saga 보상 트랜잭션 (Phase 4)

```
결제 성공 → 재고 확정 실패
  → PaymentCancelEvent 발행 → 결제 취소
```

## 파일 구조 (예정)

```
inventory/
├── domain/
│   ├── Product.kt
│   ├── Stock.kt                  # @Version 낙관적 락
│   ├── StockReservation.kt
│   ├── StockReservationStatus.kt
│   └── InventoryRepository.kt
├── application/
│   ├── InventoryService.kt       # reserveStock, confirmStock, cancelReservation
│   └── dto/
└── infrastructure/
    └── InventoryRepositoryImpl.kt
```

## 주의

- 재고 차감은 결제 확정 시점에만 (예약 ≠ 차감)
- 동시 예약 충돌은 `Stock.@Version` 낙관적 락으로 처리
- `InventoryService`를 다른 모듈에서 직접 호출 금지 → 이벤트 수신으로만 동작
