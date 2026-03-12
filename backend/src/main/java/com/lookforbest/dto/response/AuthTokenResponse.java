package com.lookforbest.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AuthTokenResponse {
    private String accessToken;
    private String refreshToken;
    private long expiresIn;
    private UserDTO user;
}
