package de.mephisto.vpin.ui.preferences.dialogs;

import de.mephisto.vpin.commons.utils.i18n.Messages;
import de.mephisto.vpin.commons.fx.DialogController;
import de.mephisto.vpin.commons.utils.JFXFuture;
import de.mephisto.vpin.connectors.iscored.GameRoom;
import de.mephisto.vpin.connectors.iscored.IScored;
import de.mephisto.vpin.connectors.iscored.IScoredGame;
import de.mephisto.vpin.connectors.iscored.Score;
import de.mephisto.vpin.connectors.iscored.Settings;
import de.mephisto.vpin.connectors.vps.VPS;
import de.mephisto.vpin.connectors.vps.model.VpsTable;
import de.mephisto.vpin.connectors.vps.model.VpsTableVersion;
import de.mephisto.vpin.restclient.iscored.IScoredGameRoom;
import de.mephisto.vpin.restclient.iscored.IScoredSettings;
import de.mephisto.vpin.restclient.tagging.TaggingUtil;
import de.mephisto.vpin.ui.competitions.dialogs.CompetitionOfflineDialogController;
import de.mephisto.vpin.ui.preferences.PreferencesSavingModel;
import de.mephisto.vpin.ui.util.ProgressDialog;
import de.mephisto.vpin.ui.util.ProgressResultModel;
import de.mephisto.vpin.ui.util.tags.TagField;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

import static de.mephisto.vpin.ui.Studio.client;

public class IScoredGameRoomDialogController implements Initializable, DialogController {
  private final static Logger LOG = LoggerFactory.getLogger(IScoredGameRoomDialogController.class);

  @FXML
  private Button saveBtn;

  @FXML
  private CheckBox synchronizationCheckbox;

  @FXML
  private CheckBox ignoreHiddenCheckbox;

  @FXML
  private Spinner<Integer> syncIntervalSpinner;

  @FXML
  private CheckBox resetCheckbox;

  @FXML
  private TextField urlField;

  @FXML
  private Button validateBtn;

  @FXML
  private ComboBox<String> badgeCombo;


  @FXML
  private Label nameLabel;

  @FXML
  private Label tableCountLabel;

  @FXML
  private Label vpsTableCountLabel;

  @FXML
  private Label scoresCountLabel;

  @FXML
  private Label publicReadPill;
  @FXML
  private Label publicWritePill;
  @FXML
  private Label readAPIPill;
  @FXML
  private Label adminApprovalPill;
  @FXML
  private Label longNamesPill;
  @FXML
  private Label datesPill;
  @FXML
  private Label tournamentColumnPill;

  @FXML
  private Node errorBox;
  @FXML
  private Label publicReadError;
  @FXML
  private Label publicWriteError;

  @FXML
  private Label summaryLabel;

  @FXML
  private Pane tags;

  private IScoredSettings iScoredSettings;
  private IScoredGameRoom gameRoom;
  private TagField tagField;

  private boolean result = false;

  @FXML
  private void onValidate() {
    refresh(true);
  }

