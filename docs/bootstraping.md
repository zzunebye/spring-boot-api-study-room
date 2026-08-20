가장 좋은 부트스트래핑 방식은 “프로젝트 생성 → PostgreSQL 연결 → Flyway 스키마 적용 → 헬스 체크 → 첫 예약 흐름”까지 한 번에 관통시키는 것입니다. 처음부터 모든 기능을 만들기보다 실행 가능한 수직 단면을 먼저 확보합니다.

## 1. Spring Initializr 설정

[start.spring.io](https://start.spring.io/)에서 다음처럼 생성합니다.

```text
Project: Gradle - Kotlin
Language: Java
Spring Boot: 화면에 표시되는 최신 Stable
Java: 21
Group: com.example
Artifact: study-room
Package: com.example.studyroom
Packaging: Jar
```

의존성:

```text
Spring Web
Spring Data JPA
Validation
Spring Security
PostgreSQL Driver
Flyway Migration
Spring Boot Actuator
Docker Compose Support
```

테스트용:

```text
Spring Boot Test
Spring Security Test
Testcontainers
Testcontainers PostgreSQL
```

Flyway는 Spring Boot 4에서는 `spring-boot-starter-flyway`로 제공되며, PostgreSQL 사용 시 `flyway-database-postgresql`도 함께 필요합니다. Spring Boot는 애플리케이션 시작 시 Flyway migration을 자동 실행합니다. [Spring Boot 공식 문서](https://docs.spring.io/spring-boot/how-to/data-initialization.html)

## 2. 초기 디렉터리 구조

처음에는 다음 정도로 구성합니다.

```text
study-room/
├── compose.yaml
├── build.gradle.kts
├── settings.gradle.kts
├── src
│   ├── main
│   │   ├── java/com/example/studyroom
│   │   │   ├── StudyRoomApplication.java
│   │   │   ├── common
│   │   │   │   ├── config
│   │   │   │   └── exception
│   │   │   ├── user
│   │   │   ├── room
│   │   │   └── reservation
│   │   └── resources
│   │       ├── application.properties
│   │       ├── application-local.properties
│   │       └── db/migration
│   │           └── V1__initial_schema.sql
│   └── test
│       └── java/com/example/studyroom
└── README.md
```

레이어별 최상위 패키지보다 기능별 패키지가 좋습니다.

```text
room/
├── RoomController.java
├── RoomService.java
├── RoomRepository.java
├── StudyRoom.java
└── dto/

reservation/
├── ReservationController.java
├── ReservationService.java
├── ReservationPolicy.java
├── ReservationRepository.java
├── Reservation.java
└── dto/
```

## 3. Gradle 의존성 확인

Initializr가 만든 `build.gradle.kts`에 다음 구성이 포함됐는지 확인합니다. Spring Boot 4.1.0 기준으로 starter 이름과 테스트 의존성 구조가 3.x와 다릅니다. **Initializr가 생성한 값을 그대로 사용**합니다.

```kotlin
plugins {
    java
    id("org.springframework.boot") version "4.1.0"
    id("io.spring.dependency-management") version "1.1.7"
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-flyway")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-webmvc")
    implementation("org.flywaydb:flyway-database-postgresql")
    developmentOnly("org.springframework.boot:spring-boot-docker-compose")
    runtimeOnly("org.postgresql:postgresql")

    testImplementation("org.springframework.boot:spring-boot-starter-actuator-test")
    testImplementation("org.springframework.boot:spring-boot-starter-data-jpa-test")
    testImplementation("org.springframework.boot:spring-boot-starter-flyway-test")
    testImplementation("org.springframework.boot:spring-boot-starter-security-test")
    testImplementation("org.springframework.boot:spring-boot-starter-validation-test")
    testImplementation("org.springframework.boot:spring-boot-starter-webmvc-test")
    testImplementation("org.springframework.boot:spring-boot-testcontainers")
    testImplementation("org.testcontainers:testcontainers-junit-jupiter")
    testImplementation("org.testcontainers:testcontainers-postgresql")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}
```

Spring Boot 3.x와의 주요 차이:

| 3.x                               | 4.x (현재)                                 |
| --------------------------------- | ------------------------------------------ |
| `spring-boot-starter-web`         | `spring-boot-starter-webmvc`               |
| `flyway-core`                     | `spring-boot-starter-flyway`               |
| `spring-boot-starter-test` (단일) | `spring-boot-starter-*-test` (기능별 분리) |
| `spring-security-test`            | `spring-boot-starter-security-test`        |
| `testcontainers:junit-jupiter`    | `testcontainers-junit-jupiter`             |
| (없음)                            | `spring-boot-testcontainers`               |

## 4. 로컬 PostgreSQL 구성

`compose.yaml`:

```yaml
services:
  postgres:
    image: postgres:17
    environment:
      POSTGRES_DB: study_room
      POSTGRES_USER: study_room
      POSTGRES_PASSWORD: local_password
    ports:
      - "5432:5432"
    volumes:
      - study-room-postgres:/var/lib/postgresql/data
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U study_room -d study_room"]
      interval: 5s
      timeout: 3s
      retries: 10

volumes:
  study-room-postgres:
```

`local_password`는 로컬 개발 전용입니다. 운영 비밀번호는 저장소에 커밋하지 않고 환경변수나 secret manager를 사용합니다.

Spring Boot의 Docker Compose 지원을 사용하면 애플리케이션 실행 시 Compose 서비스를 감지하고 연결 정보를 구성할 수 있습니다. [Spring Boot Docker Compose 공식 문서](https://docs.spring.io/spring-boot/how-to/docker-compose.html)

## 5. 애플리케이션 설정

`application.properties`:

```properties
spring.application.name=study-room
spring.profiles.default=local

spring.jpa.open-in-view=false
spring.jpa.hibernate.ddl-auto=validate
spring.jpa.properties.hibernate.jdbc.time_zone=UTC

spring.flyway.enabled=true
spring.flyway.clean-disabled=true

server.shutdown=graceful

management.endpoints.web.exposure.include=health,info
management.endpoint.health.show-details=never
```

`application-local.properties`:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/study_room
spring.datasource.username=study_room
spring.datasource.password=local_password

spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
```

중요한 원칙은 다음과 같습니다.

- 테이블 생성은 Flyway만 담당
- Hibernate는 `ddl-auto: validate`
- `ddl-auto: update`는 사용하지 않음
- 운영 설정은 환경변수로 주입
- `open-in-view`는 비활성화

Flyway와 Hibernate 자동 생성을 함께 사용하지 않고 스키마 관리 방식을 하나로 통일하는 것이 공식 권장 방향입니다. [DB 초기화 공식 문서](https://docs.spring.io/spring-boot/how-to/data-initialization.html)

## 6. 첫 DB 마이그레이션

migration 파일은 Gradle task로 생성할 수 있습니다.

```bash
./gradlew createMigration -PmigrationName=initial_schema
# → src/main/resources/db/migration/V1__initial_schema.sql
```

생성된 파일에 SQL을 작성합니다. 예: `V1__initial_schema.sql`

```sql
CREATE EXTENSION IF NOT EXISTS btree_gist;

CREATE TABLE users (
    id              BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    email           VARCHAR(255) NOT NULL,
    password_hash   VARCHAR(255) NOT NULL,
    name            VARCHAR(100) NOT NULL,
    role            VARCHAR(20) NOT NULL,
    status          VARCHAR(20) NOT NULL,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT users_email_uq UNIQUE (email),
    CONSTRAINT users_role_ck
        CHECK (role IN ('USER', 'ADMIN')),
    CONSTRAINT users_status_ck
        CHECK (status IN ('ACTIVE', 'SUSPENDED', 'WITHDRAWN'))
);

CREATE TABLE study_rooms (
    id              BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    name            VARCHAR(100) NOT NULL,
    location        VARCHAR(255) NOT NULL,
    description     TEXT,
    capacity        INTEGER NOT NULL,
    status          VARCHAR(20) NOT NULL,
    version         BIGINT NOT NULL DEFAULT 0,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT study_rooms_capacity_ck CHECK (capacity > 0),
    CONSTRAINT study_rooms_status_ck
        CHECK (status IN ('ACTIVE', 'INACTIVE', 'MAINTENANCE'))
);

CREATE TABLE reservations (
    id                  BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    user_id             BIGINT NOT NULL REFERENCES users(id),
    room_id             BIGINT NOT NULL REFERENCES study_rooms(id),
    start_at            TIMESTAMPTZ NOT NULL,
    end_at              TIMESTAMPTZ NOT NULL,
    participant_count   INTEGER NOT NULL,
    purpose             VARCHAR(255),
    status              VARCHAR(20) NOT NULL,
    request_key         UUID NOT NULL,
    cancelled_at        TIMESTAMPTZ,
    cancel_reason       VARCHAR(255),
    created_at          TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT reservations_period_ck CHECK (start_at < end_at),
    CONSTRAINT reservations_participant_count_ck
        CHECK (participant_count > 0),
    CONSTRAINT reservations_status_ck
        CHECK (status IN ('CONFIRMED', 'CANCELLED', 'COMPLETED', 'NO_SHOW')),
    CONSTRAINT reservations_user_request_key_uq
        UNIQUE (user_id, request_key)
);

ALTER TABLE reservations
ADD CONSTRAINT reservations_no_overlap
EXCLUDE USING gist (
    room_id WITH =,
    tstzrange(start_at, end_at, '[)') WITH &&
)
WHERE (status = 'CONFIRMED');

CREATE INDEX reservations_user_start_idx
    ON reservations (user_id, start_at DESC);

CREATE INDEX reservations_room_start_idx
    ON reservations (room_id, start_at);
```

운영시간과 휴무시간 테이블은 두 번째 migration으로 분리해도 됩니다. 첫 migration은 예약 정합성을 확인할 수 있는 최소 스키마에 집중합니다.

## 7. Spring Security 초기 설정

Security starter를 추가하면 기본적으로 모든 요청이 막힙니다. 부트스트래핑 단계에서는 헬스 체크만 공개합니다.

```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http)
            throws Exception {
        return http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/actuator/health").permitAll()
                        .anyRequest().authenticated()
                )
                .httpBasic(Customizer.withDefaults())
                .build();
    }
}
```

`CSRF` 비활성화는 토큰 기반 REST API를 전제로 합니다. 브라우저 세션과 쿠키 인증을 사용할 계획이라면 비활성화하면 안 됩니다.

초기에는 HTTP Basic으로 개발하고, 인증 도메인을 구현할 때 JWT 또는 OAuth2로 교체할 수 있습니다.

## 8. 첫 번째 수직 단면

프로젝트가 실행된 직후 모든 CRUD를 만들지 말고 다음 순서로 구현합니다.

```text
1. GET /actuator/health
2. GET /api/v1/rooms
3. POST /api/v1/reservations
4. GET /api/v1/me/reservations
5. DELETE /api/v1/me/reservations/{id}
```

첫 번째 완료 목표는 다음 흐름입니다.

```text
테스트 사용자와 공간 생성
→ 공간 조회
→ 예약 생성
→ 같은 시간 재예약
→ 두 번째 요청은 409 Conflict
→ 기존 예약 취소
→ 같은 시간 예약 성공
```

이 흐름이 통과하면 DB 모델, 트랜잭션, 중복 방지, 예외 변환이 함께 검증됩니다.

## 9. 공통 예외 처리

초기에 다음 예외 구조를 마련합니다.

```text
common/exception/
├── ErrorCode.java
├── ErrorResponse.java
├── BusinessException.java
└── GlobalExceptionHandler.java
```

대표 오류 코드:

```java
public enum ErrorCode {
    INVALID_REQUEST,
    USER_NOT_FOUND,
    ROOM_NOT_FOUND,
    ROOM_NOT_AVAILABLE,
    RESERVATION_TIME_CONFLICT,
    RESERVATION_POLICY_VIOLATION,
    RESERVATION_FORBIDDEN
}
```

응답 예시:

```json
{
  "code": "RESERVATION_TIME_CONFLICT",
  "message": "선택한 시간에 이미 다른 예약이 있습니다.",
  "traceId": "..."
}
```

## 10. 로컬 실행과 확인

Docker Compose 지원을 쓰지 않는 경우:

```bash
docker compose up -d
./gradlew bootRun
```

확인:

```bash
curl http://localhost:8080/actuator/health
```

기대 응답:

```json
{
  "status": "UP"
}
```

테스트와 빌드:

```bash
./gradlew test
./gradlew build
```

DB migration 확인:

```bash
docker compose exec postgres \
  psql -U study_room -d study_room \
  -c "SELECT version, description, success FROM flyway_schema_history;"
```

## 11. 첫 커밋 단위

다음처럼 작게 나누는 것이 좋습니다.

```text
1. chore: bootstrap Spring Boot project
2. chore: add PostgreSQL and Flyway configuration
3. feat: add initial reservation schema
4. feat: add room query API
5. feat: add reservation creation
6. test: verify concurrent reservation conflict
```

최초 부트스트래핑 완료 기준은 단순히 애플리케이션이 켜지는 것이 아닙니다.

- PostgreSQL 컨테이너가 정상 실행됨
- Flyway migration이 자동 적용됨
- Hibernate schema validation이 통과함
- 헬스 체크가 `UP`을 반환함
- 테스트와 빌드가 성공함
- 동시에 같은 공간을 예약해도 하나만 성공함

이후에는 `Room 조회 → Reservation 생성 → 중복 예약 통합 테스트` 순으로 구현하는 것이 가장 안전합니다.
