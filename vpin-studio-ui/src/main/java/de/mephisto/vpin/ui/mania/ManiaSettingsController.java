package de.mephisto.vpin.ui.mania;

import de.mephisto.vpin.commons.fx.UIDefaults;
import de.mephisto.vpin.commons.utils.TransitionUtil;
import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.mania.ManiaSettings;
import de.mephisto.vpin.restclient.preferences.PreferenceChangeListener;
import de.mephisto.vpin.ui.HeaderResizeableController;
import de.mephisto.vpin.ui.NavigationItem;
import de.mephisto.vpin.ui.SettingsSceneController;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.mania.util.ManiaHelper;
import de.mephisto.vpin.ui.util.Dialogs;
import javafx.animation.FadeTransition;
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

public class ManiaSettingsController extends SettingsSceneController implements Initializable, PreferenceChangeListener {
  private final static Logger LOG = LoggerFactory.getLogger(ManiaSettingsController.class);

  // Add a public no-args constructor
  public ManiaSettingsController() {
  }

  /**
   * a singleton
   */
  private static ManiaSettingsController instance;

  @FXML
  private Pane root;

  @FXML
  private Hyperlink versionLink;

  @FXML
  private Hyperlink kofiLink;

  @FXML
  private Label hostLabel;

  @FXML
  private BorderPane preferencesMain;

  @FXML
  private VBox navigationBox;

  @FXML
  private Button initialButton;

  @FXML
  private VBox menuItemsPanel;

  private Button lastSelection;

  private Node preferencesRoot;
  private static Node rootPane;

  private String lastScreen = "mania-account-settings.fxml";
  private static boolean open = false;
  private static ManiaSettingsController INSTANCE = null;

  public static void navigateTo(String id) {
    try {
      INSTANCE.load(id + ".fxml", null);
    }
    catch (IOException e) {
      LOG.error("Failed to navigate to: {}", id, e);
    }
  }

  public static boolean toggle() {
    if (open) {
      switchNode(null);
    }
    else {
      if (instance == null) {
        try {
          FXMLLoader loader = new FXMLLoader(ManiaSettingsController.class.getResource("scene-mania-preferences.fxml"));
          Node preferencesRoot = loader.load();
          ManiaSettingsController controller = loader.getController();
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

    open = !open;
    return open;
  }

  private void onOpen() {
    switchNode(preferencesRoot);
    root.setOpacity(0);
    if (lastScreen != null) {
      loadScreen(lastScreen);
    }
    menuItemsPanel.setVisible(ManiaHelper.isRegistered());
    TransitionUtil.createInFader(root, UIDefaults.FADER_DURATION).play();
  }

  @FXML
  private void onAccount(ActionEvent event) throws IOException {
    load("mania-account-settings.fxml", event);
  }

  @FXML
  private void onCabinet(ActionEvent event) throws IOException {
    load("mania-cabinet-settings.fxml", event);
  }


  @FXML
  private void onTournaments(ActionEvent event) throws IOException {
    load("mania-tournament-settings.fxml", event);
  }

  @FXML
  private void onClose(ActionEvent event) {
    doClose();
  }

  private static void doClose() {
    FadeTransition outFader = TransitionUtil.createOutFader(rootPane, UIDefaults.FADER_DURATION);
    outFader.setOnFinished(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent event) {
        HeaderResizeableController.toggleFriendsView();
      }
    });
    outFader.play();
    open = false;

  }

  @FXML
  private void onVersionLInk(ActionEvent event) {
    Dialogs.openUpdateInfoDialog(client.getSystemService().getVersion(), true);
  }

  @FXML
  private void onFriendsList(ActionEvent event) throws IOException {
    load("mania-friends-list.fxml", event);
  }

  @FXML
  private void onFriendsPendingInvites(ActionEvent event) throws IOException {
    load("mania-friends-pending-invites.fxml", event);
  }

  @FXML
  private void onPrivacySettings(ActionEvent event) throws IOException {
    load("mania-privacy-settings.fxml", event);
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
      FXMLLoader loader = new FXMLLoader(FriendsListController.class.getResource(screen));
      Node node = loader.load();
      preferencesMain.setCenter(node);
    }
    catch (Exception e) {
      LOG.error("Failed to loading friends view: " + e.getMessage(), e);
      WidgetFactory.showAlert(Studio.stage, "Error", e.getMessage());
    }
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    INSTANCE = this;

    client.getPreferenceService().addListener(this);
    menuItemsPanel.setVisible(ManiaHelper.isRegistered());

    lastSelection = initialButton;
    lastSelection.getStyleClass().add("preference-button-selected");

    versionLink.setText("VPin Studio Version " + Studio.getVersion());
    versionLink.setStyle("-fx-font-size : 12px;-fx-font-color: #B0ABAB;");
    hostLabel.setText(System.getProperty("os.name"));

    Image image6 = new Image(Studio.class.getResourceAsStream("ko-fi.png"));
    ImageView view6 = new ImageView(image6);
    view6.setPreserveRatio(true);
    view6.setFitHeight(32);
    kofiLink.setGraphic(view6);
  }

  @Override
  public void preferencesChanged(String key, Object value) {
    if (PreferenceNames.MANIA_SETTINGS.equals(key)) {
      menuItemsPanel.setVisible(ManiaHelper.isRegistered());
    }
  }
}