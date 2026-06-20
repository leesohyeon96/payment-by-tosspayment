# payment 모듈

토스페이먼츠 API 연동 + 결제 상태 관리 담당.

## 도메인 규칙

- **상태 머신**: READY → DONE → CANCELLED, READY → FAILED (역방향 불가)
- `confirm()`: READY만 가능. paymentKey, method 설정 후 DONE 전환
- `cancel()`: DONE만 가능. 부분 취소 지원. 전액 취소 시 CANCELLED
- `fail()`: READY만 가능. 스케줄러 또는 웹훅에서 호출
- **멱등성**: `paymentKey` unique 제약 — 중복 confirm 요청 시 DB 레벨에서 차단
- **낙관적 락**: `@Version version` 필드 — 동시 상태 변경 충돌 감지

## 이벤트 (Phase 2 예정)

- **발행**: `PaymentConfirmedEvent` — 결제 승인 완료 시
- **발행**: `PaymentCancelledEvent` — 결제 취소 완료 시
- **발행**: `PaymentFailedEvent` — 결제 실패/만료 시

## 파일 구조

```
payment/
├── domain/
│   ├── Payment.kt                # 엔티티 (@Version 포함)
│   ├── PaymentStatus.kt          # READY/DONE/CANCELLED/FAILED
│   ├── PaymentMethod.kt          # 결제 수단 enum
│   ├── PaymentRepository.kt
│   └── vo/Money.kt
├── application/
│   ├── PaymentService.kt         # confirm, cancel, getHistory
│   ├── PaymentScheduler.kt       # READY 10분 초과 → FAILED
│   └── dto/
│       ├── ConfirmPaymentRequest.kt
│       ├── CancelPaymentRequest.kt
│       └── PaymentResponse.kt
├── infrastructure/
│   ├── PaymentRepositoryImpl.kt
│   ├── TossPaymentClient.kt      # WebClient로 Toss API 호출
│   └── dto/                      # Toss API 요청/응답 DTO
└── presentation/
    ├── PaymentController.kt      # POST /payments/confirm, POST /payments/{id}/cancel, GET /payments
    └── WebhookController.kt      # POST /webhook/toss
```

## 주의

- `TossPaymentClient`는 `.block()` 사용 (동기 호출) — 의도적
- 웹훅 검증: `TOSS_WEBHOOK_SECRET` 헤더 검증 필수
- `PaymentScheduler`는 `@Scheduled` — 로컬에서도 5분마다 실행됨
