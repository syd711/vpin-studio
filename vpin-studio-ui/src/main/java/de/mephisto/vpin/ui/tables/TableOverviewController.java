package de.mephisto.vpin.ui.tables;

import de.mephisto.vpin.commons.fx.ConfirmationResult;
import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.connectors.vps.VPS;
import de.mephisto.vpin.connectors.vps.model.VpsDiffTypes;
import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.altsound.AltSound;
import de.mephisto.vpin.restclient.games.GameEmulatorRepresentation;
import de.mephisto.vpin.restclient.games.GameRepresentation;
import de.mephisto.vpin.restclient.popper.PlaylistRepresentation;
import de.mephisto.vpin.restclient.popper.PopperScreen;
import de.mephisto.vpin.restclient.preferences.PreferenceChangeListener;
import de.mephisto.vpin.restclient.preferences.UISettings;
import de.mephisto.vpin.restclient.validation.GameValidationCode;
import de.mephisto.vpin.restclient.validation.ValidationState;
import de.mephisto.vpin.ui.NavigationController;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.StudioFXController;
import de.mephisto.vpin.ui.WaitOverlayController;
import de.mephisto.vpin.ui.events.EventManager;
import de.mephisto.vpin.ui.tables.editors.AltSound2EditorController;
import de.mephisto.vpin.ui.tables.editors.AltSoundEditorController;
import de.mephisto.vpin.ui.tables.editors.TableScriptEditorController;
import de.mephisto.vpin.ui.tables.validation.GameValidationTexts;
import de.mephisto.vpin.ui.util.*;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.util.Callback;
import org.apache.commons.lang3.StringUtils;
import org.kordamp.ikonli.javafx.FontIcon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;

import static de.mephisto.vpin.ui.Studio.client;
import static de.mephisto.vpin.ui.Studio.stage;

public class TableOverviewController implements Initializable, StudioFXController, ListChangeListener<GameRepresentation>, PreferenceChangeListener {
  private final static Logger LOG = LoggerFactory.getLogger(TableOverviewController.class);

  @FXML
  private TableColumn<GameRepresentation, Label> columnDisplayName;

  @FXML
  private TableColumn<GameRepresentation, Label> columnVersion;

  @FXML
  private TableColumn<GameRepresentation, Label> columnEmulator;

  @FXML
  private TableColumn<GameRepresentation, String> columnVPS;

  @FXML
  private TableColumn<GameRepresentation, Label> columnRom;

  @FXML
  private TableColumn<GameRepresentation, String> columnB2S;

  @FXML
  private TableColumn<GameRepresentation, String> columnStatus;

  @FXML
  private TableColumn<GameRepresentation, String> columnPUPPack;

  @FXML
  private TableColumn<GameRepresentation, String> columnAltSound;

  @FXML
  private TableColumn<GameRepresentation, String> columnAltColor;

  @FXML
  private TableColumn<GameRepresentation, String> columnPOV;

  @FXML
  private TableColumn<GameRepresentation, String> columnHSType;

  @FXML
  private TableColumn<GameRepresentation, String> columnPlaylists;

  @FXML
  private TableColumn<GameRepresentation, String> columnDateAdded;

  @FXML
  private TableView<GameRepresentation> tableView;

  @FXML
  private TextField textfieldSearch;

  @FXML
  private Label validationErrorLabel;

  @FXML
  private Label validationErrorText;

  @FXML
  private Node validationError;

  @FXML
  private Label labelTableCount;

  @FXML
  private Button tableEditBtn;

  @FXML
  private SplitMenuButton validateBtn;

  @FXML
  private MenuItem validateAllBtn;

  @FXML
  private Button assetManagerBtn;

  @FXML
  private Button deleteBtn;

  @FXML
  private SplitMenuButton scanBtn;

  @FXML
  private MenuItem scanAllBtn;

  @FXML
  private Button playBtn;

  @FXML
  private Button stopBtn;

  @FXML
  private Button importBtn;

  @FXML
  private Button backupBtn;

  @FXML
  private SplitMenuButton uploadTableBtn;

  @FXML
  private Button reloadBtn;

  @FXML
  private Button vpsBtn;

  @FXML
  private Button vpsResetBtn;

  @FXML
  private Button musicUploadBtn;

  @FXML
  private ComboBox<PlaylistRepresentation> playlistCombo;

  @FXML
  private StackPane tableStack;

  @FXML
  private MenuItem backglassUploadItem;

  @FXML
  private MenuItem romsUploadItem;

  @FXML
  private MenuItem altSoundUploadItem;

  @FXML
  private MenuItem altColorUploadItem;

  @FXML
  private MenuItem dmdUploadItem;

  @FXML
  private MenuItem pupPackUploadItem;

  @FXML
  private MenuItem povItem;

  private Parent tablesLoadingOverlay;
  private TablesController tablesController;
  private List<PlaylistRepresentation> playlists;
  private boolean showVersionUpdates = true;
  private boolean showVpsUpdates = true;
  private SimpleDateFormat dateAddedDateFormat = new SimpleDateFormat("yyyy-MM-dd");

  private long lastKeyInputTime = System.currentTimeMillis();
  private String lastKeyInput = "";

  // Add a public no-args constructor
  public TableOverviewController() {
  }

  private ObservableList<GameRepresentation> data;
  private List<GameRepresentation> games;

  @FXML
  private void onAltSoundUpload() {
    ObservableList<GameRepresentation> selectedItems = tableView.getSelectionModel().getSelectedItems();
    if (selectedItems != null && !selectedItems.isEmpty()) {
      boolean b = TableDialogs.openAltSoundUploadDialog(tablesController.getTablesSideBarController(), selectedItems.get(0), null);
      if (b) {
        tablesController.getTablesSideBarController().getTitledPaneAltSound().setExpanded(true);
      }
    }
  }

  @FXML
  private void onAltColorUpload() {
    ObservableList<GameRepresentation> selectedItems = tableView.getSelectionModel().getSelectedItems();
    if (selectedItems != null && !selectedItems.isEmpty()) {
      boolean b = TableDialogs.openAltColorUploadDialog(tablesController.getTablesSideBarController(), selectedItems.get(0), null);
      if (b) {
        tablesController.getTablesSideBarController().getTitledPaneAltColor().setExpanded(true);
      }
    }
  }

  @FXML
  private void onRomsUpload() {
    TableDialogs.onRomUploads(tablesController.getTablesSideBarController());
  }


