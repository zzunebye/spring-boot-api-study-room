package com.example.study_room.room;

import java.time.Instant;

import org.springframework.data.jpa.repository.JpaRepository;

public interface RoomBlackoutRepository extends JpaRepository<RoomBlackout, Long> {

	boolean existsByRoomIdAndStartAtLessThanAndEndAtGreaterThan(Long roomId, Instant endAt, Instant startAt);
}
