package de.mephisto.vpin.ui.tables;

import de.mephisto.vpin.commons.fx.ConfirmationResult;
import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.connectors.vps.VPS;
import de.mephisto.vpin.connectors.vps.model.VpsDiffTypes;
import de.mephisto.vpin.connectors.vps.model.VpsTable;
import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.altsound.AltSound;
import de.mephisto.vpin.restclient.frontend.Frontend;
import de.mephisto.vpin.restclient.frontend.FrontendType;
import de.mephisto.vpin.restclient.frontend.VPinScreen;
import de.mephisto.vpin.restclient.games.*;
import de.mephisto.vpin.restclient.games.descriptors.TableUploadType;
import de.mephisto.vpin.restclient.games.descriptors.UploadDescriptor;
import de.mephisto.vpin.restclient.preferences.PreferenceChangeListener;
import de.mephisto.vpin.restclient.preferences.ServerSettings;
import de.mephisto.vpin.restclient.preferences.UISettings;
import de.mephisto.vpin.restclient.validation.*;
import de.mephisto.vpin.ui.*;
import de.mephisto.vpin.ui.events.EventManager;
import de.mephisto.vpin.ui.tables.TableOverviewController.GameRepresentationModel;
import de.mephisto.vpin.ui.tables.editors.AltSound2EditorController;
import de.mephisto.vpin.ui.tables.editors.AltSoundEditorController;
import de.mephisto.vpin.ui.tables.editors.TableScriptEditorController;
import de.mephisto.vpin.ui.tables.panels.BaseLoadingModel;
import de.mephisto.vpin.ui.tables.panels.BaseLoadingTableCell;
import de.mephisto.vpin.ui.tables.validation.GameValidationTexts;
import de.mephisto.vpin.ui.tables.vps.VpsTableColumn;
import de.mephisto.vpin.ui.util.*;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
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
import org.apache.commons.lang3.StringUtils;
import org.kordamp.ikonli.javafx.FontIcon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static de.mephisto.vpin.commons.utils.WidgetFactory.DISABLED_COLOR;
import static de.mephisto.vpin.commons.utils.WidgetFactory.hexColor;
import static de.mephisto.vpin.ui.Studio.client;
import static de.mephisto.vpin.ui.Studio.stage;

public class TableOverviewController implements Initializable, StudioFXController, ListChangeListener<GameRepresentationModel>, PreferenceChangeListener {
  private final static Logger LOG = LoggerFactory.getLogger(TableOverviewController.class);

  @FXML
  private Separator deleteSeparator;

  @FXML
  TableColumn<GameRepresentationModel, GameRepresentationModel> columnDisplayName;

  @FXML
  TableColumn<GameRepresentationModel, GameRepresentationModel> columnVersion;

  @FXML
  TableColumn<GameRepresentationModel, GameRepresentationModel> columnEmulator;

  @FXML
  TableColumn<GameRepresentationModel, GameRepresentationModel> columnVPS;

  @FXML
  TableColumn<GameRepresentationModel, GameRepresentationModel> columnRom;

  @FXML
  TableColumn<GameRepresentationModel, GameRepresentationModel> columnB2S;

  @FXML
  TableColumn<GameRepresentationModel, GameRepresentationModel> columnStatus;

  @FXML
  TableColumn<GameRepresentationModel, GameRepresentationModel> columnPUPPack;

  @FXML
  TableColumn<GameRepresentationModel, GameRepresentationModel> columnAltSound;

  @FXML
  TableColumn<GameRepresentationModel, GameRepresentationModel> columnAltColor;

  @FXML
  TableColumn<GameRepresentationModel, GameRepresentationModel> columnPOV;

  @FXML
  TableColumn<GameRepresentationModel, GameRepresentationModel> columnINI;

  @FXML
  TableColumn<GameRepresentationModel, GameRepresentationModel> columnRES;

  @FXML
  TableColumn<GameRepresentationModel, GameRepresentationModel> columnHSType;

  @FXML
  TableColumn<GameRepresentationModel, GameRepresentationModel> columnPlaylists;

  @FXML
  TableColumn<GameRepresentationModel, GameRepresentationModel> columnDateAdded;


  @FXML
  private TableColumn<GameRepresentationModel, GameRepresentationModel> columnPlayfield;

  @FXML
  private TableColumn<GameRepresentationModel, GameRepresentationModel> columnBackglass;

  @FXML
  private TableColumn<GameRepresentationModel, GameRepresentationModel> columnLoading;

  @FXML
  private TableColumn<GameRepresentationModel, GameRepresentationModel> columnWheel;

  @FXML
  private TableColumn<GameRepresentationModel, GameRepresentationModel> columnDMD;

  @FXML
  private TableColumn<GameRepresentationModel, GameRepresentationModel> columnTopper;

  @FXML
  private TableColumn<GameRepresentationModel, GameRepresentationModel> columnFullDMD;

  @FXML
  private TableColumn<GameRepresentationModel, GameRepresentationModel> columnAudio;

  @FXML
  private TableColumn<GameRepresentationModel, GameRepresentationModel> columnAudioLaunch;

  @FXML
  private TableColumn<GameRepresentationModel, GameRepresentationModel> columnInfo;

  @FXML
  private TableColumn<GameRepresentationModel, GameRepresentationModel> columnHelp;

  @FXML
  private TableColumn<GameRepresentationModel, GameRepresentationModel> columnOther2;

  @FXML
  private TableView<GameRepresentationModel> tableView;

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
  private Separator importSeparator;

  @FXML
  private Button assetManagerViewBtn;

  @FXML
  private SplitMenuButton validateBtn;

  @FXML
  private Button assetManagerBtn;

  @FXML
  private Button deleteBtn;

  @FXML
  private SplitMenuButton scanBtn;

  @FXML
  private MenuItem scanAllBtn;

  @FXML
  private SplitMenuButton playBtn;

  @FXML
  private Button stopBtn;

  @FXML
  private Button importBtn;

  @FXML
  private SplitMenuButton uploadTableBtn;

  @FXML
  private Button reloadBtn;

  @FXML
  private ComboBox<PlaylistRepresentation> playlistCombo;

  @FXML
  private Separator playlistSplitter;

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
  private MenuItem mediaUploadItem;

  @FXML
  private MenuItem pupPackUploadItem;

  @FXML
  private MenuItem povItem;

  @FXML
  private MenuItem resItem;

  @FXML
  private Button filterBtn;

  @FXML
  private Hyperlink dismissBtn;

  @FXML
  private StackPane loaderStack;

  private Parent tablesLoadingOverlay;
  private WaitOverlayController tablesLoadingController;

  private TablesController tablesController;
  private List<PlaylistRepresentation> playlists;
  private boolean showVersionUpdates = true;
  private boolean showVpsUpdates = true;
  private final SimpleDateFormat dateAddedDateFormat = new SimpleDateFormat("yyyy-MM-dd");

  private long lastKeyInputTime = System.currentTimeMillis();
  private String lastKeyInput = "";
  private UISettings uiSettings;
  private ServerSettings serverSettings;
  private ValidationSettings validationSettings;

  private TableFilterController tableFilterController;

  private final List<Consumer<GameRepresentation>> reloadConsumers = new ArrayList<>();

  private boolean assetManagerMode = false;
  private TableOverviewContextMenu contextMenuController;
  private TableOverviewColumnSorter tableOverviewColumnSorter;
  private IgnoredValidationSettings ignoredValidations;

  private List<GameRepresentation> games;
  private ObservableList<GameRepresentationModel> models;
  private FilteredList<GameRepresentationModel> data;
  private TableOverviewPredicateFactory predicateFactory = new TableOverviewPredicateFactory();

  private GameEmulatorChangeListener gameEmulatorChangeListener;
  private GameStatus status;

  // Add a public no-args constructor
  public TableOverviewController() {
  }

  @FXML
  public void onBackglassManager() {
    TableDialogs.openDirectB2sManagerDialog(tablesController.getTablesSideBarController());
  }

  @FXML
  public void onAssetView() {
    tablesController.setSidebarVisible(true);

    tablesController.getTablesSideBarController().getTitledPaneMedia().setExpanded(false);

    assetManagerMode = !assetManagerMode;
    tablesController.getAssetViewSideBarController().setVisible(assetManagerMode);
    tablesController.getTablesSideBarController().setVisible(!assetManagerMode);

    Platform.runLater(() -> {
      if (assetManagerMode) {
        tablesController.getAssetViewSideBarController().setGame(this.tablesController.getTableOverviewController(), getSelection(), VPinScreen.Wheel);
        assetManagerViewBtn.getStyleClass().add("toggle-selected");
        if (!assetManagerViewBtn.getStyleClass().contains("toggle-button-selected")) {
          assetManagerViewBtn.getStyleClass().add("toggle-button-selected");
        }
      }
      else {
        assetManagerViewBtn.getStyleClass().remove("toggle-selected");
        assetManagerViewBtn.getStyleClass().remove("toggle-button-selected");
      }

      refreshViewAssetColumns(assetManagerMode);
      refreshColumns();

      GameRepresentation selectedItem = getSelection();
      tableView.getSelectionModel().clearSelection();
      if (selectedItem != null) {
        selectGameInModel(selectedItem);
      }
    });
  }