  @FXML
  private void onMusicUpload() {
    TableDialogs.onMusicUploads(tablesController.getTablesSideBarController());
  }


  @FXML
  private void onPupPackUpload() {
    ObservableList<GameRepresentation> selectedItems = tableView.getSelectionModel().getSelectedItems();
    if (selectedItems != null && !selectedItems.isEmpty()) {
      boolean b = TableDialogs.openPupPackUploadDialog(tablesController.getTablesSideBarController(), selectedItems.get(0), null);
      if (b) {
        tablesController.getTablesSideBarController().getTitledPaneDirectB2s().setExpanded(true);
      }
    }
  }

  @FXML
  private void onBackglassUpload() {
    ObservableList<GameRepresentation> selectedItems = tableView.getSelectionModel().getSelectedItems();
    if (selectedItems != null && !selectedItems.isEmpty()) {
      boolean b = TableDialogs.directBackglassUpload(stage, selectedItems.get(0));
      if (b) {
        tablesController.getTablesSideBarController().getTitledPaneDirectB2s().setExpanded(true);
      }
    }
  }

  @FXML
  private void onDMDUpload() {
    ObservableList<GameRepresentation> selectedItems = tableView.getSelectionModel().getSelectedItems();
    if (selectedItems != null && !selectedItems.isEmpty()) {
      boolean b = TableDialogs.openDMDUploadDialog(tablesController.getTablesSideBarController(), selectedItems.get(0), null);
      if (b) {
        tablesController.getTablesSideBarController().getTitledPaneDMD().setExpanded(true);
      }
    }
  }

  @FXML
  private void onPOVUpload() {
    ObservableList<GameRepresentation> selectedItems = tableView.getSelectionModel().getSelectedItems();
    if (selectedItems != null && !selectedItems.isEmpty()) {
      boolean b = TableDialogs.openPovUploadDialog(tablesController.getTablesSideBarController(), selectedItems.get(0));
      if (b) {
        tablesController.getTablesSideBarController().getTitledPaneDMD().setExpanded(true);
      }
    }
  }

  @FXML
  private void onMediaEdit() {
    GameRepresentation selectedItems = tableView.getSelectionModel().getSelectedItem();
    TableDialogs.openPopperMediaAdminDialog(selectedItems, PopperScreen.BackGlass);
  }

  @FXML
  private void onVps() {
    GameRepresentation selectedItems = tableView.getSelectionModel().getSelectedItem();
    if (selectedItems != null && !StringUtils.isEmpty(selectedItems.getExtTableVersionId())) {
      Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
      if (desktop != null && desktop.isSupported(Desktop.Action.BROWSE)) {
        try {
          desktop.browse(new URI(VPS.getVpsTableUrl(selectedItems.getExtTableId())));
        } catch (Exception e) {
          LOG.error("Failed to open link: " + e.getMessage());
        }
      }
    }
  }

  @FXML
  private void onVpsReset() {
    ObservableList<GameRepresentation> selectedItems = tableView.getSelectionModel().getSelectedItems();
    TableActions.onVpsReset(selectedItems);
  }

  @FXML
  private void onTableEdit() {
    GameRepresentation selectedItems = tableView.getSelectionModel().getSelectedItem();
    if (selectedItems != null) {
      if (Studio.client.getPinUPPopperService().isPinUPPopperRunning()) {
        if (Dialogs.openPopperRunningWarning(Studio.stage)) {
          TableDialogs.openTableDataDialog(this, selectedItems);
        }
        return;
      }
      TableDialogs.openTableDataDialog(this, selectedItems);
    }
  }

  @FXML
  private void onBackup() {
    ObservableList<GameRepresentation> selectedItems = tableView.getSelectionModel().getSelectedItems();
    TableDialogs.openTablesBackupDialog(selectedItems);
  }

  @FXML
  private void onPlay() {
    GameRepresentation game = tableView.getSelectionModel().getSelectedItem();
    if (game != null) {
      UISettings uiSettings = client.getPreferenceService().getJsonPreference(PreferenceNames.UI_SETTINGS, UISettings.class);
      if (uiSettings.isHideVPXStartInfo()) {
        client.getVpxService().playGame(game.getId());
        return;
      }

      ConfirmationResult confirmationResult = WidgetFactory.showConfirmationWithCheckbox(stage, "Start playing table \"" + game.getGameDisplayName() + "\"?", "Start Table", "All existing VPX and Popper processes will be terminated.", null, "Do not shown again", false);
      if (!confirmationResult.isApplyClicked()) {
        if (confirmationResult.isChecked()) {
          uiSettings.setHideVPXStartInfo(true);
          client.getPreferenceService().setJsonPreference(PreferenceNames.UI_SETTINGS, uiSettings);
        }
        client.getVpxService().playGame(game.getId());
      }
    }
  }

  @FXML
  private void onStop() {
    GameRepresentation game = tableView.getSelectionModel().getSelectedItem();
    if (game != null) {
      Optional<ButtonType> result = WidgetFactory.showConfirmation(Studio.stage, "Stop all VPX and PinUP Popper processes?");
      if (result.isPresent() && result.get().equals(ButtonType.OK)) {
        client.getPinUPPopperService().terminatePopper();
      }
    }
  }

  @FXML
  private void onEmulatorSelect() {

  }

  @FXML
  private void onSearchKeyPressed(KeyEvent e) {
    if (e.getCode().equals(KeyCode.ENTER)) {
      tableView.getSelectionModel().select(0);
      tableView.requestFocus();
    }
  }

  @FXML
  private void onTableUpload() {
    if (client.getPinUPPopperService().isPinUPPopperRunning()) {
      if (Dialogs.openPopperRunningWarning(Studio.stage)) {
        openUploadDialog();
      }
      return;
    }

    openUploadDialog();
  }

  private void openUploadDialog() {
    GameRepresentation game = tableView.getSelectionModel().getSelectedItem();
    boolean updated = TableDialogs.openTableUploadDialog(game);
    if (updated) {
      onReload();

      if (this.games.contains(game)) {
        tableView.getSelectionModel().select(game);
      }
    }
  }

  @FXML
  private void onDelete() {
    if (client.getPinUPPopperService().isPinUPPopperRunning()) {
      if (Dialogs.openPopperRunningWarning(Studio.stage)) {
        deleteSelection();
      }
      return;
    }

    deleteSelection();
  }

