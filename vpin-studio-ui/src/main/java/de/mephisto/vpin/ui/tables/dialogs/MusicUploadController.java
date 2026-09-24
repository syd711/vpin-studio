package de.mephisto.vpin.ui.tables.dialogs;

import de.mephisto.vpin.restclient.util.PackageUtil;
import de.mephisto.vpin.restclient.assets.AssetType;
import de.mephisto.vpin.restclient.emulators.GameEmulatorRepresentation;
import de.mephisto.vpin.restclient.games.GameRepresentation;
import de.mephisto.vpin.restclient.util.UploaderAnalysis;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.util.UploadProgressModel;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;

public class MusicUploadController extends BaseUploadController {

  @FXML
  private Label targetFolderLabel;
  private int gameId;

  public MusicUploadController() {
    super(AssetType.MUSIC_BUNDLE, false, true, PackageUtil.ARCHIVE_SUFFIXES);
  }

  @Override
  protected UploadProgressModel createUploadModel() {
    return new MusicUploadProgressModel("Music Upload", getSelection(), getSelectedEmulatorId(), gameId);
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
      String relativeMusicPath = analysis.getRelativeMusicPathWithoutMusicFolder();
      this.targetFolderLabel.setText(getTargetFolder(relativeMusicPath));
    }
    return analyze;
  }

  /**
   * Music is installed next to the table if the server keeps the assets per table, else into the emulator's Music folder.
   */
  private String getTargetFolder(String relativeMusicPath) {
    GameEmulatorRepresentation emulator = getSelectedEmulator();
    GameRepresentation game = gameId > 0 ? Studio.client.getGameService().getGameCached(gameId) : null;
    if (emulator.isPerTableFileStructure() && game != null && game.getGameFilePath() != null) {
      return StringUtils.substringBeforeLast(game.getGameFilePath().replace('\\', '/'), "/") + "/music/" + relativeMusicPath;
    }
    File musicFolder = new File(emulator.getInstallationDirectory(), "Music");
    return new File(musicFolder, relativeMusicPath).getAbsolutePath();
  }

  public void setGameId(int gameId) {
    this.gameId = gameId;
  }
}
