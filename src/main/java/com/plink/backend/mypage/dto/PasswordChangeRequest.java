package com.plink.backend.mypage.dto;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PasswordChangeRequest {

    public String currentPassword;
    public String newPassword;
}
