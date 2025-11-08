package com.plink.backend.fourcuts.util;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Component
public class QRCodeUtil {

    public byte[] generateQRCode(String content) throws WriterException, IOException {
        // ✅ URL 내 공백만 안전하게 인코딩하고, 전체 구조는 그대로 유지
        String safeContent = content.replace(" ", "%20");

        QRCodeWriter writer = new QRCodeWriter();
        BitMatrix matrix = writer.encode(safeContent, BarcodeFormat.QR_CODE, 300, 300);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        MatrixToImageWriter.writeToStream(matrix, "PNG", outputStream);
        return outputStream.toByteArray();
    }
}
