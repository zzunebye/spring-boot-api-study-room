package com.example.study_room.common.exception;

/**
 * 비즈니스 로직 처리 중 발생할 수 있는 예외를 나타내는 클래스입니다.
 * <p>
 * 이 클래스는 {@link RuntimeException}을 확장하며, 발생한 예외에 대해 {@link ErrorCode}를 함께
 * 제공합니다.
 * 서비스 또는 도메인 계층에서 비즈니스 예외 상황을 표준화하여 처리할 때 사용합니다.
 */
public class BusinessException extends RuntimeException {

    private final ErrorCode errorCode;

    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }
}