  private void deleteSelection() {
    List<GameRepresentation> selectedGames = new ArrayList<>(tableView.getSelectionModel().getSelectedItems());
    if (selectedGames != null && !selectedGames.isEmpty()) {
      for (GameRepresentation game : selectedGames) {
        if (client.getCompetitionService().isGameReferencedByCompetitions(game.getId())) {
          WidgetFactory.showAlert(Studio.stage, "The table \"" + game.getGameDisplayName()
            + "\" is used by at least one competition.", "Delete all competitions for this table first.");
          return;
        }
      }

      tableView.getSelectionModel().clearSelection();
      boolean b = TableDialogs.openTableDeleteDialog(selectedGames, this.games);
      if (b) {
        this.onReload();
      }
    }
  }

  @FXML
  private void onTableMouseClicked(MouseEvent mouseEvent) {
    if (mouseEvent.getButton().equals(MouseButton.PRIMARY)) {
      if (mouseEvent.getClickCount() == 2) {
        onTableEdit();
      }
    }
  }

  @FXML
  private void onTablesScan() {
    List<GameRepresentation> selectedItems = new ArrayList<>(tableView.getSelectionModel().getSelectedItems());
    ProgressDialog.createProgressDialog(new TableScanProgressModel("Scanning Tables", selectedItems));
    if (selectedItems.size() == 1) {
      GameRepresentation gameRepresentation = selectedItems.get(0);
      EventManager.getInstance().notifyTableChange(gameRepresentation.getId(), gameRepresentation.getRom());
    }
    else {
      this.onReload();
    }
  }

  @FXML
  private void onTablesScanAll() {
    String title = "Re-scan all " + games.size() + " tables?";
    Optional<ButtonType> result = WidgetFactory.showConfirmation(Studio.stage, title,
      "Scanning will try to resolve ROM and highscore file names of the selected tables.", null, "Start Scan");
    if (result.isPresent() && result.get().equals(ButtonType.OK)) {
      client.clearCache();
      ProgressDialog.createProgressDialog(new TableScanProgressModel("Scanning Tables", this.games));
      this.onReload();
    }
  }

  @FXML
  private void onImport() {
    if (client.getPinUPPopperService().isPinUPPopperRunning()) {
      if (Dialogs.openPopperRunningWarning(Studio.stage)) {
        TableDialogs.openTableImportDialog();
      }
    }
    else {
      TableDialogs.openTableImportDialog();
    }
  }

  @FXML
  private void onValidate() {
    ObservableList<GameRepresentation> selectedItems = tableView.getSelectionModel().getSelectedItems();
    TableDialogs.openValidationDialog(new ArrayList<>(selectedItems), false);
  }

  @FXML
  private void onValidateAll() {
    TableDialogs.openValidationDialog(games, true);
  }

  @FXML
  private void onDismiss() {
    GameRepresentation game = tableView.getSelectionModel().getSelectedItem();
    ValidationState validationState = game.getValidationState();
    DismissalUtil.dismissValidation(game, validationState);
  }

  @FXML
  private void onDismissAll() {
    GameRepresentation game = tableView.getSelectionModel().getSelectedItem();
    TableDialogs.openDismissAllDialog(game);
  }

  public void reloadByRom(String rom) {
    List<GameRepresentation> gamesByRom = client.getGameService().getGamesByRom(rom);
    Platform.runLater(() -> {
      GameRepresentation selection = tableView.getSelectionModel().getSelectedItem();
      tableView.getSelectionModel().clearSelection();

      for (GameRepresentation g : gamesByRom) {
        GameRepresentation refreshedGame = client.getGameService().getGame(g.getId());
        int index = data.indexOf(refreshedGame);
        if (index != -1) {
          data.remove(index);
          data.add(index, refreshedGame);
        }
      }

      if (selection != null) {
        tableView.getSelectionModel().select(selection);
      }
      tableView.refresh();
    });
  }

  public void reloadByGameName(String gameName) {
    List<GameRepresentation> gamesByRom = client.getGameService().getGamesByGameName(gameName);
    Platform.runLater(() -> {
      GameRepresentation selection = tableView.getSelectionModel().getSelectedItem();
      tableView.getSelectionModel().clearSelection();

      for (GameRepresentation g : gamesByRom) {
        GameRepresentation refreshedGame = client.getGameService().getGame(g.getId());
        int index = data.indexOf(refreshedGame);
        if (index != -1) {
          data.remove(index);
          data.add(index, refreshedGame);
        }
      }

      if (selection != null) {
        tableView.getSelectionModel().select(selection);
      }
      tableView.refresh();
    });
  }

  public void reload(int id) {
    GameRepresentation refreshedGame = client.getGameService().getGame(id);

    Platform.runLater(() -> {
      tableView.getSelectionModel().getSelectedItems().removeListener(this);
      GameRepresentation selection = tableView.getSelectionModel().getSelectedItem();
      tableView.getSelectionModel().clearSelection();

      int index = data.indexOf(refreshedGame);
      if (index != -1) {
        data.remove(index);
        data.add(index, refreshedGame);
      }
      tableView.getSelectionModel().getSelectedItems().addListener(this);

      if (selection != null && data.contains(refreshedGame)) {
        tableView.getSelectionModel().select(refreshedGame);
      }
      tableView.refresh();
    });
  }

  public void showScriptEditor(GameRepresentation game) {
    String tableSource = client.getVpxService().getTableSource(game);
    if (!StringUtils.isEmpty(tableSource)) {
      try {
        FXMLLoader loader = new FXMLLoader(TableScriptEditorController.class.getResource("editor-tablescript.fxml"));
        BorderPane root = loader.load();
        root.setMaxWidth(Double.MAX_VALUE);

        StackPane editorRootStack = tablesController.getEditorRootStack();
        if (editorRootStack.getChildren().size() > 1) {
          return;
        }

        editorRootStack.getChildren().add(root);

        TableScriptEditorController editorController = loader.getController();

        String source = new String(Base64.getDecoder().decode(tableSource), Charset.forName("utf8"));
        editorController.setGame(game, source);
        editorController.setTablesController(tablesController);
      } catch (IOException e) {
        LOG.error("Failed to load VPX Editor: " + e.getMessage(), e);
      }
    }
  }

