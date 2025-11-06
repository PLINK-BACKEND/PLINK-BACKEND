package com.plink.backend.auth.dto;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class SignUpRequest {
    private String email;
    private String nickname;
    private String password;
    private String profileImageUrl;
}
