package de.mephisto.vpin.ui;

import de.mephisto.vpin.ui.preferences.AvatarPreferencesController;
import de.mephisto.vpin.ui.preferences.CardGenerationPreferencesController;
import de.mephisto.vpin.ui.preferences.NetworkPreferencesController;
import de.mephisto.vpin.ui.preferences.ScreensPreferencesController;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class PreferencesController implements Initializable {

  // Add a public no-args constructor
  public PreferencesController() {
  }

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
  private void onNetwork(ActionEvent event) throws IOException {
    load("preference-network.fxml");
  }

  @FXML
  private void onMediaValidation(ActionEvent event) throws IOException {
    load("preference-media-validators.fxml");
  }

  @FXML
  private void onConfigValidation(ActionEvent event) throws IOException {
    load("preference-config-validators.fxml");
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

  private void load(String screen) throws IOException {
    FXMLLoader loader = new FXMLLoader(ScreensPreferencesController.class.getResource(screen));
    Node node = loader.load();
    preferencesMain.setCenter(node);
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {

  }
}