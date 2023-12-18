package de.mephisto.vpin.server.system;

import de.mephisto.vpin.commons.SystemInfo;
import de.mephisto.vpin.commons.fx.OverlayWindowFX;
import de.mephisto.vpin.commons.fx.UIDefaults;
import de.mephisto.vpin.commons.utils.PropertiesStore;
import de.mephisto.vpin.commons.utils.SystemCommandExecutor;
import de.mephisto.vpin.restclient.RestClient;
import de.mephisto.vpin.restclient.archiving.ArchiveType;
import de.mephisto.vpin.restclient.components.ComponentType;
import de.mephisto.vpin.restclient.system.ScreenInfo;
import de.mephisto.vpin.restclient.system.SystemSummary;
import de.mephisto.vpin.server.VPinStudioException;
import de.mephisto.vpin.server.VPinStudioServer;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.pinemhi.PINemHiService;
import de.mephisto.vpin.server.resources.ResourceLoader;
import de.mephisto.vpin.server.util.SystemUtil;
import edu.umd.cs.findbugs.annotations.NonNull;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.geometry.Rectangle2D;
import javafx.stage.Screen;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.nio.charset.Charset;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class SystemService extends SystemInfo implements InitializingBean, ApplicationContextAware {
  private final static Logger LOG = LoggerFactory.getLogger(SystemService.class);

  public final static int SERVER_PORT = RestClient.PORT;

  public static final String COMPETITION_BADGES = "competition-badges";

  public static String ARCHIVES_FOLDER = RESOURCES + "archives";

  public static final String DEFAULT_BACKGROUND = "background.png";
  public static final String DMD = "dmd.png";

  private File pinUPSystemInstallationFolder;
  private File backupFolder;

  private ArchiveType archiveType = ArchiveType.VPA;

  @Value("${system.properties}")
  private String systemProperties;

  @Value("${server.port}")
  private int port;

  private ApplicationContext context;

  private void initBaseFolders() throws VPinStudioException {
    try {
      PropertiesStore store = PropertiesStore.create(SystemService.RESOURCES, systemProperties);
      this.archiveType = ArchiveType.VPA;

      //check test run
      if (!systemProperties.contains("-test")) {
        if (!store.containsKey(ARCHIVE_TYPE) || store.get(ARCHIVE_TYPE).equals(ArchiveType.VPBM.name().toLowerCase())) {
          archiveType = ArchiveType.VPBM;
        }
      }


      //PinUP Popper Folder
      this.pinUPSystemInstallationFolder = this.resolvePinUPSystemInstallationFolder();
      if (!store.containsKey(PINUP_SYSTEM_INSTALLATION_DIR_INST_DIR)) {
        store.set(PINUP_SYSTEM_INSTALLATION_DIR_INST_DIR, pinUPSystemInstallationFolder.getAbsolutePath().replaceAll("\\\\", "/"));
      }
      else {
        this.pinUPSystemInstallationFolder = new File(store.get(PINUP_SYSTEM_INSTALLATION_DIR_INST_DIR));
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

      this.backupFolder = new File(SystemInfo.RESOURCES, "backups");
      if (!this.backupFolder.exists() && !this.backupFolder.mkdirs()) {
        LOG.error("Failed to create backup folder " + this.backupFolder.getAbsolutePath());
      }
    } catch (Exception e) {
      String msg = "Failed to initialize base folders: " + e.getMessage();
      LOG.error(msg, e);
      throw new VPinStudioException(msg, e);
    }
  }

  public String getPupUpMediaFolderName(Game game) {
    String filename = game.getGameFile().getName();
    if (filename.endsWith(".fp")) {
      return "Future Pinball";
    }

    if (filename.endsWith(".fx")) {
      return "Pinball FX3";
    }

    return "Visual Pinball X";
  }

  /**
   * Ensures that the VPin Studio Logo is available for PinUP Popper in the T-Arc.
   */
  private void initVPinTableManagerIcon() {
//    File pcWheelFolder = new File(this.getPinUPSystemFolder(), "POPMedia/PC Games/Wheel/");
//    if (pcWheelFolder.exists()) {
//      File wheelIcon = new File(pcWheelFolder, UIDefaults.MANAGER_TITLE + ".png");
//      if (!wheelIcon.exists()) {
//        try {
//          InputStream resourceAsStream = ResourceLoader.class.getResourceAsStream("logo-500.png");
//          FileUtils.copyInputStreamToFile(resourceAsStream, wheelIcon);
//          resourceAsStream.close();
//          LOG.info("Copied VPin Table Manager icon.");
//
//          File thumbsFolder = new File(pcWheelFolder, "pthumbs");
//          de.mephisto.vpin.commons.utils.FileUtils.deleteFolder(thumbsFolder);
//        } catch (Exception e) {
//          LOG.info("Failed to copy VPin Manager wheel icon: " + e.getMessage(), e);
//        }
//      }
//    }
  }

  private void logSystemInfo() {
    LOG.info("********************************* Installation Overview ***********************************************");
    LOG.info(formatPathLog("Locale", Locale.getDefault().getDisplayName()));
    LOG.info(formatPathLog("Charset", Charset.defaultCharset().displayName()));
    LOG.info(formatPathLog("PinUP System Folder", this.getPinUPSystemFolder()));
    LOG.info(formatPathLog("PinUP Database File", this.getPinUPDatabaseFile()));
    LOG.info(formatPathLog("Pinemhi Command", this.getPinemhiCommandFile()));
    LOG.info(formatPathLog("B2S Extraction Folder", this.getB2SImageExtractionFolder()));
    LOG.info(formatPathLog("B2S Cropped Folder", this.getB2SCroppedImageFolder()));
    LOG.info(formatPathLog("Service Version", VPinStudioServer.class.getPackage().getImplementationVersion()));
    LOG.info("*******************************************************************************************************");
  }

  public static String formatPathLog(String label, String value) {
    return formatPathLog(label, value, null, null);
  }

  private static String formatPathLog(String label, File file) {
    return formatPathLog(label, file.getAbsolutePath(), file.exists(), file.canRead());
  }

  public File getB2SImageExtractionFolder() {
    return new File(RESOURCES, "b2s-raw/");
  }

  public File getB2SCroppedImageFolder() {
    return new File(RESOURCES, "b2s-cropped/");
  }

  public File getBackupFolder() {
    return backupFolder;
  }

  public SystemSummary getSystemSummary() {
    SystemSummary info = new SystemSummary();
    info.setPinupSystemDirectory(getPinUPSystemFolder().getAbsolutePath());
    info.setScreenInfos(getScreenInfos());
    info.setArchiveType(this.getArchiveType());
    return info;
  }

  private static String formatPathLog(String label, String value, Boolean exists, Boolean readable) {
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
    return new File(PINemHiService.PINEMHI_FOLDER, PINemHiService.PINEMHI_COMMAND);
  }

  public File getResettedNVRamsFolder() {
    return new File(RESOURCES, "nvrams");
  }

  @SuppressWarnings("unused")
  public Dimension getScreenSize() {
    return Toolkit.getDefaultToolkit().getScreenSize();
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
      if (!success && b) {
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
            p.info().command().get().startsWith("VPinball") ||
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

  public void setArchiveType(ArchiveType archiveType) {
    this.archiveType = archiveType;
  }

  public List<ScreenInfo> getScreenInfos() {
    List<ScreenInfo> result = new ArrayList<>();

    Screen primary = Screen.getPrimary();
    ScreenInfo info = new ScreenInfo();
    Rectangle2D screenBounds = primary.getBounds();
    info.setPortraitMode(screenBounds.getWidth() < screenBounds.getHeight());
    info.setPrimary(true);
    info.setHeight((int) screenBounds.getHeight());
    info.setWidth((int) screenBounds.getWidth());
    info.setId(1);
    result.add(info);

    int index = 2;
    ObservableList<Screen> screens = Screen.getScreens();
    for (Screen screen : screens) {
      if (screen.equals(Screen.getPrimary())) {
        continue;
      }

      info = new ScreenInfo();
      screenBounds = Screen.getPrimary().getBounds();
      info.setPortraitMode(screenBounds.getWidth() < screenBounds.getHeight());
      info.setPrimary(false);
      info.setHeight((int) screenBounds.getHeight());
      info.setWidth((int) screenBounds.getWidth());
      info.setId(index);

      result.add(info);
      index++;
    }
    return result;
  }

  /**
   * Checks to see if a specific port is available.
   *
   * @param port the port to check for availability
   */
  public static boolean available(int port) {
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

  public boolean setMaintenanceMode(boolean enabled) {
    Platform.runLater(() -> {
      OverlayWindowFX.getInstance().setMaintenanceVisible(enabled);
    });
    return enabled;
  }

  public void shutdown() {
    ((ConfigurableApplicationContext) context).close();
    System.exit(0);
  }

  public File getComponentArchiveFolder(ComponentType type) {
    File folder = new File(RESOURCES, "component-archives/");
    if (!folder.exists()) {
      folder.mkdirs();
    }
    folder = new File(folder, type.name() + "/");
    if (!folder.exists()) {
      folder.mkdirs();
    }
    return folder;
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    if (!available(port)) {
      LOG.warn("----------------------------------------------");
      LOG.warn("Instance already running, terminating server.");
      LOG.warn("==============================================");
      this.shutdown();
      return;
    }

    initBaseFolders();
    initVPinTableManagerIcon();

    if (!getPinUPSystemFolder().exists()) {
      throw new FileNotFoundException("Wrong PinUP Popper installation folder: " + getPinUPSystemFolder().getAbsolutePath() + ".\nPlease fix the PinUP Popper installation path in file ./resources/system.properties");
    }

    logSystemInfo();
  }

  @Override
  public void setApplicationContext(ApplicationContext context) throws BeansException {
    this.context = context;
  }
}