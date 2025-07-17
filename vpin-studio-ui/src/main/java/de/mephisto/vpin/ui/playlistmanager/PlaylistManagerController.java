package de.mephisto.vpin.ui.playlistmanager;

import de.mephisto.vpin.commons.fx.Debouncer;
import de.mephisto.vpin.commons.fx.DialogController;
import de.mephisto.vpin.commons.fx.UIDefaults;
import de.mephisto.vpin.commons.utils.JFXFuture;
import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.emulators.GameEmulatorRepresentation;
import de.mephisto.vpin.restclient.frontend.*;
import de.mephisto.vpin.restclient.playlists.PlaylistRepresentation;
import de.mephisto.vpin.restclient.preferences.UISettings;
import de.mephisto.vpin.restclient.util.FileUtils;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.events.EventManager;
import de.mephisto.vpin.ui.tables.TableDialogs;
import de.mephisto.vpin.ui.tables.TableOverviewController;
import de.mephisto.vpin.ui.tables.TablesSidebarController;
import de.mephisto.vpin.ui.tables.TablesSidebarPlaylistsController;
import de.mephisto.vpin.ui.util.PreferenceBindingUtil;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.input.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import org.apache.commons.lang3.StringUtils;
import org.kordamp.ikonli.javafx.FontIcon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.*;

import static de.mephisto.vpin.ui.Studio.Features;
import static de.mephisto.vpin.ui.Studio.client;

public class PlaylistManagerController implements Initializable, DialogController {
  private final static Logger LOG = LoggerFactory.getLogger(PlaylistManagerController.class);

  private static final DataFormat JAVA_FORMAT = new DataFormat("application/x-java-serialized-object");
  private static final String DROP_HINT_STYLE = "-fx-border-color: #6666FF; -fx-border-width: 2 0 2 0; -fx-padding: 3 3 1 3";

  private final Debouncer debouncer = new Debouncer();

  private static final Map<String, String> SQL_TEMPLATES = new LinkedHashMap<>();

  static {
    SQL_TEMPLATES.put("Top 10 - Most played", "SELECT * FROM Games WHERE visible=1 AND EMUID = [EMULATOR_ID] AND GameId in (SELECT GameID from GamesStats ORDER BY NumberPlays DESC LIMIT 10) ORDER BY GameDisplay");
    SQL_TEMPLATES.put("Top 10 - Least played", "SELECT * FROM Games WHERE visible=1 AND EMUID = [EMULATOR_ID] AND GameId in (SELECT GameID from GamesStats ORDER BY NumberPlays ASC LIMIT 10) ORDER BY GameDisplay");
    SQL_TEMPLATES.put("Top 10 - Not played for a long time", "SELECT * FROM Games WHERE visible=1 AND EMUID = [EMULATOR_ID] AND GameId in (SELECT GameID from GamesStats ORDER BY LastPlayed ASC LIMIT 10) ORDER BY GameDisplay");
    SQL_TEMPLATES.put("Never played tables", "SELECT * FROM Games WHERE visible=1 AND EMUID = [EMULATOR_ID] AND GameId NOT IN (SELECT GameID from GamesStats) ORDER BY GameDisplay");
    SQL_TEMPLATES.put("10 Random tables", "SELECT * FROM Games WHERE visible=1 AND EMUID = [EMULATOR_ID] ORDER BY RANDOM() LIMIT 10");
    SQL_TEMPLATES.put("All 'VPin Workshop' tables", "SELECT * FROM Games WHERE visible=1 AND EMUID = [EMULATOR_ID] AND (tags LIKE '%VPW%' OR GameDisplay LIKE '%VPW%') ORDER BY GameDisplay");
    SQL_TEMPLATES.put("All 'VR' tables", "SELECT * FROM Games WHERE visible=1 AND EMUID = [EMULATOR_ID] AND tags LIKE '%VR%' ORDER BY GameDisplay");
    SQL_TEMPLATES.put("All '4k' tables", "SELECT * FROM Games WHERE visible=1 AND EMUID = [EMULATOR_ID] AND tags LIKE '%4k%' ORDER BY GameDisplay");
    SQL_TEMPLATES.put("All kids-friendly tables", "SELECT * FROM Games WHERE visible=1 AND EMUID = [EMULATOR_ID] AND tags LIKE '%Kids%' ORDER BY GameDisplay");
    SQL_TEMPLATES.put("All iScored competed tables", "SELECT * FROM Games WHERE visible=1 AND EMUID = [EMULATOR_ID] AND TourneyID like '%iscored%' ORDER BY GameDisplay");
    SQL_TEMPLATES.put("All tournament competed tables", "SELECT * FROM Games WHERE visible=1 AND EMUID = [EMULATOR_ID] AND TourneyID like '%tournament%' ORDER BY GameDisplay");
  }

