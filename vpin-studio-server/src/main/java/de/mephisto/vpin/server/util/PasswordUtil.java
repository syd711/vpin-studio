package de.mephisto.vpin.server.util;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

/**
 *
 */
public class PasswordUtil {

  private static Cipher cipher;
  private static SecretKey secretKey;

  static {
    try {
      KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
      keyGenerator.init(128);
      SecretKey secretKey = keyGenerator.generateKey();
      cipher = Cipher.getInstance("AES");
    } catch (NoSuchAlgorithmException e) {
      throw new RuntimeException(e);
    } catch (NoSuchPaddingException e) {
      throw new RuntimeException(e);
    }
  }

  public static String encrypt(String plainText) {
    try {
      byte[] plainTextByte = plainText.getBytes();
      cipher.init(Cipher.ENCRYPT_MODE, secretKey);
      byte[] encryptedByte = cipher.doFinal(plainTextByte);
      Base64.Encoder encoder = Base64.getEncoder();
      return encoder.encodeToString(encryptedByte);
    } catch (Exception e) {
    }
    return null;
  }

  public static String decrypt(String encryptedText) {
    try {
      Base64.Decoder decoder = Base64.getDecoder();
      byte[] encryptedTextByte = decoder.decode(encryptedText);
      cipher.init(Cipher.DECRYPT_MODE, secretKey);
      byte[] decryptedByte = cipher.doFinal(encryptedTextByte);
      return new String(decryptedByte);
    } catch (Exception e) {

    }
    return null;
  }
}
