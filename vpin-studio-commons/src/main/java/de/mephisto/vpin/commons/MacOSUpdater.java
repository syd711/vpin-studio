package de.mephisto.vpin.commons;

import de.mephisto.vpin.commons.utils.Updater;
import de.mephisto.vpin.restclient.util.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermission;
import java.util.HashSet;
import java.util.Set;


public class MacOSUpdater {
  private final static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private static final String UPDATE_CLIENT_SCRIPT_NAME = "update-client.sh";

  public static void createUpdateScript() {
    try {
      LOG.info("Creating update script: " + UPDATE_CLIENT_SCRIPT_NAME);
      String macWritePath = System.getProperty("MAC_WRITE_PATH");
      String script = Updater.loadTemplate("update-client-macos.sh")
          .replace("{{MAC_WRITE_PATH}}", macWritePath)
          .replace("{{MAC_JAR_PATH}}", System.getProperty("MAC_JAR_PATH"))
          .replace("{{MAC_APP_PATH}}", System.getProperty("MAC_APP_PATH"));
      createScript(UPDATE_CLIENT_SCRIPT_NAME, script);
    }
    catch (Exception e) {
      LOG.error("Failed to create update script: {}", e.getMessage(), e);
    }
  }

  public static void launchUpdateScript() throws Exception {
    LOG.info("Launching update script:" + UPDATE_CLIENT_SCRIPT_NAME);

    ProcessBuilder processBuilder = new ProcessBuilder(System.getProperty("MAC_WRITE_PATH") + UPDATE_CLIENT_SCRIPT_NAME);
    String basePath = System.getProperty("MAC_WRITE_PATH");
    LOG.info("Using macOS base path: {}", basePath);
    processBuilder.directory(new File(basePath));

    Process process = processBuilder.start();
    LOG.info("Starting upgrade process...");

    boolean isRunning = false;
    long startTime = System.currentTimeMillis();
    long maxWaitTime = 5000;

    while (System.currentTimeMillis() - startTime < maxWaitTime) {
      if (process.isAlive()) {
        isRunning = true;
        break;
      }
      Thread.sleep(100);
    }

    if (isRunning) {
      LOG.info("Upgrade process is running.");
    }
    else {
      LOG.warn("Upgrade process did not start successfully within the time limit.");
    }
  }

  private static void createScript(String name, String body) throws IOException {
    try {
      LOG.info("Writing script " + name);
      File file = FileUtils.writeBatch(name, body);

      LOG.info("Bash file created: " + body);

      Set<PosixFilePermission> perms = new HashSet<>();
      perms.add(PosixFilePermission.OWNER_READ);
      perms.add(PosixFilePermission.OWNER_WRITE);
      perms.add(PosixFilePermission.OWNER_EXECUTE);
      Files.setPosixFilePermissions(file.toPath(), perms);
      LOG.info("Applied execute permissions to: " + file.getAbsolutePath());
    }
    catch (Exception e) {
      LOG.error("Failed to create script file: {}", e.getMessage(), e);
    }
  }

  public static void UpdateAppVersion(String appVersion, String newVersion) throws IOException {
    String cfgfilePath = System.getProperty("MAC_JAR_PATH") + "/VPin-Studio.cfg";
    String pListfilePath = System.getProperty("MAC_JAR_PATH") + "/../Info.plist";
    try {
      ReplaceTextinFile(cfgfilePath, "3.12.8", newVersion);
      ReplaceTextinFile(pListfilePath, "3.12.8", newVersion);
      ReplaceTextinFile(cfgfilePath, appVersion, newVersion);
      LOG.info("Mac Updater: Incremented app version from " + appVersion + " to " + newVersion + " in " + cfgfilePath);
      ReplaceTextinFile(pListfilePath, appVersion, newVersion);
      LOG.info("Mac Updater: Incremented app version from " + appVersion + " to " + newVersion + " in " + pListfilePath);
    }
    catch (IOException e) {
      LOG.error("Failed to increment mac app version: {}", e.getMessage(), e);
    }
  }

  public static void ReplaceTextinFile(String PATH, String oldText, String newText) throws IOException {
    try {
      String fileContent = Files.readString(Paths.get(PATH));
      fileContent = fileContent.replaceAll(oldText, newText);
      Files.writeString(Paths.get(PATH), fileContent);
      LOG.info("Replaced Text in File: " + oldText + " to " + newText + " in " + PATH);
    }
    catch (IOException e) {
      LOG.error("Error replacing Text in File: {}", e.getMessage(), e);
    }
  }
}
