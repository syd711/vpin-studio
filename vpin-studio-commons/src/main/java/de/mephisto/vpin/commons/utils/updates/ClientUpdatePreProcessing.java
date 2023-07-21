package de.mephisto.vpin.commons.utils.updates;

import de.mephisto.vpin.commons.utils.Updater;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Arrays;
import java.util.List;

public class ClientUpdatePreProcessing {
  private final static Logger LOG = LoggerFactory.getLogger(ClientUpdatePreProcessing.class);
  private static List<String> resources = Arrays.asList("VPSaveEdit.exe");

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
  }
}
