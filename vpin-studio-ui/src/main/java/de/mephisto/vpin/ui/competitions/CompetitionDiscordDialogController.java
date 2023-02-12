package de.mephisto.vpin.ui.competitions;

import de.mephisto.vpin.commons.EmulatorType;
import de.mephisto.vpin.commons.fx.DialogController;
import de.mephisto.vpin.restclient.CompetitionType;
import de.mephisto.vpin.restclient.PopperScreen;
import de.mephisto.vpin.restclient.VPinStudioClient;
import de.mephisto.vpin.restclient.discord.DiscordBotStatus;
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
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.net.URL;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.*;

import static de.mephisto.vpin.ui.Studio.client;

public class CompetitionDiscordDialogController implements Initializable, DialogController {
  private final static Logger LOG = LoggerFactory.getLogger(CompetitionDiscordDialogController.class);

  @FXML
  private ImageView iconPreview;

  @FXML
  private ImageView badgePreview;

  @FXML
  private ComboBox<String> competitionIconCombo;

  @FXML
  private ComboBox<GameRepresentation> tableCombo;

  @FXML
  private ComboBox<DiscordChannel> channelsCombo;

  @FXML
  private ComboBox<DiscordServer> serversCombo;

  @FXML
  private Button saveBtn;

  @FXML
  private TextField nameField;

  @FXML
  private Label durationLabel;

  @FXML
  private DatePicker startDatePicker;

  @FXML
  private DatePicker endDatePicker;

  @FXML
  private Pane validationContainer;

  @FXML
  private Label validationTitle;

  @FXML
  private Label validationDescription;

  private CompetitionRepresentation competition;

  private DiscordBotStatus botStatus = null;
  private List<CompetitionRepresentation> allCompetitions;

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
    this.botStatus = client.getDiscordStatus();

    competition = new CompetitionRepresentation();
    competition.setType(CompetitionType.DISCORD.name());
    competition.setName("My next competition");
    competition.setUuid(UUID.randomUUID().toString());
    competition.setOwner(String.valueOf(botStatus.getBotId()));

    Date end = Date.from(LocalDate.now().plus(7, ChronoUnit.DAYS).atStartOfDay(ZoneId.systemDefault()).toInstant());
    competition.setStartDate(Date.from(LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant()));
    competition.setEndDate(end);

    saveBtn.setDisable(true);

    nameField.setText(competition.getName());
    nameField.textProperty().addListener((observableValue, s, t1) -> {
      if (nameField.getText().length() > 40) {
        String sub = nameField.getText().substring(0, 40);
        nameField.setText(sub);
      }
      competition.setName(nameField.getText());
      validate();
    });


    List<DiscordServer> servers = client.getDiscordServers();
    ObservableList<DiscordServer> discordServers = FXCollections.observableArrayList(servers);
    serversCombo.getItems().addAll(discordServers);
    serversCombo.valueProperty().addListener((observableValue, gameRepresentation, t1) -> {
      competition.setDiscordServerId(t1.getId());

      channelsCombo.setItems(FXCollections.observableArrayList(client.getDiscordChannels(t1.getId())));
      validate();
    });


    ObservableList<DiscordChannel> discordChannels = FXCollections.observableArrayList(new ArrayList<>());
    channelsCombo.getItems().addAll(discordChannels);
    channelsCombo.valueProperty().addListener((observableValue, gameRepresentation, t1) -> {
      competition.setDiscordChannelId(t1.getId());
      validate();
    });


    startDatePicker.setValue(LocalDate.now());
    startDatePicker.valueProperty().addListener((observableValue, localDate, t1) -> {
      if (t1 != null) {
        Date date = Date.from(t1.atStartOfDay(ZoneId.systemDefault()).toInstant());
        competition.setStartDate(date);
      }
      else {
        competition.setStartDate(null);
      }
      validate();
    });

    endDatePicker.setValue(LocalDate.now().plus(7, ChronoUnit.DAYS));
    endDatePicker.valueProperty().addListener((observableValue, localDate, t1) -> {
      if (t1 != null) {
        Date date = Date.from(t1.atStartOfDay(ZoneId.systemDefault()).toInstant());
        competition.setEndDate(date);
      }
      else {
        competition.setEndDate(null);
      }

      validate();
    });

    List<GameRepresentation> games = client.getGames();
    List<GameRepresentation> filtered = new ArrayList<>();
    for (GameRepresentation game : games) {
      if (game.getEmulator().getName().equals(EmulatorType.VISUAL_PINBALL_X)) {
        filtered.add(game);
      }
    }

    ObservableList<GameRepresentation> gameRepresentations = FXCollections.observableArrayList(filtered);
    tableCombo.getItems().addAll(gameRepresentations);
    tableCombo.valueProperty().addListener((observableValue, gameRepresentation, t1) -> {
      competition.setGameId(t1.getId());
      refreshPreview(t1, competitionIconCombo.getValue());
      validate();
    });
    ArrayList<String> badges = new ArrayList<>(client.getCompetitionBadges());
    badges.add(0, null);
    ObservableList<String> imageList = FXCollections.observableList(badges);
    competitionIconCombo.setItems(imageList);
    competitionIconCombo.setCellFactory(c -> new CompetitionImageListCell(client));
    competitionIconCombo.valueProperty().addListener((observableValue, s, t1) -> {
      competition.setBadge(t1);
      refreshPreview(tableCombo.getValue(), t1);
      validate();
    });

