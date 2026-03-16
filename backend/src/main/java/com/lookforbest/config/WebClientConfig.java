package com.lookforbest.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class WebClientConfig {

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    // 不要手动创建 ObjectMapper Bean —— Spring Boot 自动配置的 ObjectMapper
    // 已包含 JavaTimeModule（支持 LocalDateTime/LocalDate 序列化）、
    // Jdk8Module 等必要模块。手动覆盖会导致这些模块丢失。
}
