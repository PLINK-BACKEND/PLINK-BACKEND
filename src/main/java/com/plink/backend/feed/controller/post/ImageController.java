package com.plink.backend.feed.controller.post;

import com.plink.backend.feed.dto.post.PostDetailResponse;
import com.plink.backend.feed.entity.post.Post;
import com.plink.backend.feed.repository.post.PostRepository;
import com.plink.backend.feed.service.post.ImageService;
import com.plink.backend.global.exception.CustomException;
import com.plink.backend.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("{slug}/images")
@RequiredArgsConstructor
public class ImageController {

    private final ImageService imageService;
    private final PostRepository postRepository;

    // 이미지 추가
    @PostMapping("/{postId}")
    public ResponseEntity<PostDetailResponse> uploadImages(
            @AuthenticationPrincipal User user,
            @PathVariable Long postId,
            @RequestParam("images") List<MultipartFile> files
    ) throws IOException  {
        imageService.saveImages(user,postId, files);

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "게시글을 찾을 수 없습니다."));

        return ResponseEntity.ok(PostDetailResponse.from(post));
    }

    // 이미지 삭제
    @DeleteMapping("/{imageId}")
    public ResponseEntity<PostDetailResponse> deleteImage( @AuthenticationPrincipal User user,@PathVariable Long imageId) {
        Post post = imageService.deleteImageAndReturnPost(user,imageId);
        return ResponseEntity.ok(PostDetailResponse.from(post));
    }
}
