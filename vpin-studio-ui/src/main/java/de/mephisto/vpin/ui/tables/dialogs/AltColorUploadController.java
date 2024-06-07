package de.mephisto.vpin.ui.tables.dialogs;

import de.mephisto.vpin.commons.fx.DialogController;
import de.mephisto.vpin.commons.utils.PackageUtil;
import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.restclient.assets.AssetType;
import de.mephisto.vpin.restclient.games.GameRepresentation;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.tables.TablesSidebarController;
import de.mephisto.vpin.ui.tables.UploadAnalysisDispatcher;
import de.mephisto.vpin.ui.util.FileSelectorDragEventHandler;
import de.mephisto.vpin.ui.util.FileSelectorDropEventHandler;
import de.mephisto.vpin.ui.util.ProgressDialog;
import de.mephisto.vpin.ui.util.StudioFileChooser;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

public class AltColorUploadController implements Initializable, DialogController {
  private final static Logger LOG = LoggerFactory.getLogger(AltColorUploadController.class);

  @FXML
  private Node root;

  @FXML
  private TextField fileNameField;

  @FXML
  private Button uploadBtn;

  @FXML
  private Button cancelBtn;

  @FXML
  private Button fileBtn;

  private File selection;

  private boolean result = false;
  private GameRepresentation game;

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
        AltColorUploadProgressModel model = new AltColorUploadProgressModel(this.game.getId(), "ALT Color Upload", selection, "altcolor");
        ProgressDialog.createProgressDialog(model);
      });
    }
  }

  @FXML
  private void onFileSelect(ActionEvent event) {
    Stage stage = (Stage) ((Button) event.getSource()).getScene().getWindow();

    this.uploadBtn.setDisable(true);

    StudioFileChooser fileChooser = new StudioFileChooser();
    fileChooser.setTitle("Select ALT Color");
    fileChooser.getExtensionFilters().addAll(
        new FileChooser.ExtensionFilter("ALT Color (Package)", "*.zip", "*.pac", "*.vni", "*.pal", "*.cRZ"));

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

    String suffix = FilenameUtils.getExtension(selection.getName());
    if (PackageUtil.isSupportedArchive(suffix)) {
      this.fileNameField.setText("Analyzing \"" + selection.getName() + "\"...");
      this.fileNameField.setDisable(true);
      this.fileBtn.setDisable(true);
      this.cancelBtn.setDisable(true);

      Platform.runLater(() -> {
        String analyze = UploadAnalysisDispatcher.validateArchive(selection, AssetType.ALT_COLOR);
        this.fileNameField.setText(this.selection.getAbsolutePath());
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
      this.fileNameField.setText(this.selection.getAbsolutePath());
      this.fileBtn.setDisable(false);
      this.cancelBtn.setDisable(false);
    }
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    this.result = false;
    this.selection = null;
    this.uploadBtn.setDisable(true);

    root.setOnDragOver(new FileSelectorDragEventHandler(root, "zip", "pac", "vni", "pal", "cRZ"));
    root.setOnDragDropped(new FileSelectorDropEventHandler(fileNameField, file -> {
      selection = file;
      refreshSelection();
    }));
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
  }

  public void setFile(File file) {
    this.selection = file;
    if (selection != null) {
      Platform.runLater(() -> {
        refreshSelection();
      });
    }
  }
}
