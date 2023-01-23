package de.mephisto.vpin.ui.competitions;

import de.mephisto.vpin.restclient.CompetitionType;
import de.mephisto.vpin.restclient.representations.CompetitionRepresentation;
import de.mephisto.vpin.ui.StudioFXController;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.VBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ResourceBundle;

import static de.mephisto.vpin.ui.Studio.client;

public class CompetitionsController implements Initializable, StudioFXController {
  private final static Logger LOG = LoggerFactory.getLogger(CompetitionsController.class);


  @FXML
  private Tab offlineTab;

  @FXML
  private Tab onlineTab;

  @FXML
  private Label createdAtLabel;

  @FXML
  private Label uuidLabel;

  @FXML
  private Label ownerLabel;

  @FXML
  private TitledPane metaDataBox;

  private CompetitionsOfflineController offlineController;
  private CompetitionsDiscordController onlineController;

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

  public void setCompetition(CompetitionRepresentation competition) {
    if(competition != null) {
      metaDataBox.setVisible(competition.getType().equals(CompetitionType.DISCORD.name()));
      if(metaDataBox.isVisible()) {
        ownerLabel.setText(competition.getOwner());
        uuidLabel.setText(competition.getUuid());
        createdAtLabel.setText(SimpleDateFormat.getDateTimeInstance().format(competition.getCreatedAt()));
      }
    }
    else {
      metaDataBox.setVisible(false);
    }
  }

  private void refreshView() {
    if (offlineController == null) {
      try {
        FXMLLoader loader = new FXMLLoader(CompetitionsOfflineController.class.getResource("tab-competitions-offline.fxml"));
        Parent offline = loader.load();
        offlineController = loader.getController();
        offlineController.setCompetitionsController(this);
        offlineTab.setContent(offline);
      } catch (IOException e) {
        LOG.error("failed to load buildIn players: " + e.getMessage(), e);
      }
    }

    boolean isDiscordBotAvailable = client.isDiscordBotAvailable();
    if (isDiscordBotAvailable) {
      if (onlineController == null) {
        try {
          FXMLLoader loader = new FXMLLoader(CompetitionsDiscordController.class.getResource("tab-competitions-discord.fxml"));
          Parent offline = loader.load();
          onlineController = loader.getController();
          onlineController.setCompetitionsController(this);
          onlineTab.setContent(offline);
        } catch (IOException e) {
          LOG.error("failed to load buildIn players: " + e.getMessage(), e);
        }
      }
    }
    else if(onlineTab.getContent() == null) {
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