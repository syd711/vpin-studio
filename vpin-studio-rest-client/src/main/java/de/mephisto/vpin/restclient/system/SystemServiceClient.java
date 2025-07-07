package de.mephisto.vpin.restclient.system;

import de.mephisto.vpin.restclient.RestClient;
import de.mephisto.vpin.restclient.client.VPinStudioClient;
import de.mephisto.vpin.restclient.client.VPinStudioClientService;
import de.mephisto.vpin.restclient.games.GameRepresentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.RestTemplate;

import java.util.Date;

/*********************************************************************************************************************
 * System
 ********************************************************************************************************************/
public class SystemServiceClient extends VPinStudioClientService {
  private final static Logger LOG = LoggerFactory.getLogger(VPinStudioClient.class);

  public SystemServiceClient(VPinStudioClient client) {
    super(client);
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

  public String backup() {
    final RestTemplate restTemplate = new RestTemplate();
    return restTemplate.getForObject(getRestClient().getBaseUrl() + API + "system/backup", String.class);
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

  public void systemShutdown() {
    final RestTemplate restTemplate = new RestTemplate();
    restTemplate.getForObject(getRestClient().getBaseUrl() + API + "system/systemshutdown", Boolean.class);
  }

  public boolean autostartInstalled() {
    final RestTemplate restTemplate = new RestTemplate();
    return Boolean.TRUE.equals(restTemplate.getForObject(getRestClient().getBaseUrl() + API + "system/autostart/installed", Boolean.class));
  }

  public boolean autostartInstall() {
    final RestTemplate restTemplate = new RestTemplate();
    return Boolean.TRUE.equals(restTemplate.getForObject(getRestClient().getBaseUrl() + API + "system/autostart/install", Boolean.class));
  }

  public boolean autostartUninstall() {
    final RestTemplate restTemplate = new RestTemplate();
    return Boolean.TRUE.equals(restTemplate.getForObject(getRestClient().getBaseUrl() + API + "system/autostart/uninstall", Boolean.class));
  }

  public boolean isDotNetInstalled() {
    final RestTemplate restTemplate = new RestTemplate();
    return Boolean.TRUE.equals(restTemplate.getForObject(getRestClient().getBaseUrl() + API + "system/dotnet", Boolean.class));
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
      return getSystemSummary().getPrimaryScreen();
    }
    else {
      return getSystemSummary().getScreenInfo(screenId);
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
