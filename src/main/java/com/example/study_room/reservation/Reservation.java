package com.example.study_room.reservation;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "reservations")
public class Reservation {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private Long userId;

	@Column(nullable = false)
	private Long roomId;

	@Column(nullable = false)
	private Instant startAt;

	@Column(nullable = false)
	private Instant endAt;

	@Column(nullable = false)
	private int participantCount;

	private String purpose;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private ReservationStatus status;

	// Key for idempotency. If the same requestKey is submitted, the reservation
	// will be rejected.
	// i.e. - double-click on the 'reserve' button.
	@Column(nullable = false)
	private UUID requestKey;

	private Instant cancelledAt;

	private String cancelReason;

	@Column(nullable = false)
	private Instant createdAt;

	@Column(nullable = false)
	private Instant updatedAt;

	// For creating proxy object(constructor without parameters) so that outside
	// can't create instance directly.
	// but internally JPA can create instance using reflection.
	protected Reservation() {
	}

	public static Reservation create(
			Long userId,
			Long roomId,
			Instant startAt,
			Instant endAt,
			int participantCount,
			String purpose,
			UUID requestKey) {
		Reservation reservation = new Reservation();
		reservation.userId = userId;
		reservation.roomId = roomId;
		reservation.startAt = startAt;
		reservation.endAt = endAt;
		reservation.participantCount = participantCount;
		reservation.purpose = purpose;
		reservation.status = ReservationStatus.CONFIRMED;
		reservation.requestKey = requestKey;
		Instant now = Instant.now();
		reservation.createdAt = now;
		reservation.updatedAt = now;
		return reservation;
	}

	public Long getId() {
		return id;
	}

	public Long getUserId() {
		return userId;
	}

	public Long getRoomId() {
		return roomId;
	}

	public Instant getStartAt() {
		return startAt;
	}

	public Instant getEndAt() {
		return endAt;
	}

	public int getParticipantCount() {
		return participantCount;
	}

	public String getPurpose() {
		return purpose;
	}

	public ReservationStatus getStatus() {
		return status;
	}

	public UUID getRequestKey() {
		return requestKey;
	}

	public Instant getCancelledAt() {
		return cancelledAt;
	}

	public String getCancelReason() {
		return cancelReason;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	public Instant getUpdatedAt() {
		return updatedAt;
	}
}
