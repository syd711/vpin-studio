package de.mephisto.vpin.server;

import de.mephisto.vpin.restclient.util.FileUtils;
import de.mephisto.vpin.commons.utils.Updater;
import de.mephisto.vpin.restclient.system.NVRamsInfo;
import de.mephisto.vpin.restclient.system.ScoringDB;
import de.mephisto.vpin.restclient.util.PackageUtil;
import de.mephisto.vpin.server.system.SystemService;
import net.sf.sevenzipjbinding.SevenZip;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static de.mephisto.vpin.server.system.SystemService.RESOURCES;

public class ServerUpdatePreProcessing {
  private final static Logger LOG = LoggerFactory.getLogger(ServerUpdatePreProcessing.class);
  private final static List<String> resources = Arrays.asList("PinVol.exe", "ffmpeg.exe", "jptch.exe", "nircmd.exe", "downloader.vbs", "PupPackScreenTweaker.exe", "puplauncher.exe", "vpxtool.exe", "maintenance.mp4", ScoringDB.SCORING_DB_NAME, "manufacturers/manufacturers.zip");
  private final static List<String> jvmFiles = Arrays.asList("jinput-dx8_64.dll");

  public static void execute() {
    init7zip();

    new Thread(() -> {
      try {
        Thread.currentThread().setName("ServerUpdatePreProcessing");
        long start = System.currentTimeMillis();
        runJvmCheck();
        runScriptCheck();
        runDeletionChecks();
        runResourcesCheck();
        runPinVolUpdateCheck();
        runVpxToolsUpdateCheck();
        runLogosUpdateCheck();

        new Thread(() -> {
          Thread.currentThread().setName("ServerUpdate Async Preprocessor");
          synchronizeNVRams(false);
        }).start();

        LOG.info("Finished resource updates check, took " + (System.currentTimeMillis() - start) + "ms.");
      }
      catch (Exception e) {
        LOG.error("Server update failed: " + e.getMessage(), e);
      }
    }).start();
  }

  private static void runDeletionChecks() {
    File b2sRaw = new File(RESOURCES, "b2s-raw");
    if (b2sRaw.exists() && b2sRaw.isDirectory()) {
      FileUtils.deleteFolder(b2sRaw);
      LOG.info("Deleted " + b2sRaw.getAbsolutePath());
    }

    File b2sCropped = new File(RESOURCES, "b2s-cropped");
    if (b2sCropped.exists() && b2sCropped.isDirectory()) {
      FileUtils.deleteFolder(b2sCropped);
      LOG.info("Deleted " + b2sCropped.getAbsolutePath());
    }
  }

  private static void runPinVolUpdateCheck() {
    long expectedSize = 1103872;
    File check = new File(RESOURCES, "PinVol.exe");
    if (check.exists()) {
      long size = check.length();
      if (expectedSize != size) {
        LOG.info("Outdated PinVol.exe found, updating...");
        Updater.download("https://raw.githubusercontent.com/syd711/vpin-studio/main/resources/PinVol.exe", check);
      }
    }
  }

  private static void runVpxToolsUpdateCheck() {
    long expectedSize = 15362048;
    File check = new File(RESOURCES, "vpxtool.exe");
    if (check.exists()) {
      long size = check.length();
      if (expectedSize != size) {
        LOG.info("Outdated vpxtool.exe found, updating...");
        Updater.download("https://raw.githubusercontent.com/syd711/vpin-studio/main/resources/vpxtool.exe", check);
      }
    }
  }


  private static void runLogosUpdateCheck() {
    long expectedSize = 15362048;
    File check = new File(RESOURCES, "logos.txt");
    if (check.exists()) {
      long size = check.length();
      if (expectedSize != size) {
        LOG.info("Outdated logos.txt found, updating...");
        Updater.download("https://raw.githubusercontent.com/syd711/vpin-studio/main/resources/logos.txt", check);
      }
    }
  }

  private static void runScriptCheck() {
    try {
      File scriptFolder = new File(RESOURCES, "scripts/");
      scriptFolder.mkdirs();

      File emulatorLaunchScript = new File(scriptFolder, "emulator-launch.bat");
      if (!emulatorLaunchScript.exists()) {
        Files.write(emulatorLaunchScript.toPath(), "curl -X POST --data-urlencode \"table=%~1\" http://localhost:8089/service/gameLaunch".getBytes());
      }
      File emulatorExitScript = new File(scriptFolder, "emulator-exit.bat");
      if (!emulatorExitScript.exists()) {
        Files.write(emulatorExitScript.toPath(), "curl -X POST --data-urlencode \"table=%~1\" http://localhost:8089/service/gameExit".getBytes());
      }

      File frontendLaunchScript = new File(scriptFolder, "frontend-launch.bat");
      if (!frontendLaunchScript.exists()) {
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
      File check = new File(RESOURCES, resource);
      if (!check.exists()) {
        check.getParentFile().mkdirs();
        LOG.info("Downloading missing resource file " + check.getAbsolutePath());
        Updater.download("https://raw.githubusercontent.com/syd711/vpin-studio/main/resources/" + resource, check);
        if (FilenameUtils.getExtension(check.getName()).equalsIgnoreCase("zip")) {
          PackageUtil.unpackTargetFolder(check, check.getParentFile(), null, Collections.emptyList(), null);
        }
      }
    }
  }

  public static NVRamsInfo synchronizeNVRams(boolean deleteAll) {
    NVRamsInfo info = new NVRamsInfo();
    try {
      File nvRamIndex = new File(RESOURCES, "index.txt");
      Updater.download("https://raw.githubusercontent.com/syd711/nvrams/main/index.txt", nvRamIndex, true);
      if (!nvRamIndex.exists()) {
        LOG.warn("Skipped nvram sync, download failed.");
        return null;
      }

      FileInputStream in = new FileInputStream(nvRamIndex);
      List<String> nvRams = IOUtils.readLines(in, Charset.defaultCharset());
      in.close();
      nvRamIndex.delete();

      File nvramFolder = new File(RESOURCES, "nvrams/");
      if (!nvramFolder.exists()) {
        nvramFolder.mkdirs();
      }

      for (String nvRam : nvRams) {
        File nvramFile = new File(nvramFolder, nvRam + ".nv");
        if (nvramFile.exists() && deleteAll) {
          if (nvramFile.delete()) {
            LOG.info("Deleted " + nvramFile.getAbsolutePath());
          }
        }

        if (!nvramFile.exists()) {
          info.setCount(info.getCount() + 1);
          Updater.download("https://raw.githubusercontent.com/syd711/nvrams/main/" + nvramFile.getName() + "/" + nvramFile.getName(), nvramFile, true);
          LOG.info("Downloaded nvram file " + nvramFile.getAbsolutePath());
        }
      }
      LOG.info("Finished NVRam synchronization, there are currently " + nvRams.size() + " resetted nvrams available.");
    }
    catch (IOException e) {
      LOG.error("Failed to sync nvrams: " + e.getMessage(), e);
    }

    return info;
  }
}
