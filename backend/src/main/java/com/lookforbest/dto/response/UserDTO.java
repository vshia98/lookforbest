package com.lookforbest.dto.response;

import com.lookforbest.entity.User;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserDTO {
    private Long id;
    private String email;
    private String username;
    private String displayName;
    private String avatarUrl;
    private String role;
    private String company;
    private String jobTitle;
    private String country;

    public static UserDTO from(User user) {
        return UserDTO.builder()
                .id(user.getId())
                .email(user.getEmail())
                .username(user.getUsername())
                .displayName(user.getDisplayName())
                .avatarUrl(user.getAvatarUrl())
                .role(user.getRole().name())
                .company(user.getCompany())
                .jobTitle(user.getJobTitle())
                .country(user.getCountry())
                .build();
    }
}
