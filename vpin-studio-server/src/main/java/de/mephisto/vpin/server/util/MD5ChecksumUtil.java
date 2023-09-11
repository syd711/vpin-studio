package de.mephisto.vpin.server.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class MD5ChecksumUtil {
  private final static Logger LOG = LoggerFactory.getLogger(MD5ChecksumUtil.class);

  private static MessageDigest mdigest;

  static {
    try {
      mdigest = MessageDigest.getInstance("MD5");
    } catch (NoSuchAlgorithmException e) {
      LOG.error("Failed to create md5 digest: " + e.getMessage(), e);
    }
  }

  public static String checksum(String value) {
    byte[] data = value.getBytes();
    mdigest.update(data, 0, data.length);

    // store the bytes returned by the digest() method
    byte[] bytes = mdigest.digest();

    // this array of bytes has bytes in decimal format
    // so we need to convert it into hexadecimal format

    // for this we create an object of StringBuilder
    // since it allows us to update the string i.e. its
    // mutable
    StringBuilder sb = new StringBuilder();

    // loop through the bytes array
    for (int i = 0; i < bytes.length; i++) {

      // the following line converts the decimal into
      // hexadecimal format and appends that to the
      // StringBuilder object
      sb.append(Integer
          .toString((bytes[i] & 0xff) + 0x100, 16)
          .substring(1));
    }

    // finally we return the complete hash
    return sb.toString();
  }
}
