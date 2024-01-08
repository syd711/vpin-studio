package de.mephisto.vpin.ui.tables.dialogs;

import de.mephisto.vpin.commons.fx.DialogController;
import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.restclient.games.GameRepresentation;
import de.mephisto.vpin.ui.tables.TablesSidebarController;
import de.mephisto.vpin.ui.util.ProgressDialog;
import de.mephisto.vpin.ui.util.ProgressResultModel;
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

public class PupPackUploadController implements Initializable, DialogController {
  private final static Logger LOG = LoggerFactory.getLogger(PupPackUploadController.class);

  private static File lastFolderSelection;

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
        PupPackUploadProgressModel model = new PupPackUploadProgressModel(tablesSidebarController, this.game.getId(), "PUP Pack Upload", selection, "puppack");
        ProgressDialog.createProgressDialog(model);
      });
    }
  }

  @FXML
  private void onFileSelect(ActionEvent e) {
    Stage stage = (Stage) ((Button) e.getSource()).getScene().getWindow();

    this.uploadBtn.setDisable(true);

    FileChooser fileChooser = new FileChooser();
    fileChooser.setTitle("Select PUP Pack");
    fileChooser.getExtensionFilters().addAll(
        new FileChooser.ExtensionFilter("PUP Pack", "*.zip"));

    if (PupPackUploadController.lastFolderSelection != null) {
      fileChooser.setInitialDirectory(PupPackUploadController.lastFolderSelection);
    }

    this.selection = fileChooser.showOpenDialog(stage);
    if (this.selection != null) {
      refreshSelection(stage);
    }
  }

  private void refreshSelection(Stage stage) {
    PupPackUploadController.lastFolderSelection = this.selection.getParentFile();
    this.fileNameField.setText("Analyzing \"" + selection.getName() + "\", please wait...");
    this.fileNameField.setDisable(true);
    this.fileBtn.setDisable(true);
    this.cancelBtn.setDisable(true);


    ProgressResultModel resultModel = ProgressDialog.createProgressDialog(new PupPackAnalyzeProgressModel(this.game.getRom(), this.game.getTableName(), "PUP Pack Analysis", this.selection));

    if (!resultModel.getResults().isEmpty()) {
      result = false;
      WidgetFactory.showAlert(stage, String.valueOf(resultModel.getResults().get(0)));
      this.fileNameField.setText("");
      this.fileBtn.setDisable(false);
      this.fileNameField.setDisable(false);
      this.cancelBtn.setDisable(false);
    }
    else {
      this.fileNameField.setText(this.selection.getAbsolutePath());
      this.fileNameField.setDisable(false);
      this.fileBtn.setDisable(false);
      this.cancelBtn.setDisable(false);
      this.uploadBtn.setDisable(false);
      this.cancelBtn.setDisable(false);
    }
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
    this.titleLabel.setText("Select PUP pack for \"" + game.getGameDisplayName() + "\":");
  }

  public void setTableSidebarController(TablesSidebarController tablesSidebarController) {
    this.tablesSidebarController = tablesSidebarController;
  }

  public void setFile(File file, Stage stage) {
    this.selection = file;
    if(selection != null) {
      Platform.runLater(() -> {
        refreshSelection(stage);
      });
    }
  }
}
