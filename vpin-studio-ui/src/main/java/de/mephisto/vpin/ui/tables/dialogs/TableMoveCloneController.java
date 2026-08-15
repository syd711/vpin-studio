package de.mephisto.vpin.ui.tables.dialogs;

import de.mephisto.vpin.commons.fx.DialogController;
import de.mephisto.vpin.restclient.emulators.GameEmulatorRepresentation;
import de.mephisto.vpin.restclient.games.GameRepresentation;
import de.mephisto.vpin.restclient.games.descriptors.SubfolderNaming;
import de.mephisto.vpin.ui.tables.TableOverviewController;
import de.mephisto.vpin.ui.util.ProgressDialog;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.ResourceBundle;

import static de.mephisto.vpin.ui.Studio.client;

public class TableMoveCloneController implements Initializable, DialogController {
  private final static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  @FXML
  private Label descriptionLabel1;
  @FXML
  private Label descriptionLabel2;

  @FXML
  private ComboBox<GameEmulatorRepresentation> targetEmulatorCombo;

  @FXML
  private CheckBox subfolderCheckBox;

  @FXML
  private VBox subfolderNamingBox;

  @FXML
  private RadioButton tableNameRadio;
  @FXML
  private RadioButton tableDisplayNameRadio;
  @FXML
  private RadioButton tableFilenameRadio;

  @FXML
  private Button okBtn;

  private TableOverviewController tableOverviewController;
  private List<GameRepresentation> games;
  private boolean move;

  @FXML
  private void onSubfolderToggle(ActionEvent e) {
    subfolderNamingBox.setDisable(!subfolderCheckBox.isSelected());
  }

  @FXML
  private void onOk(ActionEvent e) {
    Stage stage = (Stage) ((Button) e.getSource()).getScene().getWindow();
    GameEmulatorRepresentation targetEmulator = targetEmulatorCombo.getValue();
    if (targetEmulator == null) {
      return;
    }

    boolean createSubfolder = subfolderCheckBox.isSelected();
    SubfolderNaming subfolderNaming = getSelectedSubfolderNaming();

    Platform.runLater(()-> {
      stage.close();
    });

    ProgressDialog.createProgressDialog(new TableMoveCloneProgressModel(games, targetEmulator, move, createSubfolder, subfolderNaming));
    if (tableOverviewController != null) {
      tableOverviewController.doReload();
    }
  }

  private SubfolderNaming getSelectedSubfolderNaming() {
    if (tableDisplayNameRadio.isSelected()) {
      return SubfolderNaming.TABLE_DISPLAY_NAME;
    }
    if (tableFilenameRadio.isSelected()) {
      return SubfolderNaming.TABLE_FILENAME;
    }
    return SubfolderNaming.TABLE_NAME;
  }

  @FXML
  private void onCancelClick(ActionEvent e) {
    Stage stage = (Stage) ((Button) e.getSource()).getScene().getWindow();
    stage.close();
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    //populated in setData(), the emulator list depends on the game(s) being moved/cloned
    subfolderNamingBox.setDisable(true);
  }

  public void setData(TableOverviewController tableOverviewController, GameRepresentation game, boolean move) {
    setData(tableOverviewController, Collections.singletonList(game), move);
  }

  public void setData(TableOverviewController tableOverviewController, List<GameRepresentation> games, boolean move) {
    this.tableOverviewController = tableOverviewController;
    this.games = games;
    this.move = move;

    boolean bulk = games.size() > 1;
    okBtn.setText(move ? "Move" : "Clone");
    if (bulk) {
      descriptionLabel1.setText((move ? "Move " : "Clone ") + games.size() + " tables to a different VPX emulator.");
      descriptionLabel2.setText(move ? "The tables are removed from their current emulator once the move has finished." : "");
    }
    else {
      GameRepresentation game = games.get(0);
      descriptionLabel1.setText((move ? "Move \"" : "Clone \"") + game.getGameDisplayName() + "\" to a different VPX emulator.");
      descriptionLabel2.setText(move ? "The table is removed from the current emulator once the move has finished." : "");
    }

    List<GameEmulatorRepresentation> targets = client.getEmulatorService().getVpxGameEmulators().stream()
        .filter(emu -> bulk || emu.getId() != games.get(0).getEmulatorId())
        .toList();
    targetEmulatorCombo.getItems().setAll(targets);
    if (!targets.isEmpty()) {
      targetEmulatorCombo.getSelectionModel().selectFirst();
    }
    else {
      okBtn.setDisable(true);
    }
  }

  @Override
  public void onDialogCancel() {

  }
}
