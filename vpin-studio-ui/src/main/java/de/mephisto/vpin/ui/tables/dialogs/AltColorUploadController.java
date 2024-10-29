package de.mephisto.vpin.ui.tables.dialogs;

import de.mephisto.vpin.restclient.util.PackageUtil;
import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.restclient.assets.AssetType;
import de.mephisto.vpin.restclient.games.GameRepresentation;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.tables.UploadAnalysisDispatcher;
import de.mephisto.vpin.ui.util.UploadProgressModel;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import org.apache.commons.io.FilenameUtils;

import java.io.File;

public class AltColorUploadController extends BaseUploadController {

  @FXML
  private Button cancelBtn;

  @FXML
  private Button fileBtn;

  private GameRepresentation game;

  public AltColorUploadController() {
    super("ALT Color (Package)", false, false, "zip", "rar", "pac", "vni", "pal", "cRZ");
  }

  public void setGame(GameRepresentation game) {
    this.game = game;
  }

  @Override
  protected UploadProgressModel createUploadModel() {
    return new AltColorUploadProgressModel(this.game.getId(), "ALT Color Upload", getSelection(), "altcolor", finalizer);
  }

  @Override
  protected void refreshSelection() {
    File selection = getSelection();

    this.uploadBtn.setDisable(selection == null);
    if (selection != null) {
      String suffix = FilenameUtils.getExtension(selection.getName());
      if (PackageUtil.isSupportedArchive(suffix)) {
        this.fileNameField.setText("Analyzing \"" + selection.getName() + "\"...");
        this.fileNameField.setDisable(true);
        this.fileBtn.setDisable(true);
        this.cancelBtn.setDisable(true);

        Platform.runLater(() -> {
          String analyze = UploadAnalysisDispatcher.validateArchive(selection, AssetType.ALT_COLOR);
          this.fileNameField.setText(selection.getAbsolutePath());
          this.fileNameField.setDisable(false);
          this.fileBtn.setDisable(false);
          this.cancelBtn.setDisable(false);

          if (analyze != null) {
            this.fileNameField.setText("");
            this.uploadBtn.setDisable(true);
            result = false;
            WidgetFactory.showAlert(Studio.stage, analyze);
            return;
          }
          this.uploadBtn.setDisable(false);

        });
      }
      else {
        this.fileNameField.setText(selection.getAbsolutePath());
        this.fileBtn.setDisable(false);
        this.cancelBtn.setDisable(false);
      }
    }
  }
}
