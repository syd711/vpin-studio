package de.mephisto.vpin.commons.fx.pausemenu;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

public class EdgeBrowser extends Browser {
  private final static Logger LOG = LoggerFactory.getLogger(EdgeBrowser.class);

  @Override
  public File getBrowserExe() {
    File exe = new File("C:\\Program Files (x86)\\Microsoft\\Edge\\Application\\msedge.exe");
    if (!exe.exists()) {
      LOG.error("Edge installation not found: " + exe.getAbsolutePath());
      return null;
    }
    return exe;
  }
}
