package com.example.study_room.reservation;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

	Optional<Reservation> findByUserIdAndRequestKey(Long userId, UUID requestKey);
}
