# payment-by-tosspayments

토스페이먼츠 API를 연동한 결제 시스템 데모. Kotlin + Spring Boot 3.3 + Spring Data JPA + Spring Security(JWT).

## 프로젝트 구조

```
com.shl.payment
├── auth/           # 회원가입·로그인·JWT (Access+Refresh 토큰)
├── order/          # 주문 생성·조회
├── payment/        # 결제 승인·취소·내역·웹훅·스케줄러
└── common/         # 공통 응답·예외·설정·페이지 컨트롤러
```

각 도메인은 `domain` / `application` / `infrastructure` / `presentation` 4레이어로 분리.

## 핵심 도메인 규칙

- **결제 상태 머신**: READY → DONE → CANCELLED, READY → FAILED. 역방향 전이 불가
- **OrderService**: 주문 생성 시 `Payment(READY)` 레코드를 같은 트랜잭션 내 함께 생성
- **PaymentScheduler**: 10분 이상 READY 상태인 결제 → FAILED (5분 주기)
- **멱등성**: `paymentKey` DB unique 제약으로 중복 승인 방지
- **낙관적 락**: `Payment.version(@Version)` 으로 동시 상태 변경 충돌 감지

## URL 규칙

- 페이지 라우트 (Thymeleaf): `/order/**`, `/payment/**`, `/auth/**` — 단수형
- REST API: `/orders/**`, `/payments/**`, `/auth/**` — 복수형(도메인별)

## 금지 사항

- `application-local.yml` 파일 수정·생성·내용 출력 금지 (실제 키 포함, gitignore)
- DB 스키마 직접 변경 금지 — 엔티티 수정 후 JPA가 처리
- `ddl-auto: create-drop` (로컬) / `ddl-auto: update` (운영) 설정 변경 금지
- Security permitAll 범위 무분별 확대 금지 — 페이지 GET만 허용, API는 인증 필수
- 기존 결제 상태 전이 로직(`confirm`, `cancel`, `fail`) 임의 변경 금지

## 구현·리팩토링 시 주의사항

- 새 API 엔드포인트 추가 시 SecurityConfig permitAll 여부 명시적으로 확인
- 서비스 계층은 `@Transactional` 적용 — 특히 OrderService.createOrder (Order+Payment 동시 생성)
- 예외는 `BusinessException` 또는 `PaymentException` 사용, 새 예외 클래스 남발 금지
- 응답은 반드시 `ApiResponse.ok()` / `ApiResponse.fail()` 래퍼 사용
- 테스트 시 MockK 사용 (Mockito 아님)
- `PaymentResponse.from(payment)` 팩토리 메서드로 도메인→DTO 변환

## 환경변수 (Railway 운영)

`DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `JWT_SECRET`, `TOSS_SECRET_KEY`, `TOSS_CLIENT_KEY`, `TOSS_WEBHOOK_SECRET`

## 배포

- 운영: Railway + Neon PostgreSQL
- 운영 URL: https://payment-by-tosspayments-production-6d2b.up.railway.app
- 브랜치: `develop` → Railway 자동 배포
