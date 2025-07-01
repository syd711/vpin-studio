package de.mephisto.vpin.ui.archiving.dialogs;

import de.mephisto.vpin.commons.fx.DialogController;
import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.restclient.archiving.ArchiveDescriptorRepresentation;
import de.mephisto.vpin.restclient.archiving.ArchiveType;
import de.mephisto.vpin.restclient.emulators.GameEmulatorRepresentation;
import de.mephisto.vpin.restclient.games.descriptors.ArchiveRestoreDescriptor;
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
import javafx.scene.layout.Pane;
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
  private Pane emuGrid;

  @FXML
  private ComboBox<GameEmulatorRepresentation> emulatorCombo;

  private List<ArchiveDescriptorRepresentation> archiveDescriptors;


  @FXML
  private void onImport(ActionEvent e) {
    ArchiveRestoreDescriptor restoreDescriptor = new ArchiveRestoreDescriptor();
    restoreDescriptor.setEmulatorId(emulatorCombo.getValue().getId());

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
    List<GameEmulatorRepresentation> emulators = client.getEmulatorService().getVpxGameEmulators();
    ObservableList<GameEmulatorRepresentation> data = FXCollections.observableList(emulators);
    this.emulatorCombo.setItems(data);
    this.emulatorCombo.setValue(data.get(0));
  }

  @Override
  public void onDialogCancel() {

  }

  public void setData(TablesController tablesController, List<ArchiveDescriptorRepresentation> archiveDescriptors) {
    this.archiveDescriptors = archiveDescriptors;

    String title = "Restore " + this.archiveDescriptors.size() + " Tables?";
    if (this.archiveDescriptors.size() == 1) {
      title = "Restore \"" + this.archiveDescriptors.get(0).getTableDetails().getGameDisplayName() + "\"?";
    }
    titleLabel.setText(title);

    ArchiveType archiveType = client.getSystemService().getSystemSummary().getArchiveType();
    emuGrid.setVisible(archiveType.equals(ArchiveType.VPXZ));
  }
}
