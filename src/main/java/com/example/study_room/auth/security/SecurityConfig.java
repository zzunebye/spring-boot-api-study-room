package com.example.study_room.auth.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/// Spring Security 설정 클래스
/// 
///  <HTTP 요청>
///   → SecurityFilterChain (필터들)
///   → authorizeHttpRequests: /actuator/health? → 통과
///                         그 외? → authenticated() → Basic Auth 확인
///   → (인증 OK) → Controller
///   → (인증 실패) → 401
/// 
/// CSRF 검사 끔. REST API + 토큰(JWT 등) 전제일 때 흔히 사용. 쿠키/세션 기반이면 보통 켜 둠
/// 
@Configuration
@EnableWebSecurity
public class SecurityConfig {

	// “어떤 요청을 어떻게 막을지” 규칙 정의
	@Bean
	SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		return http
				.csrf(csrf -> csrf.disable())
				.authorizeHttpRequests(auth -> auth
						.requestMatchers(
								"/actuator/health",
								"/api/v1/auth",
								"/api/v1/auth/**",
								"/api/v1/rooms",
								"/api/v1/rooms/**")
						.permitAll()
						// 그 외 모든 요청은 인증 필요.
						.anyRequest().authenticated())
				// HTTP Basic 인증 사용 (개발용 임시. 나중에 JWT/OAuth2로 교체 예정)
				.httpBasic(Customizer.withDefaults())
				.build();
	}

	@Bean
	PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}
}
