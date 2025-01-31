package de.mephisto.vpin.ui.tables;

import de.mephisto.vpin.commons.utils.JFXFuture;
import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.connectors.vps.VPS;
import de.mephisto.vpin.connectors.vps.model.VpsDiffTypes;
import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.altsound.AltSound;
import de.mephisto.vpin.restclient.assets.AssetType;
import de.mephisto.vpin.restclient.frontend.Frontend;
import de.mephisto.vpin.restclient.frontend.FrontendType;
import de.mephisto.vpin.restclient.frontend.TableDetails;
import de.mephisto.vpin.restclient.frontend.VPinScreen;
import de.mephisto.vpin.restclient.games.*;
import de.mephisto.vpin.restclient.games.descriptors.UploadDescriptor;
import de.mephisto.vpin.restclient.games.descriptors.UploadType;
import de.mephisto.vpin.restclient.pinvol.PinVolPreferences;
import de.mephisto.vpin.restclient.pinvol.PinVolTableEntry;
import de.mephisto.vpin.restclient.preferences.PreferenceChangeListener;
import de.mephisto.vpin.restclient.preferences.ServerSettings;
import de.mephisto.vpin.restclient.preferences.UISettings;
import de.mephisto.vpin.restclient.validation.*;
import de.mephisto.vpin.ui.*;
import de.mephisto.vpin.ui.events.EventManager;
import de.mephisto.vpin.ui.playlistmanager.PlaylistDialogs;
import de.mephisto.vpin.ui.tables.actions.BulkActions;
import de.mephisto.vpin.ui.tables.editors.AltSound2EditorController;
import de.mephisto.vpin.ui.tables.editors.AltSoundEditorController;
import de.mephisto.vpin.ui.tables.editors.TableScriptEditorController;
import de.mephisto.vpin.ui.tables.panels.BaseLoadingColumn;
import de.mephisto.vpin.ui.tables.panels.BaseTableController;
import de.mephisto.vpin.ui.tables.panels.PlayButtonController;
import de.mephisto.vpin.ui.tables.validation.GameValidationTexts;
import de.mephisto.vpin.ui.tables.vps.VpsTableColumn;
import de.mephisto.vpin.ui.util.*;
import edu.umd.cs.findbugs.annotations.Nullable;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
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
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static de.mephisto.vpin.commons.utils.WidgetFactory.DISABLED_COLOR;
import static de.mephisto.vpin.ui.Studio.client;
import static de.mephisto.vpin.ui.Studio.stage;

