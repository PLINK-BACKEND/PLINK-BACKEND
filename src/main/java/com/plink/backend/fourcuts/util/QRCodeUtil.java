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
        // ✅ 수정 없음: URL을 그대로 QR에 사용 (이미 인코딩 완료 상태)
        QRCodeWriter writer = new QRCodeWriter();
        BitMatrix matrix = writer.encode(content.trim(), BarcodeFormat.QR_CODE, 300, 300);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        MatrixToImageWriter.writeToStream(matrix, "PNG", outputStream);
        return outputStream.toByteArray();
    }
}