  @FXML
  private VBox dataRoot;

  @FXML
  private Button closeBtn;

  @FXML
  private Button deleteBtn;

  @FXML
  private Button renameBtn;

  @FXML
  private ScrollPane scrollPane;

  @FXML
  private Button assetManagerBtn;

  @FXML
  private Separator assetManagerSeparator;

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
  private Label hintLabel;

  @FXML
  private Pane colorPickerBox;
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
  private MenuButton templateSelector;

  @FXML
  private Pane settingsBox;

  @FXML
  private Button saveSQLBtn;

  @FXML
  private PlaylistTableController playlistTableController; //fxml magic! Not unused -> id + "Controller"

  private Stage dialogStage;
  private TableOverviewController tableOverviewController;
  private boolean saveDisabled = true;

  private boolean dirty = false;

  private TreeItem<PlaylistRepresentation> draggedItem;
  private TreeCell<PlaylistRepresentation> dropZone;



  @FXML
  private void onCancelClick(ActionEvent e) {
    onClose();
    Stage stage = (Stage) ((Button) e.getSource()).getScene().getWindow();
    stage.close();
  }

  @FXML
  private void onSQLSave(ActionEvent e) {
    String sql = sqlText.getText();
    getPlaylist().setPlayListSQL(sql);
    savePlaylist();
  }

  @FXML
  private void onRename(ActionEvent ae) {
    TreeItem<PlaylistRepresentation> selectedItem = treeView.getSelectionModel().getSelectedItem();
    if (selectedItem != null) {
      PlaylistRepresentation playlist = selectedItem.getValue();
      String oldValue = playlist.getName();
      String value = WidgetFactory.showInputDialog(dialogStage, "Rename Playlist", "Enter new playlist name:", null, null, playlist.getName());
      rename(value, oldValue, playlist);
    }
  }

