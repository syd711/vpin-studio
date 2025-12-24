package de.mephisto.vpin.ui;

import de.mephisto.vpin.commons.MonitorInfoUtil;
import de.mephisto.vpin.commons.fx.ConfirmationResult;
import de.mephisto.vpin.commons.fx.ServerFX;
import de.mephisto.vpin.commons.fx.apng.ApngImageLoaderFactory;
import de.mephisto.vpin.commons.utils.*;
import de.mephisto.vpin.commons.utils.localsettings.LocalUISettings;
import de.mephisto.vpin.connectors.mania.VPinManiaClient;
import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.client.VPinStudioClient;
import de.mephisto.vpin.restclient.client.VPinStudioClientErrorHandler;
import de.mephisto.vpin.restclient.frontend.Frontend;
import de.mephisto.vpin.restclient.games.GameRepresentation;
import de.mephisto.vpin.restclient.mania.ManiaConfig;
import de.mephisto.vpin.restclient.preferences.ServerSettings;
import de.mephisto.vpin.restclient.preferences.UISettings;
import de.mephisto.vpin.restclient.system.FeaturesInfo;
import de.mephisto.vpin.restclient.system.SystemSummary;
import de.mephisto.vpin.restclient.textedit.MonitoredTextFile;
import de.mephisto.vpin.restclient.textedit.VPinFile;
import de.mephisto.vpin.restclient.util.OSUtil;
import de.mephisto.vpin.ui.events.EventManager;
import de.mephisto.vpin.ui.jobs.JobPoller;
import de.mephisto.vpin.ui.launcher.LauncherController;
import de.mephisto.vpin.ui.tables.ClearCacheProgressModel;
import de.mephisto.vpin.ui.tables.TableReloadProgressModel;
import de.mephisto.vpin.ui.tables.vbsedit.VBSManager;
import de.mephisto.vpin.ui.util.FileMonitoringService;
import de.mephisto.vpin.ui.util.ProgressDialog;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import javafx.application.Application;
import javafx.application.HostServices;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ButtonType;
import javafx.scene.image.Image;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Rectangle;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;
import net.sf.sevenzipjbinding.SevenZip;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StreamUtils;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class Studio extends Application {
  private final static Logger LOG = LoggerFactory.getLogger(Studio.class);
  private static final String MACOS_APP_NAME = "vpin-studio.app";

  /**
   * The static features activated, static for a simple access in code
   */
  public static FeaturesInfo Features;

  public static Stage stage;

  public static VPinStudioClient client;
  public static VPinManiaClient maniaClient;

  public static HostServices hostServices;

  private ServerSocket ss;

  public static void main(String[] args) {
    launch(args);
  }

  private static VPinStudioClientErrorHandler errorHandler;

  @Override
  public void start(Stage stage) throws IOException {
    try (InputStream banner = getClass().getResourceAsStream("/banner.txt")) {
      String txt = StreamUtils.copyToString(banner, StandardCharsets.UTF_8);
      LOG.info("\n" + txt);
    }

    runOperatingSystemChecks();
    runExtensionsInstallation();

    LOG.info("Studio Starting...");
    LOG.info("Locale: " + Locale.getDefault().getDisplayName());
    LOG.info("TimeZone: " + TimeZone.getDefault().getDisplayName());
    LOG.info("OS: " + System.getProperty("os.name"));
    try {
      ss = new ServerSocket(1044);
    }
    catch (IOException e) {
      LOG.error("Application already running!");
      WidgetFactory.showAlert(stage, "Another VPin Studio client is already running!");
      System.exit(-1);
    }

    Studio.stage = stage;
    Studio.hostServices = getHostServices();

    StudioUpdatePreProcessing.execute();

    Studio.errorHandler = e -> {
      client.setErrorHandler(null);
      Platform.runLater(() -> {
        Studio.stage.close();
        NavigationController.refreshControllerCache();
        NavigationController.refreshViewCache();

        Studio.loadLauncher(new Stage());
        WidgetFactory.showAlert(stage, "Server Connection Failed", "You have been disconnected from the server.");
      });
    };

    //replace the OverlayFX client with the Studio one
    Studio.client = new VPinStudioClient("localhost");
    Studio.Features = client.getSystemService().getFeatures();
    ServerFX.client = Studio.client;

    String version = client.getSystemService().getVersion();
    if (!StringUtils.isEmpty(version)) {
      loadStudio(stage, Studio.client);
    }
    else {
      ConnectionProperties connectionProperties = new ConnectionProperties();
      List<ConnectionEntry> connections = connectionProperties.getConnections();
      if (!connections.isEmpty()) {
        for (ConnectionEntry connection : connections) {
          Studio.client = new VPinStudioClient(connection.getIp());
          Studio.Features = client.getSystemService().getFeatures();
          version = client.getSystemService().getVersion();
          if (!StringUtils.isEmpty(version)) {
            loadStudio(stage, Studio.client);
            return;
          }
        }
      }
      loadLauncher(stage);
    }
  }

  private void runOperatingSystemChecks() {
    if (OSUtil.isMac()) {
      //Get location where JAR was launched from
      System.setProperty("MAC_JAR_PATH", this.getClass().getProtectionDomain().getCodeSource().getLocation().toString());
      int endOfPath = System.getProperty("MAC_JAR_PATH").toLowerCase().indexOf(MACOS_APP_NAME) + MACOS_APP_NAME.length();
      String APP_Substring = System.getProperty("MAC_JAR_PATH").substring(5, endOfPath);

      //Get Application Paths
      System.setProperty("MAC_APP_PATH", APP_Substring);

      //Update to where jar is actually from and for updating
      System.setProperty("MAC_JAR_PATH", System.getProperty("MAC_APP_PATH") + "/Contents/app");

      //Set path for writing stuff
      System.setProperty("MAC_WRITE_PATH", System.getProperty("user.home") + "/Library/Application Support/VPin-Studio/");
    }
  }

  private void runExtensionsInstallation() {
    // install our APNGImageLoader
    ApngImageLoaderFactory.install();
  }

  public static HostServices getStudioHostServices() {
    return hostServices;
  }

  public static void loadLauncher(Stage stage) {
    LOG.info("load launcher...");
    try {
      Studio.stage = stage;
      Rectangle2D screenBounds = Screen.getPrimary().getBounds();
      FXMLLoader loader = new FXMLLoader(LauncherController.class.getResource("scene-launcher.fxml"));
      Parent root = loader.load();


      Scene scene = new Scene(root);
      scene.setFill(Paint.valueOf("#212529"));
      stage.setTitle("VPin Studio Launcher");
      stage.getIcons().add(new Image(Studio.class.getResourceAsStream("logo-64.png")));
      stage.setScene(scene);
      stage.initStyle(StageStyle.UNDECORATED);
      stage.setX((screenBounds.getWidth() / 2) - (800 / 2));
      stage.setY((screenBounds.getHeight() / 2) - (400 / 2));

      LauncherController controller = loader.getController();
      controller.setStage(stage);
      stage.show();
    }
    catch (IOException e) {
      LOG.error("Failed to load launcher: " + e.getMessage(), e);
    }
  }

  public static void loadStudio(Stage stage, VPinStudioClient client) {
    LOG.info("Launching Studio...");
    try {
      try {
        File sevenZipTempFolder = new File(System.getProperty("java.io.tmpdir"), "sevenZip/");
        if (!sevenZipTempFolder.exists()) {
          sevenZipTempFolder.mkdirs();
          SevenZip.initSevenZipFromPlatformJAR(sevenZipTempFolder);
          LOG.info("Installed sevenZip");
        }
      }
      catch (Exception e) {
        LOG.error("Failed to initialize SevenZip (.rar support): " + e.getMessage(), e);
      }

      Stage splash = createSplash();

      //replace the OverlayFX client with the Studio one
      Studio.client = client;
      Studio.Features = client.getSystemService().getFeatures();
      ServerFX.client = Studio.client;

//      Platform.setImplicitExit(false);
      stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
        @Override
        public void handle(WindowEvent event) {
          if (!exit()) {
            event.consume();
          }
        }
      });

      // run later to let the splash render properly
      JFXFuture.runAsync(() -> {
            MonitorInfoUtil.logScreenSummary();

            //force pre-caching, this way, the table overview does not need to execute single GET requests
            new Thread(() -> {
              Studio.client.getVpsService().invalidateAll();
              LOG.info("Pre-cached VPS tables");
            }, "Pre-cached VPS tables Thread").start();

            createManiaClient();

            // reinitialize a new EventManager each time application starts
            EventManager.initialize();
          })
          .thenLater(() -> {
            Studio.stage = stage;

            List<Integer> unknownGameIds = client.getGameService().getUnknownGameIds();
            if (unknownGameIds != null && !unknownGameIds.isEmpty()) {
              LOG.info("Initial scan of " + unknownGameIds.size() + " unknown tables.");
              ProgressDialog.createProgressDialog(new TableReloadProgressModel(unknownGameIds));
              ProgressDialog.createProgressDialog(ClearCacheProgressModel.getReloadGamesClearCacheModel(false));
            }

            UISettings uiSettings = client.getPreferenceService().getJsonPreference(PreferenceNames.UI_SETTINGS, UISettings.class);
            client.getGameService().setIgnoredEmulatorIds(uiSettings.getIgnoredEmulatorIds());

            Rectangle2D screenBounds = Screen.getPrimary().getBounds();

            if (screenBounds.getWidth() > screenBounds.getHeight()) {
              LOG.info("Window Mode: Landscape");
            }
            else {
              LOG.info("Window Mode: Portrait");
            }

            FXMLLoader loader = new FXMLLoader(Studio.class.getResource("scene-root.fxml"));
            Parent root = null;
            try {
              root = loader.load();
            }
            catch (IOException e) {
              LOG.error("Failed to load Studio: {}", e.getMessage(), e);
            }

            Rectangle position = LocalUISettings.getPosition();

            Rectangle2D bounds = Screen.getPrimary().getVisualBounds();
            double width = bounds.getWidth() - (bounds.getWidth() * 10 / 100);
            double height = bounds.getHeight() - (bounds.getHeight() * 10 / 100);
            if (position.getWidth() > 800 && position.getHeight() > 600) {
              width = position.getWidth();
              height = position.getHeight();
            }
            Scene scene = new Scene(root, width, height, Paint.valueOf("#212529"));
            stage.getIcons().add(new Image(Studio.class.getResourceAsStream("logo-128.png")));
            stage.setScene(scene);
            stage.setMinWidth(1280);
            stage.setMinHeight(700);
            stage.setResizable(true);
            stage.initStyle(StageStyle.UNDECORATED);

            if (position.getX() != -1) {
              stage.setX(position.getX());
              stage.setY(position.getY());
            }
            else {
              stage.setX((screenBounds.getWidth() / 2) - (width / 2));
              stage.setY((screenBounds.getHeight() / 2) - (height / 2));
            }

            // ResizeHelper.addResizeListener(stage);
            FXResizeHelper.install(stage, 30, 6);

            // OLE use event bubbling, from most specific node up to windows
            //scene.addEventFilter(KeyEvent.KEY_PRESSED, new StudioKeyEventHandler(stage));
            scene.addEventHandler(KeyEvent.KEY_PRESSED, new StudioKeyEventHandler(stage));

            client.setErrorHandler(errorHandler);
            stage.show();
            splash.hide();

            //launch VPSMonitor
            VBSManager.getInstance();
          });
    }
    catch (Exception e) {
      LOG.error("Failed to load Studio: " + e.getMessage(), e);
    }
  }

  private static Stage createSplash() throws Exception {
    FXMLLoader loader = new FXMLLoader(SplashScreenController.class.getResource("scene-splash.fxml"));
    StackPane root = loader.load();
    SplashScreenController controller = loader.getController();

    double imgWidth = 800, imgHeight = 534;
    try (InputStream imgStream = Studio.class.getResourceAsStream("splash4.0.png")) {
      Image image = new Image(imgStream);
      controller.setImage(image);
      imgWidth = image.getWidth();
      imgHeight = image.getHeight();
    }

    Scene scene = new Scene(root, imgWidth, imgHeight, Color.TRANSPARENT);
    Rectangle2D screenBounds = Screen.getPrimary().getBounds();

    Stage stage = new Stage(StageStyle.UNDECORATED);
    stage.getIcons().add(new Image(Studio.class.getResourceAsStream("logo-64.png")));
    stage.setScene(scene);
    stage.setX((screenBounds.getWidth() / 2) - (imgWidth / 2));
    stage.setY((screenBounds.getHeight() / 2) - (imgHeight / 2));
    stage.setResizable(false);
    stage.show();
    return stage;
  }

  private static void createManiaClient() {
    try {
      if (Features.MANIA_ENABLED) {
        ManiaConfig config = Studio.client.getManiaService().getConfig();
        SystemSummary summary = Studio.client.getSystemService().getSystemSummary();
        Studio.maniaClient = new VPinManiaClient(config.getUrl(), summary.getSystemId());
        ServerFX.maniaClient = Studio.maniaClient;
      }
    }
    catch (Exception e) {
      LOG.error("Failed to create mania client: " + e.getMessage());
      Features.MANIA_ENABLED = false;
    }
  }

  public static String getVersion() {
    try {
      final Properties properties = new Properties();
      InputStream resourceAsStream = Studio.class.getClassLoader().getResourceAsStream("version.properties");
      properties.load(resourceAsStream);
      resourceAsStream.close();
      return properties.getProperty("vpin.studio.version");
    }
    catch (IOException e) {
      LOG.error("Failed to read version number: " + e.getMessage(), e);
    }
    return null;
  }

  public static void browse(@Nullable String url) {
    if (!StringUtils.isEmpty(url)) {
      String osName = System.getProperty("os.name");
      if (osName.contains("Windows")) {
        Studio.hostServices.showDocument(url);
      }
      else if (osName.toLowerCase().contains("mac")) {
        Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
        if (desktop != null && desktop.isSupported(Desktop.Action.BROWSE)) {
          try {
            desktop.browse(new URI(url));
          }
          catch (Exception e) {
            LOG.error("Failed to open file: " + e.getMessage(), e);
          }
        }
      }
      else if (osName.toLowerCase().contains("nux")) {
        try {
          Runtime.getRuntime().exec(new String[]{"xdg-open", url});
        }
        catch (IOException e) {
          LOG.error("Error opening browser: " + e.getMessage(), e);
          WidgetFactory.showAlert(Studio.stage, "Error", "Error opening browser: " + e.getMessage());
        }
      }
      else {
        WidgetFactory.showAlert(Studio.stage, "Error", "Failed to determine operating system for name \"" + osName + "\".");
      }
    }
  }

  public static boolean open(@Nullable File file) {
    if (file != null && file.exists()) {
      Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
      if (desktop != null && desktop.isSupported(Desktop.Action.OPEN)) {
        try {
          desktop.open(file);
          return true;
        }
        catch (Exception e) {
          LOG.error("Failed to open file: " + e.getMessage(), e);
        }
      }
    }
    return false;
  }


  public static boolean editGameFile(@NonNull GameRepresentation game, @NonNull String filePath) throws Exception {
    FileMonitoringService.getInstance().setPaused(true);

    MonitoredTextFile monitoredTextFile = new MonitoredTextFile(VPinFile.LOCAL_GAME_FILE);
    monitoredTextFile.setFileId(String.valueOf(game.getId()));
    monitoredTextFile.setPath(filePath);
    MonitoredTextFile loadedMonitoredFile = client.getTextEditorService().getText(monitoredTextFile);
    FileMonitoringService.getInstance().monitor(monitoredTextFile);

    String fileName = FilenameUtils.getBaseName(game.getGameFileName());

    String content = loadedMonitoredFile.getContent();
    File tempFile = new File(FileMonitoringService.getInstance().getMonitoringFolder(), fileName + "[" + game.getId() + "].txt");
    if (tempFile.exists() && !tempFile.delete()) {
      LOG.error("Failed to delete {}", tempFile.getAbsolutePath());
    }

    Files.write(tempFile.toPath(), content.getBytes());
    FileMonitoringService.getInstance().setPaused(false);
    return edit(tempFile);
  }

  public static boolean edit(@Nullable File file) {
    if (file != null && file.exists()) {
      String osName = System.getProperty("os.name");
      if (osName.contains("Windows")) {
        Studio.hostServices.showDocument(file.getAbsolutePath());
      }
      else if (osName.toLowerCase().contains("mac")) {
        try {
          Runtime.getRuntime().exec(new String[]{"/usr/bin/open", "-t", file.getAbsolutePath()});
        }
        catch (IOException e) {
          LOG.error("Error opening browser: " + e.getMessage(), e);
          WidgetFactory.showAlert(Studio.stage, "Error", "Error opening browser: " + e.getMessage());
        }
      }
      else if (osName.toLowerCase().contains("nux")) {
        try {
          Runtime.getRuntime().exec(new String[]{"xdg-open", file.getAbsolutePath()});
        }
        catch (IOException e) {
          LOG.error("Error opening browser: " + e.getMessage(), e);
          WidgetFactory.showAlert(Studio.stage, "Error", "Error opening browser: " + e.getMessage());
        }
      }
      else {
        WidgetFactory.showAlert(Studio.stage, "Error", "Failed to determine operating system for name \"" + osName + "\".");
      }
    }
    return false;
  }

  public static boolean exit() {
    try {
      UISettings uiSettings = client.getPreferenceService().getJsonPreference(PreferenceNames.UI_SETTINGS, UISettings.class);
      ServerSettings serverSettings = client.getPreferenceService().getJsonPreference(PreferenceNames.SERVER_SETTINGS, ServerSettings.class);
      boolean launchFrontendOnExit = serverSettings.isLaunchPopperOnExit();

      if (!launchFrontendOnExit && !uiSettings.isHideFrontendLaunchQuestion()) {
        Frontend frontend = Studio.client.getFrontendService().getFrontendCached();
        ConfirmationResult confirmationResult = WidgetFactory.showConfirmationWithCheckbox(stage, "Exit and Launch " + frontend.getName(), "Exit and Launch " + frontend.getName(), "Exit", "Select the checkbox below if you do not wish to see this question anymore.", null, "Do not show again", false);
        if (confirmationResult.isCancelClicked()) {
          return false;
        }

        if (confirmationResult.isOkClicked()) {
          new Thread(() -> {
            client.getFrontendService().restartFrontend();
          }).start();
        }

        if (confirmationResult.isChecked()) {
          uiSettings.setHideFrontendLaunchQuestion(true);
          client.getPreferenceService().setJsonPreference(uiSettings);
        }
      }
    }
    catch (Exception e) {
      LOG.error("Shutdown failed, continue to exit... {}", e.getMessage());
    }

    AtomicBoolean polling = new AtomicBoolean(false);
    try {
      final ExecutorService executor = Executors.newFixedThreadPool(1);
      final Future<?> future = executor.submit(() -> {
        client.getSystemService().setMaintenanceMode(false);
        polling.set(JobPoller.getInstance().isPolling());
      });
      future.get(2000, TimeUnit.MILLISECONDS);
      executor.shutdownNow();
    }
    catch (Exception e) {
      //ignore
    }


    if (polling.get()) {
      Optional<ButtonType> result = WidgetFactory.showConfirmation(stage, "Jobs Running", "There are still jobs running.", "These jobs will continue after quitting.", "Got it, exit VPin Studio");
      if (result.isPresent() && result.get().equals(ButtonType.OK)) {
        System.exit(0);
      }
    }
    else {
      System.exit(0);
    }
    return true;
  }
}