package de.mephisto.vpin.connectors.assets;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

public class EncryptDecrypt {

  public static final String SECRET_KEY_1 = "ssdkF$HUy2A#D%kd";
  public static final String SECRET_KEY_2 = "weJiSEvR5yAC5ftB";

  private IvParameterSpec ivParameterSpec;
  private SecretKeySpec secretKeySpec;
  private Cipher cipher;

  public EncryptDecrypt(String key) throws UnsupportedEncodingException, NoSuchPaddingException, NoSuchAlgorithmException {
    ivParameterSpec = new IvParameterSpec(key.getBytes("UTF-8"));
    secretKeySpec = new SecretKeySpec(SECRET_KEY_2.getBytes("UTF-8"), "AES");
    cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
  }


  /**
   * Encrypt the string with this internal algorithm.
   *
   * @param toBeEncrypt string object to be encrypt.
   * @return returns encrypted string.
   * @throws NoSuchPaddingException
   * @throws NoSuchAlgorithmException
   * @throws InvalidAlgorithmParameterException
   * @throws InvalidKeyException
   * @throws BadPaddingException
   * @throws IllegalBlockSizeException
   */
  public String encrypt(String toBeEncrypt) throws NoSuchPaddingException, NoSuchAlgorithmException,
      InvalidAlgorithmParameterException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException {
    cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, ivParameterSpec);
    byte[] encrypted = cipher.doFinal(toBeEncrypt.getBytes());
    return Base64.getEncoder().encodeToString(encrypted);
  }

  /**
   * Decrypt this string with the internal algorithm. The passed argument should be encrypted using
   * {@link #encrypt(String) encrypt} method of this class.
   *
   * @param encrypted encrypted string that was encrypted using {@link #encrypt(String) encrypt} method.
   * @return decrypted string.
   * @throws InvalidAlgorithmParameterException
   * @throws InvalidKeyException
   * @throws BadPaddingException
   * @throws IllegalBlockSizeException
   */
  public String decrypt(String encrypted) throws InvalidAlgorithmParameterException, InvalidKeyException,
      BadPaddingException, IllegalBlockSizeException {
    cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, ivParameterSpec);
    byte[] decryptedBytes = cipher.doFinal(Base64.getDecoder().decode(encrypted));
    return new String(decryptedBytes);
  }
}