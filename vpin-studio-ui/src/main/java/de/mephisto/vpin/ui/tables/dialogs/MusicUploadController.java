package de.mephisto.vpin.ui.tables.dialogs;

import de.mephisto.vpin.commons.fx.DialogController;
import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.restclient.assets.AssetType;
import de.mephisto.vpin.restclient.games.GameEmulatorRepresentation;
import de.mephisto.vpin.restclient.util.UploaderAnalysis;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.tables.UploadAnalysisDispatcher;
import de.mephisto.vpin.ui.util.ProgressDialog;
import de.mephisto.vpin.ui.util.StudioFileChooser;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

import static de.mephisto.vpin.ui.Studio.stage;

public class MusicUploadController implements Initializable, DialogController {
  private final static Logger LOG = LoggerFactory.getLogger(MusicUploadController.class);

  @FXML
  private TextField fileNameField;

  @FXML
  private Button uploadBtn;

  @FXML
  private Label targetFolderLabel;

  @FXML
  private ComboBox<GameEmulatorRepresentation> emulatorCombo;

  private GameEmulatorRepresentation emulatorRepresentation;

  private File selection;
  private UploaderAnalysis analysis;

  @FXML
  private void onCancelClick(ActionEvent e) {
    Stage stage = (Stage) ((Button) e.getSource()).getScene().getWindow();
    stage.close();
  }

  @FXML
  private void onUploadClick(ActionEvent event) {
    if (selection != null && selection.exists()) {
      try {
        Platform.runLater(() -> {
          Stage stage = (Stage) ((Button) event.getSource()).getScene().getWindow();
          stage.close();
        });
        MusicUploadProgressModel model = new MusicUploadProgressModel("Music Upload", selection);
        ProgressDialog.createProgressDialog(model);
      } catch (Exception e) {
        LOG.error("Upload failed: " + e.getMessage(), e);
        WidgetFactory.showAlert(stage, "Uploading Music Failed", "Please check the log file for details", "Error: " + e.getMessage());
      }
    }
  }

  @FXML
  private void onFileSelect(ActionEvent event) {
    Stage stage = (Stage) ((Button) event.getSource()).getScene().getWindow();

    StudioFileChooser fileChooser = new StudioFileChooser();
    fileChooser.setTitle("Select Music Archive");
    fileChooser.getExtensionFilters().addAll(
        new FileChooser.ExtensionFilter("Music Bundle", "*.zip"));

    this.targetFolderLabel.setText("-");
    this.selection = fileChooser.showOpenDialog(stage);
    if (this.selection != null && this.selection.exists()) {
      try {
        analysis = UploadAnalysisDispatcher.analyzeArchive(this.selection);
        String analyze = analysis.validateAssetType(AssetType.MUSIC_BUNDLE);
        if (analyze == null) {
          this.refreshSelection(this.selection);
        } else {
          WidgetFactory.showAlert(Studio.stage, "Error", analyze);
        }

      } catch (Exception e) {
        LOG.error("Music bundle analysis failed: " + e.getMessage(), e);
        WidgetFactory.showAlert(Studio.stage, "Error", "Music bundle analysis failed: " + e.getMessage());
      }
    }
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    this.selection = null;

    this.uploadBtn.setDisable(true);
    this.fileNameField.textProperty().addListener((observableValue, s, t1) -> uploadBtn.setDisable(StringUtils.isEmpty(t1)));

    List<GameEmulatorRepresentation> gameEmulators = Studio.client.getPinUPPopperService().getVpxGameEmulators();
    emulatorRepresentation = gameEmulators.get(0);
    ObservableList<GameEmulatorRepresentation> emulators = FXCollections.observableList(gameEmulators);
    emulatorCombo.setItems(emulators);
    emulatorCombo.setValue(emulatorRepresentation);
    emulatorCombo.valueProperty().addListener((observableValue, gameEmulatorRepresentation, t1) -> {
      emulatorRepresentation = t1;
      refreshSelection(this.selection);
    });
  }

  @Override
  public void onDialogCancel() {

  }

  public void setFile(Stage stage, File file, UploaderAnalysis analysis) {
    this.analysis = analysis;
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

      refreshSelection(file);
    }
  }

  private void refreshSelection(File file) {
    this.selection = file;

    String relativeMusicPath = analysis.getRelativeMusicPath(true);
    File musicFolder= new File(emulatorRepresentation.getTablesDirectory(), "Music");
    File targetFolder = new File(musicFolder, relativeMusicPath);
    this.targetFolderLabel.setText(targetFolder.getAbsolutePath());
    this.fileNameField.setText(file.getAbsolutePath());
    this.uploadBtn.setDisable(false);
  }
}
