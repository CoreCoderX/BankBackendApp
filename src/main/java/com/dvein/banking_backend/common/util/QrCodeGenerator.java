package com.dvein.banking_backend.common.util;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.util.Base64;

@Component
public class QrCodeGenerator {

    public String generateQRCodeBase64(String data, int width, int height) {
        try {
            BitMatrix bitMatrix = new MultiFormatWriter()
                    .encode(data, BarcodeFormat.QR_CODE, width, height);

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(bitMatrix, "PNG", outputStream);

            byte[] qrCodeBytes = outputStream.toByteArray();
            return Base64.getEncoder().encodeToString(qrCodeBytes);

        } catch (Exception e) {
            throw new RuntimeException("Failed to generate QR code", e);
        }
    }

    public String generateTotpQRCode(String issuer, String accountName, String secret) {
        String otpauthUrl = String.format(
                "otpauth://totp/%s:%s?secret=%s&issuer=%s",
                issuer,
                accountName,
                secret,
                issuer
        );

        return generateQRCodeBase64(otpauthUrl, 300, 300);
    }
}