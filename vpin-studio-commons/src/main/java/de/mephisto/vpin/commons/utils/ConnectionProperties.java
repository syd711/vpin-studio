package de.mephisto.vpin.commons.utils;

import de.mephisto.vpin.restclient.util.OSUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class ConnectionProperties {
  private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private final PropertiesStore store;
  public static final String ID_PREFIX = "connection";

  // Constructor
  public ConnectionProperties() {
    if (!OSUtil.isMac()) {
      File propertiesFile = new File("config/connection.properties");
      propertiesFile.getParentFile().mkdirs();
      store = PropertiesStore.create(propertiesFile);
    }
    else {
      LOG.info("Setting Mac Path for Connection.Properties");
      File propertiesFile = new File(System.getProperty("MAC_WRITE_PATH") + "config/connection.properties");
      propertiesFile.getParentFile().mkdirs();
      store = PropertiesStore.create(propertiesFile);
    }


    // Detect and convert old format
    if (containsOldFormat()) {
      LOG.info("Old format detected in connection.properties, converting to new format...");
      convertToNewFormat();
    }
  }

  private void convertToNewFormat() {
    Properties properties = store.getProperties();
    List<String> oldKeys = new ArrayList<>();

    int index = 1; // Start numbering new connections from 1

    for (String key : properties.stringPropertyNames()) {
      if (key.matches("\\d+")) {
        String ip = properties.getProperty(key);

        // Generate new keys for the connection
        String newKey = ID_PREFIX + index;
        store.set(newKey + ".ip", ip);
        store.set(newKey + ".name", ""); // Default name
        store.set(newKey + ".mac_address", ""); // Empty MAC address
        store.set(newKey + ".type", "created"); // Default type

        // Track old keys for removal
        oldKeys.add(key);
        index++;
      }
    }

    // Remove all old keys after conversion
    for (String oldKey : oldKeys) {
      store.remove(oldKey);
      properties.remove(oldKey);
    }

    LOG.info("Conversion to new format completed. Old properties cleared.");
  }

  // Check if the old format exists (keys are integers)
  private boolean containsOldFormat() {
    for (String key : store.getProperties().stringPropertyNames()) {
      if (key.matches("\\d+")) { // Old format has integer keys
        return true;
      }
    }
    return false;
  }

  // Retrieve all connections as a list of IP addresses (for now)
  public List<ConnectionEntry> getConnections() {
    List<ConnectionEntry> connections = new ArrayList<>();

    try {
      for (String key : store.getProperties().stringPropertyNames()) {
        if (key.matches(ID_PREFIX + "\\d+\\.ip")) {
          String idString = key.split("\\.")[0].replace(ID_PREFIX, "");
          int id = Integer.parseInt(idString);
          ConnectionEntry entry = createConnectionEntry(id);
          connections.add(entry);
        }
      }
    }
    catch (Exception e) {
      LOG.error("Failed to load connection properties: {}", e.getMessage(), e);
    }

    return connections;
  }

  public ConnectionEntry getConnection(int id) {
    String idPrefix = ID_PREFIX + id;

    if (store.getProperties().containsKey(idPrefix + ".ip")) {
      return createConnectionEntry(id);
    }

    LOG.warn("No connection found with ID: {}", id);
    return null;
  }

  private ConnectionEntry createConnectionEntry(int id) {
    ConnectionEntry entry = new ConnectionEntry();
    entry.setId(id);

    String keyPrefix = ID_PREFIX + id;
    entry.setIp(store.get(keyPrefix + ".ip"));
    entry.setName(store.getString(keyPrefix + ".name", ""));
    entry.setMacAddress(store.getString(keyPrefix + ".mac_address", ""));
    String typeString = store.getString(keyPrefix + ".type", "created");
    ConnectionEntry.ConnectionType type = ConnectionEntry.ConnectionType.valueOf(typeString.toUpperCase());
    entry.setType(type);
    String magicPacketPortString = store.getString(keyPrefix + ".magic_packet_port");
    if (magicPacketPortString != null && !magicPacketPortString.isEmpty()) {
      entry.setMagicPacketPort(Integer.parseInt(magicPacketPortString));
    }
    return entry;
  }

  // Add a new connection
  public void upsertConnection(String ipAddress, String name, String macAddress, ConnectionEntry.ConnectionType type) {
    ConnectionEntry connectionEntry = findConnectionIdByIp(ipAddress);

    // Create Connection
    if (connectionEntry == null) {
      int nextId = getNextAvailableId();
      LOG.info("Adding connection: {} -> {}", nextId, ipAddress);

      String keyPrefix = ID_PREFIX + nextId;
      store.set(keyPrefix + ".ip", ipAddress);
      store.set(keyPrefix + ".mac_address", macAddress);
      store.set(keyPrefix + ".name", name);
      store.set(keyPrefix + ".type", type.name().toLowerCase());

    }

    // Update connection to match type
    if (connectionEntry != null) {
      int id = connectionEntry.getId();
      LOG.warn("Updating connection: {} -> {}.", id, ipAddress);

      String keyPrefix = ID_PREFIX + id;
      store.set(keyPrefix + ".mac_address", macAddress);
      store.set(keyPrefix + ".name", name);
      store.set(keyPrefix + ".type", type.name().toLowerCase());

      // Only write magic packet port on update.
      store.set(keyPrefix + ".magic_packet_port", connectionEntry.getMagicPacketPort());

      return;
    }

    // Do not add duplicate
    if (findConnectionIdByIp(ipAddress) != null) {
      LOG.warn("Connection with IP {} already exists, skipping addition.", ipAddress);
      return;
    }
  }

  private int getNextAvailableId() {
    int maxId = 0;

    // Find the highest connection ID
    for (String key : store.getProperties().stringPropertyNames()) {
      if (key.matches(ID_PREFIX + "\\d+\\.ip")) {
        String idString = key.split("\\.")[0].replace(ID_PREFIX, "");
        int id = Integer.parseInt(idString);
        if (id > maxId) {
          maxId = id;
        }
      }
    }

    // Return the next available ID
    return (maxId + 1);
  }

  // Remove a connection by IP address
  public void removeConnection(String ipAddress) {
    LOG.info("Removing connection by IP address: {}", ipAddress);

    // Find the connection ID associated with the IP address
    ConnectionEntry connectionEntry = findConnectionIdByIp(ipAddress);

    if (connectionEntry != null) {
      int id = connectionEntry.getId();
      LOG.info("Found connection {} for IP {}, removing...", id, ipAddress);

      // List of keys associated with the given connection ID
      String keyPrefix = ID_PREFIX + id;
      List<String> keysToRemove = List.of(
          keyPrefix + ".ip",
          keyPrefix + ".name",
          keyPrefix + ".mac_address",
          keyPrefix + ".type",
          keyPrefix + ".magic_packet_port"
      );

      store.removeAll(keysToRemove);

      LOG.info("Connection with IP {} removed successfully.", ipAddress);
    }
    else {
      LOG.warn("No connection found with IP: {}", ipAddress);
    }
  }

  // Helper method to find connection ID by IP address
  private ConnectionEntry findConnectionIdByIp(String ipAddress) {
    for (String key : store.getProperties().stringPropertyNames()) {
      if (key.endsWith(".ip") && store.get(key).equals(ipAddress)) {
        String connectionKey = key.split("\\.")[0]; // Extract "connection1"
        String idString = connectionKey.replace(ID_PREFIX, "");
        int id = Integer.parseInt(idString);
        return createConnectionEntry(id); // Extract only the numeric part, e.g., "1"
      }
    }
    return null;
  }
}
