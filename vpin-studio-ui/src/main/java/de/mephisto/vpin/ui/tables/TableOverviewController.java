package de.mephisto.vpin.ui.tables;

import de.mephisto.vpin.commons.fx.ConfirmationResult;
import de.mephisto.vpin.commons.fx.Features;
import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.connectors.vps.VPS;
import de.mephisto.vpin.connectors.vps.model.VPSChange;
import de.mephisto.vpin.connectors.vps.model.VpsDiffTypes;
import de.mephisto.vpin.connectors.vps.model.VpsTable;
import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.altsound.AltSound;
import de.mephisto.vpin.restclient.games.FilterSettings;
import de.mephisto.vpin.restclient.games.GameEmulatorRepresentation;
import de.mephisto.vpin.restclient.games.GameMediaItemRepresentation;
import de.mephisto.vpin.restclient.games.GameRepresentation;
import de.mephisto.vpin.restclient.games.descriptors.TableUploadType;
import de.mephisto.vpin.restclient.games.descriptors.UploadDescriptor;
import de.mephisto.vpin.restclient.popper.Playlist;
import de.mephisto.vpin.restclient.popper.PopperScreen;
import de.mephisto.vpin.restclient.preferences.PreferenceChangeListener;
import de.mephisto.vpin.restclient.preferences.ServerSettings;
import de.mephisto.vpin.restclient.preferences.UISettings;
import de.mephisto.vpin.restclient.representations.PreferenceEntryRepresentation;
import de.mephisto.vpin.restclient.validation.*;
import de.mephisto.vpin.ui.*;
import de.mephisto.vpin.ui.events.EventManager;
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
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
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
import javafx.scene.paint.Paint;
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

import static de.mephisto.vpin.ui.Studio.client;
import static de.mephisto.vpin.ui.Studio.stage;

public class TableOverviewController implements Initializable, StudioFXController, ListChangeListener<GameRepresentation>, PreferenceChangeListener {
  private final static Logger LOG = LoggerFactory.getLogger(TableOverviewController.class);

  @FXML
  private Separator deleteSeparator;

  @FXML
  TableColumn<GameRepresentation, Label> columnDisplayName;

  @FXML
  TableColumn<GameRepresentation, Label> columnVersion;

  @FXML
  TableColumn<GameRepresentation, Label> columnEmulator;

  @FXML
  TableColumn<GameRepresentation, String> columnVPS;

  @FXML
  TableColumn<GameRepresentation, Label> columnRom;

  @FXML
  TableColumn<GameRepresentation, String> columnB2S;

  @FXML
  TableColumn<GameRepresentation, String> columnStatus;

  @FXML
  TableColumn<GameRepresentation, String> columnPUPPack;

  @FXML
  TableColumn<GameRepresentation, String> columnAltSound;

  @FXML
  TableColumn<GameRepresentation, String> columnAltColor;

  @FXML
  TableColumn<GameRepresentation, String> columnPOV;

  @FXML
  TableColumn<GameRepresentation, String> columnHSType;

  @FXML
  TableColumn<GameRepresentation, String> columnPlaylists;

  @FXML
  TableColumn<GameRepresentation, String> columnDateAdded;


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
  private Button playBtn;

  @FXML
  private Button stopBtn;

  @FXML
  private Button importBtn;

  @FXML
  private SplitMenuButton uploadTableBtn;

  @FXML
  private Button reloadBtn;

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
  private MenuItem mediaUploadItem;

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
  private ValidationSettings validationSettings;

  private TableFilterController tableFilterController;
  private List<Integer> filteredIds = new ArrayList<>();

  private final List<Consumer> reloadConsumers = new ArrayList<>();

  private boolean assetManagerMode = false;
  private TableOverviewContextMenu contextMenuController;
  private TableOverviewColumnSorter tableOverviewColumnSorter;
  private List<String> ignoredValidations;

  private ObservableList<GameRepresentation> data = FXCollections.emptyObservableList();
  private List<GameRepresentation> games = Collections.emptyList();

  // Add a public no-args constructor
  public TableOverviewController() {
  }

  @FXML
  public void onBackglassManager() {
    TableDialogs.openDirectB2sManagerDialog(tablesController.getTablesSideBarController());
  }

