package de.mephisto.vpin.ui;

import de.mephisto.vpin.ui.preferences.ScreensPreferencesController;
import de.mephisto.vpin.ui.tables.TablesController;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.Locale;
import java.util.ResourceBundle;

public class PreferencesController implements Initializable {
  private final static Logger LOG = LoggerFactory.getLogger(PreferencesController.class);

  // Add a public no-args constructor
  public PreferencesController() {
  }

  @FXML
  private Label versionLabel;

  @FXML
  private Label hostLabel;

  @FXML
  private BorderPane preferencesMain;

  @FXML
  private void onClose(ActionEvent event) throws IOException {
    Node lookup = Studio.stage.getScene().lookup("#root");
    BorderPane main = (BorderPane) lookup;
    StackPane stack = (StackPane) main.getCenter();
    stack.getChildren().remove(1);
    NavigationController.refresh();
  }

  @FXML
  private void onAvatar(ActionEvent event) throws IOException {
    load("preference-avatar.fxml");
  }

  @FXML
  private void onVPinName(ActionEvent event) throws IOException {
    load("preference-vpin-name.fxml");
  }

  @FXML
  private void onMediaValidation(ActionEvent event) throws IOException {
    load("preference-validators-pinuppopper.fxml");
  }

  @FXML
  private void onVPXValidation(ActionEvent event) throws IOException {
    load("preference-validators-vpx.fxml");
  }

  @FXML
  private void onScreens(ActionEvent event) throws IOException {
    load("preference-screens.fxml");
  }

  @FXML
  private void onReset(ActionEvent event) throws IOException {
    load("preference-reset.fxml");
  }

  @FXML
  private void onOverlay(ActionEvent event) throws IOException {
    load("preference-overlay.fxml");
  }

  @FXML
  private void onHighscoreCards(ActionEvent event) throws IOException {
    load("preference-highscore-cards.fxml");
  }

  @FXML
  private void onScoreFormat(ActionEvent event) throws IOException {
    load("preference-highscores.fxml");
  }

  @FXML
  private void onRankings(ActionEvent event) throws IOException {
    load("preference-player-rankings.fxml");
  }

  @FXML
  private void onServiceInfo(ActionEvent event) throws IOException {
    load("preference-service-info.fxml");
  }

  @FXML
  private void onServiceOptions(ActionEvent event) throws IOException {
    load("preference-service-options.fxml");
  }

  @FXML
  private void onDiscordBot(ActionEvent event) throws IOException {
    load("preference-discord-bot.fxml");
  }

  @FXML
  private void onDiscordWebhook(ActionEvent event) throws IOException {
    load("preference-discord-webhook.fxml");
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

  private void load(String screen) throws IOException {
    FXMLLoader loader = new FXMLLoader(ScreensPreferencesController.class.getResource(screen));
    Node node = loader.load();
    preferencesMain.setCenter(node);
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    versionLabel.setText("VPin Studio Version " + Studio.getVersion());
    hostLabel.setText(System.getProperty("os.name"));
  }
}