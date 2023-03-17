package de.mephisto.vpin.ui.tables.dialogs;

import de.mephisto.vpin.commons.VpaSourceType;
import de.mephisto.vpin.commons.fx.DialogController;
import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.restclient.DownloadJobDescriptor;
import de.mephisto.vpin.restclient.VpaImportDescriptor;
import de.mephisto.vpin.restclient.representations.VpaDescriptorRepresentation;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.jobs.JobPoller;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

import static de.mephisto.vpin.ui.Studio.client;
import static de.mephisto.vpin.ui.Studio.stage;

public class VpaDownloadDialogController implements Initializable, DialogController {
  private final static Logger LOG = LoggerFactory.getLogger(VpaDownloadDialogController.class);

  private static File lastFolderSelection;
  private static boolean fileSelectedLast;

  @FXML
  private TextField fileNameField;

  @FXML
  private Button downloadBtn;

  @FXML
  private Label titleLabel;

  @FXML
  private RadioButton downloadToFile;

  @FXML
  private RadioButton downloadToRepository;

  @FXML
  private Button folderBtn;

  private File targetFolder;

  private boolean result = false;
  private List<VpaDescriptorRepresentation> vpas;

  @FXML
  private void onCancelClick(ActionEvent e) {
    Stage stage = (Stage) ((Button) e.getSource()).getScene().getWindow();
    stage.close();
  }

  @FXML
  private void onDownload(ActionEvent event) {
    Stage stage = (Stage) ((Button) event.getSource()).getScene().getWindow();
    result = true;
    try {
      for (VpaDescriptorRepresentation selectedItem : vpas) {
        if (downloadToRepository.isSelected()) {
          VpaImportDescriptor descriptor = new VpaImportDescriptor();
          descriptor.setUuid(selectedItem.getManifest().getUuid());
          descriptor.setVpaSourceId(selectedItem.getSource().getId());
          descriptor.setInstall(false);
          client.importVpa(descriptor);
          JobPoller.getInstance().setPolling();
        }
        else {
          File target = new File(targetFolder, selectedItem.getFilename());
          int index = 1;
          String originalBaseName = FilenameUtils.getBaseName(target.getName());
          while (target.exists()) {
            String suffix = FilenameUtils.getExtension(target.getName());
            target = new File(target.getParentFile(), originalBaseName + " (" + index + ")." + suffix);
            index++;
          }

          long repositoryId = selectedItem.getSource().getId();
          String uuid = selectedItem.getManifest().getUuid();

          DownloadJobDescriptor job = new DownloadJobDescriptor("/vpa/download/file/" + repositoryId + "/" + uuid, target, uuid);
          job.setTitle("Download of \"" + selectedItem.getManifest().getGameDisplayName() + "\"");
          job.setDescription("Downloading file \"" + selectedItem.getFilename() + "\"");
          JobPoller.getInstance().queueJob(job);
        }
      }
    } catch (Exception e) {
      LOG.error("Download failed: " + e.getMessage(), e);
      WidgetFactory.showAlert(Studio.stage, "Downloading VPA files failed.", "Please check the log file for details.", "Error: " + e.getMessage());
    } finally {
      stage.close();
    }
  }

  @FXML
  private void onFileSelect() {
    DirectoryChooser chooser = new DirectoryChooser();
    chooser.setTitle("Select Target Folder");
    if (VpaDownloadDialogController.lastFolderSelection != null) {
      chooser.setInitialDirectory(VpaDownloadDialogController.lastFolderSelection);
    }

    this.targetFolder = chooser.showDialog(stage);
    if (this.targetFolder != null) {
      VpaDownloadDialogController.lastFolderSelection = this.targetFolder;
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

    downloadToRepository.selectedProperty().addListener((observable, oldValue, newValue) -> {
      fileNameField.setDisable(newValue);
      folderBtn.setDisable(newValue);
      downloadToFile.setSelected(!newValue);
      fileSelectedLast = !newValue;
      validateInput();
    });

    downloadToFile.selectedProperty().addListener((observable, oldValue, newValue) -> {
      downloadToRepository.setSelected(!newValue);
      fileSelectedLast = newValue;
      validateInput();
    });

    this.downloadToFile.setSelected(VpaDownloadDialogController.fileSelectedLast);
    this.downloadToRepository.setSelected(!VpaDownloadDialogController.fileSelectedLast);
  }

  private void validateInput() {
    if (downloadToFile.isSelected()) {
      this.downloadBtn.setDisable(this.targetFolder == null);
    }
    else {
      this.downloadBtn.setDisable(false);
    }
  }

  @Override
  public void onDialogCancel() {
    result = false;
  }

  public void setData(List<VpaDescriptorRepresentation> vpas) {
    this.vpas = vpas;
    if (vpas.size() == 1) {
      this.titleLabel.setText("Download \"" + vpas.get(0).getFilename() + "\"");
    }
    else {
      this.titleLabel.setText("Download " + vpas.size() + " Archives");
    }

    //do not download from file repo to file repo
    VpaDescriptorRepresentation descriptorRepresentation = vpas.get(0);
    VpaSourceType vpaSourceType = VpaSourceType.valueOf(descriptorRepresentation.getSource().getType());
    downloadToRepository.setVisible(!vpaSourceType.equals(VpaSourceType.File));
    downloadToFile.setSelected(true);
    downloadToFile.setDisable(true);
  }
}
