package de.mephisto.vpin.ui.playlistmanager;

import de.mephisto.vpin.commons.fx.DialogController;
import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.restclient.games.PlaylistRepresentation;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.util.Dialogs;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

import static de.mephisto.vpin.ui.Studio.client;

public class PlaylistManagerController implements Initializable, DialogController {
  private final static Logger LOG = LoggerFactory.getLogger(PlaylistManagerController.class);

  @FXML
  private VBox dataRoot;

  @FXML
  private Button closeBtn;

  @FXML
  private Button deleteBtn;

  @FXML
  private Label playlistNameLabel;

  @FXML
  private Button assetManagerBtn;

  @FXML
  private TreeView<PlaylistRepresentation> treeView;

  @FXML
  private PlaylistTableController playlistTableController; //fxml magic! Not unused -> id + "Controller"

  private Stage dialogStage;

  @FXML
  private void onCancelClick(ActionEvent e) {
    Stage stage = (Stage) ((Button) e.getSource()).getScene().getWindow();
    stage.close();
  }

  @FXML
  private void onMediaEdit() {

  }

  @FXML
  private void onPlaylistCreate() {

  }

  @FXML
  private void onPlaylistDelete() {
    TreeItem<PlaylistRepresentation> selectedItem = treeView.getSelectionModel().getSelectedItem();
    if(selectedItem != null) {
      PlaylistRepresentation value = selectedItem.getValue();
      Optional<ButtonType> result = WidgetFactory.showConfirmation(Studio.stage, "Delete Playlist", "Delete Playlist \"" + value.getName() + "\"?");
      if (result.isPresent() && result.get().equals(ButtonType.OK)) {
        client.getPlaylistsService().delete(value.getId());
        reload();
      }
    }

  }

  public void setData(Stage stage) {
    this.dialogStage = stage;
    this.playlistTableController.setStage(dialogStage);
  }

  private void refreshView(Optional<PlaylistRepresentation> value) {
    playlistNameLabel.setText("-");
    if (value.isPresent()) {
      playlistNameLabel.setText(value.get().getName());
      playlistTableController.setData(value);
    }
  }

  private void expandAll(TreeItem<PlaylistRepresentation> node) {
    node.setExpanded(true);
    node.getChildren().forEach(this::expandAll);
  }

  private void buildTreeModel(TreeItem<PlaylistRepresentation> parent) {
    PlaylistRepresentation value = parent.getValue();
    List<PlaylistRepresentation> children = value.getChildren();
    for (PlaylistRepresentation child : children) {
      TreeItem<PlaylistRepresentation> childNode = new TreeItem<>(child);
      parent.getChildren().add(childNode);
      buildTreeModel(childNode);
    }
  }

  private void reload() {
    List<PlaylistRepresentation> playlists = client.getPlaylistsService().getPlaylistTree();
    PlaylistRepresentation playListRoot = playlists.get(0);
    TreeItem<PlaylistRepresentation> root = new TreeItem<>(playListRoot);
    buildTreeModel(root);
    treeView.setRoot(root);
    expandAll(root);
  }

  @Override
  public void onDialogCancel() {

  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    reload();

    treeView.selectionModelProperty().get().selectedItemProperty().addListener(new ChangeListener<TreeItem<PlaylistRepresentation>>() {
      @Override
      public void changed(ObservableValue<? extends TreeItem<PlaylistRepresentation>> observable, TreeItem<PlaylistRepresentation> oldValue, TreeItem<PlaylistRepresentation> newValue) {
        if(newValue != null) {
          PlaylistRepresentation value = newValue.getValue();
          refreshView(Optional.of(value));
        }
        else {
          refreshView(Optional.empty());
        }
      }
    });
    treeView.getSelectionModel().selectFirst();
  }
}
