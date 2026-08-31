package com.example.study_room.reservation;

import java.util.UUID;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.study_room.common.exception.BusinessException;
import com.example.study_room.common.exception.ErrorCode;
import com.example.study_room.room.RoomAvailabilityChecker;
import com.example.study_room.room.RoomRepository;
import com.example.study_room.room.StudyRoom;

@Service
public class ReservationService {
    private final ReservationRepository reservationRepository;
    private final RoomRepository roomRepository;
    private final ReservationTimePolicy reservationTimePolicy;
    private final ActiveReservationPolicy activeReservationPolicy;
    private final RoomAvailabilityChecker roomAvailabilityChecker;

    public ReservationService(
            ReservationRepository reservationRepository,
            RoomRepository roomRepository,
            ReservationTimePolicy reservationTimePolicy,
            ActiveReservationPolicy activeReservationPolicy,
            RoomAvailabilityChecker roomAvailabilityChecker) {
        this.reservationRepository = reservationRepository;
        this.roomRepository = roomRepository;
        this.reservationTimePolicy = reservationTimePolicy;
        this.activeReservationPolicy = activeReservationPolicy;
        this.roomAvailabilityChecker = roomAvailabilityChecker;
    }

    @Transactional
    public ReserveResult reserve(Long userId, CreateReservationRequest request, UUID requestKey) {

        // Idempotency Check
        Reservation existingReservation = reservationRepository
                .findByUserIdAndRequestKey(userId, requestKey)
                .orElse(null);

        if (existingReservation != null) {
            return new ReserveResult(ReservationResponse.from(existingReservation), false);
        }

        StudyRoom room = roomRepository
                .findById(request.roomId())
                .orElseThrow(() -> new BusinessException(ErrorCode.ROOM_NOT_FOUND));

        if (!room.isReservable()) {
            throw new BusinessException(ErrorCode.ROOM_NOT_AVAILABLE);
        }
        if (!room.canAccommodate(request.participantCount())) {
            throw new BusinessException(ErrorCode.RESERVATION_POLICY_VIOLATION);
        }

        reservationTimePolicy.validate(request.startAt(), request.endAt());
        activeReservationPolicy.validate(userId);
        roomAvailabilityChecker.validate(room, request.startAt(), request.endAt());

        Reservation reservation = Reservation.create(
                userId,
                request.roomId(),
                request.startAt(),
                request.endAt(),
                request.participantCount(),
                request.purpose(),
                requestKey);

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
