package de.mephisto.vpin.commons;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.mephisto.vpin.commons.utils.WinRegistry;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

public class SystemInfoWindows {

  private final static Logger LOG = LoggerFactory.getLogger(SystemInfoWindows.class);

  public final static String PINUP_SYSTEM_INSTALLATION_DIR_INST_DIR = "pinupSystem.installationDir";
  public final static String PINBALLX_INSTALLATION_DIR_INST_DIR = "pinballX.installationDir";
  public final static String PINBALLY_INSTALLATION_DIR_INST_DIR = "pinballY.installationDir";
  public final static String STANDALONE_INSTALLATION_DIR_INST_DIR = "visualPinball.installationDir";
  public final static String ARCHIVE_TYPE = "archive.type";

  private final static String VPX_REG_KEY = "HKEY_CURRENT_USER\\SOFTWARE\\Visual Pinball\\VP10\\RecentDir";
  private final static String VPX_REG_KEY_2 = "HKEY_LOCAL_MACHINE\\SOFTWARE\\Microsoft\\Windows\\CurrentVersion\\App Paths\\VPinballX.exe";

  private final static String POPPER_REG_KEY = "HKEY_LOCAL_MACHINE\\SYSTEM\\ControlSet001\\Control\\Session Manager\\Environment";
  public final static String VPIN_SERVER_REG_KEY = "SOFTWARE\\WOW6432Node\\Microsoft\\Windows\\CurrentVersion\\Run\\VPin Studio Server";

