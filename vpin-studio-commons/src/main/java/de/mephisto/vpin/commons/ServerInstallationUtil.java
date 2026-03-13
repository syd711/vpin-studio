package de.mephisto.vpin.commons;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.nio.charset.StandardCharsets;

public class ServerInstallationUtil {
  private final static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  public static File SERVER_EXE = new File("./VPin-Studio-Server.exe");
  public static final String VPIN_STUDIO_SERVER_BAT = "VPin-Studio-Server.bat";
  public static File BAT = new File(VPIN_STUDIO_SERVER_BAT);

  public static boolean install() throws IOException {
    try {
      File root = new File("./");
      File vbsFile = new File("./", "server.vbs");
      String script = "wscript \"" + vbsFile.getAbsolutePath() + "\"";

      //do not remove this, it is used by the inno installer from the registry
      File runFile = new File("./", VPIN_STUDIO_SERVER_BAT);
      if (runFile.exists() && !runFile.delete()) {
        throw new IOException("Could not delete existing autostart file " + runFile.getAbsolutePath());
      }

      FileUtils.writeStringToFile(runFile, script, StandardCharsets.UTF_8);
      LOG.info("Written autostart file " + runFile.getAbsolutePath());

      String wbScript = "Dim WShell\nSet WShell = CreateObject(\"WScript.Shell\")\n" +
          "WShell.CurrentDirectory = \"" + root.getAbsolutePath() + "\"\n" +
          "WShell.Run \"VPin-Studio-Server.exe\", 0\n" +
          "Set WShell = Nothing";


      if (vbsFile.exists() && !vbsFile.delete()) {
        LOG.error("Could not delete existing vbs file {}", vbsFile.getAbsolutePath());
        throw new IOException("Could not delete existing vbs file " + vbsFile.getAbsolutePath());
      }
      FileUtils.writeStringToFile(vbsFile, wbScript, StandardCharsets.UTF_8);
      LOG.info("Written vbs file " + vbsFile.getAbsolutePath());

      return runFile.exists();
    }
    catch (Exception e) {
      LOG.error("Failed to install autostart: " + e.getMessage(), e);
      throw e;
    }
  }

//  public static File getAutostartFile() {
//    try {
//      PropertiesStore store = PropertiesStore.create(SystemInfo.RESOURCES, "system");
//      String autostartValue = store.get(SystemInfo.AUTOSTART_DIR);
//      if (!StringUtils.isEmpty(autostartValue)) {
//        File autostartFile = new File(new File(autostartValue), BAT.getName());
//        if (autostartFile.exists()) {
//          return autostartFile;
//        }
//      }
//    } catch (Exception e) {
//      LOG.error("Failed to resolve new autostart folder: " + e.getMessage(), e);
//    }
//
//
//    //use legacy lookup
//    String path = "C:/Users/%s/AppData/Roaming/Microsoft/Windows/Start Menu/Programs/Startup/" + BAT;
//    String userName = System.getProperty("user.name");
//
//    String formattedPath = String.format(path, userName);
//    File autostartFile = new File(formattedPath);
//    if (autostartFile.exists()) {
//      return autostartFile;
//    }
//
//    userName = System.getenv("USERNAME");
//    formattedPath = String.format(path, userName);
//    autostartFile = new File(formattedPath);
//    if (autostartFile.exists()) {
//      return new File(autostartFile, VPIN_STUDIO_SERVER_BAT);
//    }
//
//    String path2 = System.getenv("APPDATA") + "\\Microsoft\\Windows\\Start Menu\\Programs\\Startup";
//    autostartFile = new File(path2);
//    if (autostartFile.exists()) {
//      return new File(autostartFile, VPIN_STUDIO_SERVER_BAT);
//    }
//
//    return new File(System.getProperty("user.home"), VPIN_STUDIO_SERVER_BAT);
//  }
}
