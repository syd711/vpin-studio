package de.mephisto.vpin.server.system;

import com.sun.jna.platform.DesktopWindow;
import com.sun.jna.platform.WindowUtils;
import de.mephisto.vpin.commons.MonitorInfoUtil;
import de.mephisto.vpin.commons.SystemInfo;
import de.mephisto.vpin.commons.fx.ServerFX;
import de.mephisto.vpin.commons.utils.PropertiesStore;
import de.mephisto.vpin.restclient.archiving.ArchiveType;
import de.mephisto.vpin.restclient.components.ComponentType;
import de.mephisto.vpin.restclient.frontend.FrontendType;
import de.mephisto.vpin.restclient.system.MonitorInfo;
import de.mephisto.vpin.restclient.system.NVRamsInfo;
import de.mephisto.vpin.restclient.system.ScoringDB;
import de.mephisto.vpin.restclient.util.SystemCommandExecutor;
import de.mephisto.vpin.server.ServerUpdatePreProcessing;
import de.mephisto.vpin.server.VPinStudioException;
import de.mephisto.vpin.server.VPinStudioServer;
import de.mephisto.vpin.server.inputs.ShutdownThread;
import de.mephisto.vpin.server.pinemhi.PINemHiService;
import de.mephisto.vpin.server.util.VersionUtil;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import javafx.application.Platform;
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
import java.util.concurrent.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Service
public class SystemService extends SystemInfo implements InitializingBean, ApplicationContextAware {
  private final static Logger LOG = LoggerFactory.getLogger(SystemService.class);

  public static final String COMPETITION_BADGES = "competition-badges";

  public static final String RAW_MEDIA_FOLDER = "media-raw/";
  public static final String CROPPED_MEDIA_FOLDER = "media-cropped/";

  public static String ARCHIVES_FOLDER = RESOURCES + "archives";

  public static final String DEFAULT_BACKGROUND = "background.png";
  public static final String DMD = "dmd.png";

  private File pinupInstallationFolder;
  private File pinballXInstallationFolder;
  private File pinballYInstallationFolder;
  private File standaloneInstallationFolder;

  private File backglassServerFolder;

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
      PropertiesStore store = PropertiesStore.create(RESOURCES, systemProperties);
      this.archiveType = ArchiveType.VPA;

      //check test run
      if (!systemProperties.contains("-test")) {
        if (!store.containsKey(ARCHIVE_TYPE) || store.get(ARCHIVE_TYPE).equals(ArchiveType.VPBM.name().toLowerCase())) {
          archiveType = ArchiveType.VPBM;
        }
      }

      // Determination of the installed Frontend
      //Standalone Folder
      if (store.containsKey(STANDALONE_INSTALLATION_DIR_INST_DIR) && !StringUtils.isEmpty(store.get(STANDALONE_INSTALLATION_DIR_INST_DIR))) {
        this.standaloneInstallationFolder = new File(store.get(STANDALONE_INSTALLATION_DIR_INST_DIR));
        frontendType = FrontendType.Standalone;
      }
      //PinballX Folder
      if (store.containsKey(PINBALLX_INSTALLATION_DIR_INST_DIR) && !StringUtils.isEmpty(store.get(PINBALLX_INSTALLATION_DIR_INST_DIR))) {
        this.pinballXInstallationFolder = new File(store.get(PINBALLX_INSTALLATION_DIR_INST_DIR));
        frontendType = FrontendType.PinballX;
      }
      //PinballY Folder
      if (store.containsKey(PINBALLY_INSTALLATION_DIR_INST_DIR) && !StringUtils.isEmpty(store.get(PINBALLY_INSTALLATION_DIR_INST_DIR))) {
        this.pinballYInstallationFolder = new File(store.get(PINBALLY_INSTALLATION_DIR_INST_DIR));
        frontendType = FrontendType.PinballY;
      }
      //PinUP Popper Folder
      if (store.containsKey(PINUP_SYSTEM_INSTALLATION_DIR_INST_DIR) && !StringUtils.isEmpty(store.get(PINUP_SYSTEM_INSTALLATION_DIR_INST_DIR))) {
        this.pinupInstallationFolder = new File(store.get(PINUP_SYSTEM_INSTALLATION_DIR_INST_DIR));
        frontendType = FrontendType.Popper;
      }