  static SystemInfoWindows INSTANCE = new SystemInfoWindows();

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
    }
    catch (Exception e) {
      LOG.error("Failed to read installation folder: " + e.getMessage(), e);
    }
    return new File("C:/vPinball/PinUPSystem");
  }

  public File resolvePinballXInstallationFolder() {
    return resolveFolder("PinballX");
  }

  public File resolvePinballYInstallationFolder() {
    return resolveFolder("PinballY");
  }

  public File resolveVpx64InstallFolder() {
    File exe = resolveVpx64Exe();
    return exe != null? exe.getParentFile() : resolveFolder("Visual Pinball");
  }
  public File resolveVpx64Exe() {
    File f = resolveExe("HKEY_CLASSES_ROOT\\Applications\\VPinballX64.exe\\shell\\open\\command", null);
    if (f == null) {
      f = new File(resolveFolder("Visual Pinball"), "VPinballX64.exe");
      if (!f.exists()) {
        f = resolveVpxExe();
      }
    }
    return f;
  }

  public File resolveVpxExe() {
    File f = resolveExe("HKEY_CLASSES_ROOT\\Applications\\VPinballX.exe\\shell\\open\\command",
        "HKEY_CLASSES_ROOT\\vpx_auto_file\\shell\\edit\\command");
    if (f == null) {
      f = new File(resolveFolder("Visual Pinball"), "VPinballX.exe");
      if (!f.exists()) {
        f = null;
      }
    }
    return f;
  }

  public File resolveVptInstallFolder() {
    File exe = resolveVptExe();
    return exe != null? exe.getParentFile() : resolveFolder("Visual Pinball");
  }
  public File resolveVptExe() {
    File f = resolveExe("HKEY_CLASSES_ROOT\\Applications\\VPinball995.exe\\shell\\open\\command",
        "HKEY_CLASSES_ROOT\\vpt_auto_file\\shell\\edit\\command");
    if (f == null) {
      f = new File(resolveFolder("Visual Pinball"), "VPinball995.exe");
      if (!f.exists()) {
        f = null;
      }
    }
    return f;  
  }

  public File resolveFpInstallFolder() {
    File exe = resolveFpExe();
    return exe != null? exe.getParentFile() : resolveFolder("Future Pinball");
  }
  public File resolveFpExe() {
    File f = resolveExe("HKEY_CLASSES_ROOT\\Future Pinball Table\\Shell\\Open\\Command", null);
    if (f == null) {
      f = new File(resolveFolder("Future Pinball"), "Future Pinball.exe");
      if (!f.exists()) {
        f = null;
      }
    }
    return f;  
  }

  private File resolveFolder(String folderName) {
    File f = new File("C:/vPinball/" + folderName.replace(" ", ""));
    if (!f.exists()) {
      f = new File("C:/vPinball/" + folderName);
      if (!f.exists()) {
        f = new File("C:/" + folderName.replace(" ", ""));
        if (!f.exists()) {
          f = new File("C:/" + folderName);
        }                                                           
      }
    }
    return f;
  }

  private File resolveExe(String regkey, String extkey) {
    File f = regkey != null ? extractExe(regkey) : null;
    return f != null ? f : extkey != null ? extractExe(extkey) : null;
  }

  private File extractExe(String regkey) {
    String vpx = extractRegistryValue(readRegistry(regkey, null));
    if (StringUtils.isNotEmpty(vpx)) {
      int indexOf = vpx.toLowerCase().indexOf(".exe");
      if (indexOf > 0) {
        String exe = StringUtils.removeStart(vpx.substring(0, indexOf + 4), "\"");
        File fexe = new File(exe);
        if (fexe.exists()) {
          return fexe;
        }
      }
    }
    return null;
  }

  /**
   * cf https://github.com/vpinball/b2s-backglass/
   * => b2sbackglassserverregisterapp/b2sbackglassserverregisterapp/formBackglassServerRegApp.vb
   */
  public File resolveBackglassServerFolder() {
    try {
      String b2sClsid = extractRegistryValue(readRegistry("HKEY_CLASSES_ROOT\\B2S.Server\\CLSID", null));
      String regkey = "HKEY_CLASSES_ROOT\\WOW6432Node\\CLSID\\" + b2sClsid + "\\InprocServer32";
      String serverDllPath = extractRegistryValue(readRegistry(regkey, "CodeBase"));
      File serverDllFile = null;
      try {
        serverDllFile = new File(new URL(serverDllPath).getFile());
        if (serverDllFile.exists()) {
          return serverDllFile.getParentFile();
        }
      }
      catch (MalformedURLException ue) {
      }

      // alternative way copied from FrontendService
      Map<String, Object> pathEntry = WinRegistry.getClassesValues(".res\\b2sserver.res\\ShellNew");
      if (!pathEntry.isEmpty()) {
        String path = String.valueOf(pathEntry.values().iterator().next());
        if (path.contains("\"")) {
          path = path.substring(1);
          path = path.substring(0, path.indexOf("\""));
          File exeFile = new File(path);
          File b2sFolder = exeFile.getParentFile();
          if (b2sFolder.exists()) {
            LOG.info("Resolved backglass server directory from registry: " + b2sFolder.getAbsolutePath());
            return b2sFolder;
          }
        }
      }
    }
    catch (Exception e) {
      LOG.error("Failed to calculate backglass server folder: {}", e.getMessage(), e);
    }

    return null;
  }

  private String readRegistry(String location, String key) {
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
    }
    catch (Exception e) {
      LOG.info("Failed to read registry key " + location);
      return null;
    }
  }
  private String readRegistryValue(String location, String key) {
    String reg = readRegistry(location, key);
    return StringUtils.isNotEmpty(reg) ? extractRegistryValue(reg) : null;
  }

  /**
   * REG ADD "HKEY_CURRENT_USER\SOFTWARE\Freeware\Visual PinMame\tz_94ch" /v sound_mode /t REG_DWORD /d 1 /f
   *
   * @param location
   * @param key
   * @param value
   */
  private void writeRegistry(String location, String key, int value) {
    try {
      String cmd = "REG ADD \"" + location + "\" /v " + key + " /t REG_DWORD /d " + value + " /f";
      Process process = Runtime.getRuntime().exec(cmd);
      StreamReader reader = new StreamReader(process.getInputStream());
      reader.start();
      process.waitFor();
      reader.join();
    }
    catch (Exception e) {
      LOG.error("Failed to write registry key " + location + ": " + e.getMessage(), e);
    }
  }

  @Nullable
  private String extractRegistryValue(String output) {
    if (output != null) {
      String result = output;
      result = result.replace("\n", "").replace("\r", "").trim();

      String[] s = result.split("    ");
      if (s.length >= 4) {
        return s[3];
      }
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
      }
      catch (IOException e) {
        LOG.error("Failed to execute stream reader: " + e.getMessage(), e);
      }
    }

    public String getResult() {
      return sw.toString();
    }
  }
}
