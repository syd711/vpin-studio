package de.mephisto.vpin.restclient.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.NetworkInterface;

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
}