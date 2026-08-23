package de.mephisto.vpin.ui.competitions.dialogs;

import de.mephisto.vpin.commons.fx.Debouncer;
import de.mephisto.vpin.commons.fx.DialogController;
import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.competitions.CompetitionRepresentation;
import de.mephisto.vpin.restclient.competitions.CompetitionType;
import de.mephisto.vpin.restclient.competitions.JoinMode;
import de.mephisto.vpin.restclient.competitions.SubscriptionInfo;
import de.mephisto.vpin.restclient.discord.DiscordBotStatus;
import de.mephisto.vpin.restclient.games.GameRepresentation;
import de.mephisto.vpin.restclient.highscores.NVRamList;
import de.mephisto.vpin.ui.competitions.CompetitionsDialogHelper;
import de.mephisto.vpin.commons.utils.i18n.Messages;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.UUID;

import static de.mephisto.vpin.ui.Studio.client;

public class SubscriptionDialogController implements Initializable, DialogController {
  private final static Logger LOG = LoggerFactory.getLogger(SubscriptionDialogController.class);

  private final Debouncer debouncer = new Debouncer();

  @FXML
  private ComboBox<GameRepresentation> tableCombo;

  @FXML
  private Button saveBtn;

  @FXML
  private TextField nameField;

  @FXML
  private TextField suffixField;

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
    competition.setJoinMode(JoinMode.ROM_ONLY.name());
    competition.setScoreLimit(SubscriptionInfo.DEFAULT_SCORE_LIMIT);
    competition.setUuid(UUID.randomUUID().toString());
    competition.setOwner(String.valueOf(botStatus.getBotId()));
    competition.setDiscordServerId(this.botStatus.getServerId());

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


    tableCombo.valueProperty().addListener((observableValue, gameRepresentation, t1) -> {
      if (t1 != null) {
        competition.setGameId(t1.getId());
        competition.setRom(t1.getRom());
        String name = t1.getGameDisplayName();
        String rom = t1.getRom();
        nameField.setText(name);
        suffixField.setText(rom);
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

    //check table selection
    if (this.tableCombo.getValue() == null) {
      validationTitle.setText(Messages.get("competitions.discord_competition_edit.no_table_selected"));
      validationDescription.setText(Messages.get("competitions.subscription_join.select_a_table_for_the_subscription"));
      return;
    }

    GameRepresentation game = this.tableCombo.getValue();
    CompetitionsDialogHelper.refreshResetStatusIcon(game, nvRamList, nvramLabel);

    if (StringUtils.isEmpty(competition.getName())) {
      validationTitle.setText(Messages.get("competitions.subscription_add.no_channel_name_set"));
      validationDescription.setText(Messages.get("competitions.subscription_add.the_channel_name_must_be_set_and_should_match"));
      return;
    }

    if (this.botStatus == null || this.botStatus.getServerId() != competition.getDiscordServerId()) {
      this.botStatus = client.getDiscordService().getDiscordStatus(competition.getDiscordServerId());
    }

    if (this.botStatus.getServerId() == 0) {
      validationTitle.setText(Messages.get("competitions.subscription_join.no_discord_server_selected"));
      validationDescription.setText(Messages.get("competitions.subscription_add.select_a_default_server_in_the_bot_preferences"));
      return;
    }

    if (this.botStatus.getCategoryId() == 0) {
      validationTitle.setText(Messages.get("competitions.subscription_add.no_category_selected"));
      validationDescription.setText(Messages.get("competitions.subscription_add.select_a_category_used_for_subscriptions_in_the_bot_preferences"));
      return;
    }

    //check Discord permissions
    if (!client.getCompetitionService().hasChannelManagePermissions(competition.getDiscordServerId())) {
      validationTitle.setText(Messages.get("competitions.discord_competition_edit.insufficient_permissions"));
      validationDescription.setText(Messages.get("competitions.subscription_add.your_discord_bot_has_insufficient_permissions_to_create_a_subscription"));
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
    List<GameRepresentation> games = client.getGameService().getVpxGamesCached();
    List<GameRepresentation> filtered = new ArrayList<>();
    for (GameRepresentation game : games) {
      if (StringUtils.isEmpty(game.getRom())) {
        continue;
      }

      if (all.stream().anyMatch(c -> game.getRom().equalsIgnoreCase(c.getRom()))) {
        continue;
      }

      filtered.add(game);
    }

    ObservableList<GameRepresentation> gameRepresentations = FXCollections.observableArrayList(filtered);
    tableCombo.getItems().addAll(gameRepresentations);

//    if (selectedCompetition != null) {
//      this.resetCheckbox.setDisable(selectedCompetition.getId() != null);
//      this.resetCheckbox.setSelected(selectedCompetition.getId() != null);
//
//      GameRepresentation game = client.getGame(selectedCompetition.getGameId());
//
//      String botId = String.valueOf(botStatus.getBotId());
//      boolean isOwner = selectedCompetition.getOwner().equals(botId);
//      boolean editable = isOwner && !selectedCompetition.isStarted();
//
//      this.nameField.setText(selectedCompetition.getName());
//      this.nameField.setDisable(!editable);
//
//
//      this.tableCombo.setValue(game);
//      this.tableCombo.setDisable(!editable);
//
//
//      this.competition = selectedCompetition;
//    }
  }
}
