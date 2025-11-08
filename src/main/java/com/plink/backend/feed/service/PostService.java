package com.plink.backend.feed.service;

import com.plink.backend.commonService.S3UploadResult;
import com.plink.backend.feed.dto.post.PostCreateRequest;
import com.plink.backend.feed.dto.post.PostResponse;
import com.plink.backend.feed.dto.post.PostDetailResponse;
import com.plink.backend.feed.entity.*;
import com.plink.backend.global.exception.CustomException;
import com.plink.backend.main.repository.FestivalRepository;
import com.plink.backend.main.entity.Festival;
import com.plink.backend.commonService.S3Service;
import com.plink.backend.feed.repository.ImageRepository;
import com.plink.backend.feed.repository.PostRepository;
import com.plink.backend.feed.repository.TagRepository;
import com.plink.backend.feed.dto.post.PostUpdateRequest;
import com.plink.backend.user.entity.User;
import com.plink.backend.user.entity.UserFestival;
import com.plink.backend.user.repository.UserFestivalRepository;
import com.plink.backend.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
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
    private final UserFestivalRepository userFestivalRepository;
    private final PollService pollService;
    private final UserService userService;

    @Transactional
    // 게시글 작성하기

    public Post createPost(User author, PostCreateRequest request, String slug) throws IOException {

        // 1️⃣ 행사 검증
        Festival festival = festivalRepository.findBySlug(slug)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 축제입니다."));

        // 2️⃣ 태그 검증
        Tag tag = tagRepository.findById(request.getTagId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 태그입니다."));

        // 3️⃣ 작성자-축제 매핑 검증
        UserFestival userFestival = userFestivalRepository
                .findByUser_UserIdAndFestivalSlug(author.getUserId(), slug)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "해당 축제에서 유저를 찾을 수 없습니다."));

        // 4️⃣ 기본 내용 검증
        if (request.getPostType() == PostType.NORMAL &&
                (request.getContent() == null || request.getContent().isBlank())) {
            throw new IllegalArgumentException("게시글의 내용은 비워둘 수 없습니다.");
        }

        // 5️⃣ 이미지 개수 검증
        if (request.getImages() != null && request.getImages().size() > 3) {
            throw new IllegalArgumentException("이미지는 최대 3장까지 업로드 가능합니다.");
        }

        // 6️⃣ Post 생성 및 1차 저장
        Post post = Post.builder()
                .author(userFestival)
                .title(request.getTitle())
                .content(request.getContent())
                .tag(tag)
                .festival(festival)
                .postType(request.getPostType())
                .build();

        postRepository.save(post);

        // 7️⃣ Poll 생성 (POLL 타입일 경우만)
        if (request.getPostType() == PostType.POLL) {
            Poll poll = pollService.createPoll(author, request.getPoll());
            poll.setPost(post);
            post.setPoll(poll);
            postRepository.save(post); // 🔥 양방향 연관관계 최종 반영
        }

        // 8️⃣ 이미지 업로드 처리
        if (request.getImages() != null && !request.getImages().isEmpty()) {
            for (MultipartFile file : request.getImages()) {
                S3UploadResult uploadResult = s3Service.upload(file, "posts");

                Image image = Image.builder()
                        .post(post)
                        .s3key(uploadResult.getKey())
                        .originalName(uploadResult.getOriginalFilename())
                        .image_url(uploadResult.getUrl())
                        .build();

                post.getImages().add(image);
            }
        }

        // 9️⃣ 최종 저장
        return postRepository.save(post);
    }

    // 게시글 수정
    @Transactional
    public Post updatePost(User author,PostUpdateRequest request,Long postId){
        Post post = postRepository.findById(postId)
                .orElseThrow(()->new IllegalArgumentException("게시글을 찾을 수 없습니다."));

        // 작성자만 수정 권한을 가짐
        if (!post.getAuthor().getUser().getUserId().equals(author.getUserId())) {
            throw new IllegalArgumentException("게시글 삭제 권한이 없습니다.");
        }

        // 제목 수정 (값이 들어온 경우에만)
        if (request.getTitle() != null && !request.getTitle().isBlank()) {
            post.updateTitle(request.getTitle());
        }

        // 내용 수정
        if (request.getContent() != null && !request.getContent().isBlank()) {
            post.updateContent(request.getContent());
        }

        // 태그 수정
        if (request.getTagId() != null ) {
            Tag tag = tagRepository.findById(request.getTagId())
                    .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 태그입니다."));
            post.updateTag(tag);
        }

        return post;
    }

    // 게시글 삭제
    @Transactional
    public void deletePost(User author,Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));

        // 작성자만 삭제 권한을 가짐
        if (!post.getAuthor().getUser().getUserId().equals(author.getUserId())) {
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
    public PostDetailResponse getPostDetail(Long postId) {
        Post post = postRepository.findWithAllById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));

        return PostDetailResponse.from(post);
    }

    // 게시글 모두 조회 (최신 글이 가장 밑으로)
    @Transactional(readOnly = true)
    public Page<PostResponse> getPostList(Pageable pageable) {
        return postRepository.findAllByOrderByCreatedAtAsc(pageable)
                .map(PostResponse::from);  // Page<Post> → Page<PostResponseDto> 변환
    }

    @Transactional
    // 게시판 별로 게시글 조회
    public Page<PostResponse> getPostListByTag(Pageable pageable, Long tagId) {

        Tag tag = tagRepository.findById(tagId)
                .orElseThrow(()-> new IllegalArgumentException("존재하지 않는 게시판입니다."));

        // 2. 태그별 게시글 조회
        Page<Post> posts = postRepository.findAllByTagOrderByCreatedAtAsc(tag, pageable);

        // 3. 엔티티 → DTO 변환
        return posts.map(PostResponse::from);
    }


}