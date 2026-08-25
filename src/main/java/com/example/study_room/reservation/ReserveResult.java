package com.example.study_room.reservation;

/**
 * 예약 요청의 결과를 나타내는 클래스:
 * reservation: 예약 관련 응답 데이터
 * created: 새 예약이 생성되었는지 여부
 */
public record ReserveResult(ReservationResponse reservation, boolean created) {
}
