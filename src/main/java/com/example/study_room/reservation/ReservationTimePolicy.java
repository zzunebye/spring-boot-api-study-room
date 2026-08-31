package com.example.study_room.reservation;

import java.time.Duration;
import java.time.Instant;

import org.springframework.stereotype.Component;

import com.example.study_room.common.exception.BusinessException;
import com.example.study_room.common.exception.ErrorCode;

@Component
public class ReservationTimePolicy {

	private final ReservationProperties properties;

	public ReservationTimePolicy(ReservationProperties properties) {
		this.properties = properties;
	}

	public void validate(Instant startAt, Instant endAt) {
		if (!startAt.isBefore(endAt)) {
			throw new BusinessException(ErrorCode.RESERVATION_POLICY_VIOLATION);
		}

		Instant now = Instant.now();
		if (startAt.isBefore(now)) {
			throw new BusinessException(ErrorCode.RESERVATION_POLICY_VIOLATION);
		}

		Instant latestStart = now.plus(Duration.ofDays(properties.maximumFutureDays()));
		if (startAt.isAfter(latestStart)) {
			throw new BusinessException(ErrorCode.RESERVATION_POLICY_VIOLATION);
		}

		Duration duration = Duration.between(startAt, endAt);
		if (duration.compareTo(properties.minimumDuration()) < 0
				|| duration.compareTo(properties.maximumDuration()) > 0) {
			throw new BusinessException(ErrorCode.RESERVATION_POLICY_VIOLATION);
		}

		long slotMinutes = properties.slotUnit().toMinutes();
		if (duration.toMinutes() % slotMinutes != 0) {
			throw new BusinessException(ErrorCode.RESERVATION_POLICY_VIOLATION);
		}
	}
}
