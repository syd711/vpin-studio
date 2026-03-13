package de.mephisto.vpin.restclient.system;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import de.mephisto.vpin.restclient.RestClient;
import de.mephisto.vpin.restclient.backups.StudioBackupDescriptor;
import de.mephisto.vpin.restclient.client.VPinStudioClient;
import de.mephisto.vpin.restclient.client.VPinStudioClientService;
import de.mephisto.vpin.restclient.games.GameRepresentation;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.lang.invoke.MethodHandles;
import java.util.Date;
import java.util.HashMap;

/*********************************************************************************************************************
 * System
 ********************************************************************************************************************/
public class SystemServiceClient extends VPinStudioClientService {
  private final static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  public SystemServiceClient(VPinStudioClient client) {
    super(client);
  }

  private final static ObjectMapper objectMapper = new ObjectMapper();

  static {
    objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
    objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
  }

  public String backupSystem() {
    final RestTemplate restTemplate = new RestTemplate();
    return restTemplate.postForObject(getRestClient().getBaseUrl() + API + "system/backup/create", new HashMap<>(), String.class);
  }

  public boolean restoreSystemBackup(@NonNull File file, @NonNull StudioBackupDescriptor backupDescriptor) throws Exception {
    try {
      String url = getRestClient().getBaseUrl() + API + "system/backup/restore";
      HttpEntity upload = createUpload(file, -1, null, null, null);
      LinkedMultiValueMap<String, Object> map = (LinkedMultiValueMap<String, Object>) upload.getBody();

      String backupDescriptorJson = objectMapper.writeValueAsString(backupDescriptor);
      map.add("backupDescriptor", backupDescriptorJson);
      new RestTemplate().exchange(url, HttpMethod.POST, upload, Boolean.class);
      finalizeUpload(upload);
      return true;
    }
    catch (Exception e) {
      LOG.error("Backup upload failed: " + e.getMessage(), e);
      throw e;
    }
  }

  public Date getStartupTime() {
    return getRestClient().get(API + "system/startupTime", Date.class);
  }

  public String logs() {
    final RestTemplate restTemplate = new RestTemplate();
    return restTemplate.getForObject(getRestClient().getBaseUrl() + API + "system/logs", String.class);
  }

  public boolean isLocal() {
    String url = getRestClient().getBaseUrl();
    return url.contains("localhost") || url.contains("127.0.0.1");
  }

  public void shutdown() {
    final RestTemplate restTemplate = new RestTemplate();
    restTemplate.getForObject(getRestClient().getBaseUrl() + API + "system/shutdown", Boolean.class);
  }

  public void restart() {
    final RestTemplate restTemplate = new RestTemplate();
    restTemplate.getForObject(getRestClient().getBaseUrl() + API + "system/restart", Boolean.class);
  }

  public void mute(boolean mute) {
    final RestTemplate restTemplate = new RestTemplate();
    restTemplate.getForObject(getRestClient().getBaseUrl() + API + "system/mute/" + (mute ? "1" : "0"), Boolean.class);
  }

  public boolean isMuted() {
    final RestTemplate restTemplate = new RestTemplate();
    return restTemplate.getForObject(getRestClient().getBaseUrl() + API + "system/muted", Boolean.class);
  }

  public void systemShutdown() {
    final RestTemplate restTemplate = new RestTemplate();
    restTemplate.getForObject(getRestClient().getBaseUrl() + API + "system/systemshutdown", Boolean.class);
  }

  public void startServerUpdate(String version) {
    final RestTemplate restTemplate = new RestTemplate();
    restTemplate.getForObject(getRestClient().getBaseUrl() + API + "system/update/" + version + "/download/start", Boolean.class);
  }

  public int getServerUpdateProgress() {
    final RestTemplate restTemplate = new RestTemplate();
    return restTemplate.getForObject(getRestClient().getBaseUrl() + API + "system/update/download/status", Integer.class);
  }

  public boolean installServerUpdate() {
    final RestTemplate restTemplate = new RestTemplate();
    return restTemplate.getForObject(getRestClient().getBaseUrl() + API + "system/update/install", Boolean.class);
  }

  public int getRemoteClientProgress() {
    final RestTemplate restTemplate = new RestTemplate();
    return restTemplate.getForObject(getRestClient().getBaseUrl() + API + "system/clientupdate/download/status", Integer.class);
  }

  public void startRemoteClientUpdate(String version) {
    final RestTemplate restTemplate = new RestTemplate();
    restTemplate.getForObject(getRestClient().getBaseUrl() + API + "system/clientupdate/" + version + "/download/start", Boolean.class);
  }

  public boolean installRemoteClientUpdate() {
    final RestTemplate restTemplate = new RestTemplate();
    return restTemplate.getForObject(getRestClient().getBaseUrl() + API + "system/clientupdate/install", Boolean.class);
  }

  public String getVersion() {
    try {
      final RestTemplate restTemplate = RestClient.createTimeoutBasedTemplate(2000);
      return restTemplate.getForObject(getRestClient().getBaseUrl() + API + "system/version", String.class);
    } catch (Exception e) {
      LOG.info("Get version failed for {} ({})", getRestClient().getBaseUrl(), e.getMessage());
    }
    return null;
  }

  public FeaturesInfo getFeatures() {
    return getRestClient().get(API + "system/features", FeaturesInfo.class);
  }

  public SystemId getSystemId() {
    try {
      return getRestClient().getCached(API + "system", SystemId.class);
    } catch (Exception e) {
      // legacy system, no system id available so build a dummy one
      SystemId id = new SystemId();
      id.setVersion(getVersion());
      id.setSystemName("-need upgrade-");
      return id;
    }
  }

  public SystemSummary getSystemSummary() {
    return getSystemSummary(false);
  }

  public SystemSummary getSystemSummary(boolean reloadData) {
    return getRestClient().getCached(API + "system/info", SystemSummary.class, reloadData);
  }

  public MonitorInfo getScreenInfo(int screenId) {
    if (screenId == -1) {
      return getSystemSummary().getPrimaryMonitor();
    }
    else {
      return getSystemSummary().getMonitorInfo(screenId);
    }
  }

  public ScoringDB getScoringDatabase() {
    return getRestClient().getCached(API + "system/scoringdb", ScoringDB.class);
  }

  public NVRamsInfo resetNvRams() {
    return getRestClient().get(API + "system/resetnvrams", NVRamsInfo.class);
  }

  public void setMaintenanceMode(boolean maintenanceMode) {
    final RestTemplate restTemplate = new RestTemplate();
    String url = getRestClient().getBaseUrl() + API + "system/maintenance/" + maintenanceMode;
    LOG.info("HTTP GET " + url);
    restTemplate.getForObject(getRestClient().getBaseUrl() + API + "system/maintenance/" + maintenanceMode, Boolean.class);
  }

  public SystemData getSystemData(String filename) {
    try {
      SystemData data = new SystemData();
      data.setPath(filename);
      return getRestClient().post(API + "system/text", data, SystemData.class);
    } catch (Exception e) {
      LOG.error("Failed to read system file: " + e.getMessage(), e);
    }
    return null;
  }

  public void clearCache() {
    getRestClient().clearCache(API + "system/info");
  }

  public void testPauseMenu(GameRepresentation game, Integer value) {
    final RestTemplate restTemplate = new RestTemplate();
    restTemplate.getForObject(getRestClient().getBaseUrl() + API + "system/pausemenu/test/" + game.getId() + "/" + value, Boolean.class);
  }
}
