package com.example.study_room.reservation;

import java.time.Instant;
import java.util.UUID;

public record ReservationResponse(
		Long id,
		Long userId,
		Long roomId,
		Instant startAt,
		Instant endAt,
		int participantCount,
		String purpose,
		ReservationStatus status,
		UUID requestKey,
		Instant createdAt) {

	// static factory method
	public static ReservationResponse from(Reservation reservation) {
		return new ReservationResponse(
				reservation.getId(),
				reservation.getUserId(),
				reservation.getRoomId(),
				reservation.getStartAt(),
				reservation.getEndAt(),
				reservation.getParticipantCount(),
				reservation.getPurpose(),
				reservation.getStatus(),
				reservation.getRequestKey(),
				reservation.getCreatedAt());
	}
}
