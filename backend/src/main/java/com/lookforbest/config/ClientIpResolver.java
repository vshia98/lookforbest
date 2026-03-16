package com.lookforbest.config;

import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 解析真实客户端 IP。
 * 仅当请求来自受信任代理时才读取 X-Forwarded-For / X-Real-IP，防止客户端伪造。
 * 支持精确 IP（"127.0.0.1"）和前缀匹配（"172." 匹配所有 172.x 地址）。
 */
@Component
public class ClientIpResolver {

    @Value("${app.rate-limit.trusted-proxies:127.0.0.1,::1}")
    private String trustedProxiesConfig;

    private Set<String> trustedProxies;

    @PostConstruct
    public void init() {
        trustedProxies = Arrays.stream(trustedProxiesConfig.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toSet());
    }

    public String resolve(HttpServletRequest request) {
        String remoteAddr = request.getRemoteAddr();
        if (isTrustedProxy(remoteAddr)) {
            String xff = request.getHeader("X-Forwarded-For");
            if (xff != null && !xff.isBlank() && !"unknown".equalsIgnoreCase(xff)) {
                return xff.split(",")[0].trim();
            }
            String xri = request.getHeader("X-Real-IP");
            if (xri != null && !xri.isBlank() && !"unknown".equalsIgnoreCase(xri)) {
                return xri;
            }
        }
        return remoteAddr;
    }

    private boolean isTrustedProxy(String addr) {
        for (String proxy : trustedProxies) {
            if (proxy.endsWith(".") ? addr.startsWith(proxy) : proxy.equals(addr)) {
                return true;
            }
        }
        return false;
    }
}
