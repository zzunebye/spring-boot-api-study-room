단순한 CRUD 예제가 아니라, 실제 서비스처럼 작은 운영 가능한 서버를 만든 뒤 기능과 시스템 컴포넌트를 단계적으로 붙이는 방식이 좋습니다.

전체 과정은 다음 흐름으로 진행합니다.

```text
Spring Boot 실행
→ PostgreSQL 연결
→ 공간 조회 API
→ 사용자 인증
→ 예약과 동시성
→ 운영 정책
→ 관리자 기능
→ 비동기 알림
→ Redis
→ 모니터링
→ CI/CD와 배포
→ 부하·장애 테스트
```

기간은 주당 8~12시간 기준 약 10~12주지만, 각 단계를 완료 조건 중심으로 진행하면 됩니다.

# 공통 개발 원칙

모든 단계는 다음 사이클로 진행합니다.

```text
요구사항 작성
→ API와 DB 설계
→ Flyway migration
→ 기능 구현
→ 자동 테스트
→ 로컬에서 실제 호출
→ 스테이징 배포
→ 로그와 DB 상태 확인
```

각 단계가 끝날 때마다 반드시 다음을 남깁니다.

- 실행 가능한 코드
- Flyway migration
- 자동 테스트
- HTTP 요청 예시
- README 사용법
- Git 커밋
- 실제 실행 결과

초기 구조는 모듈형 모놀리스로 유지합니다.

```text
com.example.studyroom
├── auth
├── user
├── room
├── reservation
├── notification
└── common
```

# 0단계: 개발 환경과 저장소

## 구현

- Git 저장소 생성
- Spring Initializr 프로젝트 생성
- Java 21, Gradle, Spring Boot 설정
- Docker Compose로 PostgreSQL 실행
- `local`, `test`, `prod` 프로필 분리
- Actuator 헬스 체크 추가
- 기본 README 작성

## 주요 파일

```text
compose.yaml
build.gradle.kts
src/main/resources/application.yml
src/main/resources/application-local.yml
src/test/resources/application-test.yml
```

## 확인

```bash
docker compose up -d
./gradlew bootRun
curl http://localhost:8080/actuator/health
```

## 학습할 내용

- Spring Boot 자동 설정
- Gradle 의존성 관리
- Bean과 Application Context
- Spring profile
- Docker 컨테이너와 포트
- 환경변수와 설정 파일

## 완료 조건

- PostgreSQL이 컨테이너에서 실행됨
- Spring Boot가 DB에 연결됨
- 헬스 체크가 `UP`을 반환함
- `./gradlew test`가 성공함

---

# 1단계: DB migration과 사용자·공간 모델

첫 기능보다 먼저 DB 스키마를 코드로 관리하는 습관을 만듭니다.

## 구현

Flyway migration:

```text
V1__create_users.sql
V2__create_study_rooms.sql
V3__insert_development_seed.sql
```

테이블:

```text
users
study_rooms
```

`users`:

```text
id
email
password_hash
name
role
status
created_at
updated_at
```

`study_rooms`:

```text
id
name
location
description
capacity
status
version
created_at
updated_at
```

JPA 구성:

```text
User
StudyRoom
UserRepository
RoomRepository
```

## 연습할 내용

- Entity와 테이블의 차이
- 기본키 전략
- `TIMESTAMPTZ`
- unique, check, foreign key
- JPA 영속성 컨텍스트
- Hibernate dirty checking
- Flyway migration
- `ddl-auto: validate`

## 테스트

Testcontainers로 실제 PostgreSQL을 실행합니다.

- 애플리케이션 컨텍스트 시작
- Flyway migration 성공
- 사용자 저장과 조회
- 공간 저장과 조회
- 중복 이메일 저장 실패

H2는 사용하지 않는 것이 좋습니다. 최종 운영 DB와 테스트 DB를 PostgreSQL로 통일합니다.

