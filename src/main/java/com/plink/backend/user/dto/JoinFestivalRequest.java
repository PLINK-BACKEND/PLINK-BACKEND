package com.plink.backend.user.dto;

import lombok.Getter;
import lombok.Setter;

// 로그인 후 축제에 참여할 때 or 또다른 축제에 참여할 때
@Getter
@Setter
public class JoinFestivalRequest {
    private String slug;      // 축제 식별자
    private String nickname;  // 축제 내 닉네임
}
