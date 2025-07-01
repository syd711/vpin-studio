package de.mephisto.vpin.ui.archiving.dialogs;

import de.mephisto.vpin.commons.ArchiveSourceType;
import de.mephisto.vpin.commons.fx.DialogController;
import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.restclient.archiving.ArchiveType;
import de.mephisto.vpin.restclient.system.SystemSummary;
import de.mephisto.vpin.restclient.archiving.ArchiveSourceRepresentation;
import de.mephisto.vpin.ui.util.ProgressDialog;
import de.mephisto.vpin.ui.util.ProgressResultModel;
import de.mephisto.vpin.ui.util.StudioFileChooser;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

import static de.mephisto.vpin.ui.Studio.client;
import static de.mephisto.vpin.ui.Studio.stage;

public class ArchiveUploadController implements Initializable, DialogController {
  private final static Logger LOG = LoggerFactory.getLogger(ArchiveUploadController.class);

  @FXML
  private TextField fileNameField;

  @FXML
  private Button uploadBtn;

  @FXML
  private ComboBox<ArchiveSourceRepresentation> repositoryCombo;

  private List<File> selection;

  private boolean result = false;

  @FXML
  private void onCancelClick(ActionEvent e) {
    Stage stage = (Stage) ((Button) e.getSource()).getScene().getWindow();
    stage.close();
  }

  @FXML
  private void onUploadClick(ActionEvent event) {
    if (selection != null && !selection.isEmpty()) {
      result = true;
      try {
        Platform.runLater(() -> {
          Stage stage = (Stage) ((Button) event.getSource()).getScene().getWindow();
          stage.close();
        });

        ArchiveSourceRepresentation selectedItem = this.repositoryCombo.getSelectionModel().getSelectedItem();
        ArchiveUploadProgressModel model = new ArchiveUploadProgressModel("Backup Upload", selectedItem.getId(), selection);
        ProgressResultModel progressResult = ProgressDialog.createProgressDialog(model);

        // Cancelling the upload progress doesn't actually cancel the HTTP request, however we still do not want to continue to the next step.
        if (progressResult.isCancelled()) {
          result = false;
        }
      }
      catch (Exception e) {
        LOG.error("Upload failed: " + e.getMessage(), e);
        WidgetFactory.showAlert(stage, "Uploading archive failed", "Please check the log file for details", "Error: " + e.getMessage());
      }
    }
  }

  @FXML
  private void onFileSelect(ActionEvent event) {
    Stage stage = (Stage) ((Button) event.getSource()).getScene().getWindow();

    SystemSummary systemSummary = client.getSystemService().getSystemSummary();

    List<String> filters = Arrays.asList("*." + ArchiveType.VPXZ.name().toLowerCase());

    StudioFileChooser fileChooser = new StudioFileChooser();
    fileChooser.setTitle("Select Archives");
    fileChooser.getExtensionFilters().addAll(
        new FileChooser.ExtensionFilter("Visual Pinball Archive", filters));

    this.selection = fileChooser.showOpenMultipleDialog(stage);
    if (this.selection != null && !this.selection.isEmpty()) {
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

    List<ArchiveSourceRepresentation> repositories = new ArrayList<>(client.getArchiveService().getArchiveSources());
    repositories = repositories.stream().filter(r -> r.getType().equals(ArchiveSourceType.File.name())).collect(Collectors.toList());
    repositoryCombo.setItems(FXCollections.observableList(repositories));
    repositoryCombo.getSelectionModel().select(0);
  }

  @Override
  public void onDialogCancel() {
    result = false;
  }

  public boolean uploadFinished() {
    return result;
  }
}