  private void refreshViewAssetColumns(boolean assetManagerMode) {
    List<VPinScreen> supportedScreens = client.getFrontendService().getFrontendCached().getSupportedScreens();
    columnPlayfield.setVisible(supportedScreens.contains(VPinScreen.PlayField) && assetManagerMode && !ignoredValidations.isIgnored(String.valueOf(VPinScreen.PlayField.getValidationCode())));
    columnBackglass.setVisible(supportedScreens.contains(VPinScreen.BackGlass) && assetManagerMode && !ignoredValidations.isIgnored(String.valueOf(VPinScreen.BackGlass.getValidationCode())));
    columnLoading.setVisible(supportedScreens.contains(VPinScreen.Loading) && assetManagerMode && !ignoredValidations.isIgnored(String.valueOf(VPinScreen.Loading.getValidationCode())));
    columnWheel.setVisible(supportedScreens.contains(VPinScreen.Wheel) && assetManagerMode && !ignoredValidations.isIgnored(String.valueOf(VPinScreen.Wheel.getValidationCode())));
    columnDMD.setVisible(supportedScreens.contains(VPinScreen.DMD) && assetManagerMode && !ignoredValidations.isIgnored(String.valueOf(VPinScreen.DMD.getValidationCode())));
    columnTopper.setVisible(supportedScreens.contains(VPinScreen.Topper) && assetManagerMode && !ignoredValidations.isIgnored(String.valueOf(VPinScreen.Topper.getValidationCode())));
    columnFullDMD.setVisible(supportedScreens.contains(VPinScreen.Menu) && assetManagerMode && !ignoredValidations.isIgnored(String.valueOf(VPinScreen.Menu.getValidationCode())));
    columnAudio.setVisible(supportedScreens.contains(VPinScreen.Audio) && assetManagerMode && !ignoredValidations.isIgnored(String.valueOf(VPinScreen.Audio.getValidationCode())));
    columnAudioLaunch.setVisible(supportedScreens.contains(VPinScreen.AudioLaunch) && assetManagerMode && !ignoredValidations.isIgnored(String.valueOf(VPinScreen.AudioLaunch.getValidationCode())));
    columnInfo.setVisible(supportedScreens.contains(VPinScreen.GameInfo) && assetManagerMode && !ignoredValidations.isIgnored(String.valueOf(VPinScreen.GameInfo.getValidationCode())));
    columnHelp.setVisible(supportedScreens.contains(VPinScreen.GameHelp) && assetManagerMode && !ignoredValidations.isIgnored(String.valueOf(VPinScreen.GameHelp.getValidationCode())));
    columnOther2.setVisible(supportedScreens.contains(VPinScreen.Other2) && assetManagerMode && !ignoredValidations.isIgnored(String.valueOf(VPinScreen.Other2.getValidationCode())));
  }

  @FXML
  public void onAltSoundUpload() {
    List<GameRepresentation> selectedItems = getSelections();
    int gameId = -1;
    if (selectedItems != null && !selectedItems.isEmpty()) {
      gameId = selectedItems.get(0).getId();
    }
    TableDialogs.openAltSoundUploadDialog(null, null, gameId);
  }

  @FXML
  public void onAltColorUpload() {
    List<GameRepresentation> selectedItems = getSelections();
    if (selectedItems != null && !selectedItems.isEmpty()) {
      boolean b = TableDialogs.openAltColorUploadDialog(selectedItems.get(0), null);
      if (b) {
        tablesController.getTablesSideBarController().getTitledPaneAltColor().setExpanded(true);
      }
    }
  }

  @FXML
  public void onRomsUpload() {
    TableDialogs.onRomUploads(null);
  }

  @FXML
  public void onCfgUpload() {
    TableDialogs.openCfgUploads(null);
  }

  @FXML
  public void onNvRamUpload() {
    TableDialogs.openNvRamUploads(null);
  }


  @FXML
  public void onMusicUpload() {
    TableDialogs.onMusicUploads();
  }


  @FXML
  public void onPupPackUpload() {
    List<GameRepresentation> selectedItems = getSelections();
    if (selectedItems != null && !selectedItems.isEmpty()) {
      boolean b = TableDialogs.openPupPackUploadDialog(selectedItems.get(0), null, null);
      if (b) {
        tablesController.getTablesSideBarController().getTitledPaneDirectB2s().setExpanded(true);
      }
    }
  }

  @FXML
  public void onBackglassUpload() {
    List<GameRepresentation> selectedItems = getSelections();
    if (selectedItems != null && !selectedItems.isEmpty()) {
      boolean b = TableDialogs.directBackglassUpload(stage, selectedItems.get(0));
      if (b) {
        tablesController.getTablesSideBarController().getTitledPaneDirectB2s().setExpanded(true);
      }
    }
  }

  @FXML
  public void onIniUpload() {
    List<GameRepresentation> selectedItems = getSelections();
    if (selectedItems != null && !selectedItems.isEmpty()) {
      boolean b = TableDialogs.directIniUpload(stage, selectedItems.get(0));
      if (b) {
        tablesController.getTablesSideBarController().getTitledPaneIni().setExpanded(true);
      }
    }
  }

  @FXML
  public void onMediaUpload() {
    List<GameRepresentation> selectedItems = getSelections();
    if (selectedItems != null && !selectedItems.isEmpty()) {
      TableDialogs.openMediaUploadDialog(selectedItems.get(0), null, null);
    }
  }

  @FXML
  public void onDMDUpload() {
    List<GameRepresentation> selectedItems = getSelections();
    if (selectedItems != null && !selectedItems.isEmpty()) {
      TableDialogs.openDMDUploadDialog(selectedItems.get(0), null, null);
    }
  }

  @FXML
  public void onPOVUpload() {
    List<GameRepresentation> selectedItems = getSelections();
    if (selectedItems != null && !selectedItems.isEmpty()) {
      boolean b = TableDialogs.directPovUpload(stage, selectedItems.get(0));
      if (b) {
        tablesController.getTablesSideBarController().getTitledPanePov().setExpanded(true);
      }
    }
  }

  @FXML
  public void onResUpload() {
    List<GameRepresentation> selectedItems = getSelections();
    if (selectedItems != null && !selectedItems.isEmpty()) {
      TableDialogs.directResUpload(stage, selectedItems.get(0));
    }
  }

  @FXML
  public void onMediaEdit() {
    GameRepresentation selectedItems = getSelection();
    if (selectedItems != null) {
      TableDialogs.openTableAssetsDialog(this, selectedItems, VPinScreen.BackGlass);
    }
  }

  @FXML
  public void onVps() {
    GameRepresentation selectedItems = getSelection();
    if (selectedItems != null && !StringUtils.isEmpty(selectedItems.getExtTableVersionId())) {
      Studio.browse(VPS.getVpsTableUrl(selectedItems.getExtTableId()));
    }
  }

  @FXML
  public void onVpsReset() {
    List<GameRepresentation> selectedItems = getSelections();
    TableActions.onVpsReset(selectedItems);
  }

  @FXML
  private void onFilter() {
    tableFilterController.toggle();
  }

  @FXML
  public void onTableEdit() {
    GameRepresentation selectedItems = getSelection();
    if (selectedItems != null) {
      if (Studio.client.getFrontendService().isFrontendRunning()) {
        if (Dialogs.openFrontendRunningWarning(Studio.stage)) {
          TableDialogs.openTableDataDialog(this, selectedItems);
        }
        return;
      }
      TableDialogs.openTableDataDialog(this, selectedItems);
    }
  }

  @FXML
  public void onBackup() {
    List<GameRepresentation> selectedItems = getSelections();
    TableDialogs.openTablesBackupDialog(selectedItems);
  }

  @FXML
  public void onPlay() {
    onPlay(null);
  }

