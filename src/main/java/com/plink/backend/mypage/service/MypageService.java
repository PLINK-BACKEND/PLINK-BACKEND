package com.plink.backend.mypage.service;

import com.plink.backend.auth.dto.UserResponse;
import com.plink.backend.commonService.S3Service;
import com.plink.backend.commonService.S3UploadResult;
import com.plink.backend.feed.dto.comment.CommentResponse;
import com.plink.backend.feed.dto.poll.PollResponse;
import com.plink.backend.feed.dto.post.PostDetailResponse;
import com.plink.backend.feed.dto.post.PostResponse;
import com.plink.backend.feed.entity.Comment;
import com.plink.backend.feed.entity.Post;
import com.plink.backend.feed.entity.PostLike;
import com.plink.backend.feed.entity.PostType;
import com.plink.backend.feed.repository.CommentRepository;
import com.plink.backend.feed.repository.PostLikeRepository;
import com.plink.backend.feed.repository.PostRepository;
import com.plink.backend.global.exception.CustomException;
import com.plink.backend.user.entity.User;
import com.plink.backend.user.entity.UserFestival;
import com.plink.backend.user.repository.UserFestivalRepository;
import com.plink.backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MypageService {

    private final PostRepository postRepository;
    private final PostLikeRepository postLikeRepository;
    private final CommentRepository commentRepository;
    private final S3Service s3Service;
    private final UserFestivalRepository userFestivalRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // 내가 쓴 글 보기
    @Transactional(readOnly = true)
    public List<PostResponse> getMyPosts(Long userId) {
        List<Post> posts = postRepository.findByAuthor_User_UserId(userId);
        return posts.stream()
                .map(PostResponse::from)
                .toList();
    }

    // 내가 좋아요 누른 글 보기
    @Transactional(readOnly = true)
    public List<PostResponse> getLikedPosts(Long userId) {
        List<PostLike> likes = postLikeRepository.findByUser_User_UserIdOrderByPost_CreatedAtDesc(userId);

        return likes.stream()
                .map(like -> PostResponse.from(like.getPost()))
                .toList();
    }

    // 내가 작성한 댓글 보기
    @Transactional(readOnly = true)
    public List<CommentResponse> getMyComments(Long userId) {

        List<Comment> comments = commentRepository.findByAuthor_User_UserIdOrderByCreatedAtAsc(userId);

        return comments.stream()
                .map(CommentResponse::from)
                .toList();
    }

    // 프로필 수정
    @Transactional
    public UserResponse updateProfile(User user, String slug, String nickname, MultipartFile profileImage) throws IOException {

        boolean updated = false;

        // 해당 축제(slug) 기준으로 UserFestival 조회
        UserFestival festival = userFestivalRepository
                .findByUser_UserIdAndFestivalSlug(user.getUserId(), slug)
                .orElseThrow(() -> new IllegalStateException("해당 축제의 유저 정보를 찾을 수 없습니다."));

        //  닉네임 변경
        if (nickname != null && !nickname.isBlank()) {
            if (userFestivalRepository.existsByFestivalSlugAndNickname(slug, nickname)) {
                throw new CustomException(HttpStatus.CONFLICT, "이 행사의 닉네임은 이미 사용 중입니다.");
            }
            festival.setNickname(nickname);
            updated = true;
        }

        // 프로필 이미지 변경
        if (profileImage != null && !profileImage.isEmpty()) {
            if (user.getProfileImageUrl() != null && user.getProfileImageUrl().contains("/profiles/")) {
                String oldKey = user.getProfileImageUrl()
                        .substring(user.getProfileImageUrl().indexOf("profiles/"));
                s3Service.delete(oldKey);
            }

            S3UploadResult result = s3Service.upload(profileImage, "profiles");
            user.setProfileImageUrl(result.getUrl());
            updated = true;
        }

        if (updated) {
            userRepository.save(user);
            userFestivalRepository.save(festival);
        }

        return new UserResponse(user, festival);
    }


    // 비밀번호 변경
    @Transactional
    public void changePassword(User user, String currentPassword, String newPassword) {

        // 현재 비밀번호 확인
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new CustomException(HttpStatus.UNAUTHORIZED, "현재 비밀번호가 올바르지 않습니다.");
        }

        // 새 비밀번호 암호화
        String encodedNewPw = passwordEncoder.encode(newPassword);
        user.setPassword(encodedNewPw);

        // 저장
        userRepository.save(user);
    }


}




