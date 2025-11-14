package com.plink.backend.fourcuts.controller;

import com.plink.backend.fourcuts.dto.FourCutsResponse;
import com.plink.backend.fourcuts.service.FourCutsService;
import com.plink.backend.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/fourcuts")
@RequiredArgsConstructor
public class FourCutsController {

    private final FourCutsService fourCutsService;

    // 네컷사진 업로드 및 QR 생성
    @PostMapping("/upload")
    public FourCutsResponse uploadFourCut(@RequestParam("file") MultipartFile file) {
        return fourCutsService.uploadFourCut(file);
    }

    // 시크릿 프레임 해금 여부 조회
    @GetMapping("/{slug}/secret")
    public boolean checkSecret(
            @PathVariable String slug,
            @AuthenticationPrincipal User user
    ) {
        return fourCutsService.isSecretFrameUnlocked(slug, user);
    }
}
