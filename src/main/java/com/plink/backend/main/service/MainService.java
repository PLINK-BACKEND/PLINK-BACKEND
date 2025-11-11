package com.plink.backend.main.service;

import com.plink.backend.feed.dto.post.PostResponse;
import com.plink.backend.feed.entity.Post;
import com.plink.backend.feed.entity.PostType;
import com.plink.backend.feed.entity.ReportTargetType;
import com.plink.backend.feed.repository.HiddenContentRepository;
import com.plink.backend.feed.repository.PostRepository;
import com.plink.backend.main.dto.MainResponse;
import com.plink.backend.user.entity.User;
import com.plink.backend.user.entity.UserFestival;
import com.plink.backend.user.repository.UserFestivalRepository;
import com.plink.backend.user.repository.UserRepository;
import com.plink.backend.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MainService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final UserFestivalRepository userFestivalRepository;
    private final HiddenContentRepository hiddenContentRepository;

    // 인기글 조회하기 (3개)
    @Transactional(readOnly = true)
    public MainResponse getPopularPosts(User user, String slug) {


        List<Long> hiddenPostIds = List.of(); // 기본값

        // 로그인한 경우만 숨김(신고) 게시글 목록 조회

        if (user != null) {
            Optional<UserFestival> optionalFestival =
                    userFestivalRepository.findByUser_UserIdAndFestivalSlug(user.getUserId(), slug);

            if (optionalFestival.isPresent()) {
                hiddenPostIds = hiddenContentRepository
                        .findTargetIdsByUserFestivalAndTargetType(optionalFestival.get(), ReportTargetType.POST);
            }
        }

        // 인기 POLL 1개 (신고한 게시글 제외)
        List<Post> popularPolls = postRepository.findPopularPosts(
                slug, PostType.POLL,
                hiddenPostIds.isEmpty() ? null : hiddenPostIds,
                PageRequest.of(0, 1)
        );

        Post popularPoll = popularPolls.isEmpty() ? null : popularPolls.get(0);

        // 인기 게시글 3개 (POLL 포함 가능하나 위에서 뽑은 앙케이트 제외)
        List<Long> excludeIds = new ArrayList<>(hiddenPostIds);
        if (popularPoll != null) excludeIds.add(popularPoll.getId());

        List<Post> popularPosts = postRepository.findPopularPosts(
                slug, null,
                excludeIds.isEmpty() ? null : excludeIds,
                PageRequest.of(0, 3)
        );

        // DTO 변환 후 응답
        return MainResponse.from(popularPoll, popularPosts);

    }
}
