package de.mephisto.vpin.ui.tables.dialogs;

import de.mephisto.vpin.commons.fx.DialogController;
import de.mephisto.vpin.restclient.ImportDescriptor;
import de.mephisto.vpin.restclient.representations.PlaylistRepresentation;
import de.mephisto.vpin.restclient.representations.VpaDescriptorRepresentation;
import de.mephisto.vpin.ui.tables.RepositoryController;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
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
  private Button importBtn;

  @FXML
  private CheckBox importRomCheckbox;

  @FXML
  private CheckBox importPupPackCheckbox;

  @FXML
  private CheckBox importPopperMedia;

  @FXML
  private CheckBox highscoresCheckbox;

  @FXML
  private Label fileNameLabel;

  @FXML
  private ComboBox<PlaylistRepresentation> playlistCombo;

  private RepositoryController repositoryController;


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

    Platform.runLater(() -> {
//      String title = "Importing " + this.selection.size() + " tables";
//      if (this.selection.size() == 1) {
//        title = "Importing \"" + this.selection.get(0).getName() + "\"";
//      }
//      TableImportProgressModel model = new TableImportProgressModel(title, descriptor, this.selection);
//      Dialogs.createProgressDialog(model);
//      tablesController.onReload();
    });
  }

  @FXML
  private void onCancelClick(ActionEvent e) {
    Stage stage = (Stage) ((Button) e.getSource()).getScene().getWindow();
    stage.close();
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    this.importBtn.setDisable(true);
    List<PlaylistRepresentation> playlists = client.getPlaylists();
    ObservableList<PlaylistRepresentation> data = FXCollections.observableList(playlists);
    this.playlistCombo.setItems(data);
    this.playlistCombo.setDisable(false);
  }

  @Override
  public void onDialogCancel() {

  }

  public void setData(RepositoryController repositoryController, VpaDescriptorRepresentation vpaDescriptor) {
    this.repositoryController = repositoryController;
    this.fileNameLabel.setText(vpaDescriptor.getFilename());
  }
}