  public void onPlay(String altExe) {
    GameRepresentation game = getSelection();
    if (game != null) {
      UISettings uiSettings = client.getPreferenceService().getJsonPreference(PreferenceNames.UI_SETTINGS, UISettings.class);
      if (uiSettings.isHideVPXStartInfo()) {
        client.getVpxService().playGame(game.getId(), altExe);
        return;
      }

      Frontend frontend = client.getFrontendService().getFrontendCached();

      ConfirmationResult confirmationResult = WidgetFactory.showConfirmationWithCheckbox(stage,
          "Start playing table \"" + game.getGameDisplayName() + "\"?", "Start Table",
          FrontendUtil.replaceNames("All existing [Emulator] and [Frontend]  processes will be terminated.", frontend, "VPX"),
          null, "Do not shown again", false);

      if (!confirmationResult.isApplyClicked()) {
        if (confirmationResult.isChecked()) {
          uiSettings.setHideVPXStartInfo(true);
          client.getPreferenceService().setJsonPreference(PreferenceNames.UI_SETTINGS, uiSettings);
        }
        client.getVpxService().playGame(game.getId(), altExe);
      }
    }
  }

  @FXML
  public void onStop() {
    Frontend frontend = client.getFrontendService().getFrontendCached();
    Optional<ButtonType> result = WidgetFactory.showConfirmation(Studio.stage,
        FrontendUtil.replaceNames("Stop all [Emulator] and [Frontend] processes?", frontend, "VPX"));
    if (result.isPresent() && result.get().equals(ButtonType.OK)) {
      client.getFrontendService().terminateFrontend();
    }
  }

  @FXML
  private void onSearchKeyPressed(KeyEvent e) {
    if (e.getCode().equals(KeyCode.ENTER)) {
      tableView.getSelectionModel().select(0);
      tableView.requestFocus();
    }
  }

  @FXML
  public void onTableUpload() {
    openUploadDialogWithCheck(TableUploadType.uploadAndImport);
  }

  public void openUploadDialogWithCheck(TableUploadType uploadDescriptor) {
    if (client.getFrontendService().isFrontendRunning()) {
      if (Dialogs.openFrontendRunningWarning(Studio.stage)) {
        openUploadDialog(uploadDescriptor);
      }
      return;
    }

    openUploadDialog(uploadDescriptor);
  }

  private void openUploadDialog(TableUploadType uploadDescriptor) {
    GameRepresentation game = getSelection();
    TableDialogs.openTableUploadDialog(this, game, uploadDescriptor, null);
  }

  public void refreshFilterId() {
    this.onRefresh(tableFilterController.getFilterSettings());
  }

  public void refreshUploadResult(Optional<UploadDescriptor> uploadResult) {
    //required for new table that may or may not be part of the filtered view
    refreshFilterId();

    if (uploadResult.isPresent() && uploadResult.get().getGameId() != -1) {
      Consumer<GameRepresentation> showTableDialogConsumer = gameRepresentation -> {
        UploadDescriptor tableUploadResult = uploadResult.get();
        Optional<GameRepresentation> match = this.games.stream().filter(g -> g.getId() == tableUploadResult.getGameId()).findFirst();
        if (match.isPresent()) {
          setSelection(match.get());
          if (assetManagerMode) {
            onAssetView();
          }

          if (uiSettings.isAutoEditTableData()) {
            Platform.runLater(() -> {
              TableDialogs.openTableDataDialog(this, match.get());
            });
          }
        }
      };
      reloadConsumers.add(showTableDialogConsumer);
      onReload();
    }
  }

  @FXML
  public void onDelete() {
    if (client.getFrontendService().isFrontendRunning()) {
      if (Dialogs.openFrontendRunningWarning(Studio.stage)) {
        deleteSelection();
      }
      return;
    }

    deleteSelection();
  }

