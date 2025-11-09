package com.plink.backend.feed.dto.post;

import lombok.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class PostUpdateRequest {
    private String title;
    private String content;
    private Long tagId;

}
