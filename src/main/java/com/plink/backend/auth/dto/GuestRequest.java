package com.plink.backend.auth.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GuestRequest {
    private String nickname;
    private String profileImageUrl;
    private String slug; // 행사 식별자
}

