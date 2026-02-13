package de.mephisto.vpin.server;

import com.sun.jna.NativeLibrary;
import de.mephisto.vpin.commons.StudioMediaPlayer;
import de.mephisto.vpin.commons.utils.Updater;
import de.mephisto.vpin.restclient.system.NVRamsInfo;
import de.mephisto.vpin.restclient.system.ScoringDB;
import de.mephisto.vpin.restclient.util.PackageUtil;
import net.sf.sevenzipjbinding.SevenZip;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.*;
import java.util.List;

import static de.mephisto.vpin.server.system.SystemService.RESOURCES;

public class ServerUpdatePreProcessing {
  private final static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
  private final static List<String> deletions = Arrays.asList("PupPackScreenTweaker.exe");

  private final static List<String> jvmFiles = Arrays.asList("jinput-dx8_64.dll");

  private final static String GITHUB_RESOURCES_URL = "https://raw.githubusercontent.com/syd711/vpin-studio/main/resources/";

  private final static Map<String, String> DOWNLOADS = new LinkedHashMap<>();

  static {
    DOWNLOADS.put("PinVol.exe", GITHUB_RESOURCES_URL + "PinVol.exe");
    DOWNLOADS.put("ffmpeg.exe", GITHUB_RESOURCES_URL + "ffmpeg.exe");
    DOWNLOADS.put("jptch.exe", GITHUB_RESOURCES_URL + "jptch.exe");
    DOWNLOADS.put("nircmd.exe", GITHUB_RESOURCES_URL + "nircmd.exe");
    DOWNLOADS.put("downloader.vbs", GITHUB_RESOURCES_URL + "downloader.vbs");
    DOWNLOADS.put("puppacktweaker/PupPackScreenTweaker.exe", GITHUB_RESOURCES_URL + "puppacktweaker/PupPackScreenTweaker.exe");
    DOWNLOADS.put("puplauncher.exe", GITHUB_RESOURCES_URL + "puplauncher.exe");
    DOWNLOADS.put("vpxtool.exe", GITHUB_RESOURCES_URL + "vpxtool.exe");
    DOWNLOADS.put("maintenance.mp4", GITHUB_RESOURCES_URL + "maintenance.mp4");
    DOWNLOADS.put(ScoringDB.SCORING_DB_NAME, GITHUB_RESOURCES_URL + ScoringDB.SCORING_DB_NAME);
    DOWNLOADS.put("manufacturers/manufacturers.zip", GITHUB_RESOURCES_URL + "manufacturers/manufacturers.zip");
    DOWNLOADS.put("logos.txt", GITHUB_RESOURCES_URL + "logos.txt");
    DOWNLOADS.put("competition-badges/wovp.png", GITHUB_RESOURCES_URL + "competition-badges/wovp.png");
    DOWNLOADS.put("frames/wheel-black.png", GITHUB_RESOURCES_URL + "frames/wheel-black.png");
    DOWNLOADS.put("frames/wheel-tarcissio.png", GITHUB_RESOURCES_URL + "frames/wheel-tarcissio.png");
    DOWNLOADS.put("resources/vlc/", "https://download.videolan.org/pub/videolan/vlc/last/win64/vlc-3.0.23-win64.zip");
  }

  private final static Map<String, Long> PUP_GAMES = new HashMap<>();

  static {
    PUP_GAMES.put("pinball_fx.json", 229247L);
    PUP_GAMES.put("pinball_fx3.json", 152207L);
    PUP_GAMES.put("zaccaria.json", 209785L);
    PUP_GAMES.put("pinball_m.json", 11143L);
  }

