package com.store.rookiesoneteam.service;

import com.store.rookiesoneteam.domain.enums.UserRole;
import com.store.rookiesoneteam.domain.enums.UserStatus;
import com.store.rookiesoneteam.dto.UserDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface UserService {
    Page<UserDTO.Response> findUsersByStatus(UserStatus status, Pageable pageable);

    Page<UserDTO.Response> findUsersByStatusAndRole(UserStatus status, UserRole role, Pageable pageable);

    Page<UserDTO.Response> findAllUsers(Pageable pageable);

    UserDTO.Response findUser(String username);

    void withdraw(String username);

    List<UserDTO.UpdateRequest> getAllUsers();

    UserDTO.Response signup(UserDTO.Request request);

    /**
     * 현재 로그인한 사용자의 정보를 수정합니다.
     * @param username 현재 로그인한 사용자 아이디
     * @param updateRequest 수정할 정보가 담긴 DTO
     * @return 수정된 사용자 정보
     */
    UserDTO.Response updateMyInfo(String username, UserDTO.MyInfoUpdateRequest updateRequest);
}
