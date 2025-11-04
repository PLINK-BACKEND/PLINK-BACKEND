package com.plink.backend.feed.dto;

import lombok.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class PostRequest {
    private String title;
    private String content;
    private String tagName;
    private List<MultipartFile> images;
    private Long festivalId;
}