## 완료 조건

- 빈 DB에서 서버를 실행하면 스키마가 자동 생성됨
- 서버를 재실행해도 migration이 중복 실행되지 않음
- JPA Entity와 실제 테이블이 일치함
- PostgreSQL 통합 테스트가 통과함

---

# 2단계: 공간 조회 API

첫 사용자 기능은 읽기 전용 API로 시작합니다.

## API

```http
GET /api/v1/rooms
GET /api/v1/rooms/{roomId}
```

필터:

```http
GET /api/v1/rooms?capacity=4&status=ACTIVE
```

응답:

```json
{
  "items": [
    {
      "id": 1,
      "name": "Focus Room A",
      "location": "2층",
      "capacity": 4,
      "status": "ACTIVE"
    }
  ]
}
```

## 구현 구조

```text
room/
├── RoomController
├── RoomService
├── RoomRepository
├── StudyRoom
└── dto
    ├── RoomResponse
    └── RoomSearchCondition
```

Entity를 API 응답으로 직접 반환하지 않고 DTO로 변환합니다.

## 함께 구현할 기반 기능

- `GlobalExceptionHandler`
- `ErrorCode`
- `ErrorResponse`
- 요청값 validation
- 페이지네이션
- 구조화된 로그
- 요청별 trace ID

## 테스트

- Controller 테스트
- Repository 통합 테스트
- 없는 공간 조회 시 404
- 잘못된 필터 입력 시 400
- 비활성 공간 필터링

## 학습할 내용

- Controller, Service, Repository 책임
- DTO와 Entity 분리
- Jackson JSON 직렬화
- Bean Validation
- HTTP 상태 코드
- Spring MVC 요청 흐름

## 완료 조건

실제 HTTP 요청으로 공간 목록과 상세 정보를 조회할 수 있어야 합니다.

---

# 3단계: 회원가입과 인증

공간 조회가 동작한 다음 예약자를 식별할 수 있도록 인증을 붙입니다.

## API

```http
POST /api/v1/auth/signup
POST /api/v1/auth/login
POST /api/v1/auth/refresh
POST /api/v1/auth/logout
GET  /api/v1/me
```

## 구현

- BCrypt 비밀번호 해싱
- Access Token
- Refresh Token
- Spring Security filter chain
- 인증 사용자 principal
- USER와 ADMIN 권한
- 로그인 실패 횟수 제한의 기초
- 비밀번호가 로그에 남지 않도록 처리

Refresh Token을 DB에 저장하면 다음 필드가 필요합니다.

```text
refresh_tokens
- id
- user_id
- token_hash
- expires_at
- revoked_at
- created_at
```

원문 토큰 대신 해시를 저장합니다.

## 권한 정책

```text
GET /actuator/health       공개
GET /api/v1/rooms/**       공개
POST /api/v1/auth/**       공개
POST /api/v1/reservations 인증 필요
/api/v1/admin/**           ADMIN 필요
```

## 테스트

- 회원가입 성공
- 이메일 중복 가입 실패
- 올바른 로그인
- 잘못된 비밀번호
- 만료된 토큰
- 인증 없이 보호된 API 호출
- 일반 사용자의 관리자 API 접근 거부

## 학습할 내용

- Spring Security filter chain
- Authentication과 Authorization
- JWT 또는 세션의 장단점
- 비밀번호 해싱
- 401과 403의 차이
- 보안 경계에서의 입력 검증

## 완료 조건

로그인한 사용자가 `/api/v1/me`를 호출했을 때 자신의 정보만 조회해야 합니다.

---

# 4단계: 예약 생성

이 단계에서 서비스의 핵심 기능을 구현합니다.

## DB

```text
V4__create_reservations.sql
```

```text
reservations
- id
- user_id
- room_id
- start_at
- end_at
- participant_count
- purpose
- status
- request_key
- created_at
- updated_at
```

