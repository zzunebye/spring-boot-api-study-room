# Unsolved / Later

아직 해결하지 않았거나, 이후 단계에서 다룰 항목을 기록합니다.

## 테스트

- **동시성 통합 테스트 (5단계)** — `docs/dev-process.md` 5단계. Testcontainers PostgreSQL에서 같은 방·같은 시간으로 20개 동시 요청 → 성공 1건, 409 nineteen, DB `CONFIRMED` 1건 검증. H2 불가(gist exclusion). 예약 create API(4단계) 완료 후 진행 예정이었으나 **보류**. 다음 우선: 6단계 availability/`ReservationPolicy` 또는 7단계 `GET/DELETE /api/v1/me/reservations`.

## API / 예외 처리

- **traceId** — `ErrorResponse`에 요청별 trace ID 추가 (`bootstraping.md` §9 참고)

## 의존성 / 코드 스타일

- **Lombok 도입** — `@Getter`, `@Builder`, `@NoArgsConstructor` 등으로 Entity/DTO 보일러플레이트 축소 검토. 현재는 수동 getter/setter + static factory(`User.createForSignup`) 사용. 도입 시 `build.gradle.kts` 의존성 추가, IDE Lombok 플러그인, JPA Entity용 `@NoArgsConstructor(protected)` 등 migration 필요.
