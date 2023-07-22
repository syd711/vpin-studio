package de.mephisto.vpin.server.system;

import de.mephisto.vpin.commons.SystemInfo;
import de.mephisto.vpin.commons.fx.UIDefaults;
import de.mephisto.vpin.commons.utils.PropertiesStore;
import de.mephisto.vpin.commons.utils.SystemCommandExecutor;
import de.mephisto.vpin.restclient.RestClient;
import de.mephisto.vpin.restclient.ScreenInfo;
import de.mephisto.vpin.restclient.SystemSummary;
import de.mephisto.vpin.server.VPinStudioException;
import de.mephisto.vpin.server.VPinStudioServer;
import de.mephisto.vpin.server.backup.adapters.ArchiveType;
import de.mephisto.vpin.server.resources.ResourceLoader;
import de.mephisto.vpin.server.util.SystemUtil;
import edu.umd.cs.findbugs.annotations.NonNull;
import javafx.geometry.Rectangle2D;
import javafx.stage.Screen;
import org.apache.commons.io.FileUtils;
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
public class SystemService extends SystemInfo implements InitializingBean {
  private final static Logger LOG = LoggerFactory.getLogger(SystemService.class);

  public final static int SERVER_PORT = RestClient.PORT;

  public static final String COMPETITION_BADGES = "competition-badges";

  private final static String VPREG_STG = "VPReg.stg";

  public static String PINEMHI_FOLDER = RESOURCES + "pinemhi";
  public static String ARCHIVES_FOLDER = RESOURCES + "archives";
  private final static String PINEMHI_COMMAND = "PINemHi.exe";
  private final static String PINEMHI_INI = "pinemhi.ini";
  private final static String VPM_ALIAS = "VPMAlias.txt";
  private static final String SYSTEM_PROPERTIES = "system";
  public static final String DEFAULT_BACKGROUND = "background.png";

  private File pinUPSystemInstallationFolder;
  private File visualPinballInstallationFolder;
  private File futurePinballInstallationFolder;
  private File vpxTablesFolder;
  private File mameFolder;
  private File userFolder;

  private File pinemhiNvRamFolder;

  private ArchiveType archiveType = ArchiveType.VPBM;

