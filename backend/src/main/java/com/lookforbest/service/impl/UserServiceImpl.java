package com.lookforbest.service.impl;

import com.lookforbest.dto.request.LoginRequest;
import com.lookforbest.dto.request.RefreshTokenRequest;
import com.lookforbest.dto.request.RegisterRequest;
import com.lookforbest.dto.response.AuthTokenResponse;
import com.lookforbest.dto.response.UserDTO;
import com.lookforbest.entity.User;
import com.lookforbest.exception.BusinessException;
import com.lookforbest.exception.ResourceNotFoundException;
import com.lookforbest.repository.UserRepository;
import com.lookforbest.security.JwtUtil;
import com.lookforbest.service.UserService;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Override
    @Transactional
    public AuthTokenResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException(409, "邮箱已被注册");
        }
        if (request.getUsername() != null && userRepository.existsByUsername(request.getUsername())) {
            throw new BusinessException(409, "用户名已被使用");
        }

        User user = new User();
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setUsername(request.getUsername());
        user.setDisplayName(request.getDisplayName());
        user.setRole(User.Role.user);
        userRepository.save(user);

        return buildTokenResponse(user);
    }

    @Override
    @Transactional
    public AuthTokenResponse login(LoginRequest request) {
        String identity = request.getUsernameOrEmail();
        // 包含 @ 则按邮箱查，否则按用户名查
        User user = (identity.contains("@")
                ? userRepository.findByEmail(identity)
                : userRepository.findByUsername(identity))
                .orElseThrow(() -> new BusinessException(401, "账号或密码错误"));
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new BusinessException(401, "账号或密码错误");
        }
        if (!user.getIsActive()) {
            throw new BusinessException(403, "账号已被禁用");
        }
        return buildTokenResponse(user);
    }

    @Override
    @Transactional(readOnly = true)
    public AuthTokenResponse refreshToken(RefreshTokenRequest request) {
        try {
            String email = jwtUtil.getEmail(request.getRefreshToken());
            if (!jwtUtil.isValid(request.getRefreshToken())) {
                throw new BusinessException(401, "refresh token 无效或已过期");
            }
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new BusinessException(401, "用户不存在"));
            return buildTokenResponse(user);
        } catch (JwtException e) {
            throw new BusinessException(401, "refresh token 无效");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public User getCurrentUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email));
    }

    private AuthTokenResponse buildTokenResponse(User user) {
        String accessToken = jwtUtil.generateAccessToken(user.getEmail(), user.getRole().name());
        String refreshToken = jwtUtil.generateRefreshToken(user.getEmail());
        return AuthTokenResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .expiresIn(jwtUtil.getAccessTokenExpirySeconds())
                .user(UserDTO.from(user))
                .build();
    }
}
