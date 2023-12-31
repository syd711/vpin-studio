package de.mephisto.vpin.ui;

import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.ui.events.EventManager;
import de.mephisto.vpin.ui.events.StudioEventListener;
import de.mephisto.vpin.ui.preferences.ScreensPreferencesController;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

public class PreferencesController implements Initializable, StudioEventListener {
  private final static Logger LOG = LoggerFactory.getLogger(PreferencesController.class);

  // Add a public no-args constructor
  public PreferencesController() {
  }

  @FXML
  private Label versionLabel;

  @FXML
  private Label hostLabel;

  @FXML
  private Button avatarBtn;

  @FXML
  private Button backglassBtn;

  @FXML
  private Button mameBtn;

  @FXML
  private BorderPane preferencesMain;

  @FXML
  private VBox navigationBox;

  private static Button lastSelection;

  private static Node preferencesRoot;

  private static Button avatarButton;

  private static BorderPane prefsMain;

  private static VBox navBox;

  private static boolean dirty = false;

  private static String lastScreen = "preference-settings.fxml";

  static {

  }

  public static void markDirty() {
    dirty = true;
  }

  public static void open() {
    if (preferencesRoot == null) {
      try {
        FXMLLoader loader = new FXMLLoader(NavigationController.class.getResource("scene-preferences.fxml"));
        PreferencesController controller = loader.getController();
        preferencesRoot = loader.load();
        preferencesRoot.setUserData(controller);
      } catch (IOException e) {
        LOG.error("Failed to load preferences: " + e.getMessage(), e);
      }

      Node lookup = Studio.stage.getScene().lookup("#root");
      BorderPane main = (BorderPane) lookup;
      StackPane stack = (StackPane) main.getCenter();
      stack.getChildren().add(preferencesRoot);
    }
    else {
      Node lookup = Studio.stage.getScene().lookup("#root");
      BorderPane main = (BorderPane) lookup;
      StackPane stack = (StackPane) main.getCenter();
      stack.getChildren().add(preferencesRoot);

      loadScreen(lastScreen);
    }
  }

  @FXML
  private void onClose(ActionEvent event) {
    Node lookup = Studio.stage.getScene().lookup("#root");
    BorderPane main = (BorderPane) lookup;
    StackPane stack = (StackPane) main.getCenter();
    stack.getChildren().remove(1);

    NavigationController.refreshControllerCache();

    if (dirty) {
      dirty = false;
      EventManager.getInstance().notifyPreferenceChanged();
    }
  }

  @FXML
  private void onAvatar(ActionEvent event) throws IOException {
    load("preference-settings.fxml", event);
  }

  @FXML
  private void onAccount(ActionEvent event) throws IOException {
    load("preference-account.fxml", event);
  }

  @FXML
  private void onMediaValidation(ActionEvent event) throws IOException {
    load("preference-validators_pinuppopper.fxml", event);
  }

  @FXML
  private void onVPXValidation(ActionEvent event) throws IOException {
    load("preference-validators_vpx.fxml", event);
  }

  @FXML
  private void onVpaRepositories(ActionEvent event) throws IOException {
    load("preference-repositories.fxml", event);
  }

  @FXML
  private void onArchiveManager(ActionEvent event) throws IOException {
    load("preference-table_manager.fxml", event);
  }

  @FXML
  private void onScreens(ActionEvent event) throws IOException {
    load("preference-screens.fxml", event);
  }

  @FXML
  private void onMame(ActionEvent event) throws IOException {
    load("preference-mame.fxml", event);
  }

  @FXML
  private void onPinVol(ActionEvent event) throws IOException {
    load("preference-pinvol.fxml", event);
  }

  @FXML
  private void onPINemHi(ActionEvent event) throws IOException {
    load("preference-pinemhi.fxml", event);
  }

  @FXML
  private void onBackglassServer(ActionEvent event) throws IOException {
    load("preference-backglass.fxml", event);
  }

  @FXML
  private void onReset(ActionEvent event) throws IOException {
    load("preference-reset.fxml", event);
  }

  @FXML
  private void onCustomOptions(ActionEvent event) throws IOException {
    load("preference-popper_custom_options.fxml", event);
  }

  @FXML
  private void onVPBM(ActionEvent event) throws IOException {
    load("preference-vpbm.fxml", event);
  }

  @FXML
  private void onOverlay(ActionEvent event) throws IOException {
    load("preference-overlay.fxml", event);
  }

  @FXML
  private void onHighscoreCards(ActionEvent event) throws IOException {
    load("preference-highscore_cards.fxml", event);
  }

  @FXML
  private void onScoreFormat(ActionEvent event) throws IOException {
    load("preference-highscores.fxml", event);
  }

  @FXML
  private void onRankings(ActionEvent event) throws IOException {
    load("preference-player_rankings.fxml", event);
  }

  @FXML
  private void onServiceInfo(ActionEvent event) throws IOException {
    load("preference-service_info.fxml", event);
  }

  @FXML
  private void onDiscordBot(ActionEvent event) throws IOException {
    load("preference-discord_bot.fxml", event);
  }

  @FXML
  private void onDiscordBotFaq(ActionEvent event) throws IOException {
    load("preference-discord_faq.fxml", event);
  }

  public static void open(String preferenceType) {
    open();
    Platform.runLater(() -> {
      load("preference-" + preferenceType + ".fxml", null, preferenceType);
    });
  }

  @FXML
  private void onDiscordLink() {
    Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
    if (desktop != null && desktop.isSupported(Desktop.Action.BROWSE)) {
      try {
        desktop.browse(new URI("https://discord.gg/69YqHYd3wD"));
      } catch (Exception e) {
        LOG.error("Failed to open discord link: " + e.getMessage(), e);
      }
    }
  }

  private void load(String screen, ActionEvent event) throws IOException {
    load(screen, event, null);
  }

  private static void load(String screen, ActionEvent event, String btnId) {
    if (lastSelection != null) {
      lastSelection.getStyleClass().remove("preference-button-selected");
    }
    else {
      avatarButton.getStyleClass().remove("preference-button-selected");
    }

    if (event != null) {
      lastSelection = (Button) event.getSource();
      lastSelection.getStyleClass().add("preference-button-selected");
    }
    else if (btnId != null) {
      Optional<Node> first = navBox.getChildren().stream().filter(b -> btnId.equals(b.getId())).findFirst();
      if (first.isPresent()) {
        lastSelection = (Button) first.get();
        lastSelection.getStyleClass().add("preference-button-selected");
      }
    }

    loadScreen(screen);
  }

  public static void loadScreen(String screen) {
    lastScreen = screen;

    try {
      FXMLLoader loader = new FXMLLoader(ScreensPreferencesController.class.getResource(screen));
      Node node = loader.load();
      prefsMain.setCenter(node);
    } catch (Exception e) {
      LOG.error("Failed to loading settings view: " + e.getMessage(), e);
      WidgetFactory.showAlert(Studio.stage, "Error", e.getMessage());
    }
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    avatarButton = avatarBtn;
    prefsMain = preferencesMain;
    navBox = navigationBox;

    avatarBtn.getStyleClass().add("preference-button-selected");
    versionLabel.setText("VPin Studio Version " + Studio.getVersion());
    hostLabel.setText(System.getProperty("os.name"));

    EventManager.getInstance().addListener(this);
  }
}