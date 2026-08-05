# payment-by-tosspayments

토스페이먼츠 API 연동 결제 시스템. Kotlin + Spring Boot 3.3 + Spring Data JPA + Spring Security(JWT).

## 전역 금지 사항

- `application-local.yml` 수정·생성·내용 출력 금지 (실제 키 포함됨)
- DB 스키마 직접 변경 금지 — 엔티티 수정 후 JPA가 처리
- `ddl-auto` 설정 변경 금지 (로컬: create-drop / 운영: update)
- Security permitAll 무분별 확대 금지 — 페이지 GET만 허용, API는 인증 필수
- 사용자 허락 없이 git commit / push / merge 금지
- 새 예외 클래스 남발 금지 — `BusinessException` / `PaymentException` 사용

## 공통 컨벤션

- **응답**: 반드시 `ApiResponse.ok()` / `ApiResponse.fail()` 래퍼 사용
- **예외**: `BusinessException(code, message)` 또는 `PaymentException` 팩토리 메서드
- **테스트**: MockK 사용 (Mockito 아님)
- **트랜잭션**: 서비스 계층에 `@Transactional` 적용
- **URL**: 페이지(Thymeleaf) = 단수형 `/order/**`, API(REST) = 복수형 `/orders/**`

## 환경변수 (운영)

`SPRING_PROFILES_ACTIVE=prod`, `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `JWT_SECRET`, `TOSS_SECRET_KEY`, `TOSS_CLIENT_KEY`

## 배포

- 운영: Koyeb (Docker) + Supabase PostgreSQL (Session Pooler)
- 인스턴스: 512MB / 0.1 vCPU — JVM 옵션은 `Dockerfile`의 `JAVA_OPTS` 참고
- 유휴 1시간 후 슬립 → UptimeRobot으로 `/actuator/health` 30분 간격 핑
- 브랜치: `develop` → 자동 배포

## TODO.md 추적 규칙

작업 이력을 `TODO.md`에 남긴다. 규칙:
- 새 기능/개선 작업 시작 전 → Phase 항목에 `- [ ]` 태스크 추가
- 구현 완료 후 → `- [ ]` → `- [x]` 로 변경, Phase 제목에 `✅` 표시
- 커밋 전에 TODO 상태 반드시 업데이트

## 전체 로드맵

→ `TODO.md` 참고