  @Override
  public void afterPropertiesSet() throws Exception {
    initBaseFolders();
    initPinemHiFolders();
    initVPinTableManagerIcon();

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
      this.archiveType = !store.containsKey(ARCHIVE_TYPE) || store.get(ARCHIVE_TYPE).equals(ArchiveType.VPBM.name().toLowerCase()) ? ArchiveType.VPBM : ArchiveType.VPA;

      //PinUP Popper Folder
      this.pinUPSystemInstallationFolder = this.resolvePinUPSystemInstallationFolder();
      if (!store.containsKey(PINUP_SYSTEM_INSTALLATION_DIR_INST_DIR)) {
        store.set(PINUP_SYSTEM_INSTALLATION_DIR_INST_DIR, pinUPSystemInstallationFolder.getAbsolutePath().replaceAll("\\\\", "/"));
      }
      else {
        this.pinUPSystemInstallationFolder = new File(store.get(PINUP_SYSTEM_INSTALLATION_DIR_INST_DIR));
      }

      //Visual Pinball Folder
      this.visualPinballInstallationFolder = this.resolveVisualPinballInstallationFolder(pinUPSystemInstallationFolder);
      if (!store.containsKey(VISUAL_PINBALL_INST_DIR)) {
        store.set(VISUAL_PINBALL_INST_DIR, visualPinballInstallationFolder.getAbsolutePath().replaceAll("\\\\", "/"));
      }
      else {
        this.visualPinballInstallationFolder = new File(store.get(VISUAL_PINBALL_INST_DIR));
      }

      //Future Pinball Folder
      this.futurePinballInstallationFolder = this.resolveFuturePinballInstallationFolder(pinUPSystemInstallationFolder);
      if (!store.containsKey(FUTURE_PINBALL_INST_DIR)) {
        store.set(FUTURE_PINBALL_INST_DIR, futurePinballInstallationFolder.getAbsolutePath().replaceAll("\\\\", "/"));
      }
      else {
        this.futurePinballInstallationFolder = new File(store.get(FUTURE_PINBALL_INST_DIR));
      }

      //VPX Tables Folder
      this.vpxTablesFolder = this.resolveVpxTablesInstallationFolder(visualPinballInstallationFolder);
      if (!store.containsKey(VPX_TABLES_DIR)) {
        store.set(VPX_TABLES_DIR, vpxTablesFolder.getAbsolutePath().replaceAll("\\\\", "/"));
      }
      else {
        this.vpxTablesFolder = new File(store.get(VPX_TABLES_DIR));
      }

      //Mame Root Folder
      this.mameFolder = this.resolveMameInstallationFolder(visualPinballInstallationFolder);
      if (!store.containsKey(MAME_DIR)) {
        store.set(MAME_DIR, mameFolder.getAbsolutePath().replaceAll("\\\\", "/"));
      }
      else {
        this.mameFolder = new File(store.get(MAME_DIR));
      }

      //User Folder
      this.userFolder = this.resolveUserFolder(visualPinballInstallationFolder);
      if (!store.containsKey(USER_DIR)) {
        store.set(USER_DIR, userFolder.getAbsolutePath().replaceAll("\\\\", "/"));
      }
      else {
        this.userFolder = new File(store.get(USER_DIR));
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

  /**
   * Ensures that the VPin Studio Logo is available for PinUP Popper in the T-Arc.
   */
  private void initVPinTableManagerIcon() {
    File pcWheelFolder = new File(this.getPinUPSystemFolder(), "POPMedia/PC Games/Wheel/");
    if (pcWheelFolder.exists()) {
      File wheelIcon = new File(pcWheelFolder, UIDefaults.MANAGER_TITLE + ".png");
      if (!wheelIcon.exists()) {
        try {
          InputStream resourceAsStream = ResourceLoader.class.getResourceAsStream("logo-500.png");
          FileUtils.copyInputStreamToFile(resourceAsStream, wheelIcon);
          resourceAsStream.close();
          LOG.info("Copied VPin Table Manager icon.");

          File thumbsFolder = new File(pcWheelFolder, "pthumbs");
          de.mephisto.vpin.commons.utils.FileUtils.deleteFolder(thumbsFolder);
        } catch (Exception e) {
          LOG.info("Failed to copy VPin Manager wheel icon: " + e.getMessage(), e);
        }
      }
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

  public SystemSummary getSystemSummary() {
    SystemSummary info = new SystemSummary();
    info.setPinupSystemDirectory(getPinUPSystemFolder().getAbsolutePath());
    info.setVisualPinballDirectory(getVisualPinballInstallationFolder().getAbsolutePath());
    info.setVpinMameDirectory(getMameFolder().getAbsolutePath());
    info.setScreenInfo(getScreenInfo());
    return info;
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
    return this.userFolder;
  }

  public File getMameRomFolder() {
    return new File(this.getMameFolder(), "roms/");
  }

  @SuppressWarnings("unused")
  public File getNvramFolder() {
    return new File(getMameFolder(), "nvram/");
  }

  @NonNull
  public File getMameFolder() {
    return this.mameFolder;
  }

  @NonNull
  public File getAltSoundFolder() {
    return new File(getMameFolder(), "altsound/");
  }

  @NonNull
  public File getAltColorFolder() {
    return new File(getMameFolder(), "altcolor/");
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

  public File getVPXTablesFolder() {
    return this.vpxTablesFolder;
  }

  public File getVPXMusicFolder() {
    return new File(getVisualPinballInstallationFolder(), "Music/");
  }

  public File getVPXExe() {
    return new File(getVisualPinballInstallationFolder(), "VPinballX.exe");
  }

  public File getFuturePinballTablesFolder() {
    return new File(getFuturePinballInstallationFolder(), "Tables/");
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

  public File getPinUPDatabaseFile() {
    return new File(getPinUPSystemFolder(), "PUPDatabase.db");
  }

  public File getArchivesFolder() {
    File file = new File(RESOURCES, "archives/");
    if (!file.exists()) {
      file.mkdirs();
    }
    return file;
  }

  public File getBundlesFolder() {
    File file = new File(getArchivesFolder(), "bundles/");
    if (!file.exists()) {
      file.mkdirs();
    }
    return file;
  }

  public String getVersion() {
    return SystemUtil.getVersion();
  }

  public List<String> getCompetitionBadges() {
    File folder = new File(SystemService.RESOURCES, COMPETITION_BADGES);
    File[] files = folder.listFiles((dir, name) -> name.endsWith("png"));
    if (files != null) {
      return Arrays.stream(files).sorted().map(f -> FilenameUtils.getBaseName(f.getName())).collect(Collectors.toList());
    }
    return Collections.emptyList();
  }

  public boolean isDotNetInstalled() {
    try {
      SystemCommandExecutor executor = new SystemCommandExecutor(Arrays.asList("dotnet", "--list-sdks"));
      executor.executeCommand();
      StringBuilder standardOutputFromCommand = executor.getStandardOutputFromCommand();
      String out = standardOutputFromCommand.toString();
      return out.contains("sdk") & out.contains("dotnet");
    } catch (Exception e) {
      LOG.error("Failed to execute .net check: " + e.getMessage(), e);
    }
    return false;
  }

  public File getBagdeFile(String badge) {
    File folder = new File(SystemService.RESOURCES, COMPETITION_BADGES);
    return new File(folder, badge + ".png");
  }

  public boolean killProcesses(String name) {
    List<ProcessHandle> filteredProceses = ProcessHandle.allProcesses()
        .filter(p -> p.info().command().isPresent() && (p.info().command().get().contains(name)))
        .collect(Collectors.toList());
    boolean success = false;
    for (ProcessHandle process : filteredProceses) {
      String cmd = process.info().command().get();
      boolean b = process.destroyForcibly();
      LOG.info("Destroyed process '" + cmd + "', result: " + b);
      if(!success && b) {
        success = true;
      }
    }
    return success;
  }

  public boolean isProcessRunning(String name) {
    List<ProcessHandle> filteredProceses = ProcessHandle.allProcesses()
        .filter(p -> p.info().command().isPresent() && (p.info().command().get().contains(name)))
        .collect(Collectors.toList());
    return !filteredProceses.isEmpty();
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
                    p.info().command().get().contains("PinUpPackEditor") ||
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

  public File getVPinStudioMenuExe() {
    return new File("./VPin-Studio-Table-Manager.exe");
  }

  public ArchiveType getArchiveType() {
    return archiveType;
  }

  public ScreenInfo getScreenInfo() {
    ScreenInfo info = new ScreenInfo();
    Rectangle2D screenBounds = Screen.getPrimary().getBounds();
    info.setPortraitMode(screenBounds.getWidth() < screenBounds.getHeight());
    return info;
  }
}