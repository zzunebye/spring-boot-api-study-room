package com.example.study_room.auth;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

import javax.crypto.SecretKey;

import org.springframework.stereotype.Component;

import com.example.study_room.user.User;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtTokenProvider {

	private final JwtProperties jwtProperties;
	private final SecretKey signingKey;

	public JwtTokenProvider(JwtProperties jwtProperties) {
		this.jwtProperties = jwtProperties;
		this.signingKey = Keys.hmacShaKeyFor(
				jwtProperties.secret().getBytes(StandardCharsets.UTF_8));
	}

	/**
	 * 주어진 User 정보를 기반으로 액세스 토큰(JWT)을 생성한다.
	 * 
	 * @param user JWT에 포함할 User 정보
	 * @return 생성된 JWT 액세스 토큰 문자열
	 */
	public String createAccessToken(User user) {
		// 액세스 토큰 만료 시간 계산
		Instant now = Instant.now();
		Instant expiry = now.plus(jwtProperties.accessToken().expiration());

		// JWT 빌더를 사용해 토큰을 생성하고 반환
		return Jwts.builder()
				// 토큰 subjec (sub)에 user id를 저장
				.subject(String.valueOf(user.getId()))
				// 사용자 이메일과 권한(역할)을 claim에 포함
				.claim("email", user.getEmail())
				.claim("role", user.getRole())
				// 토큰 발급 및 만료 시간 설정
				.issuedAt(Date.from(now))
				.expiration(Date.from(expiry))
				// 비밀키로 토큰 서명
				.signWith(signingKey)
				// JWT 문자열 생성
				.compact();
	}

	public String createRefreshToken() {
		return UUID.randomUUID().toString().replace("-", "")
				+ UUID.randomUUID().toString().replace("-", "");
	}

	/**
	 * @param token 파싱할 JWT 액세스 토큰 문자열
	 * @return 토큰에서 추출한 Claims(클레임) 객체 - 기본적으로 Map
	 */
	public Claims parseAccessToken(String token) {
		return Jwts.parser() // JWT 파서 인스턴스 생성
				.verifyWith(signingKey) // 토큰 검증을 위한 서명 키 설정
				.build() // 파서 빌드
				.parseSignedClaims(token) // 서명된 클레임 파싱
				.getPayload(); // 클레임(Payload) 추출 및 반환
	}

	public Long getUserIdFromAccessToken(String token) {
		return Long.parseLong(parseAccessToken(token).getSubject());
	}

}
