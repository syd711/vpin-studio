package de.mephisto.vpin.vps;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VPSBotLauncher {
  private final static Logger LOG = LoggerFactory.getLogger(VPSBotLauncher.class);
  private final VPSBot vpsBot;

  VPSBotLauncher() throws InterruptedException {
    vpsBot = new VPSBot();
    Thread printingHook = new Thread(() -> {
      vpsBot.shutdown();
    });
    Runtime.getRuntime().addShutdownHook(printingHook);
  }

  public static void main(String[] args) throws InterruptedException {
    VPSBotLauncher vpsBotLauncher = new VPSBotLauncher();
    synchronized (vpsBotLauncher) {
      vpsBotLauncher.wait();
    }
  }
}