  private void refresh(boolean force) {
    String url = urlField.getText().trim();
    if (StringUtils.isEmpty(url)) {
      return;
    }

    if (!IScored.isIScoredGameRoomUrl(url)) {
      return;
    }

    urlField.setDisable(true);
    validateBtn.setDisable(true);
    badgeCombo.setDisable(true);
    ignoreHiddenCheckbox.setDisable(true);
    setDisabled(true);
    nameLabel.setText("–");
    resetFlags();

    ProgressResultModel progressDialog = ProgressDialog.createProgressDialog(new IScoredGameRoomLoadingProgressModel(url, force));
    if (!progressDialog.getResults().isEmpty()) {
      GameRoom gr = (GameRoom) progressDialog.getResults().getFirst();
      nameLabel.setText(gr.getName());

      validateBtn.setDisable(false);

      badgeCombo.setDisable(false);
      validateBtn.setDisable(false);
      ignoreHiddenCheckbox.setDisable(false);
      setDisabled(false);
      saveBtn.setDisable(false);

      nameLabel.setText(gr.getName());

      Settings settings = gr.getSettings();
      boolean publicRead = settings.isPublicScoresReadingEnabled();
      boolean publicWrite = settings.isPublicScoreEnteringEnabled();
      // required for VPin Studio
      setPill(publicReadPill, publicRead ? "sm-pill-ok" : "sm-pill-error", Messages.get("pref.iscored_gameroom.this_flag_must_be_enabled_for_your_game"));
      setPill(publicWritePill, publicWrite ? "sm-pill-ok" : "sm-pill-error", Messages.get("pref.iscored_gameroom.this_flag_must_be_enabled_for_your_game"));
      // only needed for hidden tables
      setPill(readAPIPill, settings.isApiReadingEnabled() ? "sm-pill-ok" : "sm-pill-warn", Messages.get("pref.iscored_gameroom.this_flag_must_be_enabled_to_post_highscores"));
      // informational
      setPill(adminApprovalPill, settings.isAdminApprovalEnabled() ? "sm-pill-ok" : "sm-pill-unknown", null);
      setPill(longNamesPill, settings.isLongNameInputEnabled() ? "sm-pill-ok" : "sm-pill-unknown", null);
      setPill(datesPill, settings.isDateFieldEnabled() ? "sm-pill-ok" : "sm-pill-unknown", null);
      setPill(tournamentColumnPill, settings.isCompetitionColumnEnabled() ? "sm-pill-ok" : "sm-pill-unknown", null);

      setShown(publicReadError, !publicRead);
      setShown(publicWriteError, !publicWrite);
      setShown(errorBox, !publicRead || !publicWrite);
      summaryLabel.setText("");

      tableCountLabel.setText(String.valueOf(gr.getTaggedGames().size()));

      badgeCombo.setValue(gameRoom.getBadge());

      int count = 0;
      List<IScoredGame> games = gr.getGames();
      for (IScoredGame game : games) {
        List<String> tags = game.getTags();
        Optional<String> first = tags.stream().filter(t -> VPS.isVpsTableUrl(t)).findFirst();
        if (first.isPresent()) {
          try {
            String vpsUrlString = first.get();
            URL urlUrl = URI.create(vpsUrlString).toURL();
            String idSegment = urlUrl.getQuery();

            String tableId = idSegment.substring(idSegment.indexOf("=") + 1);
            if (tableId.contains("&")) {
              tableId = tableId.substring(0, tableId.indexOf("&"));
            }
            else if (tableId.contains("#")) {
              tableId = tableId.substring(0, tableId.indexOf("#"));
            }

            VpsTable vpsTable = client.getVpsService().getTableById(tableId);
            if (vpsTable == null) {
              continue;
            }

            String[] split = vpsUrlString.split("#");
            VpsTableVersion vpsVersion = null;
            if (split.length > 1) {
              vpsVersion = vpsTable.getTableVersionById(split[1]);
            }

            if (vpsVersion == null) {
              continue;
            }

            count++;
          }
          catch (Exception e) {
            LOG.error("Failed to parse table entry: " + e.getMessage(), e);
          }
        }
      }
      vpsTableCountLabel.setText(String.valueOf(count));

      int scoreCount = 0;
      for (IScoredGame game : games) {
        List<Score> scores = game.getScores();
        scoreCount += scores.size();
      }
      scoresCountLabel.setText(String.valueOf(scoreCount));
    }
    else {
      urlField.setDisable(false);
      validateBtn.setDisable(false);
      saveBtn.setDisable(true);
      summaryLabel.setText(Messages.get("pref.iscored_gameroom.load_failed"));
    }
  }

  private void resetFlags() {
    for (Label pill : List.of(publicReadPill, publicWritePill, readAPIPill, adminApprovalPill, longNamesPill, datesPill, tournamentColumnPill)) {
      setPill(pill, "sm-pill-unknown", null);
    }
    setShown(errorBox, false);
    tableCountLabel.setText("–");
    vpsTableCountLabel.setText("–");
    scoresCountLabel.setText("–");
  }

  private static void setPill(Label pill, String state, @Nullable String tooltip) {
    pill.getStyleClass().removeAll("sm-pill-ok", "sm-pill-warn", "sm-pill-error", "sm-pill-unknown");
    pill.getStyleClass().add(state);
    pill.setTooltip(tooltip != null ? new Tooltip(tooltip) : null);
  }

  private static void setShown(Node node, boolean shown) {
    node.setVisible(shown);
    node.setManaged(shown);
  }

