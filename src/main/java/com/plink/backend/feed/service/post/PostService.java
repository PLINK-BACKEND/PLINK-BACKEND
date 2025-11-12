package com.plink.backend.feed.service.post;

import com.plink.backend.feed.dto.poll.PollResponse;
import com.plink.backend.feed.dto.post.PostCreateRequest;
import com.plink.backend.feed.dto.post.PostResponse;
import com.plink.backend.feed.dto.post.PostDetailResponse;
import com.plink.backend.feed.entity.poll.Poll;
import com.plink.backend.feed.entity.post.Image;
import com.plink.backend.feed.entity.post.Post;
import com.plink.backend.feed.entity.post.PostType;
import com.plink.backend.feed.entity.report.ReportTargetType;
import com.plink.backend.feed.entity.tag.Tag;
import com.plink.backend.feed.repository.report.HiddenContentRepository;
import com.plink.backend.feed.service.poll.PollService;
import com.plink.backend.global.exception.CustomException;
import com.plink.backend.festival.repository.FestivalRepository;
import com.plink.backend.festival.entity.Festival;
import com.plink.backend.commonS3.S3Service;
import com.plink.backend.feed.repository.post.ImageRepository;
import com.plink.backend.feed.repository.post.PostRepository;
import com.plink.backend.feed.repository.tag.TagRepository;
import com.plink.backend.feed.dto.post.PostUpdateRequest;
import com.plink.backend.user.entity.User;
import com.plink.backend.user.entity.UserFestival;
import com.plink.backend.user.repository.UserFestivalRepository;
import com.plink.backend.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;


@Slf4j
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
    private final ImageService imageService;
    private final UserService userService;

    @Autowired
    private SimpMessageSendingOperations messagingTemplate;


    @Transactional
    // 게시글 작성하기

    public Post createPost(User author, PostCreateRequest request, String slug)  throws IOException {

        // 행사 검증
        Festival festival = festivalRepository.findBySlug(slug)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND,"존재하지 않는 행사입니다."));

        // 태그 검증
        Tag tag = tagRepository.findById(request.getTagId())
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND,"존재하지 않는 태그입니다."));

        // 작성자-축제 매핑 검증
        UserFestival userFestival = userFestivalRepository
                .findByUser_UserIdAndFestivalSlug(author.getUserId(), slug)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "해당 축제에서 유저를 찾을 수 없습니다."));



        // 기본 내용 검증
        if (request.getPostType() == PostType.NORMAL &&
                (request.getContent() == null || request.getContent().isBlank())) {
            throw new CustomException(HttpStatus.NOT_FOUND, "내용을 비워둘 수 없습니다.");
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

        // 이미지 추가
        if (request.getImages() != null && !request.getImages().isEmpty()) {
            imageService.saveImages(post.getId(), request.getImages());
        }

        // 최종 저장
        Post finalPost = postRepository.save(post);

        String festivalSlug = festival.getSlug();
        Long tagId = tag.getId();

        // 웹소켓 메세지 전송
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {

                    String slugPath = "/topic/" + festivalSlug + "/posts";
                    String tagPath = slugPath + "/" + tagId;

                    // 행사 전체 피드에 전송
                    log.info("📡 Broadcasting to All {}", slugPath);
                    messagingTemplate.convertAndSend(slugPath, PostResponse.from(finalPost));

                    // 행사 내 특정 태그 피드에도 전송
                    log.info("📡 Broadcasting to Tag {}", tagPath);
                    messagingTemplate.convertAndSend(tagPath, PostResponse.from(finalPost));
            }
        });

        return finalPost;

    }


    // 게시글 수정
    @Transactional
    public Post updatePost(User author,PostUpdateRequest request,Long postId)throws IOException {
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
        String slug = post.getFestival().getSlug();
        Long tagId = post.getTag() != null ? post.getTag().getId() : null;
        Long deletedPostId = post.getId();

        postRepository.delete(post);

        // 웹소켓 메세지 전송
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                Map<String, Object> payload = new HashMap<>();
                payload.put("id", deletedPostId);
                payload.put("deleted", true);

                // 전체 피드 전송
                String allPath = String.format("/topic/%s/posts", slug);
                messagingTemplate.convertAndSend(allPath, payload);
                log.info("게시글 삭제 전송 (전체): {}", allPath);

                // 특정 태그 피드 전송
                if (tagId != null) {
                    String tagPath = String.format("/topic/%s/posts/%d", slug, tagId);
                    messagingTemplate.convertAndSend(tagPath, payload);
                    log.info("게시글 삭제 전송 (태그): {}", tagPath);
                }
            }
        });
    }


    // 게시글 상세 조회 (댓글까지 모두 포함)
    @Transactional(readOnly = true)
    public PostDetailResponse getPostDetail(User user, String slug, Long postId) {

        // 게시글 조회
        Post post = postRepository.findWithAllById(postId)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "게시글을 찾을 수 없습니다."));

        List<Long> hiddenCommentIds = List.of();

        // 로그인을 한 사용자
        if (user != null) {
            UserFestival userFestival = userFestivalRepository
                    .findByUser_UserIdAndFestivalSlug(user.getUserId(), slug)
                    .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "해당 축제에서 사용자를 찾을 수 없습니다."));

            List<Long> hiddenPostIds = hiddenContentRepository
                    .findTargetIdsByUserFestivalAndTargetType(userFestival, ReportTargetType.POST);
            hiddenCommentIds = hiddenContentRepository
                    .findTargetIdsByUserFestivalAndTargetType(userFestival, ReportTargetType.COMMENT);

            if (hiddenPostIds.contains(postId)) {
                throw new CustomException(HttpStatus.FORBIDDEN, "신고하여 숨긴 게시글은 볼 수 없습니다.");
            }
        }

        // 앙케이트인 경우
        if (post.getPostType() == PostType.POLL && post.getPoll() != null) {
            PollResponse pollResponse = pollService.getPollResponse(post.getPoll(), user);
            return PostDetailResponse.from(post, pollResponse, hiddenCommentIds);
        }

        return PostDetailResponse.from(post,hiddenCommentIds);
    }


    // 게시글 전체 조회
    @Transactional(readOnly = true)
    public PostResponse.SliceResult getPostListByTag(User user, String slug, Pageable pageable, String tagName, String keyword) {
        List<Long> hiddenPostIds = null;

        // 로그인을 한 사람인 지 아닌 지 확인
        if (user != null) {
            Optional<UserFestival> optionalFestival =
                    userFestivalRepository.findByUser_UserIdAndFestivalSlug(user.getUserId(), slug);

            if (optionalFestival.isPresent()) {
                hiddenPostIds = hiddenContentRepository
                        .findTargetIdsByUserFestivalAndTargetType(optionalFestival.get(), ReportTargetType.POST);
            }
        }

        Slice<Post> posts = postRepository.findPostsFiltered(
                slug, tagName,keyword,hiddenPostIds,pageable
        );

        Slice<PostResponse> mapped = posts.map(PostResponse::from);
        return PostResponse.SliceResult.from(mapped);
    }

}