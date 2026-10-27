package de.mephisto.vpin.server.discord;

import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.discord.DiscordClientSettings;
import de.mephisto.vpin.restclient.discord.DiscordClientStatus;
import de.mephisto.vpin.restclient.util.OSUtil;
import de.mephisto.vpin.restclient.util.SystemCommandExecutor;
import de.mephisto.vpin.server.frontend.FrontendStatusChangeListener;
import de.mephisto.vpin.server.frontend.FrontendStatusService;
import de.mephisto.vpin.server.frontend.TableStatusChangeListener;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.games.TableStatusChangedEvent;
import de.mephisto.vpin.server.preferences.PreferenceChangedListener;
import de.mephisto.vpin.server.preferences.PreferencesService;
import jakarta.annotation.PreDestroy;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Manages the locally installed Discord client: detects the installation, launches it minimized
 * and publishes the active table as Discord rich presence.
 */
@Service
public class DiscordClientService implements InitializingBean, PreferenceChangedListener, TableStatusChangeListener, FrontendStatusChangeListener {
  private final static Logger LOG = LoggerFactory.getLogger(DiscordClientService.class);

  /**
   * The id of the Discord application the rich presence is posted for, its name is shown as activity title.
   */
  private final static String DISCORD_APPLICATION_ID = "1561577868813668442";
  private final static String MANIA_WHEEL_URL = "https://vpin-mania.net/wheels/";

  /**
   * The Discord installation folder names and their executables, stable first.
   */
  private final static Map<String, String> DISCORD_VARIANTS = new LinkedHashMap<>();

  static {
    DISCORD_VARIANTS.put("Discord", "Discord.exe");
    DISCORD_VARIANTS.put("DiscordPTB", "DiscordPTB.exe");
    DISCORD_VARIANTS.put("DiscordCanary", "DiscordCanary.exe");
  }

  @Autowired
  private PreferencesService preferencesService;

  @Autowired
  private FrontendStatusService frontendStatusService;

  private final ExecutorService executor = Executors.newSingleThreadExecutor(r -> {
    Thread t = new Thread(r, "Discord Client Rich Presence");
    t.setDaemon(true);
    return t;
  });

  private DiscordClientSettings settings;

  /**
   * Only accessed by the executor thread.
   */
  private DiscordIpcConnection connection;

  public DiscordClientStatus getStatus() {
    DiscordClientStatus status = new DiscordClientStatus();
    File installationFolder = getInstallationFolder();
    if (installationFolder != null) {
      status.setInstalled(true);
      status.setInstallationFolder(installationFolder.getAbsolutePath());
      status.setRunning(isRunning());
    }
    return status;
  }

  /**
   * Returns the folder of the Discord installation, null if Discord is not installed.
   */
  @Nullable
  public File getInstallationFolder() {
    if (!OSUtil.isWindows()) {
      return null;
    }

    File localAppData = getLocalAppDataFolder();
    for (Map.Entry<String, String> variant : DISCORD_VARIANTS.entrySet()) {
      File folder = new File(localAppData, variant.getKey());
      if (new File(folder, "Update.exe").exists() && getExecutable(folder, variant.getValue()) != null) {
        return folder;
      }
    }
    return null;
  }

  public boolean isInstalled() {
    return getInstallationFolder() != null;
  }

  public boolean isRunning() {
    return ProcessHandle.allProcesses()
        .map(p -> p.info().command().orElse(""))
        .anyMatch(command -> DISCORD_VARIANTS.values().stream().anyMatch(exe -> StringUtils.endsWithIgnoreCase(command, File.separator + exe)));
  }

  /**
   * Launches Discord minimized to the system tray, the same way the Discord autostart does.
   */
  public boolean launchMinimized() {
    File installationFolder = getInstallationFolder();
    if (installationFolder == null) {
      LOG.info("Discord launch skipped, no Discord installation found.");
      return false;
    }
    if (isRunning()) {
      LOG.info("Discord launch skipped, Discord is already running.");
      return true;
    }

    try {
      String exe = DISCORD_VARIANTS.get(installationFolder.getName());
      File updateExe = new File(installationFolder, "Update.exe");
      SystemCommandExecutor executor = new SystemCommandExecutor(List.of(updateExe.getAbsolutePath(), "--processStart", exe, "--process-start-args", "--start-minimized"), false);
      executor.setDir(installationFolder);
      executor.executeCommandAsync();
      LOG.info("Launched Discord minimized from \"{}\"", installationFolder.getAbsolutePath());
      return true;
    }
    catch (Exception e) {
      LOG.error("Failed to launch Discord: {}", e.getMessage(), e);
    }
    return false;
  }

