package com.plink.backend.auth.dto;

import com.plink.backend.user.entity.User;
import com.plink.backend.user.entity.UserFestival;
import lombok.Getter;

@Getter
public class UserResponse {
    private final String email;
    private final String profileImageUrl;
    private final String role;
    private final String nickname;
    private String slug;

    public UserResponse(User user, UserFestival festival) {
        this.email = user.getEmail();
        this.profileImageUrl = user.getProfileImageUrl();
        this.role = user.getRole().name();
        this.nickname = (festival != null) ? festival.getNickname() : null;
        this.slug = (festival != null) ? festival.getFestivalSlug() : null;
    }
}
