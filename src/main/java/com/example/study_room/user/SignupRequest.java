package com.example.study_room.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/// Validation에 관해
/// 컴파일 타임에 값을 막거나 레코드 생성자를 바꾸는 것이 아님. 누군가 Validator를 돌리면 검사가 수행.
/// 이를 수행하는 구현체는 보통 Hibernate Validator
/// Spring MVC에서 컨트롤러 파라미터에 @Valid / @Validated가 있으면, 바인딩 직후 Validator를 호출함.
/// 
/// [바인딩] - HTTP 요청 값을 자바 객체 필드에 채워 넣는 과정
/// 
/// 일반 자바 프로젝트에서도 사용이 가능하다. 필요한건 hibernate-validator이기에, 
/// 이를 사용해 직접 Validator를 호출하면 된다.
/// ```
/// Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
/// Set<ConstraintViolation<SignupRequest>> violations = validator.validate(request);
/// ```

/**
 * SignupRequest
 * DTO object
 * 
 * 이 객체는 규칙만 선언하며 예외를 직접 던지지 않는다.
 * 생성 시 외부에서 @Valid를 통해서 검증 - Controller가 검사 트리거
 */
public record SignupRequest(
                @NotBlank @Email String email,
                @NotBlank @Size(min = 8) @Pattern(regexp = ".*[A-Z].*") String password,
                @NotBlank @Size(max = 100) String name) {
}