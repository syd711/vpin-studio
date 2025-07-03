package de.mephisto.vpin.ui.archiving.dialogs;

import de.mephisto.vpin.commons.fx.DialogController;
import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.restclient.archiving.ArchiveDescriptorRepresentation;
import de.mephisto.vpin.restclient.games.descriptors.DownloadJobDescriptor;
import de.mephisto.vpin.restclient.util.FileUtils;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.jobs.JobPoller;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.ResourceBundle;

import static de.mephisto.vpin.ui.Studio.stage;

public class ArchiveDownloadDialogController implements Initializable, DialogController {
  private final static Logger LOG = LoggerFactory.getLogger(ArchiveDownloadDialogController.class);

  @FXML
  private TextField fileNameField;

  @FXML
  private Button downloadBtn;

  @FXML
  private Label titleLabel;

  private static File targetFolder;

  private boolean result = false;
  private List<ArchiveDescriptorRepresentation> archiveDescriptors;

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
      for (ArchiveDescriptorRepresentation selectedItem : archiveDescriptors) {
        long repositoryId = selectedItem.getSource().getId();
        File asset = new File(targetFolder, selectedItem.getFilename());
        File uniqueTarget = FileUtils.uniqueFile(asset);
        DownloadJobDescriptor job = new DownloadJobDescriptor("archives/download/file/" + repositoryId + "/" + URLEncoder.encode(selectedItem.getFilename(), StandardCharsets.UTF_8), uniqueTarget);
        job.setTitle("Download of \"" + selectedItem.getFilename() + "\"");
        JobPoller.getInstance().queueJob(job);
      }
    }
    catch (Exception e) {
      LOG.error("Download failed: " + e.getMessage(), e);
      WidgetFactory.showAlert(Studio.stage, "Downloading archive files failed.", "Please check the log file for details.", "Error: " + e.getMessage());
    }
    finally {
      stage.close();
    }
  }

  @FXML
  private void onFileSelect() {
    DirectoryChooser chooser = new DirectoryChooser();
    chooser.setTitle("Select Target Folder");
    if (targetFolder != null) {
      chooser.setInitialDirectory(targetFolder);
    }

    targetFolder = chooser.showDialog(stage);
    if (targetFolder != null) {
      this.fileNameField.setText(targetFolder.getAbsolutePath());
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
  }

  private void validateInput() {
    this.downloadBtn.setDisable(this.targetFolder == null);
  }

  @Override
  public void onDialogCancel() {
    result = false;
  }

  public void setData(List<ArchiveDescriptorRepresentation> archiveDescriptors) {
    this.archiveDescriptors = archiveDescriptors;
    if (archiveDescriptors.size() == 1) {
      this.titleLabel.setText("Download \"" + archiveDescriptors.get(0).getFilename() + "\"");
    }
    else {
      this.titleLabel.setText("Download " + archiveDescriptors.size() + " Archives");
    }
  }
}
