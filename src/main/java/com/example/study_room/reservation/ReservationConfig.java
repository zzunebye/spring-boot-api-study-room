package com.example.study_room.reservation;

import java.time.ZoneId;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(ReservationProperties.class)
public class ReservationConfig {
	@Bean
	ZoneId reservationZoneId(ReservationProperties properties) {
		return properties.zoneId();
	}
}
