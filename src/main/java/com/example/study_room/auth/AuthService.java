package com.example.study_room.auth;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.study_room.common.exception.BusinessException;
import com.example.study_room.common.exception.ErrorCode;
import com.example.study_room.user.SignupRequest;
import com.example.study_room.user.User;
import com.example.study_room.user.UserRepository;
import com.example.study_room.user.UserResponse;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
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

}