  private void setDisabled(boolean error) {
    resetCheckbox.setDisable(error);
    synchronizationCheckbox.setDisable(error);
    syncIntervalSpinner.setDisable(error);
  }

  @FXML
  private void onCancelClick(ActionEvent e) {
    result = false;
    Stage stage = (Stage) ((Button) e.getSource()).getScene().getWindow();
    stage.close();
  }

  @FXML
  private void onSaveClick(ActionEvent e) {
    result = true;

    gameRoom.setUrl(urlField.getText().trim());
    gameRoom.setScoreReset(resetCheckbox.isSelected());
    gameRoom.setSynchronize(synchronizationCheckbox.isSelected());
    gameRoom.setIgnoreHidden(ignoreHiddenCheckbox.isSelected());
    gameRoom.setSyncIntervalMinutes(syncIntervalSpinner.getValue() != null ? syncIntervalSpinner.getValue() : 0);
    gameRoom.setBadge(badgeCombo.getValue());
    gameRoom.setTags(TaggingUtil.join(tagField.getTags()));

    List<IScoredGameRoom> collect = new ArrayList<>(iScoredSettings.getGameRooms().stream().filter(s -> !s.getUuid().equals(gameRoom.getUuid())).collect(Collectors.toList()));
    collect.add(gameRoom);

    iScoredSettings.setGameRooms(collect);

    Platform.runLater(() -> {
      ProgressDialog.createProgressDialog(new PreferencesSavingModel(Messages.get("dialog.synchronizing_iscored"), iScoredSettings));
      Stage stage = (Stage) ((Button) e.getSource()).getScene().getWindow();
      stage.close();
    });
  }

  @Override
  public void onDialogCancel() {
    result = false;
  }

  public void setData(@NonNull IScoredSettings iScoredSettings, @Nullable IScoredGameRoom gr) {
    this.iScoredSettings = iScoredSettings;
    this.gameRoom = gr;
    this.urlField.setDisable(gr != null);

    if (this.gameRoom == null) {
      this.gameRoom = new IScoredGameRoom();
      this.gameRoom.setUuid(UUID.randomUUID().toString());
    }
    else {
      this.urlField.setText(gr.getUrl());
      this.tagField.setTags(TaggingUtil.getTags(gr.getTags()));
    }

    this.resetCheckbox.setSelected(gameRoom.isScoreReset());
    this.synchronizationCheckbox.setSelected(gameRoom.isSynchronize());
    this.ignoreHiddenCheckbox.setSelected(gameRoom.isIgnoreHidden());
    this.syncIntervalSpinner.getValueFactory().setValue(gameRoom.getSyncIntervalMinutes());

    saveBtn.setDisable(StringUtils.isEmpty(gameRoom.getUrl()));

    if (!StringUtils.isEmpty(gameRoom.getUrl())) {
      refresh(false);
    }
  }


  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    saveBtn.setDisable(true);

    syncIntervalSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 1440, 0, 5));
    syncIntervalSpinner.focusedProperty().addListener((obs, oldValue, focused) -> {
      if (!focused) {
        syncIntervalSpinner.increment(0); // commits the typed text
      }
    });

    setDisabled(true);

    List<String> badges = new ArrayList<>(client.getCompetitionService().getCompetitionBadges());
    badges.addFirst(null);
    ObservableList<String> imageList = FXCollections.observableList(badges);
    badgeCombo.setItems(imageList);
    badgeCombo.setCellFactory(c -> new CompetitionOfflineDialogController.CompetitionImageListCell(client));
    badgeCombo.setButtonCell(new CompetitionOfflineDialogController.CompetitionImageListCell(client));
    badgeCombo.setDisable(true);

    urlField.textProperty().addListener(new ChangeListener<String>() {
      @Override
      public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
        saveBtn.setDisable(true);
        setDisabled(true);
        summaryLabel.setText(Messages.get("pref.iscored_gameroom.validate_hint"));
      }
    });

    tagField = new TagField(Collections.emptyList());
    tags.getChildren().add(tagField);

    JFXFuture.supplyAsync(() -> {
      return client.getTaggingService().getTags();
    }).thenAcceptLater((initialTags) -> {
      tagField.setSuggestions(initialTags);
    });
  }

  public boolean getResult() {
    return result;
  }
}