  @FXML
  public void onAssetView() {
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


    boolean vpxMode = emulatorCombo.getValue() == null || emulatorCombo.getValue().isVpxEmulator();

    refreshViewAssetColumns(assetManagerMode);


    columnVersion.setVisible(!assetManagerMode && vpxMode);
    columnEmulator.setVisible(!assetManagerMode);
    columnVPS.setVisible(!assetManagerMode && !uiSettings.isHideVPSUpdates() && vpxMode);
    columnRom.setVisible(!assetManagerMode && vpxMode);
    columnB2S.setVisible(!assetManagerMode && vpxMode);
    columnPUPPack.setVisible(!assetManagerMode && vpxMode);
    columnAltSound.setVisible(!assetManagerMode && vpxMode);
    columnAltColor.setVisible(!assetManagerMode && vpxMode);
    columnPOV.setVisible(!assetManagerMode && vpxMode);
    columnHSType.setVisible(!assetManagerMode && vpxMode);
    columnPlaylists.setVisible(!assetManagerMode);
    columnDateAdded.setVisible(!assetManagerMode);

    GameRepresentation selectedItem = tableView.getSelectionModel().getSelectedItem();
    tableView.getSelectionModel().clearSelection();
    if (selectedItem != null) {
      tableView.getSelectionModel().select(selectedItem);
    }
  }

  private void refreshViewAssetColumns(boolean assetManagerMode) {
    columnPlayfield.setVisible(assetManagerMode && !ignoredValidations.contains(String.valueOf(PopperScreen.PlayField.getValidationCode())));
    columnBackglass.setVisible(assetManagerMode && !ignoredValidations.contains(String.valueOf(PopperScreen.BackGlass.getValidationCode())));
    columnLoading.setVisible(assetManagerMode && !ignoredValidations.contains(String.valueOf(PopperScreen.Loading.getValidationCode())));
    columnWheel.setVisible(assetManagerMode && !ignoredValidations.contains(String.valueOf(PopperScreen.Wheel.getValidationCode())));
    columnDMD.setVisible(assetManagerMode && !ignoredValidations.contains(String.valueOf(PopperScreen.DMD.getValidationCode())));
    columnTopper.setVisible(assetManagerMode && !ignoredValidations.contains(String.valueOf(PopperScreen.Topper.getValidationCode())));
    columnFullDMD.setVisible(assetManagerMode && !ignoredValidations.contains(String.valueOf(PopperScreen.Menu.getValidationCode())));
    columnAudio.setVisible(assetManagerMode && !ignoredValidations.contains(String.valueOf(PopperScreen.Audio.getValidationCode())));
    columnAudioLaunch.setVisible(assetManagerMode && !ignoredValidations.contains(String.valueOf(PopperScreen.AudioLaunch.getValidationCode())));
    columnInfo.setVisible(assetManagerMode && !ignoredValidations.contains(String.valueOf(PopperScreen.GameInfo.getValidationCode())));
    columnHelp.setVisible(assetManagerMode && !ignoredValidations.contains(String.valueOf(PopperScreen.GameHelp.getValidationCode())));
    columnOther2.setVisible(assetManagerMode && !ignoredValidations.contains(String.valueOf(PopperScreen.Other2.getValidationCode())));
  }

  @FXML
  public void onAltSoundUpload() {
    TableDialogs.openAltSoundUploadDialog(null, null);
  }

  @FXML
  public void onAltColorUpload() {
    ObservableList<GameRepresentation> selectedItems = tableView.getSelectionModel().getSelectedItems();
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
    ObservableList<GameRepresentation> selectedItems = tableView.getSelectionModel().getSelectedItems();
    if (selectedItems != null && !selectedItems.isEmpty()) {
      boolean b = TableDialogs.openPupPackUploadDialog(selectedItems.get(0), null, null);
      if (b) {
        tablesController.getTablesSideBarController().getTitledPaneDirectB2s().setExpanded(true);
      }
    }
  }

  @FXML
  public void onBackglassUpload() {
    ObservableList<GameRepresentation> selectedItems = tableView.getSelectionModel().getSelectedItems();
    if (selectedItems != null && !selectedItems.isEmpty()) {
      boolean b = TableDialogs.directBackglassUpload(stage, selectedItems.get(0));
      if (b) {
        tablesController.getTablesSideBarController().getTitledPaneDirectB2s().setExpanded(true);
      }
    }
  }

