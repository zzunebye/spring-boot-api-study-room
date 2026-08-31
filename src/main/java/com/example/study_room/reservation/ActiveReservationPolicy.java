package com.example.study_room.reservation;

import org.springframework.stereotype.Component;

import com.example.study_room.common.exception.BusinessException;
import com.example.study_room.common.exception.ErrorCode;

@Component
public class ActiveReservationPolicy {

	private final ReservationProperties properties;
	private final ReservationRepository reservationRepository;

	public ActiveReservationPolicy(
			ReservationProperties properties,
			ReservationRepository reservationRepository) {
		this.properties = properties;
		this.reservationRepository = reservationRepository;
	}

	public void validate(Long userId) {
		long activeCount = reservationRepository.countByUserIdAndStatus(userId, ReservationStatus.CONFIRMED);
		if (activeCount >= properties.maximumActiveCount()) {
			throw new BusinessException(ErrorCode.RESERVATION_POLICY_VIOLATION);
		}
	}
}
