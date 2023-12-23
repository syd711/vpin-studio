package de.mephisto.vpin.ui.competitions;

import de.mephisto.vpin.commons.fx.discord.DiscordUserEntryController;
import de.mephisto.vpin.commons.utils.CommonImageUtil;
import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.restclient.competitions.CompetitionRepresentation;
import de.mephisto.vpin.restclient.competitions.CompetitionType;
import de.mephisto.vpin.restclient.competitions.JoinMode;
import de.mephisto.vpin.restclient.discord.DiscordChannel;
import de.mephisto.vpin.restclient.discord.DiscordServer;
import de.mephisto.vpin.restclient.players.PlayerRepresentation;
import de.mephisto.vpin.ui.NavigationController;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.StudioFXController;
import de.mephisto.vpin.ui.competitions.dialogs.CompetitionDiscordDialogController;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.text.DateFormat;
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
  private Tab maniaTab;

  @FXML
  private Tab tableSubscriptionsTab;

  @FXML
  private Label createdAtLabel;

  @FXML
  private Label uuidLabel;

  @FXML
  private Label startLabel;

  @FXML
  private Label endLabel;

  @FXML
  private Label scoreLimitLabel;

  @FXML
  private Label scoreValidationLabel;

  @FXML
  private Label channelLabel;

  @FXML
  private HBox serverBox;

  @FXML
  private HBox ownerBox;

  @FXML
  private TitledPane metaDataPane;

  @FXML
  private TitledPane competitionMembersPane;

  @FXML
  private VBox membersBox;

  @FXML
  private Accordion accordion;


  private CompetitionsOfflineController offlineController;
  private CompetitionsDiscordController discordController;
  private CompetitionsManiaController maniaController;
  private TableSubscriptionsController tableSubscriptionsController;

  private Optional<CompetitionRepresentation> competition = Optional.empty();

  // Add a public no-args constructor
  public CompetitionsController() {
  }

  @Override
  public void onViewActivated() {
    refreshUsers(competition);
    competitionMembersPane.setExpanded(competition.isPresent() && competition.get().getType().equals(CompetitionType.DISCORD.name()));
    refreshView(tabPane.getSelectionModel().selectedIndexProperty().get());
//    discordController.onReload();
//    tableSubscriptionsController.onReload();

//    offlineController.onViewActivated();
    discordController.onViewActivated();
    tableSubscriptionsController.onViewActivated();
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    loadTabs();
    updateSelection(Optional.empty());
    tabPane.getSelectionModel().selectedIndexProperty().addListener((observableValue, number, t1) -> {
      refreshView(t1);
    });
    accordion.setExpandedPane(metaDataPane);
  }

  private void refreshView(Number t1) {
    if (t1.intValue() == 0) {
      NavigationController.setBreadCrumb(Arrays.asList("Competitions", "Offline Competitions"));
      Optional<CompetitionRepresentation> selection = offlineController.getSelection();
      updateSelection(selection);
      offlineController.onReload();
    }
    else if (t1.intValue() == 1) {
      if (discordController != null) {
        NavigationController.setBreadCrumb(Arrays.asList("Competitions", "Discord Competitions"));
        Optional<CompetitionRepresentation> selection = discordController.getSelection();
        updateSelection(selection);
        discordController.onReload();
      }
    }
    else if (t1.intValue() == 2) {
//      if (tableSubscriptionsController != null) {
//        NavigationController.setBreadCrumb(Arrays.asList("Competitions", "VPin Mania Tournaments"));
//        Optional<CompetitionsManiaController.TournamentTreeModel> selection = maniaController.getSelection();
//        if(selection.isPresent()) {
//          updateSelection(Optional.of(selection.get().getCompetitionRepresentation()));
//        }
//        else {
//          updateSelection(Optional.empty());
//        }
//
//        maniaController.onReload();
//      }
    }
    else if (t1.intValue() == 3) {
      if (tableSubscriptionsController != null) {
        NavigationController.setBreadCrumb(Arrays.asList("Competitions", "Table Subscriptions"));
        Optional<CompetitionRepresentation> selection = tableSubscriptionsController.getSelection();
        updateSelection(selection);
        tableSubscriptionsController.onReload();
      }
    }
    else {
      throw new UnsupportedOperationException("Invalid tab id");
    }
  }

  public void setCompetition(CompetitionRepresentation competition) {
    this.competition = Optional.ofNullable(competition);
    updateSelection(Optional.ofNullable(competition));
  }

  private void updateSelection(Optional<CompetitionRepresentation> competitionRepresentation) {
    checkTitledPanes(competitionRepresentation);
    refreshUsers(competitionRepresentation);
    refreshMetaData(competitionRepresentation);
    updateForTabSelection(competitionRepresentation);
  }

  private void refreshMetaData(Optional<CompetitionRepresentation> competitionRepresentation) {
    uuidLabel.setText("-");
    startLabel.setText("-");
    endLabel.setText("-");
    scoreValidationLabel.setText("-");
    scoreLimitLabel.setText("-");

    if (competitionRepresentation.isPresent()) {
      String type = competitionRepresentation.get().getType();
      if (type.equals(CompetitionType.DISCORD.name()) || type.equals(CompetitionType.SUBSCRIPTION.name())) {
        CompetitionRepresentation competition = competitionRepresentation.get();
        if (metaDataPane.isVisible()) {
          uuidLabel.setText(competition.getUuid());
          serverBox.getChildren().removeAll(serverBox.getChildren());
          ownerBox.getChildren().removeAll(ownerBox.getChildren());

          if(competition.getJoinMode() != null) {
            JoinMode joinMode = JoinMode.valueOf(competition.getJoinMode());
            switch (joinMode) {
              case STRICT: {
                scoreValidationLabel.setText(CompetitionDiscordDialogController.STRICT_DESCRIPTION);
                break;
              }
              case CHECKSUM: {
                scoreValidationLabel.setText(CompetitionDiscordDialogController.CHECKSUM_DESCRIPTION);
                break;
              }
              case ROM_ONLY: {
                scoreValidationLabel.setText(CompetitionDiscordDialogController.ROM_DESCRIPTION);
                break;
              }
            }

          }

          if(competition.getScoreLimit() == 0) {
            scoreLimitLabel.setText("Table Defaults");
          }
          else {
            scoreLimitLabel.setText(String.valueOf(competition.getScoreLimit()));
          }


          createdAtLabel.setText(SimpleDateFormat.getDateTimeInstance().format(competition.getCreatedAt()));

          DiscordServer discordServer = client.getDiscordServer(competition.getDiscordServerId());
          if (discordServer != null) {
            String avatarUrl = discordServer.getAvatarUrl();
            Image image = null;
            if (avatarUrl == null) {
              image = new Image(Studio.class.getResourceAsStream("avatar-blank.png"));
            }
            else {
              image = new Image(avatarUrl);
            }

            ImageView view = new ImageView(image);
            view.setPreserveRatio(true);
            view.setFitWidth(50);
            view.setFitHeight(50);
            serverBox.getChildren().removeAll(serverBox.getChildren());
            Label label = new Label(discordServer.getName());
            label.setStyle("-fx-font-size: 14px;");

            CommonImageUtil.setClippedImage(view, (int) (image.getWidth() / 2));
            serverBox.getChildren().addAll(view, label);
          }

          List<DiscordChannel> discordChannels = client.getDiscordService().getDiscordChannels(competition.getDiscordServerId());
          Optional<DiscordChannel> first = discordChannels.stream().filter(channel -> channel.getId() == competition.getDiscordChannelId()).findFirst();
          first.ifPresent(discordChannel -> channelLabel.setText(discordChannel.getName()));

          PlayerRepresentation discordPlayer = client.getDiscordService().getDiscordPlayer(competition.getDiscordServerId(), Long.valueOf(competition.getOwner()));
          if (discordPlayer != null) {
            HBox hBox = new HBox(6);
            hBox.setAlignment(Pos.CENTER_LEFT);
            hBox = new HBox(6);
            hBox.setAlignment(Pos.CENTER_LEFT);
            Image image = new Image(client.getCachedUrlImage(discordPlayer.getAvatarUrl()));
            ImageView view = new ImageView(image);
            view.setPreserveRatio(true);
            view.setFitWidth(50);
            view.setFitHeight(50);
            ownerBox.getChildren().removeAll(ownerBox.getChildren());
            Label label = new Label(discordPlayer.getName());
            label.setStyle("-fx-font-size: 14px;");

            CommonImageUtil.setClippedImage(view, (int) (image.getWidth() / 2));
            ownerBox.getChildren().addAll(view, label);
          }

          if(competition.getStartDate() != null) {
            startLabel.setText(DateFormat.getDateInstance().format(competition.getStartDate()));
            endLabel.setText(DateFormat.getDateInstance().format(competition.getEndDate()));
          }
        }
      }
    }
  }

  private void checkTitledPanes(Optional<CompetitionRepresentation> cp) {
    competitionMembersPane.setDisable(cp.isEmpty());
    metaDataPane.setDisable(cp.isEmpty());

    if (cp.isPresent()) {
      CompetitionType competitionType = CompetitionType.valueOf(cp.get().getType());
      switch (competitionType) {
        case DISCORD: {
          competitionMembersPane.setDisable(false);
          competitionMembersPane.setExpanded(true);
          metaDataPane.setDisable(false);
          metaDataPane.setExpanded(false);
          break;
        }
        case SUBSCRIPTION: {
          competitionMembersPane.setDisable(false);
          competitionMembersPane.setExpanded(true);
          metaDataPane.setDisable(false);
          metaDataPane.setExpanded(false);
          break;
        }
        case MANIA: {
          competitionMembersPane.setDisable(true);
          competitionMembersPane.setExpanded(false);
          metaDataPane.setDisable(true);
          metaDataPane.setExpanded(false);
          break;
        }
        case OFFLINE: {
          competitionMembersPane.setDisable(true);
          competitionMembersPane.setExpanded(false);
          metaDataPane.setDisable(true);
          metaDataPane.setExpanded(false);
          break;
        }
        default: {
          throw new UnsupportedOperationException("Competition type " + competitionType + " is not mapped.");
        }
      }
    }
  }

  private void updateForTabSelection(Optional<CompetitionRepresentation> competitionRepresentation) {
    int index = tabPane.getSelectionModel().selectedIndexProperty().get();
    if (index == 0) {
      if (competitionRepresentation.isPresent()) {
        NavigationController.setBreadCrumb(Arrays.asList("Competitions", "Offline Competitions", competitionRepresentation.get().getName()));
      }
      else {
        NavigationController.setBreadCrumb(Arrays.asList("Competitions", "Offline Competitions"));
      }
    }
    else if (index == 1) {
      if (competitionRepresentation.isPresent()) {
        NavigationController.setBreadCrumb(Arrays.asList("Competitions", "Discord Competitions", competitionRepresentation.get().getName()));
      }
      else {
        NavigationController.setBreadCrumb(Arrays.asList("Competitions", "Discord Competitions"));
      }
    }
    else if (index == 2) {
      if (competitionRepresentation.isPresent()) {
        NavigationController.setBreadCrumb(Arrays.asList("Competitions", "VPin Mania Tournaments", competitionRepresentation.get().getName()));
      }
      else {
        NavigationController.setBreadCrumb(Arrays.asList("Competitions", "VPin Mania Tournaments"));
      }
    }
    else if (index == 3) {
      if (competitionRepresentation.isPresent()) {
        NavigationController.setBreadCrumb(Arrays.asList("Competitions", "Table Subscriptions", competitionRepresentation.get().getName()));
      }
      else {
        NavigationController.setBreadCrumb(Arrays.asList("Competitions", "Table Subscriptions"));
      }
    }
    else {
      throw new UnsupportedOperationException("Invalid tab.");
    }
  }

  private void refreshUsers(Optional<CompetitionRepresentation> cp) {
    membersBox.getChildren().removeAll(membersBox.getChildren());
    if (cp.isPresent()) {
      CompetitionRepresentation competition = cp.get();
      if (competitionMembersPane.isVisible()) {
        if (!competition.isActive()) {
          membersBox.getChildren().add(WidgetFactory.createDefaultLabel("The competition is not active."));
        }
        else {
          List<PlayerRepresentation> memberList = client.getCompetitionService().getDiscordCompetitionPlayers(competition.getId());
          if (memberList.isEmpty()) {
            membersBox.getChildren().add(WidgetFactory.createDefaultLabel("No discord members have joined this competition yet."));
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
    }
  }

  private void loadTabs() {
    try {
      FXMLLoader loader = new FXMLLoader(CompetitionsOfflineController.class.getResource("tab-competitions-offline.fxml"));
      Parent parent = loader.load();
      offlineController = loader.getController();
      offlineController.setCompetitionsController(this);
      offlineTab.setContent(parent);
    } catch (IOException e) {
      LOG.error("failed to load offline: " + e.getMessage(), e);
    }

    try {
      FXMLLoader loader = new FXMLLoader(CompetitionsDiscordController.class.getResource("tab-competitions-discord.fxml"));
      Parent parent = loader.load();
      discordController = loader.getController();
      discordController.setCompetitionsController(this);
      onlineTab.setContent(parent);
    } catch (IOException e) {
      LOG.error("failed to load online: " + e.getMessage(), e);
    }

    try {
      FXMLLoader loader = new FXMLLoader(CompetitionsDiscordController.class.getResource("tab-competitions-mania.fxml"));
      Parent parent = loader.load();
      maniaController = loader.getController();
      maniaController.setCompetitionsController(this);
      maniaTab.setContent(parent);
    } catch (IOException e) {
      LOG.error("failed to load online: " + e.getMessage(), e);
    }

    try {
      FXMLLoader loader = new FXMLLoader(TableSubscriptionsController.class.getResource("tab-competitions-subscriptions.fxml"));
      Parent parent = loader.load();
      tableSubscriptionsController = loader.getController();
      tableSubscriptionsController.setCompetitionsController(this);
      tableSubscriptionsTab.setContent(parent);
    } catch (IOException e) {
      LOG.error("failed to load subscriptions: " + e.getMessage(), e);
    }
  }

}