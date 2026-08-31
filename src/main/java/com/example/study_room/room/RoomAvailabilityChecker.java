package com.example.study_room.room;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import org.springframework.stereotype.Component;

import com.example.study_room.branch.Branch;
import com.example.study_room.branch.BranchRepository;
import com.example.study_room.common.exception.BusinessException;
import com.example.study_room.common.exception.ErrorCode;

@Component
public class RoomAvailabilityChecker {

	private final BranchRepository branchRepository;
	private final RoomOperatingHoursRepository operatingHoursRepository;
	private final RoomBlackoutRepository blackoutRepository;

	public RoomAvailabilityChecker(
			BranchRepository branchRepository,
			RoomOperatingHoursRepository operatingHoursRepository,
			RoomBlackoutRepository blackoutRepository) {
		this.branchRepository = branchRepository;
		this.operatingHoursRepository = operatingHoursRepository;
		this.blackoutRepository = blackoutRepository;
	}

	public void validate(StudyRoom room, Instant startAt, Instant endAt) {
		validateOperatingHours(room, startAt, endAt);
		validateNoBlackout(room.getId(), startAt, endAt);
	}

	private void validateOperatingHours(StudyRoom room, Instant startAt, Instant endAt) {
		Branch branch = branchRepository.findById(room.getBranchId())
				.orElseThrow(() -> new BusinessException(ErrorCode.BRANCH_NOT_FOUND));
		ZoneId zone = branch.getTimeZone();

		ZonedDateTime startLocal = startAt.atZone(zone);
		ZonedDateTime endLocal = endAt.atZone(zone);
		LocalDate date = startLocal.toLocalDate();
		if (!endLocal.toLocalDate().equals(date)) {
			throw new BusinessException(ErrorCode.RESERVATION_POLICY_VIOLATION);
		}

		RoomOperatingHours hours = operatingHoursRepository
				.findByRoomIdAndDayOfWeek(room.getId(), startLocal.getDayOfWeek())
				.orElseThrow(() -> new BusinessException(ErrorCode.RESERVATION_POLICY_VIOLATION));

		Instant openAt = date.atTime(hours.getOpenTime()).atZone(zone).toInstant();
		Instant closeAt = date.atTime(hours.getCloseTime()).atZone(zone).toInstant();
		if (startAt.isBefore(openAt) || endAt.isAfter(closeAt)) {
			throw new BusinessException(ErrorCode.RESERVATION_POLICY_VIOLATION);
		}
	}

	private void validateNoBlackout(Long roomId, Instant startAt, Instant endAt) {
		if (blackoutRepository.existsByRoomIdAndStartAtLessThanAndEndAtGreaterThan(roomId, endAt, startAt)) {
			throw new BusinessException(ErrorCode.RESERVATION_POLICY_VIOLATION);
		}
	}
}
