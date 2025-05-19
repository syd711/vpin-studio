package de.mephisto.vpin.ui.tables.dialogs;

import de.mephisto.vpin.restclient.assets.AssetType;
import de.mephisto.vpin.restclient.games.GameRepresentation;
import de.mephisto.vpin.restclient.util.UploaderAnalysis;
import de.mephisto.vpin.ui.util.UploadProgressModel;
import javafx.stage.Stage;

import java.io.File;

public class DMDUploadController extends BaseUploadController {

  private GameRepresentation game;

  public DMDUploadController() {
    super(AssetType.DMD_PACK, false, true, "zip", "rar", "7z");
  }

  @Override
  protected UploadProgressModel createUploadModel() {
    return new DMDUploadProgressModel("DMD Bundle Upload", getSelection(), getSelectedEmulatorId(), game, finalizer);
  }

  public void setData(Stage stage, File file, GameRepresentation game, UploaderAnalysis analysis, Runnable finalizer) {
    this.game = game;
    super.setFile(stage, file, analysis, finalizer);
  }
}
