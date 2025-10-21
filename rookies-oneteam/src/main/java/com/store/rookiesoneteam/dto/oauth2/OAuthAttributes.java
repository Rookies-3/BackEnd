package com.store.rookiesoneteam.dto.oauth2;

import com.store.rookiesoneteam.domain.entity.User;
import com.store.rookiesoneteam.domain.enums.SocialType;
import com.store.rookiesoneteam.domain.enums.UserRole;
import com.store.rookiesoneteam.domain.enums.UserStatus;
import lombok.Builder;
import lombok.Getter;

import java.util.Map;
import java.util.UUID;

@Getter
public class OAuthAttributes {
    private final Map<String, Object> attributes;
    private final String nameAttributeKey;
    private final String name;
    private final String nickname;
    private final String email;
    private final SocialType socialType;

    @Builder
    public OAuthAttributes(Map<String, Object> attributes, String nameAttributeKey, String name, String nickname, String email, SocialType socialType) {
        this.attributes = attributes;
        this.nameAttributeKey = nameAttributeKey;
        this.name = name;
        this.nickname = nickname;
        this.email = email;
        this.socialType = socialType;
    }

    public static OAuthAttributes of(String userNameAttributeName, Map<String, Object> attributes) {
        return ofGoogle(userNameAttributeName, attributes);
    }

    private static OAuthAttributes ofGoogle(String userNameAttributeName, Map<String, Object> attributes) {
        return OAuthAttributes.builder()
                .name((String) attributes.get("name"))
                .email((String) attributes.get("email"))
                .nickname((String) attributes.get("name")) // Google은 nickname이 없으므로 name으로 대체
                .attributes(attributes)
                .nameAttributeKey(userNameAttributeName)
                .socialType(SocialType.GOOGLE)
                .build();
    }

    public User toEntity(SocialType socialType, String socialId) {
        return User.builder()
                .username(email) // username을 email로 초기화
                .name(name)
                .email(email)
                .nickname(nickname)
                .role(UserRole.USER) // 기본 권한
                .status(UserStatus.ACTIVE) // 기본 상태
                .socialType(socialType)
                .socialId(socialId)
                .password(UUID.randomUUID().toString()) // 임시 비밀번호
                .build();
    }
}