      if (!getRawImageExtractionFolder().exists()) {
        boolean mkdirs = getRawImageExtractionFolder().mkdirs();
        if (!mkdirs) {
          LOG.error("Failed to create b2s image directory " + getRawImageExtractionFolder().getAbsolutePath());
        }
      }

      if (!getCroppedImageFolder().exists()) {
        boolean mkdirs = getCroppedImageFolder().mkdirs();
        if (!mkdirs) {
          LOG.error("Failed to create b2s crops directory " + getCroppedImageFolder().getAbsolutePath());
        }
      }

      this.backupFolder = new File(RESOURCES, "backups");
      if (!this.backupFolder.exists() && !this.backupFolder.mkdirs()) {
        LOG.error("Failed to create backup folder " + this.backupFolder.getAbsolutePath());
      }

      this.backglassServerFolder = resolveBackglassServerFolder();
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

  public void setFrontendType(FrontendType frontendType) {
    this.frontendType = frontendType;
  }

  private void logSystemInfo() {
    LOG.info("********************************* Installation Overview ***********************************************");
    LOG.info(formatPathLog("Locale", Locale.getDefault().getDisplayName()));
    LOG.info(formatPathLog("TimeZone", TimeZone.getDefault().getDisplayName()));
    LOG.info(formatPathLog("Charset", Charset.defaultCharset().displayName()));
    LOG.info(formatPathLog("Frontend Type", this.getFrontendType().name()));
    if (pinupInstallationFolder != null) {
      LOG.info(formatPathLog("PinupPopper Folder", this.pinupInstallationFolder));
    }
    if (pinballXInstallationFolder != null) {
      LOG.info(formatPathLog("PinballX Folder", this.pinballXInstallationFolder));
    }
    if (pinballYInstallationFolder != null) {
      LOG.info(formatPathLog("PinballY Folder", this.pinballYInstallationFolder));
    }
    if (standaloneInstallationFolder != null) {
      LOG.info(formatPathLog("Standalone VPX Folder", this.standaloneInstallationFolder));
    }
    LOG.info(formatPathLog("B2S Server", this.getBackglassServerFolder()));
    LOG.info(formatPathLog("Pinemhi Command", this.getPinemhiCommandFile()));
    LOG.info(formatPathLog("B2S Extraction Folder", this.getRawImageExtractionFolder()));
    LOG.info(formatPathLog("B2S Cropped Folder", this.getCroppedImageFolder()));
    LOG.info(formatPathLog("Service Version", VPinStudioServer.class.getPackage().getImplementationVersion()));
    LOG.info("*******************************************************************************************************");
  }

  public static String formatPathLog(String label, String value) {
    return formatPathLog(label, value, null, null);
  }

  private static String formatPathLog(String label, @Nullable File file) {
    if (file == null) {
      return formatPathLog(label, "-");
    }
    return formatPathLog(label, file.getAbsolutePath(), file.exists(), file.canRead());
  }

  public File getRawImageExtractionFolder() {
    return new File(RESOURCES, RAW_MEDIA_FOLDER);
  }

  public File getCroppedImageFolder() {
    return new File(RESOURCES, CROPPED_MEDIA_FOLDER);
  }

  public File getBackupFolder() {
    return backupFolder;
  }

