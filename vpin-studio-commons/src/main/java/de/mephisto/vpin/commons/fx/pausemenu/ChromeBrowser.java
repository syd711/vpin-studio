package de.mephisto.vpin.commons.fx.pausemenu;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.lang.invoke.MethodHandles;

public class ChromeBrowser extends Browser {
  private final static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  @Override
  public File getBrowserExe() {
    File chromeExe = new File("C:\\Program Files (x86)\\Google\\Chrome\\Application\\chrome.exe");
    if (!chromeExe.exists()) {
      chromeExe = new File("C:\\Program Files\\Google\\Chrome\\Application\\chrome.exe");
      if (!chromeExe.exists()) {
        LOG.error("Chrome installation not found: " + chromeExe.getAbsolutePath());
        return null;
      }
    }
    return chromeExe;
  }
}
