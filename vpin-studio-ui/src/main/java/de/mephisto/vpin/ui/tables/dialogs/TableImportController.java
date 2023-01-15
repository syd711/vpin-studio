package de.mephisto.vpin.ui.tables.dialogs;

import de.mephisto.vpin.commons.fx.DialogController;
import de.mephisto.vpin.restclient.representations.GameRepresentation;
import de.mephisto.vpin.ui.Studio;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

import static de.mephisto.vpin.ui.Studio.stage;

public class TableImportController implements Initializable, DialogController {
  private final static Logger LOG = LoggerFactory.getLogger(TableImportController.class);

  private static File lastFolderSelection;

  @FXML
  private Label titleLabel;

  @FXML
  private Button importBtn;

  @FXML
  private CheckBox exportPupPackCheckbox;

  @FXML
  private CheckBox exportPopperMedia;

  @FXML
  private CheckBox highscoresCheckbox;

  @FXML
  private TextField fileNameField;

  private boolean result = false;
  private GameRepresentation game;

  private List<File> selection;

  @FXML
  private void onFileSelect() {
    FileChooser fileChooser = new FileChooser();
    fileChooser.setTitle("Select VPA Files");
    fileChooser.getExtensionFilters().addAll(
        new FileChooser.ExtensionFilter("VPA", "*.vpa"));

    if (TableImportController.lastFolderSelection != null) {
      fileChooser.setInitialDirectory(TableImportController.lastFolderSelection);
    }

    this.selection = fileChooser.showOpenMultipleDialog(stage);
    if (this.selection != null && !this.selection.isEmpty()) {
      TableImportController.lastFolderSelection = this.selection.get(0).getParentFile();
      this.fileNameField.setText(this.selection.stream().map(f -> f.getName()).collect(Collectors.joining()));
    }
    else {
      this.fileNameField.setText("");
    }
  }

  @FXML
  private void onImport(ActionEvent e) {
    Stage stage = (Stage) ((Button) e.getSource()).getScene().getWindow();

    stage.close();
  }

  @FXML
  private void onCancelClick(ActionEvent e) {
    Stage stage = (Stage) ((Button) e.getSource()).getScene().getWindow();
    stage.close();
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    this.result = false;
    this.titleLabel.setText("");
  }

  public void setGame(GameRepresentation game) {
    this.game = game;
    this.titleLabel.setText("Table Import");
  }

  @Override
  public void onDialogCancel() {

  }
}
