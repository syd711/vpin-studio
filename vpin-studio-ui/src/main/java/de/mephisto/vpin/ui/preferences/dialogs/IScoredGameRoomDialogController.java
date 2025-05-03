package de.mephisto.vpin.ui.preferences.dialogs;

import de.mephisto.vpin.commons.fx.DialogController;
import de.mephisto.vpin.connectors.iscored.GameRoom;
import de.mephisto.vpin.connectors.iscored.IScored;
import de.mephisto.vpin.connectors.iscored.IScoredGame;
import de.mephisto.vpin.connectors.iscored.Score;
import de.mephisto.vpin.connectors.vps.VPS;
import de.mephisto.vpin.connectors.vps.model.VpsTable;
import de.mephisto.vpin.connectors.vps.model.VpsTableVersion;
import de.mephisto.vpin.restclient.iscored.IScoredGameRoom;
import de.mephisto.vpin.restclient.iscored.IScoredSettings;
import de.mephisto.vpin.ui.competitions.dialogs.CompetitionOfflineDialogController;
import de.mephisto.vpin.ui.tournaments.dialogs.IScoredGameRoomProgressModel;
import de.mephisto.vpin.ui.util.ProgressDialog;
import de.mephisto.vpin.ui.util.ProgressResultModel;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
  private Label publicReadHint;

  @FXML
  private Label readAPIHint;

  @FXML
  private Label publicWriteHint;

  @FXML
  private CheckBox adminApprovalCheckbox;
  @FXML
  private CheckBox readOnlyCheckbox;
  @FXML
  private CheckBox scoreEntriesCheckbox;
  @FXML
  private CheckBox readAPICheckbox;
  @FXML
  private CheckBox longNamesCheckbox;
  @FXML
  private CheckBox datesCheckbox;
  @FXML
  private CheckBox tournamentColumnCheckbox;

  private IScoredSettings iScoredSettings;
  private IScoredGameRoom gameRoom;

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
    setDisabled(true);
    nameLabel.setText("-");

    ProgressResultModel progressDialog = ProgressDialog.createProgressDialog(new IScoredGameRoomProgressModel(url, force));
    if (!progressDialog.getResults().isEmpty()) {
      GameRoom gr = (GameRoom) progressDialog.getResults().get(0);
      nameLabel.setText(gr.getName());

      urlField.setDisable(false);
      validateBtn.setDisable(false);

      badgeCombo.setDisable(false);
      urlField.setDisable(false);
      validateBtn.setDisable(false);
      setDisabled(false);
      saveBtn.setDisable(false);

      nameLabel.setText(gr.getName());

      readOnlyCheckbox.setSelected(gr.getSettings().isPublicScoresReadingEnabled());
      publicReadHint.setVisible(!readOnlyCheckbox.isSelected());
      scoreEntriesCheckbox.setSelected(gr.getSettings().isPublicScoreEnteringEnabled());
      publicWriteHint.setVisible(!scoreEntriesCheckbox.isSelected());
      readAPICheckbox.setSelected(gr.getSettings().isApiReadingEnabled());
      readAPIHint.setVisible(!readAPICheckbox.isSelected());
      adminApprovalCheckbox.setSelected(gr.getSettings().isAdminApprovalEnabled());
      longNamesCheckbox.setSelected(gr.getSettings().isLongNameInputEnabled());
      datesCheckbox.setSelected(gr.getSettings().isDateFieldEnabled());
      tournamentColumnCheckbox.setSelected(gr.getSettings().isCompetitionColumnEnabled());
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
            URL urlUrl = new URL(vpsUrlString);
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
    }
  }

  private void setDisabled(boolean error) {
    resetCheckbox.setDisable(error);
    synchronizationCheckbox.setDisable(error);
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

    gameRoom.setUrl(urlField.getText());
    gameRoom.setScoreReset(resetCheckbox.isSelected());
    gameRoom.setSynchronize(synchronizationCheckbox.isSelected());
    gameRoom.setBadge(badgeCombo.getValue());

    List<IScoredGameRoom> collect = new ArrayList<>(iScoredSettings.getGameRooms().stream().filter(s -> !s.getUuid().equals(gameRoom.getUuid())).collect(Collectors.toList()));
    collect.add(gameRoom);

    iScoredSettings.setGameRooms(collect);
    client.getPreferenceService().setJsonPreference(iScoredSettings);

    Stage stage = (Stage) ((Button) e.getSource()).getScene().getWindow();
    stage.close();
  }

  @Override
  public void onDialogCancel() {

  }

  public void setData(@NonNull IScoredSettings iScoredSettings, @Nullable IScoredGameRoom gr) {
    this.iScoredSettings = iScoredSettings;
    this.gameRoom = gr;
    if (this.gameRoom == null) {
      this.gameRoom = new IScoredGameRoom();
      this.gameRoom.setUuid(UUID.randomUUID().toString());
    }
    else {
      this.urlField.setText(gr.getUrl());
    }

    this.resetCheckbox.setSelected(gameRoom.isScoreReset());
    this.synchronizationCheckbox.setSelected(gameRoom.isSynchronize());

    saveBtn.setDisable(StringUtils.isEmpty(gameRoom.getUrl()));

    if (!StringUtils.isEmpty(gameRoom.getUrl())) {
      refresh(false);
    }
  }


  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    saveBtn.setDisable(true);

    setDisabled(true);

    List<String> badges = new ArrayList<>(client.getCompetitionService().getCompetitionBadges());
    badges.add(0, null);
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
      }
    });
  }

  public boolean getResult() {
    return result;
  }
}
