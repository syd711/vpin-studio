package de.mephisto.vpin.ui.monitor;

import de.mephisto.vpin.restclient.frontend.FrontendPlayerDisplay;
import de.mephisto.vpin.restclient.system.MonitorInfo;
import javafx.scene.image.Image;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static de.mephisto.vpin.restclient.client.VPinStudioClient.API;
import static de.mephisto.vpin.ui.Studio.client;

public class MonitoringManager {
  private final static Logger LOG = LoggerFactory.getLogger(MonitoringManager.class);
  private final static MonitoringManager INSTANCE = new MonitoringManager();

  private Map<FrontendPlayerDisplay, CachedImage> screenCache = new ConcurrentHashMap<>();
  private Map<MonitorInfo, CachedImage> monitorCache = new ConcurrentHashMap<>();
  private int cacheTimeSeconds = 2;
  private int recordingRefresh = 2;
  private int monitoringRefresh = 2;

  private MonitoringManager() {
    //ignore
  }

  public static MonitoringManager getInstance() {
    return INSTANCE;
  }

  public void setRecordingRefreshIntervalSec(int seconds) {
    recordingRefresh = seconds;
    refreshCachingTime();
  }

  public void setMonitoringRefreshIntervalSec(int seconds) {
    monitoringRefresh = seconds;
    refreshCachingTime();
  }

  private void refreshCachingTime() {
    cacheTimeSeconds = Math.min(recordingRefresh, monitoringRefresh);
    LOG.info("Monitoring refresh time set to {} seconds", cacheTimeSeconds);
  }

  public Image getRecordableScreenImage(FrontendPlayerDisplay recordingScreen) {
    if (!screenCache.containsKey(recordingScreen) || !screenCache.get(recordingScreen).isValid()) {
      long start = System.currentTimeMillis();
      Image imageCached = new Image(client.getRestClient().getBaseUrl() + API + "recorder/preview/" + recordingScreen.getScreen().name());
      long duration = System.currentTimeMillis() - start;
//      LOG.info("Refreshed {} / {}ms", recordingScreen, duration);
      screenCache.put(recordingScreen, new CachedImage(imageCached));
    }

    return screenCache.get(recordingScreen).getImage();
  }


  public Image getMonitorImage(MonitorInfo monitorInfo) {
    if (!monitorCache.containsKey(monitorInfo) || !monitorCache.get(monitorInfo).isValid()) {
      Image imageCached = new Image(client.getRestClient().getBaseUrl() + API + "recorder/previewmonitor/" + monitorInfo.getId());
      monitorCache.put(monitorInfo, new CachedImage(imageCached));
    }

    return monitorCache.get(monitorInfo).getImage();
  }

  private class CachedImage {
    private final Image image;
    private final Date fetchedTime;

    CachedImage(Image image) {
      this.image = image;
      this.fetchedTime = new Date();
    }

    public Image getImage() {
      return image;
    }

    public boolean isValid() {
      return (System.currentTimeMillis() - fetchedTime.getTime()) / 1000 < cacheTimeSeconds;
    }
  }
}
