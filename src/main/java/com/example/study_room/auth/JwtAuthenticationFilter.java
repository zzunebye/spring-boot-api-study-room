package com.example.study_room.auth;

import java.io.IOException;
import java.util.List;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/// 여기서 @Component를 붙이지 않는 이유는 자동으로 Security 체인에 들어가지 않기 때문.
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtTokenProvider jwtTokenProvider;

    public JwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider) {
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        String token = resolveBearerToken(request);

        if (token != null) {

            try {
                Claims claims = jwtTokenProvider.parseAccessToken(token);

                Long userId = Long.parseLong(claims.getSubject());
                String email = claims.get("email", String.class);
                String role = claims.get("role", String.class);

                AuthUser authUser = new AuthUser(userId, email, role);

                // 사용자의 역할(role)에 기반하여 Spring Security 권한을 생성합니다.
                // org.springframework.security.core.authority
                var authorities = List.of(new SimpleGrantedAuthority("ROLE_" + role));

                // org.springframework.security.authentication
                // Spring Security 인증 객체로,각 리퀘스트마다 ThreadLocal SecurityContext에 저장되어,
                // 해당 요청동안 확인 가능
                var authentication = new UsernamePasswordAuthenticationToken(
                        authUser, // principal -> @AuthenticationPrincipal
                        null, // credenticals (JWT API에서는 보통 null)
                        authorities // ROLE_USER, ROLE_ADMIN
                );

                // 인증 정보 저장.
                SecurityContextHolder.getContext().setAuthentication(authentication);

                // JwtException과 IllegalArgumentException는 JWT 파싱/검증과 관련된 구체적인 예외로
            } catch (JwtException | IllegalArgumentException ex) {
                // 만료, 위조, 형식 오류 → 인증 컨텍스트 비우고 통과
                SecurityContextHolder.clearContext();
            }

        }

        // 체인 계속 진행 - 다음 필터 / 컨트롤러 / 응답 작성
        // doFilter를 호출하지 않으면, 요청이 멈추고 응답이 반환되지 않음.

        filterChain.doFilter(request, response);
    }

    private String resolveBearerToken(HttpServletRequest request) {
        String header = request.getHeader(AUTHORIZATION_HEADER);
        if (header == null || !header.startsWith(BEARER_PREFIX)) {
            return null;
        }
        return header.substring(BEARER_PREFIX.length()).trim();

    }

}
