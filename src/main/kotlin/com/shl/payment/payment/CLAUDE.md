# payment 모듈

토스페이먼츠 API 연동 + 결제 상태 관리 담당.

## 패키지 구조

```
payment/
├── domain/
│   ├── Payment.kt                # 엔티티 (@Version 낙관적 락 포함)
│   ├── PaymentStatus.kt          # READY / DONE / CANCELLED / FAILED
│   ├── PaymentMethod.kt
│   ├── PaymentRepository.kt
│   └── vo/Money.kt
├── application/
│   ├── PaymentService.kt         # confirm, cancel, getHistory
│   ├── PaymentScheduler.kt       # READY 10분 초과 → FAILED (5분 주기)
│   └── dto/
│       ├── ConfirmPaymentRequest.kt
│       ├── CancelPaymentRequest.kt
│       └── PaymentResponse.kt
├── infrastructure/
│   ├── PaymentRepositoryImpl.kt
│   ├── TossPaymentClient.kt      # WebClient로 Toss API 호출 (동기 .block())
│   └── dto/                      # Toss API 요청/응답 DTO
└── presentation/
    ├── PaymentController.kt      # POST /payments/confirm, POST /payments/{id}/cancel, GET /payments
    └── WebhookController.kt      # POST /webhook/toss
```

## 도메인 규칙

- **상태 머신**: READY → DONE → CANCELLED, READY → FAILED (역방향 불가)
- `confirm()`: READY만 가능. paymentKey unique 제약으로 중복 승인 방지
- `cancel()`: DONE만 가능. 부분 취소 지원. 전액 취소 시 CANCELLED
- `fail()`: READY만 가능. 스케줄러·웹훅에서 호출
- `@Version version`: 동시 상태 변경 충돌 감지 (낙관적 락)

## 모듈 금지 사항

- 결제 상태 전이 로직(`confirm`, `cancel`, `fail`) 외부에서 직접 변경 금지
- 웹훅 처리 시 `TOSS_WEBHOOK_SECRET` 헤더 검증 생략 금지

## 할 일

→ `TODO.md` 참고