  public void showAltSoundEditor(GameRepresentation game, AltSound altSound) {
    String tableSource = client.getVpxService().getTableSource(game);
    if (!StringUtils.isEmpty(tableSource)) {
      try {
        FXMLLoader loader = new FXMLLoader(AltSoundEditorController.class.getResource("editor-altsound.fxml"));
        BorderPane root = loader.load();
        root.setMaxWidth(Double.MAX_VALUE);
        root.setMaxHeight(Double.MAX_VALUE);

        StackPane editorRootStack = tablesController.getEditorRootStack();
        if (editorRootStack.getChildren().size() > 1) {
          return;
        }

        editorRootStack.getChildren().add(root);

        AltSoundEditorController editorController = loader.getController();
        editorController.setAltSound(game, altSound);
        editorController.setTablesController(tablesController);
      } catch (IOException e) {
        LOG.error("Failed to load alt sound editor: " + e.getMessage(), e);
      }
    }
  }

  public void showAltSound2Editor(GameRepresentation game, AltSound altSound) {
    String tableSource = client.getVpxService().getTableSource(game);
    if (!StringUtils.isEmpty(tableSource)) {
      try {
        FXMLLoader loader = new FXMLLoader(AltSound2EditorController.class.getResource("editor-altsound2.fxml"));
        BorderPane root = loader.load();
        root.setMaxWidth(Double.MAX_VALUE);
        root.setMaxHeight(Double.MAX_VALUE);

        StackPane editorRootStack = tablesController.getEditorRootStack();
        if (editorRootStack.getChildren().size() > 1) {
          return;
        }

        editorRootStack.getChildren().add(root);
        AltSound2EditorController editorController = loader.getController();
        editorController.setAltSound(game, altSound);
        editorController.setTablesController(tablesController);
      } catch (IOException e) {
        LOG.error("Failed to load alt sound2 editor: " + e.getMessage(), e);
      }
    }
  }

  @FXML
  public void onReload() {
    UISettings uiSettings = client.getPreferenceService().getJsonPreference(PreferenceNames.UI_SETTINGS, UISettings.class);
    this.showVersionUpdates = !uiSettings.isHideVersions();
    this.showVpsUpdates = !uiSettings.isHideVPSUpdates();

    refreshPlaylists();

    this.textfieldSearch.setDisable(true);
    this.reloadBtn.setDisable(true);
    this.scanBtn.setDisable(true);
    this.scanAllBtn.setDisable(true);
    this.playBtn.setDisable(true);
    this.validateBtn.setDisable(true);
    this.tableEditBtn.setDisable(true);
    this.deleteBtn.setDisable(true);
    this.uploadTableBtn.setDisable(true);
    this.backupBtn.setDisable(true);
    this.importBtn.setDisable(true);
    this.stopBtn.setDisable(true);
    this.vpsBtn.setDisable(true);
    this.vpsResetBtn.setDisable(true);

    tableView.setVisible(false);
    tableStack.getChildren().add(tablesLoadingOverlay);

    new Thread(() -> {

      Platform.runLater(() -> {
        GameRepresentation selection = tableView.getSelectionModel().getSelectedItem();
        games = client.getGameService().getKnownGames();
        filterGames(games);

        tableView.setItems(data);
        tableView.refresh();

        if (selection != null) {
          final Optional<GameRepresentation> updatedGame = this.games.stream().filter(g -> g.getId() == selection.getId()).findFirst();
          if (updatedGame.isPresent()) {
            GameRepresentation gameRepresentation = updatedGame.get();
            tableView.getSelectionModel().select(gameRepresentation);
            this.playBtn.setDisable(!gameRepresentation.isGameFileAvailable());
            this.backupBtn.setDisable(!gameRepresentation.isGameFileAvailable());
          }
        }
        else if (!games.isEmpty()) {
          tableView.getSelectionModel().select(0);
        }

        tableStack.getChildren().remove(tablesLoadingOverlay);

        if (!games.isEmpty()) {
          this.validateBtn.setDisable(false);
          this.deleteBtn.setDisable(false);
          this.tableEditBtn.setDisable(false);

          GameRepresentation gameRepresentation = games.get(0);
          this.vpsBtn.setDisable(StringUtils.isEmpty(gameRepresentation.getExtTableVersionId()));
        }

        this.importBtn.setDisable(false);
        this.stopBtn.setDisable(false);
        this.textfieldSearch.setDisable(false);
        this.reloadBtn.setDisable(false);
        this.scanBtn.setDisable(false);
        this.scanAllBtn.setDisable(false);
        this.uploadTableBtn.setDisable(false);

        tableView.setVisible(true);
        labelTableCount.setText(games.size() + " tables");

        Platform.runLater(() -> {
          tableView.requestFocus();
        });
      });
    }).start();
  }

  private void refreshPlaylists() {
    this.playlistCombo.setDisable(true);
    playlists = new ArrayList<>(client.getPlaylistsService().getPlaylists());
    List<PlaylistRepresentation> pl = new ArrayList<>(playlists);
    pl.add(0, null);
    playlistCombo.setItems(FXCollections.observableList(pl));
    this.playlistCombo.setDisable(false);
  }

  private void bindSearchField() {
    textfieldSearch.textProperty().addListener((observableValue, s, filterValue) -> {
      tableView.getSelectionModel().clearSelection();
      refreshView(Optional.empty());

      filterGames(games);
      tableView.setItems(data);
    });
  }

