package com.dvein.banking_backend.transaction.util;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Base64;

@Slf4j
@Component
public class UpiQrCodeGenerator {

    public String generateUpiQrData(String upiId, String name, BigDecimal amount, String description) {
        StringBuilder qrData = new StringBuilder("upi://pay?");
        qrData.append("pa=").append(urlEncode(upiId));
        qrData.append("&pn=").append(urlEncode(name));

        if (amount != null) {
            qrData.append("&am=").append(amount.toPlainString());
        }

        if (description != null && !description.isEmpty()) {
            qrData.append("&tn=").append(urlEncode(description));
        }

        qrData.append("&cu=INR");

        return qrData.toString();
    }

    public String generateQrImage(String qrData) {
        try {
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(qrData, BarcodeFormat.QR_CODE, 300, 300);

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(bitMatrix, "PNG", outputStream);
            byte[] qrBytes = outputStream.toByteArray();

            return Base64.getEncoder().encodeToString(qrBytes);

        } catch (WriterException | IOException e) {
            log.error("Failed to generate QR code image", e);
            // Fallback to base64 encoded placeholder
            String placeholder = "QR_CODE_IMAGE_" + qrData.hashCode();
            return Base64.getEncoder().encodeToString(placeholder.getBytes());
        }
    }

    public UpiQrDataParsed parseUpiQrData(String qrData) {
        UpiQrDataParsed parsed = new UpiQrDataParsed();

        if (qrData == null || !qrData.startsWith("upi://pay?")) {
            return parsed;
        }

        String params = qrData.substring("upi://pay?".length());
        String[] paramPairs = params.split("&");

        for (String pair : paramPairs) {
            String[] keyValue = pair.split("=");
            if (keyValue.length == 2) {
                String key = keyValue[0];
                String value = urlDecode(keyValue[1]);

                switch (key) {
                    case "pa":
                        parsed.setPayeeAddress(value);
                        break;
                    case "pn":
                        parsed.setPayeeName(value);
                        break;
                    case "am":
                        try {
                            parsed.setAmount(new BigDecimal(value));
                        } catch (NumberFormatException e) {
                            log.warn("Invalid amount in QR code: {}", value);
                        }
                        break;
                    case "tn":
                        parsed.setTransactionNote(value);
                        break;
                }
            }
        }

        return parsed;
    }

    private String urlEncode(String value) {
        try {
            return java.net.URLEncoder.encode(value, "UTF-8");
        } catch (Exception e) {
            return value;
        }
    }

    private String urlDecode(String value) {
        try {
            return java.net.URLDecoder.decode(value, "UTF-8");
        } catch (Exception e) {
            return value;
        }
    }

    public static class UpiQrDataParsed {
        private String payeeAddress;
        private String payeeName;
        private BigDecimal amount;
        private String transactionNote;

        public String getPayeeAddress() { return payeeAddress; }
        public void setPayeeAddress(String payeeAddress) { this.payeeAddress = payeeAddress; }
        public String getPayeeName() { return payeeName; }
        public void setPayeeName(String payeeName) { this.payeeName = payeeName; }
        public BigDecimal getAmount() { return amount; }
        public void setAmount(BigDecimal amount) { this.amount = amount; }
        public String getTransactionNote() { return transactionNote; }
        public void setTransactionNote(String transactionNote) { this.transactionNote = transactionNote; }
    }
}