PostgreSQL exclusion constraint로 같은 공간의 시간 중복을 차단합니다.

## API

```http
POST /api/v1/reservations
Idempotency-Key: UUID
```

```json
{
  "roomId": 1,
  "startAt": "2026-09-01T10:00:00+09:00",
  "endAt": "2026-09-01T11:00:00+09:00",
  "participantCount": 3,
  "purpose": "프로젝트 회의"
}
```

## 구현 구조

```text
reservation/
├── ReservationController
├── ReservationService
├── ReservationPolicy
├── ReservationRepository
├── Reservation
├── ReservationStatus
└── dto
```

## 예약 트랜잭션

```text
인증 사용자 조회
→ 공간 조회
→ 공간 상태 및 정원 검증
→ 시간 정책 검증
→ 예약 INSERT
→ PostgreSQL 중복 제약 확인
→ 커밋
```

중복이면:

```http
409 Conflict
```

```json
{
  "code": "RESERVATION_TIME_CONFLICT",
  "message": "선택한 시간에 이미 다른 예약이 있습니다."
}
```

## 테스트

- 정상 예약
- 과거 시간 예약 실패
- 종료가 시작보다 빠른 경우 실패
- 정원 초과 실패
- 비활성 공간 예약 실패
- 동일한 멱등성 키 재요청
- 같은 공간의 중복 시간 예약 실패
- 다른 공간의 동일 시간 예약 성공
- 앞 예약 종료 시각과 다음 예약 시작 시각이 같은 경우 성공

## 학습할 내용

- `@Transactional`
- DB constraint
- 애플리케이션 검증과 DB 검증의 차이
- 멱등성
- 예외 변환
- 트랜잭션 격리 수준
- 경쟁 조건

## 완료 조건

같은 공간과 시간으로 요청을 동시에 보내도 정확히 한 건만 생성되어야 합니다.

---

# 5단계: 동시성 집중 실험

예약 기능을 만든 직후 별도의 동시성 실험을 진행합니다.

## 실험 1: 잘못된 구현

```text
중복 예약 SELECT
→ 결과가 없으면 INSERT
```

20개 요청을 동시에 보내 중복 생성 가능성을 관찰합니다.

## 실험 2: 비관적 잠금

```text
SELECT room ... FOR UPDATE
→ 예약 검사
→ INSERT
```

동작 방식과 대기 시간을 관찰합니다.

## 실험 3: PostgreSQL exclusion constraint

DB 제약 조건만으로 최종 정합성을 보장합니다.

## 비교할 항목

| 방법                 | 여러 서버 지원 | 구현 난이도 |        동시 요청 처리 |
| -------------------- | -------------: | ----------: | --------------------: |
| 애플리케이션 조회만  |         불가능 |        낮음 |             중복 가능 |
| JVM `synchronized`   |         불가능 |        낮음 | 서버가 여러 대면 실패 |
| 비관적 잠금          |           가능 |        중간 |        대기 증가 가능 |
| exclusion constraint |           가능 |        중간 |        DB가 최종 차단 |

Redis 분산 락은 이 단계에서 도입하지 않습니다. 예약 정합성은 PostgreSQL이 책임지게 합니다.

## 완료 조건

Testcontainers 통합 테스트에서 동시에 20번 예약했을 때:

```text
성공: 1건
409 Conflict: 19건
DB 예약 수: 1건
```

---

# 6단계: 예약 가능 시간과 운영 정책

예약 생성이 안전해진 다음 편의 기능을 만듭니다.

## DB

```text
room_operating_hours
room_blackouts
```

## API

```http
GET /api/v1/rooms/{roomId}/availability?date=2026-09-01
```

관리자용:

```http
POST /api/v1/admin/rooms/{roomId}/operating-hours
POST /api/v1/admin/rooms/{roomId}/blackouts
```

## 정책

