package de.mephisto.vpin.ui.tables;

import de.mephisto.vpin.commons.utils.JFXFuture;
import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.commons.utils.localsettings.BaseTableSettings;
import de.mephisto.vpin.connectors.vps.VPS;
import de.mephisto.vpin.connectors.vps.model.VpsDiffTypes;
import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.altsound.AltSound;
import de.mephisto.vpin.restclient.backups.BackupDescriptorRepresentation;
import de.mephisto.vpin.restclient.competitions.CompetitionRepresentation;
import de.mephisto.vpin.restclient.competitions.CompetitionType;
import de.mephisto.vpin.restclient.emulators.GameEmulatorRepresentation;
import de.mephisto.vpin.restclient.frontend.*;
import de.mephisto.vpin.restclient.games.FrontendMediaItemRepresentation;
import de.mephisto.vpin.restclient.games.GameRepresentation;
import de.mephisto.vpin.restclient.games.GameStatus;
import de.mephisto.vpin.restclient.games.descriptors.UploadDescriptor;
import de.mephisto.vpin.restclient.highscores.HighscoreType;
import de.mephisto.vpin.restclient.iscored.IScoredSettings;
import de.mephisto.vpin.restclient.jobs.JobType;
import de.mephisto.vpin.restclient.mania.ManiaSettings;
import de.mephisto.vpin.restclient.pinvol.PinVolPreferences;
import de.mephisto.vpin.restclient.pinvol.PinVolTableEntry;
import de.mephisto.vpin.restclient.playlists.PlaylistRepresentation;
import de.mephisto.vpin.restclient.preferences.PreferenceChangeListener;
import de.mephisto.vpin.restclient.preferences.ServerSettings;
import de.mephisto.vpin.restclient.preferences.UISettings;
import de.mephisto.vpin.restclient.validation.*;
import de.mephisto.vpin.restclient.vps.VpsSettings;
import de.mephisto.vpin.ui.*;
import de.mephisto.vpin.ui.backups.BackupDialogs;
import de.mephisto.vpin.ui.events.EventManager;
import de.mephisto.vpin.ui.events.JobFinishedEvent;
import de.mephisto.vpin.ui.events.StudioEventListener;
import de.mephisto.vpin.ui.playlistmanager.PlaylistDialogs;
import de.mephisto.vpin.ui.tables.actions.BulkActions;
import de.mephisto.vpin.ui.tables.editors.AltSound2EditorController;
import de.mephisto.vpin.ui.tables.editors.AltSoundEditorController;
import de.mephisto.vpin.ui.tables.panels.BaseLoadingColumn;
import de.mephisto.vpin.ui.tables.panels.BaseTableController;
import de.mephisto.vpin.ui.tables.panels.PlayButtonController;
import de.mephisto.vpin.ui.tables.panels.UploadsButtonController;
import de.mephisto.vpin.ui.tables.validation.GameValidationTexts;
import de.mephisto.vpin.ui.tables.vps.VpsTableColumn;
import de.mephisto.vpin.ui.tables.vps.VpsTutorialColumn;
import de.mephisto.vpin.ui.util.*;
import edu.umd.cs.findbugs.annotations.NonNull;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
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
import org.jetbrains.annotations.NotNull;
import org.kordamp.ikonli.javafx.FontIcon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static de.mephisto.vpin.commons.utils.WidgetFactory.DISABLED_COLOR;
import static de.mephisto.vpin.ui.Studio.*;

