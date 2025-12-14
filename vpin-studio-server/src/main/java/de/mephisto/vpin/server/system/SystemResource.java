package de.mephisto.vpin.server.system;

import de.mephisto.vpin.commons.fx.ServerFX;
import de.mephisto.vpin.commons.fx.UIDefaults;
import de.mephisto.vpin.commons.utils.NirCmd;
import de.mephisto.vpin.commons.utils.Updater;
import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.preferences.ServerSettings;
import de.mephisto.vpin.restclient.system.*;
import de.mephisto.vpin.restclient.util.SystemCommandExecutor;
import de.mephisto.vpin.restclient.util.SystemUtil;
import de.mephisto.vpin.restclient.util.ZipUtil;
import de.mephisto.vpin.server.VPinStudioServer;
import de.mephisto.vpin.server.frontend.FrontendConnector;
import de.mephisto.vpin.server.frontend.FrontendService;
import de.mephisto.vpin.server.frontend.popper.PinUPConnector;
import de.mephisto.vpin.server.preferences.PreferencesService;
import de.mephisto.vpin.server.util.RequestUtil;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.zip.ZipOutputStream;

import static de.mephisto.vpin.server.VPinStudioServer.API_SEGMENT;
import static de.mephisto.vpin.server.system.SystemService.COMPETITION_BADGES;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

@RestController
@RequestMapping(API_SEGMENT + "system")
public class SystemResource {
  private final static Logger LOG = LoggerFactory.getLogger(SystemResource.class);

  private final Date startupTime = new Date();

  @Autowired
  private SystemService systemService;

  @Autowired
  private PreferencesService preferencesService;

  @Autowired
  private FrontendService frontendService;

  @Autowired
  private SystemBackupService systemBackupService;

  @PostMapping("/backup/create")
  public String createBackup() {
    try {
      return systemBackupService.create();
    }
    catch (Exception e) {
      LOG.error("Backup creation failed: {}", e.getMessage(), e);
      throw new ResponseStatusException(INTERNAL_SERVER_ERROR, "Backup creation failed: " + e.getMessage());
    }
  }

  @PostMapping("/backup/restore")
  public Boolean restoreBackup(@RequestParam(value = "file") MultipartFile file,
                               @RequestParam(value = "backupDescriptor") String backupDescriptor) {
    try {
      String json = new String(file.getBytes());
      return systemBackupService.restore(json, backupDescriptor);
    }
    catch (Exception e) {
      LOG.error("Backup restore failed: {}", e.getMessage(), e);
      throw new ResponseStatusException(INTERNAL_SERVER_ERROR, "Backup restoring failed: " + e.getMessage());
    }
  }

  @GetMapping("/startupTime")
  public Date startupTime() {
    return startupTime;
  }

  @GetMapping("/pausemenu")
  public boolean pauseMenu() {
    ServerFX.getInstance().togglePauseMenu();
    return true;
  }

  @GetMapping("/pausemenu/test/{gameId}/{duration}")
  public boolean testPauseMenu(@PathVariable("gameId") int gameId, @PathVariable("duration") int duration) {
    ServerFX.getInstance().testPauseMenu(gameId, duration);
    return true;
  }

  @GetMapping("/logs")
  @ResponseBody
  public String logs() {
    try {
      Path filePath = Path.of("./vpin-studio-server.log");
      return Files.readString(filePath);
    }
    catch (IOException e) {
      LOG.error("Error reading log: " + e.getMessage(), e);
    }
    return "";
  }

