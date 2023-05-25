package de.mephisto.vpin.ui.archiving.dialogs;

import de.mephisto.vpin.commons.fx.DialogController;
import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.restclient.descriptors.ArchiveRestoreDescriptor;
import de.mephisto.vpin.restclient.representations.ArchiveDescriptorRepresentation;
import de.mephisto.vpin.restclient.representations.PlaylistRepresentation;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.jobs.JobPoller;
import de.mephisto.vpin.ui.tables.TablesController;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

import static de.mephisto.vpin.ui.Studio.client;

public class TableRestoreController implements Initializable, DialogController {
  private final static Logger LOG = LoggerFactory.getLogger(TableRestoreController.class);

  @FXML
  private Label titleLabel;

  @FXML
  private ComboBox<PlaylistRepresentation> playlistCombo;

  private List<ArchiveDescriptorRepresentation> archiveDescriptors;


  @FXML
  private void onImport(ActionEvent e) {
    ArchiveRestoreDescriptor restoreDescriptor = new ArchiveRestoreDescriptor();

    if (!this.playlistCombo.getSelectionModel().isEmpty()) {
      restoreDescriptor.setPlaylistId(this.playlistCombo.getSelectionModel().getSelectedItem().getId());
    }

    Stage stage = (Stage) ((Button) e.getSource()).getScene().getWindow();
    stage.close();

    new Thread(() -> {
      Platform.runLater(() -> {
        try {
          for (ArchiveDescriptorRepresentation archiveDescriptor : this.archiveDescriptors) {
            restoreDescriptor.setFilename(archiveDescriptor.getFilename());
            restoreDescriptor.setArchiveSourceId(archiveDescriptor.getSource().getId());
            client.getArchiveService().installTable(restoreDescriptor);
          }
          JobPoller.getInstance().setPolling();
        } catch (Exception ex) {
          LOG.error("Failed to restore: " + ex.getMessage(), ex);
          WidgetFactory.showAlert(Studio.stage, "Restore Failed", "Failed to trigger import: " + ex.getMessage());
        }

      });
    }).start();
  }

  @FXML
  private void onCancelClick(ActionEvent e) {
    Stage stage = (Stage) ((Button) e.getSource()).getScene().getWindow();
    stage.close();
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    List<PlaylistRepresentation> playlists = client.getPinUPPopperService().getPlaylists();
    ObservableList<PlaylistRepresentation> data = FXCollections.observableList(playlists);
    this.playlistCombo.setItems(data);
    this.playlistCombo.setDisable(false);
  }

  @Override
  public void onDialogCancel() {

  }

  public void setData(TablesController tablesController, List<ArchiveDescriptorRepresentation> archiveDescriptors) {
    this.archiveDescriptors = archiveDescriptors;

    String title = "Restore " + this.archiveDescriptors.size() + " Tables";
    if (this.archiveDescriptors.size() == 1) {
      title = "Restore \"" + this.archiveDescriptors.get(0).getTableDetails().getGameDisplayName() + "\"";
    }
    titleLabel.setText(title);
  }
}
