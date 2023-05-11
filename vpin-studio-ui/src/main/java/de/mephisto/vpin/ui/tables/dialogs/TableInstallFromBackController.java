package de.mephisto.vpin.ui.tables.dialogs;

import de.mephisto.vpin.commons.fx.DialogController;
import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.restclient.descriptors.ArchiveInstallDescriptor;
import de.mephisto.vpin.restclient.representations.PlaylistRepresentation;
import de.mephisto.vpin.restclient.representations.ArchiveDescriptorRepresentation;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.jobs.JobPoller;
import de.mephisto.vpin.ui.tables.TablesController;
import de.mephisto.vpin.ui.util.Dialogs;
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

public class TableInstallFromBackController implements Initializable, DialogController {
  private final static Logger LOG = LoggerFactory.getLogger(TableInstallFromBackController.class);

  @FXML
  private Label titleLabel;

  @FXML
  private ComboBox<PlaylistRepresentation> playlistCombo;

  private TablesController tablesController;
  private List<ArchiveDescriptorRepresentation> archiveDescriptors;


  @FXML
  private void onImport(ActionEvent e) {
    ArchiveInstallDescriptor installDescriptor = new ArchiveInstallDescriptor();

    if (!this.playlistCombo.getSelectionModel().isEmpty()) {
      installDescriptor.setPlaylistId(this.playlistCombo.getSelectionModel().getSelectedItem().getId());
    }

    Stage stage = (Stage) ((Button) e.getSource()).getScene().getWindow();
    stage.close();

    Platform.runLater(() -> {
      TableInstallationProgressModel model = new TableInstallationProgressModel(titleLabel.getText(), installDescriptor, this.archiveDescriptors);
      Dialogs.createProgressDialog(model);
      tablesController.getTableOverviewController().onReload();
    });

    new Thread(() -> {
      Platform.runLater(() -> {
        try {
          for (ArchiveDescriptorRepresentation archiveDescriptor : this.archiveDescriptors) {
            installDescriptor.setFilename(archiveDescriptor.getFilename());
            installDescriptor.setArchiveSourceId(archiveDescriptor.getSource().getId());
            client.installTable(installDescriptor);
          }
          JobPoller.getInstance().setPolling();
        } catch (Exception ex) {
          LOG.error("Failed to import: " + ex.getMessage(), ex);
          WidgetFactory.showAlert(Studio.stage, "Import Failed", "Failed to trigger import: " + ex.getMessage());
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
    List<PlaylistRepresentation> playlists = client.getPlaylists();
    ObservableList<PlaylistRepresentation> data = FXCollections.observableList(playlists);
    this.playlistCombo.setItems(data);
    this.playlistCombo.setDisable(false);
  }

  @Override
  public void onDialogCancel() {

  }

  public void setData(TablesController tablesController, List<ArchiveDescriptorRepresentation> archiveDescriptors) {
    this.tablesController = tablesController;
    this.archiveDescriptors = archiveDescriptors;

    String title = "Installing " + this.archiveDescriptors.size() + " Tables";
    if (this.archiveDescriptors.size() == 1) {
      title = "Installing \"" + this.archiveDescriptors.get(0).getTableDetails().getGameDisplayName() + "\"";
    }
    titleLabel.setText(title);
  }
}
