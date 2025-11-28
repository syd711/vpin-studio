package de.mephisto.vpin.commons.utils.scripts;

import java.io.File;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermission;
import java.util.HashSet;
import java.util.Set;
import java.nio.charset.StandardCharsets;

import de.mephisto.vpin.restclient.util.OSUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import de.mephisto.vpin.restclient.util.FileUtils;


public class MacOS {
  private final static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

private static final String LOG_PATH = System.getProperty("MAC_WRITE_PATH") + "Logs";
  private static final String UPDATE_CLIENT_SCRIPT_NAME =  "update-client.sh";
  private static final String UPDATE_CLIENT_SCRIPT = String.join("\n",
      "#!/bin/sh",
      "sleep 4",
      "echo \"Unzipping jar...\" >> '" + LOG_PATH+ "/vpin-studio-ui.log' 2>&1",
      "unzip -o '" + System.getProperty("MAC_WRITE_PATH") + "/vpin-studio-ui-jar.zip' -d '" + System.getProperty("MAC_WRITE_PATH") + "_updatefolder' >> '" + LOG_PATH+ "/vpin-studio-ui.log' 2>&1",
      "echo \"Removing Zip...\" >> '" + LOG_PATH+ "/vpin-studio-ui.log' 2>&1",
      "rm vpin-studio-ui-jar.zip >> '" + LOG_PATH+ "/vpin-studio-ui.log' 2>&1",
      "echo \"Closing App...\" >> '" + LOG_PATH+ "/vpin-studio-ui.log' 2>&1",
      "killall VPin-Studio >> '" + LOG_PATH+ "/vpin-studio-ui.log' 2>&1",
      "echo \"Moving Jar...\" >> '" + LOG_PATH+ "/vpin-studio-ui.log' 2>&1",
      "cp -vf '" + System.getProperty("MAC_WRITE_PATH") + "_updatefolder/vpin-studio-ui.jar' '" + System.getProperty("MAC_JAR_PATH") + "' >>  '" + LOG_PATH+ "/vpin-studio-ui.log' 2>&1",
      "echo \"Removing _updatefolder...\" >> '" + LOG_PATH+ "/vpin-studio-ui.log' 2>&1",
      "rm -rf '" + System.getProperty("MAC_WRITE_PATH") + "_updatefolder' >>  '" + LOG_PATH+ "/vpin-studio-ui.log' 2>&1",
      "echo \"Restarting client...\" >>  '" + LOG_PATH+ "/vpin-studio-ui.log' 2>&1",
      "open -n " + System.getProperty("MAC_APP_PATH")) + " >> '" + LOG_PATH+ "/vpin-studio-ui.log' 2>&1";


  //Removed for App version, no longer needed.

//    public static final String EXEC_CLIENT_SCRIPT_NAME = "VPin-Studio-macosx_aarch64.sh";
//    public static final String EXEC_CLIENT_SCRIPT = String.join("\n",
//            "#!/bin/sh",
//            "if [[ ! -d zulu11.72.19-ca-fx-jre11.0.23-macosx_aarch64 ]];",
//            "then",
//            "\ttar -xvf zulu11.72.19-ca-fx-jre11.0.23-macosx_aarch64.tar.gz",
//            "fi",
//            "",
//            "./zulu11.72.19-ca-fx-jre11.0.23-macosx_aarch64/zulu-11.jre/Contents/Home/bin/java -jar vpin-studio-ui.jar");

  public static void createUpdateScript() {
    try {
      LOG.info("Creating update script:" + UPDATE_CLIENT_SCRIPT_NAME);
      createScript(UPDATE_CLIENT_SCRIPT_NAME, UPDATE_CLIENT_SCRIPT);
    }
    catch (Exception e) {
      LOG.error("Failed to create update script: {}", e.getMessage(), e);
    }
  }

  public static void launchUpdateScript() throws Exception {
    // Use ProcessBuilder to run the new script
    LOG.info("Launching update script:" + UPDATE_CLIENT_SCRIPT_NAME);

    ProcessBuilder processBuilder = new ProcessBuilder(System.getProperty("MAC_WRITE_PATH") +UPDATE_CLIENT_SCRIPT_NAME);
    String basePath = System.getProperty("MAC_WRITE_PATH");
    LOG.info("Using macOS base path: {}", basePath);
    processBuilder.directory(new File(basePath));

    // Start the new process
    Process process = processBuilder.start();
    LOG.info("Starting upgrade process...");

    // Wait for the new process to be fully up and running
    boolean isRunning = false;
    long startTime = System.currentTimeMillis();
    long maxWaitTime = 5000; // Maximum wait time in milliseconds

    while (System.currentTimeMillis() - startTime < maxWaitTime) {
      if (process.isAlive()) {
        isRunning = true;
        break;
      }
      // Optional: Small sleep to avoid busy waiting
      Thread.sleep(100); // Check every 100 milliseconds
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

      LOG.info("Applied execute permissions to: " + file.getAbsolutePath());
    }
    catch (Exception e) {
      LOG.error("Failed to create script file: {}", e.getMessage(), e);
    }
  }

  public static void UpdateAppVersion(String appVersion,String newVersion) throws IOException  {
    String cfgfilePath = System.getProperty("MAC_JAR_PATH") + "/VPin-Studio.cfg";
    String pListfilePath = System.getProperty("MAC_JAR_PATH") + "/../Info.plist";
    try {
      //Update patch for people who updated from 3.12.8 to anything higher.
      // Update worked but APP bundle version was not incremented.
      // Since we are always expecting the "current version" to be in those files, it won't ever find it,
      // so we need to update it if it's there
      ReplaceTextinFile(cfgfilePath, "3.12.8", newVersion);
      ReplaceTextinFile(pListfilePath, "3.12.8", newVersion);
      ReplaceTextinFile(cfgfilePath, appVersion, newVersion);
      LOG.info("Mac Updater: Incremented app version from " + appVersion + " to " + newVersion + " in " + cfgfilePath);
      ReplaceTextinFile(pListfilePath, appVersion, newVersion);
      LOG.info("Mac Updater: Incremented app version from " + appVersion  + " to " + newVersion + " in " + pListfilePath);
    } catch (IOException e) {
      LOG.error("Failed to increment mac app version: {}", e.getMessage(), e);
    }
  }

  public static void ReplaceTextinFile(String PATH, String oldText, String newText) throws IOException  {
    try {
      String fileContent = new String(Files.readAllBytes(Paths.get(PATH)), StandardCharsets.UTF_8);
      fileContent = fileContent.replaceAll(oldText, newText);
      Files.write(Paths.get(PATH), fileContent.getBytes(StandardCharsets.UTF_8));
      LOG.info("Replaced Text in File: " + oldText  + " to " + newText + " in " + PATH);
    } catch (IOException e) {
      LOG.error("Error replacing Text in File: {}", e.getMessage(), e);
    }
  }
}




