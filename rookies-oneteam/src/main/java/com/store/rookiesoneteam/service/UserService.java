package com.store.rookiesoneteam.service;

import com.store.rookiesoneteam.dto.UserDTO;

import java.util.List;

public interface UserService {

    UserDTO.Response findUser(String username);

    void withdraw(String username);

    List<UserDTO.AdminResponse> getAllUsers();

    void signup(UserDTO.Request request);

    UserDTO.Response updateMyInfo(String username, UserDTO.MyInfoUpdateRequest updateRequest);
}
