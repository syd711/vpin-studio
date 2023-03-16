package de.mephisto.vpin.ui.competitions;

import de.mephisto.vpin.commons.fx.DialogController;
import de.mephisto.vpin.restclient.CompetitionType;
import de.mephisto.vpin.restclient.PopperScreen;
import de.mephisto.vpin.restclient.VPinStudioClient;
import de.mephisto.vpin.restclient.discord.DiscordBotStatus;
import de.mephisto.vpin.restclient.discord.DiscordChannel;
import de.mephisto.vpin.restclient.discord.DiscordCompetitionData;
import de.mephisto.vpin.restclient.discord.DiscordServer;
import de.mephisto.vpin.restclient.representations.*;
import de.mephisto.vpin.restclient.util.DateUtil;
import edu.umd.cs.findbugs.annotations.Nullable;
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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;

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
  private ComboBox<GameRepresentation> tableCombo;

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

  @FXML
  private CheckBox resetCheckbox;

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
    competition = new CompetitionRepresentation();
    competition.setType(CompetitionType.DISCORD.name());

    competition.setName(this.discordCompetitionData.getName());
    competition.setUuid(this.discordCompetitionData.getUuid());
    competition.setOwner(this.discordCompetitionData.getOwner());
    competition.setStartDate(this.discordCompetitionData.getSdt());
    competition.setEndDate(this.discordCompetitionData.getEdt());

    competition.setBadge(this.competitionIconCombo.getValue());
    competition.setGameId(this.tableCombo.getValue().getId());
    competition.setDiscordServerId(this.serversCombo.getValue().getId());
    competition.setDiscordChannelId(this.channelsCombo.getValue().getId());

    Stage stage = (Stage) ((Button) e.getSource()).getScene().getWindow();
    stage.close();
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
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
      if (discordCompetitionData != null) {
        List<GameRepresentation> gamesByRom = client.getGamesByRom(this.discordCompetitionData.getRom());
        tableCombo.getItems().addAll(FXCollections.observableList(gamesByRom));
        if (!gamesByRom.isEmpty()) {
          tableCombo.setValue(gamesByRom.get(0));
          refreshPreview(tableCombo.getValue(), null);
        }
      }
      else {
        this.tableCombo.setItems(FXCollections.observableList(new ArrayList<>()));
        this.refreshPreview(null, null);
      }
      validate();
    });

    ArrayList<String> badges = new ArrayList<>(client.getCompetitionBadges());
    badges.add(0, null);
    ObservableList<String> imageList = FXCollections.observableList(badges);
    competitionIconCombo.setItems(imageList);
    competitionIconCombo.setCellFactory(c -> new CompetitionImageListCell(client));
    competitionIconCombo.valueProperty().addListener((observableValue, s, t1) -> {
      refreshPreview(tableCombo.getValue(), t1);
      validate();
    });

    this.resetCheckbox.selectedProperty().addListener((observable, oldValue, newValue) -> validate());

    validate();
  }

  private void refreshPreview(@Nullable GameRepresentation game, @Nullable String badge) {
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

    this.competitionIconCombo.setDisable(true);
    this.tableCombo.setDisable(true);

    this.tableLabel.setText("-");
    this.startDateLabel.setText("-");
    this.endDateLabel.setText("-");
    this.remainingDaysLabel.setText("-");
    this.nameLabel.setText("-");
    this.ownerLabel.setText("-");

    if (this.discordCompetitionData == null) {
      validationTitle.setText("No competition selected");
      validationDescription.setText("Select a discord server and channel where the competition takes place.");
      return;
    }

    long serverId = this.serversCombo.getValue().getId();
    long channelId = this.channelsCombo.getValue().getId();

    LocalDate end = discordCompetitionData.getEdt().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    LocalDate now = DateUtil.today().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

    long remainingDays = ChronoUnit.DAYS.between(now, end);
    if (remainingDays < 0) {
      remainingDays = 0;
    }

    this.tableLabel.setText(this.discordCompetitionData.getTname());
    this.startDateLabel.setText(DateFormat.getDateInstance().format(this.discordCompetitionData.getSdt()));
    this.endDateLabel.setText(DateFormat.getDateInstance().format(this.discordCompetitionData.getEdt()));
    this.remainingDaysLabel.setText(remainingDays + " days");
    this.nameLabel.setText(this.discordCompetitionData.getName());

    PlayerRepresentation discordPlayer = client.getDiscordPlayer(serverId, Long.parseLong(this.discordCompetitionData.getOwner()));
    if (discordPlayer != null) {
      this.ownerLabel.setText(discordPlayer.getName());
    }

    CompetitionRepresentation existingEntry = client.getCompetitionByUuid(this.discordCompetitionData.getUuid());
    DiscordBotStatus discordStatus = client.getDiscordStatus();
    if (existingEntry != null && this.discordCompetitionData.getOwner().equals(String.valueOf(discordStatus.getBotId()))) {
      validationTitle.setText("Invalid competition selected");
      validationDescription.setText("You are the owner of this competition.");
      return;
    }
    //TODO check against existing


    if(this.discordCompetitionData.getEdt().before(DateUtil.today())) {
      validationTitle.setText("Invalid competition data");
      validationDescription.setText("Ups, looks like the selected competition wasn't resetted. It's already finished.");
      return;
    }

    GameRepresentation game = this.tableCombo.getValue();
    long tableSize = game.getGameFileSize();
    long competitionTableSize = this.discordCompetitionData.getFs();
    long min = competitionTableSize - 1024;
    long max = competitionTableSize + 1024;
    if (tableSize < min || tableSize > max) {
      validationTitle.setText("Invalid table version");
      validationDescription.setText("The file size of the matching table does not match the competed one.");
      return;
    }

    if(!resetCheckbox.isSelected()) {
      validationTitle.setText("Highscore reset required");
      validationDescription.setText("The reset is required in case your highscore is already higher than the others.");
      return;
    }


    this.competitionIconCombo.setDisable(false);
    this.tableCombo.setDisable(false);

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
