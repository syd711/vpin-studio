package de.mephisto.vpin.server;

import de.mephisto.vpin.commons.utils.Updater;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Collections;
import java.util.List;

public class ServerUpdatePreProcessing {
  private final static Logger LOG = LoggerFactory.getLogger(ServerUpdatePreProcessing.class);
  private final static List<String> resources = Collections.emptyList();

  public static void execute() {
    runResourcesCheck();
  }

  private static void runResourcesCheck() {
    for (String resource : resources) {
      File check = new File("resources", resource);
      if (!check.exists()) {
        LOG.info("Downloading missing resource file " + check.getAbsolutePath());
        Updater.download("https://raw.githubusercontent.com/syd711/vpin-studio/main/resources/" + resource, check);
      }
    }
    LOG.info("Finished resource updates check.");
  }
}
