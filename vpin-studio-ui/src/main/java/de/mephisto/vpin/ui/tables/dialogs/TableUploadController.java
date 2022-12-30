package de.mephisto.vpin.ui.tables.dialogs;

import de.mephisto.vpin.commons.fx.DialogController;
import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.restclient.representations.PlaylistRepresentation;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.util.Dialogs;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

import static de.mephisto.vpin.ui.Studio.client;
import static de.mephisto.vpin.ui.Studio.stage;

public class TableUploadController implements Initializable, DialogController {
  private final static Logger LOG = LoggerFactory.getLogger(TableUploadController.class);

  private static File lastFolderSelection;

  @FXML
  private TextField fileNameField;

  @FXML
  private CheckBox importCheckbox;

  @FXML
  private ComboBox<PlaylistRepresentation> playlistCombo;

  @FXML
  private Button uploadBtn;

  private List<File> selection;
  private boolean result = false;

  @FXML
  private void onCancelClick(ActionEvent e) {
    Stage stage = (Stage) ((Button) e.getSource()).getScene().getWindow();
    stage.close();
  }

  @FXML
  private void onUploadClick(ActionEvent event) {
    Stage stage = (Stage) ((Button) event.getSource()).getScene().getWindow();
    if (selection != null && !selection.isEmpty()) {
      uploadBtn.setDisable(true);
      result = true;
      try {
        boolean importToPopper = this.importCheckbox.isSelected();
        final PlaylistRepresentation value = this.playlistCombo.getValue();

        int playListId = -1;
        if(value != null) {
          playListId = value.getId();
        }

        Platform.runLater(() -> {
          stage.close();
        });

        TableUploadProgressModel model = new TableUploadProgressModel(client, "VPX Upload", selection, importToPopper, playListId);
        Dialogs.createProgressDialog(model);
      } catch (Exception e) {
        LOG.error("Upload failed: " + e.getMessage(), e);
        stage.close();
        WidgetFactory.showAlert(stage, "Uploading VPX file failed.", "Please check the log file for details.", "Error: " + e.getMessage());
      }
    }
  }

  @FXML
  private void onFileSelect() {
    FileChooser fileChooser = new FileChooser();
    fileChooser.setTitle("Select VPX Files");
    fileChooser.getExtensionFilters().addAll(
        new FileChooser.ExtensionFilter("VPX File", "*.vpx"));

    if (TableUploadController.lastFolderSelection != null) {
      fileChooser.setInitialDirectory(TableUploadController.lastFolderSelection);
    }

    this.selection = fileChooser.showOpenMultipleDialog(stage);
    if (this.selection != null && !this.selection.isEmpty()) {
      TableUploadController.lastFolderSelection = this.selection.get(0).getParentFile();
      this.fileNameField.setText(this.selection.stream().map(f -> f.getName()).collect(Collectors.joining()));
    }
    else {
      this.fileNameField.setText("");
    }
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    this.result = false;
    this.selection = null;
    this.uploadBtn.setDisable(true);
    this.fileNameField.textProperty().addListener((observableValue, s, t1) -> uploadBtn.setDisable(StringUtils.isEmpty(t1)));

    List<PlaylistRepresentation> playlists = client.getPlaylists();
    ObservableList<PlaylistRepresentation> data = FXCollections.observableList(playlists);
    this.playlistCombo.setItems(data);
    this.playlistCombo.setDisable(false);

    importCheckbox.selectedProperty().addListener((observable, oldValue, newValue) -> playlistCombo.setDisable(!newValue));
  }

  @Override
  public void onDialogCancel() {
    result = false;
  }

  public boolean uploadFinished() {
    return result;
  }
}
