package de.mephisto.vpin.ui.tables.dialogs;

import de.mephisto.vpin.restclient.emulators.GameEmulatorRepresentation;
import de.mephisto.vpin.restclient.util.PackageUtil;
import de.mephisto.vpin.restclient.assets.AssetType;
import de.mephisto.vpin.restclient.games.GameRepresentation;
import de.mephisto.vpin.restclient.util.UploaderAnalysis;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.util.UploadProgressModel;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.io.File;

public class AltSoundUploadController extends BaseUploadController {

  @FXML
  private Label tableLabel;

  @FXML
  private Label romLabel;

  private GameRepresentation game;

  public AltSoundUploadController() {
    super(AssetType.ALT_SOUND, false, true, PackageUtil.ARCHIVE_SUFFIXES);
  }

  protected UploadProgressModel createUploadModel() {
    return new AltSoundUploadProgressModel(game != null ? game.getId() : -1, "ALT Sound Upload",
        getSelection(), getSelectedEmulatorId(), game.getRom(), finalizer);
  }

  public void setData(Stage stage, File file, GameRepresentation game, UploaderAnalysis uploaderAnalysis, Runnable finalizer) {
    this.game = game;
    if (this.game != null) {
      this.romLabel.setText(this.game.getRom());
      this.tableLabel.setText(this.game.getGameDisplayName());

      GameEmulatorRepresentation gameEmulator = Studio.client.getEmulatorService().getGameEmulator(this.game.getEmulatorId());
      this.emulatorCombo.setValue(gameEmulator);
    }

    super.setFile(stage, file, uploaderAnalysis, finalizer);
  }

  @Override
  protected void startAnalysis() {
    tableLabel.setText("-");
    romLabel.setText("-");
  }

  @Override
  protected void endAnalysis(String validation, UploaderAnalysis uploaderAnalysis) {
    romLabel.setText(game.getRom());
    tableLabel.setText(game.getGameDisplayName());

    super.endAnalysis(validation, uploaderAnalysis);
  }
}
