package de.mephisto.vpin.commons.utils.network;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class WakeOnLan {
  private final static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  public static final int DEFAULT_PORT = 9; // Default WoL port

  public static void sendMagicPacket(String ipAddress, String macAddress, Integer port) throws Exception {
    port = port != null ? port : DEFAULT_PORT;

    // Parse the MAC address
    byte[] macBytes = getMacBytes(macAddress);
    byte[] bytes = new byte[6 + 16 * macBytes.length];

    // Create the wake-up frame
    for (int i = 0; i < 6; i++) {
      bytes[i] = (byte) 0xff;
    }
    for (int i = 6; i < bytes.length; i += macBytes.length) {
      System.arraycopy(macBytes, 0, bytes, i, macBytes.length);
    }

    // Broadcast so the packet reaches the NIC at Layer 2 even when the target
    // machine is asleep and cannot respond to ARP requests.
    InetAddress broadcastAddress = InetAddress.getByName("255.255.255.255");
    DatagramPacket packet = new DatagramPacket(bytes, bytes.length, broadcastAddress, port);
    try (DatagramSocket socket = new DatagramSocket()) {
      socket.setBroadcast(true);
      socket.send(packet);
    }

    LOG.info("Magic packet sent to {} on port {}", macAddress, port);
  }

  // Utility to parse the MAC address into bytes
  private static byte[] getMacBytes(String macAddress) throws IllegalArgumentException {
    String[] hex = macAddress.split("[-:]");
    if (hex.length != 6) {
      throw new IllegalArgumentException("Invalid MAC address.");
    }
    byte[] bytes = new byte[6];
    for (int i = 0; i < 6; i++) {
      bytes[i] = (byte) Integer.parseInt(hex[i], 16);
    }
    return bytes;
  }
}
