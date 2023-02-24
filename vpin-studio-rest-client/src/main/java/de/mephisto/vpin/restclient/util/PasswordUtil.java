package de.mephisto.vpin.restclient.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Base64;

/**
 *
 */
public class PasswordUtil {
  private final static Logger LOG = LoggerFactory.getLogger(PasswordUtil.class);

  public static String encrypt(String plainText) {
    try {
      return Base64.getEncoder().encodeToString(plainText.getBytes());
    } catch (Exception e) {
      LOG.error("Encrypt failed: " + e.getMessage());
    }
    return null;
  }

  public static String decrypt(String encryptedText) {
    try {
      return new String(Base64.getDecoder().decode(encryptedText));
    } catch (Exception e) {
      LOG.error("Decrypt failed: " + e.getMessage());
    }
    return null;
  }

  public static void main(String[] args) {
    String pwd = "abc123";
    System.out.println(encrypt(pwd));
    encrypt(pwd);
    System.out.println(decrypt(pwd));
  }
}
