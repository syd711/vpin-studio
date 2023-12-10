package de.mephisto.vpin.ui;

import de.mephisto.vpin.commons.utils.Updater;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Arrays;
import java.util.List;

public class StudioUpdatePreProcessing {
  private final static Logger LOG = LoggerFactory.getLogger(StudioUpdatePreProcessing.class);
  private final static List<String> resources = Arrays.asList("update-runtime.bat");

  public static void execute() {
    new Thread(() -> {
      Thread.currentThread().setName("Studio Resource Updater");
      runResourcesCheck();
      LOG.info("Finished resource updates check.");
    }).start();
  }

  private static void runResourcesCheck() {
    for (String resource : resources) {
      File check = new File("./resources", resource);
      if (!check.exists()) {
        LOG.info("Downloading missing resource file " + check.getAbsolutePath());
        Updater.download("https://github.com/syd711/vpin-studio/blob/main/resources/" + resource + "?raw=true", check);
      }
    }
  }
}
