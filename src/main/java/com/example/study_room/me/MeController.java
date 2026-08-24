package com.example.study_room.me;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.RestController;

import com.example.study_room.auth.AuthUser;
import com.example.study_room.common.exception.BusinessException;
import com.example.study_room.common.exception.ErrorCode;
import com.example.study_room.user.User;
import com.example.study_room.user.UserRepository;
import com.example.study_room.user.UserResponse;
import org.springframework.web.bind.annotation.GetMapping;

@RestController
public class MeController {
    final UserRepository userRepository;

    public MeController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /// 현재 인증된 사용자의 정보를 반환.
    /// @AuthenticationPrincipal - Annotation that is used to resolve
    /// Authentication.getPrincipal() to a method argument,
    ///
    /// “이 요청을 누가 보냈는지”를 컨트롤러 파라미터로 꺼내, 클라이언트가 userId를 body나 query로 보내게 하지 않고, 서버가
    /// 검증한 인증 정보만 쓰도록 함.
    /// `SecurityContextHolder.getContext().setAuthentication(authentication)`로
    /// 저장되었던 UsernamePasswordAuthenticationToken의 principal 객체를 꺼내옴.
    @GetMapping("api/v1/me")
    public ResponseEntity<UserResponse> me(@AuthenticationPrincipal AuthUser authUser) {
        final User user = userRepository.findById(authUser.id())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        return ResponseEntity.ok(UserResponse.from(user));
    }

}
