package com.example.study_room.reservation;

import java.time.Duration;
import java.time.Instant;

import org.springframework.stereotype.Component;

import com.example.study_room.common.exception.BusinessException;
import com.example.study_room.common.exception.ErrorCode;
import com.example.study_room.room.StudyRoom;

@Component
public class ReservationPolicy {

	private final ReservationProperties properties;
	private final ReservationRepository reservationRepository;

	public ReservationPolicy(ReservationProperties properties, ReservationRepository reservationRepository) {
		this.properties = properties;
		this.reservationRepository = reservationRepository;
	}

	/**
	 * 예약 생성 시 정책 위반 여부를 검증합니다.
	 *
	 * 1. 시작 시간이 종료 시간보다 빠른지 확인
	 * 2. 예약 시작 시간이 현재 시간보다 이전인지 확인
	 * 3. 예약 시작 최대 일수 초과 여부 확인
	 * 4. 예약 최소/최대 시간 범위 확인
	 * 5. 예약 시간이 슬롯 단위에 일치하는지 확인 (duration이 30분 배수)
	 * 6. 인원 수가 방의 최대 인원 수를 초과하는지 확인
	 * 7. 사용자의 활성 예약 건수가 최대 허용 개수를 초과하는지 확인
	 */
	public void validateForCreate(
			Long userId,
			StudyRoom room,
			Instant startAt,
			Instant endAt,
			int participantCount) {

		// 1. 시작 시간이 종료 시간보다 이전인지 확인
		if (!startAt.isBefore(endAt)) {
			throw new BusinessException(ErrorCode.RESERVATION_POLICY_VIOLATION);
		}

		Instant now = Instant.now();

		// 2. 예약 시작 시간이 현재 시간보다 이전인지 확인
		if (startAt.isBefore(now)) {
			throw new BusinessException(ErrorCode.RESERVATION_POLICY_VIOLATION);
		}

		// 3. 예약 시작 최대 일수 초과 여부 확인
		Instant latestStart = now.plus(Duration.ofDays(properties.maximumFutureDays()));
		if (startAt.isAfter(latestStart)) {
			throw new BusinessException(ErrorCode.RESERVATION_POLICY_VIOLATION);
		}

		// 4. 예약 최소/최대 시간 범위 확인
		Duration duration = Duration.between(startAt, endAt);
		if (duration.compareTo(properties.minimumDuration()) < 0
				|| duration.compareTo(properties.maximumDuration()) > 0) {
			throw new BusinessException(ErrorCode.RESERVATION_POLICY_VIOLATION);
		}

		// 5. 예약 시간이 슬롯 단위에 일치하는지 확인 (duration이 30분 배수)
		long slotMinutes = properties.slotUnit().toMinutes();
		if (duration.toMinutes() % slotMinutes != 0) {
			throw new BusinessException(ErrorCode.RESERVATION_POLICY_VIOLATION);
		}

		// 6. 인원 수가 방의 최대 인원 수를 초과하는지 확인
		if (participantCount > room.getCapacity()) {
			throw new BusinessException(ErrorCode.RESERVATION_POLICY_VIOLATION);
		}

		// 7. 사용자의 활성 예약 건수가 최대 허용 개수를 초과하는지 확인
		long activeCount = reservationRepository.countByUserIdAndStatus(userId, ReservationStatus.CONFIRMED);
		if (activeCount >= properties.maximumActiveCount()) {
			throw new BusinessException(ErrorCode.RESERVATION_POLICY_VIOLATION);
		}
	}
}
