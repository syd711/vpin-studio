package de.mephisto.vpin.ui.playlistmanager;

import de.mephisto.vpin.commons.fx.DialogController;
import de.mephisto.vpin.commons.utils.JFXFuture;
import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.frontend.Frontend;
import de.mephisto.vpin.restclient.games.GameEmulatorRepresentation;
import de.mephisto.vpin.restclient.games.GameList;
import de.mephisto.vpin.restclient.games.GameListItem;
import de.mephisto.vpin.restclient.games.PlaylistRepresentation;
import de.mephisto.vpin.restclient.games.descriptors.JobDescriptor;
import de.mephisto.vpin.restclient.jobs.JobType;
import de.mephisto.vpin.restclient.preferences.UISettings;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.events.EventManager;
import de.mephisto.vpin.ui.tables.dialogs.TableImportProgressModel;
import de.mephisto.vpin.ui.util.FrontendUtil;
import de.mephisto.vpin.ui.util.ProgressDialog;
import de.mephisto.vpin.ui.util.ProgressResultModel;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

import static de.mephisto.vpin.ui.Studio.client;

public class NewPlaylistController implements Initializable, DialogController {
  private final static Logger LOG = LoggerFactory.getLogger(NewPlaylistController.class);

  @FXML
  private TextField nameField;

  @FXML
  private Button saveBtn;

  @FXML
  private ComboBox<GameEmulatorRepresentation> emulatorCombo;

  private PlaylistManagerController playlistManagerController;

  @FXML
  private void onSaveClick(ActionEvent e) {
    String name = nameField.getText();
    Stage stage = (Stage) ((Button) e.getSource()).getScene().getWindow();

    Platform.runLater(() -> {
      stage.close();
    });

    playlistManagerController.createPlaylist(name, -1, emulatorCombo.getValue().getId());
  }

  @FXML
  private void onCancelClick(ActionEvent e) {
    Stage stage = (Stage) ((Button) e.getSource()).getScene().getWindow();
    stage.close();
  }

  @Override
  public void onDialogCancel() {

  }

  public void setData(PlaylistManagerController playlistManagerController) {
    this.playlistManagerController = playlistManagerController;
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    List<GameEmulatorRepresentation> filtered = new ArrayList<>(client.getFrontendService().getGameEmulators());
    this.emulatorCombo.setItems(FXCollections.observableList(filtered));
    this.saveBtn.setDisable(true);

    List<PlaylistRepresentation> playlists = client.getPlaylistsService().getPlaylists();

    if (!filtered.isEmpty()) {
      this.emulatorCombo.setValue(filtered.get(0));
    }

    nameField.textProperty().addListener(new ChangeListener<String>() {
      @Override
      public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
        Optional<PlaylistRepresentation> first = playlists.stream().filter(p -> p.getName().equalsIgnoreCase(newValue)).findFirst();
        boolean disable = StringUtils.isEmpty(newValue) || first.isPresent();
        saveBtn.setDisable(disable);
      }
    });
  }
}
