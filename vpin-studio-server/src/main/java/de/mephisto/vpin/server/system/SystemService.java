package de.mephisto.vpin.server.system;

import de.mephisto.vpin.commons.SystemInfo;
import de.mephisto.vpin.commons.fx.ServerFX;
import de.mephisto.vpin.commons.utils.PropertiesStore;
import de.mephisto.vpin.commons.utils.SystemCommandExecutor;
import de.mephisto.vpin.restclient.RestClient;
import de.mephisto.vpin.restclient.archiving.ArchiveType;
import de.mephisto.vpin.restclient.components.ComponentType;
import de.mephisto.vpin.restclient.frontend.FrontendType;
import de.mephisto.vpin.restclient.system.ScoringDB;
import de.mephisto.vpin.restclient.system.ScreenInfo;
import de.mephisto.vpin.server.VPinStudioException;
import de.mephisto.vpin.server.VPinStudioServer;
import de.mephisto.vpin.server.inputs.ShutdownThread;
import de.mephisto.vpin.server.pinemhi.PINemHiService;
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
import java.io.IOException;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
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

  private File frontendInstallationFolder;
  private File backupFolder;

  private ArchiveType archiveType = ArchiveType.VPA;
  private FrontendType frontendType = FrontendType.Popper;

  @Value("${system.properties}")
  private String systemProperties;

  @Value("${server.port}")
  private int port;

  private ScoringDB db;

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
      if (store.containsKey(PINUP_SYSTEM_INSTALLATION_DIR_INST_DIR) && !StringUtils.isEmpty(store.get(PINUP_SYSTEM_INSTALLATION_DIR_INST_DIR))) {
        this.frontendInstallationFolder = new File(store.get(PINUP_SYSTEM_INSTALLATION_DIR_INST_DIR));
        frontendType = FrontendType.Popper;
      }
      //PinballX Folder
      if (store.containsKey(PINBALLX_INSTALLATION_DIR_INST_DIR) && !StringUtils.isEmpty(store.get(PINBALLX_INSTALLATION_DIR_INST_DIR))) {
        this.frontendInstallationFolder = new File(store.get(PINBALLX_INSTALLATION_DIR_INST_DIR));
        frontendType = FrontendType.PinballX;
      }
      //Standalone Folder
      if (store.containsKey(STANDALONE_INSTALLATION_DIR_INST_DIR) && !StringUtils.isEmpty(store.get(STANDALONE_INSTALLATION_DIR_INST_DIR))) {
        this.frontendInstallationFolder = new File(store.get(STANDALONE_INSTALLATION_DIR_INST_DIR));
        frontendType = FrontendType.Standalone;
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
    }
    catch (Exception e) {
      String msg = "Failed to initialize base folders: " + e.getMessage();
      LOG.error(msg, e);
      throw new VPinStudioException(msg, e);
    }
  }

  public FrontendType getFrontendType() {
    return frontendType;
  }

  private void logSystemInfo() {
    LOG.info("********************************* Installation Overview ***********************************************");
    LOG.info(formatPathLog("Locale", Locale.getDefault().getDisplayName()));
    LOG.info(formatPathLog("Charset", Charset.defaultCharset().displayName()));
    LOG.info(formatPathLog("Frontend Type", this.getFrontendType().name()));
    LOG.info(formatPathLog("Frontend Folder", this.getFrontendInstallationFolder()));
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

  public File getFrontendInstallationFolder() {
    return frontendInstallationFolder;
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
    }
    catch (IOException e) {
    }
    finally {
      if (ds != null) {
        ds.close();
      }

      if (ss != null) {
        try {
          ss.close();
        }
        catch (IOException e) {
          /* should not be thrown */
        }
      }
    }

    return false;
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
    }
    catch (Exception e) {
      LOG.error("Failed to execute .net check: " + e.getMessage(), e);
    }
    return false;
  }

  public File getBadgeFile(String badge) {
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

  public List<ProcessHandle> getProcesses() {
    return ProcessHandle.allProcesses()
        .filter(p -> p.info().command().isPresent()).collect(Collectors.toList());
  }

  public boolean isVPXRunning() {
    return isVPXRunning(getProcesses());
  }

  public boolean isVPXRunning(List<ProcessHandle> allProcesses) {
    for (ProcessHandle p : allProcesses) {
      if (p.info().command().isPresent()) {
        String cmdName = p.info().command().get();
        if (cmdName.toLowerCase().contains("Visual Pinball".toLowerCase()) || cmdName.toLowerCase().contains("VisualPinball".toLowerCase()) || cmdName.toLowerCase().contains("VPinball".toLowerCase())) {
          return true;
        }
      }
    }
    return false;
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
    }
    catch (IOException e) {
    }
    finally {
      if (ds != null) {
        ds.close();
      }

      if (ss != null) {
        try {
          ss.close();
        }
        catch (IOException e) {
          /* should not be thrown */
        }
      }
    }

    return false;
  }

  public boolean setMaintenanceMode(boolean enabled) {
    Platform.runLater(() -> {
      ServerFX.getInstance().setMaintenanceVisible(enabled);
    });
    return enabled;
  }

  public void shutdown() {
    ((ConfigurableApplicationContext) context).close();
    System.exit(0);
  }

  public void systemShutdown() {
    ShutdownThread.shutdown();
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

  public ScoringDB getScoringDatabase() {
    if (db == null) {
      db = new ScoringDB();
    }
    return db;
  }

  private void loadingScoringDB() {
    db = ScoringDB.load();
  }

  private static File getScoringDBFile() {
    return new File(SystemInfo.RESOURCES, ScoringDB.SCORING_DB_NAME);
  }

  @Override
  public void setApplicationContext(ApplicationContext context) throws BeansException {
    this.context = context;
  }

  public String backup() {
    File source = new File(RESOURCES, "vpin-studio.db");
    String name = FilenameUtils.getBaseName(source.getName()) + "_" + new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new Date()) + ".db";
    File targetFolder = new File(RESOURCES, "backups/");
    File target = new File(targetFolder, name);
    try {
      targetFolder.mkdirs();
      FileUtils.copyFile(source, target);
    }
    catch (IOException e) {
      LOG.error("Failed to backup DB: " + e.getMessage(), e);
    }
    return target.getName();
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

    this.loadingScoringDB();
    new Thread(() -> {
      Thread.currentThread().setName("ScoringDB Updater");
      if (!new File("./").getAbsolutePath().contains("workspace")) {
        ScoringDB.update();
      }
      this.loadingScoringDB();
    }).start();

    initBaseFolders();
    logSystemInfo();
  }
}