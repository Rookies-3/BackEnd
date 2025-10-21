package com.store.rookiesoneteam.service.impl;

import com.store.rookiesoneteam.component.UserMapper;
import com.store.rookiesoneteam.domain.entity.User;
import com.store.rookiesoneteam.domain.enums.UserRole;
import com.store.rookiesoneteam.domain.enums.UserStatus;
import com.store.rookiesoneteam.dto.UserDTO;
import com.store.rookiesoneteam.repository.UserRepository;
import com.store.rookiesoneteam.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service("userServiceImpl")
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

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
    public UserDTO.Response findUser(String username) {
        User user = userRepository.findByUsernameAndStatus(username, UserStatus.ACTIVE)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));
        return userMapper.toResponse(user);
    }

    @Override
    @Transactional
    public void withdraw(String username) {
        User user = userRepository.findByUsernameAndStatus(username, UserStatus.ACTIVE)
                                  .orElseThrow(() -> new IllegalArgumentException("존재하지 않거나 이미 탈퇴한 사용자입니다."));
        user.deleted();
        log.info("회원탈퇴 처리 완료: username={}", user.getUsername());
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserDTO.UpdateRequest> getAllUsers() {

        return userRepository.findAll().stream()
                .map(UserMapper::toUpdateRequest)
                .toList();
    }

    @Override
    @Transactional
    public UserDTO.Response signup(UserDTO.Request request) {
        if(userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("이미 존재하는 이메일입니다.");
        }

        if(userRepository.existsByNickname(request.getNickname())) {
            throw new IllegalArgumentException("이미 존재하는 이름입니다.");
        }

        User user = userMapper.toEntity(request, passwordEncoder);
        User saved = userRepository.save(user);

        return userMapper.toResponse(saved);
    }
}
