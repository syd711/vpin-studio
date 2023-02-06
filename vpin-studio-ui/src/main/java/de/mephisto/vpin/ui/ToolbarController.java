package de.mephisto.vpin.ui;

import de.mephisto.vpin.commons.utils.Updater;
import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.ui.jobs.JobPoller;
import de.mephisto.vpin.ui.util.Dialogs;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.MenuButton;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

import static de.mephisto.vpin.ui.Studio.client;

public class ToolbarController implements Initializable {
  private final static Logger LOG = LoggerFactory.getLogger(ToolbarController.class);

  @FXML
  private Button updateBtn;

  @FXML
  private MenuButton jobBtn;

  private Node preferencesRoot;

  // Add a public no-args constructor
  public ToolbarController() {
  }

  @FXML
  private void onUpdate() {
    String newVersion = Updater.checkForUpdate(Studio.getVersion());
    Optional<ButtonType> result = WidgetFactory.showConfirmation(Studio.stage, "Update " + newVersion, "A new update has been found. Download and install update for server and client?");
    if (result.isPresent() && result.get().equals(ButtonType.OK)) {
      Dialogs.openUpdateDialog();
    }
  }

  @FXML
  private void onDisconnect() {
    Studio.stage.close();
    Studio.loadLauncher(new Stage());
  }

  @FXML
  private void onSettings(ActionEvent event) throws IOException {
    Node lookup = Studio.stage.getScene().lookup("#root");
    BorderPane main = (BorderPane) lookup;
    StackPane stack = (StackPane) main.getCenter();
    stack.getChildren().add(preferencesRoot);
  }


  @FXML
  private void onClearCache() {
    client.clearCache();
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    this.jobBtn.setVisible(false);
    new JobPoller(this.jobBtn);

    if (preferencesRoot == null) {
      try {
        FXMLLoader loader = new FXMLLoader(NavigationController.class.getResource("scene-preferences.fxml"));
        preferencesRoot = loader.load();
      } catch (IOException e) {
        LOG.error("Failed to load preferences: " + e.getMessage(), e);
      }
    }

    new Thread(() -> {
      Platform.runLater(() -> {
        String s = Updater.checkForUpdate(Studio.getVersion());
        updateBtn.setVisible(!StringUtils.isEmpty(s));
      });
    }).start();

  }
}