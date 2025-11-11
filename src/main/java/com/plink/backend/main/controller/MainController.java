package com.plink.backend.main.controller;

import com.plink.backend.feed.dto.post.PostResponse;
import com.plink.backend.main.dto.MainResponse;
import com.plink.backend.main.service.MainService;
import com.plink.backend.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/{slug}/main")
public class MainController {

    private final MainService mainService;

    // 인기글 조회
    @GetMapping("/popular")
    public ResponseEntity<MainResponse> getPopularPosts(
            @PathVariable String slug,
            @AuthenticationPrincipal User user
    ) {
        MainResponse result = mainService.getPopularPosts(user, slug);
        return ResponseEntity.ok(result);
    }
}
