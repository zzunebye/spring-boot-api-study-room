package com.example.study_room.common.exception;

import org.springframework.http.HttpStatus;

// ErrorCode 열거형은 애플리케이션 전반에서 사용되는 에러 코드를 정의합니다.
// 각 에러 코드는 해당하는 HTTP 상태 코드와 사용자에게 보여질 메시지를 포함합니다.
// 새로운 예외 상황이 발생하면 여기에 에러 코드를 추가하여 일관된 예외 처리를 할 수 있습니다.

public enum ErrorCode {

    INVALID_REQUEST(HttpStatus.BAD_REQUEST, "잘못된 요청입니다."),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."),
    EMAIL_ALREADY_EXISTS(HttpStatus.CONFLICT, "이메일이 이미 사용중."),
    ROOM_NOT_FOUND(HttpStatus.NOT_FOUND, "공간을 찾을 수 없습니다."),
    ROOM_NOT_AVAILABLE(HttpStatus.CONFLICT, "현재 이용할 수 없는 공간입니다."),
    RESERVATION_TIME_CONFLICT(HttpStatus.CONFLICT, "선택한 시간에 이미 다른 예약이 있습니다."),
    RESERVATION_POLICY_VIOLATION(HttpStatus.BAD_REQUEST, "예약 정책을 위반했습니다."),
    RESERVATION_FORBIDDEN(HttpStatus.FORBIDDEN, "예약에 대한 권한이 없습니다."),
    INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "인증 정보가 올바르지 않습니다.");

    private final HttpStatus httpStatus;
    private final String message;

    ErrorCode(HttpStatus httpStatus, String message) {
        this.httpStatus = httpStatus;
        this.message = message;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

    public String getMessage() {
        return message;
    }
}
