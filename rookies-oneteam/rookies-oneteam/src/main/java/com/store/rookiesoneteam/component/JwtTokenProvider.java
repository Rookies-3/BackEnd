package com.store.rookiesoneteam.component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.Map;

@Component
public class JwtTokenProvider {
    private final SecretKey key;
    private final long validityInMilliseconds = 1000L * 60 * 60; // 1시간
    private final long resetTokenValidity = 1000L * 60 * 30; // 30분

    public JwtTokenProvider() {
        // TODO 클라우드에 붙을 땐 비밀키 생성할 수 있게 만들기.
        String base64Secret = "u8nFz9J3Q1y7m4aVt0pX9qL2s5Yh8BvCj3kR6nW0zPqU1xY2rT5vZ8mN1oQ4wE7";
        this.key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(base64Secret));
    }

    // 토큰 생성할 때 email을 subject로 설정
    // + 토큰에 role, nickname 사용자 정보 클레임에 넣기
    // + accessToken과 resetToken의 type 구분 넣기
    public String createToken(String email, String role, String nickname, String username, Map<String, Object> extraClaims) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + validityInMilliseconds);

        // Claims 생성
        var claimsBuilder = Jwts.claims().subject(email); // 사용자 식별자: email

        // 추가 클레임이 있다면 합치기
        if (!extraClaims.isEmpty()) {
            for (Map.Entry<String, Object> entry : extraClaims.entrySet()) {
                claimsBuilder.add(entry.getKey(), entry.getValue());
            }
        }

        Claims claims = claimsBuilder
                .add("role", "ROLE_" + role)
                .add("nickname", nickname)
                .add("username", username)
                .add("type", "access_token")
                .build();

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public String createResetToken(String email, Map<String, Object> extraClaims) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + resetTokenValidity); // 30분 유효

        var claimsBuilder = Jwts.claims().subject(email);
        claimsBuilder.add("type", "reset_token");

        if (extraClaims != null && !extraClaims.isEmpty()) {
            for (Map.Entry<String, Object> entry : extraClaims.entrySet()) {
                claimsBuilder.add(entry.getKey(), entry.getValue());
            }
        }

        Claims claims = claimsBuilder.build();

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }


    // 토큰에서 Claims 전체 추출
    private Claims getClaimsFromToken(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    // 토큰에서 이메일(subject) 추출
    public String getEmailFromToken(String token) {
        return getClaimsFromToken(token).getSubject();
    }

    // 토큰에서 role 추출
    public String getRoleFromToken(String token) {
        return getClaimsFromToken(token).get("role", String.class);
    }

    // 토큰에서 nickname 추출
    public String getUsernameFromToken(String token) {
        return getClaimsFromToken(token).get("username", String.class);
    }

    // 토큰 타입 추출
    public String getTokenType(String token) {
        return getClaimsFromToken(token).get("type", String.class);
    }

    // 토큰 유효성 검증
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(key)          // 서명 검증용 키 설정
                    .build()
                    .parseSignedClaims(token);      // 파싱 시 예외 발생 여부로 유효성 판단
            return true;
        } catch (Exception e) {
            return false;                        // 서명 불일치, 만료 등 예외 발생 시 false
        }
    }

    // 리셋 토큰 검증
    public boolean validateResetToken(String token, String expectedEmail) {
        try {
            Claims claims = getClaimsFromToken(token);
            String email = claims.getSubject();
            String type = claims.get("type", String.class);
            Date expiration = claims.getExpiration();

            return email.equals(expectedEmail)
                    && "reset_token".equals(type)
                    && expiration.after(new Date());
        } catch (Exception e) {
            return false;
        }
    }


    // 토큰 만료 여부 확인
    public boolean isTokenExpired(String token) {
        try {
            Date expiration = getClaimsFromToken(token).getExpiration();
            return expiration.before(new Date());
        } catch(Exception ex) {
            return true; // 파싱 실패 시 만료된 것으로 함
        }
    }

}
