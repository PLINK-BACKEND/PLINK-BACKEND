package com.plink.backend.fourcuts.controller;

import com.plink.backend.fourcuts.dto.FourCutsResponse;
import com.plink.backend.fourcuts.service.FourCutsService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/fourcuts")
@RequiredArgsConstructor
public class FourCutsController {

    private final FourCutsService fourCutsService;

    /**
     * ✅ 네컷사진 업로드 및 QR 생성
     */
    @PostMapping("/upload")
    public FourCutsResponse uploadFourCut(@RequestParam("file") MultipartFile file) {
        return fourCutsService.uploadFourCut(file);
    }
}
