package com.plink.backend.commonService;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class S3UploadResult {
    private String key;
    private String url;
    private String originalFilename;
}