  @FXML
  public void onIniUpload() {
    ObservableList<GameRepresentation> selectedItems = tableView.getSelectionModel().getSelectedItems();
    if (selectedItems != null && !selectedItems.isEmpty()) {
      TableDialogs.directIniUpload(stage, selectedItems.get(0));
    }
  }

  @FXML
  public void onMediaUpload() {
    ObservableList<GameRepresentation> selectedItems = tableView.getSelectionModel().getSelectedItems();
    if (selectedItems != null && !selectedItems.isEmpty()) {
      TableDialogs.openMediaUploadDialog(selectedItems.get(0), null, null);
    }
  }

  @FXML
  public void onDMDUpload() {
    ObservableList<GameRepresentation> selectedItems = tableView.getSelectionModel().getSelectedItems();
    if (selectedItems != null && !selectedItems.isEmpty()) {
      TableDialogs.openDMDUploadDialog(selectedItems.get(0), null, null);
    }
  }

  @FXML
  public void onPOVUpload() {
    ObservableList<GameRepresentation> selectedItems = tableView.getSelectionModel().getSelectedItems();
    if (selectedItems != null && !selectedItems.isEmpty()) {
      boolean b = TableDialogs.directPovUpload(stage, selectedItems.get(0));
      if (b) {
        tablesController.getTablesSideBarController().getTitledPanePov().setExpanded(true);
      }
    }
  }

  @FXML
  public void onMediaEdit() {
    GameRepresentation selectedItems = tableView.getSelectionModel().getSelectedItem();
    TableDialogs.openTableAssetsDialog(this, selectedItems, PopperScreen.BackGlass);
  }

  @FXML
  public void onVps() {
    GameRepresentation selectedItems = tableView.getSelectionModel().getSelectedItem();
    if (selectedItems != null && !StringUtils.isEmpty(selectedItems.getExtTableVersionId())) {
      Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
      if (desktop != null && desktop.isSupported(Desktop.Action.BROWSE)) {
        try {
          desktop.browse(new URI(VPS.getVpsTableUrl(selectedItems.getExtTableId())));
        }
        catch (Exception e) {
          LOG.error("Failed to open link: " + e.getMessage());
        }
      }
    }
  }

  @FXML
  public void onVpsReset() {
    ObservableList<GameRepresentation> selectedItems = tableView.getSelectionModel().getSelectedItems();
    TableActions.onVpsReset(selectedItems);
  }

  @FXML
  private void onFilter() {
    tableFilterController.toggle();
  }