  @GetMapping("/download/logs")
  public void downloadArchiveFile(HttpServletResponse response) {
    InputStream in = null;
    OutputStream out = null;
    try {
      File target = new File("vpin-studio-logs.zip");
      if (target.exists() && !target.delete()) {
        throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to delete existing logs archive.");
      }

      FileOutputStream fos = new FileOutputStream(target);
      ZipOutputStream zipOut = new ZipOutputStream(fos);

      File logs = new File("./");
      File[] logFiles = logs.listFiles((dir, name) -> name.endsWith(".log"));
      for (File logFile : logFiles) {
        ZipUtil.zipFile(logFile, logFile.getName(), zipOut);
      }

//      File studioDB = new File(RESOURCES, "vpin-studio.db");
//      if (studioDB.exists()) {
//        ZipUtil.zipFile(studioDB, studioDB.getName(), zipOut);
//      }

      FrontendConnector frontendConnector = frontendService.getFrontendConnector();
      if (frontendConnector instanceof PinUPConnector) {
        PinUPConnector pinUPConnector = (PinUPConnector) frontendConnector;
        File popperDB = pinUPConnector.getDatabaseFile();
        if (popperDB.exists()) {
          ZipUtil.zipFile(popperDB, popperDB.getName(), zipOut);
        }
      }

      zipOut.close();
      fos.close();

      in = new FileInputStream(target);
      out = response.getOutputStream();
      IOUtils.copy(in, out);
      response.flushBuffer();
      LOG.info("Finished exporting log files.");
    }
    catch (IOException ex) {
      LOG.info("Error writing logs: " + ex.getLocalizedMessage(), ex);
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "IOError writing file to output stream");
    }
    finally {
      try {
        if (in != null) {
          in.close();
        }
        if (out != null) {
          out.close();
        }
      }
      catch (IOException e) {
        LOG.error("Erorr closing streams: " + e.getMessage(), e);
      }
    }
  }

  @GetMapping("")
  public SystemId system() {
    SystemId id = new SystemId();
    id.setSystemId(SystemUtil.getUniqueSystemId());

    String name = (String) preferencesService.getPreferenceValue(PreferenceNames.SYSTEM_NAME);
    if (StringUtils.isEmpty(name)) {
      name = UIDefaults.VPIN_NAME;
    }
    id.setSystemName(name);

    id.setVersion(systemService.getVersion());
    return id;
  }

  @GetMapping("/info")
  public SystemSummary info() {
    SystemSummary info = new SystemSummary();
    try {
      info.setScreenInfos(systemService.getMonitorInfos());
      info.setBackupType(systemService.getBackupType());
      info.setSystemId(SystemUtil.getUniqueSystemId());
    }
    catch (Exception e) {
      LOG.error("Failed to read system info: " + e.getMessage());
    }
    return info;
  }

  @GetMapping("/mute/{mute}")
  public boolean muteSystem(@PathVariable("mute") int mute) {
    NirCmd.muteSystem(mute == 1);
    return true;
  }


  @GetMapping("/resetnvrams")
  public NVRamsInfo resetNvRams() {
    return systemService.resetNvRams();
  }


  @GetMapping("/scoringdb")
  public ScoringDB get() {
    return systemService.getScoringDatabase();
  }

  @GetMapping("/shutdown")
  public boolean shutdown() {
    systemService.shutdown();
    return true;
  }

  @GetMapping("/systemshutdown")
  public boolean systemShutdown() {
    systemService.systemShutdown();
    return true;
  }

  @GetMapping("/backup")
  public String backup() {
    return systemService.backup();
  }

  @GetMapping("/maintenance/{enabled}")
  public boolean setMaintenanceMode(@PathVariable("enabled") boolean enabled, HttpServletRequest request) {
    boolean b = systemService.setMaintenanceMode(enabled);
    final boolean remote = request != null && request.getRequestURI() != null && !(request.getRequestURL().toString().contains("localhost") || request.getRequestURL().toString().contains("127.0.0.1"));

    new Thread(() -> {
      if (enabled) {
        frontendService.killFrontend();
      }
      else if (remote) {
        ServerSettings serverSettings = preferencesService.getJsonPreference(PreferenceNames.SERVER_SETTINGS, ServerSettings.class);
        if (serverSettings.isLaunchPopperOnExit()) {
          frontendService.restartFrontend();
        }
      }
    }).start();
    return b;
  }

  @GetMapping("/update/{version}/download/start")
  public boolean downloadUpdate(@PathVariable("version") String version) {
    new Thread(() -> {
      Thread.currentThread().setName("Server Update Downloader");
      Updater.downloadUpdate(version, Updater.SERVER_ZIP);
    }).start();
    return true;
  }

  @GetMapping("/clientupdate/{version}/download/start")
  public boolean downloadClientUpdate(@PathVariable("version") String version) {
    new Thread(() -> {
      Thread.currentThread().setName("Client Update Downloader");
      Updater.downloadUpdate(version, Updater.UI_ZIP);
    }).start();
    return true;
  }

  @GetMapping("/clientupdate/install")
  public boolean installRemoteClientUpdate() {
    systemService.killProcesses("javaw.exe");
    File uiZip = new File("./", Updater.UI_ZIP);
    if (uiZip.exists()) {
      if (!ZipUtil.unzip(uiZip, new File("./"), null)) {
        LOG.error("Extraction of " + uiZip.getAbsolutePath() + " failed.");
        return false;
      }
      if (!uiZip.delete()) {
        LOG.error("Failed to delete client archive: " + uiZip.getAbsolutePath());
        return false;
      }
      return true;
    }
    else {
      LOG.error("Failed to download client UI, missing file.");
    }
    return false;
  }

  @GetMapping("/update/download/status")
  public int updateDownloadStatus() {
    return Updater.getDownloadProgress(Updater.SERVER_ZIP, Updater.SERVER_ZIP_SIZE);
  }

  @GetMapping("/clientupdate/download/status")
  public int updateClientDownloadStatus() {
    return Updater.getDownloadProgress(Updater.UI_ZIP, Updater.UI_ZIP_SIZE);
  }

  @GetMapping("/update/install")
  public boolean installServerUpdate() throws IOException {
    File serverUpdate = new File("./VPin-Studio-Server.zip");
//    if (!serverUpdate.exists()) {
//      return false;
//    }
//    Updater.installServerUpdate();
    new Thread(() -> {
      try {
        Thread.sleep(2000);
        systemService.shutdown();
      }
      catch (InterruptedException e) {
        //ignore
      }
    }).start();
    return true;
  }

  @GetMapping("/restart")
  public boolean restart() throws IOException {
    de.mephisto.vpin.restclient.util.FileUtils.writeBatch("server-restart.bat", "timeout /T 5 /nobreak\ncd /d %~dp0\nwscript server.vbs\nexit");
    List<String> commands = Arrays.asList("cmd", "/c", "start", "server-restart.bat");
    SystemCommandExecutor executor = new SystemCommandExecutor(commands);
    executor.setDir(new File("./"));
    executor.executeCommandAsync();
    new Thread(() -> {
      try {
        Thread.sleep(1000);
        systemService.shutdown();
      }
      catch (InterruptedException e) {
        //ignore
      }
    }).start();
    return true;
  }

  @GetMapping("/version")
  public String version() {
    return systemService.getVersion();
  }

  @GetMapping("/features")
  public FeaturesInfo getFeatures() {
    return VPinStudioServer.Features;
  }


  @GetMapping("/badges")
  public List<String> getCompetitionBadges() {
    return systemService.getCompetitionBadges();
  }

  @PostMapping("/text")
  public SystemData getText(@RequestBody SystemData data) {
    File file = new File(data.getPath());
    if (file.exists()) {
      try {
        String s = FileUtils.readFileToString(file, Charset.defaultCharset());
        data.setData(s);
        return data;
      }
      catch (IOException e) {
        LOG.error("Failed to read file " + data.getPath() + ": " + e.getMessage(), e);
      }
    }
    LOG.warn("File " + data.getPath() + " does not exists.");
    return data;
  }

  @GetMapping("/badge/{name}")
  public ResponseEntity<byte[]> getBadge(@PathVariable("name") String imageName) throws Exception {
    File folder = new File(SystemService.RESOURCES, COMPETITION_BADGES);
    File[] files = folder.listFiles((dir, name) -> URLEncoder.encode(FilenameUtils.getBaseName(name), StandardCharsets.UTF_8).equals(imageName));
    if (files != null) {
      return RequestUtil.serializeImage(files[0]);
    }
    return ResponseEntity.notFound().build();
  }
}
