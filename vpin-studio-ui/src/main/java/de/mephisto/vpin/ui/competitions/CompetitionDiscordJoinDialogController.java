package de.mephisto.vpin.ui.competitions;

import de.mephisto.vpin.commons.fx.DialogController;
import de.mephisto.vpin.restclient.*;
import de.mephisto.vpin.restclient.discord.DiscordChannel;
import de.mephisto.vpin.restclient.discord.DiscordCompetitionData;
import de.mephisto.vpin.restclient.discord.DiscordServer;
import de.mephisto.vpin.restclient.representations.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.net.URL;
import java.text.DateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.*;

import static de.mephisto.vpin.ui.Studio.client;

public class CompetitionDiscordJoinDialogController implements Initializable, DialogController {
  private final static Logger LOG = LoggerFactory.getLogger(CompetitionDiscordJoinDialogController.class);

  @FXML
  private ImageView iconPreview;

  @FXML
  private ImageView badgePreview;

  @FXML
  private ComboBox<String> competitionIconCombo;

  @FXML
  private Label tableLabel;

  @FXML
  private Label ownerLabel;

  @FXML
  private Label tableMatchLabel;

  @FXML
  private ComboBox<DiscordChannel> channelsCombo;

  @FXML
  private ComboBox<DiscordServer> serversCombo;

  @FXML
  private Button saveBtn;

  @FXML
  private Label nameLabel;

  @FXML
  private Label remainingDaysLabel;

  @FXML
  private Label startDateLabel;

  @FXML
  private Label endDateLabel;

  @FXML
  private Pane validationContainer;

  @FXML
  private Label validationTitle;

  @FXML
  private Label validationDescription;

  private CompetitionRepresentation competition;

  private DiscordCompetitionData discordCompetitionData;

  @FXML
  private void onCancelClick(ActionEvent e) {
    this.competition = null;
    Stage stage = (Stage) ((Button) e.getSource()).getScene().getWindow();
    stage.close();
  }

  @FXML
  private void onSaveClick(ActionEvent e) {
    Stage stage = (Stage) ((Button) e.getSource()).getScene().getWindow();
    stage.close();
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    competition = new CompetitionRepresentation();
    competition.setType(CompetitionType.DISCORD.name());
    competition.setName("");
    competition.setUuid("");
    competition.setOwner("");

    Date end = Date.from(LocalDate.now().plus(7, ChronoUnit.DAYS).atStartOfDay(ZoneId.systemDefault()).toInstant());
    competition.setStartDate(Date.from(LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant()));
    competition.setEndDate(end);

    saveBtn.setDisable(true);


    List<DiscordServer> servers = client.getDiscordServers();
    ObservableList<DiscordServer> discordServers = FXCollections.observableArrayList(servers);
    serversCombo.getItems().addAll(discordServers);
    serversCombo.valueProperty().addListener((observableValue, gameRepresentation, t1) -> {
      this.discordCompetitionData = null;
      channelsCombo.setDisable(false);
      channelsCombo.setItems(FXCollections.observableArrayList(client.getDiscordChannels(t1.getId())));
      validate();
    });


    ObservableList<DiscordChannel> discordChannels = FXCollections.observableArrayList(new ArrayList<>());
    channelsCombo.setDisable(true);
    channelsCombo.getItems().addAll(discordChannels);
    channelsCombo.valueProperty().addListener((observableValue, gameRepresentation, t1) -> {
      this.discordCompetitionData = client.getDiscordCompetitionData(serversCombo.getValue().getId(), t1.getId());
      validate();
    });

    ArrayList<String> badges = new ArrayList<>(client.getCompetitionBadges());
    badges.add(0, null);
    ObservableList<String> imageList = FXCollections.observableList(badges);
    competitionIconCombo.setItems(imageList);
    competitionIconCombo.setCellFactory(c -> new CompetitionImageListCell(client));
    competitionIconCombo.valueProperty().addListener((observableValue, s, t1) -> {
      competition.setBadge(t1);
//      refreshPreview(tableCombo.getValue(), t1);
      validate();
    });

    validate();
  }

  private void refreshPreview(GameRepresentation game, String badge) {
    if (game != null) {
      GameMediaRepresentation gameMedia = game.getGameMedia();
      GameMediaItemRepresentation mediaItem = gameMedia.getMedia().get(PopperScreen.Wheel.name());
      if (mediaItem != null) {
        ByteArrayInputStream gameMediaItem = client.getGameMediaItem(game.getId(), PopperScreen.Wheel);
        Image image = new Image(gameMediaItem);
        iconPreview.setImage(image);

        if (badge != null) {
          Image badgeIcon = new Image(client.getCompetitionBadge(badge));
          badgePreview.setImage(badgeIcon);
        }
        else {
          badgePreview.setImage(null);
        }
      }
    }
    else {
      iconPreview.setImage(null);
    }
  }

  private void validate() {
    this.competition = null;

    validationContainer.setVisible(true);
    this.saveBtn.setDisable(true);

    this.tableLabel.setText("-");
    this.startDateLabel.setText("-");
    this.endDateLabel.setText("-");
    this.remainingDaysLabel.setText("-");
    this.nameLabel.setText("-");

    if(this.discordCompetitionData == null) {
      validationTitle.setText("No competition selected.");
      validationDescription.setText("Select a discord server and channel where the competition takes place.");
      return;
    }

    long serverId = this.serversCombo.getValue().getId();
    long channelId = this.channelsCombo.getValue().getId();

    LocalDate end = discordCompetitionData.getEndDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    LocalDate now = new Date().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

    long remainingDays = ChronoUnit.DAYS.between(now, end);
    if (remainingDays < 0) {
      remainingDays = 0;
    }

    this.tableLabel.setText(this.discordCompetitionData.getTableName());
    this.startDateLabel.setText(DateFormat.getDateInstance().format(this.discordCompetitionData.getStartDate()));
    this.endDateLabel.setText(DateFormat.getDateInstance().format(this.discordCompetitionData.getEndDate()));
    this.remainingDaysLabel.setText(remainingDays + " days");
    this.nameLabel.setText(this.discordCompetitionData.getName());

    PlayerRepresentation discordPlayer = client.getDiscordPlayer(serverId, Long.parseLong(this.discordCompetitionData.getOwner()));
    if(discordPlayer != null) {
      this.ownerLabel.setText(discordPlayer.getName());
    }

    validationContainer.setVisible(false);
    this.saveBtn.setDisable(false);
  }

  @Override
  public void onDialogCancel() {
    this.competition = null;
  }

  public CompetitionRepresentation getCompetition() {
    return competition;
  }

  public static class CompetitionImageListCell extends ListCell<String> {
    private final VPinStudioClient client;

    public CompetitionImageListCell(VPinStudioClient client) {
      this.client = client;
    }

    protected void updateItem(String item, boolean empty) {
      super.updateItem(item, empty);
      setGraphic(null);
      setText(null);
      if (item != null) {
        Image image = new Image(client.getCompetitionBadge(item));
        ImageView imageView = new ImageView(image);
        imageView.setFitWidth(80);

        int percentageWidth = (int) (80 * 100 / image.getWidth());
        int height = (int) (image.getHeight() * percentageWidth / 100);
        imageView.setFitHeight(height);
        setGraphic(imageView);
        setText(item);
      }
    }
  }
}
