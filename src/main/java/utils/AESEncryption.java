package utils;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class AESEncryption {

    // VAŽNO: Secret key treba biti sigurno čuvan - kasnije možeš premjestiti u config fajl ili environment variable
    private static final String SECRET_KEY = "eDiploma2026Key!"; // 16 karaktera za AES-128
    private static final String ALGORITHM = "AES";

    /**
     * Enkriptuje plaintext string koristeći AES
     * @param plainText Text za enkriptovanje
     * @return Base64 encoded enkriptovan string
     */
    public static String encrypt(String plainText) {
        try {
            SecretKey key = new SecretKeySpec(SECRET_KEY.getBytes(StandardCharsets.UTF_8), ALGORITHM);
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, key);
            byte[] encryptedBytes = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(encryptedBytes);
        } catch (Exception e) {
            throw new RuntimeException("Failed to encrypt data", e);
        }
    }

    /**
     * Dekriptuje AES enkriptovan string
     * @param encryptedText Base64 encoded enkriptovan string
     * @return Dekriptovan plaintext
     */
    public static String decrypt(String encryptedText) {
        try {
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

    /**
     * Test metoda za provjeru enkripcije
     */
    public static void main(String[] args) {
        String original = "test1234567890ab";
        String encrypted = encrypt(original);
        String decrypted = decrypt(encrypted);

        System.out.println("Original: " + original);
        System.out.println("Encrypted: " + encrypted);
        System.out.println("Decrypted: " + decrypted);
        System.out.println("Match: " + original.equals(decrypted));
    }
}
