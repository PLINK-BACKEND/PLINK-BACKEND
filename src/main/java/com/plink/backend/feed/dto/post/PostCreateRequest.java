package com.plink.backend.feed.dto.post;

import com.plink.backend.feed.dto.poll.PollCreateRequest;
import com.plink.backend.feed.entity.PostType;
import lombok.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class PostCreateRequest {
    private String title;
    private PostType postType;
    private String content;
    private Long tagId;
    private List<MultipartFile> images;
    private Long festivalId;
    private PollCreateRequest poll;

}


