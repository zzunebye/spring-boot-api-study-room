package com.example.study_room.room;

import java.time.DayOfWeek;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface RoomOperatingHoursRepository extends JpaRepository<RoomOperatingHours, Long> {

	Optional<RoomOperatingHours> findByRoomIdAndDayOfWeek(Long roomId, DayOfWeek dayOfWeek);
}
