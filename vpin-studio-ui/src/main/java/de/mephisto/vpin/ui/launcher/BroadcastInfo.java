package de.mephisto.vpin.ui.launcher;

public class BroadcastInfo {
    private final String systemName;
    private long lastBroadcastTime;

    public BroadcastInfo(String systemName, long lastBroadcastTime) {
        this.systemName = systemName;
        this.lastBroadcastTime = lastBroadcastTime;
    }

    public String getSystemName() {
        return systemName;
    }

    public long getLastBroadcastTime() {
        return lastBroadcastTime;
    }

    public void updateLastBroadcastTime(long lastBroadcastTime) {
        this.lastBroadcastTime = lastBroadcastTime;
    }
}