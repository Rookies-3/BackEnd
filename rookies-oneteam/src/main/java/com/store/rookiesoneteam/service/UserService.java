package com.store.rookiesoneteam.service;

import com.store.rookiesoneteam.domain.enums.UserRole;
import com.store.rookiesoneteam.domain.enums.UserStatus;
import com.store.rookiesoneteam.dto.UserDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface UserService {
    List<UserDTO.UpdateRequest> getAllUsers();

    UserDTO.Response signup(UserDTO.Request request);

    void withdraw(String username);

    UserDTO.Response findUser(String username);

    Page<UserDTO.Response> findAllUsers(Pageable pageable);

    Page<UserDTO.Response> findUsersByStatus(UserStatus status, Pageable pageable);

    Page<UserDTO.Response> findUsersByStatusAndRole(UserStatus status, UserRole role, Pageable pageable);
}
