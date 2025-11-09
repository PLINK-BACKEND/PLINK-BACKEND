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
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FourCutsService {

    private final S3Service s3Service;
    private final QRCodeUtil qrCodeUtil;

    public FourCutsResponse uploadFourCut(MultipartFile file) {
        try {
            // 파일명에서 확장자만 추출하고, 한글 이름 대신 안전한 UUID 이름 생성
            String originalName = file.getOriginalFilename();
            String ext = originalName.substring(originalName.lastIndexOf("."));
            String safeName = UUID.randomUUID() + ext; // ex) 8d9a7e3a-...jpg

            MultipartFile safeFile = new MultipartFile() {
                @Override public String getName() { return file.getName(); }
                @Override public String getOriginalFilename() { return safeName; }
                @Override public String getContentType() { return file.getContentType(); }
                @Override public boolean isEmpty() { return file.isEmpty(); }
                @Override public long getSize() { return file.getSize(); }
                @Override public byte[] getBytes() throws IOException { return file.getBytes(); }
                @Override public ByteArrayInputStream getInputStream() throws IOException { return new ByteArrayInputStream(file.getBytes()); }
                @Override public void transferTo(java.io.File dest) throws IOException { file.transferTo(dest); }
            };

            // 네컷사진 업로드
            S3UploadResult uploadResult = s3Service.upload(safeFile, "fourcuts");

            // 업로드된 URL을 QR 코드에 그대로 사용
            String imageUrl = uploadResult.getUrl();

            // QR 코드 생성
            byte[] qrBytes = qrCodeUtil.generateQRCode(imageUrl);

            // QR 코드 파일 업로드
            MultipartFile qrFile = new MultipartFile() {
                @Override public String getName() { return "qr"; }
                @Override public String getOriginalFilename() { return "qr_" + safeName; }
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
