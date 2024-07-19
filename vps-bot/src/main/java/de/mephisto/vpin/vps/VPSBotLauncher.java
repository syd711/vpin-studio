package de.mephisto.vpin.vps;

public class VPSBotLauncher {
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
