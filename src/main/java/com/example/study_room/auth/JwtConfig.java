package com.example.study_room.auth;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/// JwtProperties는 JWT 설정을 Java 객체로 읽는 곳, 
/// JwtConfig는 그 객체를 Spring Bean으로 등록하는 설정.
/// 설정을 읽는 클래스(JwtProperties) 와 Spring에 등록하는 클래스(JwtConfig)라는 다른 역할을 가진다.
/// 
/// @Configuration - 이 클래스는 Spring 설정 클래스다
/// @EnableConfigurationProperties - JwtProperties를 Bean으로 등록하고, properties 파일 값을 채워 넣어라
@Configuration
@EnableConfigurationProperties(JwtProperties.class)
public class JwtConfig {
}
