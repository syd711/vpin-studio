package de.mephisto.vpin.ui.tables.dialogs;

import de.mephisto.vpin.restclient.assets.AssetType;
import de.mephisto.vpin.restclient.emulators.GameEmulatorRepresentation;
import de.mephisto.vpin.restclient.games.GameRepresentation;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.util.UploadProgressModel;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

import java.util.List;

public class BAMCfgUploadController extends BaseUploadController {

  private GameRepresentation game;

  public BAMCfgUploadController() {
    super(AssetType.BAM_CFG, true, false, "cfg", "zip", "7z", "rar");
  }

  @FXML
  private Label gameLabel;


  @Override
  protected UploadProgressModel createUploadModel() {
    return new BamCfgUploadProgressModel("BAM Configuration File Upload", getSelections(), game.getId());
  }

  protected void refreshEmulators() {
    List<GameEmulatorRepresentation> gameEmulators = Studio.client.getEmulatorService().getFpGameEmulators();
    emulator = gameEmulators.get(0);
    ObservableList<GameEmulatorRepresentation> emulators = FXCollections.observableList(gameEmulators);
    emulatorCombo.setItems(emulators);
    emulatorCombo.setValue(emulator);
    emulatorCombo.valueProperty().addListener((observableValue, gameEmulatorRepresentation, t1) -> {
      emulator = t1;
      refreshSelection(null);
    });
  }

  public void setGame(GameRepresentation game) {
    this.game = game;
    this.gameLabel.setText(game.getGameDisplayName());
  }
}
