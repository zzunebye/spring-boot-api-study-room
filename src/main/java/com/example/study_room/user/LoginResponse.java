package com.example.study_room.user;

public record LoginResponse(
        String accessToken,
        String refreshToken,
        UserResponse user) {
}
