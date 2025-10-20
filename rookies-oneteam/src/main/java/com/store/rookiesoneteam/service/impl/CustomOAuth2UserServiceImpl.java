package com.store.rookiesoneteam.service.impl;

import com.store.rookiesoneteam.domain.entity.User;
import com.store.rookiesoneteam.domain.enums.SocialType;
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

    private static final SocialType SOCIAL_TYPE = SocialType.GOOGLE;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2UserService<OAuth2UserRequest, OAuth2User> delegate = new DefaultOAuth2UserService();
        OAuth2User oAuth2User = delegate.loadUser(userRequest);

        String userNameAttributeName = userRequest.getClientRegistration().getProviderDetails().getUserInfoEndpoint().getUserNameAttributeName();
        Map<String, Object> attributes = oAuth2User.getAttributes();

        OAuthAttributes extractAttributes = OAuthAttributes.of(userNameAttributeName, attributes);

        User createdUser = getUser(extractAttributes);

        return new DefaultOAuth2User(
                Collections.singleton(new SimpleGrantedAuthority("ROLE_" + createdUser.getRole().name())),
                attributes,
                extractAttributes.getNameAttributeKey()
        );
    }

    private User getUser(OAuthAttributes attributes) {
        String socialId = attributes.getAttributes().get(attributes.getNameAttributeKey()).toString();
        User findUser = userRepository.findBySocialTypeAndSocialId(SOCIAL_TYPE, socialId).orElse(null);

        if (findUser == null) {
            return saveUser(attributes, socialId);
        }
        return findUser;
    }

    private User saveUser(OAuthAttributes attributes, String socialId) {
        User createdUser = attributes.toEntity(SOCIAL_TYPE, socialId);
        return userRepository.save(createdUser);
    }
}
