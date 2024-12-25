package de.mephisto.vpin.ui.playlistmanager;

import de.mephisto.vpin.commons.fx.DialogController;
import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.restclient.frontend.VPinScreen;
import de.mephisto.vpin.restclient.games.PlaylistRepresentation;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.tables.TableDialogs;
import de.mephisto.vpin.ui.tables.TableOverviewController;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.kordamp.ikonli.javafx.FontIcon;
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
  private ScrollPane scrollPane;

  @FXML
  private Button assetManagerBtn;

  @FXML
  private TreeView<PlaylistRepresentation> treeView;

  @FXML
  private PlaylistTableController playlistTableController; //fxml magic! Not unused -> id + "Controller"

  private Stage dialogStage;
  private TableOverviewController tableOverviewController;

  @FXML
  private void onCancelClick(ActionEvent e) {
    Stage stage = (Stage) ((Button) e.getSource()).getScene().getWindow();
    stage.close();
  }

  @FXML
  private void onMediaEdit() {
    TreeItem<PlaylistRepresentation> selectedItem = treeView.getSelectionModel().getSelectedItem();
    if (selectedItem != null) {
      TableDialogs.openTableAssetsDialog(tableOverviewController, null, selectedItem.getValue(), VPinScreen.Wheel);
    }
  }

  @FXML
  private void onPlaylistCreate() {

  }

  @FXML
  private void onPlaylistDelete() {
    TreeItem<PlaylistRepresentation> selectedItem = treeView.getSelectionModel().getSelectedItem();
    if (selectedItem != null) {
      PlaylistRepresentation value = selectedItem.getValue();
      Optional<ButtonType> result = WidgetFactory.showConfirmation(Studio.stage, "Delete Playlist", "Delete Playlist \"" + value.getName() + "\"?");
      if (result.isPresent() && result.get().equals(ButtonType.OK)) {
        client.getPlaylistsService().delete(value.getId());
        reload();
      }
    }

  }

  public void setData(Stage stage, TableOverviewController tableOverviewController) {
    this.dialogStage = stage;
    this.tableOverviewController = tableOverviewController;
    this.playlistTableController.setStage(dialogStage);
  }

  private void refreshView(Optional<PlaylistRepresentation> value) {
    playlistNameLabel.setText("-");
    deleteBtn.setDisable(true);

    assetManagerBtn.setDisable(value.isEmpty());
    if (value.isPresent()) {
      PlaylistRepresentation plList = value.get();
      playlistNameLabel.setText(plList.getName());
      playlistTableController.setData(value);
      deleteBtn.setDisable(plList.getId() == 0);
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
    scrollPane.setFitToHeight(true);
    scrollPane.setFitToWidth(true);

    treeView.selectionModelProperty().get().selectedItemProperty().addListener(new ChangeListener<TreeItem<PlaylistRepresentation>>() {
      @Override
      public void changed(ObservableValue<? extends TreeItem<PlaylistRepresentation>> observable, TreeItem<PlaylistRepresentation> oldValue, TreeItem<PlaylistRepresentation> newValue) {
        if (newValue != null) {
          PlaylistRepresentation value = newValue.getValue();
          refreshView(Optional.of(value));
        }
        else {
          refreshView(Optional.empty());
        }
      }
    });
    treeView.setCellFactory(t -> {
      final Label label = new Label();
      label.getStyleClass().add("default-text");
      TreeCell<PlaylistRepresentation> cell = new TreeCell<PlaylistRepresentation>() {
        @Override
        protected void updateItem(PlaylistRepresentation child, boolean empty) {
          super.updateItem(child, empty);
          if (empty) {
            setGraphic(null);
          }
          else {
            setGraphic(label);
          }
        }
      };
      cell.itemProperty().addListener((obs, oldItem, newItem) -> {
        if (newItem != null) {
          label.setText(newItem.getName());
          if (!newItem.isVisible()) {
            label.setStyle(WidgetFactory.DISABLED_TEXT_STYLE);
          }
          if (newItem.isSqlPlayList()) {
            FontIcon icon = WidgetFactory.createIcon("mdi2d-database-search-outline");
            label.setGraphic(icon);
          }
          else {
            FontIcon icon = WidgetFactory.createIcon("mdi2f-format-list-checkbox");
            label.setGraphic(icon);
          }
        }
      });
      return cell;
    });

    reload();
    treeView.getSelectionModel().selectFirst();
  }
}
