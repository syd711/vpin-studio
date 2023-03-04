package de.mephisto.vpin.ui.tables.dialogs;

import de.mephisto.vpin.commons.fx.DialogController;
import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.restclient.ImportDescriptor;
import de.mephisto.vpin.restclient.representations.PlaylistRepresentation;
import de.mephisto.vpin.restclient.representations.VpaDescriptorRepresentation;
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
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

import static de.mephisto.vpin.ui.Studio.client;

public class VpaImportController implements Initializable, DialogController {
  private final static Logger LOG = LoggerFactory.getLogger(VpaImportController.class);

  @FXML
  private CheckBox importRomCheckbox;

  @FXML
  private CheckBox importPupPackCheckbox;

  @FXML
  private CheckBox importPopperMedia;

  @FXML
  private CheckBox highscoresCheckbox;

  @FXML
  private Label titleLabel;

  @FXML
  private ComboBox<PlaylistRepresentation> playlistCombo;

  private TablesController tablesController;
  private List<VpaDescriptorRepresentation> vpaDescriptors;


  @FXML
  private void onImport(ActionEvent e) {
    ImportDescriptor descriptor = new ImportDescriptor();
    descriptor.setImportRom(this.importRomCheckbox.isSelected());
    descriptor.setImportPupPack(this.importPupPackCheckbox.isSelected());
    descriptor.setImportPopperMedia(this.importPopperMedia.isSelected());
    descriptor.setImportHighscores(this.highscoresCheckbox.isSelected());

    if (!this.playlistCombo.getSelectionModel().isEmpty()) {
      descriptor.setPlaylistId(this.playlistCombo.getSelectionModel().getSelectedItem().getId());
    }

    Stage stage = (Stage) ((Button) e.getSource()).getScene().getWindow();
    stage.close();

//    Platform.runLater(() -> {
//      VpaImportProgressModel model = new VpaImportProgressModel(titleLabel.getText(), descriptor, this.vpaDescriptors);
//      Dialogs.createProgressDialog(model);
//      tablesController.getTableOverviewController().onReload();
//    });

    new Thread(() -> {
      Platform.runLater(() -> {
        try {
          for (VpaDescriptorRepresentation vpaDescriptor : this.vpaDescriptors) {
            descriptor.setUuid(vpaDescriptor.getManifest().getUuid());
            client.importVpa(descriptor);
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

  public void setData(TablesController tablesController, List<VpaDescriptorRepresentation> vpaDescriptors) {
    this.tablesController = tablesController;
    this.vpaDescriptors = vpaDescriptors;

    String title = "Installing " + this.vpaDescriptors.size() + " Tables";
    if (this.vpaDescriptors.size() == 1) {
      title = "Installing Table \"" + this.vpaDescriptors.get(0).getManifest().getGameDisplayName() + "\"";
    }
    titleLabel.setText(title);
  }
}
