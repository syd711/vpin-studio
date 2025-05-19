package de.mephisto.vpin.ui.tables.dialogs;

import de.mephisto.vpin.restclient.assets.AssetType;
import de.mephisto.vpin.restclient.games.GameRepresentation;
import de.mephisto.vpin.restclient.util.UploaderAnalysis;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.util.UploadProgressModel;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class PupPackUploadController extends BaseUploadController {

  @FXML
  private Label tableLabel;

  @FXML
  private Label romLabel;

  private String rom;

  public PupPackUploadController() {
    super(AssetType.PUP_PACK, false, false, "zip", "rar", "7z");
  } 

  @Override 
  protected UploadProgressModel createUploadModel() {
    return new PupPackUploadProgressModel(null, "PUP Pack Upload", getSelection(), finalizer);
  }

  @Override
  protected void startAnalysis() {
    tableLabel.setText("-");
    romLabel.setText("-");
  }

  @Override
  protected String validateAnalysis(UploaderAnalysis uploaderAnalysis) {
    // do the standard puppack validation
    String validation = super.validateAnalysis(uploaderAnalysis);
    if (validation == null) {
      this.rom = uploaderAnalysis.getRomFromPupPack();
    }
    return validation;
  }

  @Override
  protected void endAnalysis(String validation, UploaderAnalysis uploaderAnalysis) {
    if (rom != null) {
      //TODO Question here, seems that rom is not mandatory for altsound upload
      // but if rom is mandatory, simply return an error message 
      romLabel.setText(rom);
      GameRepresentation gameRepresentation = Studio.client.getGameService().getFirstGameByRom(rom);
      if (gameRepresentation != null) {
        tableLabel.setText(gameRepresentation.getGameDisplayName());
      }
    }
    super.endAnalysis(validation, uploaderAnalysis);
  }
}
