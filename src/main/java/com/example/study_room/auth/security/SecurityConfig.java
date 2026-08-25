package com.example.study_room.auth.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.example.study_room.auth.JwtAuthenticationFilter;
import com.example.study_room.auth.JwtTokenProvider;

/// Spring Security 설정 클래스
/// 요청 한 번 처리하는 동안의 ThreadLocal container. [Authorization] from header will be stored here
/// 
///  <HTTP 요청>
///   → SecurityFilterChain (필터들)
///   → authorizeHttpRequests: /actuator/health? → 통과
///                         그 외? → authenticated() → Basic Auth 확인
///   → (인증 OK) → Controller || → (인증 실패) → 401
/// 
/// Spring Security는 서블릿 앞에 [FilterChainProxy]를 둠. 이 프록시 객체가 컨테이너에서 SecurityFilterChain 빈을 찾아 
/// 요청을 그 체인으로 보낸다.
/// 
/// CSRF 검사 끔. REST API + 토큰(JWT 등) 전제일 때 흔히 사용. 쿠키/세션 기반이면 보통 켜 둠
/// 
/// @EnableWebSecurity는 Security 인프라(필터 프록시, HttpSecurity 빌더)를 켜는 스위치이고, 실제 규칙 객체는 @Bean.
@Configuration
@EnableWebSecurity
public class SecurityConfig {

	/// “어떤 요청을 어떻게 막을지” 규칙 정의
	/// @Bean인 이유는 반환값인 SecurityFilterChain을 Bean으로 등록시켜, 스프링 컨테이너에 등록해야 Spring
	/// Security가 쓰기 때문.
	/// 메서드의 역할은 설정 파일을 읽는 게 아니라 “이 앱의 필터 체인은 이 객체다”라고 컨테이너에 등록하는 것.

	@Bean
	SecurityFilterChain securityFilterChain(
			HttpSecurity http,
			JwtAuthenticationFilter jwtAuthenticationFilter,
			JsonAuthenticationEntryPoint authenticationEntryPoint,
			JsonAccessDeniedHandler accessDeniedHandler) throws Exception {
		return http
				.csrf(csrf -> csrf.disable())
				.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				.authorizeHttpRequests(auth -> auth
						.requestMatchers(
								"/error",
								"/actuator/health",
								"/api/v1/auth",
								"/api/v1/auth/**",
								"/api/v1/rooms",
								"/api/v1/rooms/**")
						.permitAll()
						// 그 외 모든 요청은 인증 필요.
						.anyRequest().authenticated())
				.httpBasic(AbstractHttpConfigurer::disable)
				// 브라우저용 로그인 폼(/login 페이지)을 끄기
				.formLogin(AbstractHttpConfigurer::disable)
				// HTTP Basic 인증 사용 (개발용 임시. 나중에 JWT/OAuth2로 교체 예정)
				// .httpBasic(Customizer.withDefaults()).formLogin(null)
				// 401/403을 ErrorResponse JSON으로 통일 (필터 체인에서 발생 → Advice가 못 잡음)
				.exceptionHandling(ex -> ex
						.authenticationEntryPoint(authenticationEntryPoint)
						.accessDeniedHandler(accessDeniedHandler))
				// JWT 필터를 Security 필터 체인에 등록.
				// Authorization: Bearer ...를 읽어 토큰을 검증하고 SecurityContext에 사용자를 넣는 필터
				.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
				.build();
	}

	@Bean
	JwtAuthenticationFilter jwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider) {
		return new JwtAuthenticationFilter(jwtTokenProvider);
	}

	@Bean
	PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}
}
