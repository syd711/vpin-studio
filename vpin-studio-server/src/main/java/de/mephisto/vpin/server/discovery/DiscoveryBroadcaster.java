package de.mephisto.vpin.server.discovery;

import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.server.preferences.PreferencesService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Service;

import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Enumeration;

@Service
public class DiscoveryBroadcaster implements InitializingBean {
    private final static Logger LOG = LoggerFactory.getLogger(DiscoveryBroadcaster.class);
    private final PreferencesService preferencesService;

    private Thread thread;
    private boolean shouldRun = false;

    public DiscoveryBroadcaster(PreferencesService preferencesService) {
        this.preferencesService = preferencesService;
    }

    public void stop() {
        LOG.info("Trying to stop the {}",DiscoveryBroadcaster.class.getName());

        synchronized (this) {
            LOG.info("{} wasn't running", DiscoveryBroadcaster.class.getName());

            if (thread == null)
                return;

            shouldRun = false;
            this.notify();
        }

        try {
            thread.join();
        } catch (InterruptedException e) {
            LOG.error("Failed to stop {}: {}", DiscoveryBroadcaster.class.getName(), e.getMessage(), e);
        }

        LOG.info("{} stopped", DiscoveryBroadcaster.class.getName());

        thread = null;
    }

    public synchronized void start() {
        if (thread != null) {
            LOG.info("{} already started", DiscoveryBroadcaster.class.getName());
            return;
        }

        shouldRun = true;

        LOG.info("Starting {} thread", DiscoveryBroadcaster.class.getName());

        thread = new Thread(new Runnable() {
            private DatagramSocket socket;

            @Override
            public void run() {
                // The system name can be null. If it was not previously set we'll use the server address.
                String systemName = (String) preferencesService.getPreferenceValue(PreferenceNames.SYSTEM_NAME);
                if (systemName == null) {
                    systemName = "";

                    try {
                        InetAddress localHost = InetAddress.getLocalHost();
                        systemName = localHost.getHostName();
                    } catch(Exception e) {
                        //
                    }
                }

                try {
                    socket = new DatagramSocket();

                    byte[] bytes = systemName.getBytes(StandardCharsets.US_ASCII);

                    DatagramPacket packet = new DatagramPacket(bytes, bytes.length);
                    packet.setPort(50505);

                    while (shouldRun) {
                        Enumeration<NetworkInterface> nets = NetworkInterface.getNetworkInterfaces();
                        for (NetworkInterface netint : Collections.list(nets)) {
                            if (!netint.isUp())
                                continue;

                            if (!netint.isLoopback()) {
                                for (InterfaceAddress ifaceAddress : netint.getInterfaceAddresses()) {
                                    InetAddress bcast = ifaceAddress.getBroadcast();

                                    if (bcast != null) {
                                        sendPacket(socket, packet, bcast);
                                    }
                                }
                            }
                        }

                        synchronized (this) {
                            this.wait(5000);
                        }
                    }

                    socket.close();
                } catch (Exception e) {
                    LOG.error("Error while trying to send a {} packet: {}", DiscoveryBroadcaster.class.getName(), e.getMessage(), e);
                }
            }
        });

        thread.start();
    }

    protected void sendPacket(DatagramSocket socket, DatagramPacket packet, InetAddress address) {
        packet.setAddress(address);

        try {
            socket.send(packet);
        } catch (Exception e) {
            LOG.error("Unable to broadcast for {} on {}.", DiscoveryBroadcaster.class.getName(), address, e);
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        start();
    }
}
