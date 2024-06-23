package de.mephisto.vpin.server;

import de.mephisto.vpin.commons.utils.FileUtils;
import de.mephisto.vpin.commons.utils.Updater;
import de.mephisto.vpin.restclient.system.ScoringDB;
import de.mephisto.vpin.server.system.SystemService;
import net.sf.sevenzipjbinding.SevenZip;
import net.sf.sevenzipjbinding.SevenZipNativeInitializationException;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;

public class ServerUpdatePreProcessing {
  private final static Logger LOG = LoggerFactory.getLogger(ServerUpdatePreProcessing.class);
  private final static List<String> resources = Arrays.asList("PinVol.exe", "nircmd.exe", "vpxtool.exe", "maintenance.jpg", ScoringDB.SCORING_DB_NAME);
  private final static List<String> jvmFiles = Arrays.asList("jinput-dx8_64.dll", "jinput-raw_64.dll");

  public static void execute() {
    init7zip();

    new Thread(() -> {
      try {
        Thread.currentThread().setName("ServerUpdatePreProcessing");
        runJvmCheck();
        runScriptCheck();
        runResourcesCheck();
        synchronizeNVRams();
        LOG.info("Finished resource updates check.");
      }
      catch (Exception e) {
        LOG.error("Server update failed: " + e.getMessage(), e);
      }
    }).start();
  }

  private static void runScriptCheck() {
    try {
      File scriptFolder = new File(SystemService.RESOURCES, "scripts/");
      scriptFolder.mkdirs();

      File emulatorLaunchScript = new File(scriptFolder, "emulator-launch.bat");
      if(!emulatorLaunchScript.exists()) {
        Files.write(emulatorLaunchScript.toPath(), "curl -X POST --data-urlencode \"table=%1\" http://localhost:8089/service/gameLaunch".getBytes());
      }
      File emulatorExitScript = new File(scriptFolder, "emulator-exit.bat");
      if(!emulatorExitScript.exists()) {
        Files.write(emulatorExitScript.toPath(), "curl -X POST --data-urlencode \"table=%1\" http://localhost:8089/service/gameExit".getBytes());
      }

      File frontendLaunchScript = new File(scriptFolder, "frontend-launch.bat");
      if(!frontendLaunchScript.exists()) {
        Files.write(frontendLaunchScript.toPath(), "curl -X POST --data-urlencode \"system=\" http://localhost:8089/service/frontendLaunch".getBytes());
      }
    }
    catch (Exception e) {
      LOG.error("Failed to scripting: " + e.getMessage());
    }
  }

  private static void init7zip() {
    try {
      File sevenZipTempFolder = new File(System.getProperty("java.io.tmpdir"), "sevenZipServer/");
      sevenZipTempFolder.mkdirs();
      SevenZip.initSevenZipFromPlatformJAR(sevenZipTempFolder);
      LOG.info("7z initialized.");
    }
    catch (Exception e) {
      LOG.error("Failed to initialize sevenzip: " + e.getMessage());
    }
  }

  private static void runJvmCheck() {
    for (String resource : jvmFiles) {
      File folder = new File("win32\\java\\bin\\");
      if (folder.exists()) {
        File check = new File("win32\\java\\bin\\", resource);
        if (!check.exists()) {
          LOG.info("Downloading missing JVM file " + check.getAbsolutePath());
          Updater.download("https://raw.githubusercontent.com/syd711/vpin-studio/main/resources/jvm/" + resource, check);
        }
      }
      else {
        LOG.error("No JVM folder found: " + folder.getAbsolutePath());
      }
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
    }
    catch (IOException e) {
      LOG.error("Failed to sync nvrams: " + e.getMessage(), e);
    }
  }
}
