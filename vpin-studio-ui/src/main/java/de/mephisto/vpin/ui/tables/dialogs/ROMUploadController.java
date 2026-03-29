package de.mephisto.vpin.ui.tables.dialogs;

import de.mephisto.vpin.restclient.assets.AssetType;
import de.mephisto.vpin.restclient.emulators.GameEmulatorRepresentation;
import de.mephisto.vpin.ui.util.UploadProgressModel;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import org.jetbrains.annotations.Nullable;

public class ROMUploadController extends BaseUploadController {

  @FXML
  private Label descriptionLabel;

  public ROMUploadController() {
    super(AssetType.ROM, true, true, "zip");
  }

  @Override
  protected UploadProgressModel createUploadModel() {
    return new RomUploadProgressModel("ROM Upload", getSelections(), getSelectedEmulatorId());
  }

  @Override
  public void setSelectedEmulator(@Nullable GameEmulatorRepresentation emulator) {
    super.setSelectedEmulator(emulator);

    if(emulator != null && emulator.isMameEmulator()) {
      descriptionLabel.setText("The select game for this ROM will be uploaded into the \"roms\" folder of MAME.");
    }
  }
}
