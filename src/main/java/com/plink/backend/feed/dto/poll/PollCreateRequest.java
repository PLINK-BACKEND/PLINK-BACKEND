package com.plink.backend.feed.dto.poll;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PollCreateRequest {
    private String question;
    private List<String> options;
}
