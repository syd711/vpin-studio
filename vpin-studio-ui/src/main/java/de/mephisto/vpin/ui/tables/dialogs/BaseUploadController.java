package de.mephisto.vpin.ui.tables.dialogs;

import de.mephisto.vpin.commons.fx.DialogController;
import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.restclient.assets.AssetType;
import de.mephisto.vpin.restclient.emulators.GameEmulatorRepresentation;
import de.mephisto.vpin.restclient.util.UploaderAnalysis;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.tables.UploadAnalysisDispatcher;
import de.mephisto.vpin.ui.util.FileSelectorDragEventHandler;
import de.mephisto.vpin.ui.util.FilesSelectorDropEventHandler;
import de.mephisto.vpin.ui.util.ProgressDialog;
import de.mephisto.vpin.ui.util.ProgressResultModel;
import de.mephisto.vpin.ui.util.StudioFileChooser;
import de.mephisto.vpin.ui.util.UploadProgressModel;
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

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public abstract class BaseUploadController implements Initializable, DialogController {
  private final static Logger LOG = LoggerFactory.getLogger(BaseUploadController.class);

  @FXML
  private Node root;

  @FXML
  private TextField fileNameField;

  @FXML
  private Button fileBtn;

  @FXML
  private Button cancelBtn;

  @FXML
  private Button uploadBtn;

  @FXML
  protected ComboBox<GameEmulatorRepresentation> emulatorCombo;

  protected GameEmulatorRepresentation emulator;

  private List<File> selection;

  protected Stage stage;

  private AssetType assetType;

  private boolean multiSelection;

  private boolean useEmulators;

  private String[] suffixes;

  protected Runnable finalizer;


  protected BaseUploadController(AssetType assetType, boolean multiSelection, boolean useEmulators, String... suffixes) {
    this.assetType = assetType;
    this.multiSelection = multiSelection;
    this.useEmulators = useEmulators;
    this.suffixes = suffixes;
  }

  protected abstract UploadProgressModel createUploadModel();

  /**
   * Upload done, 
   */
  protected void onUploadDone(ProgressResultModel result) {
    if (finalizer != null) {
      finalizer.run();
    }
  }

  //--------------

  @FXML
  private void onCancelClick(ActionEvent e) {
    Stage stage = (Stage) ((Button) e.getSource()).getScene().getWindow();
    stage.close();
  }

  @FXML
  protected void onUploadClick(ActionEvent event) {
    if (selection != null && !selection.isEmpty()) {
      try {
        Stage stage = (Stage) ((Button) event.getSource()).getScene().getWindow();
        stage.close();

        UploadProgressModel model = createUploadModel();
        if (model == null) {
          // ProgressModel not created, then upload is cancelled
          return;
        }

        Platform.runLater(() -> {
          ProgressResultModel result = ProgressDialog.createProgressDialog(model);
          onUploadDone(result);
        });
      }
      catch (Exception e) {
        LOG.error("Upload failed: " + e.getMessage(), e);
        WidgetFactory.showAlert(Studio.stage, "Uploading " + assetType.toString() + " failed", "Please check the log file for details", "Error: " + e.getMessage());
      }
    }
  }

  @FXML
  private void onFileSelect(ActionEvent event) {
    Stage stage = (Stage) ((Button) event.getSource()).getScene().getWindow();

    this.uploadBtn.setDisable(true);
    StudioFileChooser fileChooser = new StudioFileChooser();
    fileChooser.setTitle("Select " + assetType.toString());

    String[] extensions = new String[suffixes.length];
    for (int i = 0; i < suffixes.length; i++) {
      extensions[i] = "*." + suffixes[i];
    }
    fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter(assetType.toString(), extensions));

    if (multiSelection) {
      this.selection = fileChooser.showOpenMultipleDialog(stage);
    }
    else {
      File file = fileChooser.showOpenDialog(stage);
      if (file != null) {
        this.selection = Arrays.asList(file);
      }
    }

    if (selection != null) {
      refreshSelection(null);
    }
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    this.selection = null;

    this.uploadBtn.setDisable(true);

    if (useEmulators) {
      this.fileNameField.textProperty().addListener((observableValue, s, t1) -> uploadBtn.setDisable(StringUtils.isEmpty(t1)));

      refreshEmulators();
    }

    root.setOnDragOver(new FileSelectorDragEventHandler(root, multiSelection, suffixes));
    root.setOnDragDropped(new FilesSelectorDropEventHandler(fileNameField, files -> {
      selection = files;
      refreshSelection(null);
    }));
  }

  protected void refreshEmulators() {
    List<GameEmulatorRepresentation> gameEmulators = Studio.client.getEmulatorService().getVpxGameEmulators();
    emulator = gameEmulators.get(0);
    ObservableList<GameEmulatorRepresentation> emulators = FXCollections.observableList(gameEmulators);
    emulatorCombo.setItems(emulators);
    emulatorCombo.setValue(emulator);
    emulatorCombo.valueProperty().addListener((observableValue, gameEmulatorRepresentation, t1) -> {
      emulator = t1;
      refreshSelection(null);
    });
  }

  @Override
  public void onDialogCancel() {

  }

  public void setFile(Stage stage, File file, UploaderAnalysis analysis, Runnable finalizer) {
    this.stage = stage;
    this.finalizer = finalizer;
    if (file != null) {
      this.selection = Arrays.asList(file);
      refreshSelection(analysis);
    }
  }

  public GameEmulatorRepresentation getSelectedEmulator() {
    return emulator;
  }

  public int getSelectedEmulatorId() {
    return emulator != null ? emulator.getId() : -1;
  }

  public File getSelection() {
    return this.selection != null && !this.selection.isEmpty() ? selection.get(0) : null;
  }

  public List<File> getSelections() {
    return this.selection;
  }

  protected void refreshSelection(UploaderAnalysis analysis) {
    this.uploadBtn.setDisable(true);

    if (this.selection != null && !this.selection.isEmpty()) {
      String validation = null;
      // No analysis provided, start one
      if (analysis == null) {
        startAnalysis();
        if (multiSelection && selection.size() != 1) {
          // no validation for multi-selections ?
        }
        else {
          File file = getSelection();
          if (file != null && UploadAnalysisDispatcher.isArchive(file)) {
            analysis = UploadAnalysisDispatcher.analyzeArchive(stage, file);
            if (analysis != null) {
              validation = validateAnalysis(analysis);
            }
          }
        }
      }
      else {
        if (analysis.isArchive()) {
          validation = validateAnalysis(analysis);
        }
      }
      endAnalysis(validation, analysis);
    }
    else {
      this.fileNameField.setText("");
    }
  }

  /**
   * Called after selection changed and before analysis start, to clean specific fields
   */
  protected void startAnalysis() {
    this.fileNameField.setText("Analyzing, please wait...");
    this.fileNameField.setDisable(true);
    this.fileBtn.setDisable(true);
    this.uploadBtn.setDisable(true);
    this.cancelBtn.setDisable(true);
  }

  /**
   * Perform the analysis, by default just validateAssetType
   */
  protected String validateAnalysis(UploaderAnalysis analysis) {
    return analysis.validateAssetTypeInArchive(assetType);
  }

  /**
   * Called after analysis is done on javafx thread to update specific fields
   *
   * @param analysis         The result of the analysis
   * @param uploaderAnalysis
   */
  protected void endAnalysis(String analysis, UploaderAnalysis uploaderAnalysis) {
    if (analysis == null) {
      String collect = multiSelection ?
          this.selection.stream().map(f -> f.getName()).collect(Collectors.joining(", ")) :
          getSelection().getAbsolutePath();

      this.fileNameField.setText(collect);
      this.uploadBtn.setDisable(false);
    }
    else {
      WidgetFactory.showAlert(stage, "Invalid " + assetType.toString(), analysis);
      this.fileNameField.setText("");
    }
    this.fileBtn.setDisable(false);
    this.fileNameField.setDisable(false);
    this.cancelBtn.setDisable(false);
  }
}
