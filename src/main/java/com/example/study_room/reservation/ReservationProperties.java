package com.example.study_room.reservation;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

@ConfigurationProperties(prefix = "reservation")
@Validated
public record ReservationProperties(
		@NotNull Duration minimumDuration,
		@NotNull Duration maximumDuration,
		@NotNull Duration slotUnit,
		@Min(1) int maximumFutureDays,
		@Min(1) int maximumActiveCount) {
}
