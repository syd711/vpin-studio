package de.mephisto.vpin.server.system;

import de.mephisto.vpin.commons.ServerInstallationUtil;
import de.mephisto.vpin.commons.utils.Updater;
import de.mephisto.vpin.restclient.ScreenInfo;
import de.mephisto.vpin.restclient.SystemData;
import de.mephisto.vpin.server.util.RequestUtil;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Date;
import java.util.List;

import static de.mephisto.vpin.server.VPinStudioServer.API_SEGMENT;
import static de.mephisto.vpin.server.system.SystemService.COMPETITION_BADGES;

@RestController
@RequestMapping(API_SEGMENT + "system")
public class SystemResource {
  private final static Logger LOG = LoggerFactory.getLogger(SystemResource.class);

  private final Date startupTime = new Date();

  @Autowired
  private SystemService systemService;

  @GetMapping("/startupTime")
  public Date startupTime() {
    return startupTime;
  }

  @GetMapping("/logs")
  @ResponseBody
  public String logs() {
    try {
      Path filePath = Path.of("./vpin-studio-server.log");
      return Files.readString(filePath);
    } catch (IOException e) {
      LOG.error("Error reading log: " + e.getMessage(), e);
    }
    return "";
  }

  @GetMapping("/shutdown")
  public boolean shutdown() {
    System.exit(0);
    return true;
  }

  @GetMapping("/update/{version}/download/start")
  public boolean downloadUpdate(@PathVariable("version") String version) {
    return Updater.downloadUpdate(version, Updater.SERVER_ZIP);
  }

  @GetMapping("/update/download/status")
  public int updateDownloadStatus() {
    return Updater.getDownloadProgress(Updater.SERVER_ZIP, Updater.SERVER_ZIP_SIZE);
  }

  @GetMapping("/update/install")
  public boolean installUpdate() throws IOException {
    return Updater.installServerUpdate();
  }

  @GetMapping("/autostart/installed")
  public boolean autostart() {
    return ServerInstallationUtil.isInstalled();
  }

  @GetMapping("/autostart/install")
  public boolean installService() {
    return ServerInstallationUtil.install();
  }

  @GetMapping("/autostart/uninstall")
  public boolean uninstallService() {
    return ServerInstallationUtil.uninstall();
  }

  @GetMapping("/version")
  public String version() {
    return systemService.getVersion();
  }

  @GetMapping("/badges")
  public List<String> getCompetitionBadges() {
    return systemService.getCompetitionBadges();
  }

  @GetMapping("/dotnet")
  public boolean isDotNetInstalled() {
    return systemService.isDotNetInstalled();
  }

  @GetMapping("/screens")
  public ScreenInfo screens() {
    return systemService.getScreenInfo();
  }

  @PostMapping("/text")
  public SystemData getText(@RequestBody SystemData data) {
    File file = new File(data.getPath());
    if (file.exists()) {
      try {
        String s = FileUtils.readFileToString(file, Charset.defaultCharset());
        data.setData(s);
        return data;
      } catch (IOException e) {
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
