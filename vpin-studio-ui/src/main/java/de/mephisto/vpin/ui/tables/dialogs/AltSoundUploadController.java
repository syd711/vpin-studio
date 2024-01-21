package de.mephisto.vpin.ui.tables.dialogs;

import de.mephisto.vpin.commons.fx.DialogController;
import de.mephisto.vpin.commons.utils.AltSoundArchiveAnalyzer;
import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.restclient.games.GameRepresentation;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.tables.TablesSidebarController;
import de.mephisto.vpin.ui.util.ProgressDialog;
import de.mephisto.vpin.ui.util.StudioFileChooser;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

public class AltSoundUploadController implements Initializable, DialogController {
  private final static Logger LOG = LoggerFactory.getLogger(AltSoundUploadController.class);

  @FXML
  private TextField fileNameField;

  @FXML
  private Button uploadBtn;

  @FXML
  private Button cancelBtn;

  @FXML
  private Button fileBtn;

  @FXML
  private Label titleLabel;

  private File selection;

  private boolean result = false;
  private GameRepresentation game;
  private TablesSidebarController tablesSidebarController;

  @FXML
  private void onCancelClick(ActionEvent e) {
    Stage stage = (Stage) ((Button) e.getSource()).getScene().getWindow();
    stage.close();
  }

  @FXML
  private void onUploadClick(ActionEvent event) {
    Stage stage = (Stage) ((Button) event.getSource()).getScene().getWindow();
    if (selection != null && selection.exists()) {
      result = false;
      stage.close();

      Platform.runLater(() -> {
        AltSoundUploadProgressModel model = new AltSoundUploadProgressModel(tablesSidebarController, this.game.getId(), "ALT Sound Upload", selection, "altsound");
        ProgressDialog.createProgressDialog(model);
      });
    }
  }

  @FXML
  private void onFileSelect(ActionEvent event) {
    Stage stage = (Stage) ((Button) event.getSource()).getScene().getWindow();

    this.uploadBtn.setDisable(true);

    StudioFileChooser fileChooser = new StudioFileChooser();
    fileChooser.setTitle("Select ALT Sound");
    fileChooser.getExtensionFilters().addAll(
        new FileChooser.ExtensionFilter("ALT Sound Package", "*.zip", "*.rar"));

    this.selection = fileChooser.showOpenDialog(stage);
    this.uploadBtn.setDisable(selection == null);
    if (this.selection != null) {
      refreshSelection();
    }
    else {
      this.fileNameField.setText("");
    }
  }

  private void refreshSelection() {
    this.uploadBtn.setDisable(selection == null);

    this.fileNameField.setText("Analyzing \"" + selection.getName() + "\"...");
    this.fileNameField.setDisable(true);
    this.fileBtn.setDisable(true);
    this.cancelBtn.setDisable(true);


    Platform.runLater(() -> {
      String analyze = AltSoundArchiveAnalyzer.analyze(selection);
      this.fileNameField.setText(this.selection.getAbsolutePath());
      this.fileNameField.setDisable(false);
      this.fileBtn.setDisable(false);
      this.cancelBtn.setDisable(false);

      if (analyze != null) {
        result = false;
        WidgetFactory.showAlert(Studio.stage, analyze);
        return;
      }
      this.uploadBtn.setDisable(false);
    });
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    this.result = false;
    this.selection = null;
    this.uploadBtn.setDisable(true);
  }

  @Override
  public void onDialogCancel() {
    result = false;
  }

  public boolean uploadFinished() {
    return result;
  }

  public void setGame(GameRepresentation game) {
    this.game = game;
    this.titleLabel.setText("Select ALT sound package for \"" + game.getGameDisplayName() + "\":");
  }

  public void setTableSidebarController(TablesSidebarController tablesSidebarController) {
    this.tablesSidebarController = tablesSidebarController;
  }

  public void setFile(File file) {
    this.selection = file;
    if(selection != null) {
      Platform.runLater(() -> {
        refreshSelection();
      });
    }
  }
}