- 최소 30분
- 최대 4시간
- 30분 단위
- 최대 30일 이후까지 예약
- 운영시간 내 예약
- 점검시간 예약 금지
- 사용자당 활성 예약 최대 3개

## 중요한 규칙

가능 시간 조회는 참고 정보입니다.

```text
가능 시간 조회
→ 다른 사용자가 먼저 예약
→ 기존 사용자가 예약 요청
→ DB가 최종적으로 409 반환
```

조회 결과를 예약 보장으로 취급하면 안 됩니다.

## 테스트

- 영업시간 밖 예약 실패
- 휴무일 예약 실패
- 점검시간과 일부만 겹쳐도 실패
- 자정을 넘는 운영시간
- 타임존 변환
- 최대 예약 개수 초과

## 학습할 내용

- 시간 모델링
- `Instant`, `OffsetDateTime`, `ZoneId`
- 복합 비즈니스 정책
- 날짜 경계
- 조회 결과와 명령 결과의 차이

---

# 7단계: 예약 조회, 취소 및 상태 전환

## API

```http
GET    /api/v1/me/reservations
GET    /api/v1/me/reservations/{reservationId}
DELETE /api/v1/me/reservations/{reservationId}
```

## 상태

```text
CONFIRMED → CANCELLED
CONFIRMED → COMPLETED
CONFIRMED → NO_SHOW
```

예약은 실제 DELETE하지 않습니다.

## 규칙

- 본인 예약만 조회 및 취소
- 시작 1시간 전까지만 취소 가능
- 취소된 예약 재취소 불가
- 완료된 예약 취소 불가
- 취소된 시간대는 다시 예약 가능

## 배치 작업

Spring Scheduler로 과거 예약을 `COMPLETED`로 변경하는 작은 작업을 구현합니다.

단, 여러 서버에서 같은 스케줄러가 실행될 수 있다는 점을 학습해야 합니다. 초기에는 한 서버에서 실행하고, 확장 단계에서 리더 선출 또는 DB 기반 작업 잠금을 추가합니다.

## 학습할 내용

- 상태 머신
- soft delete와 상태 보존
- 소유권 검증
- 스케줄 작업
- 재실행 가능한 작업 설계

---

# 8단계: 관리자 기능과 감사 로그

## API

```http
POST  /api/v1/admin/rooms
PATCH /api/v1/admin/rooms/{roomId}
GET   /api/v1/admin/reservations
PATCH /api/v1/admin/reservations/{id}/status
```

## 감사 로그

```text
audit_logs
- id
- actor_user_id
- action
- target_type
- target_id
- before_data
- after_data
- created_at
```

기록 대상:

- 공간 생성 및 수정
- 공간 비활성화
- 점검시간 등록
- 관리자 예약 취소
- 사용자 정지

## 학습할 내용

- 역할 기반 접근 제어
- 관리자 API
- 변경 이력
- JSONB
- 개인정보와 로그 관리

## 완료 조건

누가 언제 어떤 공간이나 예약을 변경했는지 추적할 수 있어야 합니다.

---

# 9단계: 비동기 이벤트와 알림

예약 트랜잭션 안에서 이메일을 직접 보내면 안 됩니다.

## 먼저 구현할 구조

```text
예약 저장
→ 같은 트랜잭션에서 outbox_events 저장
→ 커밋
→ 별도 publisher가 이벤트 전달
→ notification worker가 알림 처리
```

## DB

```text
outbox_events
- id
- event_type
- aggregate_id
- payload
- status
- attempts
- next_attempt_at
- created_at
- published_at
```

## 이벤트

```text
ReservationCreated
ReservationCancelled
ReservationStartingSoon
```

## 1차 구현

Spring Scheduler가 outbox를 조회해 로그로 알림을 출력합니다.

## 2차 구현

RabbitMQ를 추가합니다.

```text
Spring Boot
→ PostgreSQL outbox
→ RabbitMQ
→ Notification Consumer
→ 이메일 발송
```

