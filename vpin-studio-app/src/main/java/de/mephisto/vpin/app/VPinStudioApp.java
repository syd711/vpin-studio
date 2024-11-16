package de.mephisto.vpin.app;

import de.mephisto.vpin.commons.fx.OverlayController;
import de.mephisto.vpin.commons.fx.ServerFX;
import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.commons.utils.controller.GameController;
import de.mephisto.vpin.commons.utils.controller.GameControllerInputListener;
import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.client.VPinStudioClient;
import de.mephisto.vpin.restclient.preferences.OverlaySettings;
import de.mephisto.vpin.restclient.representations.PreferenceEntryRepresentation;
import de.mephisto.vpin.restclient.util.SystemUtil;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Cursor;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.SceneAntialiasing;
import javafx.scene.layout.BorderPane;
import javafx.stage.Screen;
import javafx.stage.Stage;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Locale;

public class VPinStudioApp extends Application implements GameControllerInputListener {
  private final static Logger LOG = LoggerFactory.getLogger(VPinStudioApp.class);

  public static VPinStudioClient client;

  public static void main(String[] args) {
    launch(args);
  }

  @Override
  public void start(Stage overlayStage) {
    Locale.setDefault(Locale.ENGLISH);
    VPinStudioApp.client = new VPinStudioClient("localhost");
    ServerFX.client = VPinStudioApp.client;


    try {
      Platform.setImplicitExit(false);
      OverlaySettings overlaySettings = client.getJsonPreference(PreferenceNames.OVERLAY_SETTINGS, OverlaySettings.class);
      Screen screen = SystemUtil.getScreenById(overlaySettings.getOverlayScreenId());

      BorderPane root = new BorderPane();
      final Scene scene = new Scene(root, screen.getVisualBounds().getWidth(), screen.getVisualBounds().getHeight(), true, SceneAntialiasing.BALANCED);
      scene.setCursor(Cursor.NONE);

      Rectangle2D bounds = screen.getVisualBounds();
      overlayStage.setX(bounds.getMinX());
      overlayStage.setY(bounds.getMinY());

      overlayStage.setScene(scene);
      overlayStage.setFullScreenExitHint("");
      overlayStage.setAlwaysOnTop(true);
      overlayStage.setFullScreen(true);
      overlayStage.setTitle("VPin Studio Overlay");
      overlayStage.getScene().getStylesheets().add(ServerFX.class.getResource("stylesheet.css").toExternalForm());

      if (overlaySettings.getDesignType() == null) {
        WidgetFactory.showAlert(overlayStage, "Error", "No overlay design selected.", "Select an overlay design from the preferences.");
        System.exit(0);
      }

      String designType = overlaySettings.getDesignType();
      if (StringUtils.isEmpty(designType) || designType.equalsIgnoreCase("null")) {
        designType = "";
      }

      String resource = ServerFX.resolveDashboard(designType);
      FXMLLoader overlayLoader = new FXMLLoader(OverlayController.class.getResource(resource));
      Parent widgetRoot = overlayLoader.load();
      OverlayController controller = overlayLoader.getController();
      root.setCenter(widgetRoot);

      GameController.getInstance().addListener(this);

      controller.refreshData();
      overlayStage.show();
    }
    catch (Exception e) {
      LOG.error("Failed to launch VPin Studio App: " + e.getMessage(), e);
      WidgetFactory.showAlert(overlayStage, "Error", "Failed to launch VPin Studio App: " + e.getMessage());
      System.exit(-1);
    }

  }

  @Override
  public void controllerEvent(String name) {
    System.exit(0);
  }
}