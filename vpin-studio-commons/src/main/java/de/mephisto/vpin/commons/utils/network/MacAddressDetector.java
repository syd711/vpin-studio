package de.mephisto.vpin.commons.utils.network;

import de.mephisto.vpin.restclient.util.OSUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.invoke.MethodHandles;
import java.net.InetAddress;
import java.util.concurrent.TimeUnit;

/**
 * Detects the MAC address of a remote host by reading the local ARP cache.
 * The host must have been contacted shortly before, so that an ARP entry exists.
 */
public class MacAddressDetector {
  private final static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  /**
   * @return the normalized MAC address or an empty string if none could be detected.
   */
  public static String detect(String host) {
    try {
      // arp expects an IP address, so resolve hostnames first
      InetAddress address = InetAddress.getByName(host);
      if (address.isLoopbackAddress()) {
        return "";
      }
      String ip = address.getHostAddress();

      // Windows: 'arp -a <ip>', macOS/Linux: 'arp -n <ip>' ('-a <host>' is not valid on macOS)
      String[] cmd = OSUtil.isWindows() ? new String[]{"arp", "-a", ip} : new String[]{"arp", "-n", ip};
      Process process = new ProcessBuilder(cmd).redirectErrorStream(true).start();

      try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
        String line;
        while ((line = reader.readLine()) != null) {
          String[] tokens = line.trim().split("\\s+"); // Handles spaces and tabs
          boolean hostLine = false;
          for (String token : tokens) {
            // Windows/Linux list the plain IP, macOS wraps it in parentheses: "? (192.168.1.10) at ..."
            if (token.equals(ip) || token.equals("(" + ip + ")")) {
              hostLine = true;
              break;
            }
          }
          if (!hostLine) {
            continue;
          }

          for (String token : tokens) {
            // macOS omits leading zeros, e.g. "0:1c:42:a:b:c"
            if (token.matches("([0-9A-Fa-f]{1,2}[:-]){5}[0-9A-Fa-f]{1,2}")) {
              String macAddress = normalize(token);
              LOG.info("MAC Address detected for host {} ({}): {}", host, ip, macAddress);
              return macAddress;
            }
          }
        }
      }
      process.waitFor(2, TimeUnit.SECONDS);
    }
    catch (Exception e) {
      LOG.error("Error detecting MAC Address for host {}: {}", host, e.getMessage());
    }

    LOG.warn("No MAC Address found for host: {}", host);
    return "";
  }

  static String normalize(String mac) {
    String separator = mac.contains("-") ? "-" : ":";
    String[] parts = mac.split("[:-]");
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < parts.length; i++) {
      if (i > 0) {
        sb.append(separator);
      }
      sb.append(parts[i].length() == 1 ? "0" + parts[i] : parts[i]);
    }
    return sb.toString().toUpperCase();
  }
}
