package com.plink.backend.feed.service;

import com.plink.backend.feed.dto.PostRequest;
import com.plink.backend.feed.dto.PostResponse;
import com.plink.backend.main.repository.FestivalRepository;
import com.plink.backend.main.entity.Festival;
import com.plink.backend.service.S3Service;
import com.plink.backend.feed.entity.Image;
import com.plink.backend.feed.entity.Post;
import com.plink.backend.feed.entity.Tag;
import com.plink.backend.feed.repository.ImageRepository;
import com.plink.backend.feed.repository.PostRepository;
import com.plink.backend.feed.repository.TagRepository;
import com.plink.backend.feed.dto.PostUpdateRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;


@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostService {
    private final PostRepository postRepository;
    private final TagRepository tagRepository;
    private final ImageRepository imageRepository;
    private final S3Service s3Service;
    private final FestivalRepository festivalRepository;

    @Transactional
    // 게시글 작성하기
    public Post createpost(User author, PostRequest requestDto) throws IOException {

        // 태그 검증
        Tag tag = tagRepository.findByName(requestDto.getTagName())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 태그입니다."));

        // 행사 검증
        Festival festival = festivalRepository.findById(requestDto.getFestivalId())
                .orElseThrow(()-> new IllegalArgumentException("존재하지 않는 행사입니다."));

        // 이미지 개수 검증
        if (requestDto.getImages() != null && requestDto.getImages().size() > 3) {
            throw new IllegalArgumentException("이미지는 최대 3장까지 업로드 가능합니다.");
        }


        // 게시글 생성
        Post post = Post.builder()
                .author(author)
                .title(requestDto.getTitle())
                .content(requestDto.getContent())
                .tag(tag)
                .festival(festival)
                .build();
        postRepository.save(post);

        // 이미지 업로드
        if (requestDto.getImages() != null && !requestDto.getImages().isEmpty()) {
            for (MultipartFile file : requestDto.getImages()) {
                String key = s3Service.upload(file,"posts");
                Image image = Image.builder()
                        .post(post)
                        .s3key(key)
                        .originalName(file.getOriginalFilename())
                        .build();
                imageRepository.save(image);
            }
        }
        return post;
    }

    // 게시글 수정
    @Transactional
    public Post updatePost(Long postId, User currentAuthor,PostUpdateRequest requestDto){
            Post post = postRepository.findById(postId)
                    .orElseThrow(()->new IllegalArgumentException("게시글을 찾을 수 없습니다."));

            // 작성자만 수정 권한을 가짐
            if (!post.getAuthor().equals(currentAuthor)){
                throw new IllegalArgumentException("게시글 수정 권한이 없습니다.");
            }

            // 제목 수정 (값이 들어온 경우에만)
            if (requestDto.getTitle() != null && !requestDto.getTitle().isBlank()) {
                post.updateTitle(requestDto.getTitle());
            }

            // 내용 수정
            if (requestDto.getContent() != null && !requestDto.getContent().isBlank()) {
                post.updateContent(requestDto.getContent());
            }

            // 태그 수정
            if (requestDto.getTagName() != null && !requestDto.getTagName().isBlank()) {
                Tag tag = tagRepository.findByName(requestDto.getTagName())
                        .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 태그입니다."));
                post.changeTag(tag);
            }

            return post;
        }



    // 게시글 삭제
    @Transactional
    public void deletePost(Long postId, User currentAuthor) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));

        // 작성자만 삭제 권한을 가짐
        if (!post.getAuthor().equals(currentAuthor)){
            throw new IllegalArgumentException("게시글 삭제 권한이 없습니다.");
        }

        // 이미지 삭제
        if (post.getImages() != null) {
            for (Image image : post.getImages()) {
                try {
                    s3Service.delete(image.getS3key());
                } catch (Exception e) {
                    System.out.println("S3 이미지 삭제 실패: {}"+ image.getS3key());
                }
            }

        }
        postRepository.delete(post);
    }


    // 게시글 상세 조회 (댓글/이미지까지 모두 포함)
    @Transactional(readOnly = true)
    public PostResponse getPostDetail(Long postId) {
        Post post = postRepository.findWithAllById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));

        return PostResponse.from(post);
    }

    // 게시글 모두 조회 (최신 글이 가장 밑으로)
    @Transactional(readOnly = true)
    public Page<PostResponse> getPostList(Pageable pageable) {
        return postRepository.findAllByOrderByCreatedAtAsc(pageable)
                .map(PostResponse::from);  // Page<Post> → Page<PostResponseDto> 변환
    }

}
