package com.plink.backend.feed.dto;

import com.plink.backend.feed.entity.PostType;
import lombok.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class PostCreateRequest {
    private String title;
    private PostType postType;
    private String content;
    private String tagName;
    private List<MultipartFile> images;
    private Long festivalId;
    private PollCreateRequest poll;

}


