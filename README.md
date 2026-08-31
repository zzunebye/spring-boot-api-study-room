# Study Room Project

## Project Structure

Packages by feature

```
room/
├── RoomController.java
├── RoomService.java
├── RoomRepository.java
├── StudyRoom.java
├── RoomAvailabilityChecker.java
└── dto/

reservation/
├── ReservationController.java
├── ReservationService.java
├── ReservationTimePolicy.java
├── ActiveReservationPolicy.java
├── ReservationRepository.java
├── Reservation.java
└── dto/
```

## Run the project

```bash
./gradlew bootRun
```

## Test the project

```bash
./gradlew test
```

## Database

### Database Migration

Flyway를 사용할때는 `V{버전}__{설명}.sql`와 같이 파일명을 정하는 것이 관례이다.

`src/main/resources/db/migration/` 역시 스프링 부트와 Flyway를 사용할 경우 기본적으로 마이그레이션 파일을 저장하는 디렉토리이다. 이 디렉토리에 마이그레이션 파일을 저장하면 Flyway가 앱 시작(bootRun) 시 아직 적용이 안된 것 부터 자동으로 마이그레이션을 수행한다.
