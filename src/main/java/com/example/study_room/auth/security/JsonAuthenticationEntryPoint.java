package com.example.study_room.auth.security;

import java.io.IOException;

import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import com.example.study_room.common.exception.ErrorCode;
import com.example.study_room.common.exception.ErrorResponse;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import tools.jackson.databind.ObjectMapper;

/**
 * 인증되지 않은 요청(401)을 {@link ErrorResponse} JSON으로 반환한다.
 * Security 필터 체인에서 발생하므로 {@code @RestControllerAdvice}가 잡지 못하기 때문에 별도로 처리해야 한다.
 */
@Component
public class JsonAuthenticationEntryPoint implements AuthenticationEntryPoint {

	private final ObjectMapper objectMapper;

	public JsonAuthenticationEntryPoint(ObjectMapper objectMapper) {
		this.objectMapper = objectMapper;
	}

	@Override
	public void commence(
			HttpServletRequest request,
			HttpServletResponse response,
			AuthenticationException authException) throws IOException {

		ErrorCode errorCode = ErrorCode.AUTHENTICATION_REQUIRED;

		response.setStatus(errorCode.getHttpStatus().value());
		response.setContentType(MediaType.APPLICATION_JSON_VALUE);
		response.setCharacterEncoding("UTF-8");

		objectMapper.writeValue(response.getOutputStream(), ErrorResponse.from(errorCode));
	}
}
