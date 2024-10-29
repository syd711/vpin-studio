package de.mephisto.vpin.ui.tables.dialogs;

import de.mephisto.vpin.restclient.util.PackageUtil;
import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.restclient.assets.AssetType;
import de.mephisto.vpin.restclient.games.GameRepresentation;
import de.mephisto.vpin.restclient.util.UploaderAnalysis;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.tables.UploadAnalysisDispatcher;
import de.mephisto.vpin.ui.util.UploadProgressModel;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

public class AltSoundUploadController extends BaseUploadController {
  private final static Logger LOG = LoggerFactory.getLogger(AltSoundUploadController.class);


  @FXML
  private Button cancelBtn;

  @FXML
  private Button fileBtn;

  @FXML
  private Label tableLabel;

  @FXML
  private Label romLabel;

  private UploaderAnalysis<?> uploaderAnalysis;

  private GameRepresentation game;
  
  public AltSoundUploadController() {
    super("ALT Sound Package", false, true, PackageUtil.ARCHIVE_SUFFIXES);
  }

  protected UploadProgressModel createUploadModel() {
    return new AltSoundUploadProgressModel(game != null ? game.getId() : -1, "ALT Sound Upload", 
      getSelection(), getSelectedEmulatorId(), uploaderAnalysis.getRomFromAltSoundPack(), finalizer);
  }

  @Override
  protected void refreshSelection() {
    File selection = getSelection();

    this.uploadBtn.setDisable(selection == null);

    this.fileNameField.setText("Analyzing \"" + selection.getName() + "\"...");
    this.fileNameField.setDisable(true);
    this.fileBtn.setDisable(true);
    this.cancelBtn.setDisable(true);


    Platform.runLater(() -> {
      uploaderAnalysis = UploadAnalysisDispatcher.analyzeArchive(selection);
      refreshMatchingGame(uploaderAnalysis);
      String validation = uploaderAnalysis.validateAssetType(AssetType.ALT_SOUND);

      if (validation != null) {
        WidgetFactory.showAlert(stage, "Invalid ALT Sound Pack", validation);
        this.fileNameField.setText("");
        this.fileBtn.setDisable(false);
        this.fileNameField.setDisable(false);
        this.cancelBtn.setDisable(false);
      }
      else {
        this.fileNameField.setText(selection.getAbsolutePath());
        this.fileNameField.setDisable(false);
        this.fileBtn.setDisable(false);
        this.cancelBtn.setDisable(false);
        this.uploadBtn.setDisable(false);
      }
    });
  }

  public void setData(Stage stage, File file, GameRepresentation game, UploaderAnalysis<?> uploaderAnalysis, Runnable finalizer) {
    super.setFile(stage, file, finalizer);
    this.uploaderAnalysis = uploaderAnalysis;
    this.game = game;

    if (file != null) {
      refreshMatchingGame(uploaderAnalysis);
      this.fileNameField.setText(file.getAbsolutePath());
      this.fileNameField.setDisable(false);
      this.fileBtn.setDisable(false);
      this.cancelBtn.setDisable(false);
      this.uploadBtn.setDisable(false);
    }
  }


  private void refreshMatchingGame(UploaderAnalysis<?> uploaderAnalysis) {
    tableLabel.setText("-");
    romLabel.setText("-");
    this.uploadBtn.setDisable(true);
    if (uploaderAnalysis == null) {
      return;
    }

    String rom = uploaderAnalysis.getRomFromAltSoundPack();
    if (rom == null) {
      return;
    }
    romLabel.setText(rom);
    this.uploadBtn.setDisable(false);

    GameRepresentation gameRepresentation = Studio.client.getGameService().getFirstGameByRom(rom);
    if (gameRepresentation != null) {
      tableLabel.setText(gameRepresentation.getGameDisplayName());
    }
  }
}
