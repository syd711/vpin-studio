package de.mephisto.vpin.ui.playlistmanager;

import de.mephisto.vpin.commons.fx.Debouncer;
import de.mephisto.vpin.commons.fx.DialogController;
import de.mephisto.vpin.commons.utils.JFXFuture;
import de.mephisto.vpin.restclient.games.PlaylistRepresentation;
import de.mephisto.vpin.ui.util.ProgressDialog;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.Objects;
import java.util.Optional;
import java.util.ResourceBundle;

import static de.mephisto.vpin.ui.Studio.client;

public class PlaylistTemplatesController implements DialogController, Initializable {
  private final static Logger LOG = LoggerFactory.getLogger(PlaylistTemplatesController.class);

  public static Debouncer debouncer = new Debouncer();

  @FXML
  private Button okButton;

  @FXML
  private Spinner<Integer> maxEntriesSpinner;

  @FXML
  private CheckBox shuffleCheckbox;

  @FXML
  private ComboBox<PlaylistTemplate> templateComboBox;

  private PlaylistTableController playlistTableController;
  private Stage parentStage;
  private PlaylistRepresentation playlist;

  @Override
  public void onDialogCancel() {

  }

  @FXML
  private void onCancelClick(ActionEvent e) {
    Stage stage = (Stage) ((Button) e.getSource()).getScene().getWindow();
    stage.close();
  }

  @FXML
  private void onDialogSubmit(ActionEvent e) {
    Stage stage = (Stage) ((Button) e.getSource()).getScene().getWindow();

    if (templateComboBox.getValue() != null) {
      boolean shuffle = shuffleCheckbox.isSelected();
      int count = maxEntriesSpinner.getValue();
      int templateId = templateComboBox.valueProperty().get().id;
      int playlistId = playlist.getId();

      JFXFuture.supplyAsync(() -> {
        client.getPlaylistsService().clearPlaylist(playlist.getId());
        return StudioPlaylistFactory.create(templateId, count, shuffle);
      }).thenAcceptLater((games) -> {
        String title = "Adding " + games.size() + " games to \"" + playlist.getName() + "\"";
        if (games.size() == 1) {
          title = "Adding \"" + games.get(0).getGameDisplayName() + "\" to \"" + playlist.getName() + "\"";
        }
        ProgressDialog.createProgressDialog(new PlaylistUpdateProgressModel(title, playlist, games, true));
        playlistTableController.setData(Optional.of(client.getPlaylistsService().getPlaylist(playlistId)));
      });
    }

    stage.close();
  }

  public void setData(PlaylistTableController playlistTableController, Stage stage, PlaylistRepresentation playlist) {
    this.playlistTableController = playlistTableController;
    this.parentStage = stage;
    this.playlist = playlist;
    refresh();
  }

  private void refresh() {

  }

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    okButton.setDisable(true);
    templateComboBox.setItems(FXCollections.observableList(StudioPlaylistFactory.TEMPLATE_LIST));
    templateComboBox.valueProperty().addListener(new ChangeListener<PlaylistTemplate>() {
      @Override
      public void changed(ObservableValue<? extends PlaylistTemplate> observable, PlaylistTemplate oldValue, PlaylistTemplate newValue) {
        okButton.setDisable(newValue == null);
        refresh();
      }
    });

    shuffleCheckbox.selectedProperty().addListener(new ChangeListener<Boolean>() {
      @Override
      public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
        refresh();
      }
    });

    SpinnerValueFactory.IntegerSpinnerValueFactory factory = new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 100, 0);
    maxEntriesSpinner.setValueFactory(factory);
    maxEntriesSpinner.valueProperty().addListener((observable, oldValue, newValue) -> {
      refresh();
    });
  }

  static class PlaylistTemplate {
    private final int id;
    private final String name;

    public PlaylistTemplate(int id, String name) {
      this.id = id;
      this.name = name;
    }

    @Override
    public String toString() {
      return name;
    }

    @Override
    public boolean equals(Object o) {
      if (o == null || getClass() != o.getClass()) return false;
      PlaylistTemplate that = (PlaylistTemplate) o;
      return id == that.id && Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
      return Objects.hash(id, name);
    }
  }
}
