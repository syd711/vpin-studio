package de.mephisto.vpin.server;

import de.mephisto.vpin.restclient.util.SystemUtil;
import de.mephisto.vpin.server.system.SystemService;
import de.mephisto.vpin.server.util.RequestUtil;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.nio.charset.Charset;

public class VPinStudioServerStateManager {
  private final static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private static final String SERVICE_NAME = "vpin-studio-server";
  private static final String SERVICE_JAR = SERVICE_NAME + ".jar";

  public boolean isInstalled() {
    return getAutostartFile().exists();
  }

  public boolean isRunning() {
    return RequestUtil.doGet("http://localhost:" + SystemUtil.getPort() + "/system/ping");
  }

  public boolean shutdown() {
    return RequestUtil.doGet("http://localhost:" + SystemUtil.getPort() + "/system/exit");
  }

  public boolean restart() {
    return RequestUtil.doGet("http://localhost:" + SystemUtil.getPort() + "/system/restart");
  }

  public void install() throws IOException {
    File root = new File("./");
    String script = "cd /D " + root.getAbsolutePath() +
        "\nstart jdk/bin/javaw -jar " + new File(root, SERVICE_JAR).getAbsolutePath();
    FileUtils.writeStringToFile(getAutostartFile(), script, Charset.forName("UTF-8"));
    LOG.info("Written autostart file " + getAutostartFile().getAbsolutePath());
  }

  public boolean uninstall() throws Exception {
    try {
      shutdown();
      Thread.sleep(1000);
      if (!getAutostartFile().delete()) {
        throw new Exception("Failed to delete autostart file " + getAutostartFile().getAbsolutePath());
      }
      LOG.info("Deleted " + getAutostartFile().getAbsolutePath());
    } catch (InterruptedException e) {
      LOG.error("Uninstall failed: " + e.getMessage());
    }
    return false;
  }

  private File getAutostartFile() {
    String path = "C:/Users/%s/AppData/Roaming/Microsoft/Windows/Start Menu/Programs/Startup/" + SERVICE_NAME + ".bat";
    String userName = System.getProperty("user.name");

    String formattedPath = String.format(path, userName);
    return new File(formattedPath);
  }
}