  public File getBackglassServerFolder() {
    return backglassServerFolder;
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

  public File getPinupInstallationFolder() {
    return pinupInstallationFolder;
  }

  public File getPinballXInstallationFolder() {
    return pinballXInstallationFolder;
  }

  public File getPinballYInstallationFolder() {
    return pinballYInstallationFolder;
  }

  public File getStandaloneInstallationFolder() {
    return standaloneInstallationFolder;
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
    return VersionUtil.getVersion();
  }

  public List<String> getCompetitionBadges() {
    File folder = new File(RESOURCES, COMPETITION_BADGES);
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
    File folder = new File(RESOURCES, COMPETITION_BADGES);
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

  public boolean isWindowOpened(String name) {
    List<DesktopWindow> windows = WindowUtils.getAllWindows(true);
    return windows.stream().anyMatch(wdw -> StringUtils.containsIgnoreCase(wdw.getTitle(), name));
  }


  public boolean isPinballEmulatorRunning() {
    return isVPXRunning(getProcesses()) || isFPRunning(getProcesses());
  }

  public boolean isVPXRunning(List<ProcessHandle> allProcesses) {
    for (ProcessHandle p : allProcesses) {
      if (p.info().command().isPresent()) {
        String cmdName = p.info().command().get();
        String fileName = cmdName.substring(cmdName.lastIndexOf("\\") + 1);
        if (fileName.toLowerCase().contains("Visual Pinball".toLowerCase()) || fileName.toLowerCase().contains("VisualPinball".toLowerCase()) || fileName.toLowerCase().contains("VPinball".toLowerCase())) {
          LOG.info("Found active VPX process: " + fileName);
          return true;
        }
      }
    }
    return false;
  }

  public boolean isFPRunning(List<ProcessHandle> allProcesses) {
    for (ProcessHandle p : allProcesses) {
      if (p.info().command().isPresent()) {
        String cmdName = p.info().command().get();
        String fileName = cmdName.substring(cmdName.lastIndexOf("\\") + 1);
        if (fileName.toLowerCase().contains("Future Pinball")) {
          LOG.info("Found active FP process: " + fileName);
          return true;
        }
      }
    }
    return false;
  }

  public ArchiveType getArchiveType() {
    return archiveType;
  }

  public void setArchiveType(ArchiveType archiveType) {
    this.archiveType = archiveType;
  }

  public List<MonitorInfo> getMonitorInfos() {
    List<MonitorInfo> monitors = MonitorInfoUtil.getMonitors();

//    //for pinup popper, all monitors left to the primary are irrelevant
//    if (frontendType.equals(FrontendType.Popper)) {
//      boolean primaryFound = false;
//      List<MonitorInfo> filtered = new ArrayList<>();
//      for (MonitorInfo monitor : monitors) {
//        if (!monitor.isPrimary() && !primaryFound) {
//          continue;
//        }
//
//        if (monitor.isPrimary()) {
//          primaryFound = true;
//        }
//        filtered.add(monitor);
//      }
//
//      monitors = filtered;
//    }
    return monitors;
  }

  public MonitorInfo getMonitor(int monitor) {
    List<MonitorInfo> monitors = MonitorInfoUtil.getMonitors();
    if (monitor < monitors.size()) {
      return monitors.get(monitor);
    }
    return null;
  }

  /**
   * Find a monitor by the windows index (used in VPX), 
   * TODO how to match it with MonitorInfoUtils.getMonitor(), this is still unknown, uses getId() for time being but incorrect
   */
  public MonitorInfo getMonitorFromOS(int monitor) {
    List<MonitorInfo> monitors = MonitorInfoUtil.getMonitors();
    for (MonitorInfo monitorInfo : monitors) {
      if (monitorInfo.getId() == monitor) {
        return monitorInfo;
      }
    }
    return null;
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

  public boolean waitForProcess(String name, int seconds) {
    return waitForProcess(name, seconds, 0);
  }

  public boolean waitForProcess(String name, int seconds, int postDelayMs) {
    return waitFor(() -> isProcessRunning(name), "process : " + name, seconds, postDelayMs);
  }

  public boolean waitForWindow(String name, int seconds, int postDelayMs) {
    return waitFor(() -> isWindowOpened(name), "window : " + name, seconds, postDelayMs);
  }

  public boolean waitFor(Supplier<Boolean> isRunning, String name, int seconds, int postDelayMs) {
    try {
      ExecutorService executor = Executors.newSingleThreadExecutor();
      Future<Boolean> submit = executor.submit(new Callable<Boolean>() {
        @Override
        public Boolean call() throws Exception {
          while (!isRunning.get()) {
            Thread.sleep(1000);
          }
          LOG.info("Found waiting for {}", name);
          if (postDelayMs > 0) {
            Thread.sleep(postDelayMs);
          }
          return true;
        }
      });
      return submit.get(seconds, TimeUnit.SECONDS);
    }
    catch (Exception e) {
      LOG.error("Waiting for {}, failed: {}", name, e.getMessage());
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
    return new File(RESOURCES, ScoringDB.SCORING_DB_NAME);
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

  public NVRamsInfo resetNvRams() {
    return ServerUpdatePreProcessing.synchronizeNVRams(true);
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    try {
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
    catch (Exception e) {
      LOG.error("Failed to initialize system service: {}", e.getMessage(), e);
    }
    LOG.info("{} initialization finished.", this.getClass().getSimpleName());
  }
}