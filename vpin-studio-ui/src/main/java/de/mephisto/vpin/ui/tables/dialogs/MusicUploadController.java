package de.mephisto.vpin.ui.tables.dialogs;

import de.mephisto.vpin.restclient.util.PackageUtil;
import de.mephisto.vpin.restclient.assets.AssetType;
import de.mephisto.vpin.restclient.util.UploaderAnalysis;
import de.mephisto.vpin.ui.util.UploadProgressModel;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

import java.io.File;
import java.io.IOException;

public class MusicUploadController extends BaseUploadController {

  @FXML
  private Label targetFolderLabel;

  public MusicUploadController() {
    super(AssetType.MUSIC_BUNDLE, false, true, PackageUtil.ARCHIVE_SUFFIXES);
  }

  @Override
  protected UploadProgressModel createUploadModel() {
    return new MusicUploadProgressModel("Music Upload", getSelection());
  }

  @Override
  protected void startAnalysis() {
    this.targetFolderLabel.setText("-");
  }

  @Override
  protected String validateAnalysis(UploaderAnalysis analysis) {
    // first check first
    try {
      analysis.analyze();
    } catch (IOException e) {
      return "Failed to analyze music bundle: " + e.getMessage();
    }
    
    String analyze = analysis.validateAssetTypeInArchive(AssetType.MUSIC_BUNDLE);
    if (analyze == null) {
      String relativeMusicPath = analysis.getRelativeMusicPath(true);
      File musicFolder= new File(getSelectedEmulator().getInstallationDirectory(), "Music");
      File targetFolder = new File(musicFolder, relativeMusicPath);
      this.targetFolderLabel.setText(targetFolder.getAbsolutePath());
    }
    return analyze;
  }
}