    validate();
  }

  private void refreshPreview(GameRepresentation game, String badge) {
    iconPreview.setImage(null);
    badgePreview.setImage(null);

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
      }
    }
  }

  private void validate() {
    validationContainer.setVisible(true);
    this.saveBtn.setDisable(true);

    LocalDate start = startDatePicker.getValue();
    LocalDate value = endDatePicker.getValue();

    long diff = ChronoUnit.DAYS.between(start, value);
    this.durationLabel.setText(diff + " days");

    if (StringUtils.isEmpty(competition.getName())) {
      validationTitle.setText("No competition name set.");
      validationDescription.setText("Define a meaningful competition name.");
      return;
    }

    if (competition.getDiscordServerId() == 0) {
      validationTitle.setText("No discord server selected.");
      validationDescription.setText("Select a discord server where the competition takes place.");
      return;
    }

    if (competition.getDiscordChannelId() == 0) {
      validationTitle.setText("No discord channel selected.");
      validationDescription.setText("Select a discord channel for competition updates.");
      return;
    }

    if (competition.getStartDate() == null || competition.getEndDate() == null || competition.getStartDate().getTime() > competition.getEndDate().getTime()) {
      validationTitle.setText("Invalid start/end date set.");
      validationDescription.setText("Define a valid start and end date.");
      return;
    }

    //check table selection
    if (this.tableCombo.getValue() == null) {
      validationTitle.setText("No table selected.");
      validationDescription.setText("Select a table for the competition.");
      return;
    }

    GameRepresentation game = this.tableCombo.getValue();

    Date startSelection = Date.from(startDatePicker.getValue().atStartOfDay(ZoneId.systemDefault()).toInstant());
    Date endSelection = Date.from(endDatePicker.getValue().atStartOfDay(ZoneId.systemDefault()).toInstant());

    //check the active competition stored for the selected channel against the date selection
    DiscordCompetitionData discordCompetitionData = client.getDiscordCompetitionData(competition.getDiscordServerId(), competition.getDiscordChannelId());
    if (discordCompetitionData != null) {
      if (!this.competition.getUuid().equals(discordCompetitionData.getUuid())) {
        if (discordCompetitionData.isOverlappingWith(startSelection, endSelection)) {
          validationTitle.setText("Active competition found.");
          validationDescription.setText("The selected channel is already running the competition '" + discordCompetitionData.getName() + "' for this time span.");
          return;
        }
      }
    }

    //check if another active competition on this channel is active during the selected time span
    for (CompetitionRepresentation existingCompetition : allCompetitions) {
      if (existingCompetition.isFinished()) {
        continue;
      }

      if (!this.competition.getUuid().equals(existingCompetition.getUuid())
          && existingCompetition.isOverlappingWith(startSelection, endSelection)
          && existingCompetition.getDiscordServerId() == this.competition.getDiscordServerId()
          && existingCompetition.getDiscordChannelId() == this.competition.getDiscordChannelId()) {
        validationTitle.setText("Overlapping competition found.");
        validationDescription.setText("The competition " + existingCompetition.getName() + "overlaps for the the given Discord channel for this time span.");
        return;
      }

//      GameRepresentation cGame = client.getGame(competition.getGameId());
//      if (existingCompetition.isOverlappingWith(startSelection, endSelection) && String.valueOf(cGame.getRom()).equals(game.getRom()) ) {
//        validationTitle.setText("Invalid table selected");
//        validationDescription.setText("This table is already used for another competition in the selected time span.");
//        return;
//      }
    }

    //check highscore settings
    ScoreSummaryRepresentation summary = client.getGameScores(competition.getGameId());
    HighscoreMetadataRepresentation metadata = summary.getMetadata();
    if (!StringUtils.isEmpty(metadata.getStatus())) {
      validationTitle.setText("Highscore issues");
      validationDescription.setText(metadata.getStatus() + " Select a table with a valid highscore record.");
      return;
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

  public void setCompetition(List<CompetitionRepresentation> all, CompetitionRepresentation c) {
    this.allCompetitions = all;

    if (c != null) {
      this.competition = c;
      GameRepresentation game = client.getGame(c.getGameId());
      DiscordServer discordServer = client.getDiscordServer(competition.getDiscordServerId());
      String botId = String.valueOf(botStatus.getBotId());
      boolean isOwner = c.getOwner().equals(botId);

      channelsCombo.setDisable(c.getId() != null);
      serversCombo.setDisable(c.getId() != null);

      this.nameField.setText(this.competition.getName());
      this.nameField.setDisable(!isOwner);

      this.startDatePicker.setValue(this.competition.getStartDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
      this.startDatePicker.setDisable(!isOwner);
      this.endDatePicker.setValue(this.competition.getEndDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
      this.endDatePicker.setDisable(!isOwner);

      this.tableCombo.setValue(game);
      this.tableCombo.setDisable(c.getId() != null);

      this.channelsCombo.setItems(FXCollections.observableList(client.getDiscordChannels(discordServer.getId())));
      this.serversCombo.setValue(discordServer);

      List<DiscordChannel> discordChannels = client.getDiscordChannels(discordServer.getId());
      this.channelsCombo.setItems(FXCollections.observableArrayList(discordChannels));

      //the id is null when we want to duplicate an existing competition
      if (competition.getId() != null) {
        Optional<DiscordChannel> first = discordChannels.stream().filter(channel -> channel.getId() != competition.getId()).findFirst();
        first.ifPresent(discordChannel -> this.channelsCombo.setValue(discordChannel));
      }

      ObservableList<DiscordChannel> items = this.channelsCombo.getItems();
      for (DiscordChannel item : items) {
        if (item.getId() == c.getDiscordChannelId()) {
          this.channelsCombo.setValue(item);
          break;
        }
      }

      this.competitionIconCombo.setValue(c.getBadge());
      String badge = c.getBadge();
      refreshPreview(game, badge);
    }
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
