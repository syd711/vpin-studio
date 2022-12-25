package de.mephisto.vpin.updater;

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

import static de.mephisto.vpin.updater.UpdaterMain.client;

public class UpdateController implements Initializable {
  private final static Logger LOG = LoggerFactory.getLogger(UpdateController.class);

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
    new Thread(() -> {
      String s = Updater.checkForUpdate(version);
      Platform.runLater(() -> {
        if (s == null) {
          WidgetFactory.showAlert(stage, "Unable to retrieve update information.", "Please check the log files or download the latest installer.");
          System.exit(0);
        }
        else if (!s.equalsIgnoreCase(version)) {
          Optional<ButtonType> result = WidgetFactory.showConfirmation(stage, "Download and install version " + s + "?",
              "The server and the UI will be updated afterwards.");
          if (result.isPresent() && result.get().equals(ButtonType.OK)) {
            runUpdate();
          }
        }
      });
    }).start();
  }

  private void runUpdate() {
    try {
      updateLabel.setText("Updating Server...");

      client.update();

      new Thread(() -> {
        try {
          int counter = 60;
          while(counter > 0) {
            Thread.sleep(2000);
            String version = client.waitForUpdate();
            if(version == null) {
              counter--;
              if(counter == 30) {
                Platform.runLater(() -> {
                  updateLabel.setText("Updating Server...not there yet...");
                });
              }
              continue;
            }
            break;
          }

          Platform.runLater(() -> {
            String version = client.waitForUpdate();
            if(version == null) {
              WidgetFactory.showAlert(stage, "Update failed", "Please check the server log for details.");
              System.exit(0);
            }

            updateLabel.setText("Updating Client...");
            new Thread(() -> {
              try {
                Updater.updateUI(Updater.LATEST_VERSION);
                Updater.restartClient();
              } catch (Exception e) {
                Platform.runLater(() -> {
                  LOG.error("UI update failed: " + e.getMessage(), e);
                  WidgetFactory.showAlert(stage, e.getMessage());
                  System.exit(0);
                });
              }
            }).start();
          });
        } catch (Exception e) {
          LOG.error("Client update failed: " + e.getMessage(), e);
          Platform.runLater(() -> {
            WidgetFactory.showAlert(stage, e.getMessage());
            System.exit(0);
          });
        }
      }).start();
    } catch (Exception e) {
      WidgetFactory.showAlert(stage, e.getMessage());
    }
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
