package de.mephisto.vpin.ui.competitions;

import de.mephisto.vpin.commons.fx.discord.DiscordUserEntryController;
import de.mephisto.vpin.restclient.CompetitionType;
import de.mephisto.vpin.restclient.DiscordServer;
import de.mephisto.vpin.restclient.representations.CompetitionRepresentation;
import de.mephisto.vpin.restclient.representations.PlayerRepresentation;
import de.mephisto.vpin.ui.NavigationController;
import de.mephisto.vpin.ui.StudioFXController;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TitledPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

import static de.mephisto.vpin.ui.Studio.client;

public class CompetitionsController implements Initializable, StudioFXController {
  private final static Logger LOG = LoggerFactory.getLogger(CompetitionsController.class);

  @FXML
  private TabPane tabPane;

  @FXML
  private Tab offlineTab;

  @FXML
  private Tab onlineTab;

  @FXML
  private Label createdAtLabel;

  @FXML
  private Label uuidLabel;

  @FXML
  private Label serverNameLabel;

  @FXML
  private ImageView bannerImageView;


  @FXML
  private Label ownerLabel;

  @FXML
  private TitledPane metaDataPane;

  @FXML
  private VBox membersBox;

  @FXML
  private TitledPane competitionMembersPane;

  private CompetitionsOfflineController offlineController;
  private CompetitionsDiscordController discordController;

  private Optional<CompetitionRepresentation> competition = Optional.empty();

  private Label noPlayersLabel;

  // Add a public no-args constructor
  public CompetitionsController() {
  }

  @Override
  public void onViewActivated() {
    refreshUsers(competition);
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    tabPane.getSelectionModel().selectedIndexProperty().addListener((observableValue, number, t1) -> {
      if (t1.intValue() == 0) {
        NavigationController.setBreadCrumb(Arrays.asList("Competitions", "Offline Competitions"));
        Optional<CompetitionRepresentation> selection = offlineController.getSelection();
        updateSelection(selection);
        offlineController.onReload();
      }
      else {
        if(discordController != null) {
          NavigationController.setBreadCrumb(Arrays.asList("Competitions", "Discord Competitions"));
          Optional<CompetitionRepresentation> selection = discordController.getSelection();
          updateSelection(selection);
          discordController.onReload();
        }
      }
    });

    updateSelection(Optional.empty());
  }

  public void setCompetition(CompetitionRepresentation competition) {
    this.competition = Optional.ofNullable(competition);
    updateSelection(Optional.ofNullable(competition));
  }

  private void updateSelection(Optional<CompetitionRepresentation> competitionRepresentation) {
    checkTabs();
    checkTitledPanes(competitionRepresentation);

    refreshUsers(competitionRepresentation);
    refreshMetaData(competitionRepresentation);
    updateForTabSelection(competitionRepresentation);
  }

  private void refreshMetaData(Optional<CompetitionRepresentation> competitionRepresentation) {
    if (competitionRepresentation.isPresent()) {
      CompetitionRepresentation competition = competitionRepresentation.get();
      if (metaDataPane.isVisible()) {
        ownerLabel.setText(competition.getOwner());
        uuidLabel.setText(competition.getUuid());
        createdAtLabel.setText(SimpleDateFormat.getDateTimeInstance().format(competition.getCreatedAt()));

        DiscordServer discordServer = client.getDiscordServer(competition.getDiscordServerId());
        serverNameLabel.setText(discordServer.getName());
        bannerImageView.setImage(new Image(discordServer.getAvatarUrl()));
      }
    }
  }

  private void checkTitledPanes(Optional<CompetitionRepresentation> cp) {
    competitionMembersPane.setVisible(cp.isPresent() && cp.get().getType().equals(CompetitionType.DISCORD.name()));
    metaDataPane.setVisible(cp.isPresent() && cp.get().getType().equals(CompetitionType.DISCORD.name()));
  }

  private void updateForTabSelection(Optional<CompetitionRepresentation> competitionRepresentation) {
    int index = tabPane.getSelectionModel().selectedIndexProperty().get();
    if (index == 0) {
      if (competitionRepresentation.isPresent()) {
        NavigationController.setBreadCrumb(Arrays.asList("Competitions", "Offline Competitions", competitionRepresentation.get().getName()));
      }
      else {
        NavigationController.setBreadCrumb(Arrays.asList("Competitions", "Discord Competitions"));
      }
    }
    else {
      if (competitionRepresentation.isPresent()) {
        NavigationController.setBreadCrumb(Arrays.asList("Competitions", "Offline Competitions", competitionRepresentation.get().getName()));
      }
      else {
        NavigationController.setBreadCrumb(Arrays.asList("Competitions", "Discord Competitions"));
      }
    }
  }

  private void refreshUsers(Optional<CompetitionRepresentation> cp) {
    if (cp.isPresent()) {
      CompetitionRepresentation competition = cp.get();
      if (competitionMembersPane.isVisible()) {
        competitionMembersPane.setExpanded(true);

        List<PlayerRepresentation> memberList = client.getDiscordCompetitionPlayers(competition.getId());
        if(memberList.isEmpty()) {
          membersBox.getChildren().remove(getNoPlayersLabel());
          membersBox.getChildren().add(getNoPlayersLabel());
        }
        else {
          for (PlayerRepresentation player : memberList) {
            try {
              FXMLLoader loader = new FXMLLoader(DiscordUserEntryController.class.getResource("discord-user.fxml"));
              Parent playerPanel = loader.load();
              DiscordUserEntryController controller = loader.getController();
              controller.setData(player);
              membersBox.getChildren().add(playerPanel);
            } catch (IOException e) {
              LOG.error("Failed to load discord player list: " + e.getMessage(), e);
            }
          }
        }
      }
    }
    else {
      membersBox.getChildren().removeAll(membersBox.getChildren());
    }
  }

  private void checkTabs() {
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
      if (discordController == null) {
        try {
          FXMLLoader loader = new FXMLLoader(CompetitionsDiscordController.class.getResource("tab-competitions-discord.fxml"));
          Parent offline = loader.load();
          discordController = loader.getController();
          discordController.setCompetitionsController(this);
          onlineTab.setContent(offline);
        } catch (IOException e) {
          LOG.error("failed to load buildIn players: " + e.getMessage(), e);
        }
      }
    }
    else if (onlineTab.getContent() == null) {
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

  private Label getNoPlayersLabel() {
    if(this.noPlayersLabel == null) {
      noPlayersLabel = new Label("No discord members have joined this competition yet.");
      noPlayersLabel.setStyle("-fx-font-weight: bold;-fx-font-size: 14px;");
    }
    return this.noPlayersLabel;
  }
}