package de.mephisto.vpin.ui.competitions;

import de.mephisto.vpin.commons.EmulatorType;
import de.mephisto.vpin.commons.fx.DialogController;
import de.mephisto.vpin.commons.fx.UIDefaults;
import de.mephisto.vpin.restclient.CompetitionType;
import de.mephisto.vpin.restclient.PopperScreen;
import de.mephisto.vpin.restclient.client.VPinStudioClient;
import de.mephisto.vpin.restclient.discord.DiscordChannel;
import de.mephisto.vpin.restclient.representations.CompetitionRepresentation;
import de.mephisto.vpin.restclient.representations.GameMediaItemRepresentation;
import de.mephisto.vpin.restclient.representations.GameMediaRepresentation;
import de.mephisto.vpin.restclient.representations.GameRepresentation;
import de.mephisto.vpin.restclient.util.DateUtil;
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

public class CompetitionOfflineDialogController implements Initializable, DialogController {
  private final static Logger LOG = LoggerFactory.getLogger(CompetitionOfflineDialogController.class);

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
  private ComboBox<String> startTime;

  @FXML
  private ComboBox<String> endTime;

  @FXML
  private Pane validationContainer;

  @FXML
  private Label validationTitle;

  @FXML
  private Label validationDescription;

  private CompetitionRepresentation competition;

  private List<DiscordChannel> discordChannels;

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
    competition.setType(CompetitionType.OFFLINE.name());
    competition.setName("My next competition");
    competition.setUuid(UUID.randomUUID().toString());

    Date end = Date.from(LocalDate.now().plus(7, ChronoUnit.DAYS).atStartOfDay(ZoneId.systemDefault()).toInstant());
    competition.setStartDate(DateUtil.today());
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


    startDatePicker.setValue(LocalDate.now());
    startDatePicker.valueProperty().addListener((observableValue, localDate, t1) -> {
      Date date = DateUtil.formatDate(startDatePicker.getValue(), startTime.getValue());
      competition.setStartDate(date);
      validate();
    });
    startTime.setItems(FXCollections.observableList(DateUtil.TIMES));
    startTime.setValue("00:00");
    startTime.valueProperty().addListener((observable, oldValue, newValue) -> {
      Date date = DateUtil.formatDate(startDatePicker.getValue(), startTime.getValue());
      competition.setStartDate(date);
      validate();
    });

    endDatePicker.setValue(LocalDate.now().plus(7, ChronoUnit.DAYS));
    endDatePicker.valueProperty().addListener((observableValue, localDate, t1) -> {
      Date date = DateUtil.formatDate(endDatePicker.getValue(), endTime.getValue());
      competition.setEndDate(date);
      validate();
    });
    endTime.setItems(FXCollections.observableList(DateUtil.TIMES));
    endTime.setValue("00:00");
    endTime.valueProperty().addListener((observable, oldValue, newValue) -> {
      Date date = DateUtil.formatDate(endDatePicker.getValue(), endTime.getValue());
      competition.setEndDate(date);
      validate();
    });

    List<GameRepresentation> games = client.getGameService().getGamesWithScores();
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

    List<DiscordChannel> channels = new ArrayList<>(getDiscordChannels());
    channels.add(0, null);
    ObservableList<DiscordChannel> discordChannels = FXCollections.observableArrayList(channels);
    channelsCombo.getItems().addAll(discordChannels);
    channelsCombo.valueProperty().addListener((observableValue, gameRepresentation, t1) -> {
      if (t1 != null) {
        competition.setDiscordChannelId(t1.getId());
      }
      else {
        competition.setDiscordChannelId(0);
      }
      validate();
    });

    ArrayList<String> badges = new ArrayList<>(client.getCompetitionService().getCompetitionBadges());
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
          Image badgeIcon = new Image(client.getCompetitionService().getCompetitionBadge(badge));
          badgePreview.setImage(badgeIcon);
        }
      }
    }
  }

  private void validate() {
    validationContainer.setVisible(true);
    this.saveBtn.setDisable(true);

    Date startDate = competition.getStartDate();
    Date endDate = competition.getEndDate();
    this.durationLabel.setText(DateUtil.formatDuration(startDate, endDate));

    if (StringUtils.isEmpty(competition.getName())) {
      validationTitle.setText("No competition name set.");
      validationDescription.setText("Define a meaningful competition name.");
      return;
    }

    if (competition.getGameId() <= 0) {
      validationTitle.setText("No table selected.");
      validationDescription.setText("Select a table for the competition.");
      return;
    }

    if (startDate == null || endDate == null || startDate.getTime() >= endDate.getTime()) {
      validationTitle.setText("Invalid start/end date set.");
      validationDescription.setText("Define a valid start and end date.");
      return;
    }

//    GameRepresentation game = this.tableCombo.getValue();
//    Date startSelection = Date.from(startDatePicker.getValue().atStartOfDay(ZoneId.systemDefault()).toInstant());
//    Date endSelection = Date.from(endDatePicker.getValue().atStartOfDay(ZoneId.systemDefault()).toInstant());
//    for (CompetitionRepresentation existingCompetition : this.allCompetitions) {
//      if (competition.isFinished()) {
//        continue;
//      }
//
//      GameRepresentation cGame = client.getGame(competition.getGameId());
//      if (existingCompetition.isOverlappingWith(startSelection, endSelection) && String.valueOf(cGame.getRom()).equals(game.getRom()) ) {
//        validationTitle.setText("Invalid table selected");
//        validationDescription.setText("This table is already used for another competition in the selected time span.");
//        return;
//      }
//    }
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
    if (c != null) {
      this.competition = c;
      GameRepresentation game = client.getGame(c.getGameId());

      nameField.setText(this.competition.getName());
      this.startDatePicker.setValue(this.competition.getStartDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
      this.startDatePicker.setDisable(this.competition.isFinished());
      this.startTime.setValue(DateUtil.formatTimeString(this.competition.getStartDate()));

      this.endDatePicker.setValue(this.competition.getEndDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
      this.endDatePicker.setDisable(this.competition.isFinished());
      this.endTime.setValue(DateUtil.formatTimeString(this.competition.getEndDate()));

      this.tableCombo.setValue(game);
      this.tableCombo.setDisable((this.competition.getId() != null && !this.competition.isPlanned()) || this.competition.isFinished());


      Optional<DiscordChannel> channelOpt = getDiscordChannels().stream().filter(channel -> channel.getId() == c.getDiscordChannelId()).findFirst();
      channelOpt.ifPresent(discordChannel -> this.channelsCombo.setValue(discordChannel));
      this.channelsCombo.setDisable(this.competition.isFinished());

      this.competitionIconCombo.setValue(c.getBadge());
      this.competitionIconCombo.setDisable(this.competition.isFinished());
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
        Image image = new Image(client.getCompetitionService().getCompetitionBadge(item));
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

  private List<DiscordChannel> getDiscordChannels() {
    if (this.discordChannels == null) {
      this.discordChannels = client.getDiscordService().getDiscordChannels();
    }
    return this.discordChannels;
  }
}
