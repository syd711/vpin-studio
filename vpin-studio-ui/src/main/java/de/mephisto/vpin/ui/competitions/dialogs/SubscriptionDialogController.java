package de.mephisto.vpin.ui.competitions.dialogs;

import de.mephisto.vpin.commons.EmulatorType;
import de.mephisto.vpin.commons.fx.Debouncer;
import de.mephisto.vpin.commons.fx.DialogController;
import de.mephisto.vpin.commons.fx.UIDefaults;
import de.mephisto.vpin.restclient.CompetitionType;
import de.mephisto.vpin.restclient.JoinMode;
import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.client.VPinStudioClient;
import de.mephisto.vpin.restclient.discord.DiscordBotStatus;
import de.mephisto.vpin.restclient.discord.DiscordChannel;
import de.mephisto.vpin.restclient.discord.DiscordCompetitionData;
import de.mephisto.vpin.restclient.discord.DiscordServer;
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

public class SubscriptionDialogController implements Initializable, DialogController {
  private final static Logger LOG = LoggerFactory.getLogger(SubscriptionDialogController.class);

  private final Debouncer debouncer = new Debouncer();

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
  private Pane validationContainer;

  @FXML
  private Label validationTitle;

  @FXML
  private Label validationDescription;

  @FXML
  private CheckBox resetCheckbox;

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
      validate();
    });

    validate();
  }

  private void validate() {
    validationContainer.setVisible(true);
    this.saveBtn.setDisable(true);

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
      validationDescription.setText("Please set a valid nickname for your BOT on the selected server.");
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

    //check table selection
    if (this.tableCombo.getValue() == null) {
      validationTitle.setText("No table selected.");
      validationDescription.setText("Select a table for the competition.");
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

      this.nameField.setText(selectedCompetition.getName());
      this.nameField.setDisable(!editable);


            this.tableCombo.setValue(game);
      this.tableCombo.setDisable(!editable);


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
      this.competition = selectedCompetition;
    }
  }
}
