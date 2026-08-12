package de.mephisto.vpin.ui.tables.dialogs;

import de.mephisto.vpin.commons.fx.DialogController;
import de.mephisto.vpin.restclient.emulators.GameEmulatorRepresentation;
import de.mephisto.vpin.restclient.games.GameRepresentation;
import de.mephisto.vpin.ui.tables.TableOverviewController;
import de.mephisto.vpin.ui.util.ProgressDialog;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.net.URL;
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
  private Button okBtn;

  private TableOverviewController tableOverviewController;
  private GameRepresentation game;
  private boolean move;

  @FXML
  private void onOk(ActionEvent e) {
    Stage stage = (Stage) ((Button) e.getSource()).getScene().getWindow();
    GameEmulatorRepresentation targetEmulator = targetEmulatorCombo.getValue();
    if (targetEmulator == null) {
      return;
    }

    Platform.runLater(()-> {
      stage.close();
    });

    ProgressDialog.createProgressDialog(new TableMoveCloneProgressModel(game, targetEmulator, move));
    if (tableOverviewController != null) {
      tableOverviewController.doReload();
    }
  }

  @FXML
  private void onCancelClick(ActionEvent e) {
    Stage stage = (Stage) ((Button) e.getSource()).getScene().getWindow();
    stage.close();
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    //populated in setData(), the emulator list depends on the game being moved/cloned
  }

  public void setData(TableOverviewController tableOverviewController, GameRepresentation game, boolean move) {
    this.tableOverviewController = tableOverviewController;
    this.game = game;
    this.move = move;

    okBtn.setText(move ? "Move" : "Clone");
    descriptionLabel1.setText((move ? "Move \"" : "Clone \"") + game.getGameDisplayName() + "\" to a different VPX emulator.");
    descriptionLabel2.setText(move ? "The table is removed from the current emulator once the move has finished." : "");

    List<GameEmulatorRepresentation> targets = client.getEmulatorService().getVpxGameEmulators().stream()
        .filter(emu -> emu.getId() != game.getEmulatorId())
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
