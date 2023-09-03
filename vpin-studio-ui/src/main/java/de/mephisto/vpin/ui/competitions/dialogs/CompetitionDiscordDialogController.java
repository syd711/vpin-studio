package de.mephisto.vpin.ui.competitions.dialogs;

import de.mephisto.vpin.commons.fx.Debouncer;
import de.mephisto.vpin.commons.fx.DialogController;
import de.mephisto.vpin.commons.fx.UIDefaults;
import de.mephisto.vpin.connectors.vps.VPS;
import de.mephisto.vpin.connectors.vps.model.VpsTable;
import de.mephisto.vpin.connectors.vps.model.VpsTableFile;
import de.mephisto.vpin.restclient.CompetitionType;
import de.mephisto.vpin.restclient.JoinMode;
import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.client.VPinStudioClient;
import de.mephisto.vpin.restclient.discord.DiscordBotStatus;
import de.mephisto.vpin.restclient.discord.DiscordChannel;
import de.mephisto.vpin.restclient.discord.DiscordCompetitionData;
import de.mephisto.vpin.restclient.discord.DiscordServer;
import de.mephisto.vpin.restclient.popper.EmulatorType;
import de.mephisto.vpin.restclient.popper.PopperScreen;
import de.mephisto.vpin.restclient.representations.CompetitionRepresentation;
import de.mephisto.vpin.restclient.representations.GameMediaItemRepresentation;
import de.mephisto.vpin.restclient.representations.GameMediaRepresentation;
import de.mephisto.vpin.restclient.representations.GameRepresentation;
import de.mephisto.vpin.restclient.util.DateUtil;
import javafx.application.Platform;
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

  private final Debouncer debouncer = new Debouncer();

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
  private ComboBox<String> startTime;

  @FXML
  private ComboBox<String> endTime;

  @FXML
  private Button saveBtn;

  @FXML
  private TextField nameField;

  @FXML
  private TextField vpsLinkField;

  @FXML
  private TextField downloadLinkField;

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

  @FXML
  private CheckBox resetCheckbox;

  @FXML
  private CheckBox strictCheckCheckbox;

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
    long guildId = client.getPreference(PreferenceNames.DISCORD_GUILD_ID).getLongValue();
    this.botStatus = client.getDiscordService().getDiscordStatus(guildId);

    competition = new CompetitionRepresentation();
    competition.setType(CompetitionType.DISCORD.name());
    competition.setName(UIDefaults.DEFAULT_COMPETITION_NAME);
    competition.setUuid(UUID.randomUUID().toString());
    competition.setOwner(String.valueOf(botStatus.getBotId()));
    competition.setHighscoreReset(true);

    Date end = Date.from(LocalDate.now().plus(7, ChronoUnit.DAYS).atStartOfDay(ZoneId.systemDefault()).toInstant());
    competition.setStartDate(Date.from(LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant()));
    competition.setEndDate(end);

    saveBtn.setDisable(true);

    nameField.setText(competition.getName());
    nameField.textProperty().addListener((observableValue, s, t1) -> debouncer.debounce("nameField", () -> {
      Platform.runLater(() -> {
        if (nameField.getText().length() > 40) {
          String sub = nameField.getText().substring(0, 40);
          nameField.setText(sub);
        }
        competition.setName(nameField.getText());
        validate();
      });
    }, 500));


    List<DiscordServer> servers = client.getDiscordService().getDiscordServers();
    ObservableList<DiscordServer> discordServers = FXCollections.observableArrayList(servers);
    serversCombo.getItems().addAll(discordServers);
    serversCombo.valueProperty().addListener((observableValue, gameRepresentation, t1) -> {
      competition.setDiscordServerId(t1.getId());

      channelsCombo.setItems(FXCollections.observableArrayList(client.getDiscordService().getDiscordChannels(t1.getId())));
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

    List<GameRepresentation> games = client.getGameService().getGamesCached();
    List<GameRepresentation> filtered = new ArrayList<>();
    for (GameRepresentation game : games) {
      if (StringUtils.isEmpty(game.getRom())) {
        continue;
      }
      if (game.getEmulator().getName().equals(EmulatorType.VISUAL_PINBALL_X)) {
        filtered.add(game);
      }
    }

    ObservableList<GameRepresentation> gameRepresentations = FXCollections.observableArrayList(filtered);
    tableCombo.getItems().addAll(gameRepresentations);
    tableCombo.valueProperty().addListener((observableValue, gameRepresentation, t1) -> {
      competition.setGameId(t1.getId());
      refreshPreview(t1, competitionIconCombo.getValue());
      refreshVPS(t1);
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

    this.resetCheckbox.selectedProperty().addListener((observable, oldValue, newValue) -> validate());
    this.strictCheckCheckbox.setSelected(this.competition.getJoinMode() != null && this.competition.getJoinMode().equals(JoinMode.STRICT.name()));
    this.strictCheckCheckbox.selectedProperty().addListener((observable, oldValue, newValue) -> {
      this.competition.setJoinMode(newValue ? JoinMode.STRICT.name() : JoinMode.ROM_ONLY.name());
      validate();
    });

    validate();
  }

  private void refreshPreview(GameRepresentation game, String badge) {
    iconPreview.setImage(null);
    badgePreview.setImage(null);

    if (game != null) {
      GameMediaRepresentation gameMedia = game.getGameMedia();
      GameMediaItemRepresentation mediaItem = gameMedia.getDefaultMediaItem(PopperScreen.Wheel);
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

    if (competition.getDiscordServerId() == 0) {
      validationTitle.setText("No discord server selected.");
      validationDescription.setText("Select a discord server where the competition takes place.");
      return;
    }

    if (this.botStatus == null || this.botStatus.getServerId() != competition.getDiscordServerId()) {
      this.botStatus = client.getDiscordService().getDiscordStatus(competition.getDiscordServerId());
    }

    if (botStatus == null || StringUtils.isEmpty(botStatus.getBotInitials())) {
      validationTitle.setText("Invalid BOT nickname.");
      validationDescription.setText("To submit highscores, your bot must have the name pattern \"<NAME> | [<INITIALS>]\" on the selected server.");
      return;
    }

    if (competition.getDiscordChannelId() == 0) {
      validationTitle.setText("No discord channel selected.");
      validationDescription.setText("Select a discord channel for competition updates.");
      return;
    }

    //check Discord permissions
    if (!client.getCompetitionService().hasManagePermissions(competition.getDiscordServerId(), competition.getDiscordChannelId())) {
      validationTitle.setText("Insufficient Permissions");
      validationDescription.setText("Your Discord bot has insufficient permissions to join a competition. Please check the documentation for details.");
      return;
    }

    if (startDate == null || endDate == null || startDate.getTime() >= endDate.getTime()) {
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

    Date startSelection = Date.from(startDatePicker.getValue().atStartOfDay(ZoneId.systemDefault()).toInstant());
    Date endSelection = Date.from(endDatePicker.getValue().atStartOfDay(ZoneId.systemDefault()).toInstant());


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


    //check the active competition stored for the selected channel against the date selection
    DiscordCompetitionData discordCompetitionData = client.getDiscordService().getDiscordCompetitionData(competition.getDiscordServerId(), competition.getDiscordChannelId());
    if (discordCompetitionData != null) {
      //separate call since the data is still there, even if the competition is finished
      boolean active = client.getDiscordService().isCompetitionActive(competition.getDiscordServerId(), competition.getDiscordChannelId(), discordCompetitionData.getUuid());
      if (active) {
        if (discordCompetitionData.isOverlappingWith(startSelection, endSelection)) {
          validationTitle.setText("Active competition found.");
          validationDescription.setText("The selected channel is already running the competition '" + discordCompetitionData.getName() + "' for this time span.");
          return;
        }
      }
    }

    //check highscore settings //TODO
//    ScoreSummaryRepresentation summary = client.getGameScores(competition.getGameId());
//    HighscoreMetadataRepresentation metadata = summary.getMetadata();
//    if (!StringUtils.isEmpty(metadata.getStatus())) {
//      validationTitle.setText("Highscore issues");
//      validationDescription.setText(metadata.getStatus() + " Select a table with a valid highscore record.");
//      return;
//    }

    if (!resetCheckbox.isDisable() && !resetCheckbox.isSelected()) {
      validationTitle.setText("Highscore reset required");
      validationDescription.setText("The reset is required in case your highscore is already higher than the others.");
      return;
    }

    GameRepresentation game = this.tableCombo.getValue();
    if(StringUtils.isEmpty(game.getExtTableId()) || StringUtils.isEmpty(game.getExtTableVersionId())) {
      validationTitle.setText("No VPS data set for selected table");
      validationDescription.setText("The data is not required. But this way competitions know what table version is played.");
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

  public void setCompetition(List<CompetitionRepresentation> all, CompetitionRepresentation selectedCompetition) {
    this.allCompetitions = all;

    if (selectedCompetition != null) {
      this.resetCheckbox.setDisable(selectedCompetition.getId() != null);
      this.resetCheckbox.setSelected(selectedCompetition.getId() != null);

      GameRepresentation game = client.getGame(selectedCompetition.getGameId());
      DiscordServer discordServer = client.getDiscordServer(selectedCompetition.getDiscordServerId());
      List<DiscordChannel> serverChannels = client.getDiscordService().getDiscordChannels(discordServer.getId());

      String botId = String.valueOf(botStatus.getBotId());
      boolean isOwner = selectedCompetition.getOwner().equals(botId);
      boolean editable = isOwner && !selectedCompetition.isStarted();

      channelsCombo.setDisable(!editable);
      serversCombo.setDisable(!editable);
      competitionIconCombo.setDisable(!editable);

      this.nameField.setText(selectedCompetition.getName());
      this.nameField.setDisable(!editable);

      this.startDatePicker.setValue(selectedCompetition.getStartDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
      this.startDatePicker.setDisable(!editable);
      this.startTime.setValue(DateUtil.formatTimeString(selectedCompetition.getStartDate()));
      this.startTime.setDisable(!editable);

      this.endDatePicker.setValue(selectedCompetition.getEndDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
      this.endDatePicker.setDisable(!editable);
      this.endTime.setValue(DateUtil.formatTimeString(selectedCompetition.getEndDate()));
      this.endTime.setDisable(!editable);

      this.strictCheckCheckbox.setDisable(!editable);

      this.tableCombo.setValue(game);
      this.tableCombo.setDisable(!editable);

      refreshVPS(game);

      this.channelsCombo.setItems(FXCollections.observableList(serverChannels));
      this.serversCombo.setValue(discordServer);

      //the id is null when we want to duplicate an existing competition
      if (selectedCompetition.getId() != null) {
        Optional<DiscordChannel> first = serverChannels.stream().filter(channel -> channel.getId() == selectedCompetition.getId()).findFirst();
        first.ifPresent(discordChannel -> this.channelsCombo.setValue(discordChannel));
      }

      ObservableList<DiscordChannel> items = this.channelsCombo.getItems();
      for (DiscordChannel item : items) {
        if (item.getId() == selectedCompetition.getDiscordChannelId()) {
          this.channelsCombo.setValue(item);
          break;
        }
      }

      this.competitionIconCombo.setValue(selectedCompetition.getBadge());
      String badge = selectedCompetition.getBadge();

      this.competition = selectedCompetition;
      refreshPreview(game, badge);
    }
  }

  private void refreshVPS(GameRepresentation game) {
    if (!StringUtils.isEmpty(game.getExtTableId())) {
      VpsTable vpsTable = VPS.getInstance().getTableById(game.getExtTableId());
      vpsLinkField.setText("https://virtual-pinball-spreadsheet.web.app/game/" + game.getExtTableId() + "/");

      if (!StringUtils.isEmpty(game.getExtTableVersionId())) {
        List<VpsTableFile> tableFiles = vpsTable.getTableFiles();
        Optional<VpsTableFile> tableVersion = tableFiles.stream().filter(t -> t.getId().equals(game.getExtTableVersionId())).findFirst();
        if (tableVersion.isPresent() && !tableVersion.get().getUrls().isEmpty()) {
          downloadLinkField.setText(tableVersion.get().getUrls().get(0).getUrl());
        }
      }
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
}
