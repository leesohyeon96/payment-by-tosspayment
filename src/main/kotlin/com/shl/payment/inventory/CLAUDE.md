# inventory 모듈

재고 관리 담당. **현재 미구현 (Phase 1 진행 예정)**

## 패키지 구조 (예정)

```
inventory/
├── domain/
│   ├── Product.kt                # 상품 (id, name, price)
│   ├── Stock.kt                  # 재고 수량 (@Version 낙관적 락)
│   ├── StockReservation.kt       # 재고 예약
│   ├── StockReservationStatus.kt # RESERVED → CONFIRMED / CANCELLED
│   └── InventoryRepository.kt
├── application/
│   ├── InventoryService.kt       # reserveStock, confirmStock, cancelReservation
│   └── dto/
└── infrastructure/
    └── InventoryRepositoryImpl.kt
```

## 도메인 규칙

- 재고 차감은 결제 확정 시점에만 수행 (예약 ≠ 즉시 차감)
- `Stock.quantity` 동시 접근은 `@Version` 낙관적 락으로 처리
- `StockReservation` 상태: RESERVED → CONFIRMED (결제 완료), RESERVED → CANCELLED (결제 실패/취소)

## 모듈 금지 사항

- 다른 모듈에서 `InventoryService` 직접 호출 금지 → 이벤트 수신으로만 동작 (Phase 2)
- Phase 1에서는 임시로 직접 호출 허용, Phase 2에서 이벤트로 교체

## 할 일

→ `TODO.md` 참고
