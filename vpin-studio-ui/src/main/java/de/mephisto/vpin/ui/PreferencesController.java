package de.mephisto.vpin.ui;

import de.mephisto.vpin.commons.fx.Features;
import de.mephisto.vpin.commons.fx.UIDefaults;
import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.ui.events.EventManager;
import de.mephisto.vpin.ui.events.StudioEventListener;
import de.mephisto.vpin.ui.preferences.PreferenceType;
import de.mephisto.vpin.ui.preferences.ClientSettingsPreferencesController;
import de.mephisto.vpin.ui.util.Dialogs;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
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

import static de.mephisto.vpin.ui.Studio.client;

public class PreferencesController implements Initializable, StudioEventListener {
  private final static Logger LOG = LoggerFactory.getLogger(PreferencesController.class);

  // Add a public no-args constructor
  public PreferencesController() {
  }

  @FXML
  private Hyperlink versionLink;

  @FXML
  private Hyperlink kofiLink;

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

  @FXML
  private VBox tournamentGroup;

  @FXML
  private Button notificationsButton;

  private static Button lastSelection;

  private static Node preferencesRoot;

  private static Button initialBtn;

  private static BorderPane prefsMain;

  private static VBox navBox;

  private static PreferenceType dirtyPreferenceType = null;

  private static String lastScreen = "preference-settings-cabinet.fxml";

  static {

  }

  public static void markDirty(PreferenceType preferenceType) {
    PreferencesController.dirtyPreferenceType = preferenceType;
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

    if (dirtyPreferenceType != null) {
      PreferenceType preferenceType = dirtyPreferenceType;
      dirtyPreferenceType = null;
      EventManager.getInstance().notifyPreferenceChanged(preferenceType);
    }
  }

  @FXML
  private void onVersionLInk(ActionEvent event) {
    Dialogs.openUpdateInfoDialog(client.getSystemService().getVersion(), true);
  }

  @FXML
  private void onCabinetSettings(ActionEvent event) throws IOException {
    load("preference-settings-cabinet.fxml", event);
  }

  @FXML
  private void onClientSettings(ActionEvent event) throws IOException {
    load("preference-settings-client.fxml", event);
  }

  @FXML
  private void onAccount(ActionEvent event) throws IOException {
    load("preference-tournaments.fxml", event);
  }

  @FXML
  private void onMediaValidation(ActionEvent event) throws IOException {
    load("preference-validators-screens.fxml", event);
  }

  @FXML
  private void onSupport(ActionEvent event) throws IOException {
    load("preference-support.fxml", event);
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
  private void onDOF(ActionEvent event) throws IOException {
    load("preference-dof.fxml", event);
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
  private void onNotifications(ActionEvent event) throws IOException {
    load("preference-notifications.fxml", event);
  }

  @FXML
  private void onRankings(ActionEvent event) throws IOException {
    load("preference-player_rankings.fxml", event);
  }

  @FXML
  private void onServiceInfo(ActionEvent event) throws IOException {
    load("preference-settings-server.fxml", event);
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
        desktop.browse(new URI(UIDefaults.DISCORD_INVITE_LINK));
      } catch (Exception e) {
        LOG.error("Failed to open discord link: " + e.getMessage(), e);
      }
    }
  }

  @FXML
  private void onKofiLink() {
    Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
    if (desktop != null && desktop.isSupported(Desktop.Action.BROWSE)) {
      try {
        desktop.browse(new URI("https://ko-fi.com/syd711"));
      } catch (Exception e) {
        LOG.error("Failed to open kofi link: " + e.getMessage(), e);
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
      initialBtn.getStyleClass().remove("preference-button-selected");
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
      FXMLLoader loader = new FXMLLoader(ClientSettingsPreferencesController.class.getResource(screen));
      Node node = loader.load();
      prefsMain.setCenter(node);
    } catch (Exception e) {
      LOG.error("Failed to loading settings view: " + e.getMessage(), e);
      WidgetFactory.showAlert(Studio.stage, "Error", e.getMessage());
    }
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    initialBtn = avatarBtn;
    prefsMain = preferencesMain;
    navBox = navigationBox;

    tournamentGroup.managedProperty().bindBidirectional(tournamentGroup.visibleProperty());
    tournamentGroup.setVisible(Features.TOURNAMENTS_ENABLED);

    notificationsButton.managedProperty().bindBidirectional(notificationsButton.visibleProperty());
    notificationsButton.setVisible(Features.NOTIFICATIONS_ENABLED);

    avatarBtn.getStyleClass().add("preference-button-selected");
    versionLink.setText("VPin Studio Version " + Studio.getVersion());
    versionLink.setStyle("-fx-font-size : 12px;-fx-font-color: #B0ABAB;");
    hostLabel.setText(System.getProperty("os.name"));

    Image image6 = new Image(Studio.class.getResourceAsStream("ko-fi.png"));
    ImageView view6 = new ImageView(image6);
    view6.setPreserveRatio(true);
    view6.setFitHeight(32);
    kofiLink.setGraphic(view6);

    EventManager.getInstance().addListener(this);
  }
}