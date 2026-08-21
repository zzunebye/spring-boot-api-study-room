package com.example.study_room.common.exception;

/**
 * ErrorResponse는 에러 코드와 해당 메시지를 캡슐화하는 레코드 타입입니다.
 * <p>
 * 이 클래스는 애플리케이션에서 예외나 오류가 발생했을 때
 * 표준화된 응답을 전송하는 데 주로 사용됩니다.
 * </p>
 *
 * @param code    오류 코드를 나타내는 문자열 (일반적으로 ErrorCode enum에서 매핑됨)
 * @param message 오류에 대한 설명 메시지
 */

public record ErrorResponse(
        String code,
        String message) {

    public static ErrorResponse from(ErrorCode errorCode) {
        return new ErrorResponse(errorCode.name(), errorCode.getMessage());
    }
}
