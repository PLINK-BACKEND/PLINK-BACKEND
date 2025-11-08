package com.plink.backend.commonService;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class S3Service {

    private final S3Client s3;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    @Value("${cloud.aws.s3.base-url}")
    private String baseUrl;

    // 파일 업로드 : 업로드 된 s3 key를 반환
    public S3UploadResult upload(MultipartFile file, String dir) throws IOException {
        // 1-1. S3 key 규칙: 디렉토리/UUID_원본파일명
        String prefix = (dir == null || dir.isBlank()) ? "uploads" : dir;
        String key = prefix + "/" + UUID.randomUUID() + "_" + file.getOriginalFilename();

        // 1-2. Content-Type 안전 지정
        String contentType = (file.getContentType() == null || file.getContentType().isBlank())
                ? "application/octet-stream" : file.getContentType();

        // 1-3. 요청 객체
        PutObjectRequest put = PutObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .contentType(contentType)
                .acl(ObjectCannedACL.PUBLIC_READ)

                // .serverSideEncryption(ServerSideEncryption.AES256) // (옵션) SSE
                .build();

        // 1-4. 스트림 업로드 (메모리 과다 사용 방지)
        try (InputStream in = file.getInputStream()) {
            s3.putObject(put, RequestBody.fromInputStream(in, file.getSize()));
        }

        String url = baseUrl + key;

        return new S3UploadResult(key, url, file.getOriginalFilename());
    }

    // 삭제
    public void delete(String key){
        s3.deleteObject(DeleteObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .build());
    }

    // 존재 여부 확인
    public boolean exists(String key){
        try{
            s3.headObject(HeadObjectRequest.builder()
            .bucket(bucket)
                    .key(key)
                    .build());
            return true;
        } catch (S3Exception e) {
            return false;
        }
    }
}
