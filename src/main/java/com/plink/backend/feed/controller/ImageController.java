package com.plink.backend.feed.controller;

import com.plink.backend.commonService.S3Service;
import com.plink.backend.feed.dto.post.PostDetailResponse;
import com.plink.backend.feed.entity.Image;
import com.plink.backend.feed.entity.Post;
import com.plink.backend.feed.repository.ImageRepository;
import com.plink.backend.feed.repository.PostRepository;
import com.plink.backend.feed.service.ImageService;
import com.plink.backend.feed.service.PostService;
import com.plink.backend.global.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("{slug}/images")
@RequiredArgsConstructor
public class ImageController {

    private final ImageService imageService;
    private final PostRepository postRepository;

    /** 게시글 이미지 업로드 */
    @PostMapping("/{postId}")
    public ResponseEntity<PostDetailResponse> uploadImages(
            @PathVariable Long postId,
            @RequestParam("images") List<MultipartFile> files
    ) throws IOException  {
        imageService.saveImages(postId, files);

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "게시글을 찾을 수 없습니다."));

        return ResponseEntity.ok(PostDetailResponse.from(post));
    }

    /** ✅ 게시글 이미지 삭제 */
    @DeleteMapping("/{imageId}")
    public ResponseEntity<PostDetailResponse> deleteImage(@PathVariable Long imageId) {
        Post post = imageService.deleteImageAndReturnPost(imageId);
        return ResponseEntity.ok(PostDetailResponse.from(post));
    }
}
