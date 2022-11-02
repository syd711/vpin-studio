package de.mephisto.vpin.ui;

import de.mephisto.vpin.ui.preferences.CardGenerationPreferencesController;
import de.mephisto.vpin.ui.preferences.PopperPreferencesController;
import de.mephisto.vpin.ui.preferences.ValidationsPreferencesController;
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
  private void onClose(ActionEvent event) {
    Node lookup = VPinStudioApplication.stage.getScene().lookup("#root");
    BorderPane main = (BorderPane) lookup;
    StackPane stack = (StackPane) main.getCenter();
    stack.getChildren().remove(1);
  }

  @FXML
  private void onVPin(ActionEvent event) throws IOException {
    FXMLLoader loader = new FXMLLoader(ValidationsPreferencesController.class.getResource("preference-myvpin.fxml"));
    Node node = loader.load();
    preferencesMain.setCenter(node);
  }

  @FXML
  private void onTableValidation(ActionEvent event) throws IOException {
    FXMLLoader loader = new FXMLLoader(ValidationsPreferencesController.class.getResource("preference-table-validation.fxml"));
    Node node = loader.load();
    preferencesMain.setCenter(node);
  }

  @FXML
  private void onPinUPPopper(ActionEvent event) throws IOException {
    FXMLLoader loader = new FXMLLoader(PopperPreferencesController.class.getResource("preference-popper.fxml"));
    Node node = loader.load();
    preferencesMain.setCenter(node);
  }


  @FXML
  private void onOverlay(ActionEvent event) {

  }

  @FXML
  private void onHighscoreCards(ActionEvent event) throws IOException {
    FXMLLoader loader = new FXMLLoader(CardGenerationPreferencesController.class.getResource("preference-highscore-cards.fxml"));
    Node node = loader.load();
    preferencesMain.setCenter(node);
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {

  }
}