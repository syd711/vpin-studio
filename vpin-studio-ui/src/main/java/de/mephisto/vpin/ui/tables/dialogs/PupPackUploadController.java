package de.mephisto.vpin.ui.tables.dialogs;

import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.restclient.assets.AssetType;
import de.mephisto.vpin.restclient.games.GameRepresentation;
import de.mephisto.vpin.restclient.util.UploaderAnalysis;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.tables.UploadAnalysisDispatcher;
import de.mephisto.vpin.ui.util.UploadProgressModel;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.io.File;

public class PupPackUploadController extends BaseUploadController {

  @FXML
  private Button cancelBtn;

  @FXML
  private Button fileBtn;

  @FXML
  private Label tableLabel;

  @FXML
  private Label romLabel;

  public PupPackUploadController() {
    super("PUP Pack", false, false, "zip", "rar", "7z");
  } 

  @Override 
  protected UploadProgressModel createUploadModel() {
    return new PupPackUploadProgressModel(null, "PUP Pack Upload", getSelection(), finalizer);
  }



  @Override
  protected void refreshSelection() {
    File selection = getSelection();

    this.fileNameField.setText("Analyzing \"" + selection.getName() + "\", please wait...");
    this.fileNameField.setDisable(true);
    this.fileBtn.setDisable(true);
    this.cancelBtn.setDisable(true);


    UploaderAnalysis<?> analysis = UploadAnalysisDispatcher.analyzeArchive(selection);
    refreshMatchingGame(analysis);
    String analyze = analysis.validateAssetType(AssetType.PUP_PACK);

    if (analyze == null) {
      this.fileNameField.setText(selection.getAbsolutePath());
      this.fileNameField.setDisable(false);
      this.fileBtn.setDisable(false);
      this.cancelBtn.setDisable(false);
      this.uploadBtn.setDisable(false);
    }
    else {
      result = false;
      WidgetFactory.showAlert(stage, "Invalid Pup Pack", analyze);
      this.fileNameField.setText("");
      this.fileBtn.setDisable(false);
      this.fileNameField.setDisable(false);
      this.cancelBtn.setDisable(false);
    }
  }

  public void setFile(Stage stage, File file, UploaderAnalysis<?> analysis, Runnable finaliser) {
    super.setFile(stage, file, finaliser);
    if (file != null) {
      refreshMatchingGame(analysis);
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

    String rom = uploaderAnalysis.getRomFromPupPack();
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
