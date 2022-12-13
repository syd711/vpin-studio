package de.mephisto.vpin.ui.util;

import de.mephisto.vpin.ui.Studio;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;

public class Services {
  private final static Logger LOG = LoggerFactory.getLogger(Services.class);

  public static File SERVER_EXE = new File("./VPin-Studio-Server.exe");

  public static void install() throws IOException {
    File root = new File("./");
    String script = "cd /D " + root.getAbsolutePath() +
        "\nserver.bat";
    FileUtils.writeStringToFile(getAutostartFile(), script, Charset.forName("UTF-8"));
    LOG.info("Written autostart file " + getAutostartFile().getAbsolutePath());
  }

  public boolean uninstall() throws Exception {
    try {
      Studio.client.shutdown();
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

  public static File getAutostartFile() {
    String path = "C:/Users/%s/AppData/Roaming/Microsoft/Windows/Start Menu/Programs/Startup/vpin-studio-server.bat";
    String userName = System.getProperty("user.name");

    String formattedPath = String.format(path, userName);
    return new File(formattedPath);
  }
}
