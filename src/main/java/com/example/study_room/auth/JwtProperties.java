package com.example.study_room.auth;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/// Spring Boot relaxed binding으로 연관된 속성을 찾아 아래 래코드에 자동으로 연결한다.
/// ConfigurationProperties 어노테이션은 jwt.*로 시작하는 설정만 이 클래스에 묶는다.
/// Validated는 앱 시작 시 검증시키며, secret은 어떻게든 비어있으면 실패한다.
/// 
/// Properties = application.properties/yml에서 읽는 설정 객체라는 뜻
@ConfigurationProperties(prefix = "jwt")
@Validated
public record JwtProperties(
		@NotBlank String secret,
		@NotNull AccessTokenSettings accessTokenSettings,
		@NotNull RefreshTokenSettings refreshTokenSettings) {

	// nested records
	public record AccessTokenSettings(@NotNull Duration expiration) {
	}

	public record RefreshTokenSettings(@NotNull Duration expiration) {
	}
}
