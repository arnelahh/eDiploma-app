package utils;

import dao.CloudDatabaseConnection;


import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;


public class AESEncryption {

    private static final String SECRET_KEY = CloudDatabaseConnection.getSecretKey();
    private static final String ALGORITHM = "AES";


    public static String encrypt(String plainText) {
        try {
            if (SECRET_KEY == null || SECRET_KEY.isEmpty()) {
                throw new RuntimeException("Secret Key nije učitan! Provjeri database.properties.");
            }
            SecretKey key = new SecretKeySpec(SECRET_KEY.getBytes(StandardCharsets.UTF_8), ALGORITHM);
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, key);
            byte[] encryptedBytes = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(encryptedBytes);
        } catch (Exception e) {
            throw new RuntimeException("Failed to encrypt data", e);
        }
    }

    public static String decrypt(String encryptedText) {
        try {
            if (SECRET_KEY == null || SECRET_KEY.isEmpty()) {
                throw new RuntimeException("Secret Key nije učitan! Provjeri database.properties.");
            }
            SecretKey key = new SecretKeySpec(SECRET_KEY.getBytes(StandardCharsets.UTF_8), ALGORITHM);
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, key);
            byte[] decodedBytes = Base64.getDecoder().decode(encryptedText);
            byte[] decryptedBytes = cipher.doFinal(decodedBytes);
            return new String(decryptedBytes, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("Failed to decrypt data", e);
        }
    }
}
