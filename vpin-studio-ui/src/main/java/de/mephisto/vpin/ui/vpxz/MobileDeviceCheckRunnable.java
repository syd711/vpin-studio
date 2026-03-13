package de.mephisto.vpin.ui.vpxz;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;

public class MobileDeviceCheckRunnable implements Runnable {
  private final static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private final VPXZController vpxzController;
  private boolean paused;

  public MobileDeviceCheckRunnable(VPXZController vpxzController) {
    this.vpxzController = vpxzController;
  }

  @Override
  public void run() {
    if (!paused) {
      this.vpxzController.refreshConnection();
    }
  }

  public void setPaused(boolean b) {
    this.paused = b;
  }
}