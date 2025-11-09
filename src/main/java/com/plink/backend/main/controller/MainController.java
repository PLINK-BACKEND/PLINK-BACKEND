package com.plink.backend.main.controller;

import com.plink.backend.feed.dto.post.PostResponse;
import com.plink.backend.main.service.MainService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/{slug}/main")
public class MainController {

    private final MainService mainService;

    // 인기글 top3 조회
    @GetMapping("/popular")
    public ResponseEntity<List<PostResponse>> getPopularPosts() {
        List<PostResponse> response = mainService.getPopularPosts();
        return ResponseEntity.ok(response);
    }
}
