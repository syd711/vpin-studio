package de.mephisto.vpin.commons.utils.scripts;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.attribute.PosixFilePermission;
import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.mephisto.vpin.commons.utils.FileUtils;

public class MacOS {
    private final static Logger LOG = LoggerFactory.getLogger(MacOS.class);

    private static final String UPDATE_CLIENT_SCRIPT_NAME = "update-client.sh";
    private static final String UPDATE_CLIENT_SCRIPT = String.join("\n",
            "#!/bin/sh",
            "sleep 4",
            "unzip -o vpin-studio-ui-jar.zip",
            "rm vpin-studio-ui-jar.zip",
            "./VPin-Studio-macosx_aarch64.sh &");

    public static final String EXEC_CLIENT_SCRIPT_NAME = "VPin-Studio-macosx_aarch64.sh";
    public static final String EXEC_CLIENT_SCRIPT = String.join("\n",
            "#!/bin/sh",
            "if [[ ! -d zulu11.72.19-ca-fx-jre11.0.23-macosx_aarch64 ]];",
            "then",
            "\ttar -xvf zulu11.72.19-ca-fx-jre11.0.23-macosx_aarch64.tar.gz",
            "fi",
            "",
            "./zulu11.72.19-ca-fx-jre11.0.23-macosx_aarch64/zulu-11.jre/Contents/Home/bin/java -jar vpin-studio-ui.jar");

    public static void createUpdateScript() {
        try {
            createScript(UPDATE_CLIENT_SCRIPT_NAME, UPDATE_CLIENT_SCRIPT);
        } catch (Exception e) {
            LOG.error("Failed to create upgrade script: {}", e.getMessage(), e);
        }
    }

    public static void createExecScript() {
        try {
            createScript(EXEC_CLIENT_SCRIPT_NAME, EXEC_CLIENT_SCRIPT);
        } catch (Exception e) {
            LOG.error("Failed to create exec script: {}", e.getMessage(), e);
        }
    }

    public static void launchUpdateScript() throws Exception {
        // Use ProcessBuilder to run the new script
        ProcessBuilder processBuilder = new ProcessBuilder("./" + UPDATE_CLIENT_SCRIPT_NAME);
        processBuilder.directory(getBasePath());

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
        } else {
            LOG.warn("Upgrade process did not start successfully within the time limit.");
        }
    }

    private static void createScript(String name, String body) throws IOException {
        File file = FileUtils.writeBatch(name, body);
        LOG.info("Bash file created: " + body);

        Set<PosixFilePermission> perms = new HashSet<>();
        perms.add(PosixFilePermission.OWNER_READ);
        perms.add(PosixFilePermission.OWNER_WRITE);
        perms.add(PosixFilePermission.OWNER_EXECUTE);
        Files.setPosixFilePermissions(file.toPath(), perms);

        LOG.info("Applied execute permissions to: " + file.getAbsolutePath());
    }

    private static File getBasePath() {
      return new File("./");
    }
}
