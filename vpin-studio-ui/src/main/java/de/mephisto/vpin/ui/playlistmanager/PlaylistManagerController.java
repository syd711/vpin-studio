package de.mephisto.vpin.ui.playlistmanager;

import de.mephisto.vpin.commons.fx.Debouncer;
import de.mephisto.vpin.commons.fx.DialogController;
import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.frontend.FrontendType;
import de.mephisto.vpin.restclient.frontend.VPinScreen;
import de.mephisto.vpin.restclient.games.PlaylistRepresentation;
import de.mephisto.vpin.restclient.preferences.UISettings;
import de.mephisto.vpin.restclient.util.FileUtils;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.tables.TableDialogs;
import de.mephisto.vpin.ui.tables.TableOverviewController;
import de.mephisto.vpin.ui.util.PreferenceBindingUtil;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import org.apache.commons.lang3.StringUtils;
import org.kordamp.ikonli.javafx.FontIcon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

import static de.mephisto.vpin.ui.Studio.client;
import static de.mephisto.vpin.ui.Studio.stage;

public class PlaylistManagerController implements Initializable, DialogController {
  private final static Logger LOG = LoggerFactory.getLogger(PlaylistManagerController.class);
  private final Debouncer debouncer = new Debouncer();

  @FXML
  private VBox dataRoot;

  @FXML
  private Button closeBtn;

  @FXML
  private Button deleteBtn;

  @FXML
  private ScrollPane scrollPane;

  @FXML
  private Button assetManagerBtn;

  @FXML
  private CheckBox visibilityCheckbox;
  @FXML
  private CheckBox uglyCheckbox;
  @FXML
  private CheckBox defaultMediaCheckbox;
  @FXML
  private CheckBox disableSysListsCheckbox;
  @FXML
  private CheckBox sqlCheckbox;

  @FXML
  private TextArea sqlText;

  @FXML
  private TextField dofCommandText;

  @FXML
  private TextField passcodeText;

  @FXML
  private TextField mediaNameText;

  @FXML
  private ColorPicker colorPicker;

  @FXML
  private TreeView<PlaylistRepresentation> treeView;

  @FXML
  private Pane errorContainer;

  @FXML
  private Label errorLabel;

  @FXML
  private Pane dofCommandBox;
  @FXML
  private Pane mediaNameBox;
  @FXML
  private Pane passcodeBox;
  @FXML
  private Pane uglyBox;
  @FXML
  private Pane visibleBox;
  @FXML
  private Pane mediaDefaultsBox;
  @FXML
  private Pane disableSysListsBox;

  @FXML
  private PlaylistTableController playlistTableController; //fxml magic! Not unused -> id + "Controller"

  private Stage dialogStage;
  private TableOverviewController tableOverviewController;
  private boolean saveDisabled = true;

  @FXML
  private void onCancelClick(ActionEvent e) {
    Stage stage = (Stage) ((Button) e.getSource()).getScene().getWindow();
    stage.close();
  }