바로 RabbitMQ부터 시작하지 않는 이유는 DB commit과 메시지 발행 사이의 유실 문제를 먼저 경험하기 위해서입니다.

## 테스트

- 예약 생성과 outbox 저장의 원자성
- 발행 실패 후 재시도
- 같은 이벤트 중복 소비
- consumer 재시작
- RabbitMQ 중단 후 복구
- 이메일 실패가 예약을 취소하지 않는지 확인

## 학습할 내용

- 비동기 메시지
- transactional outbox
- at-least-once delivery
- 중복 소비와 멱등성
- 재시도와 dead letter queue

---

# 10단계: Redis 도입

Redis는 문제를 해결할 필요가 생긴 뒤 추가합니다.

## 적절한 사용처

### 공간 목록 캐시

```text
GET /rooms
→ Redis 조회
→ 없으면 PostgreSQL 조회
→ Redis 저장
```

공간이 수정되면 캐시를 무효화합니다.

### Rate limiting

```text
로그인 시도
예약 생성
가능 시간 반복 조회
```

사용자 또는 IP별 요청 횟수를 제한합니다.

### 짧은 중복 요청 방지

멱등성 처리 성능을 보조할 수 있지만, 최종 기록은 PostgreSQL에 남겨야 합니다.

## 사용하지 않을 곳

- 예약 데이터의 최종 저장소
- 예약 중복 방지의 유일한 수단
- 사용자 권한의 유일한 저장소

## 테스트

- Redis가 없을 때도 핵심 예약 기능 동작
- 캐시 적중과 미적중
- 공간 수정 후 캐시 무효화
- TTL 만료
- 요청 제한 초과 시 429
- 오래된 캐시 데이터가 예약 정합성을 깨지 않는지 확인

## 학습할 내용

- cache-aside
- TTL
- cache invalidation
- Redis 자료구조
- rate limiting
- 캐시 장애 격리

---

# 11단계: 관측성

운영 서버는 문제가 생겼을 때 원인을 찾을 수 있어야 합니다.

## 구성

- Spring Boot Actuator
- Micrometer
- Prometheus
- Grafana
- 구조화된 JSON 로그
- trace ID
- 요청 처리 시간
- DB connection pool 지표

## 주요 지표

```text
HTTP 요청 수
HTTP 오류율
응답 시간 p50, p95, p99
예약 성공 수
예약 충돌 수
DB connection 사용량
Outbox 미처리 이벤트 수
RabbitMQ queue 길이
Redis 적중률
알림 실패 수
```

## 대시보드

최소 3개를 만듭니다.

```text
API 상태
예약 비즈니스 상태
인프라 상태
```

## 알림 예시

- 5분간 5xx 비율 5% 초과
- Outbox 미처리 이벤트 100건 초과
- DB connection pool 90% 이상
- RabbitMQ dead letter 발생
- 예약 API p95가 1초 초과

## 학습할 내용

- 로그, 메트릭, 트레이스 차이
- 평균보다 percentile이 중요한 이유
- 애플리케이션 지표와 비즈니스 지표
- 장애 탐지

---

# 12단계: CI/CD와 스테이징 배포

## CI

GitHub Actions 기준:

```text
Checkout
→ Java 설치
→ Gradle 테스트
→ 통합 테스트
→ 애플리케이션 빌드
→ Docker 이미지 빌드
→ 이미지 저장소 push
→ 스테이징 배포
→ 헬스 체크
```

## 배포 환경

처음에는 다음 정도면 충분합니다.

```text
Nginx 또는 Load Balancer
Spring Boot 1개
PostgreSQL
Redis
RabbitMQ
Prometheus/Grafana
```

관리형 PostgreSQL과 Redis를 사용할 수 있다면 운영 부담이 줄어듭니다.

## 배포 시 확인

