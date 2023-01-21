package de.mephisto.vpin.ui.competitions;

import de.mephisto.vpin.ui.StudioFXController;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.layout.VBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import static de.mephisto.vpin.ui.Studio.client;

public class CompetitionsController implements Initializable, StudioFXController {
  private final static Logger LOG = LoggerFactory.getLogger(CompetitionsController.class);

  private CompetitionsOfflineController offlineController;
  private CompetitionsDiscordController onlineController;

  @FXML
  private Tab offlineTab;

  @FXML
  private Tab onlineTab;

  // Add a public no-args constructor
  public CompetitionsController() {
  }

  @Override
  public void onViewActivated() {
    refreshView();
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    refreshView();
  }

  private void refreshView() {
    if(offlineController == null) {
      try {
        FXMLLoader loader = new FXMLLoader(CompetitionsOfflineController.class.getResource("tab-competitions-offline.fxml"));
        Parent offline = loader.load();
        offlineController = loader.getController();
        offlineTab.setContent(offline);
      } catch (IOException e) {
        LOG.error("failed to load buildIn players: " + e.getMessage(), e);
      }
    }

    boolean isDiscordBotAvailable = client.isDiscordBotAvailable();
    if(isDiscordBotAvailable && onlineController == null) {
      try {
        FXMLLoader loader = new FXMLLoader(CompetitionsDiscordController.class.getResource("tab-competitions-discord.fxml"));
        Parent offline = loader.load();
        onlineController = loader.getController();
        onlineTab.setContent(offline);
      } catch (IOException e) {
        LOG.error("failed to load buildIn players: " + e.getMessage(), e);
      }
    }
    else {
      VBox content = new VBox();
      content.setSpacing(3);
      content.setPadding(new Insets(12, 12, 12, 12));
      Label title = new Label("No Discord bot configured.");
      title.setStyle("-fx-font-weight: bold;-fx-font-size: 14px;");
      Label description = new Label("Open the Discord preferences and check how to create and configure a Discord bot.");
      description.setStyle("-fx-font-size: 14px;");
      content.getChildren().addAll(title, description);
      onlineTab.setContent(content);
    }
  }
}