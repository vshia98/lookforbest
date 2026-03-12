package com.lookforbest.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lookforbest.dto.request.OAuthLoginRequest;
import com.lookforbest.dto.response.AuthTokenResponse;
import com.lookforbest.dto.response.UserDTO;
import com.lookforbest.entity.User;
import com.lookforbest.entity.UserOAuthBinding;
import com.lookforbest.exception.BusinessException;
import com.lookforbest.repository.UserOAuthBindingRepository;
import com.lookforbest.repository.UserRepository;
import com.lookforbest.security.JwtUtil;
import com.lookforbest.service.OAuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class OAuthServiceImpl implements OAuthService {

    private final UserRepository userRepository;
    private final UserOAuthBindingRepository oauthBindingRepository;
    private final JwtUtil jwtUtil;
    private final ObjectMapper objectMapper;
    private final RestTemplate restTemplate;

    @Value("${app.oauth.wechat.app-id:}")
    private String wechatAppId;

    @Value("${app.oauth.wechat.app-secret:}")
    private String wechatAppSecret;

    @Value("${app.oauth.wechat-web.app-id:}")
    private String wechatWebAppId;

    @Value("${app.oauth.wechat-web.app-secret:}")
    private String wechatWebAppSecret;

    private static final String GOOGLE_TOKENINFO_URL = "https://oauth2.googleapis.com/tokeninfo";
    private static final String WECHAT_MINIAPP_SESSION_URL =
            "https://api.weixin.qq.com/sns/jscode2session";
    private static final String WECHAT_WEB_ACCESS_TOKEN_URL =
            "https://api.weixin.qq.com/sns/oauth2/access_token";
    private static final String WECHAT_WEB_USERINFO_URL =
            "https://api.weixin.qq.com/sns/userinfo";

    @Override
    @Transactional
    public AuthTokenResponse login(OAuthLoginRequest request) {
        return switch (request.getProvider()) {
            case "google"           -> loginWithGoogle(request.getCode());
            case "wechat_miniapp"   -> loginWithWechatMiniapp(request.getCode());
            case "wechat_web"       -> loginWithWechatWeb(request.getCode());
            default -> throw new BusinessException(400, "不支持的登录方式: " + request.getProvider());
        };
    }

    // ─── Google ────────────────────────────────────────────────────────────────

    private AuthTokenResponse loginWithGoogle(String idToken) {
        JsonNode info = verifyGoogleIdToken(idToken);
        String googleUserId = info.path("sub").asText();
        String email        = info.path("email").asText();
        String name         = info.path("name").asText(null);
        String picture      = info.path("picture").asText(null);

        return findOrCreateUser("google", googleUserId, email, name, picture, null, null);
    }

    private JsonNode verifyGoogleIdToken(String idToken) {
        try {
            String url = UriComponentsBuilder.fromHttpUrl(GOOGLE_TOKENINFO_URL)
                    .queryParam("id_token", idToken)
                    .toUriString();
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            if (!response.getStatusCode().is2xxSuccessful()) {
                throw new BusinessException(401, "Google token 验证失败");
            }
            JsonNode node = objectMapper.readTree(response.getBody());
            if (node.has("error_description")) {
                throw new BusinessException(401, "Google token 无效: " + node.path("error_description").asText());
            }
            return node;
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("Google token 验证异常", e);
            throw new BusinessException(401, "Google 登录验证失败");
        }
    }

    // ─── WeChat Miniapp ────────────────────────────────────────────────────────

    private AuthTokenResponse loginWithWechatMiniapp(String code) {
        try {
            String url = UriComponentsBuilder.fromHttpUrl(WECHAT_MINIAPP_SESSION_URL)
                    .queryParam("appid",      wechatAppId)
                    .queryParam("secret",     wechatAppSecret)
                    .queryParam("js_code",    code)
                    .queryParam("grant_type", "authorization_code")
                    .toUriString();
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            JsonNode node = objectMapper.readTree(response.getBody());
            if (node.has("errcode") && node.path("errcode").asInt() != 0) {
                throw new BusinessException(401, "微信登录失败: " + node.path("errmsg").asText());
            }
            String openid  = node.path("openid").asText();
            String unionid = node.path("unionid").asText(null);

            // 微信小程序无法直接获取邮箱，使用 openid 作为虚拟邮箱
            String virtualEmail = "wx_mp_" + openid + "@wechat.internal";
            return findOrCreateUser("wechat_miniapp", openid, virtualEmail, null, null, unionid, null);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("微信小程序登录异常", e);
            throw new BusinessException(401, "微信登录失败");
        }
    }

    // ─── WeChat Web ────────────────────────────────────────────────────────────

    private AuthTokenResponse loginWithWechatWeb(String code) {
        try {
            // 1. code 换 access_token
            String tokenUrl = UriComponentsBuilder.fromHttpUrl(WECHAT_WEB_ACCESS_TOKEN_URL)
                    .queryParam("appid",      wechatWebAppId)
                    .queryParam("secret",     wechatWebAppSecret)
                    .queryParam("code",       code)
                    .queryParam("grant_type", "authorization_code")
                    .toUriString();
            ResponseEntity<String> tokenResp = restTemplate.getForEntity(tokenUrl, String.class);
            JsonNode tokenNode = objectMapper.readTree(tokenResp.getBody());
            if (tokenNode.has("errcode")) {
                throw new BusinessException(401, "微信授权码无效: " + tokenNode.path("errmsg").asText());
            }
            String accessToken = tokenNode.path("access_token").asText();
            String openid      = tokenNode.path("openid").asText();
            String unionid     = tokenNode.path("unionid").asText(null);

            // 2. 获取用户信息
            String userInfoUrl = UriComponentsBuilder.fromHttpUrl(WECHAT_WEB_USERINFO_URL)
                    .queryParam("access_token", accessToken)
                    .queryParam("openid", openid)
                    .queryParam("lang", "zh_CN")
                    .toUriString();
            ResponseEntity<String> userResp = restTemplate.getForEntity(userInfoUrl, String.class);
            JsonNode userNode = objectMapper.readTree(userResp.getBody());
            String nickname   = userNode.path("nickname").asText(null);
            String headimgurl = userNode.path("headimgurl").asText(null);

            String virtualEmail = "wx_web_" + openid + "@wechat.internal";
            return findOrCreateUser("wechat_web", openid, virtualEmail, nickname, headimgurl,
                    unionid, accessToken);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("微信网页登录异常", e);
            throw new BusinessException(401, "微信登录失败");
        }
    }

    // ─── 核心：查找或创建用户 ────────────────────────────────────────────────────

    private AuthTokenResponse findOrCreateUser(
            String provider, String providerUserId,
            String email, String displayName, String avatarUrl,
            String unionid, String providerAccessToken) {

        Optional<UserOAuthBinding> bindingOpt =
                oauthBindingRepository.findByProviderAndProviderUserId(provider, providerUserId);

        User user;
        if (bindingOpt.isPresent()) {
            // 已绑定 → 直接取关联用户
            user = bindingOpt.get().getUser();
            // 更新 access_token
            if (providerAccessToken != null) {
                bindingOpt.get().setAccessToken(providerAccessToken);
                oauthBindingRepository.save(bindingOpt.get());
            }
        } else {
            // 未绑定：尝试按邮箱合并已有账号，否则新建
            user = userRepository.findByEmail(email).orElseGet(() -> {
                User newUser = new User();
                newUser.setEmail(email);
                newUser.setPasswordHash(null);   // OAuth 用户无密码
                newUser.setDisplayName(displayName);
                newUser.setAvatarUrl(avatarUrl);
                newUser.setUsername(generateUsername(displayName, provider, providerUserId));
                newUser.setRole(User.Role.user);
                newUser.setIsActive(true);
                newUser.setEmailVerified(provider.equals("google")); // Google 邮箱已验证
                return userRepository.save(newUser);
            });

            // 创建绑定关系
            UserOAuthBinding binding = new UserOAuthBinding();
            binding.setUser(user);
            binding.setProvider(provider);
            binding.setProviderUserId(providerUserId);
            binding.setAccessToken(providerAccessToken);
            if (unionid != null) {
                binding.setExtraData("{\"unionid\":\"" + unionid + "\"}");
            }
            oauthBindingRepository.save(binding);
        }

        if (!user.getIsActive()) {
            throw new BusinessException(403, "账号已被禁用");
        }

        String accessToken  = jwtUtil.generateAccessToken(user.getEmail(), user.getRole().name());
        String refreshToken = jwtUtil.generateRefreshToken(user.getEmail());
        return AuthTokenResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .expiresIn(jwtUtil.getAccessTokenExpirySeconds())
                .user(UserDTO.from(user))
                .build();
    }

    private String generateUsername(String displayName, String provider, String providerUserId) {
        if (displayName != null && !displayName.isBlank()) {
            // 去除非字母数字字符，取前16位
            String base = displayName.replaceAll("[^a-zA-Z0-9_]", "");
            if (!base.isBlank()) {
                String candidate = base.substring(0, Math.min(base.length(), 16));
                if (!userRepository.existsByUsername(candidate)) return candidate;
                candidate = candidate + "_" + UUID.randomUUID().toString().substring(0, 4);
                if (!userRepository.existsByUsername(candidate)) return candidate;
            }
        }
        String candidate = provider.replace("_", "") + "_" + providerUserId.substring(0, Math.min(8, providerUserId.length()));
        if (!userRepository.existsByUsername(candidate)) return candidate;
        return candidate + "_" + UUID.randomUUID().toString().substring(0, 4);
    }
}
