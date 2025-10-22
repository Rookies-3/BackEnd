package com.store.rookiesoneteam.dto.oauth2;

import com.store.rookiesoneteam.domain.enums.SocialType;
import lombok.Builder;
import lombok.Getter;

import java.util.Map;

/**
 * 소셜 로그인 플랫폼에서 받아온 사용자 정보를 담는 순수한 데이터 전송 객체(DTO)입니다.
 */
@Getter
@Builder
public class OAuthAttributes {
    private final Map<String, Object> attributes;
    private final String nameAttributeKey;
    private final String name;
    private final String nickname;
    private final String email;
    private final SocialType socialType;
}