  private void deleteSelection() {
    List<GameRepresentation> selectedGames = getSelections();
    if (selectedGames != null && !selectedGames.isEmpty()) {
      for (GameRepresentation game : selectedGames) {
        if (client.getCompetitionService().isGameReferencedByCompetitions(game.getId())) {
          WidgetFactory.showAlert(Studio.stage, "The table \"" + game.getGameDisplayName()
              + "\" is used by at least one competition.", "Delete all competitions for this table first.");
          return;
        }
      }

      tableView.getSelectionModel().clearSelection();
      TableDialogs.openTableDeleteDialog(selectedGames, this.games);
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
  public void onTablesScan() {
    List<GameRepresentation> selectedItems = getSelections();
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
  public void onTablesScanAll() {
    boolean scanned = TableDialogs.openScanAllDialog(this.games);
    if (scanned) {
      this.onReload();
    }
  }

  @FXML
  public void onImport() {
    GameEmulatorRepresentation emulatorSelection = getEmulatorSelection();
    if (emulatorSelection != null && emulatorSelection.getId() == -1) {
      WidgetFactory.showInformation(stage, "No emulator selected.", "Select a specific emulator to import tables from.");
      return;
    }

    if (client.getFrontendService().isFrontendRunning()) {
      if (Dialogs.openFrontendRunningWarning(Studio.stage)) {
        TableDialogs.openTableImportDialog(emulatorSelection);
      }
    }
    else {
      TableDialogs.openTableImportDialog(emulatorSelection);
    }
  }

  @FXML
  public void onValidate() {
    List<GameRepresentation> selectedItems = getSelections();
    TableDialogs.openValidationDialog(new ArrayList<>(selectedItems), false);
  }

  @FXML
  public void onValidateAll() {
    boolean done = TableDialogs.openValidationDialog(this.games, true);
    if (done) {
      onReload();
    }
  }

  @FXML
  private void onDismiss() {
    GameRepresentation game = getSelection();
    if (game != null) {
      ValidationState validationState = game.getValidationState();
      DismissalUtil.dismissValidation(game, validationState);
    }
  }

  @FXML
  private void onValidationSettings() {
    GameRepresentation game = getSelection();
    if (game != null) {
      ValidationState validationState = game.getValidationState();
      int code = validationState.getCode();
      if (code >= GameValidationCode.CODE_NO_AUDIO && code <= GameValidationCode.CODE_NO_WHEEL_IMAGE) {
        PreferencesController.open("validators_screens");
      }
      else {
        PreferencesController.open("validators_vpx");
      }

    }
  }

  @FXML
  private void onDismissAll() {
    List<GameRepresentation> selectedItems = getSelections();
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

  public void reload(GameRepresentation refreshedGame, boolean select) {
    if (refreshedGame != null) {
      if (select) {
        tableView.getSelectionModel().getSelectedItems().removeListener(this);
        tableView.getSelectionModel().clearSelection();
      }
      GameRepresentationModel model = null;
      int index = games.indexOf(refreshedGame);
      if (index != -1) {
        games.remove(index);
        games.add(index, refreshedGame);
        // also change the model that triggers a screen refresh
        model = new GameRepresentationModel(refreshedGame);
        models.remove(index);
        models.add(index, model);

      }

      // select the reloaded game
      if (select) {
        tableView.getSelectionModel().getSelectedItems().addListener(this);
        tableView.getSelectionModel().select(model);
      }

      // force refresh the view for elements not observed by the table
      tableView.refresh();
    }
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
      }
      catch (IOException e) {
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
        if (editorRootStack.getChildren().size() > 3) {
          return;
        }

        editorRootStack.getChildren().add(root);

        AltSoundEditorController editorController = loader.getController();
        editorController.setAltSound(game, altSound);
        editorController.setTablesController(tablesController);
      }
      catch (IOException e) {
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
      }
      catch (IOException e) {
        LOG.error("Failed to load alt sound2 editor: " + e.getMessage(), e);
      }
    }
  }


  /**
   * @return true if the filtered list did change and reload is required
   */
  public synchronized boolean onRefresh(FilterSettings filterSettings) {
    GameEmulatorRepresentation emulatorSelection = getEmulatorSelection();
    if (filterSettings.isResetted(emulatorSelection == null || emulatorSelection.isVpxEmulator())) {
      predicateFactory.setFilterIds(null);
      this.data.setPredicate(predicateFactory.buildPredicate());
    }
    else {
      setBusy("Filtering Tables...", true);
      new Thread(() -> {
        List<Integer> filteredIds = client.getGameService().filterGames(filterSettings);
        predicateFactory.setFilterIds(filteredIds);
        Platform.runLater(() -> {
          this.data.setPredicate(predicateFactory.buildPredicate());
          setBusy("", false);
        });
      }).start();
    }
    return true;
  }

  @FXML
  private void onReloadPressed(ActionEvent e) {
    client.getFrontendService().reload();
    client.getGameService().reload();
    this.onReload();
  }

  public void onReload() {
    status = client.getGameStatusService().getStatus();
    doReload(true);
  }

  public void onSwitchFromCache() {
    doReload(false);
  }

  public void doReload(boolean clearCache) {
    UISettings uiSettings = client.getPreferenceService().getJsonPreference(PreferenceNames.UI_SETTINGS, UISettings.class);
    this.showVersionUpdates = !uiSettings.isHideVersions();
    this.showVpsUpdates = !uiSettings.isHideVPSUpdates();

    refreshPlaylists();
    refreshEmulators(uiSettings);

    this.textfieldSearch.setDisable(true);
    this.reloadBtn.setDisable(true);
    this.scanBtn.setDisable(true);
    this.scanAllBtn.setDisable(true);
    this.playBtn.setDisable(true);
    this.validateBtn.setDisable(true);
    this.tableEditBtn.setDisable(true);
    this.deleteBtn.setDisable(true);
    this.uploadTableBtn.setDisable(true);
    this.importBtn.setDisable(true);
    this.stopBtn.setDisable(true);

    setBusy("Loading Tables...", true);
    new Thread(() -> {
      try {
        GameRepresentation selection = getSelection();
        GameRepresentationModel selectedItem = tableView.getSelectionModel().getSelectedItem();
        GameEmulatorRepresentation value = this.emulatorCombo.getValue();
        int id = -1;
        if (value != null) {
          id = value.getId();
        }

        if (clearCache) {
          client.getGameService().clearCache(id);
        }
        List<GameRepresentation> _games = client.getGameService().getGamesCached(id);

        // as the load of tables could take some time, users may have switched to another emulators in between
        // if this is the case, do not refresh the UI with the results
        GameEmulatorRepresentation valueAfterSearch = this.emulatorCombo.getValue();
        if (valueAfterSearch != null && valueAfterSearch.getId() != id) {
          return;
        }

        Platform.runLater(() -> {

          setItems(_games);

          if (selection != null) {
            final Optional<GameRepresentation> updatedGame = this.games.stream().filter(g -> g.getId() == selection.getId()).findFirst();
            if (updatedGame.isPresent()) {
              GameRepresentation gameRepresentation = updatedGame.get();
              //tableView.getSelectionModel().select(gameRepresentation);
              this.playBtn.setDisable(gameRepresentation.getGameFilePath() == null);
            }
          }

          if (!games.isEmpty()) {
            this.validateBtn.setDisable(false);
            this.deleteBtn.setDisable(false);
            this.tableEditBtn.setDisable(false);
          }
          else {
            Frontend frontend = client.getFrontendService().getFrontendCached();
            this.validationErrorLabel.setText("No tables found");
            this.validationErrorText.setText(FrontendUtil.replaceName("Check the emulator setup in [Frontend]"
                + ". Make sure that all(!) directories are set and reload after fixing these.", frontend));
          }

          this.importBtn.setDisable(false);
          this.stopBtn.setDisable(false);
          this.textfieldSearch.setDisable(false);
          this.reloadBtn.setDisable(false);
          this.scanBtn.setDisable(false);
          this.scanAllBtn.setDisable(false);
          this.uploadTableBtn.setDisable(false);

          tableView.requestFocus();

          if (selectedItem == null) {
            tableView.getSelectionModel().select(0);
          }
          else {
            tableView.getSelectionModel().select(selectedItem);
          }

          for (Consumer<GameRepresentation> reloadConsumer : reloadConsumers) {
            reloadConsumer.accept(selection);
          }
          reloadConsumers.clear();

          setBusy("", false);
        });


      }
      catch (Exception e) {
        LOG.error("Failed to load tables: " + e.getMessage(), e);
        Platform.runLater(() -> {
          WidgetFactory.showAlert(stage, "Error", "Loading tables failed: " + e.getMessage());
        });
      }

    }).start();
  }

  private void setItems(List<GameRepresentation> _games) {

    this.games = _games;
    this.models = FXCollections.observableArrayList();
    for (GameRepresentation g : _games) {
      models.add(new GameRepresentationModel(g));
    }

    // Wrap games in a FilteredList
    this.data = new FilteredList<>(models);
    // When predicate change, update data count
    this.data.predicateProperty().addListener((o, oldP, newP) -> {
      labelTableCount.setText(data.size() + " games");
    });

    // Wrap the FilteredList in a SortedList
    SortedList<GameRepresentationModel> sortedData = new SortedList<>(this.data);
    // Bind the SortedList comparator to the TableView comparator.
    sortedData.comparatorProperty().bind(Bindings.createObjectBinding(
        () -> tableOverviewColumnSorter.buildComparator(tableView),
        tableView.comparatorProperty()));
    // Set a dummy SortPolicy to tell the TableView data is successfully sorted
    tableView.setSortPolicy(tableView -> true);

    // Set the items in the TableView
    tableView.setItems(sortedData);

    // filter the list and refresh number of items
    this.data.setPredicate(predicateFactory.buildPredicate());
  }


  private void setBusy(String msg, boolean b) {
    tablesLoadingController.setLoadingMessage(msg);
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
    List<PlaylistRepresentation> pl = new ArrayList<>(playlists);
    pl.add(0, null);
    playlistCombo.setItems(FXCollections.observableList(pl));
    this.playlistCombo.setDisable(false);
  }


  private void refreshEmulators(UISettings uiSettings) {
    this.emulatorCombo.valueProperty().removeListener(gameEmulatorChangeListener);
    GameEmulatorRepresentation selectedEmu = this.emulatorCombo.getSelectionModel().getSelectedItem();

    this.emulatorCombo.setDisable(true);
    List<GameEmulatorRepresentation> emulators = new ArrayList<>(client.getFrontendService().getGameEmulatorsUncached());
    List<GameEmulatorRepresentation> filtered = emulators.stream().filter(e -> !uiSettings.getIgnoredEmulatorIds().contains(Integer.valueOf(e.getId()))).collect(Collectors.toList());

    GameEmulatorRepresentation allVpx = new GameEmulatorRepresentation();
    allVpx.setId(-1);
    allVpx.setName("All VPX Tables");
    allVpx.setVpxEmulator(true);
    filtered.add(0, allVpx);

    this.emulatorCombo.setItems(FXCollections.observableList(filtered));
    this.emulatorCombo.setDisable(false);

    if (selectedEmu == null) {
      this.emulatorCombo.getSelectionModel().selectFirst();
    }

    this.emulatorCombo.valueProperty().addListener(gameEmulatorChangeListener);
  }

  private void bindSearchField() {
    textfieldSearch.textProperty().addListener((observableValue, s, filterValue) -> {
      tableView.getSelectionModel().clearSelection();
      refreshView(Optional.empty());

      // reset the Predicate to trigger the table refiltering
      predicateFactory.setFilterTerm(filterValue);
      data.setPredicate(predicateFactory.buildPredicate());
    });
  }

  private void bindTable() {
    tableView.setPlaceholder(new Label("No matching tables found."));

    tableView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

    // set ValueCellFactory and CellFactory, and get a renderer that is responsible to render the cell
    configureColumn(columnDisplayName, (value, model) -> {
      Label label = new Label(value.getGameDisplayName());
      label.getStyleClass().add("default-text");
      label.setStyle(getLabelCss(value));

      String tooltip = value.getGameFilePath();
      if (status != null) {
        if (status.getLastActiveId() == value.getId() || status.getGameId() == value.getId()) {
          label.setStyle("-fx-text-fill: " + WidgetFactory.OK_COLOR + ";-fx-font-weight: bold;");
          tooltip += "\nThis is the last played game.";
        }
      }
      if (value.getGameFilePath() != null) {
        label.setTooltip(new Tooltip(tooltip));
      }
      return label;
    }, true);

    configureLoadingColumn(columnEmulator, "", (value, model) -> {
      GameEmulatorRepresentation gameEmulator = model.getGameEmulator();
      Label label = new Label(gameEmulator.getName());
      label.getStyleClass().add("default-text");
      label.setStyle(getLabelCss(value));
      return label;
    });

    configureColumn(columnVersion, (value, model) -> {
      Label label = new Label(value.getVersion());
      label.getStyleClass().add("default-text");
      label.setStyle(getLabelCss(value));
      if (showVersionUpdates && value.isUpdateAvailable()) {
        Frontend frontend = client.getFrontendService().getFrontendCached();

        FontIcon updateIcon = WidgetFactory.createUpdateIcon();
        Tooltip tt = new Tooltip("The table version in [Frontend] is \"" + value.getVersion()
            + "\", while the linked VPS table has version \"" + value.getExtVersion() + "\".\n\n"
            + "Update the table, correct the selected VPS table or fix the version in the \"[Frontend] Table Settings\" section.");
        FrontendUtil.replaceName(tt, frontend);
        tt.setWrapText(true);
        tt.setMaxWidth(400);
        label.setTooltip(tt);
        label.setGraphic(updateIcon);
      }
      return label;
    }, true);

    configureColumn(columnRom, (value, model) -> {
      String rom = value.getRom();
      List<Integer> ignoredValidations = Collections.emptyList();
      if (value.getIgnoredValidations() != null) {
        ignoredValidations = value.getIgnoredValidations();
      }
      Label label = new Label(rom);
      if (!value.isRomExists() && value.isRomRequired() && !ignoredValidations.contains(GameValidationCode.CODE_ROM_NOT_EXISTS)) {
        String color = WidgetFactory.ERROR_COLOR;
        label.setStyle("-fx-font-color: " + color + ";-fx-text-fill: " + color + ";-fx-font-weight: bold;");
      }
      else {
        label.getStyleClass().add("default-text");
        label.setStyle(getLabelCss(value));
      }
      return label;
    }, true);

    configureColumn(columnHSType, (value, model) -> {
      String hsType = value.getHighscoreType();
      if (!StringUtils.isEmpty(hsType) && hsType.equals("EM")) {
        hsType = "Text";
      }
      Label label = new Label(hsType);
      label.getStyleClass().add("default-text");
      label.setStyle(getLabelCss(value));
      return label;
    }, true);

    configureColumn(columnB2S, (value, model) -> {
      if (value.getDirectB2SPath() != null) {
        if (this.showVpsUpdates && uiSettings.isVpsBackglass() && value.getVpsUpdates().contains(VpsDiffTypes.b2s)) {
          return WidgetFactory.createCheckAndUpdateIcon("New backglass updates available");
        }
        else {
          return WidgetFactory.createCheckboxIcon(getIconColor(value), value.getDirectB2SPath());
        }
      }
      return null;
    }, true);

    configureLoadingColumn(columnVPS, "Loading...", (value, model) -> {
      UISettings uiSettings = client.getPreferenceService().getJsonPreference(PreferenceNames.UI_SETTINGS, UISettings.class);
      return new VpsTableColumn(model.getGame().getExtTableId(), model.game.getExtTableVersionId(), model.game.getVpsUpdates(), uiSettings);
    });

    configureColumn(columnPOV, (value, model) -> {
      if (value.getPovPath() != null) {
        if (this.showVpsUpdates && uiSettings.isVpsPOV() && value.getVpsUpdates().contains(VpsDiffTypes.pov)) {
          return WidgetFactory.createCheckAndUpdateIcon("New POV updates available");
        }
        else {
          return WidgetFactory.createCheckboxIcon(getIconColor(value), value.getPovPath());
        }
      }
      return null;
    }, true);

    configureColumn(columnINI, (value, model) -> {
      if (value.getIniPath() != null) {
        return WidgetFactory.createCheckboxIcon(getIconColor(value), value.getIniPath());
      }
      return null;
    }, true);

    configureColumn(columnRES, (value, model) -> {
      if (value.getResPath() != null) {
        return WidgetFactory.createCheckboxIcon(getIconColor(value), value.getResPath());
      }
      return null;
    }, true);

    configureColumn(columnAltSound, (value, model) -> {
      if (value.isAltSoundAvailable()) {
        if (this.showVpsUpdates && uiSettings.isVpsAltSound() && value.getVpsUpdates().contains(VpsDiffTypes.altSound)) {
          return WidgetFactory.createCheckAndUpdateIcon("New ALT sound updates available");
        }
        else {
          return WidgetFactory.createCheckboxIcon(getIconColor(value));
        }
      }
      return null;
    }, true);

    configureColumn(columnAltColor, (value, model) -> {
      if (value.getAltColorType() != null) {
        if (this.showVpsUpdates && uiSettings.isVpsAltColor() && value.getVpsUpdates().contains(VpsDiffTypes.altColor)) {
          return WidgetFactory.createCheckAndUpdateIcon("New ALT color updates available");
        }
        else {
          return WidgetFactory.createCheckboxIcon(getIconColor(value));
        }
      }
      return null;
    }, true);

    configureColumn(columnPUPPack, (value, model) -> {
      if (value.getPupPackPath() != null) {
        if (this.showVpsUpdates && uiSettings.isVpsPUPPack() && value.getVpsUpdates().contains(VpsDiffTypes.pupPack)) {
          return WidgetFactory.createCheckAndUpdateIcon("New PUP pack updates available");
        }
        else {
          return WidgetFactory.createCheckboxIcon(getIconColor(value), value.getPupPackPath());
        }
      }
      return null;
    }, true);

    configureColumn(columnStatus, (value, model) -> {
      ValidationState validationState = value.getValidationState();
      FontIcon statusIcon = WidgetFactory.createCheckIcon(getIconColor(value));
      if (value.getIgnoredValidations() != null && !value.getIgnoredValidations().contains(-1)) {
        if (validationState != null && validationState.getCode() > 0) {
          statusIcon = WidgetFactory.createExclamationIcon(getIconColor(value));
        }
      }

      Button btn = new Button();
      btn.getStyleClass().add("table-media-button");
      HBox graphics = new HBox(3);
      graphics.setAlignment(Pos.CENTER_RIGHT);
      graphics.setMinWidth(34);
      graphics.getChildren().add(statusIcon);

      if (!StringUtils.isEmpty(value.getNotes())) {
        String notes = value.getNotes();
        Tooltip tooltip = new Tooltip(value.getNotes());
        tooltip.setWrapText(true);
        btn.setTooltip(tooltip);

        FontIcon icon = WidgetFactory.createIcon("mdi2c-comment");
        icon.setIconSize(16);
        graphics.getChildren().add(0, icon);

        if (notes.contains("//ERROR")) {
          icon.setIconColor(Paint.valueOf(WidgetFactory.ERROR_COLOR));
        }
        else if (notes.contains("//TODO")) {
          icon.setIconColor(Paint.valueOf(WidgetFactory.TODO_COLOR));
        }
        else if (notes.contains("//OUTDATED")) {
          icon.setIconColor(Paint.valueOf(WidgetFactory.OUTDATED_COLOR));
        }
      }

      btn.setGraphic(graphics);
      btn.setOnAction(event -> {
        tableView.getSelectionModel().clearSelection();
        tableView.getSelectionModel().select(model);
        Platform.runLater(() -> {
          TableDialogs.openNotesDialog(value);
        });
      });

      return btn;
    }, true);

    configureColumn(columnDateAdded, (value, model) -> {
      Label label = null;
      if (value.getDateAdded() != null) {
        label = new Label(dateAddedDateFormat.format(value.getDateAdded()));
      }
      else {
        label = new Label("-");
      }
      label.getStyleClass().add("default-text");
      return label;
    }, true);

    columnPlaylists.setSortable(false);
    configureColumn(columnPlaylists, (value, model) -> {
      HBox box = new HBox();
      List<PlaylistRepresentation> matches = new ArrayList<>();
      boolean fav = false;
      Integer favColor = null;

      boolean globalFav = false;
      Integer globalFavColor = null;

      for (PlaylistRepresentation playlist : playlists) {
        if (playlist.containsGame(value.getId())) {
          if (!fav && playlist.isFavGame(value.getId())) {
            favColor = playlist.getMenuColor();
            fav = true;
          }
          if (!globalFav && playlist.isGlobalFavGame(value.getId())) {
            globalFavColor = playlist.getMenuColor();
            globalFav = true;
          }
          matches.add(playlist);
        }
      }

      int ICON_WIDTH = 26;
      double width = 0;
      if (fav) {
        Label label = WidgetFactory.createLocalFavoritePlaylistIcon();
        if (favColor != null) {
          ((FontIcon) label.getGraphic()).setIconColor(Paint.valueOf(hexColor(favColor)));
        }
        box.getChildren().add(label);
        width += ICON_WIDTH;
      }

      if (globalFav) {
        Label label = WidgetFactory.createGlobalFavoritePlaylistIcon();
        if (globalFavColor != null) {
          ((FontIcon) label.getGraphic()).setIconColor(Paint.valueOf(hexColor(globalFavColor)));
        }
        box.getChildren().add(label);
        width += ICON_WIDTH;
      }

      int count = 0;
      for (PlaylistRepresentation match : matches) {
        if (width < (columnPlaylists.widthProperty().get() - ICON_WIDTH)) {
          box.getChildren().add(WidgetFactory.createPlaylistIcon(match));
          width += ICON_WIDTH;
          count++;
          continue;
        }

        Label label = new Label("+" + (matches.size() - count));
        label.setStyle("-fx-font-size: 14px;-fx-font-weight: bold; -fx-padding: 1 0 0 0;");
        label.getStyleClass().add("default-text");
        box.getChildren().add(label);
        break;
      }
      box.setStyle("-fx-padding: 3 0 0 0;");
      return box;
    }, true);

    List<VPinScreen> supportedScreens = client.getFrontendService().getFrontendCached().getSupportedScreens();
    configureColumn(columnPlayfield, (value, model) -> createAssetStatus(value, VPinScreen.PlayField), supportedScreens.contains(VPinScreen.PlayField));
    configureColumn(columnBackglass, (value, model) -> createAssetStatus(value, VPinScreen.BackGlass), supportedScreens.contains(VPinScreen.BackGlass));
    configureColumn(columnLoading, (value, model) -> createAssetStatus(value, VPinScreen.Loading), supportedScreens.contains(VPinScreen.Loading));
    configureColumn(columnWheel, (value, model) -> createAssetStatus(value, VPinScreen.Wheel), supportedScreens.contains(VPinScreen.Wheel));
    configureColumn(columnDMD, (value, model) -> createAssetStatus(value, VPinScreen.DMD), supportedScreens.contains(VPinScreen.DMD));
    configureColumn(columnTopper, (value, model) -> createAssetStatus(value, VPinScreen.Topper), supportedScreens.contains(VPinScreen.Topper));
    configureColumn(columnFullDMD, (value, model) -> createAssetStatus(value, VPinScreen.Menu), supportedScreens.contains(VPinScreen.Menu));
    configureColumn(columnAudio, (value, model) -> createAssetStatus(value, VPinScreen.Audio), supportedScreens.contains(VPinScreen.Audio));
    configureColumn(columnAudioLaunch, (value, model) -> createAssetStatus(value, VPinScreen.AudioLaunch), supportedScreens.contains(VPinScreen.AudioLaunch));
    configureColumn(columnInfo, (value, model) -> createAssetStatus(value, VPinScreen.GameInfo), supportedScreens.contains(VPinScreen.GameInfo));
    configureColumn(columnHelp, (value, model) -> createAssetStatus(value, VPinScreen.GameHelp), supportedScreens.contains(VPinScreen.GameHelp));
    configureColumn(columnOther2, (value, model) -> createAssetStatus(value, VPinScreen.Other2), supportedScreens.contains(VPinScreen.Other2));

    setItems(new ArrayList<>());

    tableView.setEditable(true);
    tableView.getSelectionModel().getSelectedItems().addListener(this);
    //tableView.setSortPolicy(tableView -> tableOverviewColumnSorter.sort(tableView));

    tableView.setRowFactory(
        tableView -> {
          final TableRow<GameRepresentationModel> row = new TableRow<>();
          final ContextMenu menu = new ContextMenu();

          //ListChangeListener<GameRepresentation> changeListener = (ListChangeListener.Change<? extends GameRepresentation> c) ->
          //    contextMenuController.refreshContextMenu(tableView, menu, this.getSelection());

          row.itemProperty().addListener((obs, oldItem, newItem) -> {
            if (newItem == null) {
              menu.getItems().clear();
            }
            else {
              contextMenuController.refreshContextMenu(tableView, menu, newItem.getGame());
            }
          });

          row.emptyProperty().addListener((obs, wasEmpty, isNowEmpty) ->
              row.setContextMenu(isNowEmpty ? null : menu));

          return row;
        });


    tableView.setOnKeyPressed(event -> {
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

      for (GameRepresentationModel model : data) {
        GameRepresentation game = model.getGame();
        if (game.getGameDisplayName().toLowerCase().startsWith(text.toLowerCase())) {
          setSelection(game);
          break;
        }
      }
    });
  }

  //------------------------------
  @FunctionalInterface
  private interface ColumnRenderer {
    Node render(GameRepresentation game, GameRepresentationModel model);
  }

  private void configureColumn(TableColumn<GameRepresentationModel, GameRepresentationModel> column, ColumnRenderer renderer, boolean visible) {
    column.setVisible(visible);
    column.setCellValueFactory(cellData -> {
      GameRepresentationModel model = cellData.getValue();
      return model;
    });
    column.setCellFactory(cellData -> {
      TableCell<GameRepresentationModel, GameRepresentationModel> cell = new TableCell<>();
      cell.itemProperty().addListener((obs, old, model) -> {
        if (model != null) {
          Node node = renderer.render(model.getGame(), model);
          cell.graphicProperty().bind(Bindings.when(cell.emptyProperty()).then((Node) null).otherwise(node));
        }
      });
      return cell;
    });
  }

  private void configureLoadingColumn(TableColumn<GameRepresentationModel, GameRepresentationModel> column,
                                      String loading, ColumnRenderer renderer) {

    //if (true) { configureColumn(column, renderer); return; }

    column.setCellValueFactory(cellData -> cellData.getValue());
    column.setCellFactory(cellData -> new BaseLoadingTableCell<GameRepresentationModel>() {

      @Override
      protected String getLoading(GameRepresentationModel model) {
        return loading;
      }

      @Override
      protected void renderItem(GameRepresentationModel model) {
        Node node = renderer.render(model.getGame(), model);
        setGraphic(node);
      }
    });
  }

  private Node createAssetStatus(GameRepresentation value, VPinScreen VPinScreen) {
    FrontendMediaItemRepresentation defaultMediaItem = value.getGameMedia().getDefaultMediaItem(VPinScreen);
    ValidationProfile defaultProfile = validationSettings.getDefaultProfile();
    ValidationConfig config = defaultProfile.getOrCreateConfig(VPinScreen.getValidationCode());
    boolean ignored = value.getIgnoredValidations().contains(VPinScreen.getValidationCode());

    StringBuilder tt = new StringBuilder();
    Button btn = new Button();
    btn.getStyleClass().add("table-media-button");

    if (defaultMediaItem != null) {
      String mimeType = defaultMediaItem.getMimeType();
      tt.append("Name:\t ");
      tt.append(defaultMediaItem.getName());
      tt.append("\n");
      if (mimeType.contains("audio")) {
        tt.append("Type:\t Audio\n");
        FontIcon icon = WidgetFactory.createIcon("bi-music-note-beamed");
        if (!ignored && !config.getMedia().equals(ValidatorMedia.audio)) {
          icon.setIconColor(Paint.valueOf(WidgetFactory.ERROR_COLOR));
          tt.append("This asset should have the type \"" + config.getMedia().getDisplayName() + "\".\n");
        }

        btn.setGraphic(icon);
      }
      else if (mimeType.contains("image")) {
        tt.append("Type:\t Picture\n");
        FontIcon icon = WidgetFactory.createIcon("bi-card-image");
        if (!ignored && !config.getMedia().equals(ValidatorMedia.image) && !config.getMedia().equals(ValidatorMedia.imageOrVideo)) {
          icon.setIconColor(Paint.valueOf(WidgetFactory.ERROR_COLOR));
          tt.append("This asset should have the type \"" + config.getMedia().getDisplayName() + "\".\n");
        }
        btn.setGraphic(icon);
      }
      else if (mimeType.contains("video")) {
        tt.append("Type:\t Video\n");
        FontIcon icon = WidgetFactory.createIcon("bi-film");
        if (!ignored && !config.getMedia().equals(ValidatorMedia.video) && !config.getMedia().equals(ValidatorMedia.imageOrVideo)) {
          icon.setIconColor(Paint.valueOf(WidgetFactory.ERROR_COLOR));
          tt.append("This asset should have the type \"" + config.getMedia().getDisplayName() + "\".\n");
        }
        btn.setGraphic(icon);
      }

      FontIcon fontIcon = (FontIcon) btn.getGraphic();
      if (!ignored && config.getOption().equals(ValidatorOption.empty)) {
        fontIcon.setIconColor(Paint.valueOf(WidgetFactory.ERROR_COLOR));
        tt.append("This asset should remain empty.\n");
      }

    }
    else {
      if (config.getMedia().equals(ValidatorMedia.image)) {
        btn.setGraphic(WidgetFactory.createIcon("bi-card-image"));
      }
      else if (config.getMedia().equals(ValidatorMedia.audio)) {
        btn.setGraphic(WidgetFactory.createIcon("bi-music-note-beamed"));
      }
      else if (config.getMedia().equals(ValidatorMedia.video)) {
        btn.setGraphic(WidgetFactory.createIcon("bi-film"));
      }
      else if (config.getMedia().equals(ValidatorMedia.imageOrVideo)) {
        btn.setGraphic(WidgetFactory.createIcon("bi-images"));
      }

      FontIcon fontIcon = (FontIcon) btn.getGraphic();
      tt.append("Preferred Asset Type: " + config.getMedia().getDisplayName() + "\n");
      tt.append("No asset selected.\n");

      if (!ignored) {
        if (config.getOption().equals(ValidatorOption.empty)) {
          fontIcon.setIconColor(Paint.valueOf(DISABLED_COLOR));
          tt.append("This asset should remain empty.\n");
        }
        else if (config.getOption().equals(ValidatorOption.optional)) {
          fontIcon.setIconColor(Paint.valueOf(DISABLED_COLOR));
          tt.append("This asset is optional.\n");
        }
        else if (config.getOption().equals(ValidatorOption.mandatory)) {
          fontIcon.setIconColor(Paint.valueOf(WidgetFactory.ERROR_COLOR));
          tt.append("This asset is mandatory.\n");
        }
      }
    }

    Tooltip tooltip = new Tooltip(tt.toString());
    tooltip.setWrapText(true);
    btn.setTooltip(tooltip);
    btn.setOnAction(event -> {
      showAssetDetails(value, VPinScreen);
    });

    return btn;
  }

  private void showAssetDetails(GameRepresentation game, VPinScreen VPinScreen) {
    tableView.getSelectionModel().clearSelection();
    selectGameInModel(game);
    Platform.runLater(() -> {
      this.tablesController.getAssetViewSideBarController().setGame(tablesController.getTableOverviewController(), game, VPinScreen);
    });
  }

  // filterGames() moved to TableOverviewPredicateFactory

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
      boolean errorneous = game.getValidationState() != null && game.getValidationState().getCode() > 0;
      validationError.setVisible(errorneous && !game.getIgnoredValidations().contains(-1));
      if (errorneous) {
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

    if (getSelections().size() > 1) {
      Optional<GameRepresentation> first = getSelections().stream().filter(game -> game.getValidationState() != null).findFirst();
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
  public void onViewActivated(NavigationOptions options) {
    NavigationController.setBreadCrumb(Arrays.asList("Tables"));
  }

  public void setRootController(TablesController tablesController) {
    this.tablesController = tablesController;
    new TableOverviewDragDropHandler(tablesController);

    // start the relod process when the stage is on
    Studio.stage.setOnShown(e -> this.onReload());
  }

  public List<GameRepresentation> getGames() {
    return games;
  }

  public void initSelection() {
    GameRepresentation game = getSelection();
    if (game != null) {
      NavigationController.setBreadCrumb(Arrays.asList("Tables", game.getGameDisplayName()));
    }
  }

  public GameRepresentation getSelection() {
    GameRepresentationModel selection = tableView.getSelectionModel().getSelectedItem();
    return selection != null ? selection.getGame() : null;
  }

  public List<GameRepresentation> getSelections() {
    List<GameRepresentationModel> models = tableView.getSelectionModel().getSelectedItems();
    return models.stream().map(model -> model.getGame()).collect(Collectors.toList());
  }

  public void updatePlaylist() {
    this.playlists = client.getPlaylistsService().getPlaylists();
    List<PlaylistRepresentation> refreshedData = new ArrayList<>(this.playlists);
    refreshedData.add(0, null);
    this.playlistCombo.setItems(FXCollections.observableList(refreshedData));
  }

  @Override
  public void onChanged(Change<? extends GameRepresentationModel> c) {
    boolean disable = c.getList().isEmpty() || c.getList().size() > 1;
    altColorUploadItem.setDisable(disable);
    mediaUploadItem.setDisable(disable);
    povItem.setDisable(disable);
    resItem.setDisable(disable);
    backglassUploadItem.setDisable(disable);
    iniUploadMenuItem.setDisable(disable);

    validateBtn.setDisable(c.getList().isEmpty());
    deleteBtn.setDisable(c.getList().isEmpty());
    playBtn.setDisable(disable);
    scanBtn.setDisable(c.getList().isEmpty());
    assetManagerBtn.setDisable(disable);
    tableEditBtn.setDisable(disable);
    validationError.setVisible(c.getList().size() != 1);

    if (c.getList().isEmpty()) {
      refreshView(Optional.empty());
    }
    else {
      GameRepresentation gameRepresentation = c.getList().get(0).getGame();
      playBtn.setDisable(gameRepresentation.getGameFilePath() == null);
      refreshView(Optional.ofNullable(gameRepresentation));

      if (gameRepresentation.getGameFilePath() != null) {
        GameEmulatorRepresentation gameEmulator = client.getFrontendService().getGameEmulator(gameRepresentation.getEmulatorId());
        playBtn.getItems().clear();

        List<String> altVPXExeNames = gameEmulator.getAltVPXExeNames();
        for (String altVPXExeName : altVPXExeNames) {
          MenuItem item = new MenuItem(altVPXExeName);
          item.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
              onPlay(altVPXExeName);
            }
          });
          playBtn.getItems().add(item);
        }
      }
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
      return DISABLED_COLOR;
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
    selectGameInModel(game);
    this.tableView.scrollTo(tableView.getSelectionModel().getSelectedItem());
  }

  public void selectGameInModel(GameRepresentation game) {
    Optional<GameRepresentationModel> model = models.stream().filter(m -> m.getGame().getId() == game.getId()).findFirst();
    if (model.isPresent()) {
      this.tableView.getSelectionModel().clearSelection();
      this.tableView.getSelectionModel().select(model.get());
      this.tableView.scrollTo(model.get());
    }
  }

  public void selectGameInModel(int gameId) {
    Optional<GameRepresentationModel> model = models.stream().filter(m -> m.getGame().getId() == gameId).findFirst();
    if (model.isPresent()) {
      this.tableView.getSelectionModel().clearSelection();
      this.tableView.getSelectionModel().select(model.get());
      this.tableView.scrollTo(model.get());
    }
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    status = client.getGameStatusService().getStatus();
    gameEmulatorChangeListener = new GameEmulatorChangeListener();

    contextMenuController = new TableOverviewContextMenu(this);
    tableOverviewColumnSorter = new TableOverviewColumnSorter(this);

    deleteSeparator.managedProperty().bindBidirectional(this.deleteSeparator.visibleProperty());

    this.playlistSplitter.managedProperty().bindBidirectional(playlistSplitter.visibleProperty());
    this.playlistCombo.managedProperty().bindBidirectional(this.playlistCombo.visibleProperty());
    this.importBtn.managedProperty().bindBidirectional(this.importBtn.visibleProperty());
    this.uploadTableBtn.managedProperty().bindBidirectional(this.uploadTableBtn.visibleProperty());
    this.deleteBtn.managedProperty().bindBidirectional(this.deleteBtn.visibleProperty());
    this.scanBtn.managedProperty().bindBidirectional(this.scanBtn.visibleProperty());
    this.playBtn.managedProperty().bindBidirectional(this.playBtn.visibleProperty());
    this.stopBtn.managedProperty().bindBidirectional(this.stopBtn.visibleProperty());
    this.assetManagerBtn.managedProperty().bindBidirectional(this.assetManagerBtn.visibleProperty());
    this.assetManagerViewBtn.managedProperty().bindBidirectional(this.assetManagerViewBtn.visibleProperty());
    this.tableEditBtn.managedProperty().bindBidirectional(this.tableEditBtn.visibleProperty());
    this.importSeparator.managedProperty().bindBidirectional(this.importSeparator.visibleProperty());

    Frontend frontend = client.getFrontendService().getFrontendCached();
    FrontendType frontendType = frontend.getFrontendType();

    FrontendUtil.replaceName(importBtn.getTooltip(), frontend);
    FrontendUtil.replaceName(uploadTableBtn.getTooltip(), frontend);
    FrontendUtil.replaceName(stopBtn.getTooltip(), frontend);

    if (frontendType.equals(FrontendType.Standalone)) {
      importBtn.setVisible(false);
      columnEmulator.setVisible(false);
    }

    if (!frontendType.supportPupPacks()) {
      uploadTableBtn.getItems().remove(pupPackUploadItem);
    }
    if (!frontendType.supportMedias()) {
      uploadTableBtn.getItems().remove(mediaUploadItem);
      this.assetManagerBtn.setVisible(false);
      this.assetManagerViewBtn.setVisible(false);
    }
    if (!frontendType.supportPlaylists()) {
      playlistCombo.setVisible(false);
      importSeparator.setVisible(false);
      playlistSplitter.setVisible(false);
    }

    try {
      FXMLLoader loader = new FXMLLoader(WaitOverlayController.class.getResource("overlay-wait.fxml"));
      tablesLoadingOverlay = loader.load();
      tablesLoadingOverlay.setTranslateY(-100);
      tablesLoadingController = loader.getController();
    }
    catch (IOException e) {
      LOG.error("Failed to load loading overlay: " + e.getMessage());
    }

    try {
      FXMLLoader loader = new FXMLLoader(TableFilterController.class.getResource("scene-tables-overview-filter.fxml"));
      loader.load();
      tableFilterController = loader.getController();
      tableFilterController.setTableController(this);
    }
    catch (IOException e) {
      LOG.error("Failed to load loading filter: " + e.getMessage(), e);
    }


    playlistCombo.setCellFactory(c -> new WidgetFactory.PlaylistBackgroundImageListCell());
    playlistCombo.setButtonCell(new WidgetFactory.PlaylistBackgroundImageListCell());
    playlistCombo.valueProperty().addListener(new ChangeListener<PlaylistRepresentation>() {
      @Override
      public void changed(ObservableValue<? extends PlaylistRepresentation> observableValue, PlaylistRepresentation playlist, PlaylistRepresentation t1) {
        predicateFactory.setFilterPlaylist(t1);
        data.setPredicate(predicateFactory.buildPredicate());
      }
    });

    bindTable();
    bindSearchField();

    if (frontendType.supportPupPacks()) {
      Image image3 = new Image(Studio.class.getResourceAsStream("popper-media.png"));
      ImageView iconMedia = new ImageView(image3);
      iconMedia.setFitWidth(18);
      iconMedia.setFitHeight(18);
      assetManagerBtn.setGraphic(iconMedia);

      Image image6 = new Image(Studio.class.getResourceAsStream("popper-assets.png"));
      ImageView view6 = new ImageView(image6);
      view6.setFitWidth(18);
      view6.setFitHeight(18);
      assetManagerViewBtn.setGraphic(view6);
    }
    else {
      columnPUPPack.setVisible(false);
    }

    columnPlaylists.setVisible(frontendType.supportPlaylists());

    preferencesChanged(PreferenceNames.UI_SETTINGS, null);
    preferencesChanged(PreferenceNames.SERVER_SETTINGS, null);
    preferencesChanged(PreferenceNames.VALIDATION_SETTINGS, null);
    preferencesChanged(PreferenceNames.IGNORED_VALIDATIONS, null);

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

    refreshColumns();
    assetManagerViewBtn.managedProperty().bindBidirectional(assetManagerViewBtn.visibleProperty());
  }

  private void refreshViewForEmulator() {
    FrontendType frontendType = client.getFrontendService().getFrontendType();
    GameEmulatorRepresentation newValue = emulatorCombo.getValue();
    tableFilterController.setEmulator(newValue);
    boolean vpxOrFpEmulator = newValue == null || newValue.isVpxEmulator() || newValue.isFpEmulator();
    boolean vpxEmulator = newValue == null || newValue.isVpxEmulator();

    this.importBtn.setVisible(!frontendType.equals(FrontendType.Standalone));
    this.importBtn.setDisable(!vpxOrFpEmulator);
    this.deleteBtn.setVisible(vpxOrFpEmulator);
    this.uploadTableBtn.setVisible(vpxEmulator);
    this.scanBtn.setVisible(vpxEmulator);
    this.playBtn.setVisible(vpxEmulator);
    this.stopBtn.setVisible(vpxEmulator);

    deleteSeparator.setVisible(vpxOrFpEmulator);

    refreshColumns();

    tablesController.getTablesSideBarController().refreshViewForEmulator(newValue);
  }

  private void refreshColumns() {
    GameEmulatorRepresentation newValue = emulatorCombo.getValue();
    boolean vpxMode = newValue == null || newValue.isVpxEmulator();
    FrontendType frontendType = client.getFrontendService().getFrontendType();

    columnVersion.setVisible(vpxMode && !assetManagerMode && uiSettings.isColumnVersion());
    columnEmulator.setVisible(vpxMode && !assetManagerMode && frontendType.isNotStandalone() && uiSettings.isColumnEmulator());
    columnVPS.setVisible(vpxMode && !assetManagerMode && uiSettings.isColumnVpsStatus());
    columnRom.setVisible(vpxMode && !assetManagerMode && uiSettings.isColumnRom());
    columnB2S.setVisible(vpxMode && !assetManagerMode && uiSettings.isColumnBackglass());
    columnPUPPack.setVisible(vpxMode && !assetManagerMode && uiSettings.isColumnPupPack() && frontendType.supportPupPacks());
    columnAltSound.setVisible(vpxMode && !assetManagerMode && uiSettings.isColumnAltSound());
    columnAltColor.setVisible(vpxMode && !assetManagerMode && uiSettings.isColumnAltColor());
    columnPOV.setVisible(vpxMode && !assetManagerMode && uiSettings.isColumnPov());
    columnINI.setVisible(vpxMode && !assetManagerMode && uiSettings.isColumnIni());
    columnRES.setVisible(vpxMode && !assetManagerMode && uiSettings.isColumnRes());
    columnHSType.setVisible(vpxMode && !assetManagerMode && uiSettings.isColumnHighscore());
    columnDateAdded.setVisible(!assetManagerMode && uiSettings.isColumnDateAdded());
    columnPlaylists.setVisible(!assetManagerMode && frontendType.supportPlaylists() && uiSettings.isColumnPlaylists());
  }

  @Override
  public void preferencesChanged(String key, Object value) {
    if (key.equals(PreferenceNames.UI_SETTINGS)) {
      uiSettings = client.getPreferenceService().getJsonPreference(PreferenceNames.UI_SETTINGS, UISettings.class);
      refreshColumns();
    }
    else if (key.equals(PreferenceNames.SERVER_SETTINGS)) {
      serverSettings = client.getPreferenceService().getJsonPreference(PreferenceNames.SERVER_SETTINGS, ServerSettings.class);
    }
    else if (key.equals(PreferenceNames.VALIDATION_SETTINGS)) {
      validationSettings = client.getPreferenceService().getJsonPreference(PreferenceNames.VALIDATION_SETTINGS, ValidationSettings.class);
    }
    else if (key.equals(PreferenceNames.IGNORED_VALIDATIONS)) {
      ignoredValidations = client.getPreferenceService().getJsonPreference(PreferenceNames.IGNORED_VALIDATIONS, IgnoredValidationSettings.class);
      refreshViewAssetColumns(assetManagerMode);
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

  public TablesController getTablesController() {
    return tablesController;
  }

  public boolean isAssetManagerMode() {
    return this.assetManagerMode;
  }

  public StackPane getTableStack() {
    return this.tableStack;
  }

  public StackPane getLoaderStack() {
    return loaderStack;
  }

  public TableView<GameRepresentationModel> getTableView() {
    return this.tableView;
  }

  public ServerSettings getServerSettings() {
    return this.serverSettings;
  }

  public UISettings getUISettings() {
    return this.uiSettings;
  }

  public Button getFilterButton() {
    return this.filterBtn;
  }

  public GameEmulatorRepresentation getEmulatorSelection() {
    return this.emulatorCombo.getSelectionModel().getSelectedItem();
  }

  class GameEmulatorChangeListener implements ChangeListener<GameEmulatorRepresentation> {
    @Override
    public void changed(ObservableValue<? extends GameEmulatorRepresentation> observable, GameEmulatorRepresentation oldValue, GameEmulatorRepresentation newValue) {
      // callback to filter tables, once the data has been reloaded
      Platform.runLater(() -> {
        reloadConsumers.add(selection -> {
          refreshViewForEmulator();
          tableFilterController.applyFilter();
        });
        // just reload from cache
        onSwitchFromCache();
      });
    }
  }

  //----------------------------------
  public static class GameRepresentationModel extends BaseLoadingModel<GameRepresentationModel> {

    private GameRepresentation game;

    VpsTable vpsTable;

    GameEmulatorRepresentation gameEmulator;

    public GameRepresentationModel(GameRepresentation game) {
      this.game = game;
    }

    public GameRepresentation getGame() {
      return game;
    }

    public void setGame(GameRepresentation game) {
      this.game = game;
      fireValueChangedEvent();
    }

    public VpsTable getVpsTable() {
      return vpsTable;
    }

    public GameEmulatorRepresentation getGameEmulator() {
      return gameEmulator;
    }

    @Override
    public String getName() {
      return game.getGameDisplayName();
    }

    @Override
    public void load() {
      this.vpsTable = client.getVpsService().getTableById(game.getExtTableId());
      this.gameEmulator = client.getFrontendService().getGameEmulator(game.getEmulatorId());
    }

    @Override
    public void loaded() {
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
      GameRepresentationModel that = (GameRepresentationModel) o;
      return Objects.equals(game, that.game);
    }

    @Override
    public int hashCode() {
      return Objects.hashCode(game);
    }

    @Override
    public String toString() {
      return "GameRepresentationModel \"" + game.getGameDisplayName() + "\"";
    }
  }
}