package com.store.rookiesoneteam.component;

import com.store.rookiesoneteam.domain.entity.User;
import com.store.rookiesoneteam.domain.enums.SocialType;
import com.store.rookiesoneteam.domain.enums.UserRole;
import com.store.rookiesoneteam.domain.enums.UserStatus;
import com.store.rookiesoneteam.dto.oauth2.OAuthAttributes;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;

@Component
public class OAuth2AttributeMapper {

    /**
     * 소셜 로그인 플랫폼에서 받은 사용자 정보(attributes)를 우리가 정의한 DTO(OAuthAttributes)로 변환합니다.
     */
    public OAuthAttributes mapToDto(String userNameAttributeName, Map<String, Object> attributes) {
        // 현재는 Google만 지원하므로 Google 변환 로직만 구현합니다.
        return OAuthAttributes.builder()
                .name((String) attributes.get("name"))
                .email((String) attributes.get("email"))
                .nickname((String) attributes.get("name")) // Google은 nickname이 없으므로 name으로 대체
                .attributes(attributes)
                .nameAttributeKey(userNameAttributeName)
                .socialType(SocialType.GOOGLE)
                .build();
    }

    /**
     * DTO(OAuthAttributes)에 담긴 정보를 바탕으로 새로운 User 엔티티를 생성합니다.
     */
    public User mapToUserEntity(OAuthAttributes attributes, String socialId) {
        return User.builder()
                .username(attributes.getEmail()) // 초기 username은 email로 설정
                .name(attributes.getName())
                .email(attributes.getEmail())
                .nickname(attributes.getNickname())
                .role(UserRole.USER) // 신규 가입자는 USER 권한 부여 (USER1 -> USER로 수정)
                .status(UserStatus.ACTIVE) // 소셜 로그인은 바로 활성 상태
                .socialType(attributes.getSocialType())
                .socialId(socialId)
                .password(UUID.randomUUID().toString()) // 비밀번호는 사용하지 않으므로 임의의 값으로 설정
                .build();
    }
}
