# payment-by-tosspayments

토스페이먼츠 API 연동 결제 시스템. Kotlin + Spring Boot 3.3 + Spring Data JPA + Spring Security(JWT).

## 전역 금지 사항

- `application-local.yml` 수정·생성·내용 출력 금지 (실제 키 포함됨)
- DB 스키마 직접 변경 금지 — 엔티티 수정 후 JPA가 처리
- `ddl-auto` 설정 변경 금지 (로컬: create-drop / 운영: update)
- Security permitAll 무분별 확대 금지 — 페이지 GET만 허용, API는 인증 필수
- 새 예외 클래스 남발 금지 — `BusinessException` / `PaymentException` 사용

## 공통 컨벤션

- **응답**: 반드시 `ApiResponse.ok()` / `ApiResponse.fail()` 래퍼 사용
- **예외**: `BusinessException(code, message)` 또는 `PaymentException` 팩토리 메서드
- **테스트**: MockK 사용 (Mockito 아님)
- **트랜잭션**: 서비스 계층에 `@Transactional` 적용
- **URL**: 페이지(Thymeleaf) = 단수형 `/order/**`, API(REST) = 복수형 `/orders/**`

## 환경변수 (Railway 운영)

`DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `JWT_SECRET`, `TOSS_SECRET_KEY`, `TOSS_CLIENT_KEY`, `TOSS_WEBHOOK_SECRET`

## 배포

- 운영: Railway + Neon PostgreSQL
- 운영 URL: https://payment-by-tosspayments-production-6d2b.up.railway.app
- 브랜치: `develop` → Railway 자동 배포

## 전체 로드맵

→ `TODO.md` 참고
