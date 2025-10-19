package com.store.rookiesoneteam.service;

import com.store.rookiesoneteam.dto.LoginDTO;
import jakarta.servlet.http.HttpServletRequest;

public interface LoginService {
    LoginDTO.Response login(LoginDTO.Request dto, HttpServletRequest request);
}