  private void rename(String value, String oldValue, PlaylistRepresentation playlist) {
    value = FileUtils.replaceWindowsChars(value);
    if (!StringUtils.isEmpty(value) && !oldValue.equalsIgnoreCase(value)) {
      try {
        playlist.setName(value);

        JFXFuture.supplyAsync(() -> client.getPlaylistsService().savePlaylist(playlist))
            .thenAcceptLater(update -> reload(update, () -> {
              if (update == null) {
                WidgetFactory.showAlert(dialogStage, "Error", "Playlist renaming failed. Please report this problem.");
              }
              else {
                select(treeView.getRoot(), update);
              }
            }));
      }
      catch (Exception e) {
        LOG.error("Playlist renaming failed: {}", e.getMessage(), e);
        WidgetFactory.showAlert(dialogStage, "Error", "Playlist renaming failed: " + e.getMessage());
      }
    }
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
  private void onReload() {
    TreeItem<PlaylistRepresentation> selectedItem = treeView.getSelectionModel().getSelectedItem();
    PlaylistRepresentation selection = selectedItem != null ? selectedItem.getValue() : null;
    JFXFuture.runAsync(() -> client.getPlaylistsService().clearCache())
        .thenLater(() -> reload(null, () -> {
          if (treeView.isShowRoot()) {
            if (selectedItem != null) {
              select(treeView.getRoot(), selection);
            }
            else {
              select(treeView.getRoot(), treeView.getRoot().getValue());
            }
          }
          else {
            TreeItem<PlaylistRepresentation> rootList = treeView.getRoot().getChildren().get(0);
            select(rootList, selection);
          }
        }));
  }

  @FXML
  private void onPlaylistCreate() {
    if (client.getFrontendService().getFrontendType().equals(FrontendType.Popper)) {
      String description = null;
      String value = WidgetFactory.showInputDialog(dialogStage, "New Playlist", "Enter new playlist name:", description, null, "New Playlist");
      if (!StringUtils.isEmpty(value)) {
        value = FileUtils.replaceWindowsChars(value);
        int parentId = -1;
        TreeItem<PlaylistRepresentation> parentItem = treeView.getSelectionModel().getSelectedItem();
        if (parentItem != null) {
          parentId = parentItem.getValue().getId();
        }
        else {
          parentId = treeView.getRoot().getValue().getId();
        }

        createPlaylist(value, parentId, getDefaultGameEmulator().getId());
      }
    }
    else {
      PlaylistDialogs.openCreatePlaylistDialog(this);
    }
  }

  public void createPlaylist(String value, int parentId, int emulatorId) {
    try {
      PlaylistRepresentation newPlayList = new PlaylistRepresentation();
      newPlayList.setName(value);
      newPlayList.setUseDefaults(true);
      newPlayList.setVisible(true);
      newPlayList.setEmulatorId(emulatorId);
      newPlayList.setParentId(parentId);
      newPlayList.setMenuColor((int) Long.parseLong("FFFFFF", 16));
      newPlayList.setMediaName("pl_" + FileUtils.replaceWindowsChars(value));

      //workaround for PinballX
      if (!Features.PLAYLIST_EXTENDED) {
        newPlayList.setEmulatorId(getDefaultGameEmulator().getId());
      }

      JFXFuture
          .supplyAsync(() -> client.getPlaylistsService().savePlaylist(newPlayList))
          .thenAcceptLater(update -> reload(update, () -> {
            select(treeView.getRoot(), update);
          }))
          .onErrorLater(e -> {
            LOG.error("Playlist creation failed: {}", e.getMessage(), e);
            WidgetFactory.showAlert(dialogStage, "Error", "Playlist creation failed: " + e.getMessage());
          });
    }
    catch (Exception e) {
    }
  }

  @FXML
  private void onPlaylistDelete() {
    TreeItem<PlaylistRepresentation> selectedItem = treeView.getSelectionModel().getSelectedItem();
    if (selectedItem != null) {
      PlaylistRepresentation value = selectedItem.getValue();

      String help2 = null;
      String btnText = "Delete Playlist";
      if (!selectedItem.getChildren().isEmpty()) {
        help2 = "The child playlists of this playlist will be deleted too!";
        btnText = "Delete All";
      }

      Optional<ButtonType> result = WidgetFactory.showConfirmation(Studio.stage, "Delete Playlist", "Delete Playlist \"" + value.getName() + "\"?", help2, btnText);
      if (result.isPresent() && result.get().equals(ButtonType.OK)) {
        JFXFuture.runAsync(() -> client.getPlaylistsService().delete(value.getId()))
            .thenLater(() -> reload(value, () -> {
            }));
      }
    }
  }

  public void setData(Stage stage, TableOverviewController tableOverviewController, PlaylistRepresentation selectedPlaylist) {
    this.dialogStage = stage;
    this.tableOverviewController = tableOverviewController;
    this.playlistTableController.setStage(dialogStage);

    dialogStage.setOnHiding(new EventHandler<WindowEvent>() {
      @Override
      public void handle(WindowEvent event) {
        if (dirty) {
          EventManager.getInstance().notifyTablesChanged();
        }
      }
    });

    reload(selectedPlaylist, null);
  }

  private void select(TreeItem<PlaylistRepresentation> root, PlaylistRepresentation selectedPlaylist) {
    if (root.getValue() == null || selectedPlaylist == null) {
      onReload();
      return;
    }

    if (root.getValue().getId() == selectedPlaylist.getId()) {
      treeView.getSelectionModel().select(root);
    }
    else {
      List<TreeItem<PlaylistRepresentation>> children = root.getChildren();
      for (TreeItem<PlaylistRepresentation> child : children) {
        select(child, selectedPlaylist);
      }
    }
  }

  private void refreshView(Optional<PlaylistRepresentation> value) {
    saveDisabled = true;

    dialogStage.setTitle("Playlist Manager");
    deleteBtn.setDisable(true);

    sqlText.setDisable(value.isEmpty() || !value.get().isSqlPlayList());
    if (sqlText.isDisabled()) {
      sqlText.setText("");
    }

    renameBtn.setDisable(value.isEmpty());
    sqlCheckbox.setDisable(value.isEmpty());
    visibilityCheckbox.setDisable(value.isEmpty());
    uglyCheckbox.setDisable(value.isEmpty());
    defaultMediaCheckbox.setDisable(value.isEmpty());
    disableSysListsCheckbox.setDisable(value.isEmpty());
    colorPicker.setDisable(value.isEmpty());
    dofCommandText.setDisable(value.isEmpty());
    dofCommandText.setText("");
    mediaNameText.setDisable(value.isEmpty());
    mediaNameText.setText("");
    passcodeText.setDisable(value.isEmpty());
    passcodeText.setText("");
    templateSelector.setDisable(value.isEmpty() || !value.get().isSqlPlayList());

    errorContainer.setVisible(false);

    assetManagerBtn.setDisable(value.isEmpty());
    if (value.isPresent()) {
      PlaylistRepresentation plList = value.get();
      dialogStage.setTitle("Playlist Manager - " + plList.getName());
      playlistTableController.setData(value);
      deleteBtn.setDisable(plList.getId() == 0 || !plList.getChildren().isEmpty());

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

  private void reload(PlaylistRepresentation playlist, Runnable r) {
    JFXFuture.supplyAsync(() -> client.getPlaylistsService().getPlaylistTree())
        .thenAcceptLater(playListRoot -> {
          TreeItem<PlaylistRepresentation> root = new TreeItem<>(playListRoot);
          buildTreeModel(root);
          treeView.setRoot(root);
          treeView.setShowRoot(Features.PLAYLIST_EXTENDED);
          expandAll(root);

          if (playlist != null) {
            select(treeView.getRoot(), playlist);
          }
          else {
            select(treeView.getRoot(), treeView.getRoot().getValue());
          }

          // execute
          if (r != null) {
            r.run();
          }
          // finally refresh the sidebar
          if (playlist != null) {
            refreshSidebar(playlist);
          }
        });
  }

  @Override
  public void onDialogCancel() {
    onClose();
  }

  private void onClose() {

  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    scrollPane.setFitToHeight(true);
    scrollPane.setFitToWidth(true);

    treeView.setEditable(true);
    settingsBox.managedProperty().bindBidirectional(settingsBox.visibleProperty());
    saveSQLBtn.setDisable(true);

    assetManagerSeparator.managedProperty().bindBidirectional(assetManagerSeparator.visibleProperty());
    assetManagerBtn.managedProperty().bindBidirectional(assetManagerBtn.visibleProperty());
    colorPickerBox.managedProperty().bindBidirectional(colorPickerBox.visibleProperty());
    dofCommandBox.managedProperty().bindBidirectional(dofCommandBox.visibleProperty());
    mediaNameBox.managedProperty().bindBidirectional(mediaNameBox.visibleProperty());
    passcodeBox.managedProperty().bindBidirectional(passcodeBox.visibleProperty());
    uglyBox.managedProperty().bindBidirectional(uglyBox.visibleProperty());
    visibleBox.managedProperty().bindBidirectional(visibleBox.visibleProperty());
    mediaDefaultsBox.managedProperty().bindBidirectional(mediaDefaultsBox.visibleProperty());
    disableSysListsBox.managedProperty().bindBidirectional(disableSysListsBox.visibleProperty());

    FrontendType frontendType = client.getFrontendService().getFrontendType();
    templateSelector.setVisible(frontendType.equals(FrontendType.Popper));
    hintLabel.setVisible(frontendType.equals(FrontendType.Popper));

    settingsBox.setVisible(Features.PLAYLIST_EXTENDED);
//    assetManagerSeparator.setVisible(frontendType.supportPlaylists());
//    assetManagerBtn.setVisible(frontendType.supportPlaylists());

    colorPickerBox.setVisible(frontendType.equals(FrontendType.Popper));
    templateSelector.setDisable(true);

    if (!frontendType.equals(FrontendType.Popper)) {
      uglyBox.setVisible(false);
      mediaDefaultsBox.setVisible(false);
      disableSysListsBox.setVisible(false);
      dofCommandBox.setVisible(false);
      passcodeBox.setVisible(false);
    }
    else {
      Set<Map.Entry<String, String>> entries = SQL_TEMPLATES.entrySet();
      for (Map.Entry<String, String> entry : entries) {
        MenuItem item = new MenuItem(entry.getKey());
        item.getStyleClass().add("default-text");
        item.setOnAction(new EventHandler<ActionEvent>() {
          @Override
          public void handle(ActionEvent event) {
            sqlText.setText(formatQuery(entry.getValue()));
            savePlaylist();
          }

          private String formatQuery(String value) {
            TreeItem<PlaylistRepresentation> selectedItem = treeView.getSelectionModel().getSelectedItem();
            PlaylistRepresentation pl = selectedItem.getValue();

            int emuId = getDefaultGameEmulator().getId();
            value = value.replaceAll("\\[EMULATOR_ID\\]", String.valueOf(emuId));
            return value;
          }
        });
        templateSelector.getItems().add(item);
      }

    }

    errorContainer.managedProperty().bindBidirectional(errorContainer.visibleProperty());

    UISettings uiSettings = client.getPreferenceService().getJsonPreference(PreferenceNames.UI_SETTINGS, UISettings.class);

    treeView.selectionModelProperty().get().selectedItemProperty().addListener(new ChangeListener<TreeItem<PlaylistRepresentation>>() {
      @Override
      public void changed(ObservableValue<? extends TreeItem<PlaylistRepresentation>> observable, TreeItem<PlaylistRepresentation> oldValue, TreeItem<PlaylistRepresentation> newValue) {
        if (newValue != null && newValue.getValue() != null) {
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

      final TextField directEditField = new TextField();

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

        @Override
        public void startEdit() {
          super.startEdit();
          String oldValue = getPlaylist().getName();
          directEditField.setText(oldValue);
          Platform.runLater(() -> {
            directEditField.requestFocus();
            directEditField.selectAll();
          });
          directEditField.setOnKeyReleased(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent t) {
              if (t.getCode() == KeyCode.ENTER) {
                String name = directEditField.getText();
                if (!StringUtils.isEmpty(name)) {
                  rename(name, oldValue, getPlaylist());
                }
                commitEdit(getPlaylist());
                //OLE done already in rename ?? 
                //savePlaylist();
              }
              else if (t.getCode() == KeyCode.ESCAPE) {
                cancelEdit();
              }
            }
          });
          setGraphic(directEditField);
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
          TableOverviewController.createPlaylistTooltip(newItem, playlistIcon);
          graphics.setGraphic(playlistIcon.getGraphic());
        }
      });

      if (client.getFrontendService().getFrontendType().equals(FrontendType.Popper)) {
        cell.setOnDragDetected((MouseEvent event) -> dragDetected(event, cell, treeView));
        cell.setOnDragOver((DragEvent event) -> dragOver(event, cell, treeView));
        cell.setOnDragDropped((DragEvent event) -> drop(event, cell, treeView));
        cell.setOnDragDone((DragEvent event) -> clearDropLocation());
      }

      return cell;
    });

    sqlCheckbox.selectedProperty().addListener(new ChangeListener<Boolean>() {
      @Override
      public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
        sqlText.setDisable(!newValue);
        templateSelector.setDisable(!newValue);
        if (!newValue) {
          errorContainer.setVisible(false);
          getPlaylist().setSqlError(null);
          saveSQLBtn.setDisable(true);
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

    disableSysListsCheckbox.selectedProperty().addListener(new ChangeListener<Boolean>() {
      @Override
      public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
        getPlaylist().setHideSysLists(newValue);
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
        saveSQLBtn.setDisable(false);
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
        if (!value.equalsIgnoreCase(newValue)) {
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
  }

  private void clearDropLocation() {
    if (dropZone != null) {
      dropZone.setStyle("");
    }
  }

  private void drop(DragEvent event, TreeCell<PlaylistRepresentation> treeCell, TreeView<PlaylistRepresentation> treeView) {
    Dragboard db = event.getDragboard();
    boolean success = false;
    if (!db.hasContent(JAVA_FORMAT)) {
      return;
    }

    TreeItem<PlaylistRepresentation> dropTarget = treeCell.getTreeItem();
    TreeItem<PlaylistRepresentation> droppedItemParent = draggedItem.getParent();

    // remove from previous location
    droppedItemParent.getChildren().remove(draggedItem);

    // dropping on parent node makes it the last child
    if (Objects.equals(droppedItemParent, dropTarget)) {
      dropTarget.getChildren().add(draggedItem);
      treeView.getSelectionModel().select(draggedItem);
    }
    else if (this.draggedItem.getValue().getParentId() == dropTarget.getValue().getParentId()) {
      //drop to a sibling changes order and moves it before the drop target
      PlaylistRepresentation targetList = dropTarget.getValue();
      int newIndex = dropTarget.getParent().getValue().getChildren().indexOf(targetList);

      dropTarget.getParent().getChildren().remove(draggedItem);
      dropTarget.getParent().getChildren().add(newIndex, draggedItem);
    }
    else {
      //drop above the parent
      TreeItem<PlaylistRepresentation> parent = dropTarget.getParent() != null ? dropTarget.getParent() : treeView.getRoot();
      int indexInParent = parent.getChildren().indexOf(dropTarget);
      draggedItem.getValue().setParentId(parent.getValue().getId());
      parent.getChildren().add(indexInParent + 1, draggedItem);
    }
    treeView.getSelectionModel().select(draggedItem);
    event.setDropCompleted(success);

    treeView.refresh();
    applyPlaylistOrder(treeView.getRoot(), 1000);
    savePlaylist();
    savePlaylistOrder();
    onReload();
  }

  private void dragOver(DragEvent event, TreeCell<PlaylistRepresentation> treeCell, TreeView<PlaylistRepresentation> treeView) {
    if (!event.getDragboard().hasContent(JAVA_FORMAT)) {
      return;
    }
    TreeItem thisItem = treeCell.getTreeItem();

    // can't drop on itself
    if (draggedItem == null || thisItem == null || thisItem == draggedItem) return;
    // ignore if this is the root
    if (draggedItem.getParent() == null) {
      clearDropLocation();
      return;
    }

    event.acceptTransferModes(TransferMode.MOVE);
    if (!Objects.equals(dropZone, treeCell)) {
      clearDropLocation();
      this.dropZone = treeCell;
      dropZone.setStyle(DROP_HINT_STYLE);
    }
  }

  private void dragDetected(MouseEvent event, TreeCell<PlaylistRepresentation> treeCell, TreeView<PlaylistRepresentation> treeView) {
    draggedItem = treeCell.getTreeItem();

    // root can't be dragged
    if (draggedItem.getParent() == null) {
      return;
    }
    Dragboard db = treeCell.startDragAndDrop(TransferMode.MOVE);
    ClipboardContent content = new ClipboardContent();
    content.put(JAVA_FORMAT, draggedItem.getValue());
    db.setContent(content);
    db.setDragView(treeCell.snapshot(null, null));
    event.consume();
  }

  private PlaylistRepresentation getPlaylist() {
    return treeView.getSelectionModel().getSelectedItem().getValue();
  }

  private void savePlaylistOrder() {
    PlaylistOrder order = new PlaylistOrder();
    collectPlaylistOrder(order, treeView.getRoot());
    client.getPlaylistsService().savePlaylistOrder(order);
  }

  private void savePlaylist() {
    if (saveDisabled) {
      return;
    }

    TreeItem<PlaylistRepresentation> selectedItem = treeView.getSelectionModel().getSelectedItem();
    if (selectedItem != null) {
      PlaylistRepresentation value = selectedItem.getValue();
      try {
        PlaylistRepresentation update = client.getPlaylistsService().savePlaylist(value);
        selectedItem.setValue(update);
        dirty = true;

        Platform.runLater(() -> {
          playlistTableController.setData(Optional.of(update));

          if (update.isSqlPlayList()) {
            errorLabel.setText(update.getSqlError());
            errorContainer.setVisible(!StringUtils.isEmpty(update.getSqlError()));
            saveSQLBtn.setDisable(true);
          }
          List<PlaylistGame> games = update.getGames();
          for (PlaylistGame playlistGame : games) {
            EventManager.getInstance().notifyTableChange(playlistGame.getId(), null);
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

  private void collectPlaylistOrder(PlaylistOrder order, TreeItem<PlaylistRepresentation> node) {
    List<TreeItem<PlaylistRepresentation>> children = node.getChildren();
    for (TreeItem<PlaylistRepresentation> child : children) {
      order.getPlaylistToOrderId().put(child.getValue().getId(), child.getValue().getDisplayOrder());
      collectPlaylistOrder(order, child);
    }
  }

  private GameEmulatorRepresentation getDefaultGameEmulator() {
    List<GameEmulatorRepresentation> gameEmulators = client.getEmulatorService().getVpxGameEmulators();
    gameEmulators.sort((o1, o2) -> o2.getId() - o1.getId());
    return gameEmulators.size() > 0 ? gameEmulators.get(0) : null;
  }


  private void applyPlaylistOrder(TreeItem<PlaylistRepresentation> node, int orderId) {
    List<TreeItem<PlaylistRepresentation>> children = node.getChildren();
    for (TreeItem<PlaylistRepresentation> child : children) {
      child.getValue().setDisplayOrder(orderId);
      orderId++;
      applyPlaylistOrder(child, orderId);
    }
  }

  public void refreshSidebar(PlaylistRepresentation playlist) {
    tableOverviewController.refreshPlaylists();
    TablesSidebarController sidebarController = tableOverviewController.getTablesController().getTablesSideBarController();
    TablesSidebarPlaylistsController playlistsController = sidebarController.getTablesSidebarPlaylistController();
    playlistsController.refreshView();
    playlistsController.refreshPlaylist(playlist, false);
  }
}
