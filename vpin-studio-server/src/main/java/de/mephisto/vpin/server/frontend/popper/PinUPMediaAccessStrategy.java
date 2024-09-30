package de.mephisto.vpin.server.frontend.popper;

import de.mephisto.vpin.restclient.frontend.VPinScreen;
import de.mephisto.vpin.server.frontend.DefaultMediaAccessStrategy;

import java.io.File;

public class PinUPMediaAccessStrategy extends DefaultMediaAccessStrategy {

  @Override
  public File getScreenMediaFolder(File mediaDirectory, String gameFileName, VPinScreen screen) {
    return new File(mediaDirectory, screen.name());
  }
}