public class TableOverviewController extends BaseTableController<GameRepresentation, GameRepresentationModel>
    implements Initializable, StudioFXController, ListChangeListener<GameRepresentationModel>, PreferenceChangeListener {

  private final static Logger LOG = LoggerFactory.getLogger(TableOverviewController.class);

  public static final int ALL_VPX_ID = -10;

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
  TableColumn<GameRepresentationModel, GameRepresentationModel> columnRating;

  @FXML
  TableColumn<GameRepresentationModel, GameRepresentationModel> columnPUPPack;

  @FXML
  TableColumn<GameRepresentationModel, GameRepresentationModel> columnAltSound;

  @FXML
  TableColumn<GameRepresentationModel, GameRepresentationModel> columnAltColor;

  @FXML
  TableColumn<GameRepresentationModel, GameRepresentationModel> columnPinVol;

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
  TableColumn<GameRepresentationModel, GameRepresentationModel> columnDateModified;

  @FXML
  TableColumn<GameRepresentationModel, GameRepresentationModel> columnLauncher;

  @FXML
  TableColumn<GameRepresentationModel, GameRepresentationModel> columnPlayfield;

  @FXML
  TableColumn<GameRepresentationModel, GameRepresentationModel> columnBackglass;

  @FXML
  TableColumn<GameRepresentationModel, GameRepresentationModel> columnLoading;

  @FXML
  TableColumn<GameRepresentationModel, GameRepresentationModel> columnWheel;

  @FXML
  TableColumn<GameRepresentationModel, GameRepresentationModel> columnDMD;

  @FXML
  TableColumn<GameRepresentationModel, GameRepresentationModel> columnTopper;

  @FXML
  TableColumn<GameRepresentationModel, GameRepresentationModel> columnFullDMD;

  @FXML
  TableColumn<GameRepresentationModel, GameRepresentationModel> columnAudio;

  @FXML
  TableColumn<GameRepresentationModel, GameRepresentationModel> columnAudioLaunch;

  @FXML
  TableColumn<GameRepresentationModel, GameRepresentationModel> columnInfo;

  @FXML
  TableColumn<GameRepresentationModel, GameRepresentationModel> columnHelp;

  @FXML
  TableColumn<GameRepresentationModel, GameRepresentationModel> columnOther2;

  @FXML
  TableColumn<GameRepresentationModel, GameRepresentationModel> columnComment;

  @FXML
  private ComboBox<GameEmulatorRepresentation> emulatorCombo;

  @FXML
  private Label validationErrorLabel;

  @FXML
  private Label validationErrorText;

  @FXML
  private Node validationError;

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
  private Button playlistManagerBtn;

  @FXML
  private SplitMenuButton scanBtn;

  @FXML
  private MenuItem scanAllBtn;

  @FXML
  private Button stopBtn;

  @FXML
  private Button importBtn;

  @FXML
  private SplitMenuButton uploadTableBtn;

  @FXML
  private Separator assetManagerSeparator;

  @FXML
  private MenuItem backglassUploadItem;

  @FXML
  private MenuItem romsUploadItem;

  @FXML
  private MenuItem iniUploadMenuItem;

  @FXML
  private MenuItem nvUploadMenuItem;

  @FXML
  private MenuItem altSoundUploadItem;

  @FXML
  private MenuItem cfgUploadItem;

  @FXML
  private MenuItem patchItem;

  @FXML
  private MenuItem altColorUploadItem;

  @FXML
  private MenuItem dmdUploadItem;

  @FXML
  private MenuItem mediaUploadItem;

  @FXML
  private MenuItem musicUploadItem;

  @FXML
  private MenuItem pupPackUploadItem;

  @FXML
  private MenuItem povItem;

  @FXML
  private MenuItem resItem;

  @FXML
  private Hyperlink dismissBtn;

  @FXML
  private ToolBar toolbar;

  @FXML
  private HBox importUploadButtonGroup;

  @FXML
  private HBox validationButtonGroup;

  private boolean showVersionUpdates = true;
  private boolean showVpsUpdates = true;
  public static final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

  private UISettings uiSettings;
  private ServerSettings serverSettings;
  private ValidationSettings validationSettings;

  private final List<Consumer<GameRepresentation>> reloadConsumers = new ArrayList<>();

  private boolean assetManagerMode = false;
  private TableOverviewContextMenu contextMenuController;
  private IgnoredValidationSettings ignoredValidations;

  private PlayButtonController playButtonController;

  private GameEmulatorChangeListener gameEmulatorChangeListener;
  private GameStatus status;
  private VPinScreen assetScreenSelection;

  // Add a public no-args constructor
  public TableOverviewController() {
  }

  @FXML
  public void onBackglassManager(GameRepresentation game) {
    tablesController.switchToBackglassManagerTab(game);
  }

  @FXML
  private void onPlaylistManager() {
    openPlaylistManager(playlistCombo.getValue());
  }

  private void openPlaylistManager(PlaylistRepresentation playlistRepresentation) {
    PlaylistDialogs.openPlaylistManager(this, playlistRepresentation);
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
        tablesController.getAssetViewSideBarController().setGame(this.tablesController.getTableOverviewController(), getSelection(), assetScreenSelection == null ? VPinScreen.Wheel : assetScreenSelection);
        assetManagerViewBtn.getStyleClass().add("toggle-selected");
        if (!assetManagerViewBtn.getStyleClass().contains("toggle-button-selected")) {
          assetManagerViewBtn.getStyleClass().add("toggle-button-selected");
        }
        importUploadButtonGroup.setVisible(false);
        validationButtonGroup.setVisible(false);
      }
      else {
        assetManagerViewBtn.getStyleClass().remove("toggle-selected");
        assetManagerViewBtn.getStyleClass().remove("toggle-button-selected");
        importUploadButtonGroup.setVisible(true);
        validationButtonGroup.setVisible(true);
      }

      refreshViewAssetColumns(assetManagerMode);
      refreshColumns();

      GameRepresentation selectedItem = getSelection();
      clearSelection();
      if (selectedItem != null) {
        selectBeanInModel(selectedItem, false);
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
    GameRepresentation game = null;
    if (selectedItems != null && !selectedItems.isEmpty()) {
      game = selectedItems.get(0);
    }
    TableDialogs.openAltSoundUploadDialog(game, null, null, null);
  }

  @FXML
  public void onAltColorUpload() {
    List<GameRepresentation> selectedItems = getSelections();
    if (selectedItems != null && !selectedItems.isEmpty()) {
      TableDialogs.openAltColorUploadDialog(selectedItems.get(0), null, null, () -> Platform.runLater(() -> {
        tablesController.getTablesSideBarController().getTitledPaneAltColor().setExpanded(true);
      }));
    }
  }

  @FXML
  public void onRomsUpload() {
    TableDialogs.onRomUploads(null, null);
  }

  @FXML
  public void onCfgUpload() {
    TableDialogs.openCfgUploads(null, null);
  }

  @FXML
  public void onPatchUpload() {
    List<GameRepresentation> selectedItems = getSelections();
    if (selectedItems != null && !selectedItems.isEmpty()) {
      TableDialogs.openPatchUpload(selectedItems.get(0), null, null, null);
    }
  }

  @FXML
  public void onNvRamUpload() {
    TableDialogs.openNvRamUploads(null, null);
  }


  @FXML
  public void onMusicUpload() {
    TableDialogs.onMusicUploads(null, null, null);
  }


  @FXML
  public void onPupPackUpload() {
    List<GameRepresentation> selectedItems = getSelections();
    if (selectedItems != null && !selectedItems.isEmpty()) {
      TableDialogs.openPupPackUploadDialog(selectedItems.get(0), null, null, () -> Platform.runLater(() -> {
        tablesController.getTablesSideBarController().getTitledPaneDirectB2s().setExpanded(true);
      }));
    }
  }

  @FXML
  public void onBackglassUpload() {
    List<GameRepresentation> selectedItems = getSelections();
    if (selectedItems != null && !selectedItems.isEmpty()) {
      boolean b = TableDialogs.directUpload(stage, AssetType.DIRECTB2S, selectedItems.get(0), null);
      if (b) {
        tablesController.getTablesSideBarController().getTitledPaneDirectB2s().setExpanded(true);
      }
    }
  }

  @FXML
  public void onIniUpload() {
    List<GameRepresentation> selectedItems = getSelections();
    if (selectedItems != null && !selectedItems.isEmpty()) {
      boolean b = TableDialogs.directUpload(stage, AssetType.INI, selectedItems.get(0), null);
      if (b) {
        tablesController.getTablesSideBarController().getTitledPaneIni().setExpanded(true);
      }
    }
  }

  @FXML
  public void onMediaUpload() {
    List<GameRepresentation> selectedItems = getSelections();
    if (selectedItems != null && !selectedItems.isEmpty()) {
      TableDialogs.openMediaUploadDialog(Studio.stage, selectedItems.get(0), null, null, null);
    }
  }

  @FXML
  public void onDMDUpload() {
    List<GameRepresentation> selectedItems = getSelections();
    if (selectedItems != null && !selectedItems.isEmpty()) {
      TableDialogs.openDMDUploadDialog(selectedItems.get(0), null, null, null);
    }
  }

  @FXML
  public void onPOVUpload() {
    List<GameRepresentation> selectedItems = getSelections();
    if (selectedItems != null && !selectedItems.isEmpty()) {
      boolean b = TableDialogs.directUpload(stage, AssetType.POV, selectedItems.get(0), null);
      if (b) {
        tablesController.getTablesSideBarController().getTitledPanePov().setExpanded(true);
      }
    }
  }

  @FXML
  public void onResUpload() {
    List<GameRepresentation> selectedItems = getSelections();
    if (selectedItems != null && !selectedItems.isEmpty()) {
      TableDialogs.directUpload(stage, AssetType.RES, selectedItems.get(0), null);
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
  public void onVpsResetUpdates() {
    List<GameRepresentation> selectedItems = getSelections();
    onVpsResetUpdates(selectedItems);
  }

  public static void onVpsResetUpdates(List<GameRepresentation> selectedItems) {
    List<GameRepresentation> collect = selectedItems.stream().filter(g -> !g.getVpsUpdates().isEmpty()).collect(Collectors.toList());
    ProgressDialog.createProgressDialog(new VPSResetProgressModel(collect));
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

  /**
   * Not mapped to a button in toolbar, but could be. Useful for context menu
   */
  @FXML
  public void onTableStatusToggle() {
    List<GameRepresentation> selections = getSelections();
    toggleTableStatus(selections);
  }

  private static void toggleTableStatus(List<GameRepresentation> games) {
    for (GameRepresentation game : games) {
      TableDetails detail = client.getFrontendService().getTableDetails(game.getId());
      boolean isDisable = game.isDisabled();
      detail.setStatus(isDisable ? 1 : 0);
      try {
        client.getFrontendService().saveTableDetails(detail, game.getId());
        EventManager.getInstance().notifyTableChange(game.getId(), null);
      }
      catch (Exception e) {
        LOG.error("Cannot " + (isDisable ? "enable" : "disable") + " the game " + game.getGameFileName(), e);
        WidgetFactory.showAlert(Studio.stage, "The table \"" + game.getGameDisplayName()
            + "\" couldn't be " + (isDisable ? "enabled" : "disabled") + ".", "Please try again.");
      }
    }
  }

  @FXML
  public void onBackup() {
    List<GameRepresentation> selectedItems = getSelections();
    TableDialogs.openTablesBackupDialog(selectedItems);
  }

  @FXML
  public void onStop() {
    Frontend frontend = client.getFrontendService().getFrontendCached();
    Optional<ButtonType> result = WidgetFactory.showConfirmation(Studio.stage,
        FrontendUtil.replaceNames("Stop all emulators and [Frontend] processes?", frontend, null));
    if (result.isPresent() && result.get().equals(ButtonType.OK)) {
      client.getFrontendService().terminateFrontend();
    }
  }

  @FXML
  public void onTableUpload() {
    openUploadDialogWithCheck(null);
  }

  public void openUploadDialogWithCheck(@Nullable UploadType uploadType) {
    if (client.getFrontendService().isFrontendRunning()) {
      if (Dialogs.openFrontendRunningWarning(Studio.stage)) {
        openUploadDialog(uploadType);
      }
      return;
    }

    openUploadDialog(uploadType);
  }

  private void openUploadDialog(@Nullable UploadType uploadType) {
    GameRepresentation game = getSelection();
    if (game != null) {
      GameEmulatorRepresentation emu = client.getFrontendService().getGameEmulator(game.getEmulatorId());
      TableDialogs.openTableUploadDialog(game, emu.getEmulatorType(), uploadType, null);
    }
    else {
      GameEmulatorRepresentation value = emulatorCombo.getValue();
      if (value != null) {
        TableDialogs.openTableUploadDialog(null, value.getEmulatorType(), uploadType, null);
      }
      else {
        TableDialogs.openTableUploadDialog(null, null, uploadType, null);
      }
    }
  }

  public void refreshFilters() {
    getTableFilterController().applyFilters();
  }

  public void refreshUploadResult(UploadDescriptor uploadResult) {
    if (uploadResult != null && uploadResult.getGameId() != -1) {
      //the cache miss will result in caching the new table
      GameRepresentation game = client.getGameService().getGame(uploadResult.getGameId());
      reloadItem(game);

      //required for new table that may or may not be part of the filtered view
      refreshFilters();

      // select game if not already selected
      GameRepresentation selected = getSelection();
      if (selected == null || selected.getId() != game.getId()) {
        setSelection(game);
      }

      if (assetManagerMode) {
        onAssetView();
      }

      if (uiSettings.isAutoEditTableData()) {
        Platform.runLater(() -> {
          TableDialogs.openTableDataDialog(this, game);
        });
      }
    }
  }

  @FXML
  protected void onDelete(Event e) {
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

      TableDialogs.openTableDeleteDialog(this, selectedGames, getData());
      tableView.getSelectionModel().clearSelection();
    }
  }

  @FXML
  private void onTableMouseClicked(MouseEvent mouseEvent) {
    if (mouseEvent.getButton().equals(MouseButton.PRIMARY)) {
      if (mouseEvent.getClickCount() == 2) {
        if (mouseEvent.isShiftDown()) {
          onMediaEdit();
          return;
        }
        onTableEdit();
      }
    }
  }

  @FXML
  public void onTablesScan() {
    List<GameRepresentation> selectedItems = getSelections();
    ProgressDialog.createProgressDialog(new TableScanProgressModel("Scanning Tables", selectedItems));
    for (GameRepresentation selectedItem : selectedItems) {
      EventManager.getInstance().notifyTableChange(selectedItem.getId(), selectedItem.getRom());
    }
  }

  @FXML
  public void onTablesScanAll() {
    boolean scanned = TableDialogs.openScanAllDialog(getData());
    if (scanned) {
      this.doReload();
    }
  }

  @FXML
  public void onImport() {
    if (client.getFrontendService().isFrontendRunning()) {
      if (Dialogs.openFrontendRunningWarning(Studio.stage)) {
        TableDialogs.openTableImportDialog(this.emulatorCombo.getValue());
      }
    }
    else {
      TableDialogs.openTableImportDialog(this.emulatorCombo.getValue());
    }
  }

  @FXML
  public void onValidate() {
    List<GameRepresentation> selectedItems = getSelections();
    TableDialogs.openValidationDialog(new ArrayList<>(selectedItems), false);
  }

  @FXML
  public void onValidateAll() {
    boolean done = TableDialogs.openValidationDialog(getData(), true);
    if (done) {
      doReload();
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


  @FXML
  private void onReload(ActionEvent e) {
    ProgressDialog.createProgressDialog(new CacheInvalidationProgressModel());
    this.doReload();
  }

  public void doReload() {
    status = client.getGameStatusService().getStatus();
    client.getGameService().clearCache();
    doReload(true);
  }

  public void onSwitchFromCache() {
    doReload(false);
  }

  public void doReload(boolean clearCache) {
    UISettings uiSettings = client.getPreferenceService().getJsonPreference(PreferenceNames.UI_SETTINGS, UISettings.class);
    this.showVersionUpdates = !uiSettings.isHideVersions();
    this.showVpsUpdates = !uiSettings.isHideVPSUpdates();

    startReload("Loading Tables...");

    refreshPlaylists();
    refreshEmulators(uiSettings);

    this.searchTextField.setDisable(true);
    this.reloadBtn.setDisable(true);
    this.scanBtn.setDisable(true);
    this.scanAllBtn.setDisable(true);
    this.playButtonController.setDisable(true);
    this.validateBtn.setDisable(true);
    this.tableEditBtn.setDisable(true);
    this.deleteBtn.setDisable(true);
    this.uploadTableBtn.setDisable(true);
    this.importBtn.setDisable(true);
    this.stopBtn.setDisable(true);

    GameRepresentation selection = getSelection();
    GameRepresentationModel selectedItem = tableView.getSelectionModel().getSelectedItem();
    GameEmulatorRepresentation value = this.emulatorCombo.getSelectionModel().getSelectedItem();
    boolean isAllVpxSelected = client.getFrontendService().isAllVpx(value);

    JFXFuture.supplyAsync(() -> {
          if (clearCache) {
            if (isAllVpxSelected) {
              client.getGameService().clearCache();
            }
            else {
              client.getGameService().clearCache(value.getId());
            }
          }

          return isAllVpxSelected
              ? client.getGameService().getVpxGamesCached()
              : client.getGameService().getGamesByEmulator(value.getId());
        })
        .onErrorSupply(e -> {
          LOG.error("Loading tables failed", e);
          Platform.runLater(() -> WidgetFactory.showAlert(stage, "Error", "Loading tables failed: " + e.getMessage()));
          return Collections.emptyList();
        })
        .thenAcceptLater(data -> {
          // as the load of tables could take some time, users may have switched to another emulators in between
          // if this is the case, do not refresh the UI with the results
          GameEmulatorRepresentation valueAfterSearch = this.emulatorCombo.getValue();
          if (valueAfterSearch != null && (value == null || valueAfterSearch.getId() != value.getId())) {
            return;
          }

          tableView.getSelectionModel().getSelectedItems().removeListener(this);
          setItems(data);
          refreshFilters();

          if (selection != null) {
            final Optional<GameRepresentationModel> updatedGame = this.models.stream().filter(g -> g.getGameId() == selection.getId()).findFirst();
            if (updatedGame.isPresent()) {
              GameRepresentation gameRepresentation = updatedGame.get().getBean();
              //tableView.getSelectionModel().select(gameRepresentation);
              this.playButtonController.setDisable(gameRepresentation.getGameFilePath() == null);
            }
          }

          if (!data.isEmpty()) {
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
          this.searchTextField.setDisable(false);
          this.reloadBtn.setDisable(false);
          this.scanBtn.setDisable(false);
          this.scanAllBtn.setDisable(false);
          this.uploadTableBtn.setDisable(false);

          tableView.requestFocus();

          tableView.getSelectionModel().getSelectedItems().addListener(this);
          if (selectedItem == null) {
            //TODO this will result in a duplicate initial selection which may lead to a deadlock
//        tableView.getSelectionModel().select(0);
          }
          else {
            tableView.getSelectionModel().select(selectedItem);
          }

          for (Consumer<GameRepresentation> reloadConsumer : reloadConsumers) {
            reloadConsumer.accept(selection);
          }
          reloadConsumers.clear();

          endReload();

          //TODO fixed above TODO by postphone the selection, no idea if this is feasable
          Platform.runLater(() -> {
            if (tableView.getSelectionModel().getSelectedItems().isEmpty()) {
              tableView.getSelectionModel().select(0);
            }
          });
        });
  }

  private void refreshEmulators(UISettings uiSettings) {
    this.emulatorCombo.valueProperty().removeListener(gameEmulatorChangeListener);
    GameEmulatorRepresentation selectedEmu = this.emulatorCombo.getSelectionModel().getSelectedItem();

    this.emulatorCombo.setDisable(true);
    List<GameEmulatorRepresentation> filtered = new ArrayList<>(client.getFrontendService().getFilteredEmulatorsWithAllVpx(uiSettings));
    this.emulatorCombo.setItems(FXCollections.observableList(filtered));
    this.emulatorCombo.setDisable(false);

    if (selectedEmu == null) {
      this.emulatorCombo.getSelectionModel().selectFirst();
    }

    this.emulatorCombo.valueProperty().addListener(gameEmulatorChangeListener);
  }

  private void bindTable() {
    tableView.setPlaceholder(new Label("No matching tables found."));

    tableView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

    FrontendType frontendType = client.getFrontendService().getFrontendType();

    // set ValueCellFactory and CellFactory, and get a renderer that is responsible to render the cell
    BaseLoadingColumn.configureColumn(columnDisplayName, (value, model) -> {
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
    }, this, true);

    BaseLoadingColumn.configureLoadingColumn(columnEmulator, "", (value, model) -> {
      GameEmulatorRepresentation gameEmulator = model.getGameEmulator();
      Label label = new Label(gameEmulator.getName());
      label.getStyleClass().add("default-text");
      label.setStyle(getLabelCss(value));
      return label;
    });

    BaseLoadingColumn.configureColumn(columnVersion, (value, model) -> {
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
    }, this, true);

    BaseLoadingColumn.configureColumn(columnRom, (value, model) -> {
      String rom = value.getRom();
      List<Integer> ignoredValidations = Collections.emptyList();
      if (value.getIgnoredValidations() != null) {
        ignoredValidations = value.getIgnoredValidations();
      }
      if (!StringUtils.isEmpty(value.getRomAlias())) {
        rom = rom + " [" + value.getRomAlias() + "]";
      }

      Label label = new Label(rom);
      if (!StringUtils.isEmpty(value.getRomAlias())) {
        label.setTooltip(new Tooltip("This rom is aliased."));
      }
      if (!value.isRomExists() && value.isRomRequired() && !ignoredValidations.contains(GameValidationCode.CODE_ROM_NOT_EXISTS)) {
        String color = WidgetFactory.ERROR_COLOR;
        label.setStyle("-fx-font-color: " + color + ";-fx-text-fill: " + color + ";-fx-font-weight: bold;");
      }
      else {
        label.getStyleClass().add("default-text");
        label.setStyle(getLabelCss(value));
      }
      return label;
    }, this, true);

    BaseLoadingColumn.configureColumn(columnHSType, (value, model) -> {
      String hsType = value.getHighscoreType();
      if (!StringUtils.isEmpty(hsType) && hsType.equals("EM")) {
        hsType = "Text";
      }
      Label label = new Label(hsType);
      label.getStyleClass().add("default-text");
      label.setStyle(getLabelCss(value));
      return label;
    }, this, true);

    BaseLoadingColumn.configureColumn(columnB2S, (value, model) -> {
      boolean hasUpdate = this.showVpsUpdates && uiSettings.isVpsBackglass() && value.getVpsUpdates().contains(VpsDiffTypes.b2s);
      if (value.getDirectB2SPath() != null) {
        if (hasUpdate) {
          return WidgetFactory.createCheckAndUpdateIcon("New backglass updates available");
        }
        else {
          return WidgetFactory.createCheckboxIcon(getIconColor(value), value.getDirectB2SPath());
        }
      }
      else if (hasUpdate) {
        return WidgetFactory.createUpdateIcon("New backglass updates available");
      }
      return null;
    }, this, true);

    BaseLoadingColumn.configureLoadingColumn(columnVPS, "Loading...", (value, model) -> {
      UISettings uiSettings = client.getPreferenceService().getJsonPreference(PreferenceNames.UI_SETTINGS, UISettings.class);
      return new VpsTableColumn(model.getGame().getExtTableId(), model.getGame().getExtTableVersionId(), model.getGame().getVpsUpdates(), uiSettings);
    });

    BaseLoadingColumn.configureColumn(columnPOV, (value, model) -> {
      boolean hasUpdate = this.showVpsUpdates && uiSettings.isVpsPOV() && value.getVpsUpdates().contains(VpsDiffTypes.pov);
      if (value.getPovPath() != null) {
        if (hasUpdate) {
          return WidgetFactory.createCheckAndUpdateIcon("New POV updates available");
        }
        else {
          return WidgetFactory.createCheckboxIcon(getIconColor(value), value.getPovPath());
        }
      }
      else if (hasUpdate) {
        return WidgetFactory.createUpdateIcon("New POV updates available");
      }
      return null;
    }, this, true);

    BaseLoadingColumn.configureColumn(columnINI, (value, model) -> {
      if (value.getIniPath() != null) {
        return WidgetFactory.createCheckboxIcon(getIconColor(value), value.getIniPath());
      }
      return null;
    }, this, true);

    BaseLoadingColumn.configureColumn(columnRES, (value, model) -> {
      if (value.getResPath() != null) {
        return WidgetFactory.createCheckboxIcon(getIconColor(value), value.getResPath());
      }
      return null;
    }, this, true);

    BaseLoadingColumn.configureColumn(columnAltSound, (value, model) -> {
      boolean hasUpdate = this.showVpsUpdates && uiSettings.isVpsAltSound() && value.getVpsUpdates().contains(VpsDiffTypes.altSound);
      if (value.isAltSoundAvailable()) {
        if (hasUpdate) {
          return WidgetFactory.createCheckAndUpdateIcon("New ALT sound updates available");
        }
        else {
          return WidgetFactory.createCheckboxIcon(getIconColor(value));
        }
      }
      else if (hasUpdate) {
        return WidgetFactory.createUpdateIcon("New ALT sound updates available");
      }
      return null;
    }, this, true);

    BaseLoadingColumn.configureColumn(columnAltColor, (value, model) -> {
      boolean hasUpdate = this.showVpsUpdates && uiSettings.isVpsAltColor() && value.getVpsUpdates().contains(VpsDiffTypes.altColor);
      if (value.getAltColorType() != null) {
        if (hasUpdate) {
          return WidgetFactory.createCheckAndUpdateIcon("New ALT color updates available");
        }
        else {
          return WidgetFactory.createCheckboxIcon(getIconColor(value));
        }
      }
      else if (hasUpdate) {
        return WidgetFactory.createUpdateIcon("New ALT color updates available");
      }
      return null;
    }, this, true);

    BaseLoadingColumn.configureColumn(columnPUPPack, (value, model) -> {
      boolean hasUpdate = this.showVpsUpdates && uiSettings.isVpsPUPPack() && value.getVpsUpdates().contains(VpsDiffTypes.pupPack);
      if (value.getPupPackPath() != null) {
        if (hasUpdate) {
          return WidgetFactory.createCheckAndUpdateIcon("New PUP pack updates available");
        }
        else {
          return WidgetFactory.createCheckboxIcon(getIconColor(value), value.getPupPackPath());
        }
      }
      else if (hasUpdate) {
        return WidgetFactory.createUpdateIcon("New PUP pack updates available");
      }
      return null;
    }, this, true);

    BaseLoadingColumn.configureColumn(columnStatus, (value, model) -> {
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

      if (!StringUtils.isEmpty(value.getComment())) {
        String notes = value.getComment();
        Tooltip tooltip = new Tooltip(value.getComment());
        tooltip.setWrapText(true);
        btn.setTooltip(tooltip);

        FontIcon icon = WidgetFactory.createIcon("mdi2c-comment");
        icon.setIconSize(16);
        graphics.getChildren().add(0, icon);

        if (notes.toLowerCase().contains("//error")) {
          icon.setIconColor(Paint.valueOf(WidgetFactory.ERROR_COLOR));
        }
        else if (notes.toLowerCase().contains("//todo")) {
          icon.setIconColor(Paint.valueOf(WidgetFactory.TODO_COLOR));
        }
        else if (notes.toLowerCase().contains("//outdated")) {
          icon.setIconColor(Paint.valueOf(WidgetFactory.OUTDATED_COLOR));
        }
      }

      btn.setGraphic(graphics);
      btn.setOnAction(event -> {
        tableView.getSelectionModel().clearSelection();
        tableView.getSelectionModel().select(model);
        Platform.runLater(() -> {
          TableDialogs.openCommentDialog(value);
        });
      });

      return btn;
    }, this, true);

    BaseLoadingColumn.configureColumn(columnPinVol, (value, model) -> {
      Label label = new Label("-");
      PinVolPreferences prefs = client.getPinVolService().getPinVolTablePreferences();
      GameRepresentation game = model.getGame();
      PinVolTableEntry entry = prefs.getTableEntry(game.getGameFileName(), game.isVpxGame(), game.isFpGame());
      if (entry != null) {
        StringBuilder builder = new StringBuilder();
        builder.append(entry.getPrimaryVolume());
        builder.append(" / ");
        builder.append(entry.getSecondaryVolume());

        if (entry.getSsfBassVolume() > 0 || entry.getSsfFrontVolume() > 0 || entry.getSsfRearVolume() > 0) {
          builder.append(" / ");
          builder.append(entry.getSsfBassVolume());
          builder.append(" / ");
          builder.append(entry.getSsfRearVolume());
          builder.append(" / ");
          builder.append(entry.getSsfFrontVolume());
        }

        StringBuilder tt = new StringBuilder();
        tt.append("Primary Volume:\t");
        tt.append(entry.getPrimaryVolume());
        tt.append("\n");
        tt.append("Secondary Volume:\t");
        tt.append(entry.getSecondaryVolume());
        tt.append("\n");
        if (entry.getSsfBassVolume() > 0 || entry.getSsfFrontVolume() > 0 || entry.getSsfRearVolume() > 0) {
          tt.append("Bass Volume:\t\t");
          tt.append(entry.getSsfBassVolume());
          tt.append("\n");
          tt.append("Rear Volume:\t\t");
          tt.append(entry.getSsfRearVolume());
          tt.append("\n");
          tt.append("Front Volume:\t\t");
          tt.append(entry.getSsfFrontVolume());

        }


        label.setTooltip(new Tooltip(tt.toString()));
        label.setText(builder.toString());
      }
      label.getStyleClass().add("default-text");
      return label;
    }, this, true);

    BaseLoadingColumn.configureColumn(columnDateAdded, (value, model) -> {
      Label label = null;
      if (value.getDateAdded() != null) {
        label = new Label(dateFormat.format(value.getDateAdded()));
      }
      else {
        label = new Label("-");
      }
      label.getStyleClass().add("default-text");
      return label;
    }, this, true);


    BaseLoadingColumn.configureColumn(columnRating, (value, model) -> {
      int rating = value.getRating();
      int nonRating = 5 - rating;

      HBox root = new HBox(1);
      root.setAlignment(Pos.CENTER);
      int index = 0;
      for (int i = 0; i < rating; i++) {
        Label label = new Label();
        label.setUserData(index);
        FontIcon icon = WidgetFactory.createIcon("mdi2s-star");
        icon.setIconSize(20);
        label.setGraphic(icon);
        label.setCursor(Cursor.HAND);
        label.setOnMouseClicked(event -> setGameRating(value, (Integer) label.getUserData()));
        root.getChildren().add(label);
        index++;
      }

      for (int i = 0; i < nonRating; i++) {
        Label label = new Label();
        label.setUserData(index);
        FontIcon icon = WidgetFactory.createIcon("mdi2s-star-outline");
        label.setGraphic(icon);
        icon.setIconSize(20);
        icon.setIconColor(Paint.valueOf(DISABLED_COLOR));
        label.setCursor(Cursor.HAND);
        label.setOnMouseClicked(event -> setGameRating(value, (Integer) label.getUserData()));
        root.getChildren().add(label);
        index++;
      }
      return root;
    }, this, true);


    BaseLoadingColumn.configureColumn(columnDateModified, (value, model) -> {
      Label label = null;
      if (value.getDateAdded() != null) {
        label = new Label(dateFormat.format(value.getDateUpdated()));
      }
      else {
        label = new Label("-");
      }
      label.getStyleClass().add("default-text");
      return label;
    }, this, true);

    BaseLoadingColumn.configureColumn(columnLauncher, (value, model) -> {
      Label label = new Label(model.getGame().getLauncher());
      label.getStyleClass().add("default-text");
      return label;
    }, this, true);

    columnComment.setSortable(false);
    BaseLoadingColumn.configureColumn(columnComment, (value, model) -> {
      String text = model.getGame().getComment();
      Label label = new Label();
      if (!StringUtils.isEmpty(text)) {
        label.setText(text.replaceAll("\\n", " "));
        label.setTooltip(new Tooltip(text));
      }
      label.getStyleClass().add("default-text");
      return label;
    }, this, true);

    columnPlaylists.setSortable(false);
    BaseLoadingColumn.configureColumn(columnPlaylists, (value, model) -> {
      HBox box = new HBox();
      List<PlaylistRepresentation> matches = new ArrayList<>();
      List<PlaylistRepresentation> playlists = getPlaylists();
      if (playlists != null) {
        for (PlaylistRepresentation playlist : playlists) {
          if (playlist != null && playlist.containsGame(value.getId())) {
            matches.add(playlist);
          }
        }
      }

      int ICON_WIDTH = 22;
      double width = 0;
      int count = 0;

      //force refresh here since the color is saved through the playlists
      UISettings uiSettings = client.getPreferenceService().getJsonPreference(PreferenceNames.UI_SETTINGS, UISettings.class);
      for (PlaylistRepresentation match : matches) {
        if (width < (columnPlaylists.widthProperty().get() - ICON_WIDTH)) {
          Label playlistIcon = WidgetFactory.createPlaylistIcon(match, uiSettings);
          if (match.getId() >= 0) {
            Button plButton = new Button("", playlistIcon.getGraphic());
            plButton.getStyleClass().add("ghost-button-tiny");
            plButton.setOnAction(new EventHandler<ActionEvent>() {
              @Override
              public void handle(ActionEvent event) {
                openPlaylistManager(match);
              }
            });
            box.getChildren().add(plButton);
          }
          else {
            box.getChildren().add(playlistIcon);
          }
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
    }, this, true);


    List<VPinScreen> supportedScreens = client.getFrontendService().getFrontendCached().getSupportedScreens();
    BaseLoadingColumn.configureColumn(columnPlayfield, (value, model) -> createAssetStatus(value, model, VPinScreen.PlayField, event -> {
      showAssetDetails(value, VPinScreen.PlayField);
    }), this, supportedScreens.contains(VPinScreen.PlayField));
    BaseLoadingColumn.configureColumn(columnBackglass, (value, model) -> createAssetStatus(value, model, VPinScreen.BackGlass, event -> {
      showAssetDetails(value, VPinScreen.BackGlass);
    }), this, supportedScreens.contains(VPinScreen.BackGlass));
    BaseLoadingColumn.configureColumn(columnLoading, (value, model) -> createAssetStatus(value, model, VPinScreen.Loading, event -> {
      showAssetDetails(value, VPinScreen.Loading);
    }), this, supportedScreens.contains(VPinScreen.Loading));
    BaseLoadingColumn.configureColumn(columnWheel, (value, model) -> createAssetStatus(value, model, VPinScreen.Wheel, event -> {
      showAssetDetails(value, VPinScreen.Wheel);
    }), this, supportedScreens.contains(VPinScreen.Wheel));
    BaseLoadingColumn.configureColumn(columnDMD, (value, model) -> createAssetStatus(value, model, VPinScreen.DMD, event -> {
      showAssetDetails(value, VPinScreen.DMD);
    }), this, supportedScreens.contains(VPinScreen.DMD));
    BaseLoadingColumn.configureColumn(columnTopper, (value, model) -> createAssetStatus(value, model, VPinScreen.Topper, event -> {
      showAssetDetails(value, VPinScreen.Topper);
    }), this, supportedScreens.contains(VPinScreen.Topper));
    BaseLoadingColumn.configureColumn(columnFullDMD, (value, model) -> createAssetStatus(value, model, VPinScreen.Menu, event -> {
      showAssetDetails(value, VPinScreen.Menu);
    }), this, supportedScreens.contains(VPinScreen.Menu));
    BaseLoadingColumn.configureColumn(columnAudio, (value, model) -> createAssetStatus(value, model, VPinScreen.Audio, event -> {
      showAssetDetails(value, VPinScreen.Audio);
    }), this, supportedScreens.contains(VPinScreen.Audio));
    BaseLoadingColumn.configureColumn(columnAudioLaunch, (value, model) -> createAssetStatus(value, model, VPinScreen.AudioLaunch, event -> {
      showAssetDetails(value, VPinScreen.AudioLaunch);
    }), this, supportedScreens.contains(VPinScreen.AudioLaunch));
    BaseLoadingColumn.configureColumn(columnInfo, (value, model) -> createAssetStatus(value, model, VPinScreen.GameInfo, event -> {
      showAssetDetails(value, VPinScreen.GameInfo);
    }), this, supportedScreens.contains(VPinScreen.GameInfo));
    BaseLoadingColumn.configureColumn(columnHelp, (value, model) -> createAssetStatus(value, model, VPinScreen.GameHelp, event -> {
      showAssetDetails(value, VPinScreen.GameHelp);
    }), this, supportedScreens.contains(VPinScreen.GameHelp));
    BaseLoadingColumn.configureColumn(columnOther2, (value, model) -> createAssetStatus(value, model, VPinScreen.Other2, event -> {
      showAssetDetails(value, VPinScreen.Other2);
    }), this, supportedScreens.contains(VPinScreen.Other2));

    tableView.setEditable(true);
    tableView.setRowFactory(
        tableView -> {
          final TableRow<GameRepresentationModel> row = new TableRow<>();
          final ContextMenu menu = new ContextMenu();

          //ListChangeListener<GameRepresentation> changeListener = (ListChangeListener.Change<? extends GameRepresentation> c) ->
          //    contextMenuController.refreshContextMenu(tableView, menu, this.getSelection());

          row.itemProperty().addListener((obs, oldItem, newItem) -> {
            if (newItem == null) {
//              menu.getItems().clear();
            }
            else {
              contextMenuController.refreshContextMenu(tableView, menu, newItem.getGame());
            }
          });

          row.emptyProperty().addListener((obs, wasEmpty, isNowEmpty) ->
              row.setContextMenu(isNowEmpty ? null : menu));

          return row;
        });

  }

  private void setGameRating(GameRepresentation value, int i) {
    try {
      TableDetails tableDetails = client.getFrontendService().getTableDetails(value.getId());
      if (tableDetails != null) {
        tableDetails.setGameRating((i + 1));
        client.getFrontendService().saveTableDetails(tableDetails, value.getId());
        EventManager.getInstance().notifyTableChange(value.getId(), null, null);
      }
    }
    catch (Exception e) {
      LOG.error("Rating update failed: {}", e.getMessage(), e);
      WidgetFactory.showAlert(stage, "Error", "Rating update failed: " + e.getMessage());
    }
  }

  //------------------------------

  public static Node createAssetStatus(GameRepresentation value, GameRepresentationModel model, VPinScreen screen, EventHandler eventHandler) {
    ValidationSettings validationSettings = client.getPreferenceService().getJsonPreference(PreferenceNames.VALIDATION_SETTINGS, ValidationSettings.class);
    FrontendMediaItemRepresentation defaultMediaItem = model.getFrontendMedia().getDefaultMediaItem(screen);
    ValidationProfile defaultProfile = validationSettings.getDefaultProfile();
    ValidationConfig config = defaultProfile.getOrCreateConfig(screen.getValidationCode());
    boolean ignored = value.getIgnoredValidations().contains(screen.getValidationCode());

    StringBuilder tt = new StringBuilder();
    Button btn = new Button();
    btn.getStyleClass().add("table-media-button");

    //TODO this whole calculation has be be moved into the FrontendMedia entity.
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
    btn.setOnAction(eventHandler);

    return btn;
  }

  private void showAssetDetails(GameRepresentation game, VPinScreen screen) {
    assetScreenSelection = screen;
    selectBeanInModel(game, false);

    Platform.runLater(() -> {
      this.tablesController.getAssetViewSideBarController().setGame(tablesController.getTableOverviewController(), game, assetScreenSelection);
    });
  }

  @Override
  public void reloadItem(GameRepresentation refreshedGame) {
    // reload only if the emulator is matching
    GameEmulatorRepresentation value = this.emulatorCombo.getValue();
    if (value != null && (value.getId() == refreshedGame.getEmulatorId() || value.getEmulatorType().equals(value.getEmulatorType()))) {
      super.reloadItem(refreshedGame);
    }
  }

  /**
   *
   */
  @Override
  public void refreshView(GameRepresentation game) {
    dismissBtn.setVisible(true);

    setValidationVisible(false);
    validationErrorLabel.setText("");
    validationErrorText.setText("");

    if (assetManagerMode) {
      this.tablesController.getAssetViewSideBarController().setGame(tablesController.getTableOverviewController(), game, assetScreenSelection);
    }
    else {
      List<GameRepresentation> games = this.tableView.getSelectionModel().getSelectedItems().stream().map(GameRepresentationModel::getGame).collect(Collectors.toList());
      this.tablesController.getTablesSideBarController().setGames(game != null ? Optional.of(game) : Optional.empty(), games);
    }
    if (game != null) {
      boolean errorneous = game.getValidationState() != null && game.getValidationState().getCode() > 0;
      setValidationVisible(errorneous && !game.getIgnoredValidations().contains(-1));
      if (errorneous) {
        LocalizedValidation validationMessage = GameValidationTexts.validate(game);
        validationErrorLabel.setText(validationMessage.getLabel());
        validationErrorText.setText(validationMessage.getText());
      }
      NavigationController.setBreadCrumb(Arrays.asList("Tables", game.getGameDisplayName()));
    }
    else {
      setValidationVisible(false);
      NavigationController.setBreadCrumb(Arrays.asList("Tables"));
    }

    if (getSelections().size() > 1) {
      Optional<GameRepresentation> first = getSelections().stream().filter(g -> g.getValidationState() != null).findFirst();
      if (first.isPresent()) {
        dismissBtn.setVisible(false);

        setValidationVisible(true);
        validationErrorLabel.setText("One or more of the selected tables have issues.");
        validationErrorText.setText("");
      }
    }
  }

  public void setValidationVisible(boolean visible) {
    validationError.setVisible(visible);
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

    if (this.models == null) {
      this.doReload();
    }

    GameRepresentation game = getSelection();
    if (game != null) {
      NavigationController.setBreadCrumb(Arrays.asList("Tables", game.getGameDisplayName()));
    }
  }

  @Override
  public void setRootController(TablesController tablesController) {
    super.setRootController(tablesController);
    // start the reload process when the stage is on
    Studio.stage.setOnShown(e -> this.doReload());
  }

  public List<GameRepresentation> getGames() {
    return getData();
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
    playButtonController.setDisable(disable);
    scanBtn.setDisable(c.getList().isEmpty());
    assetManagerBtn.setDisable(disable);
    tableEditBtn.setDisable(disable);
    setValidationVisible(c.getList().size() != 1);

    if (c.getList().isEmpty()) {
      refreshView(null);
    }
    else {
      GameRepresentation gameRepresentation = c.getList().get(0).getGame();
      playButtonController.setDisable(gameRepresentation.getGameFilePath() == null);
      playButtonController.setData(gameRepresentation);
      refreshView(gameRepresentation);
    }

    List<GameRepresentation> selection = new ArrayList<>(c.getList().stream().map(g -> g.getGame()).collect(Collectors.toList()));
    EventManager.getInstance().notifyTableSelectionChanged(selection);
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

  public static String getLabelCss(GameRepresentation value) {
    String status = "";
    if (value.isDisabled()) {
      status = WidgetFactory.DISABLED_TEXT_STYLE;
    }
    return status;
  }


  public void selectGameInModel(int gameId) {
    Optional<GameRepresentationModel> model = models.stream().filter(m -> m.getGame().getId() == gameId).findFirst();
    if (model.isPresent()) {
      this.tableView.getSelectionModel().clearSelection();
      this.tableView.getSelectionModel().select(model.get());
      this.tableView.scrollTo(model.get());
    }
    else {
      GameRepresentation game = client.getGameService().getGame(gameId);
      GameEmulatorRepresentation gameEmulator = client.getFrontendService().getGameEmulator(game.getEmulatorId());
      GameEmulatorRepresentation value = emulatorCombo.getValue();
      if (value.getId() != gameEmulator.getId()) {
        reloadConsumers.add(new Consumer<GameRepresentation>() {
          @Override
          public void accept(GameRepresentation gameRepresentation) {
            Platform.runLater(() -> {
              selectGameInModel(gameId);
            });
          }
        });
        emulatorCombo.setValue(gameEmulator);
      }
    }
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    super.initialize("game", "games", new TableOverviewColumnSorter(this));
//    validationError.managedProperty().bindBidirectional(validationError.visibleProperty());
    validationButtonGroup.managedProperty().bindBidirectional(validationButtonGroup.visibleProperty());
    importUploadButtonGroup.managedProperty().bindBidirectional(importUploadButtonGroup.visibleProperty());
    playlistManagerBtn.managedProperty().bindBidirectional(playlistManagerBtn.visibleProperty());


    status = client.getGameStatusService().getStatus();
    gameEmulatorChangeListener = new GameEmulatorChangeListener();

    contextMenuController = new TableOverviewContextMenu(this);

    deleteSeparator.managedProperty().bindBidirectional(this.deleteSeparator.visibleProperty());

    this.uploadTableBtn.managedProperty().bindBidirectional(this.uploadTableBtn.visibleProperty());
    this.deleteBtn.managedProperty().bindBidirectional(this.deleteBtn.visibleProperty());
    this.scanBtn.managedProperty().bindBidirectional(this.scanBtn.visibleProperty());
    this.stopBtn.managedProperty().bindBidirectional(this.stopBtn.visibleProperty());
    this.assetManagerSeparator.managedProperty().bindBidirectional(assetManagerSeparator.visibleProperty());
    this.assetManagerBtn.managedProperty().bindBidirectional(this.assetManagerBtn.visibleProperty());
    this.assetManagerViewBtn.managedProperty().bindBidirectional(this.assetManagerViewBtn.visibleProperty());
    this.tableEditBtn.managedProperty().bindBidirectional(this.tableEditBtn.visibleProperty());
    this.importSeparator.managedProperty().bindBidirectional(this.importSeparator.visibleProperty());
    this.importBtn.managedProperty().bindBidirectional(this.importBtn.visibleProperty());

    Frontend frontend = client.getFrontendService().getFrontendCached();
    FrontendType frontendType = frontend.getFrontendType();

    FrontendUtil.replaceName(importBtn.getTooltip(), frontend);
    FrontendUtil.replaceName(uploadTableBtn.getTooltip(), frontend);
    FrontendUtil.replaceName(stopBtn.getTooltip(), frontend);

    playlistManagerBtn.setVisible(frontendType.supportPlaylistsCrud());

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

    super.loadFilterPanel("scene-tables-overview-filter.fxml");

    super.loadPlaylistCombo();

    setValidationVisible(false);

    new TableOverviewDragDropHandler(this, tableView, loaderStack);

    bindTable();

    if (frontendType.supportPupPacks()) {
      Image image3 = new Image(Studio.class.getResourceAsStream("popper-media.png"));
      ImageView iconMedia = new ImageView(image3);
      iconMedia.setFitWidth(18);
      iconMedia.setFitHeight(18);
//      assetManagerBtn.setGraphic(iconMedia);

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
    preferencesChanged(PreferenceNames.IGNORED_VALIDATION_SETTINGS, null);

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

    Platform.runLater(() -> {
      getTableFilterController().applyFilters();
    });

    try {
      FXMLLoader loader = new FXMLLoader(PlayButtonController.class.getResource("play-btn.fxml"));
      SplitMenuButton playBtn = loader.load();
      playButtonController = loader.getController();
      int i = toolbar.getItems().indexOf(stopBtn);
      toolbar.getItems().add(i, playBtn);
    }
    catch (IOException e) {
      LOG.error("failed to load play button: " + e.getMessage(), e);
    }
  }

  private void refreshViewForEmulator() {
    FrontendType frontendType = client.getFrontendService().getFrontendType();
    GameEmulatorRepresentation newValue = emulatorCombo.getValue();
    getTableFilterController().setEmulator(newValue);
    boolean vpxOrFpEmulator = newValue == null || newValue.isVpxEmulator() || newValue.isFpEmulator();
    boolean vpxEmulator = newValue == null || newValue.isVpxEmulator();

    this.importBtn.setVisible(!frontendType.equals(FrontendType.Standalone) && vpxOrFpEmulator);
    this.importSeparator.setVisible(!frontendType.equals(FrontendType.Standalone) && vpxOrFpEmulator);
    this.importBtn.setDisable(!vpxOrFpEmulator);
    this.deleteBtn.setVisible(vpxOrFpEmulator);
    this.uploadTableBtn.setVisible(vpxOrFpEmulator);
    this.scanBtn.setVisible(vpxEmulator);
    this.playButtonController.setVisible(vpxOrFpEmulator);
    this.stopBtn.setVisible(vpxOrFpEmulator);

    altSoundUploadItem.setVisible(vpxEmulator);
    altColorUploadItem.setVisible(vpxEmulator);
    dmdUploadItem.setVisible(vpxEmulator);
    patchItem.setVisible(vpxEmulator);
    iniUploadMenuItem.setVisible(vpxEmulator);
    povItem.setVisible(vpxEmulator);
    nvUploadMenuItem.setVisible(vpxEmulator);
    resItem.setVisible(vpxEmulator);
    mediaUploadItem.setVisible(vpxOrFpEmulator);
    musicUploadItem.setVisible(vpxEmulator);
    cfgUploadItem.setVisible(vpxEmulator);
    romsUploadItem.setVisible(vpxEmulator);
    pupPackUploadItem.setVisible(vpxEmulator);

    deleteSeparator.setVisible(vpxOrFpEmulator);

    refreshColumns();

    tablesController.getTablesSideBarController().refreshViewForEmulator(newValue);
  }

  private TableFilterController getTableFilterController() {
    return (TableFilterController) filterController;
  }


  private void refreshColumns() {
    GameEmulatorRepresentation newValue = emulatorCombo.getValue();
    boolean vpxMode = newValue == null || newValue.isVpxEmulator();
    boolean fpMode = newValue == null || newValue.isFpEmulator();
    boolean fxMode = newValue == null || newValue.isFxEmulator();
    FrontendType frontendType = client.getFrontendService().getFrontendType();

    columnVersion.setVisible((vpxMode || fpMode) && !assetManagerMode && uiSettings.isColumnVersion());
    columnEmulator.setVisible((vpxMode || fpMode) && !assetManagerMode && frontendType.isNotStandalone() && uiSettings.isColumnEmulator());
    columnVPS.setVisible((vpxMode || fpMode || fxMode) && !assetManagerMode && uiSettings.isColumnVpsStatus());
    columnRom.setVisible(vpxMode && !assetManagerMode && uiSettings.isColumnRom());
    columnB2S.setVisible((vpxMode || fpMode) && !assetManagerMode && uiSettings.isColumnBackglass());
    columnRating.setVisible((vpxMode || fpMode) && !assetManagerMode && frontendType.supportRating() && uiSettings.isColumnRating());
    columnPUPPack.setVisible(vpxMode && !assetManagerMode && uiSettings.isColumnPupPack() && frontendType.supportPupPacks());
    columnPinVol.setVisible(vpxMode && !assetManagerMode && uiSettings.isColumnPinVol());
    columnAltSound.setVisible(vpxMode && !assetManagerMode && uiSettings.isColumnAltSound());
    columnAltColor.setVisible(vpxMode && !assetManagerMode && uiSettings.isColumnAltColor());
    columnPOV.setVisible(vpxMode && !assetManagerMode && uiSettings.isColumnPov());
    columnINI.setVisible(vpxMode && !assetManagerMode && uiSettings.isColumnIni());
    columnRES.setVisible(vpxMode && !assetManagerMode && uiSettings.isColumnRes());
    columnHSType.setVisible(vpxMode && !assetManagerMode && uiSettings.isColumnHighscore());
    columnDateAdded.setVisible((vpxMode || fpMode) && !assetManagerMode && uiSettings.isColumnDateAdded());
    columnDateModified.setVisible((vpxMode || fpMode) && !assetManagerMode && uiSettings.isColumnDateModified());
    columnLauncher.setVisible(vpxMode && !assetManagerMode && uiSettings.isColumnLauncher());
    columnComment.setVisible((vpxMode || fpMode || fxMode) && !assetManagerMode && uiSettings.isColumnComment());
    columnPlaylists.setVisible((vpxMode || fpMode || fxMode) && !assetManagerMode && frontendType.supportPlaylists() && uiSettings.isColumnPlaylists());
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
    else if (key.equals(PreferenceNames.IGNORED_VALIDATION_SETTINGS)) {
      ignoredValidations = client.getPreferenceService().getJsonPreference(PreferenceNames.IGNORED_VALIDATION_SETTINGS, IgnoredValidationSettings.class);
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

  public ServerSettings getServerSettings() {
    return this.serverSettings;
  }

  public UISettings getUISettings() {
    return this.uiSettings;
  }

  public GameEmulatorRepresentation getEmulatorSelection() {
    GameEmulatorRepresentation selectedEmu = this.emulatorCombo.getSelectionModel().getSelectedItem();
    return client.getFrontendService().isAllVpx(selectedEmu) ? null : selectedEmu;
  }

  public void onPlay() {
    playButtonController.onPlay();
  }

  class GameEmulatorChangeListener implements ChangeListener<GameEmulatorRepresentation> {
    @Override
    public void changed(ObservableValue<? extends GameEmulatorRepresentation> observable, GameEmulatorRepresentation oldValue, GameEmulatorRepresentation newValue) {
      // callback to filter tables, once the data has been reloaded
      Platform.runLater(() -> {
        reloadConsumers.add(selection -> {
          refreshViewForEmulator();
          getTableFilterController().applyFilters();
        });
        // just reload from cache
        onSwitchFromCache();
      });
    }
  }

  //----------------------------------

  protected GameRepresentationModel toModel(GameRepresentation game) {
    return new GameRepresentationModel(game);
  }

  public class PlaylistBackgroundImageListCell extends ListCell<PlaylistRepresentation> {
    public PlaylistBackgroundImageListCell() {
    }

    protected void updateItem(PlaylistRepresentation item, boolean empty) {
      super.updateItem(item, empty);
      setGraphic(null);
      setText(null);
      if (item != null) {
        Label playlistIcon = WidgetFactory.createPlaylistIcon(item, uiSettings);
        setGraphic(playlistIcon);

        setText(" " + item.toString());
      }
    }
  }

  @Override
  public void onKeyEvent(KeyEvent event) {
    super.onKeyEvent(event);
    if (event.isConsumed()) {
      return;
    }

    contextMenuController.handleKeyEvent(event);

    List<GameRepresentation> games = tableView.getSelectionModel().getSelectedItems().stream().map(g -> g.getBean()).collect(Collectors.toList());


    if (event.getCode() == KeyCode.K && event.isControlDown()) {
      onStop();
      event.consume();
    }
    else if (!games.isEmpty() && BulkActions.consume(games, event)) {
      //done
    }
  }
}