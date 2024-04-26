package de.mephisto.vpin.ui.tables;

import de.mephisto.vpin.commons.fx.ConfirmationResult;
import de.mephisto.vpin.commons.fx.Features;
import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.connectors.vps.VPS;
import de.mephisto.vpin.connectors.vps.model.VPSChange;
import de.mephisto.vpin.connectors.vps.model.VpsDiffTypes;
import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.altsound.AltSound;
import de.mephisto.vpin.restclient.games.FilterSettings;
import de.mephisto.vpin.restclient.games.GameEmulatorRepresentation;
import de.mephisto.vpin.restclient.games.GameMediaItemRepresentation;
import de.mephisto.vpin.restclient.games.GameRepresentation;
import de.mephisto.vpin.restclient.games.descriptors.TableUploadDescriptor;
import de.mephisto.vpin.restclient.popper.Playlist;
import de.mephisto.vpin.restclient.popper.PopperScreen;
import de.mephisto.vpin.restclient.preferences.PreferenceChangeListener;
import de.mephisto.vpin.restclient.preferences.ServerSettings;
import de.mephisto.vpin.restclient.preferences.UISettings;
import de.mephisto.vpin.restclient.validation.GameValidationCode;
import de.mephisto.vpin.restclient.validation.ValidationState;
import de.mephisto.vpin.ui.NavigationController;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.StudioFXController;
import de.mephisto.vpin.ui.WaitOverlayController;
import de.mephisto.vpin.ui.events.EventManager;
import de.mephisto.vpin.ui.tables.dialogs.TableUploadResult;
import de.mephisto.vpin.ui.tables.editors.AltSound2EditorController;
import de.mephisto.vpin.ui.tables.editors.AltSoundEditorController;
import de.mephisto.vpin.ui.tables.editors.TableScriptEditorController;
import de.mephisto.vpin.ui.tables.validation.GameValidationTexts;
import de.mephisto.vpin.ui.util.*;
import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
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
import javafx.scene.paint.Paint;
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
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static de.mephisto.vpin.ui.Studio.client;
import static de.mephisto.vpin.ui.Studio.stage;

public class TableOverviewController implements Initializable, StudioFXController, ListChangeListener<GameRepresentation>, PreferenceChangeListener {
  private final static Logger LOG = LoggerFactory.getLogger(TableOverviewController.class);

  @FXML
  private Separator vpsSeparator;
  @FXML
  private Separator deleteSeparator;
  @FXML
  private Separator b2sManagerSeparator;
  @FXML
  private Separator stopSeparator;

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
  private TableColumn<GameRepresentation, String> columnPlayfield;

  @FXML
  private TableColumn<GameRepresentation, String> columnBackglass;

  @FXML
  private TableColumn<GameRepresentation, String> columnLoading;

  @FXML
  private TableColumn<GameRepresentation, String> columnWheel;

  @FXML
  private TableColumn<GameRepresentation, String> columnDMD;

  @FXML
  private TableColumn<GameRepresentation, String> columnTopper;

  @FXML
  private TableColumn<GameRepresentation, String> columnFullDMD;

  @FXML
  private TableColumn<GameRepresentation, String> columnAudio;

  @FXML
  private TableColumn<GameRepresentation, String> columnAudioLaunch;

  @FXML
  private TableColumn<GameRepresentation, String> columnInfo;

  @FXML
  private TableColumn<GameRepresentation, String> columnHelp;

  @FXML
  private TableColumn<GameRepresentation, String> columnOther2;

  @FXML
  private TableView<GameRepresentation> tableView;

  @FXML
  private ComboBox<GameEmulatorRepresentation> emulatorCombo;

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
  private Button assetManagerViewBtn;

  @FXML
  private Button backglassBtn;

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
  private ComboBox<Playlist> playlistCombo;

  @FXML
  private StackPane tableStack;

  @FXML
  private MenuItem backglassUploadItem;

  @FXML
  private MenuItem romsUploadItem;

  @FXML
  private MenuItem iniUploadMenuItem;

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

  @FXML
  private Button filterBtn;

  @FXML
  private Hyperlink dismissBtn;

  @FXML
  private StackPane loaderStack;

  private Parent tablesLoadingOverlay;
  private TablesController tablesController;
  private List<Playlist> playlists;
  private boolean showVersionUpdates = true;
  private boolean showVpsUpdates = true;
  private final SimpleDateFormat dateAddedDateFormat = new SimpleDateFormat("yyyy-MM-dd");

  private long lastKeyInputTime = System.currentTimeMillis();
  private String lastKeyInput = "";
  private UISettings uiSettings;
  private ServerSettings serverSettings;

  private TableFilterController tableFilterController;
  private List<Integer> filteredIds = new ArrayList<>();

  private final List<Consumer> reloadConsumers = new ArrayList<>();

  private boolean assetManagerMode = false;

  // Add a public no-args constructor
  public TableOverviewController() {
  }

  private ObservableList<GameRepresentation> data;
  private List<GameRepresentation> games;

  @FXML
  private void onBackglassManager() {
    TableDialogs.openDirectB2sManagerDialog(tablesController.getTablesSideBarController());
  }

