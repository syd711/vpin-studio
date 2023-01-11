package de.mephisto.vpin.poppermenu;

import de.mephisto.vpin.commons.utils.Updater;
import de.mephisto.vpin.commons.utils.WidgetFactory;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

import static de.mephisto.vpin.poppermenu.MenuMain.client;

public class MenuController implements Initializable {
  private final static Logger LOG = LoggerFactory.getLogger(MenuController.class);

  private double xOffset;
  private double yOffset;

  @FXML
  private Label studioLabel;

  @FXML
  private Label versionLabel;

  @FXML
  private Label updateLabel;

  @FXML
  private BorderPane header;
  private Stage stage;

  @FXML
  private void onCloseClick() {
    System.exit(0);
  }

  private void onUpdateCheck(String version) {

  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    Font font = Font.font("Impact", FontPosture.findByName("regular"), 28);
    studioLabel.setFont(font);

    updateLabel.setText("Checking Version...");
    String version = client.version();
    versionLabel.setText(version);

    onUpdateCheck(version);
  }

  public void setStage(Stage stage) {
    this.stage = stage;
    header.setOnMousePressed(event -> {
      xOffset = stage.getX() - event.getScreenX();
      yOffset = stage.getY() - event.getScreenY();
    });
    header.setOnMouseDragged(event -> {
      stage.setX(event.getScreenX() + xOffset);
      stage.setY(event.getScreenY() + yOffset);
    });
  }
}