  /**
   * Publishes the given game as Discord rich presence, null clears the presence.
   */
  public void updateRichPresence(@Nullable Game game) {
    if (!OSUtil.isWindows()) {
      return;
    }

    String name = game != null ? game.getGameDisplayName() : null;
    String extTableId = game != null ? game.getExtTableId() : null;
    executor.submit(() -> {
      if (name == null) {
        clearActivity();
        return;
      }

      try {
        Map<String, Object> activity = new LinkedHashMap<>();
        activity.put("type", 0); // "Playing"
        activity.put("details", name);
        activity.put("status_display_type", 2); // show the details instead of the application name in the member list
        activity.put("timestamps", Map.of("start", System.currentTimeMillis()));
        String wheelUrl = getWheelUrl(extTableId);
        if (wheelUrl != null) {
          activity.put("assets", Map.of("large_image", wheelUrl, "large_text", name));
        }

        if (connection == null) {
          connection = DiscordIpcConnection.connect(DISCORD_APPLICATION_ID);
        }
        connection.setActivity(activity);
        LOG.info("Updated Discord rich presence to \"{}\"", name);
      }
      catch (IOException e) {
        LOG.info("Discord rich presence update failed: {}", e.getMessage());
        disconnect();
      }
    });
  }

  /**
   * Must be called on the executor thread.
   */
  private void clearActivity() {
    if (connection != null) {
      try {
        connection.setActivity(null);
        LOG.info("Cleared Discord rich presence");
      }
      catch (IOException e) {
        LOG.info("Failed to clear Discord rich presence: {}", e.getMessage());
      }
      // Discord removes the activity anyway once the connection is closed
      disconnect();
    }
  }

  private void disconnect() {
    if (connection != null) {
      connection.close();
      connection = null;
    }
  }

  /**
   * Returns the VPin Mania wheel icon url for the given VPS table id, null if there is none.
   */
  @Nullable
  private String getWheelUrl(@Nullable String extTableId) {
    if (StringUtils.isEmpty(extTableId)) {
      return null;
    }

    String url = MANIA_WHEEL_URL + extTableId + ".png";
    try {
      HttpURLConnection http = (HttpURLConnection) URI.create(url).toURL().openConnection();
      http.setRequestMethod("HEAD");
      http.setConnectTimeout(3000);
      http.setReadTimeout(3000);
      int responseCode = http.getResponseCode();
      http.disconnect();
      if (responseCode == HttpURLConnection.HTTP_OK) {
        return url;
      }
    }
    catch (Exception e) {
      LOG.info("Failed to check VPin Mania wheel icon \"{}\": {}", url, e.getMessage());
    }
    return null;
  }

  @Nullable
  private File getExecutable(File installationFolder, String exe) {
    File[] appFolders = installationFolder.listFiles((dir, name) -> name.startsWith("app-") && new File(new File(dir, name), exe).exists());
    if (appFolders == null || appFolders.length == 0) {
      return null;
    }
    Arrays.sort(appFolders);
    return new File(appFolders[appFolders.length - 1], exe);
  }

  private File getLocalAppDataFolder() {
    String localAppData = System.getenv("LOCALAPPDATA");
    if (!StringUtils.isEmpty(localAppData)) {
      return new File(localAppData);
    }
    return new File(System.getProperty("user.home"), "AppData/Local");
  }

  //-------------------- TableStatusChangeListener ---------------------------------------------------------------------

  @Override
  public void tableLaunched(TableStatusChangedEvent event) {
    if (settings.isStatusUpdateEnabled()) {
      updateRichPresence(event.getGame());
    }
  }

  @Override
  public void tableExited(TableStatusChangedEvent event) {
    updateRichPresence(null);
  }

  //-------------------- FrontendStatusChangeListener ------------------------------------------------------------------

  @Override
  public void frontendLaunched() {
    //ignore
  }

  @Override
  public void frontendExited() {
    // no table exit event is fired when the frontend is killed
    updateRichPresence(null);
  }

  @Override
  public void frontendRestarted() {
    updateRichPresence(null);
  }

  //-------------------- PreferenceChangedListener ---------------------------------------------------------------------

  @Override
  public void preferenceChanged(String propertyName, Object oldValue, Object newValue) {
    if (PreferenceNames.DISCORD_CLIENT_SETTINGS.equals(propertyName)) {
      settings = preferencesService.getJsonPreference(PreferenceNames.DISCORD_CLIENT_SETTINGS, DiscordClientSettings.class);
      if (!settings.isStatusUpdateEnabled()) {
        updateRichPresence(null);
      }
    }
  }

  @PreDestroy
  public void shutdown() {
    executor.submit(this::clearActivity);
    executor.shutdown();
  }

  @Override
  public void afterPropertiesSet() {
    try {
      preferencesService.addChangeListener(this);
      preferenceChanged(PreferenceNames.DISCORD_CLIENT_SETTINGS, null, null);
      frontendStatusService.addTableStatusChangeListener(this);
      frontendStatusService.addFrontendStatusChangeListener(this);

      if (settings.isLaunchOnStartup()) {
        if (!OSUtil.isWindows()) {
          LOG.info("The Discord client launch is only supported on Windows, launch skipped.");
        }
        else {
          launchMinimized();
        }
      }
    }
    catch (Exception e) {
      LOG.error("Discord client service initialization failed: {}", e.getMessage(), e);
    }
    LOG.info("{} initialization finished.", this.getClass().getSimpleName());
  }
}
