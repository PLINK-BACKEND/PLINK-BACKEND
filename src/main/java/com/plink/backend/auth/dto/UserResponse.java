package com.plink.backend.auth.dto;

import com.plink.backend.user.entity.User;
import lombok.Getter;

@Getter
public class UserResponse {
    private final String email;
    private final String nickname;
    private final String profileImageUrl;
    private final String role;

    public UserResponse(User user) {
        this.email = user.getEmail();
        this.nickname = user.getNickname();
        this.profileImageUrl = user.getProfileImageUrl();
        this.role = user.getRole().name();
    }
}
