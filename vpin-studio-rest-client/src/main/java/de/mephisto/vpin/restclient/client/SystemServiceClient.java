package de.mephisto.vpin.restclient.client;

import de.mephisto.vpin.restclient.ScreenInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.RestTemplate;

import java.util.Date;

/*********************************************************************************************************************
 * System
 ********************************************************************************************************************/
public class SystemServiceClient extends VPinStudioClientService {
  private final static Logger LOG = LoggerFactory.getLogger(VPinStudioClient.class);

  private ScreenInfo screenInfo;

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
    return url.contains("localhost") || url.contains("127.0.0.1");
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

  public String version() {
    try {
      final RestTemplate restTemplate = new RestTemplate();
      return restTemplate.getForObject(getRestClient().getBaseUrl() + API + "system/version", String.class);
    } catch (Exception e) {
      LOG.error("Get version failed for " + getRestClient().getBaseUrl());
    }
    return null;
  }

  public ScreenInfo getScreenInfo() {
    try {
      if (screenInfo == null) {
        screenInfo = getRestClient().getCached(API + "system/screens", ScreenInfo.class);
      }
      return screenInfo;
    } catch (Exception e) {
      LOG.error("Failed to read screen info: " + e.getMessage());
    }
    return null;
  }

  public void clearCache() {
    this.screenInfo = null;
    getScreenInfo();
  }
}
