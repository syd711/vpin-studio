package de.mephisto.vpin.commons;

import de.mephisto.vpin.commons.utils.PropertiesStore;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class ServerInstallationUtil {
  private final static Logger LOG = LoggerFactory.getLogger(ServerInstallationUtil.class);

  public static File SERVER_EXE = new File("./VPin-Studio-Server.exe");
  public static final String VPIN_STUDIO_SERVER_BAT = "vpin-studio-server.bat";
  public static File BAT = new File(VPIN_STUDIO_SERVER_BAT);

  public static boolean install() throws IOException {
    try {
      File root = new File("./");
      String script = "cd /D " + root.getAbsolutePath() +
        "\nserver.bat";
      FileUtils.writeStringToFile(getAutostartFile(), script, StandardCharsets.UTF_8);
      LOG.info("Written autostart file " + getAutostartFile().getAbsolutePath());
      return getAutostartFile().exists();
    } catch (IOException e) {
      LOG.error("Failed to install autostart file \"" + getAutostartFile().getAbsolutePath() + "\": " + e.getMessage(), e);
      throw e;
    }
  }

  public static boolean isInstalled() {
    return getAutostartFile().exists();
  }

  public static boolean uninstall() {
    if (!getAutostartFile().delete()) {
      LOG.error("Failed to delete autostart file " + getAutostartFile().getAbsolutePath());
      return false;
    }
    LOG.info("Deleted " + getAutostartFile().getAbsolutePath());
    return !getAutostartFile().exists();
  }

  public static File getAutostartFile() {
    try {
      PropertiesStore store = PropertiesStore.create(SystemInfo.RESOURCES, "system");
      String autostartValue = store.get(SystemInfo.AUTOSTART_DIR);
      if (!StringUtils.isEmpty(autostartValue)) {
        File autostartFile = new File(new File(autostartValue), BAT.getName());
        if (autostartFile.exists()) {
          return autostartFile;
        }
      }
    } catch (Exception e) {
      LOG.error("Failed to resolve new autostart folder: " + e.getMessage(), e);
    }


    //use legacy lookup
    String path = "C:/Users/%s/AppData/Roaming/Microsoft/Windows/Start Menu/Programs/Startup/" + BAT;
    String userName = System.getProperty("user.name");

    String formattedPath = String.format(path, userName);
    File autostartFolder = new File(formattedPath);
    if (autostartFolder.exists()) {
      return autostartFolder;
    }

    userName = System.getenv("USERNAME");
    formattedPath = String.format(path, userName);
    autostartFolder = new File(formattedPath);
    if (autostartFolder.exists()) {
      return new File(autostartFolder, VPIN_STUDIO_SERVER_BAT);
    }

    String path2 = System.getenv("APPDATA") + "\\Microsoft\\Windows\\Start Menu\\Programs\\Startup";
    autostartFolder = new File(path2);
    if (autostartFolder.exists()) {
      return new File(autostartFolder, VPIN_STUDIO_SERVER_BAT);
    }

    return new File(System.getProperty("user.home"), VPIN_STUDIO_SERVER_BAT);
  }
}
