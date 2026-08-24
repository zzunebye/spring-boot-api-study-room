package com.example.study_room.auth;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

/**
 * RefreshTokenHasher는 리프레시 토큰을 안전하게 해싱하기 위한 헬퍼(utility) 클래스.
 * 인스턴스화할 수 없으며, 정적 메서드를 통해 해시 기능만 제공.
 */

public final class RefreshTokenHasher {

    private RefreshTokenHasher() {
    }

    public static String hash(String rawToken) {
        try {
            // 여기서 digest란 MessageDigest 클래스의 인스턴스로, 입력된 데이터를 SHA-256 해시 함수로 변환(해싱)하는 역할.
            // 즉, 'digest'는 '소화하다'라는 의미처럼, 데이터를 일정한 크기의 해시값으로 '소화(변환)'하는 객체.
            MessageDigest digest = MessageDigest.getInstance("SHA-256");

            // 전달받은 rawToken을 UTF-8 인코딩 방식으로 바이트 배열로 변환한 후,
            // SHA-256 알고리즘으로 해시하여 바이트 배열 hashed에 저장.
            // 해시된 바이트 배열을 16진수 문자열로 변환하여 반환.
            byte[] hashed = digest.digest(rawToken.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hashed);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is not available", exception);
        }
    }
}
