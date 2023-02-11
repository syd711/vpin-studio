package de.mephisto.vpin.ui.tables.dialogs;

import de.mephisto.vpin.commons.fx.DialogController;
import de.mephisto.vpin.restclient.ImportDescriptor;
import de.mephisto.vpin.restclient.representations.PlaylistRepresentation;
import de.mephisto.vpin.ui.tables.TablesController;
import de.mephisto.vpin.ui.util.Dialogs;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

import static de.mephisto.vpin.ui.Studio.client;
import static de.mephisto.vpin.ui.Studio.stage;

public class TableImportController implements Initializable, DialogController {
  private final static Logger LOG = LoggerFactory.getLogger(TableImportController.class);

  private static File lastFolderSelection;

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
  private TextField fileNameField;

  @FXML
  private ComboBox<PlaylistRepresentation> playlistCombo;

  private List<File> selection;
  private TablesController tablesController;

  @FXML
  private void onFileSelect() {
    FileChooser fileChooser = new FileChooser();
    fileChooser.setTitle("Select VPA Files");
    fileChooser.getExtensionFilters().addAll(
        new FileChooser.ExtensionFilter("VPA", "*.vpa"));

    if (TableImportController.lastFolderSelection != null) {
      fileChooser.setInitialDirectory(TableImportController.lastFolderSelection);
    }

    this.selection = fileChooser.showOpenMultipleDialog(stage);
    if (this.selection != null && !this.selection.isEmpty()) {
      TableImportController.lastFolderSelection = this.selection.get(0).getParentFile();
      List<String> names = this.selection.stream().map(f -> f.getName()).collect(Collectors.toList());
      this.fileNameField.setText(String.join(", ", names));
    }
    else {
      this.fileNameField.setText("");
    }
  }

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
      String title = "Importing " + this.selection.size() + " tables";
      if (this.selection.size() == 1) {
        title = "Importing \"" + this.selection.get(0).getName() + "\"";
      }
      TableImportProgressModel model = new TableImportProgressModel(title, descriptor, this.selection);
      Dialogs.createProgressDialog(model);
      tablesController.onReload();
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
    this.fileNameField.textProperty().addListener((observableValue, s, t1) -> importBtn.setDisable(StringUtils.isEmpty(t1)));

    List<PlaylistRepresentation> playlists = client.getPlaylists();
    ObservableList<PlaylistRepresentation> data = FXCollections.observableList(playlists);
    this.playlistCombo.setItems(data);
    this.playlistCombo.setDisable(false);
  }

  @Override
  public void onDialogCancel() {

  }

  public void setTablesController(TablesController tablesController) {
    this.tablesController = tablesController;
  }
}
