package com.store.rookiesoneteam.service.impl;

import com.store.rookiesoneteam.component.OAuth2AttributeMapper;
import com.store.rookiesoneteam.domain.entity.User;
import com.store.rookiesoneteam.dto.oauth2.OAuthAttributes;
import com.store.rookiesoneteam.repository.UserRepository;
import com.store.rookiesoneteam.service.CustomOAuth2UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional
public class CustomOAuth2UserServiceImpl extends DefaultOAuth2UserService implements CustomOAuth2UserService {

    private final UserRepository userRepository;
    private final OAuth2AttributeMapper attributeMapper; // 새로운 매퍼 주입

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2UserService<OAuth2UserRequest, OAuth2User> delegate = new DefaultOAuth2UserService();
        OAuth2User oAuth2User = delegate.loadUser(userRequest);

        String userNameAttributeName = userRequest.getClientRegistration().getProviderDetails().getUserInfoEndpoint().getUserNameAttributeName();
        Map<String, Object> attributes = oAuth2User.getAttributes();

        // 매퍼를 사용하여 DTO 생성
        OAuthAttributes extractAttributes = attributeMapper.mapToDto(userNameAttributeName, attributes);

        // DTO를 기반으로 User 엔티티를 찾거나 새로 생성
        User userEntity = getUser(extractAttributes);

        return new DefaultOAuth2User(
                Collections.singleton(new SimpleGrantedAuthority("ROLE_" + userEntity.getRole().name())),
                attributes,
                extractAttributes.getNameAttributeKey()
        );
    }

    private User getUser(OAuthAttributes attributes) {
        String socialId = attributes.getAttributes().get(attributes.getNameAttributeKey()).toString();
        // 소셜 정보로 사용자를 찾고, 없으면 새로 저장하는 로직
        return userRepository.findBySocialTypeAndSocialId(attributes.getSocialType(), socialId)
                .orElseGet(() -> saveUser(attributes, socialId));
    }

    private User saveUser(OAuthAttributes attributes, String socialId) {
        // 매퍼를 사용하여 User 엔티티 생성
        User newUser = attributeMapper.mapToUserEntity(attributes, socialId);
        return userRepository.save(newUser);
    }
}