  private void bindTable() {
    data = FXCollections.observableArrayList(Collections.emptyList());
    labelTableCount.setText(data.size() + " tables");
    tableView.setPlaceholder(new Label("No matching tables found."));

    tableView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

    columnDisplayName.setCellValueFactory(cellData -> {
      GameRepresentation value = cellData.getValue();
      Label label = new Label(value.getGameDisplayName());
      label.getStyleClass().add("default-text");
      label.setStyle(getLabelCss(value));
      return new SimpleObjectProperty(label);
    });

    columnEmulator.setCellValueFactory(cellData -> {
      GameRepresentation value = cellData.getValue();
      GameEmulatorRepresentation gameEmulator = client.getPinUPPopperService().getGameEmulator(value.getEmulatorId());
      Label label = new Label(gameEmulator.getName());
      label.getStyleClass().add("default-text");
      label.setStyle(getLabelCss(value));
      return new SimpleObjectProperty(label);
    });

    columnVersion.setCellValueFactory(cellData -> {
      GameRepresentation value = cellData.getValue();
      Label label = new Label(value.getVersion());
      label.getStyleClass().add("default-text");
      label.setStyle(getLabelCss(value));
      if (showVersionUpdates && value.isUpdateAvailable()) {
        FontIcon updateIcon = WidgetFactory.createUpdateIcon();
        Tooltip tt = new Tooltip("The table version in PinUP Popper is \"" + value.getVersion() + "\", while the linked VPS table has version \"" + value.getExtVersion() + "\".\n\n" +
          "Update the table, correct the selected VPS table or fix the version in the \"PinUP Popper Table Settings\" section.");
        tt.setWrapText(true);
        tt.setMaxWidth(400);
        label.setTooltip(tt);
        label.setGraphic(updateIcon);
      }
      return new SimpleObjectProperty(label);
    });

    columnRom.setCellValueFactory(cellData -> {
      GameRepresentation value = cellData.getValue();
      String rom = value.getRom();
      List<Integer> ignoredValidations = Collections.emptyList();
      if (value.getIgnoredValidations() != null) {
        ignoredValidations = value.getIgnoredValidations();
      }
      if (!value.isRomExists() && value.isRomRequired() && !ignoredValidations.contains(GameValidationCode.CODE_ROM_NOT_EXISTS)) {
        Label label = new Label(rom);
        String color = "#FF3333";
        label.setStyle("-fx-font-color: " + color + ";-fx-text-fill: " + color + ";-fx-font-weight: bold;");
        return new SimpleObjectProperty(label);
      }

      Label label = new Label(rom);
      label.getStyleClass().add("default-text");
      label.setStyle(getLabelCss(value));
      return new SimpleObjectProperty(label);
    });

    columnHSType.setCellValueFactory(cellData -> {
      GameRepresentation value = cellData.getValue();
      String hsType = value.getHighscoreType();
      if (!StringUtils.isEmpty(hsType) && hsType.equals("EM")) {
        hsType = "Text";
      }
      Label label = new Label(hsType);
      label.getStyleClass().add("default-text");
      label.setStyle(getLabelCss(value));
      return new SimpleObjectProperty(label);
    });

    columnB2S.setCellValueFactory(cellData -> {
      GameRepresentation value = cellData.getValue();
      if (value.isDirectB2SAvailable()) {
        if (this.showVpsUpdates && value.getUpdates().contains(VpsDiffTypes.b2s.name())) {
          HBox checkAndUpdateIcon = WidgetFactory.createCheckAndUpdateIcon("New backglass updates available");
          return new SimpleObjectProperty(checkAndUpdateIcon);
        }
        return new SimpleObjectProperty(WidgetFactory.createCheckboxIcon(getIconColor(value)));
      }
      return new SimpleStringProperty("");
    });

    columnVPS.setCellValueFactory(cellData -> {
      GameRepresentation value = cellData.getValue();
      try {
        if (!value.getUpdates().isEmpty()) {
          FontIcon updateIcon = WidgetFactory.createUpdateIcon();

          Label label = new Label();
          label.setGraphic(updateIcon);

          List<String> collect = value.getUpdates().stream().map(update -> "- " + VpsDiffTypes.valueOf(update)).collect(Collectors.toList());
          String tooltip = "The table or its assets have received updates:\n\n" + String.join("\n", collect) + "\n\nYou can reset this indicator with the VPS button from the toolbar.";
          Tooltip tt = new Tooltip(tooltip);
          tt.setWrapText(true);
          tt.setMaxWidth(400);
          label.setTooltip(tt);

          return new SimpleObjectProperty(label);
        }
      } catch (Exception e) {
        LOG.error("Failed to render VPS update: " + e.getMessage());
      }
      return new SimpleStringProperty("");
    });

    columnPOV.setCellValueFactory(cellData -> {
      GameRepresentation value = cellData.getValue();
      if (value.isPovAvailable() || value.isIniAvailable()) {
        if (this.showVpsUpdates && value.getUpdates().contains(VpsDiffTypes.pov.name())) {
          HBox checkAndUpdateIcon = WidgetFactory.createCheckAndUpdateIcon("New POV updates available");
          return new SimpleObjectProperty(checkAndUpdateIcon);
        }
        FontIcon checkboxIcon = WidgetFactory.createCheckboxIcon(getIconColor(value));
        if (value.isPovAvailable()) {
          Tooltip.install(checkboxIcon, new Tooltip("POV file available"));
        }
        else {
          Tooltip.install(checkboxIcon, new Tooltip("INI file available"));
        }
        return new SimpleObjectProperty(checkboxIcon);
      }
      return new SimpleStringProperty("");
    });

    columnAltSound.setCellValueFactory(cellData -> {
      GameRepresentation value = cellData.getValue();
      if (value.isAltSoundAvailable()) {
        if (this.showVpsUpdates && value.getUpdates().contains(VpsDiffTypes.altSound.name())) {
          HBox checkAndUpdateIcon = WidgetFactory.createCheckAndUpdateIcon("New ALT sound updates available");
          return new SimpleObjectProperty(checkAndUpdateIcon);
        }
        return new SimpleObjectProperty(WidgetFactory.createCheckboxIcon(getIconColor(value)));
      }
      return new SimpleStringProperty("");
    });

    columnAltColor.setCellValueFactory(cellData -> {
      GameRepresentation value = cellData.getValue();
      if (value.getAltColorType() != null) {
        if (this.showVpsUpdates && value.getUpdates().contains(VpsDiffTypes.altColor.name())) {
          HBox checkAndUpdateIcon = WidgetFactory.createCheckAndUpdateIcon("New ALT color updates available");
          return new SimpleObjectProperty(checkAndUpdateIcon);
        }
        return new SimpleObjectProperty(WidgetFactory.createCheckboxIcon(getIconColor(value)));
      }
      return new SimpleStringProperty("");
    });


    columnPUPPack.setCellValueFactory(cellData -> {
      GameRepresentation value = cellData.getValue();
      if (value.isPupPackAvailable()) {
        if (this.showVpsUpdates && value.getUpdates().contains(VpsDiffTypes.pupPack.name())) {
          HBox checkAndUpdateIcon = WidgetFactory.createCheckAndUpdateIcon("New PUP pack updates available");
          return new SimpleObjectProperty(checkAndUpdateIcon);
        }
        return new SimpleObjectProperty(WidgetFactory.createCheckboxIcon(getIconColor(value)));
      }
      return new SimpleStringProperty("");
    });

    columnStatus.setCellValueFactory(cellData -> {
      GameRepresentation value = cellData.getValue();
      ValidationState validationState = value.getValidationState();
      if (value.getIgnoredValidations() != null && !value.getIgnoredValidations().contains(-1)) {
        if (validationState != null && validationState.getCode() > 0) {
          return new SimpleObjectProperty(WidgetFactory.createExclamationIcon(getIconColor(value)));
        }
      }

      return new SimpleObjectProperty(WidgetFactory.createCheckIcon(getIconColor(value)));
    });

    columnDateAdded.setCellValueFactory(cellData -> {
      GameRepresentation value = cellData.getValue();
      if (value.getDateAdded() != null) {
        return new SimpleObjectProperty(dateAddedDateFormat.format(value.getDateAdded()));
      }
      return new SimpleObjectProperty<>("-");
    });

    columnPlaylists.setSortable(false);
    columnPlaylists.setCellValueFactory(cellData -> {
      GameRepresentation value = cellData.getValue();
      HBox box = new HBox();
      List<PlaylistRepresentation> matches = new ArrayList<>();
      for (PlaylistRepresentation playlist : playlists) {
        if (playlist.getGameIds().contains(value.getId())) {
          matches.add(playlist);
        }
      }

      for (PlaylistRepresentation match : matches) {
        box.getChildren().add(WidgetFactory.createPlaylistIcon(match));
        if (box.getChildren().size() == 3 && matches.size() > box.getChildren().size()) {
          Label label = new Label("+" + (matches.size() - box.getChildren().size()));
          label.setStyle("-fx-font-size: 14px;-fx-font-weight: bold; -fx-padding: 1 0 0 0;");
          box.getChildren().add(label);
          break;
        }
      }
      box.setStyle("-fx-padding: 3 0 0 0;");
      return new SimpleObjectProperty(box);
    });


    tableView.setItems(data);
    tableView.setEditable(true);
    tableView.getSelectionModel().getSelectedItems().addListener(this);
    tableView.setSortPolicy(new Callback<TableView<GameRepresentation>, Boolean>() {
      @Override
      public Boolean call(TableView<GameRepresentation> gameRepresentationTableView) {
        GameRepresentation selectedItem = tableView.getSelectionModel().getSelectedItem();
        if (!gameRepresentationTableView.getSortOrder().isEmpty()) {
          TableColumn<GameRepresentation, ?> column = gameRepresentationTableView.getSortOrder().get(0);
          if (column.equals(columnDisplayName)) {
            Collections.sort(tableView.getItems(), Comparator.comparing(o -> o.getGameDisplayName()));
            if (column.getSortType().equals(TableColumn.SortType.DESCENDING)) {
              Collections.reverse(tableView.getItems());
            }
            return true;
          }
          else if (column.equals(columnVersion)) {
            Collections.sort(tableView.getItems(), Comparator.comparing(o -> String.valueOf(o.getVersion())));
            if (column.getSortType().equals(TableColumn.SortType.DESCENDING)) {
              Collections.reverse(tableView.getItems());
            }
            return true;
          }
          else if (column.equals(columnEmulator)) {
            Collections.sort(tableView.getItems(), Comparator.comparing(o -> o.getEmulatorId()));
            if (column.getSortType().equals(TableColumn.SortType.DESCENDING)) {
              Collections.reverse(tableView.getItems());
            }
            return true;
          }
          else if (column.equals(columnDateAdded)) {
            Collections.sort(tableView.getItems(), (o1, o2) -> {
              Date date1 = o1.getDateAdded() == null ? new Date() : o1.getDateAdded();
              Date date2 = o2.getDateAdded() == null ? new Date() : o2.getDateAdded();
              return date1.compareTo(date2);
            });
            if (column.getSortType().equals(TableColumn.SortType.DESCENDING)) {
              Collections.reverse(tableView.getItems());
            }
            return true;
          }
          else if (column.equals(columnB2S)) {
            Collections.sort(tableView.getItems(), Comparator.comparing(GameRepresentation::isDirectB2SAvailable));
            if (column.getSortType().equals(TableColumn.SortType.DESCENDING)) {
              Collections.reverse(tableView.getItems());
            }
            return true;
          }
          else if (column.equals(columnVPS)) {
            Collections.sort(tableView.getItems(), (o1, o2) -> {
              if (o1.getUpdates() == null || o1.getUpdates().isEmpty()) {
                return -1;
              }
              return 1;
            });
            if (column.getSortType().equals(TableColumn.SortType.DESCENDING)) {
              Collections.reverse(tableView.getItems());
            }
            return true;
          }
          else if (column.equals(columnPUPPack)) {
            Collections.sort(tableView.getItems(), Comparator.comparing(GameRepresentation::isPupPackAvailable));
            if (column.getSortType().equals(TableColumn.SortType.DESCENDING)) {
              Collections.reverse(tableView.getItems());
            }
            return true;
          }
          else if (column.equals(columnAltColor)) {
            Collections.sort(tableView.getItems(), Comparator.comparing(o -> String.valueOf(o.getAltColorType())));
            if (column.getSortType().equals(TableColumn.SortType.DESCENDING)) {
              Collections.reverse(tableView.getItems());
            }
            return true;
          }
          else if (column.equals(columnAltSound)) {
            Collections.sort(tableView.getItems(), Comparator.comparing(GameRepresentation::isAltSoundAvailable));
            if (column.getSortType().equals(TableColumn.SortType.DESCENDING)) {
              Collections.reverse(tableView.getItems());
            }
            return true;
          }
          else if (column.equals(columnRom)) {
            Collections.sort(tableView.getItems(), Comparator.comparing(o -> String.valueOf(o.getRom())));
            if (column.getSortType().equals(TableColumn.SortType.DESCENDING)) {
              Collections.reverse(tableView.getItems());
            }
            return true;
          }
          else if (column.equals(columnPOV)) {
            Collections.sort(tableView.getItems(), Comparator.comparing(GameRepresentation::isPovAvailable));
            if (column.getSortType().equals(TableColumn.SortType.DESCENDING)) {
              Collections.reverse(tableView.getItems());
            }
            return true;
          }
          else if (column.equals(columnHSType)) {
            Collections.sort(tableView.getItems(), Comparator.comparing(o -> String.valueOf(o.getHighscoreType())));
            if (column.getSortType().equals(TableColumn.SortType.DESCENDING)) {
              Collections.reverse(tableView.getItems());
            }
            return true;
          }
        }
        return true;
      }
    });

    tableView.setRowFactory(
      tableView -> {
        final TableRow<GameRepresentation> row = new TableRow<>();
        final ContextMenu rowMenu = new ContextMenu();

        MenuItem scanItem = new MenuItem("Scan");
        scanItem.setGraphic(WidgetFactory.createIcon("mdi2m-map-search-outline"));
        scanItem.setOnAction(actionEvent -> onTablesScan());
        scanItem.setDisable(tableView.getSelectionModel().isEmpty());
        rowMenu.getItems().add(scanItem);

        MenuItem scanAllItem = new MenuItem("Scan All");
        scanAllItem.setGraphic(WidgetFactory.createIcon("mdi2m-map-search"));
        scanAllItem.setOnAction(actionEvent -> onTablesScanAll());
        rowMenu.getItems().add(scanAllItem);

        rowMenu.getItems().add(new SeparatorMenuItem());

        MenuItem validateItem = new MenuItem("Validate");
        validateItem.setGraphic(WidgetFactory.createIcon("mdi2m-magnify"));
        validateItem.setDisable(tableView.getSelectionModel().isEmpty());
        validateItem.setOnAction(actionEvent -> onValidate());
        rowMenu.getItems().add(validateItem);

        rowMenu.getItems().add(new SeparatorMenuItem());

        MenuItem launchItem = new MenuItem("Launch");
        launchItem.setGraphic(WidgetFactory.createGreenIcon("mdi2p-play"));
        launchItem.setDisable(tableView.getSelectionModel().isEmpty());
        launchItem.setOnAction(actionEvent -> onPlay());
        rowMenu.getItems().add(launchItem);

        rowMenu.getItems().add(new SeparatorMenuItem());

        MenuItem exportItem = new MenuItem("Export");
        exportItem.setGraphic(WidgetFactory.createIcon("mdi2e-export"));
        exportItem.setDisable(tableView.getSelectionModel().isEmpty());
        exportItem.setOnAction(actionEvent -> onBackup());
        rowMenu.getItems().add(exportItem);

        rowMenu.getItems().add(new SeparatorMenuItem());


        MenuItem removeItem = new MenuItem("Delete");
        removeItem.setOnAction(actionEvent -> onDelete());
        removeItem.setDisable(tableView.getSelectionModel().isEmpty());
        removeItem.setGraphic(WidgetFactory.createAlertIcon("mdi2d-delete-outline"));
        rowMenu.getItems().add(removeItem);

        // only display context menu for non-empty rows:
        row.contextMenuProperty().bind(
          Bindings.when(row.emptyProperty())
            .then((ContextMenu) null)
            .otherwise(rowMenu));
        return row;
      });


    tableView.setOnKeyPressed(new EventHandler<KeyEvent>() {
      @Override
      public void handle(KeyEvent event) {
        if (Keys.isSpecial(event)) {
          return;
        }

        String text = event.getText();

        long timeDiff = System.currentTimeMillis() - lastKeyInputTime;
        if (timeDiff > 800) {
          lastKeyInputTime = System.currentTimeMillis();
          lastKeyInput = text;
        }
        else {
          lastKeyInputTime = System.currentTimeMillis();
          lastKeyInput = lastKeyInput + text;
          text = lastKeyInput;
        }

        for (GameRepresentation game : data) {
          if (game.getGameDisplayName().toLowerCase().startsWith(text.toLowerCase())) {
            tableView.getSelectionModel().clearSelection();
            tableView.getSelectionModel().select(game);
            tableView.scrollTo(tableView.getSelectionModel().getSelectedItem());
            break;
          }
        }
      }
    });
  }

