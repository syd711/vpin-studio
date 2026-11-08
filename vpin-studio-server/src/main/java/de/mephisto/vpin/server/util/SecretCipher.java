package de.mephisto.vpin.server.util;

import com.sun.jna.platform.win32.Crypt32Util;
import com.sun.jna.platform.win32.WinCrypt;
import de.mephisto.vpin.commons.SystemInfo;
import de.mephisto.vpin.restclient.util.OSUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.File;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Encrypts secrets (e.g. account passwords) before they are written to the database.
 * <p>
 * Windows: DPAPI, bound to the Windows account the server runs as.
 * Other OS: AES-GCM with a random key stored in the resources folder.
 * <p>
 * Encrypted values carry a prefix, so legacy plain text values can be detected and migrated.
 */
public class SecretCipher {
  private final static Logger LOG = LoggerFactory.getLogger(SecretCipher.class);

  private static final String PREFIX = "enc:";
  private static final String DPAPI_PREFIX = PREFIX + "dpapi:";
  private static final String AES_PREFIX = PREFIX + "aes:";

  private static final String KEY_FILE_NAME = "secret.key";
  private static final int GCM_IV_LENGTH = 12;
  private static final int GCM_TAG_BITS = 128;

  private static final SecureRandom RANDOM = new SecureRandom();

  private SecretCipher() {
  }

  public static boolean isEncrypted(String value) {
    return value != null && value.startsWith(PREFIX);
  }

  /**
   * @return the encrypted value, or the value unchanged if it is blank or already encrypted.
   */
  public static String encrypt(String plain) {
    if (StringUtils.isEmpty(plain) || isEncrypted(plain)) {
      return plain;
    }
    byte[] data = plain.getBytes(StandardCharsets.UTF_8);
    try {
      if (OSUtil.isWindows()) {
        byte[] enc = Crypt32Util.cryptProtectData(data, null, WinCrypt.CRYPTPROTECT_UI_FORBIDDEN, "VPin Studio", null);
        return DPAPI_PREFIX + Base64.getEncoder().encodeToString(enc);
      }
      return AES_PREFIX + Base64.getEncoder().encodeToString(aesEncrypt(data));
    }
    catch (Exception e) {
      throw new IllegalStateException("Failed to encrypt secret: " + e.getMessage(), e);
    }
  }

  /**
   * @return the decrypted value, the value unchanged if it is not encrypted (legacy plain text),
   * or null if it can't be decrypted (e.g. database copied to another machine or Windows account).
   */
  public static String decrypt(String value) {
    if (!isEncrypted(value)) {
      return value;
    }
    try {
      if (value.startsWith(DPAPI_PREFIX)) {
        byte[] enc = Base64.getDecoder().decode(value.substring(DPAPI_PREFIX.length()));
        byte[] plain = Crypt32Util.cryptUnprotectData(enc, null, WinCrypt.CRYPTPROTECT_UI_FORBIDDEN, null);
        return new String(plain, StandardCharsets.UTF_8);
      }
      if (value.startsWith(AES_PREFIX)) {
        byte[] enc = Base64.getDecoder().decode(value.substring(AES_PREFIX.length()));
        return new String(aesDecrypt(enc), StandardCharsets.UTF_8);
      }
      LOG.warn("Unknown secret encryption format.");
    }
    catch (Throwable e) {
      LOG.warn("Failed to decrypt secret, it has to be entered again: {}", e.getMessage());
    }
    return null;
  }

  private static byte[] aesEncrypt(byte[] data) throws Exception {
    byte[] iv = new byte[GCM_IV_LENGTH];
    RANDOM.nextBytes(iv);
    Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
    cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(getAesKey(), "AES"), new GCMParameterSpec(GCM_TAG_BITS, iv));
    byte[] enc = cipher.doFinal(data);
    return ByteBuffer.allocate(iv.length + enc.length).put(iv).put(enc).array();
  }

  private static byte[] aesDecrypt(byte[] data) throws Exception {
    Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
    cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(getAesKey(), "AES"), new GCMParameterSpec(GCM_TAG_BITS, data, 0, GCM_IV_LENGTH));
    return cipher.doFinal(data, GCM_IV_LENGTH, data.length - GCM_IV_LENGTH);
  }

  private static synchronized byte[] getAesKey() throws Exception {
    File keyFile = new File(SystemInfo.RESOURCES, KEY_FILE_NAME);
    if (keyFile.exists()) {
      return Files.readAllBytes(keyFile.toPath());
    }
    byte[] key = new byte[32];
    RANDOM.nextBytes(key);
    keyFile.getParentFile().mkdirs();
    Files.write(keyFile.toPath(), key);
    if (!keyFile.setReadable(false, false) || !keyFile.setReadable(true, true)
        || !keyFile.setWritable(false, false) || !keyFile.setWritable(true, true)) {
      LOG.warn("Could not restrict permissions of {}", keyFile.getAbsolutePath());
    }
    LOG.info("Created new secret key file {}", keyFile.getAbsolutePath());
    return key;
  }
}
