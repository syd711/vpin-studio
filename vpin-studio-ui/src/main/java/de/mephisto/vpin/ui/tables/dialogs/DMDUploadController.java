package de.mephisto.vpin.ui.tables.dialogs;

import de.mephisto.vpin.commons.fx.DialogController;
import de.mephisto.vpin.restclient.util.PackageUtil;
import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.restclient.assets.AssetType;
import de.mephisto.vpin.restclient.games.GameEmulatorRepresentation;
import de.mephisto.vpin.restclient.games.GameRepresentation;
import de.mephisto.vpin.restclient.util.UploaderAnalysis;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.tables.UploadAnalysisDispatcher;
import de.mephisto.vpin.ui.util.FileSelectorDragEventHandler;
import de.mephisto.vpin.ui.util.FileSelectorDropEventHandler;
import de.mephisto.vpin.ui.util.ProgressDialog;
import de.mephisto.vpin.ui.util.StudioFileChooser;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class DMDUploadController implements Initializable, DialogController {
  private final static Logger LOG = LoggerFactory.getLogger(DMDUploadController.class);

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

  @FXML
  private ComboBox<GameEmulatorRepresentation> emulatorCombo;

  private File selection;
  private Stage stage;

  private GameEmulatorRepresentation emulatorRepresentation;

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
      stage.close();

      Platform.runLater(() -> {
        DMDUploadProgressModel model = new DMDUploadProgressModel("DMD Bundle Upload", selection, this.emulatorCombo.getValue().getId(), game);
        ProgressDialog.createProgressDialog(model);
      });
    }
  }

  @FXML
  private void onFileSelect(ActionEvent e) {
    Stage stage = (Stage) ((Button) e.getSource()).getScene().getWindow();

    this.uploadBtn.setDisable(true);

    StudioFileChooser fileChooser = new StudioFileChooser();
    fileChooser.setTitle("Select DMD Bundle");
    fileChooser.getExtensionFilters().addAll(
        new FileChooser.ExtensionFilter("DMD Bundle", "*.zip", "*.rar", "*.7z"));
    this.selection = fileChooser.showOpenDialog(stage);
    if (this.selection != null) {
      refreshSelection(stage);
    }
  }

  private void refreshSelection(Stage stage) {
    this.fileNameField.setText("Analyzing \"" + selection.getName() + "\", please wait...");
    this.fileNameField.setDisable(true);
    this.fileBtn.setDisable(true);
    this.cancelBtn.setDisable(true);


    String analyze = UploadAnalysisDispatcher.validateArchive(selection, AssetType.DMD_PACK);

    if (analyze != null) {
      WidgetFactory.showAlert(stage, analyze);
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
    root.setOnDragOver(new FileSelectorDragEventHandler(root, PackageUtil.ARCHIVE_SUFFIXES));
    root.setOnDragDropped(new FileSelectorDropEventHandler(fileNameField, file -> {
      selection = file;
      refreshSelection(stage);
    }));

    this.selection = null;
    this.uploadBtn.setDisable(true);

    List<GameEmulatorRepresentation> gameEmulators = Studio.client.getFrontendService().getVpxGameEmulators();
    emulatorRepresentation = gameEmulators.get(0);
    ObservableList<GameEmulatorRepresentation> emulators = FXCollections.observableList(gameEmulators);
    emulatorCombo.setItems(emulators);
    emulatorCombo.setValue(emulatorRepresentation);
    emulatorCombo.valueProperty().addListener((observableValue, gameEmulatorRepresentation, t1) -> {
      emulatorRepresentation = t1;
    });
  }

  @Override
  public void onDialogCancel() {

  }

  public void setData(GameRepresentation game, UploaderAnalysis analysis, File file, Stage stage) {
    this.game = game;
    this.selection = file;
    this.stage = stage;
    if (selection != null) {
      if (analysis != null) {
        this.fileNameField.setText(this.selection.getAbsolutePath());
        this.fileNameField.setDisable(false);
        this.fileBtn.setDisable(false);
        this.cancelBtn.setDisable(false);
        this.uploadBtn.setDisable(false);
        this.cancelBtn.setDisable(false);
      }
      else {
        refreshSelection(stage);
      }
    }
  }
}
