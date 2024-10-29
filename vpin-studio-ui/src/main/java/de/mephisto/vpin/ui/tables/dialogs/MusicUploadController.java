package de.mephisto.vpin.ui.tables.dialogs;

import de.mephisto.vpin.restclient.util.PackageUtil;
import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.restclient.assets.AssetType;
import de.mephisto.vpin.restclient.util.UploaderAnalysis;
import de.mephisto.vpin.ui.tables.UploadAnalysisDispatcher;
import de.mephisto.vpin.ui.util.UploadProgressModel;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

public class MusicUploadController extends BaseUploadController {
  private final static Logger LOG = LoggerFactory.getLogger(MusicUploadController.class);

  @FXML
  private Label targetFolderLabel;

  public MusicUploadController() {
    super("Music Bundle", false, true, PackageUtil.ARCHIVE_SUFFIXES);
  }

  @Override
  protected UploadProgressModel createUploadModel() {
    return new MusicUploadProgressModel("Music Upload", getSelection(), finalizer);
  }
 
  public void setFile(Stage stage, File file, UploaderAnalysis<?> analysis, Runnable finalizer) {
    super.setFile(stage, file, finalizer);
    if (file != null) {
      if(analysis == null) {
        analysis = UploadAnalysisDispatcher.analyzeArchive(file);
        try {
          analysis.analyze();
        } catch (IOException e) {
          Platform.runLater(() -> {
            WidgetFactory.showAlert(stage, "Error" , "Failed to analyze music bundle: " + e.getMessage());
          });
        }
      }

      refreshSelection();
    }
  }

  protected void refreshSelection() {
    File selection = getSelection();

    this.targetFolderLabel.setText("-");
    if (selection != null && selection.exists()) {
      try {
        UploaderAnalysis<?> analysis = UploadAnalysisDispatcher.analyzeArchive(selection);
        String analyze = analysis.validateAssetType(AssetType.MUSIC_BUNDLE);
        if (analyze == null) {
          String relativeMusicPath = analysis.getRelativeMusicPath(true);
          File musicFolder= new File(getSelectedEmulator().getTablesDirectory(), "Music");
          File targetFolder = new File(musicFolder, relativeMusicPath);
          this.targetFolderLabel.setText(targetFolder.getAbsolutePath());
          this.fileNameField.setText(selection.getAbsolutePath());
          this.uploadBtn.setDisable(false);
        } else {
          WidgetFactory.showAlert(stage, "Error", analyze);
        }
      } catch (Exception e) {
        LOG.error("Music bundle analysis failed: " + e.getMessage(), e);
        WidgetFactory.showAlert(stage, "Error", "Music bundle analysis failed: " + e.getMessage());
      }
    }
  }
}