  private void filterGames(List<GameRepresentation> games) {
    List<GameRepresentation> filtered = new ArrayList<>();
    String filterValue = textfieldSearch.textProperty().getValue();

    PlaylistRepresentation playlist = playlistCombo.getValue();

    for (GameRepresentation game : games) {
      if (playlist != null && !playlist.getGameIds().contains(game.getId())) {
        continue;
      }

      if (game.getGameDisplayName().toLowerCase().contains(filterValue.toLowerCase())) {
        filtered.add(game);
      }
      else if (!StringUtils.isEmpty(game.getRom()) && game.getRom().toLowerCase().contains(filterValue.toLowerCase())) {
        filtered.add(game);
      }
    }
    data = FXCollections.observableList(filtered);
  }

  private void refreshView(Optional<GameRepresentation> g) {
    validationError.setVisible(false);
    validationErrorLabel.setText("");
    validationErrorText.setText("");
    this.tablesController.getTablesSideBarController().setGame(g);
    if (g.isPresent()) {
      GameRepresentation game = g.get();
      validationError.setVisible(game.getValidationState().getCode() > 0 && !game.getIgnoredValidations().contains(-1));
      if (game.getValidationState().getCode() > 0) {
        LocalizedValidation validationMessage = GameValidationTexts.validate(game);
        validationErrorLabel.setText(validationMessage.getLabel());
        validationErrorText.setText(validationMessage.getText());
      }
      NavigationController.setBreadCrumb(Arrays.asList("Tables", game.getGameDisplayName()));
    }
    else {
      validationError.setVisible(false);
      NavigationController.setBreadCrumb(Arrays.asList("Tables"));
    }
  }

