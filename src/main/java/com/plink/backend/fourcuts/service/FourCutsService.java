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

@Service
@RequiredArgsConstructor
public class FourCutsService {

    private final S3Service s3Service;
    private final QRCodeUtil qrCodeUtil;

    /**
     * 네컷사진 업로드 + QR 코드 생성
     */
    public FourCutsResponse uploadFourCut(MultipartFile file) {
        try {
            // 1️⃣ 네컷사진을 S3에 업로드
            S3UploadResult uploadResult = s3Service.upload(file, "fourcuts");

            // 2️⃣ 이미지 URL 기반으로 QR 코드 생성
            byte[] qrBytes = qrCodeUtil.generateQRCode(uploadResult.getUrl());

            // 3️⃣ QR 코드 이미지를 다시 S3에 업로드
            MultipartFile qrFile = new MultipartFile() {
                @Override public String getName() { return "qr"; }
                @Override public String getOriginalFilename() { return "qr_" + file.getOriginalFilename(); }
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

            // 4️⃣ 결과 반환
            return new FourCutsResponse(uploadResult.getUrl(), qrUploadResult.getUrl());

        } catch (Exception e) {
            throw new RuntimeException("네컷사진 업로드 실패: " + e.getMessage(), e);
        }
    }
}