  @FXML
  private void onMediaEdit() {
    TreeItem<PlaylistRepresentation> selectedItem = treeView.getSelectionModel().getSelectedItem();
    if (selectedItem != null) {
      dialogStage.close();
      Platform.runLater(() -> {
        TableDialogs.openTableAssetsDialog(tableOverviewController, null, selectedItem.getValue(), VPinScreen.Wheel);
      });
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

  public void setData(Stage stage, TableOverviewController tableOverviewController, PlaylistRepresentation selectedPlaylist) {
    this.dialogStage = stage;
    this.tableOverviewController = tableOverviewController;
    this.playlistTableController.setStage(dialogStage);

    if (selectedPlaylist == null) {
      treeView.getSelectionModel().selectFirst();
    }
    else {
      treeView.getSelectionModel().select(new TreeItem<>(selectedPlaylist));
    }
  }

  private void refreshView(Optional<PlaylistRepresentation> value) {
    saveDisabled = true;

    dialogStage.setTitle("Playlist Manager");
    deleteBtn.setDisable(true);

    sqlText.setDisable(value.isEmpty() || !value.get().isSqlPlayList());

    sqlCheckbox.setDisable(value.isEmpty());
    visibilityCheckbox.setDisable(value.isEmpty());
    uglyCheckbox.setDisable(value.isEmpty());
    defaultMediaCheckbox.setDisable(value.isEmpty());
    disableSysListsCheckbox.setDisable(value.isEmpty());
    colorPicker.setDisable(value.isEmpty());
    dofCommandText.setDisable(value.isEmpty());

    errorContainer.setVisible(false);

    assetManagerBtn.setDisable(value.isEmpty());
    if (value.isPresent()) {
      PlaylistRepresentation plList = value.get();
      dialogStage.setTitle("Playlist Manager - " + plList.getName());
      playlistTableController.setData(value);
      deleteBtn.setDisable(plList.getId() == 0);

      errorContainer.setVisible(!StringUtils.isEmpty(plList.getSqlError()));
      errorLabel.setText(plList.getSqlError());

      sqlText.setText(plList.getPlayListSQL());
      sqlCheckbox.setSelected(plList.isSqlPlayList());
      visibilityCheckbox.setSelected(plList.isVisible());
      uglyCheckbox.setSelected(plList.isUglyList());
      defaultMediaCheckbox.setSelected(plList.isUseDefaults());
      disableSysListsCheckbox.setSelected(plList.isHideSysLists());
      dofCommandText.setText(plList.getDofCommand());
      mediaNameText.setText(plList.getMediaName());

      if (plList.getPassCode() != 0) {
        passcodeText.setText(String.valueOf(plList.getPassCode()));
      }
      else {
        passcodeText.setText("");
      }

      colorPicker.setValue(Color.web(WidgetFactory.hexColor(plList.getMenuColor())));
    }

    saveDisabled = false;
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

    dofCommandBox.managedProperty().bindBidirectional(dofCommandBox.visibleProperty());
    mediaNameBox.managedProperty().bindBidirectional(mediaNameBox.visibleProperty());
    passcodeBox.managedProperty().bindBidirectional(passcodeBox.visibleProperty());
    uglyBox.managedProperty().bindBidirectional(uglyBox.visibleProperty());
    visibleBox.managedProperty().bindBidirectional(visibleBox.visibleProperty());
    mediaDefaultsBox.managedProperty().bindBidirectional(mediaDefaultsBox.visibleProperty());
    disableSysListsBox.managedProperty().bindBidirectional(disableSysListsBox.visibleProperty());

    FrontendType frontendType = client.getFrontendService().getFrontendType();
    if (!frontendType.equals(FrontendType.Popper)) {
      uglyBox.setVisible(false);
      mediaDefaultsBox.setVisible(false);
      disableSysListsBox.setVisible(false);
      dofCommandBox.setVisible(false);
      passcodeBox.setVisible(false);
    }

    errorContainer.managedProperty().bindBidirectional(errorContainer.visibleProperty());

    UISettings uiSettings = client.getPreferenceService().getJsonPreference(PreferenceNames.UI_SETTINGS, UISettings.class);

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
      final HBox cellNode = new HBox();
      cellNode.setAlignment(Pos.CENTER_LEFT);

      final Label label = new Label();
      final Label graphics = new Label();
      label.getStyleClass().add("default-text");
      cellNode.getChildren().add(graphics);
      cellNode.getChildren().add(label);

      TreeCell<PlaylistRepresentation> cell = new TreeCell<PlaylistRepresentation>() {
        @Override
        protected void updateItem(PlaylistRepresentation child, boolean empty) {
          super.updateItem(child, empty);
          if (empty) {
            setGraphic(null);
          }
          else {
            setGraphic(cellNode);
          }
        }
      };
      cell.itemProperty().addListener((obs, oldItem, newItem) -> {
        if (newItem != null) {
          label.setText(newItem.getName());
          FontIcon icon = null;
          String tooltip = null;
          if (newItem.isSqlPlayList()) {
            tooltip = "SQL Playlist";
            icon = WidgetFactory.createIcon("mdi2d-database-search-outline");

          }
          else {
            tooltip = "Curated Playlist";
            icon = WidgetFactory.createIcon("mdi2f-format-list-checkbox");
          }

          label.setGraphic(icon);
          label.setTooltip(new Tooltip(tooltip));

          if (!StringUtils.isEmpty(newItem.getSqlError())) {
            label.setStyle(WidgetFactory.ERROR_STYLE);
            label.setTooltip(new Tooltip(newItem.getSqlError()));
          }
          else if (!newItem.isVisible()) {
            label.setStyle(WidgetFactory.DISABLED_TEXT_STYLE);
          }
          else {
            label.setStyle(WidgetFactory.DEFAULT_TEXT_STYLE);
          }

          Label playlistIcon = WidgetFactory.createPlaylistIcon(newItem, uiSettings);
          graphics.setGraphic(playlistIcon.getGraphic());
        }
      });
      return cell;
    });

    sqlCheckbox.selectedProperty().addListener(new ChangeListener<Boolean>() {
      @Override
      public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
        sqlText.setDisable(!newValue);
        if (!newValue) {
          errorContainer.setVisible(false);
          getPlaylist().setSqlError(null);
        }
        getPlaylist().setSqlPlayList(newValue);
        savePlaylist();
      }
    });

