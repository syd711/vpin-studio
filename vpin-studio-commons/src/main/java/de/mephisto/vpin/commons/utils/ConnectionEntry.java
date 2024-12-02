package de.mephisto.vpin.commons.utils;

import de.mephisto.vpin.commons.utils.network.WakeOnLan;

public class ConnectionEntry {
    public enum ConnectionType {
        CREATED, DISCOVERED
    }
    
    private int id;
    private String ip;
    private String name;
    private String macAddress;
    private ConnectionType type;
    private int magicPacketPort = WakeOnLan.DEFAULT_PORT;

    // Getters and setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getIp() { return ip; }
    public void setIp(String ip) { this.ip = ip; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getMacAddress() { return macAddress; }
    public void setMacAddress(String macAddress) { this.macAddress = macAddress; }
    public ConnectionType getType() { return type; }
    public void setType(ConnectionType type) { this.type = type; }
    public int getMagicPacketPort() { return magicPacketPort; }
    public void setMagicPacketPort(int port) { this.magicPacketPort = port; }
}
