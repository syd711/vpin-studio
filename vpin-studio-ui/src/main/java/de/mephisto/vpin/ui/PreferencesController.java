package de.mephisto.vpin.ui;

import de.mephisto.vpin.commons.fx.Features;
import de.mephisto.vpin.commons.fx.UIDefaults;
import de.mephisto.vpin.commons.utils.TransitionUtil;
import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.restclient.frontend.FrontendType;
import de.mephisto.vpin.ui.events.EventManager;
import de.mephisto.vpin.ui.events.StudioEventListener;
import de.mephisto.vpin.ui.preferences.ClientSettingsPreferencesController;
import de.mephisto.vpin.ui.preferences.PreferenceType;
import de.mephisto.vpin.ui.util.Dialogs;
import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

import static de.mephisto.vpin.ui.Studio.client;

public class PreferencesController extends SettingsSceneController implements Initializable, StudioEventListener {
  private final static Logger LOG = LoggerFactory.getLogger(PreferencesController.class);

  // Add a public no-args constructor
  public PreferencesController() {
  }

  /**
   * a singleton
   */
  private static PreferencesController instance;

  @FXML
  private Pane root;

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
  private Button settings_clientBtn;

  @FXML
  private Button mameBtn;

  @FXML
  private Button vpuBtn;

  @FXML
  private Button vpfBtn;

  @FXML
  private Button iScoredBtn;

  @FXML
  private Button validators_screensBtn;

  @FXML
  private Button validators_backglassBtn;

  @FXML
  private Button repositoriesBtn;

  @FXML
  private Button popperSettingsBtn;

  @FXML
  private Button pinballXSettingsBtn;

  @FXML
  private Button vpbmBtn;

  @FXML
  private Button overlayBtn;

  @FXML
  private Button pauseMenuBtn;

  @FXML
  private Button validators_vpxBtn;

  @FXML
  private Button highscore_cardsBtn;

  @FXML
  private Button systemBtn;

  @FXML
  private Button highscoresBtn;

  @FXML
  private Button webhooksBtn;

  @FXML
  private BorderPane preferencesMain;

  @FXML
  private VBox navigationBox;

  @FXML
  private VBox frontendPreferences;

  @FXML
  private Button notificationsButton;

  private Button lastSelection;

  private Node preferencesRoot;

  private PreferenceType dirtyPreferenceType = null;

  private String lastScreen = "preference-settings-cabinet.fxml";

  public static void markDirty(PreferenceType preferenceType) {
    if (instance != null) {
      instance.dirtyPreferenceType = preferenceType;
    }
  }

  public static void open() {
    if (instance == null) {
      try {
        FXMLLoader loader = new FXMLLoader(PreferencesController.class.getResource("scene-preferences.fxml"));
        Node preferencesRoot = loader.load();
        PreferencesController controller = loader.getController();
        preferencesRoot.setUserData(controller);
        preferencesRoot.addEventHandler(KeyEvent.ANY, ke -> {
          if (ke.getCode() == KeyCode.ESCAPE && !ke.isConsumed()) {
            instance.onClose(null);
          }
          // consume all other events, preventing them to bubble up
          ke.consume();
        });
        controller.preferencesRoot = preferencesRoot;

        instance = controller;
      }
      catch (IOException e) {
        LOG.error("Failed to load preferences: " + e.getMessage(), e);
      }
    }
    instance.onOpen();
  }

  private void onOpen() {
    switchNode(preferencesRoot);
    root.setOpacity(0);
    if (lastScreen != null) {
      loadScreen(lastScreen);
    }
    TransitionUtil.createInFader(root, UIDefaults.FADER_DURATION).play();
  }

