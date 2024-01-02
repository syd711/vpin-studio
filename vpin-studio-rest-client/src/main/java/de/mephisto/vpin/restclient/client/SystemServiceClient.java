package de.mephisto.vpin.restclient.client;

import de.mephisto.vpin.restclient.system.ScreenInfo;
import de.mephisto.vpin.restclient.system.SystemData;
import de.mephisto.vpin.restclient.system.SystemSummary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.RestTemplate;

import java.util.Date;

/*********************************************************************************************************************
 * System
 ********************************************************************************************************************/
public class SystemServiceClient extends VPinStudioClientService {
  private final static Logger LOG = LoggerFactory.getLogger(VPinStudioClient.class);

  private SystemSummary systemSummary;

  SystemServiceClient(VPinStudioClient client) {
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
    return false; //url.contains("localhost") || url.contains("127.0.0.1");
  }

  public void shutdown() {
    final RestTemplate restTemplate = new RestTemplate();
    restTemplate.getForObject(getRestClient().getBaseUrl() + API + "system/shutdown", Boolean.class);
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
    return restTemplate.getForObject(getRestClient().getBaseUrl() + API + "system/update/download/client/status", Integer.class);
  }

  public void startRemoteClientUpdate(String version) {
    final RestTemplate restTemplate = new RestTemplate();
    restTemplate.getForObject(getRestClient().getBaseUrl() + API + "system/update/" + version + "/download/client/start", Boolean.class);
  }

  public boolean installRemoteClientUpdate() {
    final RestTemplate restTemplate = new RestTemplate();
    return restTemplate.getForObject(getRestClient().getBaseUrl() + API + "system/update/client/install", Boolean.class);
  }

  public String getVersion() {
    try {
      final RestTemplate restTemplate = new RestTemplate();
      return restTemplate.getForObject(getRestClient().getBaseUrl() + API + "system/version", String.class);
    } catch (Exception e) {
      LOG.error("Get version failed for " + getRestClient().getBaseUrl());
    }
    return null;
  }

  public SystemSummary getSystemSummary() {
    return getRestClient().getCached(API + "system/info", SystemSummary.class);
  }

  public void setMaintenanceMode(boolean maintenanceMode) {
    final RestTemplate restTemplate = new RestTemplate();
    String url = getRestClient().getBaseUrl() + API + "system/maintenance/" + maintenanceMode;
    LOG.info("HTTP GET " + url);
    restTemplate.getForObject(getRestClient().getBaseUrl() + API + "system/maintenance/" + maintenanceMode, Boolean.class);
  }

  public ScreenInfo getScreenInfo() {
    SystemSummary summary = getRestClient().getCached(API + "system/info", SystemSummary.class);
    return summary.getMainScreenInfo();
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

}
