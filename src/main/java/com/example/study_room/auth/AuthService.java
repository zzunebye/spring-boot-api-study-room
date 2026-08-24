package com.example.study_room.auth;

import java.time.Instant;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.study_room.common.exception.BusinessException;
import com.example.study_room.common.exception.ErrorCode;
import com.example.study_room.user.LoginRequest;
import com.example.study_room.user.LoginResponse;
import com.example.study_room.user.SignupRequest;
import com.example.study_room.user.User;
import com.example.study_room.user.UserRepository;
import com.example.study_room.user.UserResponse;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final JwtProperties jwtProperties;

    public AuthService(
            UserRepository userRepository,
            RefreshTokenRepository refreshTokenRepository,
            PasswordEncoder passwordEncoder,
            JwtProperties jwtProperties,
            JwtTokenProvider jwtTokenProvider) {
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
        this.jwtProperties = jwtProperties;
    }

    public UserResponse signup(SignupRequest request) {
        // 이메일 중복 확인
        if (userRepository.existsByEmail(request.email())) {
            throw new BusinessException(ErrorCode.EMAIL_ALREADY_EXISTS);
        }

        User user = User.createForSignup(
                request.email(),
                passwordEncoder.encode(request.password()),
                request.name());
        User saved = userRepository.save(user);

        // 4. DTO 반환 (password 제외)
        return UserResponse.from(saved);

    }

    /**
     * 로그인 요청을 처리하는 메서드.
     * 1. 이메일로 사용자를 조회하고, 비밀번호가 일치하는지 확인한 뒤,
     * 2. access token과 refresh token을 발급한다.
     * 3. refresh token 정보를 저장 후, 로그인 응답을 반환한다.
     *
     * Access Token은 서버에 '유효 목록'이 없기 때문에 클라이언트가 발급 받은 토큰은 만료 전까지 유효하다.
     * Refresh Token은 로그인 마다 DB에 row가 하나 더 생기며 (발급), 예전 기기 refresh도 revoked_at IS
     * NULL && expires_at > now()면 여전히 유효하다. 따라서 다중 디바이스 동시 로그인 허용?
     * 
     * @param request 로그인 요청 정보 (이메일, 비밀번호)
     * @return LoginResponse (access token, refresh token, 사용자 정보)
     */
    @Transactional
    public LoginResponse login(LoginRequest request) {

        User user = userRepository.findByEmail(request.email()).orElseThrow(
                () -> new BusinessException(ErrorCode.INVALID_CREDENTIALS));

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new BusinessException(ErrorCode.INVALID_CREDENTIALS);
        }

        // 항상 access token 생성.
        String accessToken = jwtTokenProvider.createAccessToken(user);
        // 항상 refresh token 생성.
        String refreshToken = jwtTokenProvider.createRefreshToken();

        // refresh token을 해시하여 hex 값 생성
        String tokenHash = RefreshTokenHasher.hash(refreshToken);
        Instant expiresAt = Instant.now().plus(jwtProperties.refreshTokenSettings().expiration());

        refreshTokenRepository.save(RefreshToken.issue(user.getId(), tokenHash, expiresAt));

        return new LoginResponse(accessToken, refreshToken, UserResponse.from(user));
    }

    @Transactional(readOnly = true)
    public RefreshResponse refresh(RefreshTokenRequest request) {
        String tokenHash = RefreshTokenHasher.hash(request.refreshToken());

        RefreshToken stored = refreshTokenRepository.findByTokenHash(tokenHash)
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_REFRESH_TOKEN));

        // 지금 시점에서 유효하지 않다면 예외 발생
        if (!stored.isActive(Instant.now())) {
            throw new BusinessException(ErrorCode.INVALID_REFRESH_TOKEN);
        }

        User user = userRepository.findById(stored.getUserId())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        String accessToken = jwtTokenProvider.createAccessToken(user);

        return new RefreshResponse(accessToken);
    }

    @Transactional
    public void logout(RefreshTokenRequest request) {
        String refreshToken = request.refreshToken();
        String hashed = RefreshTokenHasher.hash(refreshToken);
        // filter, ifPresent are Java Optional's methods.
        refreshTokenRepository.findByTokenHash(hashed)
                .filter(token -> token.isActive(Instant.now()))
                // For idempotency
                .ifPresent(token -> {
                    // 저장된 refresh token을 revoke(폐기) 처리하고, 변경 사항을 저장한다.
                    token.revoke(); // 토큰의 활성 상태를 비활성화
                    refreshTokenRepository.save(token); // 상태가 변경된 토큰을 저장소에 반영
                });

    }
}
