package com.example.study_room.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import jakarta.validation.ConstraintViolationException;

@RestControllerAdvice
public class GlobalExceptionHandler {
	// [BusinessException]이 발생하면 handleBusinessException 메소드가 실행됩니다.
	// 먼저, 예외 객체에서 ErrorCode를 추출합니다.
	// ErrorCode로부터 HTTP 상태 값을 얻어 ResponseEntity의 상태 코드로 설정합니다.
	// 그리고 ErrorResponse 객체로 변환하여 응답 본문에 담아 반환합니다.
	//
	// ResponseEntity는 Spring Framework에서 HTTP 응답의 상태 코드, 헤더, 본문을 모두 포함할 수 있게 해주는
	// 클래스입니다.
	@ExceptionHandler(BusinessException.class)
	public ResponseEntity<ErrorResponse> handleBusinessException(BusinessException exception) {
		ErrorCode errorCode = exception.getErrorCode();
		return ResponseEntity
				.status(errorCode.getHttpStatus())
				.body(ErrorResponse.from(errorCode));
	}

	@ExceptionHandler({ MethodArgumentNotValidException.class, ConstraintViolationException.class })
	public ResponseEntity<ErrorResponse> handleValidation(Exception ex) {
		return ResponseEntity
				.status(HttpStatus.BAD_REQUEST)
				.body(ErrorResponse.from(ErrorCode.INVALID_REQUEST));
	}

}
