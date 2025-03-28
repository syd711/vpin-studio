package de.mephisto.vpin.ui.tables.dialogs;

import de.mephisto.vpin.restclient.assets.AssetType;
import de.mephisto.vpin.restclient.emulators.GameEmulatorRepresentation;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.util.UploadProgressModel;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.List;

public class BAMCfgUploadController extends BaseUploadController {

  public BAMCfgUploadController() {
    super(AssetType.BAM_CFG, true, true, "cfg", "zip", "7z", "rar");
  }

  @Override
  protected UploadProgressModel createUploadModel() {
    return new CfgUploadProgressModel("BAM Configuration File Upload", getSelections(), getSelectedEmulatorId(), finalizer);
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
}
