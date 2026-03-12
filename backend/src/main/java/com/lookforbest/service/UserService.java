package com.lookforbest.service;

import com.lookforbest.dto.request.LoginRequest;
import com.lookforbest.dto.request.RefreshTokenRequest;
import com.lookforbest.dto.request.RegisterRequest;
import com.lookforbest.dto.response.AuthTokenResponse;
import com.lookforbest.entity.User;

public interface UserService {
    AuthTokenResponse register(RegisterRequest request);
    AuthTokenResponse login(LoginRequest request);
    AuthTokenResponse refreshToken(RefreshTokenRequest request);
    User getCurrentUser(String email);
}
