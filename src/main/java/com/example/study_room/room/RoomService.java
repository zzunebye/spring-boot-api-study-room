package com.example.study_room.room;

import java.util.List;

import org.springframework.stereotype.Service;

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

    public List<RoomResponse> getAllRooms() {
        // 컬렉션을 Entity → DTO로 바꿀 때 자주 쓰는 패턴
        return roomRepository.findAll().stream()
                .map(RoomResponse::from)
                .toList();
    }

}