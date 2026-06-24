package com.dvein.banking_backend.common.util;

import org.mindrot.jbcrypt.BCrypt;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;

/**
 * Cryptographic utility for the banking application.
 *
 * <p>Password hashing: BCrypt (cost factor 12)
 * <p>Symmetric encryption: AES-256-GCM (authenticated encryption, random IV per operation)
 * <p>Hashing: SHA-256
 */
@Component
public class EncryptionUtil {

    private static final String AES_ALGORITHM  = "AES";
    private static final String GCM_CIPHER     = "AES/GCM/NoPadding";
    private static final int    GCM_TAG_BITS   = 128;
    private static final int    GCM_IV_BYTES   = 12;  // 96-bit IV recommended for GCM

    /**
     * AES encryption key — read from application.properties / environment variable.
     * MUST be externalized; never hardcode in source.
     * Recommended: set via environment variable ENCRYPTION_SECRET_KEY
     */
    @Value("${encryption.secret-key}")
    private String rawSecretKey;

    // =========================================================================
    // Password Hashing (BCrypt)
    // =========================================================================

    public String hashPassword(String password) {
        return BCrypt.hashpw(password, BCrypt.gensalt(12));
    }

    public boolean verifyPassword(String plainPassword, String hashedPassword) {
        return BCrypt.checkpw(plainPassword, hashedPassword);
    }

    // =========================================================================
    // Symmetric Encryption (AES-256-GCM)
    // =========================================================================

    /**
     * Encrypts {@code plainText} using AES-256-GCM.
     * A random 96-bit IV is generated for each call and prepended to the
     * ciphertext so it can be extracted during decryption.
     *
     * @return Base64-encoded string: [12-byte IV | ciphertext+auth-tag]
     */
    public String encrypt(String plainText) {
        try {
            byte[] iv = generateIv();
            Cipher cipher = Cipher.getInstance(GCM_CIPHER);
            cipher.init(Cipher.ENCRYPT_MODE, buildKey(), new GCMParameterSpec(GCM_TAG_BITS, iv));

            byte[] cipherText = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));

            // Prepend IV to ciphertext so decrypt can extract it
            byte[] ivPlusCipher = new byte[GCM_IV_BYTES + cipherText.length];
            System.arraycopy(iv, 0, ivPlusCipher, 0, GCM_IV_BYTES);
            System.arraycopy(cipherText, 0, ivPlusCipher, GCM_IV_BYTES, cipherText.length);

            return Base64.getEncoder().encodeToString(ivPlusCipher);

        } catch (Exception e) {
            throw new RuntimeException("Encryption failed", e);
        }
    }

    /**
     * Decrypts a value produced by {@link #encrypt(String)}.
     */
    public String decrypt(String encryptedBase64) {
        try {
            byte[] ivPlusCipher = Base64.getDecoder().decode(encryptedBase64);

            // Extract IV (first 12 bytes)
            byte[] iv         = Arrays.copyOfRange(ivPlusCipher, 0, GCM_IV_BYTES);
            byte[] cipherText = Arrays.copyOfRange(ivPlusCipher, GCM_IV_BYTES, ivPlusCipher.length);

            Cipher cipher = Cipher.getInstance(GCM_CIPHER);
            cipher.init(Cipher.DECRYPT_MODE, buildKey(), new GCMParameterSpec(GCM_TAG_BITS, iv));

            return new String(cipher.doFinal(cipherText), StandardCharsets.UTF_8);

        } catch (Exception e) {
            throw new RuntimeException("Decryption failed", e);
        }
    }

    // =========================================================================
    // SHA-256 Hashing
    // =========================================================================

    public String hashSHA256(String data) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(data.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder(hash.length * 2);
            for (byte b : hash) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString();
        } catch (Exception e) {
            throw new RuntimeException("Hashing failed", e);
        }
    }

    // =========================================================================
    // Helpers
    // =========================================================================

    private SecretKey buildKey() throws Exception {
        // Derive a 256-bit key from the configured secret via SHA-256
        MessageDigest sha = MessageDigest.getInstance("SHA-256");
        byte[] keyBytes = sha.digest(rawSecretKey.getBytes(StandardCharsets.UTF_8));
        return new SecretKeySpec(keyBytes, AES_ALGORITHM);
    }

    private byte[] generateIv() {
        byte[] iv = new byte[GCM_IV_BYTES];
        new SecureRandom().nextBytes(iv);
        return iv;
    }
}