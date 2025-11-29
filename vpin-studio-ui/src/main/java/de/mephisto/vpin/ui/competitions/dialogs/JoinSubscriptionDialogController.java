package de.mephisto.vpin.ui.competitions.dialogs;

import de.mephisto.vpin.commons.fx.DialogController;
import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.competitions.CompetitionRepresentation;
import de.mephisto.vpin.restclient.competitions.CompetitionType;
import de.mephisto.vpin.restclient.competitions.SubscriptionInfo;
import de.mephisto.vpin.restclient.discord.DiscordBotStatus;
import de.mephisto.vpin.restclient.discord.DiscordChannel;
import de.mephisto.vpin.restclient.discord.DiscordServer;
import de.mephisto.vpin.restclient.highscores.NVRamList;
import de.mephisto.vpin.restclient.games.GameRepresentation;
import de.mephisto.vpin.ui.competitions.CompetitionsDialogHelper;
import edu.umd.cs.findbugs.annotations.Nullable;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

import static de.mephisto.vpin.ui.Studio.client;

public class JoinSubscriptionDialogController implements Initializable, DialogController {
  private final static Logger LOG = LoggerFactory.getLogger(JoinSubscriptionDialogController.class);

  @FXML
  private ComboBox<GameRepresentation> tableCombo;

  @FXML
  private ComboBox<DiscordServer> serverCombo;

  @FXML
  private ComboBox<DiscordChannel> channelCombo;

  @FXML
  private Button saveBtn;

  @FXML
  private Pane validationContainer;

  @FXML
  private Label validationTitle;

  @FXML
  private Label validationDescription;

  @FXML
  private CheckBox resetCheckbox;

  @FXML
  private Label nvramLabel;

  private NVRamList nvRamList;

  private CompetitionRepresentation competition;
  private List<CompetitionRepresentation> allCompetitions;

  private DiscordBotStatus botStatus = null;

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
    this.nvRamList = client.getNvRamsService().getResettedNVRams();

    long guildId = client.getPreferenceService().getPreference(PreferenceNames.DISCORD_GUILD_ID).getLongValue();
    this.botStatus = client.getDiscordService().getDiscordStatus(guildId);

    competition = new CompetitionRepresentation();
    competition.setType(CompetitionType.SUBSCRIPTION.name());
    competition.setName("");
    competition.setDiscordServerId(this.botStatus.getServerId());

    saveBtn.setDisable(true);
    channelCombo.setDisable(true);
    tableCombo.setDisable(true);

    List<DiscordServer> servers = client.getDiscordService().getDiscordServers();
    ObservableList<DiscordServer> discordServers = FXCollections.observableArrayList(servers);
    serverCombo.setItems(FXCollections.observableList(discordServers));
    serverCombo.valueProperty().addListener((observable, oldValue, newValue) -> {
      channelCombo.setDisable(newValue == null);
      if (newValue != null) {
        competition.setDiscordServerId(newValue.getId());

        List<DiscordChannel> discordChannels = client.getDiscordService().getDiscordChannels(newValue.getId());
        List<DiscordChannel> filtered = discordChannels.stream().filter(channel -> channel.getName().contains("§")).collect(Collectors.toList());
        channelCombo.setItems(FXCollections.observableArrayList(filtered));
      }
      else {
        competition.setDiscordServerId(0);
        channelCombo.setItems(FXCollections.observableArrayList(Collections.emptyList()));
      }
      validate();
    });

    List<DiscordChannel> discordChannels = FXCollections.observableArrayList(new ArrayList<>());
    channelCombo.getItems().addAll(discordChannels);
    channelCombo.valueProperty().addListener((observableValue, gameRepresentation, t1) -> {
      tableCombo.setDisable(t1 == null);
      if (t1 != null) {
        SubscriptionInfo subscriptionInfo = client.getDiscordService().getSubscriptionInfo(competition.getDiscordServerId(), t1.getId());
        if (subscriptionInfo == null) {
          tableCombo.setDisable(true);
          validationContainer.setVisible(true);
          this.saveBtn.setDisable(true);

          validationTitle.setText("Not a subscription channel");
          validationDescription.setText("No subscription data was found in the pinned messages of this channel.");
          return;
        }

        competition.setDiscordChannelId(t1.getId());
        competition.setOwner(String.valueOf(subscriptionInfo.getOwnerId()));
        competition.setUuid(String.valueOf(subscriptionInfo.getUuid()));

        String rom = t1.getName().substring(t1.getName().lastIndexOf("§") + 1);
        refreshTables(rom);
      }
      else {
        competition.setDiscordChannelId(0);
      }
      validate();
    });


    tableCombo.valueProperty().addListener((observableValue, gameRepresentation, t1) -> {
      if (t1 != null) {
        competition.setGameId(t1.getId());
        competition.setRom(t1.getRom());
        competition.setName(t1.getGameDisplayName());
      }
      validate();
    });

    resetCheckbox.selectedProperty().addListener(new ChangeListener<Boolean>() {
      @Override
      public void changed(ObservableValue<? extends Boolean> observableValue, Boolean aBoolean, Boolean t1) {
        competition.setHighscoreReset(t1);
      }
    });

    validate();
  }

  private void validate() {
    validationContainer.setVisible(true);
    this.saveBtn.setDisable(true);

    if (this.competition.getDiscordServerId() == 0) {
      validationTitle.setText("No Discord server selected.");
      validationDescription.setText("Select a Discord server.");
      return;
    }

    if (this.channelCombo.getItems().isEmpty()) {
      validationTitle.setText("No subscriptions found.");
      validationDescription.setText("No table subscriptions have been found for the selected server.");
      return;
    }

    if (this.competition.getDiscordChannelId() == 0) {
      validationTitle.setText("No Discord channel selected.");
      validationDescription.setText("Select a Discord text channel with an active subscription.");
      return;
    }

    //check table selection
    if (this.tableCombo.getValue() == null) {
      validationTitle.setText("No table selected.");
      validationDescription.setText("Select a table for the subscription.");
      return;
    }

    GameRepresentation game = this.tableCombo.getValue();
    CompetitionsDialogHelper.refreshResetStatusIcon(game, nvRamList, nvramLabel);

    if (this.botStatus == null || this.botStatus.getServerId() != competition.getDiscordServerId()) {
      this.botStatus = client.getDiscordService().getDiscordStatus(competition.getDiscordServerId());
    }

    //check Discord permissions
    if (!client.getCompetitionService().hasManagePermissions(competition.getDiscordServerId())) {
      validationTitle.setText("Insufficient Permissions");
      validationDescription.setText("Your Discord bot has insufficient permissions to create posts for that subscription. Please check the documentation for details.");
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

  public void setCompetition(List<CompetitionRepresentation> all) {
    this.allCompetitions = all;
    refreshTables(null);
  }

  private void refreshTables(@Nullable String filterRom) {
    List<GameRepresentation> games = client.getGameService().getVpxGamesCached();
    List<GameRepresentation> filtered = new ArrayList<>();
    for (GameRepresentation game : games) {
      if (StringUtils.isEmpty(game.getRom())) {
        continue;
      }

      if (!game.getRom().equalsIgnoreCase(filterRom)) {
        continue;
      }

      //filter subscription for the same table on the same server!
      if (allCompetitions.stream().anyMatch(c -> game.getRom().equalsIgnoreCase(c.getRom()) && c.getDiscordServerId() == competition.getDiscordServerId())) {
        continue;
      }
      filtered.add(game);
    }

    List<GameRepresentation> gameRepresentations = FXCollections.observableArrayList(filtered);
    tableCombo.getItems().setAll(gameRepresentations);
  }
}
