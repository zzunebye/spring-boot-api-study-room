package com.example.study_room.reservation;

import java.time.Instant;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * userId / requestKey are not included in the body.
 * - userId: JWT (@AuthenticationPrincipal)
 * - requestKey: Idempotency-Key header
 */
public record CreateReservationRequest(
		@NotNull Long roomId,
		@NotNull Instant startAt,
		@NotNull Instant endAt,
		@NotNull @Min(1) Integer participantCount,
		@Size(max = 255) String purpose) {
}
