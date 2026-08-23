package de.mephisto.vpin.ui.competitions.dialogs;

import de.mephisto.vpin.commons.fx.DialogController;
import de.mephisto.vpin.restclient.client.VPinStudioClient;
import de.mephisto.vpin.restclient.competitions.CompetitionRepresentation;
import de.mephisto.vpin.restclient.competitions.CompetitionType;
import de.mephisto.vpin.restclient.competitions.JoinMode;
import de.mephisto.vpin.restclient.discord.DiscordBotStatus;
import de.mephisto.vpin.restclient.discord.DiscordChannel;
import de.mephisto.vpin.restclient.discord.DiscordCompetitionData;
import de.mephisto.vpin.restclient.discord.DiscordServer;
import de.mephisto.vpin.restclient.frontend.VPinScreen;
import de.mephisto.vpin.restclient.games.FrontendMediaItemRepresentation;
import de.mephisto.vpin.restclient.games.FrontendMediaRepresentation;
import de.mephisto.vpin.restclient.games.GameRepresentation;
import de.mephisto.vpin.restclient.highscores.NVRamList;
import de.mephisto.vpin.restclient.players.PlayerRepresentation;
import de.mephisto.vpin.restclient.util.DateUtil;
import de.mephisto.vpin.restclient.util.FileUtils;
import de.mephisto.vpin.ui.competitions.CompetitionsDialogHelper;
import de.mephisto.vpin.commons.utils.i18n.Messages;
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
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.net.URL;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.ArrayList;
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
  private Label remainingTimeLabel;

  @FXML
  private Label startDateLabel;

  @FXML
  private Label scoreLimitLabel;

  @FXML
  private Label scoreValidationLabel;

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

  @FXML
  private Label nvramLabel;

  private NVRamList nvRamList;

  private CompetitionRepresentation competition;

  private DiscordCompetitionData discordCompetitionData;

  private DiscordBotStatus botStatus;

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
    competition.setStartDate(this.discordCompetitionData.getSdt().toInstant());
    competition.setEndDate(this.discordCompetitionData.getEdt().toInstant());
    competition.setJoinMode(this.discordCompetitionData.getMode());
    competition.setHighscoreReset(true);

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
    this.nvRamList = client.getNvRamsService().getResettedNVRams();

    List<DiscordServer> servers = client.getDiscordService().getDiscordServers();
    ObservableList<DiscordServer> discordServers = FXCollections.observableArrayList(servers);
    serversCombo.getItems().addAll(discordServers);
    serversCombo.valueProperty().addListener((observableValue, gameRepresentation, t1) -> {
      this.discordCompetitionData = null;
      channelsCombo.setDisable(false);
      channelsCombo.setItems(FXCollections.observableArrayList(client.getDiscordService().getDiscordChannels(t1.getId())));
      validate();
    });


    ObservableList<DiscordChannel> discordChannels = FXCollections.observableArrayList(new ArrayList<>());
    channelsCombo.setDisable(true);
    channelsCombo.getItems().addAll(discordChannels);
    channelsCombo.valueProperty().addListener((observableValue, gameRepresentation, t1) -> {
      validate();
    });

    ArrayList<String> badges = new ArrayList<>(client.getCompetitionService().getCompetitionBadges());
    badges.addFirst(null);
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
      FrontendMediaRepresentation frontendMedia = client.getFrontendService().getFrontendMedia(game.getId());
      FrontendMediaItemRepresentation mediaItem = frontendMedia.getDefaultMediaItem(VPinScreen.Wheel);
      if (mediaItem != null) {
        ByteArrayInputStream gameMediaItem = client.getWheelIcon(game.getId(), false);
        Image image = new Image(gameMediaItem);
        iconPreview.setImage(image);

        if (badge != null) {
          Image badgeIcon = new Image(client.getCompetitionService().getCompetitionBadge(badge));
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
    refreshPreview(null, null);

    validationContainer.setVisible(true);
    this.saveBtn.setDisable(true);

    this.competitionIconCombo.setDisable(true);
    this.tableCombo.setDisable(true);

    this.tableLabel.setText("-");
    this.startDateLabel.setText("-");
    this.endDateLabel.setText("-");
    this.remainingTimeLabel.setText("-");
    this.nameLabel.setText("-");
    this.ownerLabel.setText("-");


    DiscordServer server = this.serversCombo.getValue();
    DiscordChannel channel = this.channelsCombo.getValue();


    if (server == null) {
      validationTitle.setText(Messages.get("competitions.discord_competition_edit.no_discord_server_selected"));
      validationDescription.setText(Messages.get("competitions.discord_competition_edit.select_a_discord_server_where_the_competition_takes_place"));
      return;
    }

    if (this.botStatus == null || this.botStatus.getServerId() != server.getId()) {
      this.botStatus = client.getDiscordService().getDiscordStatus(server.getId());
    }

    if (botStatus == null || botStatus.getBotInitials().isEmpty()) {
      validationTitle.setText(Messages.get("competitions.discord_competition_edit.invalid_bot_nickname"));
      validationDescription.setText(Messages.get("competitions.discord_competition_edit.to_submit_highscores_your_bot_must_have_the_name_pattern"));
      return;
    }

    if (channel == null) {
      validationTitle.setText(Messages.get("competitions.discord_competition_edit.no_discord_channel_selected"));
      validationDescription.setText(Messages.get("competitions.discord_competition_join.select_a_discord_channel_where_the_competition_takes_place"));
      return;
    }

    //check Discord permissions
    if (!client.getCompetitionService().hasChannelManagePermissions(server.getId(), channel.getId())) {
      validationTitle.setText(Messages.get("competitions.discord_competition_join.insufficient_permissions"));
      validationDescription.setText(Messages.get("competitions.discord_competition_join.your_discord_bot_has_insufficient_permissions_to_start"));
      return;
    }

    this.discordCompetitionData = client.getDiscordService().getDiscordCompetitionData(server.getId(), channel.getId());
    if (discordCompetitionData != null) {
      List<GameRepresentation> gamesByRom = client.getGameService().getGamesByRom(this.discordCompetitionData.getRom());

      tableCombo.getItems().addAll(FXCollections.observableList(gamesByRom));
      if (!gamesByRom.isEmpty()) {
        tableCombo.setValue(gamesByRom.getFirst());
        refreshPreview(tableCombo.getValue(), null);
      }
    }
    else {
      this.tableCombo.setItems(FXCollections.observableList(new ArrayList<>()));
    }

    if (this.discordCompetitionData == null) {
      validationTitle.setText(Messages.get("competitions.discord_competition_join.no_competition_found"));
      validationDescription.setText(Messages.get("competitions.discord_competition_join.select_a_discord_server_and_channel_where_the_competition_takes_place"));
      return;
    }


    DateTimeFormatter formatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM);
    this.tableLabel.setText(this.discordCompetitionData.getTname());
    this.startDateLabel.setText(formatter.format(this.discordCompetitionData.getSdt()));
    this.endDateLabel.setText(formatter.format(this.discordCompetitionData.getEdt()));
    this.remainingTimeLabel.setText(DateUtil.formatDuration(this.discordCompetitionData.getSdt(), this.discordCompetitionData.getEdt()));
    this.nameLabel.setText(this.discordCompetitionData.getName());

    String mode = this.discordCompetitionData.getMode();
    if(StringUtils.isEmpty(mode)) {
      this.scoreValidationLabel.setText(CompetitionDiscordDialogController.ROM_DESCRIPTION);
    }
    else{
      JoinMode m = JoinMode.valueOf(mode);
      if(m.equals(JoinMode.ROM_ONLY)) {
        this.scoreValidationLabel.setText(CompetitionDiscordDialogController.ROM_DESCRIPTION);
      }
      else if(m.equals(JoinMode.STRICT)) {
        this.scoreValidationLabel.setText(CompetitionDiscordDialogController.STRICT_DESCRIPTION);
      }
      else {
        this.scoreValidationLabel.setText(CompetitionDiscordDialogController.CHECKSUM_DESCRIPTION);
      }
    }


    this.scoreLimitLabel.setText(String.valueOf(this.discordCompetitionData.getScrL()));

    PlayerRepresentation discordPlayer = client.getDiscordService().getDiscordPlayer(server.getId(), Long.parseLong(this.discordCompetitionData.getOwner()));
    if (discordPlayer != null) {
      this.ownerLabel.setText(discordPlayer.getName());
    }

    CompetitionRepresentation existingEntry = client.getCompetitionService().getCompetitionByUuid(this.discordCompetitionData.getUuid());
    boolean isOwner = this.discordCompetitionData.getOwner().equals(String.valueOf(botStatus.getBotId()));
    if (existingEntry != null && isOwner) {
      validationTitle.setText(Messages.get("competitions.discord_competition_join.invalid_competition_selected"));
      validationDescription.setText(Messages.get("competitions.discord_competition_join.you_are_the_owner_of_this_competition"));
      return;
    }

    if (existingEntry != null) {
      validationTitle.setText(Messages.get("competitions.discord_competition_join.competition_exist"));
      validationDescription.setText(Messages.get("competitions.discord_competition_join.you_already_joined_this_competition"));
      return;
    }

    if (this.discordCompetitionData.getEdt().isBefore(OffsetDateTime.now())) {
      validationTitle.setText(Messages.get("competitions.discord_competition_join.invalid_competition_data"));
      validationDescription.setText(Messages.get("competitions.discord_competition_join.ups_looks_like_the_selected_competition_wasnt_reset"));
      return;
    }

    if (tableCombo.getItems().isEmpty()) {
      validationTitle.setText(Messages.get("competitions.discord_competition_join.no_matching_table_found"));
      validationDescription.setText(Messages.get("competitions.discord_competition_join.none_of_your_tables_matches_the_rom_name", this.discordCompetitionData.getRom(), this.discordCompetitionData.getTname()));
      return;
    }

    this.tableCombo.setDisable(false);

    GameRepresentation game = this.tableCombo.getValue();
    if(StringUtils.isEmpty(mode)) {
      //ignore, the rom check is already done here
    }
    else{
      JoinMode m = JoinMode.valueOf(mode);
      if(m.equals(JoinMode.ROM_ONLY)) {
        //ignore, the rom check is already done here
      }
      else if(m.equals(JoinMode.STRICT)) {
        long tableSize = game.getGameFileSize();
        long competitionTableSize = this.discordCompetitionData.getFs();
        long min = competitionTableSize - (1024*1024);
        long max = competitionTableSize + (1024*1024);
        if (tableSize < min || tableSize > max) {
          validationTitle.setText(Messages.get("competitions.discord_competition_join.the_table_file_size_does_not_match"));
          validationDescription.setText(Messages.get("competitions.discord_competition_join.the_size_of_your_vpx_file_differs_by", FileUtils.readableFileSize(competitionTableSize-tableSize)));
          return;
        }
      }
      else if(m.equals(JoinMode.CHECKSUM)) {
        String checksum = client.getVpxService().getCheckSum(game);
        String chksm = discordCompetitionData.getChksm();
        if(!chksm.equalsIgnoreCase(checksum)) {
          validationTitle.setText(Messages.get("competitions.discord_competition_join.the_checksum_does_not_match"));
          validationDescription.setText(Messages.get("competitions.discord_competition_join.this_mean_the_script_of_your_vpx_file_and_that_of"));
          return;
        }
      }
    }

    CompetitionsDialogHelper.refreshResetStatusIcon(game, nvRamList, nvramLabel);

    if (!resetCheckbox.isSelected()) {
      validationTitle.setText(Messages.get("competitions.discord_competition_edit.highscore_reset_required"));
      validationDescription.setText(Messages.get("competitions.discord_competition_edit.the_reset_is_required_in_case_your_highscore_2"));
      return;
    }

    this.competitionIconCombo.setDisable(false);

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
