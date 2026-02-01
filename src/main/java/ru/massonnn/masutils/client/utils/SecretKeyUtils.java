package ru.massonnn.masutils.client.utils;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

public class SecretKeyUtils {

    private static final byte[] PART_A = {109, 97, 115, 115};
    private static final byte[] PART_B = {111, 110, 110, 110};

    private static final MessageDigest SHA256_DIGEST;

    static {
        try {
            SHA256_DIGEST = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not found", e);
        }
    }

    public static String getHardwareUUID() {
        try {
            String fingerPrint = System.getProperty("os.name") +
                    System.getProperty("os.arch") +
                    System.getProperty("user.name") +
                    Runtime.getRuntime().availableProcessors();

            return UUID.nameUUIDFromBytes(fingerPrint.getBytes(StandardCharsets.UTF_8)).toString();
        } catch (Exception e) {
            return UUID.randomUUID().toString();
        }
    }

    public static String generateSignature(String data, long nonce, long timestamp, String hwid) {
        try {
            byte[] keyBytes = new byte[PART_A.length + PART_B.length];
            System.arraycopy(PART_A, 0, keyBytes, 0, PART_A.length);
            System.arraycopy(PART_B, 0, keyBytes, PART_A.length, PART_B.length);

            SecretKeySpec secretKey = new SecretKeySpec(keyBytes, "HmacSHA256");
            Mac hmac = Mac.getInstance("HmacSHA256");
            hmac.init(secretKey);

            String input = data + nonce + timestamp;
            byte[] hashBytes = hmac.doFinal(input.getBytes(StandardCharsets.UTF_8));

            return bytesToHex(hashBytes);
        } catch (Exception e) {
            return "error";
        }
    }

    public static long solvePoW(String data, String hwid, long timestamp, int difficulty) {
        String target = "0".repeat(difficulty);
        long nonce = 0;

        while (true) {
            String input = data + hwid + nonce + timestamp;
            String hash = calculateSha256(input);

            if (hash.startsWith(target)) {
                return nonce;
            }
            nonce++;

            if (nonce == Long.MAX_VALUE) break;
        }
        return nonce;
    }

    private static String calculateSha256(String base) {
        synchronized (SHA256_DIGEST) {
            byte[] hash = SHA256_DIGEST.digest(base.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(hash);
        }
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder hexString = new StringBuilder();
        for (byte b : bytes) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) hexString.append('0');
            hexString.append(hex);
        }
        return hexString.toString();
    }
}