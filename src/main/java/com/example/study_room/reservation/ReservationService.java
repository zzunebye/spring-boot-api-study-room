package com.example.study_room.reservation;

import java.util.UUID;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.study_room.common.exception.BusinessException;
import com.example.study_room.common.exception.ErrorCode;
import com.example.study_room.room.RoomRepository;
import com.example.study_room.room.StudyRoom;

@Service
public class ReservationService {
    private final ReservationRepository reservationRepository;
    private final RoomRepository roomRepository;
    private final ReservationPolicy reservationPolicy;

    public ReservationService(
            ReservationRepository reservationRepository,
            RoomRepository roomRepository,
            ReservationPolicy reservationPolicy) {
        this.reservationRepository = reservationRepository;
        this.roomRepository = roomRepository;
        this.reservationPolicy = reservationPolicy;
    }

    @Transactional
    public ReserveResult reserve(Long userId, CreateReservationRequest request, UUID requestKey) {

        // Idempotency Check
        Reservation existingReservation = reservationRepository.findByUserIdAndRequestKey(userId, requestKey)
                .orElse(null);
        if (existingReservation != null) {
            return new ReserveResult(ReservationResponse.from(existingReservation), false);
        }

        // Find Room by ID
        StudyRoom room = roomRepository.findById(request.roomId())
                .orElseThrow(() -> new BusinessException(ErrorCode.ROOM_NOT_FOUND));

        if (!"ACTIVE".equals(room.getStatus())) {
            throw new BusinessException(ErrorCode.ROOM_NOT_AVAILABLE);
        }

        // ReservationPolicy: time range, slot grid, future limit, capacity, active
        // count
        reservationPolicy.validateForCreate(
                userId,
                room,
                request.startAt(),
                request.endAt(),
                request.participantCount());

        Reservation reservation = Reservation.create(
                userId,
                request.roomId(),
                request.startAt(),
                request.endAt(),
                request.participantCount(),
                request.purpose(),
                requestKey);

        // Attempt to save the reservation, handling duplicate or conflicting time
        try {
            Reservation saved = reservationRepository.saveAndFlush(reservation);
            return new ReserveResult(ReservationResponse.from(saved), true);
        } catch (DataIntegrityViolationException ex) {
            // Postgres aborts this transaction on constraint failure.
            // Do not query the session here — throw and let the TX roll back.
            throw new BusinessException(ErrorCode.RESERVATION_TIME_CONFLICT);
        }
    }
}
