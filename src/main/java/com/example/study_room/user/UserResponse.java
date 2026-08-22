package com.example.study_room.user;

public record UserResponse(
        Long id,
        String email,
        String name,
        String role,
        String status) {

    public static UserResponse from(User user) {
        return new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getName(),
                user.getRole(),
                user.getStatus());
        // passwordHash는 절대 포함하지 않음
    }
}
