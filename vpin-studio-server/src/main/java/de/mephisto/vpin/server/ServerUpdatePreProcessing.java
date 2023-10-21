package de.mephisto.vpin.server;

import de.mephisto.vpin.commons.utils.Updater;
import de.mephisto.vpin.connectors.vps.VPS;
import de.mephisto.vpin.server.system.SystemService;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;

public class ServerUpdatePreProcessing {
  private final static Logger LOG = LoggerFactory.getLogger(ServerUpdatePreProcessing.class);
  private final static List<String> resources = Arrays.asList("PinVol.exe", "maintenance.jpg");

  public static void execute() {
    runVPSCheck();
    runResourcesCheck();
    synchronizeNVRams();
    LOG.info("Finished resource updates check.");
  }

  private static void runVPSCheck() {
    try {
      VPS.getInstance().download();
    } catch (Exception e) {
      LOG.error("Failed to update VPS sheet: " + e.getMessage(), e);
    }
  }

  private static void runResourcesCheck() {
    for (String resource : resources) {
      File check = new File(SystemService.RESOURCES, resource);
      if (!check.exists()) {
        LOG.info("Downloading missing resource file " + check.getAbsolutePath());
        Updater.download("https://raw.githubusercontent.com/syd711/vpin-studio/main/resources/" + resource, check);
      }
    }
  }

  private static void synchronizeNVRams() {
    try {
      File nvRamIndex = new File(SystemService.RESOURCES, "index.txt");
      Updater.download("https://raw.githubusercontent.com/syd711/nvrams/main/index.txt", nvRamIndex, true);

      FileInputStream in = new FileInputStream(nvRamIndex);
      List<String> nvRams = IOUtils.readLines(in, Charset.defaultCharset());
      in.close();
      nvRamIndex.delete();

      File nvramFolder = new File(SystemService.RESOURCES, "nvrams/");
      if (!nvramFolder.exists()) {
        nvramFolder.mkdirs();
      }

      for (String nvRam : nvRams) {

        File nvramFile = new File(nvramFolder, nvRam + ".nv");
        if (!nvramFile.exists()) {
          Updater.download("https://raw.githubusercontent.com/syd711/nvrams/main/" + nvramFile.getName() + "/" + nvramFile.getName(), nvramFile, true);
          LOG.info("Downloaded missing nvram file " + nvramFile.getAbsolutePath());
        }
      }
      LOG.info("Finished NVRam synchronization, there are currently " + nvRams.size() + " resetted nvrams available.");
    } catch (IOException e) {
      LOG.error("Failed to sync nvrams: " + e.getMessage(), e);
    }
  }
}
