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
import java.util.stream.Collectors;

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

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new CustomException(ErrorCode.INCORRECT_PASSWORD);
        }

        if (StringUtils.hasText(request.getName())) {
            user.changeName(request.getName());
        }
        if (StringUtils.hasText(request.getNewPassword())) {
            user.changePassword(passwordEncoder.encode(request.getNewPassword()));
        }

        if (StringUtils.hasText(request.getNickname()) && !Objects.equals(user.getNickname(), request.getNickname())) {
            if (userRepository.existsByNickname(request.getNickname())) {
                throw new CustomException(ErrorCode.DUPLICATE_NICKNAME);
            }
            user.changeNickname(request.getNickname());
        }

        if (StringUtils.hasText(request.getEmail()) && !Objects.equals(user.getEmail(), request.getEmail())) {
            if (userRepository.existsByEmail(request.getEmail())) {
                throw new CustomException(ErrorCode.DUPLICATE_EMAIL);
            }
            user.changeEmail(request.getEmail());
        }

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
    public void signup(UserDTO.Request request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new CustomException(ErrorCode.DUPLICATE_USERNAME);
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new CustomException(ErrorCode.DUPLICATE_EMAIL);
        }
        if (userRepository.existsByNickname(request.getNickname())) {
            throw new CustomException(ErrorCode.DUPLICATE_NICKNAME);
        }

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
}
