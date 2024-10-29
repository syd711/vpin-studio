package de.mephisto.vpin.ui.tables.dialogs;

import de.mephisto.vpin.commons.fx.DialogController;
import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.restclient.games.GameEmulatorRepresentation;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.util.FileSelectorDragEventHandler;
import de.mephisto.vpin.ui.util.FilesSelectorDropEventHandler;
import de.mephisto.vpin.ui.util.ProgressDialog;
import de.mephisto.vpin.ui.util.StudioFileChooser;
import de.mephisto.vpin.ui.util.UploadProgressModel;
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
  protected TextField fileNameField;

  @FXML
  protected Button uploadBtn;

  @FXML
  private ComboBox<GameEmulatorRepresentation> emulatorCombo;

  private GameEmulatorRepresentation emulator;

  private List<File> selection;

  protected Stage stage;

  protected boolean result = false;

  private String title;
  
  private boolean multiSelection;

  private boolean useEmulators;

  private String[] suffixes;

  protected Runnable finalizer;


  protected BaseUploadController(String title, boolean multiSelection, boolean useEmulators, String... suffixes) {
    this.title = title;
    this.multiSelection = multiSelection;
    this.useEmulators = useEmulators;
    this.suffixes = suffixes;
  }

  protected abstract UploadProgressModel createUploadModel();

  //--------------

  @FXML
  private void onCancelClick(ActionEvent e) {
    Stage stage = (Stage) ((Button) e.getSource()).getScene().getWindow();
    stage.close();
  }

  @FXML
  private void onUploadClick(ActionEvent event) {
    //SINGLE if (selection != null && selection.exists()) {
    if (selection != null && !selection.isEmpty()) {
      result = false;
      try {
        Stage stage = (Stage) ((Button) event.getSource()).getScene().getWindow();
        stage.close();

        UploadProgressModel model = createUploadModel();
        ProgressDialog.createProgressDialog(model);
      }
      catch (Exception e) {
        LOG.error("Upload failed: " + e.getMessage(), e);
        WidgetFactory.showAlert(Studio.stage, "Uploading " + title + " failed", "Please check the log file for details", "Error: " + e.getMessage());
      }
    }
  }

  @FXML
  private void onFileSelect(ActionEvent event) {
    Stage stage = (Stage) ((Button) event.getSource()).getScene().getWindow();

    this.uploadBtn.setDisable(true);
    StudioFileChooser fileChooser = new StudioFileChooser();
    fileChooser.setTitle("Select " + title);

    String[] extensions = new String[suffixes.length];
    for (int i = 0; i < suffixes.length; i++) {
      extensions[i] = "*." + suffixes[i];
    }
    fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter(title, extensions));

    if (multiSelection) {
      this.selection = fileChooser.showOpenMultipleDialog(stage);
    }
    else {
      this.selection = Arrays.asList(fileChooser.showOpenDialog(stage));
    }
 
    this.uploadBtn.setDisable(selection == null);
    refreshSelection();
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    this.result = false;
    this.selection = null;

    this.uploadBtn.setDisable(true);

    if (useEmulators) {
      this.fileNameField.textProperty().addListener((observableValue, s, t1) -> uploadBtn.setDisable(StringUtils.isEmpty(t1)));

      List<GameEmulatorRepresentation> gameEmulators = Studio.client.getFrontendService().getVpxGameEmulators();
      emulator = gameEmulators.get(0);
      ObservableList<GameEmulatorRepresentation> emulators = FXCollections.observableList(gameEmulators);
      emulatorCombo.setItems(emulators);
      emulatorCombo.setValue(emulator);
      emulatorCombo.valueProperty().addListener((observableValue, gameEmulatorRepresentation, t1) -> {
        emulator = t1;
        refreshSelection();
      });
    }

    root.setOnDragOver(new FileSelectorDragEventHandler(root, multiSelection, suffixes));
    root.setOnDragDropped(new FilesSelectorDropEventHandler(fileNameField, files -> {
      selection = files;
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

  public void setFile(Stage stage, File file, Runnable finalizer) {
    this.stage = stage;
    this.finalizer = finalizer;
    if(file != null) {
      this.selection = Arrays.asList(file);
      refreshSelection();
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

  protected void refreshSelection() {
    if (this.selection != null && !this.selection.isEmpty()) {
      String collect = this.selection.stream().map(f -> f.getName()).collect(Collectors.joining(", "));
      this.fileNameField.setText(collect);
    }
    else {
      this.fileNameField.setText("");
    }
  }
}
