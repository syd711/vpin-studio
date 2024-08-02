package de.mephisto.vpin.ui.competitions.dialogs;

import de.mephisto.vpin.commons.fx.DialogController;
import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.connectors.iscored.GameRoom;
import de.mephisto.vpin.connectors.iscored.IScoredGame;
import de.mephisto.vpin.connectors.iscored.Score;
import de.mephisto.vpin.connectors.vps.VPS;
import de.mephisto.vpin.connectors.vps.model.VpsTable;
import de.mephisto.vpin.connectors.vps.model.VpsTableVersion;
import de.mephisto.vpin.ui.util.AutoCompleteTextField;
import de.mephisto.vpin.ui.util.AutoCompleteTextFieldChangeListener;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.TreeSet;
import java.util.stream.Collectors;

import static de.mephisto.vpin.ui.Studio.client;

public class IScoredInfoDialogController implements DialogController, Initializable {
  private final static Logger LOG = LoggerFactory.getLogger(IScoredInfoDialogController.class);

  @FXML
  private Button okButton;

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
  private Label publicWriteHint;

  @FXML
  private CheckBox adminApprovalCheckbox;
  @FXML
  private CheckBox readOnlyCheckbox;
  @FXML
  private CheckBox scoreEntriesCheckbox;
  @FXML
  private CheckBox longNamesCheckbox;
  @FXML
  private CheckBox datesCheckbox;

  @Override
  public void onDialogCancel() {

  }

  @FXML
  private void onCancelClick(ActionEvent e) {
    Stage stage = (Stage) ((Button) e.getSource()).getScene().getWindow();
    stage.close();
  }

  @FXML
  private void onDialogSubmit(ActionEvent e) {
    Stage stage = (Stage) ((Button) e.getSource()).getScene().getWindow();
    stage.close();
  }

  public void setData(Stage stage, GameRoom gameRoom) {
    try {
      nameLabel.setText(gameRoom.getName());

      readOnlyCheckbox.setSelected(gameRoom.getSettings().isPublicScoresReadingEnabled());
      publicReadHint.setVisible(!readOnlyCheckbox.isSelected());

      scoreEntriesCheckbox.setSelected(gameRoom.getSettings().isPublicScoreEnteringEnabled());
      publicWriteHint.setVisible(!scoreEntriesCheckbox.isSelected());

      adminApprovalCheckbox.setSelected(gameRoom.getSettings().isAdminApprovalEnabled());
      longNamesCheckbox.setSelected(gameRoom.getSettings().isLongNameInputEnabled());
      datesCheckbox.setSelected(gameRoom.getSettings().isDateFieldEnabled());

      tableCountLabel.setText(String.valueOf(gameRoom.getGames().size()));

      int count = 0;
      List<IScoredGame> games = gameRoom.getGames();
      for (IScoredGame game : games) {
        List<String> tags = game.getTags();
        Optional<String> first = tags.stream().filter(t -> VPS.isVpsTableUrl(t)).findFirst();
        if (first.isPresent()) {
          try {
            String vpsUrl = first.get();
            URL url = new URL(vpsUrl);
            String idSegment = url.getQuery();

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

            String[] split = vpsUrl.split("#");
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
    catch (Exception e) {
      LOG.error("Failed to read iScored data: " + e.getMessage());
      WidgetFactory.showAlert(stage, "Error", "Error reading game room data: " + e.getMessage());
    }
  }

  @Override
  public void initialize(URL location, ResourceBundle resources) {
  }
}
