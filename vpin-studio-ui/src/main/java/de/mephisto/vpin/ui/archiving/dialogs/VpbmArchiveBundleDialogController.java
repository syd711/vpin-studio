package de.mephisto.vpin.ui.archiving.dialogs;

import de.mephisto.vpin.commons.fx.DialogController;
import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.descriptors.ArchiveBundleDescriptor;
import de.mephisto.vpin.restclient.representations.ArchiveDescriptorRepresentation;
import de.mephisto.vpin.restclient.representations.PreferenceEntryRepresentation;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.util.Dialogs;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

import static de.mephisto.vpin.ui.Studio.client;
import static de.mephisto.vpin.ui.Studio.stage;

public class VpbmArchiveBundleDialogController implements Initializable, DialogController {
  private final static Logger LOG = LoggerFactory.getLogger(VpbmArchiveBundleDialogController.class);

  private static File lastFolderSelection;

  @FXML
  private TextField fileNameField;

  @FXML
  private TextField exportHostId;

  @FXML
  private Button downloadBtn;

  @FXML
  private Label titleLabel;

  private File targetFolder;

  private boolean result = false;
  private List<ArchiveDescriptorRepresentation> archiveDescriptors;

  @FXML
  private void onCancelClick(ActionEvent e) {
    Stage stage = (Stage) ((Button) e.getSource()).getScene().getWindow();
    stage.close();
  }

  @FXML
  private void onDownload(ActionEvent event) {
    result = true;
    try {
      ArchiveBundleDescriptor archiveBundleDescriptor = new ArchiveBundleDescriptor();
      archiveBundleDescriptor.setArchiveSourceId(archiveDescriptors.get(0).getSource().getId());
      archiveBundleDescriptor.setExportHostId(this.exportHostId.getText());

      for (ArchiveDescriptorRepresentation selectedItem : archiveDescriptors) {
        archiveBundleDescriptor.getArchiveNames().add(selectedItem.getFilename());
      }

      Platform.runLater(()-> {
        Stage stage = (Stage) ((Button) event.getSource()).getScene().getWindow();
        stage.close();
      });

      Dialogs.createProgressDialog(new BundleProgressModel("Bundle Creation", this.targetFolder, archiveBundleDescriptor));

    } catch (Exception e) {
      LOG.error("Download failed: " + e.getMessage(), e);
      WidgetFactory.showAlert(Studio.stage, "Downloading archive files failed.", "Please check the log file for details.", "Error: " + e.getMessage());
    }
  }

  @FXML
  private void onFileSelect() {
    DirectoryChooser chooser = new DirectoryChooser();
    chooser.setTitle("Select Target Folder");
    if (VpbmArchiveBundleDialogController.lastFolderSelection != null) {
      chooser.setInitialDirectory(VpbmArchiveBundleDialogController.lastFolderSelection);
    }

    this.targetFolder = chooser.showDialog(stage);
    if (this.targetFolder != null) {
      VpbmArchiveBundleDialogController.lastFolderSelection = this.targetFolder;
      this.fileNameField.setText(this.targetFolder.getAbsolutePath());
    }
    else {
      this.fileNameField.setText("");
    }
    validateInput();
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    this.result = false;

    this.downloadBtn.setDisable(true);
    this.fileNameField.textProperty().addListener((observableValue, s, t1) -> downloadBtn.setDisable(StringUtils.isEmpty(t1)));
    if (VpbmArchiveBundleDialogController.lastFolderSelection != null) {
      fileNameField.setText(VpbmArchiveBundleDialogController.lastFolderSelection.getAbsolutePath());
    }

    PreferenceEntryRepresentation preference = client.getPreference(PreferenceNames.VPBM_EXTERNAL_HOST_IDENTIFIER);
    String value = preference.getValue();
    if (!StringUtils.isEmpty(value)) {
      exportHostId.setText(value);
    }
  }

  private void validateInput() {
    this.downloadBtn.setDisable(true);

    if (this.targetFolder == null) {
      return;
    }

    if (StringUtils.isEmpty(this.exportHostId.getText())) {
      return;
    }

    this.downloadBtn.setDisable(false);
  }

  @Override
  public void onDialogCancel() {
    result = false;
  }

  public void setData(List<ArchiveDescriptorRepresentation> archiveDescriptors) {
    this.archiveDescriptors = archiveDescriptors;
    if (archiveDescriptors.size() == 1) {
      this.titleLabel.setText("Create Bundle for \"" + archiveDescriptors.get(0).getFilename() + "\"");
    }
    else {
      this.titleLabel.setText("Create Bundle of " + archiveDescriptors.size() + " Archives");
    }
  }
}
