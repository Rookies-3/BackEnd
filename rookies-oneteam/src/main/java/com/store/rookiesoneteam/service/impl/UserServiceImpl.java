package com.store.rookiesoneteam.service.impl;

import com.store.rookiesoneteam.component.UserMapper;
import com.store.rookiesoneteam.domain.entity.User;
import com.store.rookiesoneteam.domain.enums.UserRole;
import com.store.rookiesoneteam.domain.enums.UserStatus;
import com.store.rookiesoneteam.dto.UserDTO;
import com.store.rookiesoneteam.error.CustomException;
import com.store.rookiesoneteam.error.ErrorCode;
import com.store.rookiesoneteam.repository.UserRepository;
import com.store.rookiesoneteam.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Objects;

@Service("userServiceImpl")
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public UserDTO.Response updateMyInfo(String username, UserDTO.MyInfoUpdateRequest request) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        // 1. 현재 비밀번호 확인 (필수)
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new CustomException(ErrorCode.INCORRECT_PASSWORD);
        }

        // 2. 변경 요청이 들어온 필드만 선택적으로 업데이트
        if (StringUtils.hasText(request.getName())) {
            user.changeName(request.getName());
        }
        if (StringUtils.hasText(request.getNewPassword())) {
            user.changePassword(passwordEncoder.encode(request.getNewPassword()));
        }

        // 닉네임 변경 및 중복 확인
        if (StringUtils.hasText(request.getNickname()) && !Objects.equals(user.getNickname(), request.getNickname())) {
            if (userRepository.existsByNickname(request.getNickname())) {
                throw new CustomException(ErrorCode.DUPLICATE_NICKNAME);
            }
            user.changeNickname(request.getNickname());
        }

        // 이메일 변경 및 중복 확인
        if (StringUtils.hasText(request.getEmail()) && !Objects.equals(user.getEmail(), request.getEmail())) {
            if (userRepository.existsByEmail(request.getEmail())) {
                throw new CustomException(ErrorCode.DUPLICATE_EMAIL);
            }
            user.changeEmail(request.getEmail());
        }

        // 전화번호 변경 및 중복 확인
        if (StringUtils.hasText(request.getPhone()) && !Objects.equals(user.getPhone(), request.getPhone())) {
            if (userRepository.existsByPhone(request.getPhone())) {
                throw new CustomException(ErrorCode.DUPLICATE_PHONE);
            }
            user.changePhone(request.getPhone());
        }

        return userMapper.toResponse(user);
    }

    @Override
    @Transactional
    public UserDTO.Response signup(UserDTO.Request request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new CustomException(ErrorCode.DUPLICATE_USERNAME);
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new CustomException(ErrorCode.DUPLICATE_EMAIL);
        }
        if (userRepository.existsByNickname(request.getNickname())) {
            throw new CustomException(ErrorCode.DUPLICATE_NICKNAME);
        }

        User user = userMapper.toEntity(request, passwordEncoder);
        User saved = userRepository.save(user);

        return userMapper.toResponse(saved);
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

    // --- 이하 관리자용 또는 기타 메소드 (변경 없음) ---
    @Override
    public Page<UserDTO.Response> findUsersByStatus(UserStatus status, Pageable pageable) {
        return userRepository.findAllByStatus(status, pageable)
                .map(userMapper::toResponse);
    }

    @Override
    public Page<UserDTO.Response> findUsersByStatusAndRole(UserStatus status, UserRole role, Pageable pageable) {
        return userRepository.findAllByStatusAndRole(status, role, pageable)
                .map(userMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserDTO.Response> findAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable)
                .map(userMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserDTO.UpdateRequest> getAllUsers() {
        return userRepository.findAll().stream()
                .map(UserMapper::toUpdateRequest)
                .toList();
    }
}
