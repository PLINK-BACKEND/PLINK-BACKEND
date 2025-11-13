package com.plink.backend.feed.service.post;

import com.plink.backend.commonS3.S3Service;
import com.plink.backend.commonS3.S3UploadResult;
import com.plink.backend.feed.entity.post.Image;
import com.plink.backend.feed.entity.post.Post;
import com.plink.backend.feed.repository.post.ImageRepository;
import com.plink.backend.feed.repository.post.PostRepository;
import com.plink.backend.global.exception.CustomException;
import com.plink.backend.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ImageService {

    private final ImageRepository imageRepository;
    private final PostRepository postRepository;
    private final S3Service s3Service;

    // 이미지 업로드
    public List<Image> saveImages(User author,Long postId, List<MultipartFile> files) throws IOException {
        if (files == null || files.isEmpty()) return new ArrayList<>();

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "게시글을 찾을 수 없습니다."));

        if (!post.getAuthor().getUser().getUserId().equals(author.getUserId())) {
            throw new CustomException(HttpStatus.FORBIDDEN, "게시글 수정 권한이 없습니다.");
        }

        if (post.getImages().size() + files.size() > 3) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "이미지는 최대 3장까지 업로드할 수 있습니다.");
        }

        List<Image> savedImages = new ArrayList<>();

        for (MultipartFile file : files) {
            S3UploadResult uploadResult = s3Service.upload(file, "posts");
            Image image = Image.builder()
                    .post(post)
                    .s3key(uploadResult.getKey())
                    .originalName(uploadResult.getOriginalFilename())
                    .imageUrl(uploadResult.getUrl())
                    .build();
            imageRepository.save(image);
            post.getImages().add(image);
            savedImages.add(image);
        }

        imageRepository.flush();       // DB에 즉시 반영

        return savedImages;
    }

    //게시글 이미지 삭제
    public Post deleteImageAndReturnPost(User author, Long imageId) {
        Image image = imageRepository.findById(imageId)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "이미지를 찾을 수 없습니다."));

        Post post = image.getPost();

        if (!post.getAuthor().getUser().getUserId().equals(author.getUserId())) {
            throw new CustomException(HttpStatus.FORBIDDEN, "게시글 수정 권한이 없습니다.");
        }


        // S3 삭제
        s3Service.delete(image.getS3key());

        // DB 삭제
        imageRepository.delete(image);

        // 엔티티 갱신
        post.getImages().removeIf(img -> img.getId().equals(imageId));

        return post;
    }
}
