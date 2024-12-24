package de.mephisto.vpin.ui.playlistmanager;

import de.mephisto.vpin.commons.fx.DialogController;
import de.mephisto.vpin.restclient.games.PlaylistRepresentation;
import de.mephisto.vpin.ui.Studio;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

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

  }

  public void setData(Stage stage) {
    this.dialogStage = stage;
  }

  private void refreshView(Optional<PlaylistRepresentation> value) {
    playlistNameLabel.setText("-");
    if (value.isPresent()) {
      playlistNameLabel.setText(value.get().getName());
      playlistTableController.setData(dialogStage, value);
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

  @Override
  public void onDialogCancel() {

  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    List<PlaylistRepresentation> playlists = Studio.client.getPlaylistsService().getPlaylistTree();
    PlaylistRepresentation playListRoot = playlists.get(0);

    TreeItem<PlaylistRepresentation> root = new TreeItem<>(playListRoot);
    buildTreeModel(root);
    treeView.setRoot(root);

    expandAll(root);

    treeView.selectionModelProperty().get().selectedItemProperty().addListener(new ChangeListener<TreeItem<PlaylistRepresentation>>() {
      @Override
      public void changed(ObservableValue<? extends TreeItem<PlaylistRepresentation>> observable, TreeItem<PlaylistRepresentation> oldValue, TreeItem<PlaylistRepresentation> newValue) {
        PlaylistRepresentation value = newValue.getValue();
        refreshView(Optional.of(value));
      }
    });
    treeView.getSelectionModel().selectFirst();
  }
}
