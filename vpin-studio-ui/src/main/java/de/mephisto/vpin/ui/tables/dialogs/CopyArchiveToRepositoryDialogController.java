package de.mephisto.vpin.ui.tables.dialogs;

import de.mephisto.vpin.commons.fx.DialogController;
import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.restclient.descriptors.ArchiveDownloadAndInstallDescriptor;
import de.mephisto.vpin.restclient.representations.ArchiveDescriptorRepresentation;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.jobs.JobPoller;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

import static de.mephisto.vpin.ui.Studio.client;

public class CopyArchiveToRepositoryDialogController implements Initializable, DialogController {
  private final static Logger LOG = LoggerFactory.getLogger(CopyArchiveToRepositoryDialogController.class);

  @FXML
  private Label titleLabel;

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
        ArchiveDownloadAndInstallDescriptor descriptor = new ArchiveDownloadAndInstallDescriptor();
        descriptor.setInstall(false);
        descriptor.setFilename(selectedItem.getFilename());
        descriptor.setArchiveSourceId(selectedItem.getSource().getId());
        client.downloadArchive(descriptor);
        JobPoller.getInstance().setPolling();
      }
    } catch (Exception e) {
      LOG.error("Copy to repository failed: " + e.getMessage(), e);
      WidgetFactory.showAlert(Studio.stage, "Copying archive files failed.", "Please check the log file for details.", "Error: " + e.getMessage());
    } finally {
      stage.close();
    }
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    this.result = false;
  }

  @Override
  public void onDialogCancel() {
    result = false;
  }

  public void setData(List<ArchiveDescriptorRepresentation> archiveDescriptors) {
    this.archiveDescriptors = archiveDescriptors;
    if (archiveDescriptors.size() == 1) {
      this.titleLabel.setText("Copy \"" + archiveDescriptors.get(0).getFilename() + "\"");
    }
    else {
      this.titleLabel.setText("Copy " + archiveDescriptors.size() + " Archives");
    }
  }
}
