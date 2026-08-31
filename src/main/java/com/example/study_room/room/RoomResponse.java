package com.example.study_room.room;

/**
 * RoomResponse
 * Entity를 그대로 내보내지 않고 필요한 정보만 선택하여 반환하기 위한 DTO 클래스
 */
public record RoomResponse(
        Long id,
        Long branchId,
        String name,
        String location,
        int capacity,
        String status) {
    // Entity -> DTO 변환 정적 팩토리 메서드
    // 인스턴스 없이 호출하기 위해 정적 메서드로 선언 - RoomResponse::from
    // from은 Java에서 관례적으로 사용 (from/of)
    public static RoomResponse from(StudyRoom room) {
        return new RoomResponse(
                room.getId(),
                room.getBranchId(),
                room.getName(),
                room.getLocation(),
                room.getCapacity(),
                room.getStatus());
    }

}