  @Override
  public void onViewActivated() {
    NavigationController.setBreadCrumb(Arrays.asList("Tables"));
  }

  public void setRootController(TablesController tablesController) {
    this.tablesController = tablesController;
    this.onReload();
  }

  public List<GameRepresentation> getGames() {
    return games;
  }

  public void initSelection() {
    GameRepresentation game = tableView.getSelectionModel().getSelectedItem();
    if (game != null) {
      NavigationController.setBreadCrumb(Arrays.asList("Tables", game.getGameDisplayName()));
    }
  }

  public GameRepresentation getSelection() {
    return tableView.getSelectionModel().getSelectedItem();
  }

  public List<PlaylistRepresentation> getPlaylists() {
    return this.playlists;
  }

  public void updatePlaylist(PlaylistRepresentation update) {
    int pos = this.playlists.indexOf(update);
    this.playlists.remove(update);
    this.playlists.add(pos, update);

    List<PlaylistRepresentation> refreshedData = new ArrayList<>(this.playlists);
    refreshedData.add(0, null);
    this.playlistCombo.setItems(FXCollections.observableList(refreshedData));

    GameRepresentation selectedItem = this.tableView.getSelectionModel().getSelectedItem();
    if (selectedItem != null) {
      EventManager.getInstance().notifyTableChange(selectedItem.getId(), null);
    }
  }

