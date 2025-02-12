package de.mephisto.vpin.restclient.util;

import edu.umd.cs.findbugs.annotations.Nullable;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.NetworkInterface;
import java.net.URL;

public class NetworkUtil {
  private final static Logger LOG = LoggerFactory.getLogger(NetworkUtil.class);

  public static String getMacAddress() {
    try {
      InetAddress localHost = InetAddress.getLocalHost();
      NetworkInterface ni = NetworkInterface.getByInetAddress(localHost);
      byte[] hardwareAddress = ni.getHardwareAddress();

      String[] hexadecimal = new String[hardwareAddress.length];
      for (int i = 0; i < hardwareAddress.length; i++) {
        hexadecimal[i] = String.format("%02X", hardwareAddress[i]);
      }
      return String.join("-", hexadecimal);
    }
    catch (Exception e) {
      LOG.error("Failed to read hardware address: {}", e.getMessage());
    }
    return null;
  }

  public static boolean isValidUrl(@Nullable String url) {
    if(!StringUtils.isEmpty(url)) {
      return false;
    }

    try {
      URL u = new URL(url);
    }
    catch (MalformedURLException e) {
      return false;
    }
    return true;
  }
}