- 환경변수로 DB 접속 정보 전달
- HTTPS
- 운영용 비밀번호
- Actuator 외부 노출 제한
- Flyway migration 자동 실행
- DB 백업
- 롤백 가능한 Docker 이미지
- graceful shutdown
- readiness와 liveness 분리

## migration 규칙

운영에 적용된 migration 파일은 수정하지 않습니다.

```text
V1 수정 금지
V2 수정 금지
새로운 변경은 V3, V4로 추가
```

삭제나 컬럼 타입 변경은 확장과 축소 방식으로 진행합니다.

```text
새 컬럼 추가
→ 애플리케이션이 양쪽 지원
→ 데이터 이전
→ 새 컬럼으로 전환
→ 이전 컬럼 제거
```

---

# 13단계: 부하 및 장애 테스트

마지막에는 정상 상황보다 실패 상황을 실험합니다.

## 부하 테스트

k6 또는 Gatling을 사용합니다.

시나리오:

```text
100명이 공간 목록 조회
50명이 같은 시간 예약
예약 조회와 취소 반복
관리자가 점검시간 등록
```

측정:

- 초당 요청 수
- p95 응답 시간
- 409 비율
- 5xx 비율
- DB connection 사용량
- CPU와 메모리
- lock 대기 시간

## 장애 실험

- PostgreSQL 일시 중단
- Redis 중단
- RabbitMQ 중단
- 알림 consumer 강제 종료
- Spring Boot 배포 중 종료
- 동일 이벤트 중복 전달
- DB connection pool 고갈

각 장애에 대해 기록합니다.

```text
사용자에게 보이는 현상
로그
메트릭
데이터 손실 여부
복구 방법
개선할 내용
```

# 추천 학습 마일스톤

| 마일스톤 | 사용자에게 보이는 결과 | 핵심 학습                   |
| -------- | ---------------------- | --------------------------- |
| M1       | 공간 조회              | Spring MVC, JPA, PostgreSQL |
| M2       | 회원가입과 로그인      | Spring Security             |
| M3       | 안전한 예약            | 트랜잭션, 동시성, DB 제약   |
| M4       | 가능 시간과 취소       | 도메인 정책, 시간 모델링    |
| M5       | 관리자 운영            | 권한, 감사 로그             |
| M6       | 예약 알림              | Outbox, RabbitMQ            |
| M7       | 조회 성능과 제한       | Redis                       |
| M8       | 상태 대시보드          | Prometheus, Grafana         |
| M9       | 실제 배포              | Docker, CI/CD               |
| M10      | 장애 대응              | 부하 테스트, 복구           |

# 첫 일주일 실행 계획

## 1일 차

- Spring Initializr 프로젝트 생성
- Git 저장소 구성
- PostgreSQL Compose 실행
- Actuator 헬스 체크

## 2일 차

- Flyway 설정
- `users`, `study_rooms` migration
- `ddl-auto: validate`
- PostgreSQL 접속과 migration 기록 확인

## 3일 차

- `StudyRoom` Entity
- Repository
- 공간 seed 데이터
- Testcontainers Repository 테스트

## 4일 차

- `GET /api/v1/rooms`
- `GET /api/v1/rooms/{id}`
- DTO와 예외 응답
- Controller 테스트

## 5일 차

- Docker 이미지 빌드
- 전체 테스트
- 실제 HTTP 호출
- README 작성
- 첫 번째 스테이징 배포

첫 주의 완료 결과는 “Spring Boot가 실행된다”가 아니라 “PostgreSQL에 저장된 공간을 실제 배포된 API에서 조회할 수 있다”입니다.

그다음 두 번째 주에는 인증, 세 번째 주에는 예약과 동시성으로 넘어가는 순서가 가장 좋습니다. 핵심 예약 흐름이 완성되기 전에는 Redis, RabbitMQ, Kubernetes 같은 컴포넌트를 먼저 추가하지 않는 것이 학습과 설계 모두에 유리합니다.