    visibilityCheckbox.selectedProperty().addListener(new ChangeListener<Boolean>() {
      @Override
      public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
        getPlaylist().setVisible(newValue);
        savePlaylist();
      }
    });

    uglyCheckbox.selectedProperty().addListener(new ChangeListener<Boolean>() {
      @Override
      public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
        getPlaylist().setUglyList(newValue);
        savePlaylist();
      }
    });

    defaultMediaCheckbox.selectedProperty().addListener(new ChangeListener<Boolean>() {
      @Override
      public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
        getPlaylist().setUseDefaults(newValue);
        savePlaylist();
      }
    });

    sqlText.textProperty().addListener(new ChangeListener<String>() {
      @Override
      public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
        if (saveDisabled) {
          return;
        }
        debouncer.debounce("sqlText", () -> {
          getPlaylist().setPlayListSQL(newValue);
          savePlaylist();
        }, 500);
      }
    });

    dofCommandText.textProperty().addListener(new ChangeListener<String>() {
      @Override
      public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
        if (saveDisabled) {
          return;
        }

        debouncer.debounce("dofCommand", () -> {
          getPlaylist().setDofCommand(newValue);
          savePlaylist();
        }, 500);
      }
    });

    passcodeText.textProperty().addListener(new ChangeListener<String>() {
      @Override
      public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
        if (saveDisabled) {
          return;
        }

        if (StringUtils.isEmpty(newValue)) {
          getPlaylist().setPassCode(0);
          savePlaylist();
          return;
        }

        try {
          Integer.parseInt(newValue);
        }
        catch (NumberFormatException e) {
          passcodeText.setText(oldValue);
          return;
        }

        if (newValue.length() < 4) {
          return;
        }

        if (newValue.length() > 4) {
          passcodeText.setText(oldValue);
          return;
        }
        getPlaylist().setPassCode(Integer.parseInt(newValue));
        savePlaylist();
      }
    });

    mediaNameText.textProperty().addListener(new ChangeListener<String>() {
      @Override
      public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
        if (saveDisabled) {
          return;
        }

        if (StringUtils.isEmpty(newValue)) {
          String v = "pl_" + getPlaylist().getName();
          mediaNameText.setText(v);
          return;
        }

        String value = FileUtils.replaceWindowsChars(newValue);
        if(!value.equalsIgnoreCase(newValue)) {
          mediaNameText.setText(value);
          return;
        }

        if (!value.startsWith("pl_")) {
          value = "pl_" + getPlaylist().getName();
          mediaNameText.setText(value);
          return;
        }

        debouncer.debounce("mediaNameText", () -> {
          getPlaylist().setMediaName(newValue);
          savePlaylist();
        }, 500);
      }
    });

    colorPicker.valueProperty().addListener((observableValue, color, t1) -> {
      String colorhex = PreferenceBindingUtil.toHexString(t1);
      if (colorhex.startsWith("#")) {
        colorhex = colorhex.substring(1);
      }
      getPlaylist().setMenuColor((int) Long.parseLong(colorhex, 16));
      savePlaylist();
    });

    reload();
  }

  private PlaylistRepresentation getPlaylist() {
    return treeView.getSelectionModel().getSelectedItem().getValue();
  }

  private void savePlaylist() {
    if (saveDisabled) {
      return;
    }

    TreeItem<PlaylistRepresentation> selectedItem = treeView.getSelectionModel().getSelectedItem();
    if (selectedItem != null) {
      PlaylistRepresentation value = selectedItem.getValue();
      try {
        PlaylistRepresentation update = client.getPlaylistsService().saveGame(value);
        selectedItem.setValue(update);

        Platform.runLater(() -> {
          playlistTableController.setData(Optional.of(update));

          if (update.isSqlPlayList()) {
            errorLabel.setText(update.getSqlError());
            errorContainer.setVisible(!StringUtils.isEmpty(update.getSqlError()));
          }
        });
      }
      catch (Exception e) {
        LOG.error("Failed to save playlist: {}", e.getMessage(), e);
        Platform.runLater(() -> {
          WidgetFactory.showAlert(dialogStage, "Error", "Failed to save playlist: " + e.getMessage());
        });
      }
    }
  }

}