  @FXML
  private void onClose(ActionEvent event) {
    FadeTransition outFader = TransitionUtil.createOutFader(root, UIDefaults.FADER_DURATION);
    outFader.setOnFinished(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent event) {
        switchNode(null);
      }
    });
    outFader.play();
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
    load("preference-settings_client.fxml", event);
  }

  @FXML
  private void onWebhooks(ActionEvent event) throws IOException {
    load("preference-webhooks.fxml", event);
  }

  @FXML
  private void onSystemSettings(ActionEvent event) throws IOException {
    load("preference-system-settings.fxml", event);
  }

  @FXML
  private void onMediaValidation(ActionEvent event) throws IOException {
    load("preference-validators_screens.fxml", event);
  }

  @FXML
  private void onBackglassValidation(ActionEvent event) throws IOException {
    load("preference-validators_backglass.fxml", event);
  }


  @FXML
  private void onIScored(ActionEvent event) throws IOException {
    load("preference-iscored.fxml", event);
  }

  @FXML
  private void onSupport(ActionEvent event) throws IOException {
    load("preference-support.fxml", event);
  }

  @FXML
  private void onShortcuts(ActionEvent event) throws IOException {
    load("preference-shortcuts.fxml", event);
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
  private void onDOFLinx(ActionEvent event) throws IOException {
    load("preference-doflinx.fxml", event);
  }

  @FXML
  private void onReset(ActionEvent event) throws IOException {
    load("preference-frontend-reset.fxml", event);
  }

  @FXML
  private void onPopperSettings(ActionEvent event) throws IOException {
    load("preference-popper-settings.fxml", event);
  }

  @FXML
  private void onPinballXSettings(ActionEvent event) throws IOException {
    load("preference-pinballx-settings.fxml", event);
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
  private void onControllerSetup(ActionEvent event) throws IOException {
    load("preference-inputs.fxml", event);
  }

  @FXML
  private void onPauseMenu(ActionEvent event) throws IOException {
    load("preference-pause-menu.fxml", event);
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
  private void onVpu(ActionEvent event) throws IOException {
    load("preference-vpu.fxml", event);
  }

  @FXML
  private void onVpf(ActionEvent event) throws IOException {
    load("preference-vpf.fxml", event);
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
      instance.load("preference-" + preferenceType + ".fxml", null, preferenceType + "Btn");
    });
  }

  @FXML
  private void onDiscordLink() {
    Studio.browse(UIDefaults.DISCORD_INVITE_LINK);
  }

  @FXML
  private void onKofiLink() {
    Studio.browse("https://ko-fi.com/syd711");
  }

  private void load(String screen, ActionEvent event) throws IOException {
    load(screen, event, null);
  }

  private void load(String screen, ActionEvent event, String btnId) {
    if (lastSelection != null) {
      lastSelection.getStyleClass().remove("preference-button-selected");
    }
    else {
      avatarBtn.getStyleClass().remove("preference-button-selected");
    }

    if (event != null) {
      lastSelection = (Button) event.getSource();
      lastSelection.getStyleClass().add("preference-button-selected");
    }
    else if (btnId != null) {
      Optional<Node> first = navigationBox.getChildren().stream().filter(b -> btnId.equals(b.getId())).findFirst();
      if (first.isPresent()) {
        lastSelection = (Button) first.get();
        lastSelection.getStyleClass().add("preference-button-selected");
      }
    }

    loadScreen(screen);
  }

  private void loadScreen(String screen) {
    lastScreen = screen;

    try {
      FXMLLoader loader = new FXMLLoader(ClientSettingsPreferencesController.class.getResource(screen));
      Node node = loader.load();
      preferencesMain.setCenter(node);
    }
    catch (Exception e) {
      LOG.error("Failed to loading settings view: " + e.getMessage(), e);
      WidgetFactory.showAlert(Studio.stage, "Error", e.getMessage());
    }
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    popperSettingsBtn.managedProperty().bindBidirectional(popperSettingsBtn.visibleProperty());
    pinballXSettingsBtn.managedProperty().bindBidirectional(pinballXSettingsBtn.visibleProperty());
    repositoriesBtn.managedProperty().bindBidirectional(repositoriesBtn.visibleProperty());
    notificationsButton.managedProperty().bindBidirectional(notificationsButton.visibleProperty());
    vpbmBtn.managedProperty().bindBidirectional(vpbmBtn.visibleProperty());
    overlayBtn.managedProperty().bindBidirectional(overlayBtn.visibleProperty());
    pauseMenuBtn.managedProperty().bindBidirectional(pauseMenuBtn.visibleProperty());
    highscore_cardsBtn.managedProperty().bindBidirectional(highscore_cardsBtn.visibleProperty());
    frontendPreferences.managedProperty().bindBidirectional(frontendPreferences.visibleProperty());
    validators_screensBtn.managedProperty().bindBidirectional(validators_screensBtn.visibleProperty());
    validators_backglassBtn.managedProperty().bindBidirectional(validators_backglassBtn.visibleProperty());
    vpuBtn.managedProperty().bindBidirectional(vpuBtn.visibleProperty());
    vpfBtn.managedProperty().bindBidirectional(vpfBtn.visibleProperty());
    webhooksBtn.managedProperty().bindBidirectional(webhooksBtn.visibleProperty());

    FrontendType frontendType = client.getFrontendService().getFrontendType();
    vpbmBtn.setVisible(frontendType.supportArchive());
    repositoriesBtn.setVisible(false);
//    repositoriesBtn.setVisible(frontendType.supportArchive());

    // activation of custom options according to installed frontend
    frontendPreferences.setVisible(frontendType.equals(FrontendType.Popper) || frontendType.equals(FrontendType.PinballX));
    popperSettingsBtn.setVisible(frontendType.equals(FrontendType.Popper));
    pinballXSettingsBtn.setVisible(frontendType.equals(FrontendType.PinballX));

    notificationsButton.setVisible(frontendType.isNotStandalone() && Features.NOTIFICATIONS_ENABLED);
    overlayBtn.setVisible(frontendType.isNotStandalone());

    pauseMenuBtn.setVisible(frontendType.supportControls());
    highscore_cardsBtn.setVisible(frontendType.isNotStandalone());
    validators_screensBtn.setVisible(frontendType.isNotStandalone());
    validators_backglassBtn.setVisible(frontendType.isNotStandalone());

    vpuBtn.setVisible(Features.VP_UNIVERSE);
    vpfBtn.setVisible(Features.VP_FORUMS);
    webhooksBtn.setVisible(Features.WEBHOOKS_ENABLED);

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