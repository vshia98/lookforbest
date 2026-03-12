package com.lookforbest.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OAuthLoginRequest {

    /**
     * 登录方式：google / wechat_web / wechat_miniapp
     */
    @NotBlank
    private String provider;

    /**
     * Google：idToken（前端通过 Google Sign-In 获取）
     * WeChat Web：code（OAuth 授权码）
     * WeChat Miniapp：code（wx.login() 返回的 code）
     */
    @NotBlank
    private String code;
}
