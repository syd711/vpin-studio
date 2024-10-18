package de.mephisto.vpin.ui.backglassmanager.dialogs;

import de.mephisto.vpin.commons.fx.DialogController;
import de.mephisto.vpin.restclient.directb2s.DirectB2S;
import de.mephisto.vpin.ui.util.FileSelectorDragEventHandler;
import de.mephisto.vpin.ui.util.FileSelectorDropEventHandler;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

public class ResGeneratorDialogController implements Initializable, DialogController {
  private final static Logger LOG = LoggerFactory.getLogger(ResGeneratorDialogController.class);

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
  private Label screenResLabel;

  @FXML
  private Label playfieldDimensionLabel;

  @FXML
  private Label backglassDimensionLabel;

  @FXML
  private Label dmdDimensionLabel;

  private File selection;

  private boolean result = false;
  private DirectB2S directB2S;

  @FXML
  private void onCancelClick(ActionEvent e) {
    Stage stage = (Stage) ((Button) e.getSource()).getScene().getWindow();
    stage.close();
  }

  @FXML
  private void onGenerateClick(ActionEvent event) {
    Stage stage = (Stage) ((Button) event.getSource()).getScene().getWindow();
    if (selection != null && selection.exists()) {
      Platform.runLater(() -> {

      });
    }
  }

  @FXML
  private void onFileSelect(ActionEvent event) {
    Stage stage = (Stage) ((Button) event.getSource()).getScene().getWindow();

    this.uploadBtn.setDisable(true);

    StudioFileChooser fileChooser = new StudioFileChooser();
    fileChooser.setTitle("Select Frame Picture");
    fileChooser.getExtensionFilters().addAll(
        new FileChooser.ExtensionFilter("Image", "*.jpg", "*.png"));

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
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    this.result = false;
    this.selection = null;
    this.uploadBtn.setDisable(true);

    root.setOnDragOver(new FileSelectorDragEventHandler(root, "png", "jpg"));
    root.setOnDragDropped(new FileSelectorDropEventHandler(fileNameField, file -> {
      selection = file;
      refreshSelection();
    }));
  }

  @Override
  public void onDialogCancel() {
    result = false;
  }

  public void setData(Stage stage, DirectB2S directB2S) {
    this.directB2S = directB2S;
  }
}
