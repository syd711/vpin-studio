package de.mephisto.vpin.ui.launcher;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.mephisto.vpin.commons.fx.UIDefaults;

public class DiscoveryListener {
    private final static Logger LOG = LoggerFactory.getLogger(DiscoveryListener.class);

    private BroadcastDataChangeListener listener;

    private Thread broadcastListenerThread;
    private Thread staleEntriesRemoverThread;
    private DatagramSocket socket;
    private boolean shouldListen = false;
    private final Map<InetAddress, BroadcastInfo> broadcastData = new ConcurrentHashMap<>();

    public Map<InetAddress, BroadcastInfo> getBroadcasts() {
        return broadcastData;
    }

    public void setBroadcastDataChangeListener(BroadcastDataChangeListener listener) {
        this.listener = listener;
    }

    private void notifyBroadcastDataChanged() {
        if (listener != null) {
            listener.onBroadcastDataChanged();
        }
    }

    private void listenForBroadcast() {
        try {
            LOG.info("Listening for Broadcast connections");

            socket = new DatagramSocket(50505);
            byte[] buffer = new byte[256];
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

            while (shouldListen) {
                socket.receive(packet);

                String receivedSystemName = new String(packet.getData(), 0, packet.getLength(),
                        StandardCharsets.US_ASCII);
                if (receivedSystemName.isEmpty()) {
                    receivedSystemName = UIDefaults.VPIN_NAME;
                }

                InetAddress senderAddress = packet.getAddress();

                long currentTime = System.currentTimeMillis();
                if (!broadcastData.containsKey(senderAddress)) {
                    // Log the received information
                    LOG.info("Received broadcast for the first time from IP: {}, System Name: {}",
                            senderAddress.getHostAddress(),
                            receivedSystemName);

                    // Store the IP and system name
                    broadcastData.put(senderAddress, new BroadcastInfo(receivedSystemName, currentTime));

                    // Notify that broadcast data has changed
                    notifyBroadcastDataChanged();
                } else {
                    BroadcastInfo existingInfo = broadcastData.get(senderAddress);
                    existingInfo.updateLastBroadcastTime(currentTime);
                }
            }
        } catch (Exception e) {
            LOG.error("Error receiving broadcast: {}", e.getMessage());
        }
    }

    public synchronized void startBroadcastListener() {
        if (broadcastListenerThread != null) {
            LOG.info("Broadcast listener already started");
            return;
        }

        shouldListen = true;

        broadcastListenerThread = new Thread(this::listenForBroadcast);

        broadcastListenerThread.start();

        // Start the thread that removes stale entries
        startStaleEntriesRemover();
    }

    public synchronized void stopBroadcastListener() {
        new Thread(() -> {
            if (broadcastListenerThread == null) {
                LOG.info("Broadcast listener not running");
                return;
            }

            shouldListen = false;

            try {
                broadcastListenerThread.join(); // Wait for the listener thread to stop
            } catch (InterruptedException e) {
                LOG.error("Failed to stop broadcast listener: {}", e.getMessage(), e);
            }

            // Close the DatagramSocket to release the port
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }

            LOG.info("Broadcast listener stopped");
            broadcastListenerThread = null;

            stopStaleEntriesRemover();
        }).start();
    }

    public synchronized void startStaleEntriesRemover() {
        if (staleEntriesRemoverThread != null) {
            LOG.info("Stale entries remover already started");
            return;
        }

        staleEntriesRemoverThread = new Thread(() -> {
            while (shouldListen) { // Stop checking if shouldListen is false
                long currentTime = System.currentTimeMillis();
                boolean dataChanged = false;

                // Iterate over the broadcastData and remove stale entries
                for (InetAddress ip : new ArrayList<>(broadcastData.keySet())) {
                    BroadcastInfo info = broadcastData.get(ip);
                    if (currentTime - info.getLastBroadcastTime() > 10000) { // More than 10 seconds have passed
                        LOG.info("Removing stale broadcast data for IP: {}", ip.getHostAddress());
                        broadcastData.remove(ip);
                        dataChanged = true;
                    }
                }

                if (dataChanged) {
                    notifyBroadcastDataChanged();
                }

                try {
                    Thread.sleep(2000); // Check every 2 seconds
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        });

        staleEntriesRemoverThread.start();
    }

    public synchronized void stopStaleEntriesRemover() {
        if (staleEntriesRemoverThread == null) {
            LOG.info("Stale entries remover not running");
            return;
        }

        shouldListen = false; // This will stop the stale entries remover loop

        try {
            staleEntriesRemoverThread.join(); // Wait for the stale entries remover thread to stop
        } catch (InterruptedException e) {
            LOG.error("Failed to stop stale entries remover: {}", e.getMessage(), e);
        }

        LOG.info("Stale entries remover stopped");
        staleEntriesRemoverThread = null;
    }

}
