package de.mephisto.vpin.ui.tournaments;

import de.mephisto.vpin.restclient.competitions.CompetitionRepresentation;
import de.mephisto.vpin.restclient.competitions.CompetitionType;
import de.mephisto.vpin.restclient.competitions.JoinMode;
import de.mephisto.vpin.ui.NavigationController;
import de.mephisto.vpin.ui.StudioFXController;
import de.mephisto.vpin.ui.competitions.dialogs.CompetitionDiscordDialogController;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Optional;
import java.util.ResourceBundle;

public class TournamentsController implements Initializable, StudioFXController {
  private final static Logger LOG = LoggerFactory.getLogger(TournamentsController.class);

  @FXML
  private TabPane tabPane;

  @FXML
  private Tab maniaTab;

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

  private TournamentsManiaController maniaController;

  private Optional<CompetitionRepresentation> competition = Optional.empty();

  // Add a public no-args constructor
  public TournamentsController() {
  }

  @Override
  public void onViewActivated() {
    refreshUsers(competition);
    competitionMembersPane.setExpanded(true);
    refreshView(tabPane.getSelectionModel().selectedIndexProperty().get());
    maniaController.onViewActivated();
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
//    if (t1.intValue() == 0) {
//      NavigationController.setBreadCrumb(Arrays.asList("Competitions", "Offline Competitions"));
//      Optional<CompetitionRepresentation> selection = maniaController.getSelection();
//      updateSelection(selection);
//      maniaController.onReload();
//    }
//    else {
//      throw new UnsupportedOperationException("Invalid tab id");
//    }
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

        }
      }
    }
  }

  private void checkTitledPanes(Optional<CompetitionRepresentation> cp) {
    competitionMembersPane.setDisable(cp.isEmpty());
    metaDataPane.setDisable(cp.isEmpty());
  }

  private void updateForTabSelection(Optional<CompetitionRepresentation> competitionRepresentation) {
    int index = tabPane.getSelectionModel().selectedIndexProperty().get();
    if (index == 0) {
      if (competitionRepresentation.isPresent()) {
        NavigationController.setBreadCrumb(Arrays.asList("Competitions", "VPin Mania Tournaments", competitionRepresentation.get().getName()));
      }
      else {
        NavigationController.setBreadCrumb(Arrays.asList("Competitions", "VPin Mania Tournaments"));
      }
    }
    else {
      throw new UnsupportedOperationException("Invalid tab.");
    }
  }

  private void refreshUsers(Optional<CompetitionRepresentation> cp) {
//    membersBox.getChildren().removeAll(membersBox.getChildren());
//    if (cp.isPresent()) {
//      CompetitionRepresentation competition = cp.get();
//      if (competitionMembersPane.isVisible()) {
//        if (!competition.isActive()) {
//          membersBox.getChildren().add(WidgetFactory.createDefaultLabel("The competition is not active."));
//        }
//        else {
//          List<PlayerRepresentation> memberList = client.getCompetitionService().getDiscordCompetitionPlayers(competition.getId());
//          if (memberList.isEmpty()) {
//            membersBox.getChildren().add(WidgetFactory.createDefaultLabel("No discord members have joined this competition yet."));
//          }
//          else {
//            for (PlayerRepresentation player : memberList) {
//              try {
//                FXMLLoader loader = new FXMLLoader(DiscordUserEntryController.class.getResource("discord-user.fxml"));
//                Parent playerPanel = loader.load();
//                DiscordUserEntryController controller = loader.getController();
//                controller.setData(player);
//                membersBox.getChildren().add(playerPanel);
//              } catch (IOException e) {
//                LOG.error("Failed to load discord player list: " + e.getMessage(), e);
//              }
//            }
//          }
//        }
//      }
//    }
  }

  private void loadTabs() {
    try {
      FXMLLoader loader = new FXMLLoader(TournamentsManiaController.class.getResource("tab-competitions-mania.fxml"));
      Parent parent = loader.load();
      maniaController = loader.getController();
      maniaController.setTournamentsController(this);
      maniaTab.setContent(parent);
    } catch (IOException e) {
      LOG.error("failed to load online: " + e.getMessage(), e);
    }
  }

}