package com.plink.backend.feed.service;

import com.plink.backend.commonService.S3UploadResult;
import com.plink.backend.feed.dto.poll.PollResponse;
import com.plink.backend.feed.dto.post.PostCreateRequest;
import com.plink.backend.feed.dto.post.PostResponse;
import com.plink.backend.feed.dto.post.PostDetailResponse;
import com.plink.backend.feed.entity.*;
import com.plink.backend.feed.repository.HiddenContentRepository;
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
import org.springframework.data.domain.Slice;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


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
    private final HiddenContentRepository hiddenContentRepository;
    private final PollService pollService;
    private final UserService userService;

    @Transactional
    // 게시글 작성하기

    public Post createPost(User author, PostCreateRequest request, String slug) throws IOException {

        // 행사 검증
        Festival festival = festivalRepository.findBySlug(slug)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 축제입니다."));

        // 태그 검증
        Tag tag = tagRepository.findById(request.getTagId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 태그입니다."));

        // 작성자-축제 매핑 검증
        UserFestival userFestival = userFestivalRepository
                .findByUser_UserIdAndFestivalSlug(author.getUserId(), slug)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "해당 축제에서 유저를 찾을 수 없습니다."));

        // 기본 내용 검증
        if (request.getPostType() == PostType.NORMAL &&
                (request.getContent() == null || request.getContent().isBlank())) {
            throw new IllegalArgumentException("게시글의 내용은 비워둘 수 없습니다.");
        }

        // 이미지 개수 검증
        if (request.getImages() != null && request.getImages().size() > 3) {
            throw new IllegalArgumentException("이미지는 최대 3장까지 업로드 가능합니다.");
        }

        // Post 생성 및 1차 저장
        Post post = Post.builder()
                .author(userFestival)
                .title(request.getTitle())
                .content(request.getContent())
                .tag(tag)
                .festival(festival)
                .postType(request.getPostType())
                .build();

        postRepository.save(post);

        //  Poll 생성 (POLL 타입일 경우만)
        if (request.getPostType() == PostType.POLL) {
            Poll poll = pollService.createPoll(author, request.getPoll());
            poll.setPost(post);
            post.setPoll(poll);
            postRepository.save(post);
        }

        // 이미지 업로드 처리
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

        // 최종 저장
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


    // 게시글 상세 조회 (댓글까지 모두 포함)
    @Transactional(readOnly = true)
    public PostDetailResponse getPostDetail(User user, String slug, Long postId) {
        Post post = postRepository.findWithAllById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));

        // 로그인한 사용자가 있을 경우, 숨긴 댓글 ID 목록 조회
        List<Long> hiddenCommentIds = new ArrayList<>();

        if (user != null) {
            UserFestival userFestival = userFestivalRepository
                    .findByUser_UserIdAndFestivalSlug(user.getUserId(), slug)
                    .orElse(null);

            if (userFestival != null) {
                hiddenCommentIds = hiddenContentRepository
                        .findTargetIdsByUserFestivalAndTargetType(userFestival, ReportTargetType.COMMENT);
            }
        }

        PollResponse pollResponse = null;
        if (post.getPostType() == PostType.POLL && post.getPoll() != null) {
            pollResponse = pollService.getPollResponse(post.getPoll(), user);
        }

        return PostDetailResponse.from(post, pollResponse, hiddenCommentIds);

    }


    @Transactional(readOnly = true)
    public Slice<PostResponse> getPostListByTag(User user, String slug, Pageable pageable, Long tagId) {

        List<Long> hiddenPostIds = new ArrayList<>();
        UserFestival userFestival = null;

        // 로그인한 사용자라면 UserFestival 조회
        if (user != null) {
            userFestival = userFestivalRepository
                    .findByUser_UserIdAndFestivalSlug(user.getUserId(), slug)
                    .orElse(null);

            if (userFestival != null) {
                hiddenPostIds = hiddenContentRepository
                        .findTargetIdsByUserFestivalAndTargetType(userFestival, ReportTargetType.POST);
            }
        }

        Slice<Post> posts;

        // 태그가 없는 경우 (전체 게시글)
        if (tagId == null) {
            if (userFestival != null && !hiddenPostIds.isEmpty()) {
                posts = postRepository.findAllByIdNotInOrderByCreatedAtAsc(hiddenPostIds, pageable);
            } else {
                posts = postRepository.findAllByOrderByCreatedAtAsc(pageable);
            }
        }
        // 태그가 있는 경우 (특정 게시판)
        else {
            if (userFestival != null && !hiddenPostIds.isEmpty()) {
                posts = postRepository.findAllByTag_IdAndIdNotInOrderByCreatedAtAsc(tagId, hiddenPostIds, pageable);
            } else {
                posts = postRepository.findAllByTag_IdOrderByCreatedAtAsc(tagId, pageable);
            }
        }

        return posts.map(PostResponse::from);
    }


}