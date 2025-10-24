package com.store.rookiesoneteam.service.impl;

import com.store.rookiesoneteam.component.UserMapper;
import com.store.rookiesoneteam.domain.entity.User;
import com.store.rookiesoneteam.domain.enums.UserStatus;
import com.store.rookiesoneteam.dto.UserDTO;
import com.store.rookiesoneteam.error.CustomException;
import com.store.rookiesoneteam.error.ErrorCode;
import com.store.rookiesoneteam.repository.UserRepository;
import com.store.rookiesoneteam.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Service("userServiceImpl")
@RequiredArgsConstructor
@Slf4j
public class    UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public UserDTO.Response updateMyInfo(String username, UserDTO.MyInfoUpdateRequest request) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        // 1. 현재 비밀번호 확인
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new CustomException(ErrorCode.INCORRECT_PASSWORD);
        }

        // 2. 각 필드 업데이트 (중복 검사 포함)
        updateField(request.getName(), user::changeName);
        updatePasswordField(request.getNewPassword(), user);
        updateUniqueField(request.getNickname(), user.getNickname(), userRepository::existsByNickname, ErrorCode.DUPLICATE_NICKNAME, user::changeNickname);
        updateUniqueField(request.getEmail(), user.getEmail(), userRepository::existsByEmail, ErrorCode.DUPLICATE_EMAIL, user::changeEmail);
        updateUniqueField(request.getPhone(), user.getPhone(), userRepository::existsByPhone, ErrorCode.DUPLICATE_PHONE, user::changePhone);

        return userMapper.toResponse(user);
    }

    @Override
    @Transactional
    public void signup(UserDTO.Request request) {
        validateSignupRequest(request);
        User user = userMapper.toEntity(request);
        userRepository.save(user);
    }

    @Override
    @Transactional(readOnly = true)
    public UserDTO.Response findUser(String username) {
        User user = userRepository.findByUsernameAndStatus(username, UserStatus.ACTIVE)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        return userMapper.toResponse(user);
    }

    @Override
    @Transactional
    public void withdraw(String username) {
        User user = userRepository.findByUsernameAndStatus(username, UserStatus.ACTIVE)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        user.deleted();
        log.info("회원탈퇴 처리 완료: username={}", user.getUsername());
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserDTO.AdminResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(userMapper::toAdminResponse)
                .collect(Collectors.toList());
    }

    // --- Private Helper Methods ---

    /**
     * 단순 필드 업데이트를 위한 헬퍼 메소드
     */
    private void updateField(String value, Consumer<String> updater) {
        if (StringUtils.hasText(value)) {
            updater.accept(value);
        }
    }

    /**
     * 비밀번호 필드 업데이트를 위한 헬퍼 메소드
     */
    private void updatePasswordField(String newPassword, User user) {
        if (StringUtils.hasText(newPassword)) {
            user.changePassword(passwordEncoder.encode(newPassword));
        }
    }

    /**
     * 고유 값(Unique) 필드 업데이트를 위한 헬퍼 메소드 (중복 검사 포함)
     */
    private <T> void updateUniqueField(T newValue, T currentValue, Predicate<T> existsCheck, ErrorCode errorCode, Consumer<T> updater) {
        if (newValue != null && !Objects.equals(currentValue, newValue)) {
            if (existsCheck.test(newValue)) {
                throw new CustomException(errorCode);
            }
            updater.accept(newValue);
        }
    }

    /**
     * 회원가입 요청 데이터의 유효성을 검사하는 헬퍼 메소드
     */
    private void validateSignupRequest(UserDTO.Request request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new CustomException(ErrorCode.DUPLICATE_USERNAME);
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new CustomException(ErrorCode.DUPLICATE_EMAIL);
        }
        if (userRepository.existsByNickname(request.getNickname())) {
            throw new CustomException(ErrorCode.DUPLICATE_NICKNAME);
        }
    }
}
