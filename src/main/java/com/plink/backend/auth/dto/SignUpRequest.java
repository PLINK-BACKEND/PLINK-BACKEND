package com.plink.backend.auth.dto;

import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Getter @Setter
public class SignUpRequest {
    private String email;
    private String nickname;
    private String password;
    private MultipartFile profileImage;
    private String slug; // 행사 식별자
}
