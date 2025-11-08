package com.plink.backend.fourcuts.service;

import com.plink.backend.commonService.S3Service;
import com.plink.backend.commonService.S3UploadResult;
import com.plink.backend.fourcuts.dto.FourCutsResponse;
import com.plink.backend.fourcuts.util.QRCodeUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Service
@RequiredArgsConstructor
public class FourCutsService {

    private final S3Service s3Service;
    private final QRCodeUtil qrCodeUtil;

    public FourCutsResponse uploadFourCut(MultipartFile file) {
        try {
            /**
             * ✅ 1️⃣ 파일명 인코딩 처리
             * - S3Service는 수정하지 않으므로, 여기서 한글 파일명만 미리 안전하게 바꾼 래퍼 MultipartFile을 생성
             */
            String encodedName = URLEncoder.encode(file.getOriginalFilename(), StandardCharsets.UTF_8)
                    .replace("+", "%20");

            MultipartFile encodedFile = new MultipartFile() {
                @Override public String getName() { return file.getName(); }
                @Override public String getOriginalFilename() { return encodedName; } // ✅ 수정: 한글 인코딩된 이름
                @Override public String getContentType() { return file.getContentType(); }
                @Override public boolean isEmpty() { return file.isEmpty(); }
                @Override public long getSize() { return file.getSize(); }
                @Override public byte[] getBytes() throws IOException { return file.getBytes(); }
                @Override public ByteArrayInputStream getInputStream() throws IOException { return new ByteArrayInputStream(file.getBytes()); }
                @Override public void transferTo(java.io.File dest) throws IOException { file.transferTo(dest); }
            };

            // ✅ 2️⃣ S3 업로드 (공통 S3Service는 그대로 사용)
            S3UploadResult uploadResult = s3Service.upload(encodedFile, "fourcuts");

            // ✅ 3️⃣ 업로드된 URL 그대로 QR 생성에 사용
            String imageUrl = uploadResult.getUrl();

            // 4️⃣ QR 코드 생성
            byte[] qrBytes = qrCodeUtil.generateQRCode(imageUrl);

            // 5️⃣ QR 코드 파일 업로드
            MultipartFile qrFile = new MultipartFile() {
                @Override public String getName() { return "qr"; }
                @Override public String getOriginalFilename() { return "qr_" + encodedName; } // ✅ QR 파일명도 안전하게
                @Override public String getContentType() { return "image/png"; }
                @Override public boolean isEmpty() { return qrBytes.length == 0; }
                @Override public long getSize() { return qrBytes.length; }
                @Override public byte[] getBytes() { return qrBytes; }
                @Override public ByteArrayInputStream getInputStream() { return new ByteArrayInputStream(qrBytes); }
                @Override public void transferTo(java.io.File dest) throws IOException {
                    throw new UnsupportedOperationException("not used");
                }
            };

            S3UploadResult qrUploadResult = s3Service.upload(qrFile, "fourcuts/qr");

            return new FourCutsResponse(imageUrl, qrUploadResult.getUrl());

        } catch (Exception e) {
            throw new RuntimeException("네컷사진 업로드 실패: " + e.getMessage(), e);
        }
    }
}