  @FXML
  public void onTableEdit() {
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
  public void onBackup() {
    ObservableList<GameRepresentation> selectedItems = tableView.getSelectionModel().getSelectedItems();
    TableDialogs.openTablesBackupDialog(selectedItems);
  }

  @FXML
  public void onPlay() {
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
  public void onStop() {
    Optional<ButtonType> result = WidgetFactory.showConfirmation(Studio.stage, "Stop all VPX and PinUP Popper processes?");
    if (result.isPresent() && result.get().equals(ButtonType.OK)) {
      client.getPinUPPopperService().terminatePopper();
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
    if (client.getPinUPPopperService().isPinUPPopperRunning()) {
      if (Dialogs.openPopperRunningWarning(Studio.stage)) {
        openUploadDialog(uploadDescriptor);
      }
      return;
    }

    openUploadDialog(uploadDescriptor);
  }

  private void openUploadDialog(TableUploadType uploadDescriptor) {
    GameRepresentation game = tableView.getSelectionModel().getSelectedItem();
    TableDialogs.openTableUploadDialog(this, game, uploadDescriptor, null);
  }

  public void refreshUploadResult(Optional<UploadDescriptor> uploadResult) {
    if (uploadResult.isPresent() && uploadResult.get().getGameId() != -1) {
      Consumer<GameRepresentation> c = gameRepresentation -> {
        UploadDescriptor tableUploadResult = uploadResult.get();
        Optional<GameRepresentation> match = this.games.stream().filter(g -> g.getId() == tableUploadResult.getGameId()).findFirst();
        if (match.isPresent()) {
          tableView.getSelectionModel().clearSelection();
          tableView.getSelectionModel().select(match.get());
          tableView.scrollTo(tableView.getSelectionModel().getSelectedItem());
          if (uiSettings.isAutoEditTableData()) {
            TableDialogs.openTableDataDialog(this, match.get());
          }
        }
      };
      reloadConsumers.add(c);
      onReload();
    }
  }

  @FXML
  public void onDelete() {
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
  public void onTablesScanAll() {
    boolean scanned = TableDialogs.openScanAllDialog(this.games);
    if (scanned) {
      this.onReload();
    }
  }

  @FXML
  public void onImport() {
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
  public void onValidate() {
    ObservableList<GameRepresentation> selectedItems = tableView.getSelectionModel().getSelectedItems();
    TableDialogs.openValidationDialog(new ArrayList<>(selectedItems), false);
  }

  @FXML
  public void onValidateAll() {
    boolean done = TableDialogs.openValidationDialog(games, true);
    if (done) {
      onReload();
    }
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
  private void onValidationSettings() {
    PreferencesController.open("validators_vpx");
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
      boolean reloadNeeded = onRefresh(tableFilterController.getFilterSettings());
      if (reloadNeeded) {
        return;
      }
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
        if (editorRootStack.getChildren().size() > 1) {
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
    List<Integer> integers = client.getGameService().filterGames(filterSettings);
    if (integers == null || (!this.filteredIds.isEmpty() && integers.equals(this.filteredIds))) {
      return false;
    }

    tableView.setVisible(false);
    setBusy(true);

    new Thread(() -> {
      try {
        Platform.runLater(() -> {
          setFilterIds(integers);
          filterGames(games);
          tableView.setItems(data);
          labelTableCount.setText(data.size() + " tables");

          tableView.refresh();
          setBusy(false);
        });
      }
      catch (Exception e) {
        LOG.error("Error filtering tables: " + e.getMessage());
        WidgetFactory.showAlert(Studio.stage, "Error", "Error filtering tables: " + e.getMessage());
      }
    }).start();
    return true;
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
    this.importBtn.setDisable(true);
    this.stopBtn.setDisable(true);

    setBusy(true);

    new Thread(() -> {

      Platform.runLater(() -> {
        GameRepresentation selection = tableView.getSelectionModel().getSelectedItem();
        games = client.getGameService().getKnownGames();

        filterGames(games);
        tableView.setItems(data);
        labelTableCount.setText(data.size() + " tables");

        tableView.refresh();

        if (selection != null) {
          final Optional<GameRepresentation> updatedGame = this.games.stream().filter(g -> g.getId() == selection.getId()).findFirst();
          if (updatedGame.isPresent()) {
            GameRepresentation gameRepresentation = updatedGame.get();
            tableView.getSelectionModel().select(gameRepresentation);
            this.playBtn.setDisable(!gameRepresentation.isGameFileAvailable());
          }
        }
        else if (!games.isEmpty()) {
          tableView.getSelectionModel().select(0);
        }
        if (!games.isEmpty()) {
          this.validateBtn.setDisable(false);
          this.deleteBtn.setDisable(false);
          this.tableEditBtn.setDisable(false);
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
      labelTableCount.setText(data.size() + " tables");
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
      int iconSize = 14;
      try {
        HBox row = new HBox(3);
        row.setAlignment(Pos.CENTER_LEFT);

        Label label = new Label("-");
        label.getStyleClass().add("default-title");

        VpsTable vpsTable = null;

        label = new Label();
        if (!StringUtils.isEmpty(value.getExtTableId()) && VPS.getInstance().getTableById(value.getExtTableId()) != null) {
          vpsTable = VPS.getInstance().getTableById(value.getExtTableId());
          FontIcon checkboxIcon = WidgetFactory.createCheckboxIcon();
          checkboxIcon.setIconSize(iconSize);
          label.setGraphic(checkboxIcon);
          label.setTooltip(new Tooltip("VPS Table:\n" + vpsTable.getDisplayName()));
          row.getChildren().add(label);
        }
        else {
          label = new Label(" - ");
          label.setStyle("-fx-text-fill: #FFFFFF;");
          label.setTooltip(new Tooltip("No VPS table mapped."));
          row.getChildren().add(label);
        }

        label = new Label(" / ");
        label.setStyle("-fx-text-fill: #FFFFFF;");
        row.getChildren().add(label);

        label = new Label();
        if (!StringUtils.isEmpty(value.getExtTableVersionId()) && vpsTable != null && VPS.getInstance().getTableVersionById(vpsTable, value.getExtTableVersionId()) != null) {
          FontIcon checkboxIcon = WidgetFactory.createCheckboxIcon();
          checkboxIcon.setIconSize(iconSize);
          label.setGraphic(checkboxIcon);
          label.setTooltip(new Tooltip("VPS Table Version:\n" + VPS.getInstance().getTableVersionById(vpsTable, value.getExtTableVersionId()).toString()));
          row.getChildren().add(label);
        }
        else {
          label = new Label(" - ");
          label.setStyle("-fx-text-fill: #FFFFFF;");
          label.setTooltip(new Tooltip("No VPS table version mapped."));
          row.getChildren().add(label);
        }


        label = new Label(" / ");
        label.setStyle("-fx-text-fill: #FFFFFF;");
        row.getChildren().add(label);

        label = new Label();
        if (!value.getVpsUpdates().isEmpty() && !StringUtils.isEmpty(value.getExtTableId())) {
          FontIcon updateIcon = WidgetFactory.createUpdateIcon();
          updateIcon.setIconSize(iconSize + 4);
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

          row.getChildren().add(label);
        }
        else {
          label = new Label(" - ");
          label.setTooltip(new Tooltip("No updates available."));
          label.setStyle("-fx-text-fill: #FFFFFF;");
          row.getChildren().add(label);
        }


        return new SimpleObjectProperty(row);
      }
      catch (Exception e) {
        LOG.error("Failed to render VPS update: " + e.getMessage(), e);
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
      }

      btn.setGraphic(graphics);
      btn.setOnAction(event -> {
        tableView.getSelectionModel().clearSelection();
        tableView.getSelectionModel().select(value);
        Platform.runLater(() -> {
          TableDialogs.openNotesDialog(value);
        });
      });

      return new SimpleObjectProperty(btn);
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
    labelTableCount.setText(data.size() + " tables");
    tableView.setEditable(true);
    tableView.getSelectionModel().getSelectedItems().addListener(this);
    tableView.setSortPolicy(tableView -> tableOverviewColumnSorter.sort(tableView));

    tableView.setRowFactory(
        tableView -> {
          final TableRow<GameRepresentation> row = new TableRow<>();
          final ContextMenu menu = new ContextMenu();

          ListChangeListener<GameRepresentation> changeListener = (ListChangeListener.Change<? extends GameRepresentation> c) ->
              contextMenuController.refreshContextMenu(tableView, menu, this.tableView.getSelectionModel().getSelectedItem());

          row.itemProperty().addListener((obs, oldItem, newItem) -> {
            if (newItem == null) {
              menu.getItems().clear();
            }
            else {
              contextMenuController.refreshContextMenu(tableView, menu, newItem);
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

      for (GameRepresentation game : data) {
        if (game.getGameDisplayName().toLowerCase().startsWith(text.toLowerCase())) {
          tableView.getSelectionModel().clearSelection();
          tableView.getSelectionModel().select(game);
          tableView.scrollTo(tableView.getSelectionModel().getSelectedItem());
          break;
        }
      }
    });
  }

  private Node createAssetStatus(GameRepresentation value, PopperScreen popperScreen) {
    GameMediaItemRepresentation defaultMediaItem = value.getGameMedia().getDefaultMediaItem(popperScreen);
    ValidationProfile defaultProfile = validationSettings.getDefaultProfile();
    ValidationConfig config = defaultProfile.getOrCreateConfig(popperScreen.getValidationCode());
    boolean ignored = value.getIgnoredValidations().contains(popperScreen.getValidationCode());

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
          fontIcon.setIconColor(Paint.valueOf(WidgetFactory.DISABLED_COLOR));
          tt.append("This asset should remain empty.\n");
        }
        else if (config.getOption().equals(ValidatorOption.optional)) {
          fontIcon.setIconColor(Paint.valueOf(WidgetFactory.DISABLED_COLOR));
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
      showAssetDetails(value, popperScreen);
    });
    return btn;
  }

  private void showAssetDetails(GameRepresentation game, PopperScreen popperScreen) {
    tableView.getSelectionModel().clearSelection();
    tableView.getSelectionModel().select(game);
    Platform.runLater(() -> {
      this.tablesController.getAssetViewSideBarController().setGame(tablesController.getTableOverviewController(), game, popperScreen);
    });
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
    new TableOverviewDragDropHandler(tablesController);
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
    dmdUploadItem.setDisable(disable);
    mediaUploadItem.setDisable(disable);
    povItem.setDisable(disable);
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
      GameRepresentation gameRepresentation = c.getList().get(0);
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
    contextMenuController = new TableOverviewContextMenu(this);
    tableOverviewColumnSorter = new TableOverviewColumnSorter(this);

    deleteSeparator.managedProperty().bindBidirectional(this.deleteSeparator.visibleProperty());

    this.importBtn.managedProperty().bindBidirectional(this.importBtn.visibleProperty());
    this.uploadTableBtn.managedProperty().bindBidirectional(this.uploadTableBtn.visibleProperty());
    this.deleteBtn.managedProperty().bindBidirectional(this.deleteBtn.visibleProperty());
    this.scanBtn.managedProperty().bindBidirectional(this.scanBtn.visibleProperty());
    this.playBtn.managedProperty().bindBidirectional(this.playBtn.visibleProperty());
    this.stopBtn.managedProperty().bindBidirectional(this.stopBtn.visibleProperty());

    new Thread(() -> {
      try {
        VPS.getInstance().update();
      }
      catch (Exception e) {
        LOG.error("VPS update failed: " + e.getMessage(), e);
      }
    }).start();

    try {
      FXMLLoader loader = new FXMLLoader(WaitOverlayController.class.getResource("overlay-wait.fxml"));
      tablesLoadingOverlay = loader.load();
      tablesLoadingOverlay.setTranslateY(-100);
      WaitOverlayController ctrl = loader.getController();
      ctrl.setLoadingMessage("Loading Tables...");
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
    playlistCombo.valueProperty().addListener(new ChangeListener<Playlist>() {
      @Override
      public void changed(ObservableValue<? extends Playlist> observableValue, Playlist Playlist, Playlist t1) {
        GameRepresentation selectedItem = tableView.getSelectionModel().getSelectedItem();
        tableView.getSelectionModel().clearSelection();
        filterGames(games);
        tableView.setItems(data);
        labelTableCount.setText(data.size() + " tables");

        if (!data.isEmpty()) {
          if (data.contains(selectedItem)) {
            tableView.getSelectionModel().select(selectedItem);
          }
          else {
            tableView.getSelectionModel().select(0);
          }
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


    Image image3 = new Image(Studio.class.getResourceAsStream("popper-media.png"));
    ImageView iconPopperMedia = new ImageView(image3);
    iconPopperMedia.setFitWidth(18);
    iconPopperMedia.setFitHeight(18);
    assetManagerBtn.setGraphic(iconPopperMedia);

    Image image4 = new Image(Studio.class.getResourceAsStream("popper-edit.png"));
    ImageView iconPopperEdit = new ImageView(image4);
    iconPopperEdit.setFitWidth(18);
    iconPopperEdit.setFitHeight(18);
    tableEditBtn.setGraphic(iconPopperEdit);

    Image image6 = new Image(Studio.class.getResourceAsStream("popper-assets.png"));
    ImageView view6 = new ImageView(image6);
    view6.setFitWidth(18);
    view6.setFitHeight(18);
    assetManagerViewBtn.setGraphic(view6);

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

    assetManagerViewBtn.managedProperty().bindBidirectional(assetManagerViewBtn.visibleProperty());
    assetManagerViewBtn.setVisible(Features.ASSET_MODE);
  }

  private void refreshViewForEmulator() {
    GameEmulatorRepresentation newValue = emulatorCombo.getValue();
    tableFilterController.setEmulator(newValue);
    boolean vpxMode = newValue == null || newValue.isVpxEmulator();

    this.importBtn.setVisible(vpxMode);
    this.uploadTableBtn.setVisible(vpxMode);
    this.deleteBtn.setVisible(vpxMode);
    this.scanBtn.setVisible(vpxMode);
    this.playBtn.setVisible(vpxMode);
    this.stopBtn.setVisible(vpxMode);

    deleteSeparator.setVisible(vpxMode);

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
    else if (key.equals(PreferenceNames.VALIDATION_SETTINGS)) {
      validationSettings = client.getPreferenceService().getJsonPreference(PreferenceNames.VALIDATION_SETTINGS, ValidationSettings.class);
    }
    else if (key.equals(PreferenceNames.IGNORED_VALIDATIONS)) {
      PreferenceEntryRepresentation preference = client.getPreferenceService().getPreference(PreferenceNames.IGNORED_VALIDATIONS);
      ignoredValidations = preference.getCSVValue();
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

  public StackPane getTableStack() {
    return this.tableStack;
  }

  public StackPane getLoaderStack() {
    return loaderStack;
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