  @Override
  public void onChanged(Change<? extends GameRepresentation> c) {
    boolean disable = c.getList().isEmpty() || c.getList().size() > 1;
    altColorUploadItem.setDisable(disable);
    altSoundUploadItem.setDisable(disable);
    pupPackUploadItem.setDisable(disable);
    dmdUploadItem.setDisable(disable);
    povItem.setDisable(disable);
    backglassUploadItem.setDisable(disable);


    validateBtn.setDisable(c.getList().isEmpty());
    deleteBtn.setDisable(c.getList().isEmpty());
    backupBtn.setDisable(true);
    playBtn.setDisable(disable);
    scanBtn.setDisable(c.getList().isEmpty());
    assetManagerBtn.setDisable(disable);
    tableEditBtn.setDisable(disable);
    validationError.setVisible(c.getList().size() != 1);

    vpsBtn.setDisable(c.getList().size() != 1 || StringUtils.isEmpty(c.getList().get(0).getExtTableId()));
    vpsResetBtn.setDisable(c.getList().stream().filter(g -> !g.getUpdates().isEmpty()).collect(Collectors.toList()).isEmpty());

    if (c.getList().isEmpty()) {
      refreshView(Optional.empty());
    }
    else {
      GameRepresentation gameRepresentation = c.getList().get(0);
      backupBtn.setDisable(!gameRepresentation.isGameFileAvailable());
      playBtn.setDisable(!gameRepresentation.isGameFileAvailable());
      refreshView(Optional.ofNullable(gameRepresentation));
    }
  }

  public void closeEditors() {
    StackPane editorRootStack = tablesController.getEditorRootStack();
    List<Node> nodes = new ArrayList<>(editorRootStack.getChildren());
    for (Node node : nodes) {
      if (node instanceof TabPane) {
        continue;
      }
      editorRootStack.getChildren().remove(node);
    }
  }


  private String getIconColor(GameRepresentation value) {
    if (value.isDisabled()) {
      return "#B0ABAB";
    }
    return null;
  }

  private String getLabelCss(GameRepresentation value) {
    String status = "";
    if (value.isDisabled()) {
      status = "-fx-font-color: #B0ABAB;-fx-text-fill:#B0ABAB;";
    }
    return status;
  }

  public void setSelection(GameRepresentation game) {
    tablesController.getTabPane().getSelectionModel().select(0);
    this.tableView.getSelectionModel().clearSelection();
    this.tableView.getSelectionModel().select(game);
    this.tableView.scrollTo(tableView.getSelectionModel().getSelectedItem());
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    new Thread(() -> {
      try {
        VPS.getInstance().update();
      } catch (Exception e) {
        LOG.error("VPS update failed: " + e.getMessage(), e);
      }
    }).start();

    try {
      FXMLLoader loader = new FXMLLoader(WaitOverlayController.class.getResource("overlay-wait.fxml"));
      tablesLoadingOverlay = loader.load();
      WaitOverlayController ctrl = loader.getController();
      ctrl.setLoadingMessage("Loading Tables...");
    } catch (IOException e) {
      LOG.error("Failed to load loading overlay: " + e.getMessage());
    }


    playlistCombo.setCellFactory(c -> new WidgetFactory.PlaylistBackgroundImageListCell());
    playlistCombo.setButtonCell(new WidgetFactory.PlaylistBackgroundImageListCell());
    playlistCombo.valueProperty().addListener(new ChangeListener<PlaylistRepresentation>() {
      @Override
      public void changed(ObservableValue<? extends PlaylistRepresentation> observableValue, PlaylistRepresentation playlistRepresentation, PlaylistRepresentation t1) {
        tableView.getSelectionModel().clearSelection();
        filterGames(games);
        tableView.setItems(data);

        if (!data.isEmpty()) {
          tableView.getSelectionModel().select(0);
        }
      }
    });

    bindTable();
    bindSearchField();


    Image image = new Image(Studio.class.getResourceAsStream("vps.png"));
    ImageView view = new ImageView(image);
    view.setFitWidth(18);
    view.setFitHeight(18);
    vpsBtn.setGraphic(view);

    Image image2 = new Image(Studio.class.getResourceAsStream("vps-checked.png"));
    ImageView view2 = new ImageView(image2);
    view2.setFitWidth(18);
    view2.setFitHeight(18);
    vpsResetBtn.setGraphic(view2);

    Image image3 = new Image(Studio.class.getResourceAsStream("popper-media.png"));
    ImageView view3 = new ImageView(image3);
    view3.setFitWidth(18);
    view3.setFitHeight(18);
    assetManagerBtn.setGraphic(view3);

    Image image4 = new Image(Studio.class.getResourceAsStream("popper-edit.png"));
    ImageView view4 = new ImageView(image4);
    view4.setFitWidth(18);
    view4.setFitHeight(18);
    tableEditBtn.setGraphic(view4);

    columnEmulator.setVisible(false);
    preferencesChanged(PreferenceNames.UI_SETTINGS, null);
    client.getPreferenceService().addListener(this);
    Platform.runLater(() -> {
      Dialogs.openUpdateInfoDialog(client.getSystemService().getVersion(), false);
    });
  }

  @Override
  public void preferencesChanged(String key, Object value) {
    if (key.equals(PreferenceNames.UI_SETTINGS)) {
      UISettings uiSettings = client.getPreferenceService().getJsonPreference(PreferenceNames.UI_SETTINGS, UISettings.class);
      columnEmulator.setVisible(!uiSettings.isHideEmulatorColumn());
    }
  }

  public void selectPrevious() {
    int selectedIndex = this.tableView.getSelectionModel().getSelectedIndex();
    if (!tableView.getItems().isEmpty() && selectedIndex > 0) {
      tableView.getSelectionModel().clearSelection();
      tableView.getSelectionModel().select((selectedIndex - 1));
    }
  }

  public void selectNext() {
    int selectedIndex = this.tableView.getSelectionModel().getSelectedIndex();
    if (!tableView.getItems().isEmpty() && (selectedIndex + 1) < tableView.getItems().size()) {
      tableView.getSelectionModel().clearSelection();
      tableView.getSelectionModel().select((selectedIndex + 1));
    }
  }
}