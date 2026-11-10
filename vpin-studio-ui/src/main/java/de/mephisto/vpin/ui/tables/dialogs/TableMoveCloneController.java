package de.mephisto.vpin.ui.tables.dialogs;

import de.mephisto.vpin.commons.utils.i18n.Messages;
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
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.apache.commons.lang3.StringUtils;
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
  private RadioButton moveRadio;
  @FXML
  private ToggleGroup operationGroup;

  @FXML
  private Label descriptionLabel1;
  @FXML
  private Label descriptionLabel2;

  @FXML
  private ComboBox<GameEmulatorRepresentation> targetEmulatorCombo;

  @FXML
  private Label emulatorVersionLabel;

  @FXML
  private Label layoutHintLabel;

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

  /**
   * The subfolder selection of the user, restored when switching back from a VPX 10.8.1 emulator that enforces it.
   */
  private boolean userSubfolderSelection;

  @FXML
  private void onSubfolderToggle(ActionEvent e) {
    userSubfolderSelection = subfolderCheckBox.isSelected();
    subfolderNamingBox.setDisable(!subfolderCheckBox.isSelected());
  }

  /**
   * VPX 10.8.1 keeps the ROM, NVRAM, ... next to the table, so the table always needs its own subfolder there.
   * Older versions use the shared folders of the installation, the subfolder is optional then.
   */
  private void onTargetEmulatorChange(GameEmulatorRepresentation target) {
    if (target == null) {
      emulatorVersionLabel.setText("");
      layoutHintLabel.setText("");
      return;
    }

    boolean perTable = target.isPerTableFileStructureSupported();
    String version = target.getEmulatorVersion();
    emulatorVersionLabel.setText(StringUtils.isEmpty(version) ? "" : Messages.get("tables.table_move_clone.emulator_version", version));
    emulatorVersionLabel.setVisible(!StringUtils.isEmpty(version));
    emulatorVersionLabel.setManaged(!StringUtils.isEmpty(version));
    layoutHintLabel.setText(perTable ? Messages.get("tables.table_move_clone.layout_per_table_hint") : Messages.get("tables.table_move_clone.layout_shared_hint"));

    subfolderCheckBox.setSelected(perTable || userSubfolderSelection);
    subfolderCheckBox.setDisable(perTable);
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

    ProgressDialog.createProgressDialog(new TableMoveCloneProgressModel(games, targetEmulator, isMove(), createSubfolder, subfolderNaming));
    if (tableOverviewController != null) {
      tableOverviewController.doReload();
    }
  }

  private boolean isMove() {
    return moveRadio.isSelected();
  }

  /**
   * Updates the descriptions and the OK button for the selected operation (move or clone).
   */
  private void onOperationChange() {
    if (games == null || games.isEmpty()) {
      return;
    }

    boolean move = isMove();
    okBtn.setText(move ? Messages.get("dialog.move_button") : Messages.get("dialog.clone_button"));
    if (games.size() > 1) {
      descriptionLabel1.setText(move ? Messages.get("dialog.move_tables_desc", games.size()) : Messages.get("dialog.clone_tables_desc", games.size()));
      descriptionLabel2.setText(move ? Messages.get("dialog.move_tables_removed_hint") : "");
    }
    else {
      GameRepresentation game = games.get(0);
      descriptionLabel1.setText(move ? Messages.get("dialog.move_table_desc", game.getGameDisplayName()) : Messages.get("dialog.clone_table_desc", game.getGameDisplayName()));
      descriptionLabel2.setText(move ? Messages.get("dialog.move_table_removed_hint") : "");
    }
    descriptionLabel2.setVisible(move);
    descriptionLabel2.setManaged(move);
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
    targetEmulatorCombo.valueProperty().addListener((obs, oldValue, newValue) -> onTargetEmulatorChange(newValue));
    operationGroup.selectedToggleProperty().addListener((obs, oldValue, newValue) -> {
      // keep one operation selected at all times
      if (newValue == null) {
        operationGroup.selectToggle(oldValue);
        return;
      }
      onOperationChange();
    });
  }

  public void setData(TableOverviewController tableOverviewController, GameRepresentation game) {
    setData(tableOverviewController, Collections.singletonList(game));
  }

  public void setData(TableOverviewController tableOverviewController, List<GameRepresentation> games) {
    this.tableOverviewController = tableOverviewController;
    this.games = games;

    boolean bulk = games.size() > 1;
    onOperationChange();

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
