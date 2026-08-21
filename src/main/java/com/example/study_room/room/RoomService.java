package com.example.study_room.room;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.study_room.common.exception.BusinessException;
import com.example.study_room.common.exception.ErrorCode;

/**
 * RoomRepository를 사용하여 데이터를 조회하고 반환하는 비즈니스 로직 서비스로, Controller는 Service만 호출.
 * 
 * Service - an operation offered as an interface that stands alone in the
 * model, with no encapsulated state. May also indicate that a class is a
 * "Business Service Facade" (in the Core J2EE patterns sense), or something
 * similar. This annotation is a general-purpose stereotype and individual teams
 * may narrow their semantics and use as appropriate.
 */
@Service
public class RoomService {
	private final RoomRepository roomRepository;

	public RoomService(RoomRepository roomRepository) {
		this.roomRepository = roomRepository;
	}

	public List<RoomResponse> getAllRooms(
			String status,
			Integer minCapacity,
			Integer maxCapacity) {

		// 컬렉션을 Entity → DTO로 바꿀 때 자주 쓰는 패턴
		// return roomRepository.findAll().stream()
		// .map(RoomResponse::from)
		// .toList();
		validateStatus(status);
		validateCapacityRange(minCapacity, maxCapacity);

		// Service는 Repository의 구현 방식을 모른다. 따라서 아래 Search가 어떤 조회 방식을 쓰는지 관심 X
		return roomRepository.search(status, minCapacity, maxCapacity).stream()
				.map(RoomResponse::from)
				.toList();
	}

	private void validateStatus(String status) {
		if (status == null) {
			return;
		}
		if (!status.equals("ACTIVE")
				&& !status.equals("INACTIVE")
				&& !status.equals("MAINTENANCE")) {
			throw new BusinessException(ErrorCode.INVALID_REQUEST);
		}
	}

	private void validateCapacityRange(Integer minCapacity, Integer maxCapacity) {
		if (minCapacity != null && maxCapacity != null && minCapacity > maxCapacity) {
			throw new BusinessException(ErrorCode.INVALID_REQUEST);
		}
	}

	public RoomResponse getRoom(Long id) {
		// 단건 조회는 map(RoomResponse::from).orElseThrow(...)가 Spring REST API에서 가장 흔한 패턴.
		return roomRepository.findById(id)
				.map(RoomResponse::from)
				.orElseThrow(() -> new BusinessException(ErrorCode.ROOM_NOT_FOUND));

	}

}