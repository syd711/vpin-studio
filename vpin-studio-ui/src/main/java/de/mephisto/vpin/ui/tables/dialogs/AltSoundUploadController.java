package de.mephisto.vpin.ui.tables.dialogs;

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

  private String rom;
  
  public AltSoundUploadController() {
    super(AssetType.ALT_SOUND, false, true, PackageUtil.ARCHIVE_SUFFIXES);
  }

  protected UploadProgressModel createUploadModel() {
    return new AltSoundUploadProgressModel(game != null ? game.getId() : -1, "ALT Sound Upload", 
      getSelection(), getSelectedEmulatorId(), rom, finalizer);
  }

  public void setData(Stage stage, File file, GameRepresentation game, UploaderAnalysis uploaderAnalysis, Runnable finalizer) {
    this.game = game;
    super.setFile(stage, file, uploaderAnalysis, finalizer);
  }

  @Override
  protected void startAnalysis() {
    tableLabel.setText("-");
    romLabel.setText("-");  
  }

  @Override
  protected String validateAnalysis(UploaderAnalysis uploaderAnalysis) {
    rom = uploaderAnalysis.getRomFromAltSoundPack();
    return super.validateAnalysis(uploaderAnalysis);
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
