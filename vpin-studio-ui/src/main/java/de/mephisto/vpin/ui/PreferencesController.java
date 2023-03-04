package de.mephisto.vpin.ui;

import de.mephisto.vpin.ui.preferences.ScreensPreferencesController;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
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
  private Button avatarBtn;

  @FXML
  private BorderPane preferencesMain;

  private Button lastSelection;

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
    load("preference-avatar.fxml", event);
  }

  @FXML
  private void onVPinName(ActionEvent event) throws IOException {
    load("preference-vpin-name.fxml", event);
  }

  @FXML
  private void onMediaValidation(ActionEvent event) throws IOException {
    load("preference-validators-pinuppopper.fxml", event);
  }

  @FXML
  private void onVPXValidation(ActionEvent event) throws IOException {
    load("preference-validators-vpx.fxml", event);
  }

  @FXML
  private void onVpaRepositories(ActionEvent event) throws IOException {
    load("preference-vpa-repositories.fxml", event);
  }

  @FXML
  private void onArchiveManager(ActionEvent event) throws IOException {
    load("preference-table-manager.fxml", event);
  }

  @FXML
  private void onScreens(ActionEvent event) throws IOException {
    load("preference-screens.fxml", event);
  }

  @FXML
  private void onReset(ActionEvent event) throws IOException {
    load("preference-reset.fxml", event);
  }

  @FXML
  private void onOverlay(ActionEvent event) throws IOException {
    load("preference-overlay.fxml", event);
  }

  @FXML
  private void onHighscoreCards(ActionEvent event) throws IOException {
    load("preference-highscore-cards.fxml", event);
  }

  @FXML
  private void onScoreFormat(ActionEvent event) throws IOException {
    load("preference-highscores.fxml", event);
  }

  @FXML
  private void onRankings(ActionEvent event) throws IOException {
    load("preference-player-rankings.fxml", event);
  }

  @FXML
  private void onServiceInfo(ActionEvent event) throws IOException {
    load("preference-service-info.fxml", event);
  }

  @FXML
  private void onServiceOptions(ActionEvent event) throws IOException {
    load("preference-service-options.fxml", event);
  }

  @FXML
  private void onDiscordBot(ActionEvent event) throws IOException {
    load("preference-discord-bot.fxml", event);
  }

  @FXML
  private void onDiscordBotFaq(ActionEvent event) throws IOException {
    load("preference-discord-faq.fxml", event);
  }

  @FXML
  private void onDiscordWebhook(ActionEvent event) throws IOException {
    load("preference-discord-webhook.fxml", event);
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
    if(lastSelection != null) {
      lastSelection.getStyleClass().remove("preference-button-selected");
    }
    else {
      avatarBtn.getStyleClass().remove("preference-button-selected");
    }

    lastSelection = (Button) event.getSource();
    lastSelection.getStyleClass().add("preference-button-selected");

    FXMLLoader loader = new FXMLLoader(ScreensPreferencesController.class.getResource(screen));
    Node node = loader.load();
    preferencesMain.setCenter(node);
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    avatarBtn.getStyleClass().add("preference-button-selected");
    versionLabel.setText("VPin Studio Version " + Studio.getVersion());
    hostLabel.setText(System.getProperty("os.name"));
  }
}