  public static void execute() {
    init7zip();

    new Thread(() -> {
      try {
        Thread.currentThread().setName("ServerUpdatePreProcessing");
        long start = System.currentTimeMillis();


        runJvmCheck();
        runScriptCheck();
        runDeletionChecks();
        runPinVolUpdateCheck();
        runVpxToolsUpdateCheck();
        runLogosUpdateCheck();
        runDOFTesterCheck();
        runPupGamesUpdateCheck();
        runDownloadableInstallationsCheck();
        runVlcCheck();
        runDeletions();


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

  private static void runVlcCheck() {
    try {
      String vlcPath = new File(RESOURCES, "vlc/vlc-3.0.23/").getAbsolutePath();
      // Add to library search path
      NativeLibrary.addSearchPath("libvlc", vlcPath);
      NativeLibrary.addSearchPath("libvlccore", vlcPath);
      LOG.info("VLC player initialized.");

      StudioMediaPlayer.runVLCCheck();
    }
    catch (Exception e) {
      LOG.error("Failed to initialize VLC: {}", e.getMessage(), e);
    }
  }

  private static void runDownloadableInstallationsCheck() {
    for (Map.Entry<String, String> entry : DOWNLOADS.entrySet()) {
      String key = entry.getKey();
      String url = entry.getValue();

      if (key.endsWith("/")) {
        // Folder-based download: download zip and extract into folder
        File folder = new File(key);
        if (!folder.exists() || folder.listFiles().length == 0) {
          LOG.info("Starting installation of {}", url);
          folder.mkdirs();
          String fileName = new File(url).getName();
          File targetFile = new File(folder, fileName);
          ServerUpdatePreProcessorUI.downloadWithProgressDialog(url, targetFile, folder);
        }
      }
      else {
        // File-based download: download to RESOURCES/<key>
        File check = new File(RESOURCES, key);
        if (!check.exists()) {
          if (!check.getParentFile().exists()) {
            check.getParentFile().mkdirs();
          }
          LOG.info("Downloading missing resource file {}", check.getAbsolutePath());
          ServerUpdatePreProcessorUI.downloadWithProgressDialog(url, check, null);
          if (FilenameUtils.getExtension(check.getName()).equalsIgnoreCase("zip")) {
            PackageUtil.unpackTargetFolder(check, check.getParentFile(), null, Collections.emptyList(), null);
          }
        }
      }
    }
  }

  private static void runDeletions() {
    for (String deletion : deletions) {
      File check = new File(RESOURCES, deletion);
      if (check.exists() && !check.delete()) {
        LOG.error("Failed to clean up file: " + check.getAbsolutePath());
      }
    }
  }

  private static void runDeletionChecks() {

  }

  private static void runPinVolUpdateCheck() {
    long expectedSize = 1103872;
    File check = new File(RESOURCES, "PinVol.exe");
    if (check.exists()) {
      long size = check.length();
      if (expectedSize != size) {
        LOG.info("Outdated PinVol.exe found, updating...");
        Updater.downloadAndOverwrite("https://raw.githubusercontent.com/syd711/vpin-studio/main/resources/PinVol.exe", check, true);
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
        Updater.downloadAndOverwrite("https://raw.githubusercontent.com/syd711/vpin-studio/main/resources/vpxtool.exe", check, true);
      }
    }
  }

  private static void runDOFTesterCheck() {
    File testerFolder = new File(RESOURCES, "DOFTest");
    if (!testerFolder.exists()) {
      testerFolder.mkdirs();
    }
    List<String> dofTesterFileNames = Arrays.asList("DirectOutput.dll", "DirectOutputComObject.dll", "DirectOutputTest.exe", "DirectOutputTest.exe.config", "DirectOutputTest.pdb", "Readme.txt");
    for (String dofTesterFileName : dofTesterFileNames) {
      File check = new File(testerFolder, dofTesterFileName);
      if (!check.exists()) {
        LOG.info("Outdated {} found, updating...", check.getName());
        Updater.download("https://raw.githubusercontent.com/syd711/vpin-studio/main/resources/DOFTest/" + dofTesterFileName, check);
      }
    }
  }

  private static void runLogosUpdateCheck() {
    long expectedSize = 119856;
    File check = new File(RESOURCES, "logos.txt");
    if (!check.exists() || expectedSize != check.length()) {
      LOG.info("Outdated logos.txt found, updating...");
      Updater.downloadAndOverwrite("https://raw.githubusercontent.com/syd711/vpin-studio/main/resources/logos.txt", check, true);
    }
  }

  private static void runPupGamesUpdateCheck() {
    for (Map.Entry<String, Long> entry : PUP_GAMES.entrySet()) {
      File check = new File(RESOURCES, "pupgames/" + entry.getKey());
      long expectedSize = entry.getValue();
      if (!check.exists() || check.length() != expectedSize) {
        LOG.info("Outdated pupgames file {}/({}) found, updating...", entry.getKey(), check.length() + "/" + expectedSize);
        check.getParentFile().mkdirs();
        Updater.downloadAndOverwrite("https://raw.githubusercontent.com/syd711/vpin-studio/main/resources/pupgames/" + entry.getKey(), check, true);
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
      LOG.info("Initializing 7z.");
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
          LOG.info("Downloaded nvram file {}", nvramFile.getAbsolutePath());
        }
      }
      LOG.info("Finished NVRam synchronization, there are currently {} resetted nvrams available.", nvRams.size());
    }
    catch (IOException e) {
      LOG.error("Failed to sync nvrams: {}", e.getMessage(), e);
    }

    return info;
  }
}