public class TableOverviewController extends BaseTableController<GameRepresentation, GameRepresentationModel>
    implements Initializable, StudioFXController, ListChangeListener<GameRepresentationModel>, PreferenceChangeListener, StudioEventListener {

  private final static Logger LOG = LoggerFactory.getLogger(TableOverviewController.class);

  public static final int ALL_VPX_ID = -10;

  @FXML
  private Separator deleteSeparator;

  @FXML
  TableColumn<GameRepresentationModel, GameRepresentationModel> columnDisplayName;

  @FXML
  TableColumn<GameRepresentationModel, GameRepresentationModel> columnVersion;

  @FXML
  TableColumn<GameRepresentationModel, GameRepresentationModel> columnPatchVersion;

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
  TableColumn<GameRepresentationModel, GameRepresentationModel> columnTutorials;

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
  private ToolBar secondaryToolbar;

  @FXML
  private Node validationError;

  @FXML
  private Button tableEditBtn;

  @FXML
  private Button emulatorBtn;

  @FXML
  private Button converterBtn;

  @FXML
  private Separator importSeparator;

  @FXML
  private Separator mappingSeparator;

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
  private Button exportBtn;

  @FXML
  private Separator assetManagerSeparator;

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
  private VpsSettings vpsSettings;
  private ServerSettings serverSettings;
  private IScoredSettings iScoredSettings;

  private final List<Consumer<GameRepresentation>> reloadConsumers = new ArrayList<>();

  private boolean assetManagerMode = false;
  private TableOverviewContextMenu contextMenuController;
  private IgnoredValidationSettings ignoredValidations;

  private PlayButtonController playButtonController;
  private UploadsButtonController uploadsButtonController;

  private GameEmulatorChangeListener gameEmulatorChangeListener;
  private GameStatus status;
  private VPinScreen assetScreenSelection;
  private Parent playBtn;
  private Parent uploadsButton;

  // Add a public no-args constructor
  public TableOverviewController() {
  }

  @FXML
  private void onEmulatorManager() {
    GameEmulatorRepresentation emu = emulatorCombo.getSelectionModel().getSelectedItem();
    if (emu != null) {
      NavigationController.navigateTo(NavigationItem.SystemManager, new NavigationOptions(emulatorCombo.getSelectionModel().getSelectedItem()));
    }
  }

  public void onTableReload() {
    List<GameRepresentation> selections = getSelections();
    for (GameRepresentation selection : selections) {
      client.getGameService().reload(selection.getId());
      EventManager.getInstance().notifyTableChange(selection.getId(), null);
    }
  }

  @FXML
  private void onPlaylistManager() {
    openPlaylistManager(playlistCombo.getValue());
  }

  private void openPlaylistManager(PlaylistRepresentation playlistRepresentation) {
    PlaylistDialogs.openPlaylistManager(this, playlistRepresentation);
  }

  @FXML
  private void onConvert() {
    TableDialogs.openConverterDialog(getSelections());
  }

  @FXML
  public void onAssetView() {
    tablesController.setSidebarVisible(true);

    tablesController.getTablesSideBarController().getTitledPaneMedia().setExpanded(false);

    assetManagerMode = !assetManagerMode;
    tablesController.getAssetViewSideBarController().setVisible(assetManagerMode);
    tablesController.getTablesSideBarController().setVisible(!assetManagerMode);
    converterBtn.setVisible(assetManagerMode);

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

  @FXML
  public void onResetRatings() {
    List<GameRepresentation> selectedItems = getSelections();
    for (GameRepresentation selectedItem : selectedItems) {
      setGameRating(selectedItem, -1);
    }
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
    JFXFuture.supplyAsync(() -> {
      return client.getAuthenticationService().isAuthenticated();
    }).thenAcceptLater(authenticated -> {
      if (authenticated) {
        List<GameRepresentation> selectedItems = getSelections();
        BackupDialogs.openTablesBackupDialog(selectedItems);
      }
      else {
        WidgetFactory.showInformation(stage, "Authentication Required", "Go to the backup settings for more details.");
      }
    });
  }

  @FXML
  public void onStop() {
    Frontend frontend = client.getFrontendService().getFrontendCached();
    Optional<ButtonType> result = WidgetFactory.showConfirmation(Studio.stage,
        FrontendUtil.replaceNames("Stop all emulators and [Frontend] processes?", frontend, null));
    if (result.isPresent() && result.get().equals(ButtonType.OK)) {
      JFXFuture.supplyAsync(() -> {
        return client.getFrontendService().terminateFrontend();
      }).thenAcceptLater((requestResult) -> {
        LOG.info("Kill frontend request finished.");
      });
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
        List<CompetitionRepresentation> gameCompetitions = client.getCompetitionService().getGameCompetitions(game.getId());
        for (CompetitionRepresentation gameCompetition : gameCompetitions) {
          Optional<ButtonType> result = WidgetFactory.showConfirmation(stage, "The table \"" + game.getGameDisplayName()
                  + "\" is used by for competition \"" + gameCompetition.toString() + "\" (type: " + gameCompetition.getType() + ").",
              "Delete this competition?",
              "You need to delete all competition references before deleting a table.", "Delete Competition");
          if (result.isPresent() && result.get().equals(ButtonType.OK)) {
            client.getCompetitionService().deleteCompetition(gameCompetition);
            EventManager.getInstance().notifyTableChange(game.getId(), null);
          }
          else {
            return;
          }
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
  public void onEmulatorReload() {
    GameEmulatorRepresentation value = emulatorCombo.getValue();
    if (value != null) {
      ProgressDialog.createProgressDialog(ClearCacheProgressModel.getReloadGamesClearCacheModel(value.getId()));
      this.doReload();
    }
  }

  @FXML
  public void onReload() {
    ProgressDialog.createProgressDialog(ClearCacheProgressModel.getReloadGamesClearCacheModel(true));
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
    uiSettings = client.getPreferenceService().getJsonPreference(PreferenceNames.UI_SETTINGS, UISettings.class);
    vpsSettings = client.getPreferenceService().getJsonPreference(PreferenceNames.VPS_SETTINGS, VpsSettings.class);
    this.showVersionUpdates = !uiSettings.isHideVersions();
    this.showVpsUpdates = !vpsSettings.isHideVPSUpdates();

    startReload("Loading Tables...");

    refreshPlaylists();
    refreshEmulators();

    this.searchTextField.setDisable(true);
    this.reloadBtn.setDisable(true);
    this.scanBtn.setDisable(true);
    this.scanAllBtn.setDisable(true);
    this.playButtonController.setDisable(true);
    this.validateBtn.setDisable(true);
    this.tableEditBtn.setDisable(true);
    this.deleteBtn.setDisable(true);
    this.uploadsButtonController.setDisable(true);
    this.importBtn.setDisable(true);
    this.exportBtn.setDisable(true);
    this.stopBtn.setDisable(true);

    this.emulatorCombo.setDisable(true);
    this.playlistCombo.setDisable(true);

    GameRepresentation selection = getSelection();
    GameRepresentationModel selectedItem = tableView.getSelectionModel().getSelectedItem();
    GameEmulatorRepresentation value = this.emulatorCombo.getSelectionModel().getSelectedItem();
    boolean isAllVpxSelected = client.getEmulatorService().isAllVpx(value);

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
          this.emulatorCombo.setDisable(false);
          this.playlistCombo.setDisable(false);

          tableView.getSelectionModel().getSelectedItems().removeListener(this);
          setItems(data);
          refreshFilters();

          if (selection != null) {
            final Optional<GameRepresentationModel> updatedGame = this.models.stream().filter(g -> g.getGameId() == selection.getId()).findFirst();
            if (updatedGame.isPresent()) {
              GameRepresentation gameRepresentation = updatedGame.get().getBean();
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

          GameEmulatorRepresentation emulatorRepresentation = emulatorCombo.valueProperty().get();
          this.importBtn.setDisable(!isAllVpxSelected);
          this.exportBtn.setDisable(!isAllVpxSelected);
          this.stopBtn.setDisable(false);
          this.searchTextField.setDisable(false);
          this.reloadBtn.setDisable(false);
          this.scanBtn.setDisable(false);
          this.scanAllBtn.setDisable(false);
          this.uploadsButtonController.setDisable(false);

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

  private void refreshEmulators() {
    this.emulatorCombo.valueProperty().removeListener(gameEmulatorChangeListener);
    final GameEmulatorRepresentation selectedEmu = this.emulatorCombo.getSelectionModel().getSelectedItem();

    this.emulatorCombo.setDisable(true);
    JFXFuture.supplyAsync(() -> client.getEmulatorService().getFilteredEmulatorsWithAllVpx(uiSettings))
        .thenAcceptLater(filtered -> {
          this.emulatorCombo.valueProperty().removeListener(gameEmulatorChangeListener);
          this.emulatorCombo.setItems(FXCollections.observableList(filtered));
          this.emulatorCombo.setDisable(false);

          if (selectedEmu != null) {
            this.emulatorCombo.getSelectionModel().select(selectedEmu);
          }
          GameEmulatorRepresentation newSelection = this.emulatorCombo.getSelectionModel().getSelectedItem();
          if (newSelection == null) {
            this.emulatorCombo.getSelectionModel().selectFirst();
            newSelection = this.emulatorCombo.getSelectionModel().getSelectedItem();
          }

          emulatorBtn.setDisable(newSelection == null || newSelection.getId() == -1);
          this.emulatorCombo.valueProperty().addListener(gameEmulatorChangeListener);
        });
  }

  private void bindTable() {
    tableView.setPlaceholder(new Label("No matching games found."));

    tableView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

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
        String ttText = "The table version in [Frontend] is \"" + value.getVersion()
            + "\", while the linked VPS table has version \"" + value.getExtVersion() + "\".\n\n"
            + "Update the table, correct the selected VPS table or fix the version in the \"Table Data\" section.";
        ttText = FrontendUtil.replaceName(ttText, frontend);
        Tooltip tt = new Tooltip(ttText);
        tt.setWrapText(true);
        tt.setMaxWidth(400);
        label.setTooltip(tt);
        label.setGraphic(updateIcon);
      }
      return label;
    }, this, true);

    BaseLoadingColumn.configureColumn(columnPatchVersion, (value, model) -> {
      Label label = new Label(value.getPatchVersion());
      label.getStyleClass().add("default-text");
      label.setStyle(getLabelCss(value));
      return label;
    }, this, true);

    BaseLoadingColumn.configureColumn(columnRom, (value, model) -> {
      final String rom = value.getRom();
      if (StringUtils.isEmpty(rom)) {
        return new Label();
      }

      String labelValue = rom;
      List<Integer> ignoredValidations = Collections.emptyList();
      if (value.getIgnoredValidations() != null) {
        ignoredValidations = value.getIgnoredValidations();
      }
      if (!StringUtils.isEmpty(value.getRomAlias())) {
        labelValue = rom + " [" + value.getRomAlias() + "]";
      }

      Label label = new Label(labelValue);
      if (!StringUtils.isEmpty(value.getRomAlias())) {
        StringBuilder builder = new StringBuilder("This ROM is aliased and uses the ROM \"" + value.getRom() + "\"");
        List<GameRepresentation> sharedGames = getData().stream().filter(g -> !StringUtils.isEmpty(g.getRom()) && g.getRom().equals(rom) && g.getId() != value.getId()).collect(Collectors.toList());
        if (!sharedGames.isEmpty()) {
          builder.append("\n\nThe following tables share the same ROM:\n");
          for (GameRepresentation sharedGame : sharedGames) {
            builder.append("- " + sharedGame.getGameDisplayName());
          }
        }
        label.setTooltip(new Tooltip(builder.toString()));
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
      HighscoreType type = value.getHighscoreType();
      String hsType = HighscoreType.EM.equals(type) ? "Text" : type != null ? type.name() : null;
      Label label = new Label(hsType);
      label.getStyleClass().add("default-text");
      label.setStyle(getLabelCss(value));
      return label;
    }, this, true);

    BaseLoadingColumn.configureColumn(columnB2S, (value, model) -> {
      boolean hasUpdate = this.showVpsUpdates && vpsSettings.isVpsBackglass() && value.getVpsUpdates().contains(VpsDiffTypes.b2s);
      if (value.getDirectB2SPath() != null) {
        int nbVersions = value.getNbDirectB2S();
        FontIcon icon = null;
        if (nbVersions > 9) {
          icon = WidgetFactory.createIcon("mdi2n-numeric-9-plus-box-multiple-outline", getIconColor(value));
          //icon = WidgetFactory.createIcon("mdi2n-numeric-9-plus-circle-outline", 24, getIconColor(value));
        }
        else if (nbVersions > 1) {
          icon = WidgetFactory.createIcon("mdi2n-numeric-" + nbVersions + "-box-multiple-outline", getIconColor(value));
          //icon = WidgetFactory.createIcon("mdi2n-numeric-" + nbVersions + "-circle-outline", 24, getIconColor(value));
        }
        else {
          icon = WidgetFactory.createEditIcon(value.isDisabled() ? DISABLED_COLOR : "#FFFFFF");
        }

        Button button = new Button();
        icon.setIconSize(22);
        button.getStyleClass().add("table-media-button");
        button.setGraphic(icon);
        button.setOnAction(new EventHandler<ActionEvent>() {
          @Override
          public void handle(ActionEvent event) {
            tablesController.switchToBackglassManagerTab(value);
          }
        });

        if (hasUpdate) {
          button.setTooltip(new Tooltip("A new backglass version or an update for the existing one is available"));
        }
        else {
          button.setTooltip(new Tooltip(value.getDirectB2SPath()));
        }

        return button;
      }
      else if (hasUpdate) {
        return WidgetFactory.createUpdateIcon("A new backglass version or an update for the existing one is available");
      }
      return null;
    }, this, true);

    BaseLoadingColumn.configureLoadingColumn(columnVPS, "Loading...", (value, model) -> {
      return new VpsTableColumn(model.getGame().getExtTableId(), model.getGame().getExtTableVersionId(), value.isDisabled(), model.getGame().isIgnoreUpdates(), model.getGame().getVpsUpdates(), vpsSettings);
    });

    BaseLoadingColumn.configureColumn(columnPOV, (value, model) -> {
      boolean hasUpdate = this.showVpsUpdates && vpsSettings.isVpsPOV() && value.getVpsUpdates().contains(VpsDiffTypes.pov);
      if (value.getPovPath() != null) {
        if (model.getGame().isIgnoreUpdates()) {
          return WidgetFactory.createCheckAndIgnoredIcon("Updates for this table are ignored.");
        }
        if (hasUpdate) {
          return WidgetFactory.createCheckAndUpdateIcon("A new POV file or an update for the existing one is available");
        }
        else {
          return WidgetFactory.createCheckboxIcon(getIconColor(value), value.getPovPath());
        }
      }
      else if (hasUpdate) {
        return WidgetFactory.createUpdateIcon("A new POV file or an update for the existing one is available");
      }
      return null;
    }, this, true);

    BaseLoadingColumn.configureColumn(columnINI, (value, model) -> {
      if (value.getIniPath() != null) {
        Button compBtn = new Button();
        compBtn.getStyleClass().add("table-media-button");
        compBtn.setTooltip(new Tooltip("Edit " + value.getIniPath()));
        FontIcon cmpIcon = WidgetFactory.createEditIcon(null);
        cmpIcon.setIconSize(22);
        compBtn.setGraphic(cmpIcon);
        compBtn.setOnAction(new EventHandler<ActionEvent>() {
          @Override
          public void handle(ActionEvent event) {
            try {
              GameRepresentation gameRepresentation = value;
              String iniPath = gameRepresentation.getIniPath();
              Studio.editGameFile(gameRepresentation, iniPath);
            }
            catch (Exception e) {
              LOG.error("Failed to open .ini file: {}", e.getMessage(), e);
              WidgetFactory.showAlert(Studio.stage, "Error", "Failed to open .ini file: " + e.getMessage());
            }
          }
        });
        return compBtn;
      }
      return null;
    }, this, true);

    BaseLoadingColumn.configureColumn(columnRES, (value, model) -> {
      if (value.getResPath() != null) {
        Button compBtn = new Button();
        compBtn.getStyleClass().add("table-media-button");
        compBtn.setTooltip(new Tooltip("Edit " + value.getResPath()));
        FontIcon cmpIcon = WidgetFactory.createEditIcon(null);
        cmpIcon.setIconSize(22);
        compBtn.setGraphic(cmpIcon);
        compBtn.setOnAction(new EventHandler<ActionEvent>() {
          @Override
          public void handle(ActionEvent event) {
            try {
              GameRepresentation gameRepresentation = value;
              String resPath = gameRepresentation.getResPath();
              Studio.editGameFile(gameRepresentation, resPath);
            }
            catch (Exception e) {
              LOG.error("Failed to open .res file: {}", e.getMessage(), e);
              WidgetFactory.showAlert(Studio.stage, "Error", "Failed to open .res file: " + e.getMessage());
            }
          }
        });
        return compBtn;
      }
      return null;
    }, this, true);

    BaseLoadingColumn.configureColumn(columnAltSound, (value, model) -> {
      boolean hasUpdate = this.showVpsUpdates && vpsSettings.isVpsAltSound() && value.getVpsUpdates().contains(VpsDiffTypes.altSound);
      if (value.isAltSoundAvailable()) {
        if (model.getGame().isIgnoreUpdates()) {
          return WidgetFactory.createCheckAndIgnoredIcon("Updates for this table are ignored.");
        }
        if (hasUpdate) {
          return WidgetFactory.createCheckAndUpdateIcon("A new ALT sound bundle or an update for the existing one is available");
        }
        else {
          return WidgetFactory.createCheckboxIcon(getIconColor(value));
        }
      }
      else if (hasUpdate) {
        return WidgetFactory.createUpdateIcon("A new ALT sound bundle or an update for the existing one is available");
      }
      return null;
    }, this, true);

    BaseLoadingColumn.configureColumn(columnAltColor, (value, model) -> {
      boolean hasUpdate = this.showVpsUpdates && vpsSettings.isVpsAltColor() && value.getVpsUpdates().contains(VpsDiffTypes.altColor);

      if (value.getAltColorType() != null) {
        Label label = new Label(value.getAltColorType().name());
        label.getStyleClass().add("default-text");
        label.setStyle(getLabelCss(value));

        if (hasUpdate) {
          //add update icon
          label.setGraphic(WidgetFactory.createUpdateIcon("A new ALT color or an update for the existing one is available"));
        }
        return label;
      }
      else if (hasUpdate) {
        //We don't have a type, so just show the update.
        return WidgetFactory.createUpdateIcon("A new ALT color or an update for the existing one is available");
      }
      return null;
    }, this, true);

    BaseLoadingColumn.configureColumn(columnPUPPack, (value, model) -> {
      boolean hasUpdate = this.showVpsUpdates && vpsSettings.isVpsPUPPack() && value.getVpsUpdates().contains(VpsDiffTypes.pupPack);
      if (value.getPupPackName() != null) {
        if (model.getGame().isIgnoreUpdates()) {
          return WidgetFactory.createCheckAndIgnoredIcon("Updates for this table are ignored.");
        }
        if (hasUpdate) {
          return WidgetFactory.createCheckAndUpdateIcon("A new PUP pack or an update for the existing one is available");
        }
        else {
          return WidgetFactory.createCheckboxIcon(getIconColor(value), value.getPupPackName());
        }
      }
      else if (hasUpdate) {
        return WidgetFactory.createUpdateIcon("A new PUP pack or an update for the existing one is available");
      }
      return null;
    }, this, true);

    BaseLoadingColumn.configureColumn(columnStatus, (value, model) -> {
      HBox row = new HBox(2);
      row.setAlignment(Pos.CENTER_RIGHT);
      row.setMinWidth(34);

      BackupDescriptorRepresentation backup = client.getBackupService().getBackup(value);
      if (backup != null) {
        Button compBtn = new Button();
        compBtn.getStyleClass().add("table-media-button");
        compBtn.setTooltip(new Tooltip("Show the backup of this table in the backups view."));
        FontIcon cmpIcon = WidgetFactory.createIcon("mdi2a-archive-outline");
        compBtn.setGraphic(cmpIcon);
        row.getChildren().add(compBtn);
        compBtn.setOnAction(event -> {
          Platform.runLater(() -> {
            getTablesController().switchToBackupsTab(backup);
          });
        });
      }

      if (iScoredSettings != null && iScoredSettings.isEnabled() && !value.getCompetitionTypes().isEmpty()) {
        Button compBtn = new Button();
        compBtn.getStyleClass().add("table-media-button");
        compBtn.setTooltip(new Tooltip("This table is competed."));
        FontIcon cmpIcon = WidgetFactory.createIcon("mdi2t-trophy-variant");
        cmpIcon.setIconColor(Paint.valueOf("#00c4ec"));
        compBtn.setGraphic(cmpIcon);
        row.getChildren().add(compBtn);
        compBtn.setOnAction(event -> {
          Platform.runLater(() -> {
            CompetitionType competitionType = value.getCompetitionTypes().get(0);
            if (competitionType.equals(CompetitionType.MANIA)) {
              ManiaSettings maniaSettings = client.getPreferenceService().getJsonPreference(PreferenceNames.MANIA_SETTINGS, ManiaSettings.class);
              if (maniaSettings.isTournamentsEnabled()) {
                NavigationController.navigateTo(NavigationItem.Tournaments, new NavigationOptions(-1));
              }
              else {
                WidgetFactory.showInformation(stage, "Tournaments not enabled!", "You must enable the Mania Tournaments view to navigate there.");
              }
            }
            else {
              NavigationController.navigateTo(NavigationItem.Competitions, new NavigationOptions(competitionType));
            }
          });
        });
      }

      if (!StringUtils.isEmpty(value.getComment())) {
        Button commentBtn = new Button();
        FontIcon icon = WidgetFactory.createIcon("mdi2c-comment");
        icon.setIconSize(16);
        commentBtn.setGraphic(icon);
        commentBtn.getStyleClass().add("table-media-button");
        String notes = value.getComment();
        Tooltip tooltip = new Tooltip(value.getComment());
        tooltip.setWrapText(true);
        commentBtn.setTooltip(tooltip);

        if (notes.toLowerCase().contains("//error")) {
          icon.setIconColor(Paint.valueOf(WidgetFactory.ERROR_COLOR));
        }
        else if (notes.toLowerCase().contains("//todo")) {
          icon.setIconColor(Paint.valueOf(WidgetFactory.TODO_COLOR));
        }
        else if (notes.toLowerCase().contains("//outdated")) {
          icon.setIconColor(Paint.valueOf(WidgetFactory.OUTDATED_COLOR));
        }

        row.getChildren().add(commentBtn);
        commentBtn.setOnAction(event -> {
          tableView.getSelectionModel().clearSelection();
          tableView.getSelectionModel().select(model);
          Platform.runLater(() -> {
            TableDialogs.openCommentDialog(this, value);
          });
        });
      }


      ValidationState validationState = value.getValidationState();
      FontIcon statusIcon = WidgetFactory.createCheckIcon(getIconColor(value));
      Label statusLabel = new Label();
      if (value.getIgnoredValidations() != null && !value.getIgnoredValidations().contains(-1)) {
        if (validationState != null && validationState.getCode() > 0) {
          statusIcon = WidgetFactory.createExclamationIcon(getIconColor(value));
          statusLabel.setTooltip(new Tooltip("This table has configuration issues."));
        }
      }
      statusLabel.setGraphic(statusIcon);
      row.getChildren().add(statusLabel);

      return row;
    }, this, true);

    BaseLoadingColumn.configureColumn(columnPinVol, (value, model) -> {
      Label label = new Label("-");
      PinVolPreferences prefs = client.getPinVolService().getPinVolTablePreferences();
      GameRepresentation game = model.getGame();
      PinVolTableEntry entry = prefs.getTableEntry(game.getGameFileName(), client.getEmulatorService().isVpxGame(game), client.getEmulatorService().isFpGame(game));
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

    BaseLoadingColumn.configureColumn(columnTutorials, (value, model) -> {
      String vpsTableId = value.getExtTableId();
      return new VpsTutorialColumn(vpsTableId);
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
      label.setStyle(getLabelCss(value));
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
        FontIcon icon = WidgetFactory.createIcon("mdi2s-star", getIconColor(value));
        icon.setIconSize(WidgetFactory.DEFAULT_ICON_SIZE + 2);
        label.setGraphic(icon);
        label.setCursor(Cursor.HAND);
        label.setOnMouseClicked(event -> setGameRating(value, (Integer) label.getUserData()));
        root.getChildren().add(label);
        index++;
      }

      for (int i = 0; i < nonRating; i++) {
        Label label = new Label();
        label.setUserData(index);
        FontIcon icon = WidgetFactory.createIcon("mdi2s-star-outline", getIconColor(value));
        label.setGraphic(icon);
        icon.setIconSize(WidgetFactory.DEFAULT_ICON_SIZE + 2);
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
      label.setStyle(getLabelCss(value));
      return label;
    }, this, true);

    BaseLoadingColumn.configureColumn(columnLauncher, (value, model) -> {
      Label label = new Label(model.getGame().getLauncher());
      label.getStyleClass().add("default-text");
      label.setStyle(getLabelCss(value));
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
      label.setStyle(getLabelCss(value));
      return label;
    }, this, true);

    columnPlaylists.setSortable(false);
    BaseLoadingColumn.configureColumn(columnPlaylists, (value, model) -> {
      HBox box = new HBox(3);
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
          Label playlistIcon = WidgetFactory.createPlaylistIcon(match, uiSettings, value.isDisabled());

          Tooltip tooltip = createPlaylistTooltip(match, playlistIcon);
          if (match.getId() >= 0) {
            Button plButton = new Button("", playlistIcon.getGraphic());
            plButton.setTooltip(tooltip);
            plButton.getStyleClass().add("playlist-button");
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

        //This adds overflow text
        Label label = new Label("+" + (matches.size() - count));
        label.setStyle("-fx-font-size: 14px;-fx-font-weight: bold; -fx-padding: 3 0 0 0;");
        label.getStyleClass().add("default-text");

        box.getChildren().add(label);
        break;
      }
      //This is the box that contains the data for each item
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


          row.selectedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
              if (tableView.getSelectionModel().getSelectedItems().isEmpty()) {
                return;
              }
              contextMenuController.refreshContextMenu(tableView, menu, tableView.getSelectionModel().getSelectedItems());
            }
          });

          row.itemProperty().addListener((obs, oldItem, newItem) -> {
            if (newItem == null) {
//              menu.getItems().clear();
            }
            else {
//              contextMenuController.refreshContextMenu(tableView, menu, newItem.getGame());
            }
          });

          row.emptyProperty().addListener((obs, wasEmpty, isNowEmpty) ->
              row.setContextMenu(isNowEmpty ? null : menu));

          return row;
        });

  }

  @NonNull
  public static Tooltip createPlaylistTooltip(PlaylistRepresentation match, Label playlistIcon) {
    Tooltip tooltip = new Tooltip(match.getName());

//    FrontendMediaRepresentation medias = client.getPlaylistMediaService().getPlaylistMediaCached(match.getId());
//    FrontendMediaItemRepresentation mediaItem = medias.getDefaultMediaItem(VPinScreen.Wheel);
//    if (mediaItem != null) {
//      String url = client.getURL(mediaItem.getUri()) + "/" + URLEncoder.encode(mediaItem.getName(), Charset.defaultCharset());
//      InputStream in = client.getCachedUrlImage(url);
//      if (in != null) {
//        Image scaledWheel = new Image(in, 150, 150, false, false);
//        ImageView imageView = new ImageView(scaledWheel);
//        tooltip.setGraphic(imageView);
//
//        Image icon = new Image(client.getCachedUrlImage(url), 24, 24, false, true);
//        playlistIcon.setGraphic(new ImageView(icon));
//      }
//    }
    playlistIcon.setTooltip(tooltip);
    return tooltip;
  }

  private void setGameRating(GameRepresentation game, int rating) {
    try {
      TableDetails tableDetails = client.getFrontendService().getTableDetails(game.getId());
      if (tableDetails != null) {
        tableDetails.setGameRating((rating + 1));
        client.getFrontendService().saveTableDetails(tableDetails, game.getId());
        EventManager.getInstance().notifyTableChange(game.getId(), null, null);
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

  /**
   *
   */
  @Override
  public void refreshView(GameRepresentationModel model) {
    GameRepresentation game = model != null ? model.getGame() : null;

    dismissBtn.setVisible(true);

    setValidationVisible(false);
    validationErrorLabel.setText("");
    validationErrorText.setText("");

    if (assetManagerMode) {
      this.tablesController.getAssetViewSideBarController().setGame(tablesController.getTableOverviewController(), game, assetScreenSelection);
      this.converterBtn.setDisable(getSelections().isEmpty());
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

    refreshEmulators();
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
    uploadsButtonController.setTablesController(tablesController);
    // start the reload process when the stage is on
    Studio.stage.setOnShown(e -> this.doReload());
  }

  public List<GameRepresentation> getGames() {
    return getData();
  }

  @Override
  public void onChanged(Change<? extends GameRepresentationModel> c) {
    boolean disable = c.getList().isEmpty() || c.getList().size() > 1;

    uploadsButtonController.setData(c.getList().stream().map(g -> g.getGame()).collect(Collectors.toList()), this.emulatorCombo.getValue());

    validateBtn.setDisable(c.getList().isEmpty());
    deleteBtn.setDisable(c.getList().isEmpty());
    playButtonController.setDisable(disable);
    scanBtn.setDisable(c.getList().isEmpty());
    exportBtn.setDisable(c.getList().isEmpty());
    assetManagerBtn.setDisable(disable);
    tableEditBtn.setDisable(disable);
    setValidationVisible(c.getList().size() != 1);

    if (c.getList().isEmpty()) {
      refreshView(null);
    }
    else {
      GameRepresentationModel model = c.getList().get(0);
      GameRepresentation gameRepresentation = model.getGame();
      playButtonController.setDisable(gameRepresentation.getGameFilePath() == null);
      playButtonController.setData(gameRepresentation);
      refreshView(model);
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
      GameEmulatorRepresentation gameEmulator = client.getEmulatorService().getGameEmulator(game.getEmulatorId());
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
    ToolbarController.INSTANCE.setTableOverviewController(this);
    super.initialize("game", "games", new TableOverviewColumnSorter(this));

    //manually fix new columns
    BaseTableSettings tableSettings = getTableSettings();
    if (tableSettings != null && !tableSettings.getColumnOrder().contains(columnRating.getId())) {
      tableView.getColumns().remove(columnRating);
      tableView.getColumns().add(tableView.getColumns().indexOf(columnPlaylists), columnRating);
    }
    if (!getTableSettings().getColumnOrder().contains(columnTutorials.getId())) {
      tableView.getColumns().remove(columnTutorials);
      tableView.getColumns().add(tableView.getColumns().indexOf(columnHSType), columnTutorials);
    }

    iScoredSettings = client.getPreferenceService().getJsonPreference(PreferenceNames.ISCORED_SETTINGS, IScoredSettings.class);
    columnStatus.setPrefWidth(iScoredSettings != null && iScoredSettings.isEnabled() ? 75 : 55);


//    validationError.managedProperty().bindBidirectional(validationError.visibleProperty());
    validationButtonGroup.managedProperty().bindBidirectional(validationButtonGroup.visibleProperty());
    importUploadButtonGroup.managedProperty().bindBidirectional(importUploadButtonGroup.visibleProperty());
    playlistManagerBtn.managedProperty().bindBidirectional(playlistManagerBtn.visibleProperty());
    converterBtn.managedProperty().bindBidirectional(converterBtn.visibleProperty());
    converterBtn.setVisible(false);

    status = client.getGameStatusService().getStatus();
    gameEmulatorChangeListener = new GameEmulatorChangeListener();

    contextMenuController = new TableOverviewContextMenu(this);

    deleteSeparator.managedProperty().bindBidirectional(this.deleteSeparator.visibleProperty());

    this.deleteBtn.managedProperty().bindBidirectional(this.deleteBtn.visibleProperty());
    this.scanBtn.managedProperty().bindBidirectional(this.scanBtn.visibleProperty());
    this.stopBtn.managedProperty().bindBidirectional(this.stopBtn.visibleProperty());
    this.assetManagerSeparator.managedProperty().bindBidirectional(assetManagerSeparator.visibleProperty());
    this.assetManagerBtn.managedProperty().bindBidirectional(this.assetManagerBtn.visibleProperty());
    this.assetManagerViewBtn.managedProperty().bindBidirectional(this.assetManagerViewBtn.visibleProperty());
    this.tableEditBtn.managedProperty().bindBidirectional(this.tableEditBtn.visibleProperty());
    this.importSeparator.managedProperty().bindBidirectional(this.importSeparator.visibleProperty());
    this.importBtn.managedProperty().bindBidirectional(this.importBtn.visibleProperty());
    this.exportBtn.managedProperty().bindBidirectional(this.exportBtn.visibleProperty());
    this.secondaryToolbar.managedProperty().bindBidirectional(this.secondaryToolbar.visibleProperty());

    Frontend frontend = client.getFrontendService().getFrontendCached();

    FrontendUtil.replaceName(importBtn.getTooltip(), frontend);
    FrontendUtil.replaceName(exportBtn.getTooltip(), frontend);
    FrontendUtil.replaceName(stopBtn.getTooltip(), frontend);

    playlistManagerBtn.setVisible(Features.PLAYLIST_CRUD);

    if (Features.IS_STANDALONE) {
      importBtn.setVisible(false);
      columnEmulator.setVisible(false);
    }

    if (!Features.MEDIA_ENABLED) {
      this.assetManagerBtn.setVisible(false);
      this.assetManagerViewBtn.setVisible(false);
    }

    super.loadFilterPanel("scene-tables-overview-filter.fxml");

    super.loadPlaylistCombo();

    setValidationVisible(false);

    new TableOverviewDragDropHandler(this, tableView, loaderStack);

    bindTable();

    if (Features.PUPPACKS_ENABLED) {
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

    columnPlaylists.setVisible(Features.PLAYLIST_ENABLED);

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
      playBtn = loader.load();
      playButtonController = loader.getController();
      int i = toolbar.getItems().indexOf(stopBtn);
      toolbar.getItems().add(i, playBtn);
    }
    catch (IOException e) {
      LOG.error("failed to load play button: " + e.getMessage(), e);
    }

    try {
      FXMLLoader loader = new FXMLLoader(UploadsButtonController.class.getResource("uploads-btn.fxml"));
      uploadsButton = loader.load();
      uploadsButtonController = loader.getController();
      importUploadButtonGroup.getChildren().add(1, uploadsButton);
    }
    catch (IOException e) {
      LOG.error("failed to load uploads button: " + e.getMessage(), e);
    }

    if (Features.TABLES_SECONDARY_TOOLBAR) {
      stage.widthProperty().addListener(new ChangeListener<Number>() {
        @Override
        public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
          Platform.runLater(() -> {
            refreshToolbars();
          });
        }
      });
      Platform.runLater(() -> {
        refreshToolbars();
      });
    }

    EventManager.getInstance().addListener(this);
  }

  private void refreshToolbars() {
    double width = stage.getWidth();
    if (width <= 2400) {
      secondaryToolbar.getItems().clear();

      toolbar.getItems().remove(assetManagerSeparator);
      toolbar.getItems().remove(assetManagerViewBtn);
      secondaryToolbar.getItems().add(assetManagerViewBtn);
      toolbar.getItems().remove(assetManagerBtn);
      secondaryToolbar.getItems().add(assetManagerBtn);
      toolbar.getItems().remove(tableEditBtn);
      secondaryToolbar.getItems().add(tableEditBtn);
      toolbar.getItems().remove(converterBtn);
      secondaryToolbar.getItems().add(converterBtn);
      toolbar.getItems().remove(deleteSeparator);
      secondaryToolbar.getItems().add(deleteSeparator);
      toolbar.getItems().remove(uploadsButton);
      secondaryToolbar.getItems().add(uploadsButton);
      toolbar.getItems().remove(playBtn);
      secondaryToolbar.getItems().add(playBtn);
      toolbar.getItems().remove(stopBtn);
      secondaryToolbar.getItems().add(stopBtn);
      toolbar.getItems().remove(importUploadButtonGroup);
      secondaryToolbar.getItems().add(importUploadButtonGroup);
      toolbar.getItems().remove(mappingSeparator);
      secondaryToolbar.getItems().add(mappingSeparator);
      toolbar.getItems().remove(scanBtn);
      secondaryToolbar.getItems().add(scanBtn);
      toolbar.getItems().remove(validateBtn);
      secondaryToolbar.getItems().add(validateBtn);

      secondaryToolbar.setVisible(true);
    }
    else {
      if (!toolbar.getItems().contains(validateBtn)) {
        addToToolbar(toolbar, assetManagerSeparator);
        addToToolbar(toolbar, assetManagerViewBtn);
        addToToolbar(toolbar, assetManagerBtn);
        addToToolbar(toolbar, tableEditBtn);
        addToToolbar(toolbar, converterBtn);
        addToToolbar(toolbar, deleteSeparator);
        addToToolbar(toolbar, uploadsButton);
        addToToolbar(toolbar, playBtn);
        addToToolbar(toolbar, stopBtn);
        addToToolbar(toolbar, importUploadButtonGroup);
        addToToolbar(toolbar, mappingSeparator);
        addToToolbar(toolbar, scanBtn);
        addToToolbar(toolbar, validateBtn);
      }

      secondaryToolbar.setVisible(false);
    }
  }

  private void addToToolbar(ToolBar toolbar, Parent btn) {
    if (!toolbar.getItems().contains(btn)) {
      toolbar.getItems().add(btn);
    }
  }

  private void refreshViewForEmulator() {
    FrontendType frontendType = client.getFrontendService().getFrontendType();
    GameEmulatorRepresentation newValue = emulatorCombo.getValue();
    getTableFilterController().setEmulator(newValue);
    boolean vpxOrFpEmulator = newValue == null || newValue.isVpxEmulator() || newValue.isFpEmulator();
    boolean vpxEmulator = newValue == null || newValue.isVpxEmulator();
    boolean fpEmulator = newValue == null || newValue.isFpEmulator();

    this.exportBtn.setVisible(Features.BACKUPS_ENABLED);
    this.importBtn.setVisible(!frontendType.equals(FrontendType.Standalone));
    this.importSeparator.setVisible(!frontendType.equals(FrontendType.Standalone));
    this.emulatorBtn.setDisable(newValue == null || newValue.getId() == -1);
    this.exportBtn.setDisable(!vpxOrFpEmulator);
    this.deleteBtn.setVisible(vpxOrFpEmulator);
    this.scanBtn.setVisible(vpxEmulator);
//    this.playButtonController.setVisible(vpxOrFpEmulator);
//    this.stopBtn.setVisible(vpxOrFpEmulator);

    this.uploadsButtonController.updateVisibility(vpxOrFpEmulator, vpxEmulator, fpEmulator);

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
    boolean fx1Mode = newValue == null || newValue.getType().equals(EmulatorType.ZenFX);
    boolean fx3Mode = newValue == null || newValue.getType().equals(EmulatorType.ZenFX3);
    boolean pinballMMode = newValue != null && newValue.getType().equals(EmulatorType.PinballM);
    boolean zaccariaMode = newValue == null || newValue.isZaccariaEmulator();

    columnVersion.setVisible((vpxMode || fpMode) && !assetManagerMode && uiSettings.isColumnVersion());
    columnEmulator.setVisible((vpxMode || fpMode) && !assetManagerMode && !Features.IS_STANDALONE && uiSettings.isColumnEmulator());
    columnVPS.setVisible((vpxMode || fpMode || fxMode || zaccariaMode) && !assetManagerMode && uiSettings.isColumnVpsStatus());
    columnPatchVersion.setVisible((vpxMode || fpMode || fxMode) && !assetManagerMode && uiSettings.isColumnPatchVersion());
    columnRom.setVisible(pinballMMode || fx1Mode || (vpxMode && !assetManagerMode && uiSettings.isColumnRom()));
    columnB2S.setVisible((vpxMode || fpMode) && !assetManagerMode && uiSettings.isColumnBackglass());
    columnRating.setVisible((vpxMode || fpMode) && !assetManagerMode && Features.RATINGS && uiSettings.isColumnRating());
    columnPUPPack.setVisible(vpxMode && !assetManagerMode && uiSettings.isColumnPupPack() && Features.PUPPACKS_ENABLED);
    columnPinVol.setVisible(vpxMode && !assetManagerMode && uiSettings.isColumnPinVol());
    columnAltSound.setVisible(vpxMode && !assetManagerMode && uiSettings.isColumnAltSound());
    columnAltColor.setVisible((vpxMode || fx1Mode || fx3Mode) && !assetManagerMode && uiSettings.isColumnAltColor());
    columnPOV.setVisible(vpxMode && !assetManagerMode && uiSettings.isColumnPov());
    columnTutorials.setVisible(vpxMode && !assetManagerMode && uiSettings.isColumnTutorial());
    columnINI.setVisible(vpxMode && !assetManagerMode && uiSettings.isColumnIni());
    columnRES.setVisible(vpxMode && !assetManagerMode && uiSettings.isColumnRes());
    columnHSType.setVisible(vpxMode && !assetManagerMode && uiSettings.isColumnHighscore());
    columnDateAdded.setVisible((vpxMode || fpMode) && !assetManagerMode && uiSettings.isColumnDateAdded());
    columnDateModified.setVisible((vpxMode || fpMode) && !assetManagerMode && uiSettings.isColumnDateModified());
    columnLauncher.setVisible(vpxMode && !assetManagerMode && uiSettings.isColumnLauncher());
    columnComment.setVisible((vpxMode || fpMode || fxMode || zaccariaMode) && !assetManagerMode && uiSettings.isColumnComment());
    columnPlaylists.setVisible((vpxMode || fpMode || fxMode || zaccariaMode) && !assetManagerMode && Features.PLAYLIST_ENABLED && uiSettings.isColumnPlaylists());
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
    else if (key.equals(PreferenceNames.ISCORED_SETTINGS)) {
      iScoredSettings = client.getPreferenceService().getJsonPreference(PreferenceNames.ISCORED_SETTINGS, IScoredSettings.class);
      if (Features.COMPETITIONS_ENABLED) {
        columnStatus.setPrefWidth(iScoredSettings.isEnabled() ? 75 : 55);
      }
    }
    else if (key.equals(PreferenceNames.IGNORED_VALIDATION_SETTINGS)) {
      ignoredValidations = client.getPreferenceService().getJsonPreference(PreferenceNames.IGNORED_VALIDATION_SETTINGS, IgnoredValidationSettings.class);
      refreshViewAssetColumns(assetManagerMode);
    }
  }

  @Override
  public void jobFinished(@NonNull JobFinishedEvent event) {
    JobType jobType = event.getJobType();
    if (jobType.equals(JobType.TABLE_BACKUP)) {
      new Thread(() -> {
        client.getBackupService().invalidateBackupCache();
      }).start();
      EventManager.getInstance().notifyTableChange(event.getGameId(), null);
    }
  }

  @Override
  protected int getPreferredColumnIndex(@NotNull String columnId) {
    return -1;
  }

  public TablesController getTablesController() {
    return tablesController;
  }

  public UploadsButtonController getUploadsButtonController() {
    return uploadsButtonController;
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
    return client.getEmulatorService().isAllVpx(selectedEmu) ? null : selectedEmu;
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
        TableOverviewController.createPlaylistTooltip(item, playlistIcon);
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
      return;
    }
    if (event.getCode() == KeyCode.W && event.isControlDown()) {
      onVpsResetUpdates();
      event.consume();
      return;
    }
    else if (!games.isEmpty() && BulkActions.consume(games, event)) {
      //done
    }
  }
}