  @FXML
  private void onAssetView() {
    tablesController.getTablesSideBarController().getTitledPaneMedia().setExpanded(false);

    assetManagerMode = !assetManagerMode;
    tablesController.getAssetViewSideBarController().setVisible(assetManagerMode);
    tablesController.getTablesSideBarController().setVisible(!assetManagerMode);

    if (assetManagerMode) {
      tablesController.getAssetViewSideBarController().setGame(this.tablesController.getTableOverviewController(), tableView.getSelectionModel().getSelectedItem(), PopperScreen.Wheel);
      assetManagerViewBtn.getStyleClass().add("toggle-selected");
      if (!assetManagerViewBtn.getStyleClass().contains("toggle-button-selected")) {
        assetManagerViewBtn.getStyleClass().add("toggle-button-selected");
      }
    }
    else {
      assetManagerViewBtn.getStyleClass().remove("toggle-selected");
      assetManagerViewBtn.getStyleClass().remove("toggle-button-selected");
    }


    columnPlayfield.setVisible(assetManagerMode);
    columnBackglass.setVisible(assetManagerMode);
    columnLoading.setVisible(assetManagerMode);
    columnWheel.setVisible(assetManagerMode);
    columnDMD.setVisible(assetManagerMode);
    columnTopper.setVisible(assetManagerMode);
    columnFullDMD.setVisible(assetManagerMode);
    columnAudio.setVisible(assetManagerMode);
    columnAudioLaunch.setVisible(assetManagerMode);
    columnInfo.setVisible(assetManagerMode);
    columnHelp.setVisible(assetManagerMode);
    columnOther2.setVisible(assetManagerMode);

    columnVersion.setVisible(!assetManagerMode);
    columnEmulator.setVisible(!assetManagerMode);
    columnVPS.setVisible(!assetManagerMode && !uiSettings.isHideVPSUpdates());
    columnRom.setVisible(!assetManagerMode);
    columnB2S.setVisible(!assetManagerMode);
    columnPUPPack.setVisible(!assetManagerMode);
    columnAltSound.setVisible(!assetManagerMode);
    columnAltColor.setVisible(!assetManagerMode);
    columnPOV.setVisible(!assetManagerMode);
    columnHSType.setVisible(!assetManagerMode);
    columnPlaylists.setVisible(!assetManagerMode);
    columnDateAdded.setVisible(!assetManagerMode);
  }

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
  private void onIniUpload() {
    ObservableList<GameRepresentation> selectedItems = tableView.getSelectionModel().getSelectedItems();
    if (selectedItems != null && !selectedItems.isEmpty()) {
      TableDialogs.iniUpload(stage, selectedItems.get(0));
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
    TableDialogs.openTableAssetsDialog(this, selectedItems, PopperScreen.BackGlass);
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
  private void onFilter() {
    tableFilterController.toggle();
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
    Optional<ButtonType> result = WidgetFactory.showConfirmation(Studio.stage, "Stop all VPX and PinUP Popper processes?");
    if (result.isPresent() && result.get().equals(ButtonType.OK)) {
      client.getPinUPPopperService().terminatePopper();
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
    openUploadDialogWithCheck(TableUploadDescriptor.uploadAndImport);
  }

  private void openUploadDialogWithCheck(TableUploadDescriptor uploadDescriptor) {
    if (client.getPinUPPopperService().isPinUPPopperRunning()) {
      if (Dialogs.openPopperRunningWarning(Studio.stage)) {
        openUploadDialog(uploadDescriptor);
      }
      return;
    }

    openUploadDialog(uploadDescriptor);
  }

  private void openUploadDialog(TableUploadDescriptor uploadDescriptor) {
    GameRepresentation game = tableView.getSelectionModel().getSelectedItem();
    Optional<TableUploadResult> uploadResult = TableDialogs.openTableUploadDialog(game, uploadDescriptor);
    if (uploadResult.isPresent() && uploadResult.get().getGameId() != -1) {
      Consumer<GameRepresentation> c = gameRepresentation -> {
        TableUploadResult tableUploadResult = uploadResult.get();
        Optional<GameRepresentation> match = this.games.stream().filter(g -> g.getId() == tableUploadResult.getGameId()).findFirst();
        if (match.isPresent()) {
          tableView.getSelectionModel().clearSelection();
          tableView.getSelectionModel().select(match.get());
          tableView.scrollTo(tableView.getSelectionModel().getSelectedItem());

          TableUploadDescriptor uploadMode = tableUploadResult.getUploadMode();
          if (uiSettings.isAutoEditTableData()) {
            if (!uploadMode.equals(TableUploadDescriptor.upload)) {
              TableDialogs.openTableDataDialog(this, match.get());
            }
          }
        }
      };
      reloadConsumers.add(c);
      onReload();
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
    boolean scanned = TableDialogs.openScanAllDialog(this.games);
    if (scanned) {
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
    if (game != null) {
      ValidationState validationState = game.getValidationState();
      DismissalUtil.dismissValidation(game, validationState);
    }
  }

  @FXML
  private void onDismissAll() {
    List<GameRepresentation> selectedItems = new ArrayList<>(tableView.getSelectionModel().getSelectedItems());
    if (selectedItems.size() == 1) {
      TableDialogs.openDismissAllDialog(selectedItems.get(0));
    }
    else if (selectedItems.size() > 1) {
      Optional<ButtonType> result = WidgetFactory.showConfirmation(stage, "Dismiss All", "Dismiss all validation errors of the selected tables?", "You can re-enable them anytime by validating them again.", "Dismiss Selection");
      if (result.isPresent() && result.get().equals(ButtonType.OK)) {
        ProgressDialog.createProgressDialog(new TableDismissAllProgressModel(selectedItems));
      }
    }
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

    //if the refreshed game is part of the current filter, refresh the whole view, not only the game
    if (filteredIds.contains(refreshedGame.getId())) {
      Platform.runLater(() -> {
        onRefresh(tableFilterController.getFilterSettings());
      });
      return;
    }

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


  public synchronized void onRefresh(FilterSettings filterSettings) {
    GameEmulatorRepresentation value = this.emulatorCombo.getValue();
    tableView.setVisible(false);

    setBusy(true);

    new Thread(() -> {
      try {
        List<Integer> integers = client.getGameService().filterGames(filterSettings);
        Platform.runLater(() -> {
          setFilterIds(integers);
          filterGames(games);
          tableView.setItems(data);
          tableView.refresh();

          GameEmulatorRepresentation emulator = getEmulatorSelection();
          if (filterSettings.isResetted(emulator == null || emulator.isVpxEmulator())) {
            labelTableCount.setText(games.size() + " tables");
          }
          else {
            labelTableCount.setText(data.size() + " of " + games.size() + " tables");
          }
          setBusy(false);
        });
      } catch (Exception e) {
        LOG.error("Error filtering tables: " + e.getMessage());
        WidgetFactory.showAlert(Studio.stage, "Error", "Error filtering tables: " + e.getMessage());
      }
    }).start();
  }

  @FXML
  private void onReloadPressed(ActionEvent e) {
    client.getPinUPPopperService().clearCache();
    client.getGameService().reload();
    this.onReload();
  }

  public void onReload() {
    UISettings uiSettings = client.getPreferenceService().getJsonPreference(PreferenceNames.UI_SETTINGS, UISettings.class);
    this.showVersionUpdates = !uiSettings.isHideVersions();
    this.showVpsUpdates = !uiSettings.isHideVPSUpdates();

    refreshPlaylists();
    refreshEmulators();

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

    setBusy(true);

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
        if (!games.isEmpty()) {
          this.validateBtn.setDisable(false);
          this.deleteBtn.setDisable(false);
          this.tableEditBtn.setDisable(false);

          GameRepresentation gameRepresentation = games.get(0);
          this.vpsBtn.setDisable(StringUtils.isEmpty(gameRepresentation.getExtTableVersionId()));
        }
        else {
          this.validationErrorLabel.setText("No tables found");
          this.validationErrorText.setText("Check the emulator setup in PinUP Popper. Make sure that all(!) directories are set and reload after fixing these.");
        }

        this.importBtn.setDisable(false);
        this.stopBtn.setDisable(false);
        this.textfieldSearch.setDisable(false);
        this.reloadBtn.setDisable(false);
        this.scanBtn.setDisable(false);
        this.scanAllBtn.setDisable(false);
        this.uploadTableBtn.setDisable(false);

        labelTableCount.setText(games.size() + " tables");

        setBusy(false);
        Platform.runLater(() -> {
          tableView.requestFocus();

          for (Consumer reloadConsumer : reloadConsumers) {
            reloadConsumer.accept(selection);
          }
          reloadConsumers.clear();
        });
      });
    }).start();
  }

  private void setBusy(boolean b) {
    if (b) {
      tableView.setVisible(false);
      if (!loaderStack.getChildren().contains(tablesLoadingOverlay)) {
        loaderStack.getChildren().add(tablesLoadingOverlay);
      }
    }
    else {
      tableView.setVisible(true);
      loaderStack.getChildren().remove(tablesLoadingOverlay);
    }
  }

  private void refreshPlaylists() {
    this.playlistCombo.setDisable(true);
    playlists = new ArrayList<>(client.getPlaylistsService().getPlaylists());
    List<Playlist> pl = new ArrayList<>(playlists);
    pl.add(0, null);
    playlistCombo.setItems(FXCollections.observableList(pl));
    this.playlistCombo.setDisable(false);
  }


  private void refreshEmulators() {
    this.emulatorCombo.setDisable(true);
    List<GameEmulatorRepresentation> emulators = new ArrayList<>(client.getPinUPPopperService().getGameEmulatorsUncached());
    emulators.add(0, null);
    this.emulatorCombo.setItems(FXCollections.observableList(emulators));
    this.emulatorCombo.setDisable(false);
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
        if (this.showVpsUpdates && uiSettings.isVpsBackglass() && value.getVpsUpdates().contains(VpsDiffTypes.b2s)) {
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
        if (!value.getVpsUpdates().isEmpty()) {
          FontIcon updateIcon = WidgetFactory.createUpdateIcon();

          Label label = new Label();
          label.setGraphic(updateIcon);

          StringBuilder builder = new StringBuilder();
          List<VPSChange> changes = value.getVpsUpdates().getChanges();
          for (VPSChange change : changes) {
            builder.append(change.toString(value.getExtTableId()));
            builder.append("\n");
          }

          String tooltip = "The table or its assets have received updates:\n\n" + builder + "\n\nYou can reset this indicator with the VPS button from the toolbar.";
          Tooltip tt = new Tooltip(tooltip);
          tt.setStyle("-fx-font-weight: bold;");
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
        if (this.showVpsUpdates && uiSettings.isVpsPOV() && value.getVpsUpdates().contains(VpsDiffTypes.pov)) {
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
        if (this.showVpsUpdates && uiSettings.isVpsAltSound() && value.getVpsUpdates().contains(VpsDiffTypes.altSound)) {
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
        if (this.showVpsUpdates && uiSettings.isVpsAltColor() && value.getVpsUpdates().contains(VpsDiffTypes.altColor)) {
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
        if (this.showVpsUpdates && uiSettings.isVpsPUPPack() && value.getVpsUpdates().contains(VpsDiffTypes.pupPack)) {
          HBox checkAndUpdateIcon = WidgetFactory.createCheckAndUpdateIcon("New PUP pack updates available");
          return new SimpleObjectProperty(checkAndUpdateIcon);
        }
        FontIcon checkboxIcon = WidgetFactory.createCheckboxIcon(getIconColor(value));
        return new SimpleObjectProperty(checkboxIcon);
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
      List<Playlist> matches = new ArrayList<>();
      boolean fav = false;
      for (Playlist playlist : playlists) {
        if (playlist.containsGame(value.getId())) {
          if (!fav && (playlist.isFavGame(value.getId()) || playlist.isGlobalFavGame(value.getId()))) {
            fav = true;
          }
          matches.add(playlist);
        }
      }

      if (fav) {
        FontIcon icon = WidgetFactory.createIcon("mdi2s-star");
        icon.setIconSize(24);
        icon.setIconColor(Paint.valueOf("#FF9933"));
        Label label = new Label();
        label.setTooltip(new Tooltip("The game is marked as favorite on at least one playlist."));
        label.setGraphic(icon);
        box.getChildren().add(label);
      }

      int maxLength = 3;
      if (fav) {
        maxLength = 2;
      }
      for (Playlist match : matches) {
        box.getChildren().add(WidgetFactory.createPlaylistIcon(match));
        if (box.getChildren().size() == maxLength && matches.size() > box.getChildren().size()) {
          Label label = new Label("+" + (matches.size() - box.getChildren().size()));
          label.setStyle("-fx-font-size: 14px;-fx-font-weight: bold; -fx-padding: 1 0 0 0;");
          box.getChildren().add(label);
          break;
        }
      }
      box.setStyle("-fx-padding: 3 0 0 0;");
      return new SimpleObjectProperty(box);
    });

    columnPlayfield.setCellValueFactory(cellData -> new SimpleObjectProperty(createAssetStatus(cellData.getValue(), PopperScreen.PlayField)));
    columnBackglass.setCellValueFactory(cellData -> new SimpleObjectProperty(createAssetStatus(cellData.getValue(), PopperScreen.BackGlass)));
    columnLoading.setCellValueFactory(cellData -> new SimpleObjectProperty(createAssetStatus(cellData.getValue(), PopperScreen.Loading)));
    columnWheel.setCellValueFactory(cellData -> new SimpleObjectProperty(createAssetStatus(cellData.getValue(), PopperScreen.Wheel)));
    columnDMD.setCellValueFactory(cellData -> new SimpleObjectProperty(createAssetStatus(cellData.getValue(), PopperScreen.DMD)));
    columnTopper.setCellValueFactory(cellData -> new SimpleObjectProperty(createAssetStatus(cellData.getValue(), PopperScreen.Topper)));
    columnFullDMD.setCellValueFactory(cellData -> new SimpleObjectProperty(createAssetStatus(cellData.getValue(), PopperScreen.Menu)));
    columnAudio.setCellValueFactory(cellData -> new SimpleObjectProperty(createAssetStatus(cellData.getValue(), PopperScreen.Audio)));
    columnAudioLaunch.setCellValueFactory(cellData -> new SimpleObjectProperty(createAssetStatus(cellData.getValue(), PopperScreen.AudioLaunch)));
    columnInfo.setCellValueFactory(cellData -> new SimpleObjectProperty(createAssetStatus(cellData.getValue(), PopperScreen.GameInfo)));
    columnHelp.setCellValueFactory(cellData -> new SimpleObjectProperty(createAssetStatus(cellData.getValue(), PopperScreen.GameHelp)));
    columnOther2.setCellValueFactory(cellData -> new SimpleObjectProperty(createAssetStatus(cellData.getValue(), PopperScreen.Other2)));


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
              if (o1.getVpsUpdates().isEmpty()) {
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
            Collections.sort(tableView.getItems(), Comparator.comparing(o -> String.valueOf(o.getPupPackName())));
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
        final ContextMenu menu = new ContextMenu();

        ListChangeListener<GameRepresentation> changeListener = (ListChangeListener.Change<? extends GameRepresentation> c) ->
          refreshContextMenu(tableView, menu, this.tableView.getSelectionModel().getSelectedItem());

        row.itemProperty().addListener((obs, oldItem, newItem) -> {
          if (newItem == null) {
            menu.getItems().clear();
          }
          else {
            refreshContextMenu(tableView, menu, newItem);
          }
        });

        row.emptyProperty().addListener((obs, wasEmpty, isNowEmpty) ->
          row.setContextMenu(isNowEmpty ? null : menu));

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

  private Node createAssetStatus(GameRepresentation value, PopperScreen popperScreen) {
    GameMediaItemRepresentation defaultMediaItem = value.getGameMedia().getDefaultMediaItem(popperScreen);
    if (defaultMediaItem != null) {
      String mimeType = defaultMediaItem.getMimeType();
      Button btn = new Button();
      btn.getStyleClass().add("table-media-button");
      StringBuilder tt = new StringBuilder();
      tt.append("Name:\t ");
      tt.append(defaultMediaItem.getName());
      tt.append("\n");
      if (mimeType.contains("audio")) {
        tt.append("Type:\t Audio\n");
        btn.setGraphic(WidgetFactory.createIcon("bi-music-note-beamed"));
      }
      else if (mimeType.contains("image")) {
        tt.append("Type:\t Picture\n");
        btn.setGraphic(WidgetFactory.createIcon("bi-card-image"));
      }
      else if (mimeType.contains("video")) {
        tt.append("Type:\t Video\n");
        btn.setGraphic(WidgetFactory.createIcon("bi-film"));
      }

      FontIcon fontIcon = (FontIcon) btn.getGraphic();
      fontIcon.setIconSize(20);
//      if (ignoredMedia.contains(popperScreen.name())) {
//        tt.append("Status:\t Ignored");
//        fontIcon.setIconColor(Paint.valueOf(WidgetFactory.DISABLED_COLOR));
//      }

      btn.setOnAction(event -> {
//        btn.getStyleClass().removeAll();
//        btn.getStyleClass().add("table-media-button-selected");
        showAssetDetails(value, popperScreen);
      });
      Tooltip tooltip = new Tooltip(tt.toString());
      tooltip.setWrapText(true);
      btn.setTooltip(tooltip);
      return btn;
    }
    return new Label("");
  }

  private void showAssetDetails(GameRepresentation game, PopperScreen popperScreen) {
    tableView.getSelectionModel().clearSelection();
    tableView.getSelectionModel().select(game);
    Platform.runLater(() -> {
      this.tablesController.getAssetViewSideBarController().setGame(tablesController.getTableOverviewController(), game, popperScreen);
    });
  }

  private void refreshContextMenu(TableView<GameRepresentation> tableView, ContextMenu ctxMenu, GameRepresentation game) {
    ctxMenu.getItems().clear();

    Image image3 = new Image(Studio.class.getResourceAsStream("popper-media.png"));
    ImageView view3 = new ImageView(image3);
    view3.setFitWidth(18);
    view3.setFitHeight(18);

    Image image4 = new Image(Studio.class.getResourceAsStream("popper-edit.png"));
    ImageView view4 = new ImageView(image4);
    view4.setFitWidth(18);
    view4.setFitHeight(18);


    MenuItem dataItem = new MenuItem("Edit Table Data");
    dataItem.setGraphic(view4);
    dataItem.setOnAction(actionEvent -> onTableEdit());
    dataItem.setDisable(tableView.getSelectionModel().isEmpty());
    ctxMenu.getItems().add(dataItem);

    MenuItem assetsItem = new MenuItem("Edit Table Assets");
    assetsItem.setGraphic(view3);
    assetsItem.setOnAction(actionEvent -> onMediaEdit());
    assetsItem.setDisable(tableView.getSelectionModel().isEmpty());
    ctxMenu.getItems().add(assetsItem);

    if (game.isVpxGame()) {
      ctxMenu.getItems().add(new SeparatorMenuItem());

      MenuItem scanItem = new MenuItem("Scan");
      scanItem.setGraphic(WidgetFactory.createIcon("mdi2m-map-search-outline"));
      scanItem.setOnAction(actionEvent -> onTablesScan());
      scanItem.setDisable(tableView.getSelectionModel().isEmpty());
      ctxMenu.getItems().add(scanItem);

      MenuItem scanAllItem = new MenuItem("Scan All");
      scanAllItem.setGraphic(WidgetFactory.createIcon("mdi2m-map-search"));
      scanAllItem.setOnAction(actionEvent -> onTablesScanAll());
      ctxMenu.getItems().add(scanAllItem);

      ctxMenu.getItems().add(new SeparatorMenuItem());

      MenuItem importsItem = new MenuItem("Import Tables");
      importsItem.setGraphic(WidgetFactory.createIcon("mdi2d-database-import-outline"));
      importsItem.setOnAction(actionEvent -> onImport());
      ctxMenu.getItems().add(importsItem);

      ctxMenu.getItems().add(new SeparatorMenuItem());

      MenuItem uploadAndImportTableItem = new MenuItem("Upload and Import Table");
      uploadAndImportTableItem.setGraphic(WidgetFactory.createIcon("mdi2u-upload"));
      uploadAndImportTableItem.setDisable(tableView.getSelectionModel().isEmpty());
      uploadAndImportTableItem.setOnAction(actionEvent -> openUploadDialogWithCheck(TableUploadDescriptor.uploadAndImport));
      ctxMenu.getItems().add(uploadAndImportTableItem);

      MenuItem uploadAndReplaceTableItem = new MenuItem("Upload and Replace Table");
      uploadAndReplaceTableItem.setGraphic(WidgetFactory.createIcon("mdi2u-upload"));
      uploadAndReplaceTableItem.setDisable(tableView.getSelectionModel().isEmpty());
      uploadAndReplaceTableItem.setOnAction(actionEvent -> openUploadDialogWithCheck(TableUploadDescriptor.uploadAndReplace));
      ctxMenu.getItems().add(uploadAndReplaceTableItem);

      MenuItem uploadAndCloneTableItem = new MenuItem("Upload and Clone Table");
      uploadAndCloneTableItem.setGraphic(WidgetFactory.createIcon("mdi2u-upload"));
      uploadAndCloneTableItem.setDisable(tableView.getSelectionModel().isEmpty() || game == null || game.getGameFileName().contains("\\"));
      uploadAndCloneTableItem.setOnAction(actionEvent -> openUploadDialogWithCheck(TableUploadDescriptor.uploadAndClone));
      ctxMenu.getItems().add(uploadAndCloneTableItem);

      Menu uploadMenu = new Menu("Upload...");

      MenuItem altColorFilesItem = new MenuItem("Upload ALT Color Files");
      altColorFilesItem.setGraphic(WidgetFactory.createIcon("mdi2u-upload"));
      altColorFilesItem.setDisable(tableView.getSelectionModel().isEmpty());
      altColorFilesItem.setOnAction(actionEvent -> onAltColorUpload());
      uploadMenu.getItems().add(altColorFilesItem);

      MenuItem altSoundItem = new MenuItem("Upload ALT Sound Pack");
      altSoundItem.setGraphic(WidgetFactory.createIcon("mdi2u-upload"));
      altSoundItem.setDisable(tableView.getSelectionModel().isEmpty());
      altSoundItem.setOnAction(actionEvent -> onAltSoundUpload());
      uploadMenu.getItems().add(altSoundItem);

      MenuItem uploadB2SItem = new MenuItem("Upload Backglass");
      uploadB2SItem.setGraphic(WidgetFactory.createIcon("mdi2u-upload"));
      uploadB2SItem.setDisable(tableView.getSelectionModel().isEmpty());
      uploadB2SItem.setOnAction(actionEvent -> onBackglassUpload());
      uploadMenu.getItems().add(uploadB2SItem);

      MenuItem dmdItem = new MenuItem("Upload DMD Pack");
      dmdItem.setGraphic(WidgetFactory.createIcon("mdi2u-upload"));
      dmdItem.setDisable(tableView.getSelectionModel().isEmpty());
      dmdItem.setOnAction(actionEvent -> onDMDUpload());
      uploadMenu.getItems().add(dmdItem);

      MenuItem iniItem = new MenuItem("Upload INI File");
      iniItem.setGraphic(WidgetFactory.createIcon("mdi2u-upload"));
      iniItem.setDisable(tableView.getSelectionModel().isEmpty());
      iniItem.setOnAction(actionEvent -> onIniUpload());
      uploadMenu.getItems().add(iniItem);

      MenuItem musicItem = new MenuItem("Upload Music Pack");
      musicItem.setGraphic(WidgetFactory.createIcon("mdi2u-upload"));
      musicItem.setDisable(tableView.getSelectionModel().isEmpty());
      musicItem.setOnAction(actionEvent -> onMusicUpload());
      uploadMenu.getItems().add(musicItem);

      MenuItem povItem = new MenuItem("Upload POV File");
      povItem.setGraphic(WidgetFactory.createIcon("mdi2u-upload"));
      povItem.setDisable(tableView.getSelectionModel().isEmpty());
      povItem.setOnAction(actionEvent -> onPOVUpload());
      uploadMenu.getItems().add(povItem);

      MenuItem pupPackItem = new MenuItem("Upload PUP Pack");
      pupPackItem.setGraphic(WidgetFactory.createIcon("mdi2u-upload"));
      pupPackItem.setDisable(tableView.getSelectionModel().isEmpty());
      pupPackItem.setOnAction(actionEvent -> onPupPackUpload());
      uploadMenu.getItems().add(pupPackItem);

      MenuItem romsItem = new MenuItem("Upload ROMs");
      romsItem.setGraphic(WidgetFactory.createIcon("mdi2u-upload"));
      romsItem.setDisable(tableView.getSelectionModel().isEmpty());
      romsItem.setOnAction(actionEvent -> onRomsUpload());
      uploadMenu.getItems().add(romsItem);

      ctxMenu.getItems().add(uploadMenu);
    }


    ctxMenu.getItems().add(new SeparatorMenuItem());

    MenuItem validateItem = new MenuItem("Validate");
    validateItem.setGraphic(WidgetFactory.createIcon("mdi2m-magnify"));
    validateItem.setDisable(tableView.getSelectionModel().isEmpty());
    validateItem.setOnAction(actionEvent -> onValidate());
    ctxMenu.getItems().add(validateItem);

    MenuItem validateAllItem = new MenuItem("Validate All");
    validateAllItem.setGraphic(WidgetFactory.createIcon("mdi2m-magnify"));
    validateAllItem.setDisable(tableView.getSelectionModel().isEmpty());
    validateAllItem.setOnAction(actionEvent -> onValidateAll());
    ctxMenu.getItems().add(validateAllItem);

    if (game.isVpxGame()) {
      ctxMenu.getItems().add(new SeparatorMenuItem());

      MenuItem launchItem = new MenuItem("Launch");
      launchItem.setGraphic(WidgetFactory.createGreenIcon("mdi2p-play"));
      launchItem.setDisable(tableView.getSelectionModel().isEmpty());
      launchItem.setOnAction(actionEvent -> onPlay());
      ctxMenu.getItems().add(launchItem);

      ctxMenu.getItems().add(new SeparatorMenuItem());

      MenuItem exportItem = new MenuItem("Export");
      exportItem.setGraphic(WidgetFactory.createIcon("mdi2e-export"));
      exportItem.setDisable(tableView.getSelectionModel().isEmpty());
      exportItem.setOnAction(actionEvent -> onBackup());
      ctxMenu.getItems().add(exportItem);

      ctxMenu.getItems().add(new SeparatorMenuItem());


      MenuItem removeItem = new MenuItem("Delete");
      removeItem.setOnAction(actionEvent -> onDelete());
      removeItem.setDisable(tableView.getSelectionModel().isEmpty());
      removeItem.setGraphic(WidgetFactory.createAlertIcon("mdi2d-delete-outline"));
      ctxMenu.getItems().add(removeItem);
    }
  }

  private void filterGames(List<GameRepresentation> games) {
    List<GameRepresentation> filtered = new ArrayList<>();
    String filterValue = textfieldSearch.textProperty().getValue();

    Playlist playlist = playlistCombo.getValue();
    GameEmulatorRepresentation emulator = emulatorCombo.getValue();

    //if all tables are filtered...
    if (filteredIds.isEmpty() && !tableFilterController.getFilterSettings().isResetted(emulator == null || emulator.isVpxEmulator())) {
      data = FXCollections.emptyObservableList();
      return;
    }

    for (GameRepresentation game : games) {
      if (!filteredIds.isEmpty() && !filteredIds.contains(game.getId())) {
        continue;
      }

      if (emulator != null && game.getEmulatorId() != emulator.getId()) {
        continue;
      }

      if (emulator == null && !game.isVpxGame()) {
        continue;
      }

      if (playlist != null && !playlist.containsGame(game.getId())) {
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
    dismissBtn.setVisible(true);

    validationError.setVisible(false);
    validationErrorLabel.setText("");
    validationErrorText.setText("");

    if (assetManagerMode) {
      this.tablesController.getAssetViewSideBarController().setGame(tablesController.getTableOverviewController(), g.orElse(null), null);
    }
    else {
      this.tablesController.getTablesSideBarController().setGame(g);
    }
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

    if (tableView.getSelectionModel().getSelectedItems().size() > 1) {
      Optional<GameRepresentation> first = tableView.getSelectionModel().getSelectedItems().stream().filter(game -> game.getValidationState() != null).findFirst();
      if (first.isPresent()) {
        dismissBtn.setVisible(false);

        validationError.setVisible(true);
        validationErrorLabel.setText("One or more of the selected tables have issues.");
        validationErrorText.setText("");
      }
    }
  }

  public void setVisible(boolean b) {
    if (!b) {
      tablesController.getAssetViewSideBarController().setVisible(b);
      tablesController.getTablesSideBarController().setVisible(b);
    }
    else {
      tablesController.getAssetViewSideBarController().setVisible(assetManagerMode);
      tablesController.getTablesSideBarController().setVisible(!assetManagerMode);
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

  public List<Playlist> getPlaylists() {
    return this.playlists;
  }

  public void updatePlaylist(Playlist update) {
    int pos = this.playlists.indexOf(update);
    this.playlists.remove(update);
    this.playlists.add(pos, update);

    List<Playlist> refreshedData = new ArrayList<>(this.playlists);
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
    iniUploadMenuItem.setDisable(disable);


    validateBtn.setDisable(c.getList().isEmpty());
    deleteBtn.setDisable(c.getList().isEmpty());
    backupBtn.setDisable(true);
    playBtn.setDisable(disable);
    scanBtn.setDisable(c.getList().isEmpty());
    assetManagerBtn.setDisable(disable);
    tableEditBtn.setDisable(disable);
    validationError.setVisible(c.getList().size() != 1);

    vpsBtn.setDisable(c.getList().size() != 1 || StringUtils.isEmpty(c.getList().get(0).getExtTableId()));
    vpsResetBtn.setDisable(c.getList().stream().filter(g -> g.getVpsUpdates() != null && !g.getVpsUpdates().isEmpty()).collect(Collectors.toList()).isEmpty());

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
      status = WidgetFactory.DISABLED_TEXT_STYLE;
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
    vpsSeparator.managedProperty().bindBidirectional(this.vpsSeparator.visibleProperty());
    deleteSeparator.managedProperty().bindBidirectional(this.deleteSeparator.visibleProperty());
    b2sManagerSeparator.managedProperty().bindBidirectional(this.b2sManagerSeparator.visibleProperty());
    stopSeparator.managedProperty().bindBidirectional(this.stopSeparator.visibleProperty());

    this.vpsBtn.managedProperty().bindBidirectional(this.vpsBtn.visibleProperty());
    this.vpsResetBtn.managedProperty().bindBidirectional(this.vpsResetBtn.visibleProperty());
    this.importBtn.managedProperty().bindBidirectional(this.importBtn.visibleProperty());
    this.uploadTableBtn.managedProperty().bindBidirectional(this.uploadTableBtn.visibleProperty());
    this.deleteBtn.managedProperty().bindBidirectional(this.deleteBtn.visibleProperty());
    this.scanBtn.managedProperty().bindBidirectional(this.scanBtn.visibleProperty());
    this.playBtn.managedProperty().bindBidirectional(this.playBtn.visibleProperty());
    this.stopBtn.managedProperty().bindBidirectional(this.stopBtn.visibleProperty());
    this.backupBtn.managedProperty().bindBidirectional(this.backupBtn.visibleProperty());
    this.backglassBtn.managedProperty().bindBidirectional(this.backglassBtn.visibleProperty());

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
      tablesLoadingOverlay.setTranslateY(-100);
      WaitOverlayController ctrl = loader.getController();
      ctrl.setLoadingMessage("Loading Tables...");
    } catch (IOException e) {
      LOG.error("Failed to load loading overlay: " + e.getMessage());
    }

    try {
      FXMLLoader loader = new FXMLLoader(TableFilterController.class.getResource("scene-tables-overview-filter.fxml"));
      loader.load();
      tableFilterController = loader.getController();
      tableFilterController.setTableController(this);
    } catch (IOException e) {
      LOG.error("Failed to load loading filter: " + e.getMessage(), e);
    }


    playlistCombo.setCellFactory(c -> new WidgetFactory.PlaylistBackgroundImageListCell());
    playlistCombo.setButtonCell(new WidgetFactory.PlaylistBackgroundImageListCell());
    playlistCombo.valueProperty().addListener(new ChangeListener<Playlist>() {
      @Override
      public void changed(ObservableValue<? extends Playlist> observableValue, Playlist Playlist, Playlist t1) {
        tableView.getSelectionModel().clearSelection();
        filterGames(games);
        tableView.setItems(data);

        if (!data.isEmpty()) {
          tableView.getSelectionModel().select(0);
        }
      }
    });

    emulatorCombo.valueProperty().addListener(new ChangeListener<GameEmulatorRepresentation>() {
      @Override
      public void changed(ObservableValue<? extends GameEmulatorRepresentation> observable, GameEmulatorRepresentation oldValue, GameEmulatorRepresentation newValue) {
        tableView.getSelectionModel().clearSelection();
        refreshViewForEmulator();
        tableFilterController.applyFilter();
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

    Image image5 = new Image(Studio.class.getResourceAsStream("b2s.png"));
    ImageView view5 = new ImageView(image5);
    view5.setFitWidth(18);
    view5.setFitHeight(18);
    backglassBtn.setGraphic(view5);

    preferencesChanged(PreferenceNames.UI_SETTINGS, null);
    preferencesChanged(PreferenceNames.SERVER_SETTINGS, null);

    client.getPreferenceService().addListener(this);
    Platform.runLater(() -> {
      Dialogs.openUpdateInfoDialog(client.getSystemService().getVersion(), false);
    });

    columnPlayfield.setVisible(false);
    columnBackglass.setVisible(false);
    columnLoading.setVisible(false);
    columnWheel.setVisible(false);
    columnDMD.setVisible(false);
    columnTopper.setVisible(false);
    columnFullDMD.setVisible(false);
    columnAudio.setVisible(false);
    columnAudioLaunch.setVisible(false);
    columnInfo.setVisible(false);
    columnHelp.setVisible(false);
    columnOther2.setVisible(false);

    assetManagerViewBtn.managedProperty().bindBidirectional(assetManagerViewBtn.visibleProperty());
    assetManagerViewBtn.setVisible(Features.ASSET_MODE);
  }

  private void refreshViewForEmulator() {
    GameEmulatorRepresentation newValue = emulatorCombo.getValue();
    tableFilterController.setEmulator(newValue);
    boolean vpxMode = newValue == null || newValue.isVpxEmulator();

    this.vpsBtn.setVisible(vpxMode);
    this.vpsResetBtn.setVisible(vpxMode);
    this.importBtn.setVisible(vpxMode);
    this.uploadTableBtn.setVisible(vpxMode);
    this.deleteBtn.setVisible(vpxMode);
    this.scanBtn.setVisible(vpxMode);
    this.playBtn.setVisible(vpxMode);
    this.stopBtn.setVisible(vpxMode);
    this.backupBtn.setVisible(vpxMode);
    this.backglassBtn.setVisible(vpxMode);

    vpsSeparator.setVisible(vpxMode);
    deleteSeparator.setVisible(vpxMode);
    b2sManagerSeparator.setVisible(vpxMode);
    stopSeparator.setVisible(vpxMode);

    columnVersion.setVisible(vpxMode && !assetManagerMode);
    columnEmulator.setVisible(vpxMode && !assetManagerMode);
    columnVPS.setVisible(vpxMode && !assetManagerMode);
    columnRom.setVisible(vpxMode && !assetManagerMode);
    columnB2S.setVisible(vpxMode && !assetManagerMode);
    columnPUPPack.setVisible(vpxMode && !assetManagerMode);
    columnAltSound.setVisible(vpxMode && !assetManagerMode);
    columnAltColor.setVisible(vpxMode && !assetManagerMode);
    columnPOV.setVisible(vpxMode && !assetManagerMode);
    columnHSType.setVisible(vpxMode && !assetManagerMode);

    tablesController.getTablesSideBarController().refreshViewForEmulator(newValue);
  }

  @Override
  public void preferencesChanged(String key, Object value) {
    if (key.equals(PreferenceNames.UI_SETTINGS)) {
      uiSettings = client.getPreferenceService().getJsonPreference(PreferenceNames.UI_SETTINGS, UISettings.class);
      columnVPS.setVisible(!assetManagerMode && !uiSettings.isHideVPSUpdates());
    }
    else if (key.equals(PreferenceNames.SERVER_SETTINGS)) {
      serverSettings = client.getPreferenceService().getJsonPreference(PreferenceNames.SERVER_SETTINGS, ServerSettings.class);
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

  public StackPane getTableStack() {
    return this.tableStack;
  }

  public TableView getTableView() {
    return this.tableView;
  }

  public ServerSettings getServerSettings() {
    return this.serverSettings;
  }

  public UISettings getUISettings() {
    return this.uiSettings;
  }

  public void setFilterIds(List<Integer> filteredIds) {
    this.filteredIds = filteredIds;
  }

  public Button getFilterButton() {
    return this.filterBtn;
  }

  public GameEmulatorRepresentation getEmulatorSelection() {
    return this.emulatorCombo.getSelectionModel().getSelectedItem();
  }
}