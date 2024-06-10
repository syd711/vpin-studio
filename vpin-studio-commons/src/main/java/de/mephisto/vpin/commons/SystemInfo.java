package de.mephisto.vpin.commons;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;

public class SystemInfo {
  private final static Logger LOG = LoggerFactory.getLogger(SystemInfo.class);
  public static String RESOURCES = "./resources/";

  public final static String PINUP_SYSTEM_INSTALLATION_DIR_INST_DIR = "pinupSystem.installationDir";
  public final static String ARCHIVE_TYPE = "archive.type";

  private final static String VPX_REG_KEY = "HKEY_CURRENT_USER\\SOFTWARE\\Visual Pinball\\VP10\\RecentDir";
  private final static String VPX_REG_KEY_2 = "HKEY_LOCAL_MACHINE\\SOFTWARE\\Microsoft\\Windows\\CurrentVersion\\App Paths\\VPinballX.exe";

  private final static String POPPER_REG_KEY = "HKEY_LOCAL_MACHINE\\SYSTEM\\ControlSet001\\Control\\Session Manager\\Environment";
  public final static String VPIN_SERVER_REG_KEY = "SOFTWARE\\WOW6432Node\\Microsoft\\Windows\\CurrentVersion\\Run\\VPin Studio Server";


  @NonNull
  public File resolvePinUPSystemInstallationFolder() {
    try {
      String popperInstDir = System.getenv("PopperInstDir");
      if (!StringUtils.isEmpty(popperInstDir)) {
        return new File(popperInstDir, "PinUPSystem");
      }

      String output = readRegistry(POPPER_REG_KEY, "PopperInstDir");
      if (output != null && output.trim().length() > 0) {
        String path = extractRegistryValue(output);
        File folder = new File(path, "PinUPSystem");
        if (folder.exists()) {
          return folder;
        }
        LOG.error("Found registry entry for " + POPPER_REG_KEY + ", but that folder does not exist. I'm using the default installation folder instead.");
      }
    } catch (Exception e) {
      LOG.error("Failed to read installation folder: " + e.getMessage(), e);
    }
    return new File("C:/vPinball/PinUPSystem");
  }

  @NonNull
  public File resolveVisualPinballInstallationFolder(@NonNull File pinUPSystemInstallationFolder) {
    File file = new File(pinUPSystemInstallationFolder.getParent(), "VisualPinball");
    if (!file.exists()) {
      file = new File(pinUPSystemInstallationFolder.getParent(), "Visual Pinball");
    }
    if (!file.exists()) {
      LOG.info("The system info could not derive the Visual Pinball installation folder from the PinUP Popper installation, checking windows registry next.");
      file = resolveVisualPinballInstallationFolder();
    }
    return file;
  }
  public File resolveVisualPinballInstallationFolder() {
    String tablesDir = readRegistry(VPX_REG_KEY, "LoadDir");
    if (tablesDir != null) {
      tablesDir = extractRegistryValue(tablesDir);
      if (tablesDir != null) {
        LOG.info("Resolve Visual Pinball tables folder " + tablesDir);
        File file = new File(tablesDir);
        if (file.exists()) {
          return file.getParentFile();
        }
      }
    }
    // else
    String vpxDir = readRegistry(VPX_REG_KEY_2, null);
    if (vpxDir!=null) {
      vpxDir = extractRegistryValue(vpxDir);
      LOG.info("Resolve Visual Pinball tables folder " + vpxDir);
      String exe = "VPinballX.exe";
      if (StringUtils.endsWithIgnoreCase(vpxDir, exe)) {
        File file = new File(StringUtils.removeEndIgnoreCase(vpxDir, exe));
        if (file.exists()) {
          return file;
        }
      }
    }
    return null;
  }

  public File resolveUserFolder(@NonNull File visualPinballInstallationFolder) {
    return new File(visualPinballInstallationFolder, "User/");
  }

  public File resolveMameInstallationFolder(@NonNull File visualPinballInstallationFolder) {
    return new File(visualPinballInstallationFolder, "VPinMAME/");
  }

  public File resolveVpxTablesInstallationFolder(@NonNull File visualPinballInstallationFolder) {
    return new File(visualPinballInstallationFolder, "Tables/");
  }

  public String readRegistry(String location, String key) {
    try {
      // Run reg query, then read output with StreamReader (internal class)
      String cmd = "reg query " + "\"" + location + "\"";
      if (key != null) {
        cmd = "reg query " + '"' + location + "\" /v " + key;
      }
      Process process = Runtime.getRuntime().exec(cmd);
      StreamReader reader = new StreamReader(process.getInputStream());
      reader.start();
      process.waitFor();
      reader.join();
      return reader.getResult();
    } catch (Exception e) {
      LOG.info("Failed to read registry key " + location);
      return null;
    }
  }

  /**
   * REG ADD "HKEY_CURRENT_USER\SOFTWARE\Freeware\Visual PinMame\tz_94ch" /v sound_mode /t REG_DWORD /d 1 /f
   *
   * @param location
   * @param key
   * @param value
   */
  public void writeRegistry(String location, String key, int value) {
    try {
      String cmd = "REG ADD \"" + location + "\" /v " + key + " /t REG_DWORD /d " + value + " /f";
      Process process = Runtime.getRuntime().exec(cmd);
      StreamReader reader = new StreamReader(process.getInputStream());
      reader.start();
      process.waitFor();
      reader.join();
    } catch (Exception e) {
      LOG.error("Failed to write registry key " + location + ": " + e.getMessage(), e);
    }
  }

  @Nullable
  public String extractRegistryValue(String output) {
    String result = output;
    result = result.replace("\n", "").replace("\r", "").trim();

    String[] s = result.split("    ");
    if (s.length >= 4) {
      return s[3];
    }
    return null;
  }

  static class StreamReader extends Thread {
    private InputStream is = null;
    private final StringWriter sw = new StringWriter();

    public StreamReader(InputStream is) {
      this.is = is;
    }

    public void run() {
      try {
        int c;
        while ((c = is.read()) != -1)
          sw.write(c);
      } catch (IOException e) {
        LOG.error("Failed to execute stream reader: " + e.getMessage(), e);
      }
    }

    public String getResult() {
      return sw.toString();
    }
  }
}
