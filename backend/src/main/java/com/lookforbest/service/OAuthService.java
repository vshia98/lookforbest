package com.lookforbest.service;

import com.lookforbest.dto.request.OAuthLoginRequest;
import com.lookforbest.dto.response.AuthTokenResponse;

public interface OAuthService {
    AuthTokenResponse login(OAuthLoginRequest request);
}
