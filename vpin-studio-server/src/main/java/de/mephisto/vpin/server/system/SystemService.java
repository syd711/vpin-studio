package de.mephisto.vpin.server.system;

import de.mephisto.vpin.commons.utils.PropertiesStore;
import de.mephisto.vpin.commons.utils.SystemCommandExecutor;
import de.mephisto.vpin.restclient.RestClient;
import de.mephisto.vpin.server.VPinStudioException;
import de.mephisto.vpin.server.VPinStudioServer;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.io.*;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class SystemService implements InitializingBean {
  private final static Logger LOG = LoggerFactory.getLogger(SystemService.class);

  public final static int SERVER_PORT = RestClient.PORT;

  public static final String COMPETITION_BADGES = "competition-badges";

  private final static String VPX_REG_KEY = "HKEY_CURRENT_USER\\SOFTWARE\\Visual Pinball\\VP10\\RecentDir";
  private final static String POPPER_REG_KEY = "HKEY_LOCAL_MACHINE\\SYSTEM\\ControlSet001\\Control\\Session Manager\\Environment";
  private final static String VPREG_STG = "VPReg.stg";
  public static String RESOURCES = "./resources/";

  private final static String PINUP_SYSTEM_INSTALLATION_DIR_INST_DIR = "pinupSystem.installationDir";
  private final static String VISUAL_PINBALL_INST_DIR = "visualPinball.installationDir";
  private final static String FUTURE_PINBALL_INST_DIR = "futurePinball.installationDir";
  public static String PINEMHI_FOLDER = RESOURCES + "pinemhi";
  private final static String PINEMHI_COMMAND = "PINemHi.exe";
  private final static String PINEMHI_INI = "pinemhi.ini";
  private final static String VPM_ALIAS = "VPMAlias.txt";
  private static final String SYSTEM_PROPERTIES = "system";


  private File pinUPSystemInstallationFolder;
  private File visualPinballInstallationFolder;
  private File futurePinballInstallationFolder;

  private File pinemhiNvRamFolder;

  @Override
  public void afterPropertiesSet() throws Exception {
    initBaseFolders();
    initPinemHiFolders();

    if (!getPinUPSystemFolder().exists()) {
      throw new FileNotFoundException("Wrong PinUP Popper installation folder: " + getPinUPSystemFolder().getAbsolutePath() + ".\nPlease fix the PinUP Popper installation path in file ./resources/system.properties");
    }
    if (!getVisualPinballInstallationFolder().exists()) {
      throw new FileNotFoundException("Wrong Visual Pinball installation folder: " + getVisualPinballInstallationFolder().getAbsolutePath() + ".\nPlease fix the Visual Pinball installation path in file ./resources/system.properties");
    }
    logSystemInfo();
  }

  private void initBaseFolders() throws VPinStudioException {
    try {
      PropertiesStore store = PropertiesStore.create(SystemService.RESOURCES, SYSTEM_PROPERTIES);

      //PinUP Popper Folder
      this.pinUPSystemInstallationFolder = this.resolvePinUPSystemInstallationFolder();
      if (!store.containsKey(PINUP_SYSTEM_INSTALLATION_DIR_INST_DIR)) {
        store.set(PINUP_SYSTEM_INSTALLATION_DIR_INST_DIR, pinUPSystemInstallationFolder.getAbsolutePath().replaceAll("\\\\", "/"));
      }
      else {
        this.pinUPSystemInstallationFolder = new File(store.get(PINUP_SYSTEM_INSTALLATION_DIR_INST_DIR));
      }

      //Visual Pinball Folder
      this.visualPinballInstallationFolder = this.resolveVisualPinballInstallationFolder();
      if (!store.containsKey(VISUAL_PINBALL_INST_DIR)) {
        store.set(VISUAL_PINBALL_INST_DIR, visualPinballInstallationFolder.getAbsolutePath().replaceAll("\\\\", "/"));
      }
      else {
        this.visualPinballInstallationFolder = new File(store.get(VISUAL_PINBALL_INST_DIR));
      }

      //Future Pinball Folder
      this.futurePinballInstallationFolder = this.resolveFuturePinballInstallationFolder();
      if (!store.containsKey(FUTURE_PINBALL_INST_DIR)) {
        store.set(FUTURE_PINBALL_INST_DIR, futurePinballInstallationFolder.getAbsolutePath().replaceAll("\\\\", "/"));
      }
      else {
        this.futurePinballInstallationFolder = new File(store.get(FUTURE_PINBALL_INST_DIR));
      }

      if (!getB2SImageExtractionFolder().exists()) {
        boolean mkdirs = getB2SImageExtractionFolder().mkdirs();
        if (!mkdirs) {
          LOG.error("Failed to create b2s image directory " + getB2SImageExtractionFolder().getAbsolutePath());
        }
      }

      if (!getB2SCroppedImageFolder().exists()) {
        boolean mkdirs = getB2SCroppedImageFolder().mkdirs();
        if (!mkdirs) {
          LOG.error("Failed to create b2s crops directory " + getB2SCroppedImageFolder().getAbsolutePath());
        }
      }
    } catch (Exception e) {
      String msg = "Failed to initialize base folders: " + e.getMessage();
      LOG.error(msg, e);
      throw new VPinStudioException(msg, e);
    }
  }

  private void logSystemInfo() {
    LOG.info("********************************* Installation Overview ***********************************************");
    LOG.info(formatPathLog("Locale", Locale.getDefault().getDisplayName()));
    LOG.info(formatPathLog("Charset", Charset.defaultCharset().displayName()));
    LOG.info(formatPathLog("PinUP System Folder", this.getPinUPSystemFolder()));
    LOG.info(formatPathLog("PinUP Database File", this.getPinUPDatabaseFile()));
    LOG.info(formatPathLog("Visual Pinball Folder", this.getVisualPinballInstallationFolder()));
    LOG.info(formatPathLog("Visual Pinball Tables Folder", this.getVPXTablesFolder()));
    LOG.info(formatPathLog("Mame Folder", this.getMameFolder()));
    LOG.info(formatPathLog("ROM Folder", this.getMameRomFolder()));
    LOG.info(formatPathLog("NVRam Folder", this.getNvramFolder()));
    LOG.info(formatPathLog("Pinemhi NVRam Folder", this.pinemhiNvRamFolder));
    LOG.info(formatPathLog("Pinemhi Command", this.getPinemhiCommandFile()));
    LOG.info(formatPathLog("VPReg File", this.getVPRegFile()));
    LOG.info(formatPathLog("B2S Extraction Folder", this.getB2SImageExtractionFolder()));
    LOG.info(formatPathLog("B2S Cropped Folder", this.getB2SCroppedImageFolder()));
    LOG.info(formatPathLog("VPX Files", String.valueOf(this.getVPXTables().length)));
    LOG.info(formatPathLog("Service Version", VPinStudioServer.class.getPackage().getImplementationVersion()));
    LOG.info("*******************************************************************************************************");
  }

  private void initPinemHiFolders() throws VPinStudioException {
    try {
      File file = new File(PINEMHI_FOLDER, PINEMHI_INI);
      if (!file.exists()) {
        throw new FileNotFoundException("pinemhi.ini file (" + file.getAbsolutePath() + ") not found.");
      }

      FileInputStream fileInputStream = new FileInputStream(file);
      java.util.List<String> lines = IOUtils.readLines(fileInputStream, StandardCharsets.UTF_8);
      fileInputStream.close();

      boolean writeUpdates = false;
      List<String> updatedLines = new ArrayList<>();
      for (String line : lines) {
        if (line.startsWith("VP=")) {
          String vpValue = line.split("=")[1];
          pinemhiNvRamFolder = new File(vpValue);
          if (!pinemhiNvRamFolder.exists()) {
            pinemhiNvRamFolder = getNvramFolder();
            line = "VP=" + pinemhiNvRamFolder.getAbsolutePath() + "\\";
            writeUpdates = true;
          }
        }
        updatedLines.add(line);
      }

      if (writeUpdates) {
        FileOutputStream out = new FileOutputStream(file);
        IOUtils.writeLines(updatedLines, "\n", out, StandardCharsets.UTF_8);
        out.close();
        LOG.info("Written updates to " + file.getAbsolutePath());
      }

      LOG.info("Finished pinemhi installation check.");
    } catch (Exception e) {
      String msg = "Failed to run installation for pinemhi: " + e.getMessage();
      LOG.error(msg, e);
      throw new VPinStudioException(msg, e);
    }
  }

  private String formatPathLog(String label, String value) {
    return formatPathLog(label, value, null, null);
  }

  private String formatPathLog(String label, File file) {
    return formatPathLog(label, file.getAbsolutePath(), file.exists(), file.canRead());
  }

  public File getVPMAliasFile() {
    return new File(this.getMameFolder(), VPM_ALIAS);
  }

  public File getB2SImageExtractionFolder() {
    return new File(RESOURCES, "b2s-raw/");
  }

  public File getB2SCroppedImageFolder() {
    return new File(RESOURCES, "b2s-cropped/");
  }

  private String formatPathLog(String label, String value, Boolean exists, Boolean readable) {
    StringBuilder b = new StringBuilder(label);
    b.append(":");
    while (b.length() < 33) {
      b.append(" ");
    }
    b.append(value);

    if (exists != null) {
      while (b.length() < 89) {
        b.append(" ");
      }
      if (!exists) {
        b.append("   [NOT FOUND]");
      }
      else if (!readable) {
        b.append("[NOT READABLE]");
      }
      else {
        b.append("          [OK]");
      }
    }
    return b.toString();
  }

  private File resolvePinUPSystemInstallationFolder() {
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
      }
    } catch (Exception e) {
      LOG.error("Failed to read installation folder: " + e.getMessage(), e);
    }
    return new File("C:/vPinball/Visual Pinball");
  }

  private File resolveVisualPinballInstallationFolder() {
    File file = new File(pinUPSystemInstallationFolder.getParent(), "VisualPinball");
    if (!file.exists()) {
      LOG.info("The system info could not derive the Visual Pinball installation folder from the PinUP Popper installation, checking windows registry next.");
      String tablesDir = readRegistry(VPX_REG_KEY, "LoadDir");
      if (tablesDir != null) {
        tablesDir = extractRegistryValue(tablesDir);
        LOG.info("Resolve Visual Pinball tables folder " + tablesDir);
        file = new File(tablesDir);
        if (file.exists()) {
          return file.getParentFile();
        }
      }
    }
    return file;
  }

  private File resolveFuturePinballInstallationFolder() {
    File file = new File(pinUPSystemInstallationFolder.getParent(), "FuturePinball");
    if (!file.exists()) {
      LOG.info("The system info could not derive the Future Pinball installation folder from the PinUP Popper installation, checking windows registry next.");
    }
    return file;
  }

  @NonNull
  public File getPinemhiCommandFile() {
    return new File(PINEMHI_FOLDER, PINEMHI_COMMAND);
  }

  @SuppressWarnings("unused")
  public Dimension getScreenSize() {
    return Toolkit.getDefaultToolkit().getScreenSize();
  }

  @NonNull
  public File getVPRegFile() {
    return new File(this.getVisualPinballUserFolder(), VPREG_STG);
  }

  @NonNull
  public File getVisualPinballUserFolder() {
    return new File(this.getVisualPinballInstallationFolder(), "User");
  }

  public File getMameRomFolder() {
    return new File(getVisualPinballInstallationFolder(), "VPinMAME/roms/");
  }

  @SuppressWarnings("unused")
  public File getNvramFolder() {
    return new File(getMameFolder(), "nvram/");
  }

  @NonNull
  public File getMameFolder() {
    return new File(getVisualPinballInstallationFolder(), "VPinMAME/");
  }

  public File[] getVPXTables() {
    return getVPXTablesFolder().listFiles((dir, name) -> name.endsWith(".vpx"));
  }

  public File getVisualPinballInstallationFolder() {
    return visualPinballInstallationFolder;
  }

  public File getFuturePinballInstallationFolder() {
    return futurePinballInstallationFolder;
  }

  public String get7ZipCommand() {
    return new File(SystemService.RESOURCES, "7z.exe").getAbsolutePath();
  }

  public File getVPXTablesFolder() {
    return new File(getVisualPinballInstallationFolder(), "Tables/");
  }

  public File getVPXExe() {
    return new File(getVisualPinballInstallationFolder(), "VPinballX.exe");
  }

  public File getFuturePinballTablesFolder() {
    return new File(getFuturePinballInstallationFolder(), "Tables/");
  }

  public File getDirectB2SMediaFolder() {
    return new File(RESOURCES, "directB2S/");
  }

  public File getPinUPSystemFolder() {
    return pinUPSystemInstallationFolder;
  }


  /**
   * Checks to see if a specific port is available.
   *
   * @param port the port to check for availability
   */
  public static boolean isAvailable(int port) {
    ServerSocket ss = null;
    DatagramSocket ds = null;
    try {
      ss = new ServerSocket(port);
      ss.setReuseAddress(true);
      ds = new DatagramSocket(port);
      ds.setReuseAddress(true);
      return true;
    } catch (IOException e) {
    } finally {
      if (ds != null) {
        ds.close();
      }

      if (ss != null) {
        try {
          ss.close();
        } catch (IOException e) {
          /* should not be thrown */
        }
      }
    }

    return false;
  }

  static String extractRegistryValue(String output) {
    String result = output;
    result = result.replace("\n", "").replace("\r", "").trim();

    String[] s = result.split("    ");
    return s[3];
  }

  static final String readRegistry(String location, String key) {
    try {
      // Run reg query, then read output with StreamReader (internal class)
      String cmd = "reg query " + '"' + location;
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
      LOG.error("Failed to read registry key " + location);
      return null;
    }
  }

  public File getPinUPDatabaseFile() {
    return new File(getPinUPSystemFolder(), "PUPDatabase.db");
  }

  public File getVpaArchiveFolder() {
    File file = new File(RESOURCES, "vpa/");
    if (!file.exists()) {
      file.mkdirs();
    }
    return file;
  }

  public String getVersion() {
    try {
      final Properties properties = new Properties();
      InputStream resourceAsStream = VPinStudioServer.class.getClassLoader().getResourceAsStream("version.properties");
      properties.load(resourceAsStream);
      resourceAsStream.close();
      return properties.getProperty("vpin.studio.version");
    } catch (IOException e) {
      LOG.error("Failed to read version number: " + e.getMessage(), e);
    }
    return null;
  }

  public List<String> getCompetitionBadges() {
    File folder = new File(SystemService.RESOURCES, COMPETITION_BADGES);
    File[] files = folder.listFiles((dir, name) -> name.endsWith("png"));
    if (files != null) {
      return Arrays.stream(files).sorted().map(f -> FilenameUtils.getBaseName(f.getName())).collect(Collectors.toList());
    }
    return Collections.emptyList();
  }

  public File getBagdeFile(String badge) {
    File folder = new File(SystemService.RESOURCES, COMPETITION_BADGES);
    return new File(folder, badge + ".png");
  }

  public void killProcesses(String name) {
    List<ProcessHandle> pinUpProcesses = ProcessHandle.allProcesses()
        .filter(p -> p.info().command().isPresent() && (p.info().command().get().contains(name)))
        .collect(Collectors.toList());
    for (ProcessHandle pinUpProcess : pinUpProcesses) {
      String cmd = pinUpProcess.info().command().get();
      boolean b = pinUpProcess.destroyForcibly();
      LOG.info("Destroyed process '" + cmd + "', result: " + b);
    }
  }

  public boolean killPopper() {
    List<ProcessHandle> pinUpProcesses = ProcessHandle
        .allProcesses()
        .filter(p -> p.info().command().isPresent() &&
            (
                p.info().command().get().contains("PinUpMenu") ||
                    p.info().command().get().contains("PinUpDisplay") ||
                    p.info().command().get().contains("PinUpPlayer") ||
                    p.info().command().get().contains("VPXStarter") ||
                    p.info().command().get().contains("VPinballX") ||
                    p.info().command().get().contains("B2SBackglassServerEXE") ||
                    p.info().command().get().contains("DOF")))
        .collect(Collectors.toList());

    if (pinUpProcesses.isEmpty()) {
      LOG.info("No PinUP processes found, termination canceled.");
      return false;
    }

    for (ProcessHandle pinUpProcess : pinUpProcesses) {
      String cmd = pinUpProcess.info().command().get();
      boolean b = pinUpProcess.destroyForcibly();
      LOG.info("Destroyed process '" + cmd + "', result: " + b);
    }
    return true;
  }

  public void restartPopper() {
    killPopper();

    try {
      List<String> params = Arrays.asList("cmd", "/c", "start", "PinUpMenu.exe");
      SystemCommandExecutor executor = new SystemCommandExecutor(params, false);
      executor.setDir(getPinUPSystemFolder());
      executor.executeCommandAsync();

      StringBuilder standardOutputFromCommand = executor.getStandardOutputFromCommand();
      StringBuilder standardErrorFromCommand = executor.getStandardErrorFromCommand();
      if (!StringUtils.isEmpty(standardErrorFromCommand.toString())) {
        LOG.error("Popper restart failed: {}", standardErrorFromCommand);
      }
    } catch (Exception e) {
      LOG.error("Failed to start PinUP Popper again: " + e.getMessage(), e);
    }
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
