package de.mephisto.vpin.ui.tables.dialogs;

import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.connectors.vps.VPS;
import de.mephisto.vpin.connectors.vps.matcher.VpsMatch;
import de.mephisto.vpin.connectors.vps.model.VpsTable;
import de.mephisto.vpin.connectors.vps.model.VpsTableVersion;
import de.mephisto.vpin.connectors.vps.model.VpsUrl;
import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.emulators.GameEmulatorRepresentation;
import de.mephisto.vpin.restclient.frontend.Frontend;
import de.mephisto.vpin.restclient.frontend.TableDetails;
import de.mephisto.vpin.restclient.frontend.VPinScreen;
import de.mephisto.vpin.restclient.games.GameList;
import de.mephisto.vpin.restclient.games.GameListItem;
import de.mephisto.vpin.restclient.games.GameRepresentation;
import de.mephisto.vpin.restclient.highscores.HighscoreFiles;
import de.mephisto.vpin.restclient.preferences.ServerSettings;
import de.mephisto.vpin.restclient.preferences.UISettings;
import de.mephisto.vpin.restclient.tagging.TaggingUtil;
import de.mephisto.vpin.restclient.util.FileUtils;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.events.EventManager;
import de.mephisto.vpin.ui.tables.TableDialogs;
import de.mephisto.vpin.ui.tables.TableOverviewController;
import de.mephisto.vpin.ui.tables.TablesSidebarPlaylistsController;
import de.mephisto.vpin.ui.tables.models.TableStatus;
import de.mephisto.vpin.ui.tables.panels.PinVolSettingsController;
import de.mephisto.vpin.ui.tables.panels.PropperRenamingController;
import de.mephisto.vpin.ui.tables.vps.VpsTableVersionCell;
import de.mephisto.vpin.ui.util.AutoCompleteTextField;
import de.mephisto.vpin.ui.util.AutoCompleteTextFieldChangeListener;
import de.mephisto.vpin.ui.util.binding.BeanBinder;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

import static de.mephisto.vpin.ui.Studio.Features;
import static de.mephisto.vpin.ui.Studio.client;

public class TableDataController extends BasePrevNextController implements AutoCompleteTextFieldChangeListener, ChangeListener<VpsTableVersion> {
  private final static Logger LOG = LoggerFactory.getLogger(TableDataController.class);

  private final static TableStatus STATUS_DISABLED = new TableStatus(0, "InActive (Disabled)");
  private final static TableStatus STATUS_NORMAL = new TableStatus(1, "Visible (Normal)");
  private final static TableStatus STATUS_MATURE = new TableStatus(2, "Visible (Mature/Hidden)");
  private final static TableStatus STATUS_WIP = new TableStatus(3, "Work In Progress");

  public final static List<TableStatus> TABLE_STATUSES_FULL = new ArrayList<>(Arrays.asList(STATUS_DISABLED, STATUS_NORMAL, STATUS_MATURE, STATUS_WIP));
  public final static List<TableStatus> TABLE_STATUSES_MINI = new ArrayList<>(Arrays.asList(STATUS_DISABLED, STATUS_NORMAL));

  public static final int MIN_HEIGHT = 712;
  private static int lastTab = 0;


  @FXML
  private TextField gameName;

  @FXML
  private TextField gameFileName;

  @FXML
  private TextField gameVersion;

  @FXML
  private TextField patchVersion;

  @FXML
  private GridPane patchVersionPanel;

  @FXML
  private ComboBox<String> gameTypeCombo;

  @FXML
  private ComboBox<String> gameTheme;

  @FXML
  private TextField gameDisplayName;

  @FXML
  private TextField gameYear;

  @FXML
  private ComboBox<String> manufacturer;

  @FXML
  private Spinner<Integer> numberOfPlayers;

  @FXML
  private ComboBox<String> category;

  @FXML
  private TextField author;

  @FXML
  private ComboBox<String> launchCustomVar;

  @FXML
  private Spinner<Integer> gameRating;

  @FXML
  private GridPane customizationPane;

  @FXML
  private TextField dof;

  @FXML
  private TextField IPDBNum;

  @FXML
  private TextField altRunMode;

  @FXML
  private TextField url;

  @FXML
  private TextField designedBy;

  @FXML
  private TextArea notes;

  @FXML
  private ComboBox<String> custom2;

  @FXML
  private ComboBox<String> custom3;

  @FXML
  private TextField custom4;

  @FXML
  private TextField custom5;

  @FXML
  private TextField webDbId;

  @FXML
  private TextField webLink;

  @FXML
  private CheckBox modCheckbox;

  @FXML
  private TextField tourneyId;

  @FXML
  private TextArea gNotes;

  @FXML
  private TextArea gDetails;

  @FXML
  private TextArea gLog;

  @FXML
  private TextArea gPlayLog;

  @FXML
  private Slider volumeSlider;

  @FXML
  private ComboBox<TableStatus> statusCombo;

  @FXML
  private ComboBox<String> launcherCombo;

  @FXML
  private TabPane tabPane;

  @FXML
  private Button fixVersionBtn;

  @FXML
  private Button idpdBtn;

  @FXML
  private SplitMenuButton autoFillBtn;

  @FXML
  private Button vpsResetBtn;

  @FXML
  private TextField nameField;

  @FXML
  private Button openVpsTableBtn;

  @FXML
  private Button openVpsTableVersionBtn;

  @FXML
  private Button copyTableVersionBtn;

  @FXML
  private HBox buttonsBar;

  @FXML
  private Button openAssetMgrBtn;

  @FXML
  private Label titleLabel;

  @FXML
  private Label databaseIdLabel;

  @FXML
  private Label hintCustom2;
  @FXML
  private Label hintCustom3;
  @FXML
  private Label hintCustom4;
  @FXML
  private Label hintCustom5;
  @FXML
  private Label hintWebId;


  @FXML
  private Tab commentsTab;
  @FXML
  private Tab playlistsTab;
  @FXML
  private Tab metaDataTab;
  @FXML
  private Tab extrasTab;
  @FXML
  private Tab customizationTab;
  @FXML
  private VBox customizationRoot;

  @FXML
  private Tab scoreDataTab;
  @FXML
  private Tab screensTab;
  @FXML
  private Tab statisticsTab;


  @FXML
  private VBox detailsRoot;

  @FXML
  private ScrollPane detailsScroller;

  @FXML
  private CheckBox autoFillCheckbox;

  @FXML
  private Node vpsPanel;

  @FXML
  private ComboBox<VpsTableVersion> tableVersionsCombo;
  private AutoCompleteTextField autoCompleteNameField;

  private TableOverviewController tableOverviewController;

  private GameRepresentation game;
  private final BeanBinder<TableDetails> tableDetailsBinder = new BeanBinder<>();
  private String initialVpxFileName = null;

  private UISettings uiSettings;
  private ServerSettings serverSettings;

  private Stage stage;
  private TableDataTabStatisticsController tableStatisticsController;
  private TableDataTabScreensController tableScreensController;
  private TableDataTabScoreDataController tableDataTabScoreDataController;
  private TableDataTabCommentsController tableDataTabCommentsController;
  private TablesSidebarPlaylistsController tablesSidebarPlaylistsController;
  private PropperRenamingController propperRenamingController;
  private Pane propertRenamingRoot;
  private PinVolSettingsController pinVolController;

  @FXML
  private void onAssetManager(ActionEvent e) {
    this.onCancelClick(e);
    Platform.runLater(() -> {
      TableDialogs.openTableAssetsDialog(tableOverviewController, this.game, VPinScreen.BackGlass);
    });
  }

  @FXML
  private void onVpsReset() {
    this.tableVersionsCombo.valueProperty().removeListener(this);
    game.setExtTableId(null);
    game.setExtTableVersionId(null);
    game.setExtVersion(null);

    setVpsTableIdValue("");
    setVpsVersionIdValue("");

    autoCompleteNameField.setText("");
    this.tableVersionsCombo.setValue(null);
    refreshVersionsCombo(null);
    propperRenamingController.setVpsTable(null);

    openVpsTableBtn.setDisable(true);
    openVpsTableVersionBtn.setDisable(true);
    copyTableVersionBtn.setDisable(true);
    fixVersionBtn.setDisable(true);

    this.tableVersionsCombo.valueProperty().addListener(this);
  }

  @FXML
  private void onAutoMatch() {
    boolean autofill = this.autoFillCheckbox.isSelected();
    if (autofill) {
      this.tableVersionsCombo.valueProperty().removeListener(this);
    }

    VpsMatch vpsMatch = client.getFrontendService().autoMatch(game.getId(), true, true);
    if (vpsMatch != null) {
      String mappedTableId = vpsMatch.getExtTableId();
      String mappedVersion = vpsMatch.getExtTableVersionId();

      // set the detected version
      if (StringUtils.isEmpty(gameVersion.getText())) {
        gameVersion.setText(vpsMatch.getVersion());
      }

      setVpsTableIdValue(mappedTableId);
      game.setExtTableId(mappedTableId);

      openVpsTableBtn.setDisable(false);

      VpsTable vpsTable = client.getVpsService().getTableById(mappedTableId);
      if (vpsTable != null) {
        propperRenamingController.setVpsTable(vpsTable);
        autoCompleteNameField.setText(vpsTable.getDisplayName());

        VpsTableVersion version = vpsTable.getTableVersionById(mappedVersion);
        if (version != null) {
          setVpsVersionIdValue(mappedVersion);
          game.setExtTableVersionId(mappedVersion);
          game.setExtVersion(version.getVersion());

          propperRenamingController.setVpsTableVersion(version);

          openVpsTableVersionBtn.setDisable(false);
          copyTableVersionBtn.setDisable(false);
          fixVersionBtn.setDisable(StringUtils.equals(gameVersion.getText(), game.getExtVersion()));
        }
        refreshVersionsCombo(vpsTable);
        tableVersionsCombo.setValue(version);
      }

      if (autofill) {
        onAutoFill();
      }
    }

    if (autofill) {
      this.tableVersionsCombo.valueProperty().addListener(this);
    }
  }

  @FXML
  private void onAutoMatchAll() {
    if (tableOverviewController != null) {
      List<GameRepresentation> games = tableOverviewController.getTableView().getItems().stream().map(g -> g.getGame()).collect(Collectors.toList());
      TableDialogs.openAutoMatchAll(games);
    }

  }

  @FXML
  private void onAutoFill() {
    try {
      if (tableDetailsBinder != null) {
        LOG.info("Auto-fill table version");
        String vpsTableId = this.game.getExtTableId();
        String vpsVersionId = this.game.getExtTableVersionId();

        TableDetails tableDetails = tableDetailsBinder.getBean();
        TableDetails td = TableDialogs.openAutoFillSettingsDialog(this.stage, Arrays.asList(this.game), tableDetails, vpsTableId, vpsVersionId);
        if (td != null) {
          tableDataTabCommentsController.setTags(TaggingUtil.getTags(td.getTags()));
          tableDetailsBinder.setBean(td, true);
          setDialogDirty(true);
        }
      }
    }
    catch (Exception e) {
      WidgetFactory.showAlert(Studio.stage, "Error", "Auto-fill failed: " + e.getMessage());
    }
  }

  @FXML
  private void onAutoFillAll() {
    List<GameRepresentation> vpxGamesCached = client.getGameService().getVpxGamesCached();
    TableDialogs.openAutoFillSettingsDialog(Studio.stage, vpxGamesCached, null);
  }

  @FXML
  private void onCopyTableVersion() {
    Clipboard clipboard = Clipboard.getSystemClipboard();
    ClipboardContent content = new ClipboardContent();
    String vpsTableUrl = VPS.getVpsTableUrl(game.getExtTableId(), game.getExtTableVersionId());
    content.putString(vpsTableUrl);
    clipboard.setContent(content);
  }

  @FXML
  private void onVpsTableOpen() {
    Studio.browse(VPS.getVpsTableUrl(game.getExtTableId()));
  }

  @FXML
  private void onVpsTableVersionOpen() {
    VpsTableVersion value = this.tableVersionsCombo.getValue();
    if (value != null) {
      VpsUrl vpsUrl = value.getUrls().get(0);
      Studio.browse(vpsUrl.getUrl());
    }
  }

  @FXML
  private void onVersionFix(ActionEvent e) {
    String gVersion = game.getExtVersion();
    if (StringUtils.isEmpty(gVersion)) {
      VpsTableVersion value = this.tableVersionsCombo.getValue();
      if (value != null) {
        gVersion = value.getVersion();
      }
    }
    gameVersion.setText(gVersion);
    fixVersionBtn.setDisable(true);
  }

  @Override
  protected void openNext() {
    tableOverviewController.selectNextModel();
    GameRepresentation selection = tableOverviewController.getSelection();
    if (selection != null && !selection.equals(this.game)) {
      switchGame(selection);
    }
  }

  @Override
  protected void openPrev() {
    tableOverviewController.selectPreviousModel();
    GameRepresentation selection = tableOverviewController.getSelection();
    if (selection != null && !selection.equals(this.game)) {
      switchGame(selection);
    }
  }

  @Override
  protected void autosave(@NonNull Runnable onSuccess) {
    if (doSave(false)) {
      if (onSuccess != null) onSuccess.run();
    }
  }

  @FXML
  private void onWeblinkProperty() {
    String text = this.webLink.getText();
    if (!StringUtils.isEmpty(text) && text.startsWith("http")) {
      Studio.browse(text);
    }
  }

  @FXML
  private void onUrlProperty() {
    String text = this.url.getText();
    if (!StringUtils.isEmpty(text) && text.startsWith("http")) {
      Studio.browse(text);
    }
  }

  @FXML
  private void onIdpdProperty() {
    String text = this.IPDBNum.getText();
    if (!StringUtils.isEmpty(text) && text.startsWith("http")) {
      Studio.browse(text);
    }
  }

  private String findDuplicate(int emuId, String updated) {
    GameList importableTables = client.getFrontendService().getImportableTables(emuId);
    List<GameListItem> items = importableTables.getItems();
    for (GameListItem item : items) {
      String name = item.getName();
      if (name.equalsIgnoreCase(updated)) {
        return name;
      }
    }

    List<GameRepresentation> gameList = client.getGameService().getGamesByFileName(emuId, updated);
    for (GameRepresentation gameRepresentation : gameList) {
      if (gameRepresentation.getId() != this.game.getId()) {
        return gameRepresentation.getGameFileName();
      }
    }
    return null;
  }

  @FXML
  private void onApplyClick(ActionEvent e) {
    TableDataController.lastTab = tabPane.getSelectionModel().getSelectedIndex();
    doSave(false);
  }

  @FXML
  private void onSaveClick(ActionEvent e) {
    TableDataController.lastTab = tabPane.getSelectionModel().getSelectedIndex();
    doSave(true);
  }

  private synchronized boolean doSave(boolean closeDialog) {
    TableDetails tableDetails = tableDetailsBinder.getBean();
    if (tableDetails != null) {
      String updatedGameFileName = tableDetails.getGameFileName();
      if (client.getEmulatorService().isVpxGame(game) && !updatedGameFileName.toLowerCase().endsWith(".vpx")) {
        updatedGameFileName = updatedGameFileName + ".vpx";
      }
      else if (client.getEmulatorService().isFpGame(game) && !updatedGameFileName.toLowerCase().endsWith(".fpt")) {
        updatedGameFileName = updatedGameFileName + ".fpt";
      }

      if (!updatedGameFileName.trim().equalsIgnoreCase(initialVpxFileName.trim())) {
        String duplicate = findDuplicate(game.getEmulatorId(), updatedGameFileName);
        if (duplicate != null) {
          WidgetFactory.showAlert(stage, "Error", "Another file with the name \"" + duplicate + "\" already exist. Please chooser another name.");
          return false;
        }
      }
    }

    boolean success = true;
    try {
      // do not notify TableChange as it will be done globally once all controllers are saved
      success &= pinVolController.save(false);
      success &= tableScreensController.save();
      success &= tableDataTabCommentsController.save(tableDetails);
      success &= tableDataTabScoreDataController.save();

      if (tableDetails != null) {
        tableDetails = client.getFrontendService().saveTableDetails(tableDetails, game.getId());
      }
      client.getFrontendService().saveVpsMapping(game.getId(), game.getExtTableId(), game.getExtTableVersionId());

      setDialogDirty(false);

      if (closeDialog) {
        stage.close();
      }

      EventManager.getInstance().notifyTableChange(game.getId(), null);

      if (client.getEmulatorService().isVpxGame(game)) {
        tableDataTabScoreDataController.refreshScannedValues();
      }
    }
    catch (Exception ex) {
      success = false;
      LOG.error("Error saving table manifest: " + ex.getMessage(), ex);
      WidgetFactory.showAlert(Studio.stage, "Error", "Error saving table manifest: " + ex.getMessage());
    }
    return success;
  }

  @FXML
  private void onCancelClick(ActionEvent e) {
    TableDataController.lastTab = tabPane.getSelectionModel().getSelectedIndex();
    Stage stage = (Stage) ((Button) e.getSource()).getScene().getWindow();
    stage.close();

    EventManager.getInstance().notifyTableChange(this.game.getId(), null);
  }

  @Override
  public void onDialogCancel() {
    TableDataController.lastTab = tabPane.getSelectionModel().getSelectedIndex();
    //EventManager.getInstance().notifyTableChange(this.game.getId(), null);
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    openAssetMgrBtn.managedProperty().bindBidirectional(openAssetMgrBtn.visibleProperty());
    patchVersionPanel.managedProperty().bindBidirectional(patchVersionPanel.visibleProperty());
    customizationPane.managedProperty().bindBidirectional(customizationPane.visibleProperty());

    try {
      if (!Features.FIELDS_STANDARD) {
        tabPane.getTabs().remove(metaDataTab);
      }

      if (!Features.FIELDS_EXTENDED) {
        tabPane.getTabs().remove(extrasTab);
        customizationPane.setVisible(false);
      }

      if (!Features.MEDIA_ENABLED) {
        buttonsBar.getChildren().remove(openAssetMgrBtn);
      }

      if (!Features.PLAYLIST_ENABLED) {
        tabPane.getTabs().remove(playlistsTab);
      }

      hintCustom2.setVisible(false);
      hintCustom3.setVisible(false);
      hintCustom4.setVisible(false);
      hintCustom5.setVisible(false);
      hintWebId.setVisible(false);

      tableVersionsCombo.setCellFactory(c -> new VpsTableVersionCell());
      tableVersionsCombo.setButtonCell(new VpsTableVersionCell());
      tableVersionsCombo.setVisibleRowCount(5);

      // when a value changes, mark the dialog dirty
      tableDetailsBinder.addListener((bean, key, value) -> {
        setDialogDirty(true);
      });
    }
    catch (Exception e) {
      LOG.error("Failed to initialize table data manager: " + e.getMessage(), e);
    }

    this.serverSettings = client.getPreferenceService().getJsonPreference(PreferenceNames.SERVER_SETTINGS, ServerSettings.class);
    this.uiSettings = client.getPreferenceService().getJsonPreference(PreferenceNames.UI_SETTINGS, UISettings.class);

    boolean patchVersionEnabled = !StringUtils.isEmpty(serverSettings.getMappingPatchVersion());
    patchVersion.setDisable(!patchVersionEnabled);
    patchVersionPanel.setVisible(Features.FIELDS_EXTENDED && patchVersionEnabled);

    List<VpsTable> tables = client.getVpsService().getTables();
    List<String> collect = new ArrayList<>(tables.stream().map(t -> t.getDisplayName()).collect(Collectors.toSet()));
    autoCompleteNameField = new AutoCompleteTextField(this.nameField, this, collect);
  }

  private void loadTabs() {
    try {
      FXMLLoader loader = new FXMLLoader(TableDataTabScreensController.class.getResource("dialog-table-data-tab-screens.fxml"));
      Parent builtInRoot = loader.load();
      tableScreensController = loader.getController();
      screensTab.setContent(builtInRoot);
      tableScreensController.dirtyProperty().addListener((obs, oldValue, newValue) -> {
        if (newValue) {
          setDialogDirty(true);
        }
      });
    }
    catch (IOException e) {
      LOG.error("Failed to load dialog-table-data-tab-screens.fxml: " + e.getMessage(), e);
    }

    try {
      FXMLLoader loader = new FXMLLoader(TableDataTabScoreDataController.class.getResource("dialog-table-data-tab-scoredata.fxml"));
      Parent scoreDataRoot = loader.load();
      tableDataTabScoreDataController = loader.getController();
      tableDataTabScoreDataController.initBindings(this);
      scoreDataTab.setContent(scoreDataRoot);
    }
    catch (IOException e) {
      LOG.error("Failed to load dialog-table-data-tab-scoredata.fxml: " + e.getMessage(), e);
    }

    try {
      FXMLLoader loader = new FXMLLoader(PropperRenamingController.class.getResource("propper-renaming.fxml"));
      propertRenamingRoot = loader.load();
      propperRenamingController = loader.getController();
      detailsRoot.getChildren().add(1, propertRenamingRoot);
      propertRenamingRoot.managedProperty().bind(propertRenamingRoot.visibleProperty());

      propperRenamingController.initBindings(752, uiSettings, gameDisplayName, gameFileName, gameName);
    }
    catch (IOException e) {
      LOG.error("Failed to load propper-renaming.fxml: " + e.getMessage(), e);
    }

    try {
      FXMLLoader loader = new FXMLLoader(TableDataTabCommentsController.class.getResource("dialog-table-data-tab-comments.fxml"));
      Parent commentsRoot = loader.load();
      tableDataTabCommentsController = loader.getController();
      tableDataTabCommentsController.initBindings(this);
      commentsTab.setContent(commentsRoot);
    }
    catch (IOException e) {
      LOG.error("Failed to load dialog-table-data-tab-comments.fxml: " + e.getMessage(), e);
    }

    try {
      if (Features.PLAYLIST_ENABLED) {
        FXMLLoader loader = new FXMLLoader(TablesSidebarPlaylistsController.class.getResource("scene-tables-sidebar-playlists.fxml"));
        Parent playlistsRoot = loader.load();
        tablesSidebarPlaylistsController = loader.getController();
        tablesSidebarPlaylistsController.setTableOverviewController(this.tableOverviewController);
        tablesSidebarPlaylistsController.setDialogMode();
        playlistsTab.setContent(playlistsRoot);
      }

      if (Features.STATISTICS_ENABLED) {
        try {
          FXMLLoader loader = new FXMLLoader(TableDataTabStatisticsController.class.getResource("dialog-table-data-tab-statistics.fxml"));
          Parent builtInRoot = loader.load();
          tableStatisticsController = loader.getController();
          statisticsTab.setContent(builtInRoot);
        }
        catch (IOException e) {
          LOG.error("Failed to load dialog-table-data-tab-statistics.fxml: " + e.getMessage(), e);
        }
      }
      else {
        tabPane.getTabs().remove(statisticsTab);
      }
    }
    catch (IOException e) {
      LOG.error("Failed to load dialog-table-data-tab-comments.fxml: " + e.getMessage(), e);
    }

    try {
      FXMLLoader loader = new FXMLLoader(PinVolSettingsController.class.getResource("pinvol-settings.fxml"));
      Parent builtInRoot = loader.load();
      pinVolController = loader.getController();
      customizationRoot.getChildren().add(builtInRoot);
      pinVolController.dirtyProperty().addListener((obs, oldValue, newValue) -> {
        if (newValue) {
          setDialogDirty(true);
        }
      });
    }
    catch (IOException e) {
      LOG.error("Failed to load pinvol settings panel: " + e.getMessage(), e);
    }
  }

  private void initBindings() {
    Frontend frontend = client.getFrontendService().getFrontendCached();

    autoFillCheckbox.setSelected(uiSettings.isAutoApplyVpsData());
    autoFillCheckbox.selectedProperty().addListener((observable, oldValue, newValue) -> {
      uiSettings.setAutoApplyVpsData(newValue);
      client.getPreferenceService().setJsonPreference(uiSettings);
    });

    //---------------
    // TAB Details

    gameVersion.textProperty().addListener((observable, oldValue, newValue) -> {
      tableDetailsBinder.setProperty("gameVersion", gameVersion.getText());
      fixVersionBtn.setDisable(!StringUtils.isEmpty(game.getExtVersion()) && game.getExtVersion().equals(newValue));
    });
    patchVersion.textProperty().addListener((observable, oldValue, newValue) -> {
      setPatchVersionValue(newValue);
    });

    tableDetailsBinder.bindTextField(gameName, "gameName", (observable, oldValue, newValue) -> {
      if (FileUtils.isValidFilename(newValue)) {
        tableDetailsBinder.setProperty("gameName", newValue);
      }
      else {
        gameName.setText(oldValue);
      }
    });
    gameName.setDisable(Features.IS_STANDALONE);

    tableDetailsBinder.bindTextField(gameFileName, "gameFileName", (observable, oldValue, newValue) -> {
        if (oldValue != null && oldValue.contains("\\") && !newValue.contains("\\")) {
          gameFileName.setText(oldValue);
          return;
        }

        if (oldValue != null && oldValue.contains("\\")) {
          String oldGameFileName = tableDetailsBinder.getProperty("gameFileName", null);
          String oldBase = oldGameFileName != null? oldGameFileName.substring(0, oldGameFileName.indexOf("\\") + 1) : null;
          String newBase = newValue.substring(0, newValue.indexOf("\\") + 1);
          if (!newBase.equals(oldBase)) {
            String name = newValue.substring(newValue.indexOf("\\") + 1);
            gameFileName.setText(oldBase + name);
            return;
          }
        }

        if (FileUtils.isValidFilenameWithPath(newValue)) {
          tableDetailsBinder.setProperty("gameFileName", newValue);
        }
        else {
          gameFileName.setText(oldValue);
        }
      });

    tableDetailsBinder.bindTextField(gameDisplayName, "gameDisplayName");
    gameDisplayName.setDisable(Features.IS_STANDALONE);

    //---------------
    // TAB Meta Data

    List<TableStatus> statuses = TableDataController.supportedStatuses();
    statusCombo.setDisable(statuses.isEmpty());
    statusCombo.setItems(FXCollections.observableList(statuses));
    tableDetailsBinder.bindComboBox(statusCombo, "status", null, status -> status.getValue());

    gameTheme.setItems(FXCollections.observableList(frontend.getFieldLookups().getGameTheme()));
    tableDetailsBinder.bindComboBox(gameTheme, "gameTheme");

    tableDetailsBinder.bindTextField(gameYear, "gameYear", (observable, oldValue, newValue) -> {
      if (newValue == null) {
        tableDetailsBinder.setProperty("gameYear", null);
        return;
      }
      // else
      if (!newValue.matches("\\d*")) {
        newValue = newValue.replaceAll("[^\\d]", "");
        gameYear.setText(newValue);
      }
      if (newValue.length() > 4) {
        newValue = newValue.substring(0, 4);
        gameYear.setText(newValue);
      }

      tableDetailsBinder.setProperty("gameYear", newValue.length() > 0 ? Integer.parseInt(newValue) : 0);
    });

    manufacturer.setItems(FXCollections.observableList(frontend.getFieldLookups().getManufacturer()));
    tableDetailsBinder.bindComboBox(manufacturer, "manufacturer");

    gameTypeCombo.setItems(FXCollections.observableList(frontend.getFieldLookups().getGameType()));
    tableDetailsBinder.bindComboBox(gameTypeCombo, "gameType");

    tableDetailsBinder.bindSpinner(numberOfPlayers, "numberOfPlayers", 0, 4);

    category.setItems(FXCollections.observableList(frontend.getFieldLookups().getCategory()));
    tableDetailsBinder.bindComboBox(category, "category");

    tableDetailsBinder.bindTextField(author, "author");

    tableDetailsBinder.bindSpinner(gameRating, "gameRating", 0, 10);

    tableDetailsBinder.bindTextField(IPDBNum, "IPDBNum");
    idpdBtn.visibleProperty().bind(Bindings.createBooleanBinding(() -> IPDBNum.getText() != null && IPDBNum.getText().startsWith("http"), IPDBNum.textProperty()));

    tableDetailsBinder.bindTextField(url, "url");

    tableDetailsBinder.bindTextField(designedBy, "designedBy");

    tableDetailsBinder.bindTextField(notes, "notes");

    //---------------
    // TAB Customization

    tableDetailsBinder.bindComboBoxList(launcherCombo, "launcherList", true);
    tableDetailsBinder.bindComboBox(launcherCombo, "altLaunchExe");

    tableDetailsBinder.bindTextField(altRunMode, "altRunMode");

    launchCustomVar.setItems(FXCollections.observableList(frontend.getFieldLookups().getCustom1()));
    tableDetailsBinder.bindComboBox(launchCustomVar, "launchCustomVar");

    custom2.setItems(FXCollections.observableList(frontend.getFieldLookups().getCustom2()));
    tableDetailsBinder.bindComboBox(custom2, "custom2");

    custom3.setItems(FXCollections.observableList(frontend.getFieldLookups().getCustom3()));
    tableDetailsBinder.bindComboBox(custom3, "custom3");

    tableDetailsBinder.bindTextField(dof, "dof");

    tableDetailsBinder.bindSlider(volumeSlider, "volume", 
        value -> value != null ? String.valueOf(value.intValue()) : null, 
        value -> value != null? Integer.parseInt((String) value) : 100);

    tableDetailsBinder.bindTextField(custom4, "custom4");
    tableDetailsBinder.bindTextField(custom5, "custom5");
    tableDetailsBinder.bindTextField(webDbId, "webGameId");
    tableDetailsBinder.bindTextField(webLink, "webLink2Url");
    tableDetailsBinder.bindTextField(tourneyId, "tourneyId");
    tableDetailsBinder.bindCheckbox(modCheckbox, "mod");

    tableDetailsBinder.bindTextField(gDetails, "gDetails");
    tableDetailsBinder.bindTextField(gLog, "gLog");
    tableDetailsBinder.bindTextField(gPlayLog, "gPlayLog");
    tableDetailsBinder.bindTextField(gNotes, "gNotes");

  }

  public void setGame(@NonNull Stage stage, @Nullable TableOverviewController overviewController, GameRepresentation game, int tab) {
    this.stage = stage;

    this.tableOverviewController = overviewController;
      nextButton.setVisible(overviewController != null);
      prevButton.setVisible(overviewController != null);
      openAssetMgrBtn.setVisible(overviewController != null);

    loadTabs();
    initBindings();

    this.stage.setOnShowing(new EventHandler<WindowEvent>() {
      @Override
      public void handle(WindowEvent event) {
        BorderPane root = (BorderPane) stage.getScene().getRoot();
        root.setPrefHeight(MIN_HEIGHT);
      }
    });

    // select tab in paraleter or last opened tab if tab < 0
    if (tab >= 0) {
      TableDataController.lastTab = tab;
    }
    tabPane.getSelectionModel().select(TableDataController.lastTab);

    switchGame(game);
  }

  protected void switchGame(GameRepresentation game) {
    try {
      this.game = game;
      TableDetails tableDetails = client.getFrontendService().getTableDetails(game.getId());
      tableDetailsBinder.setBean(tableDetails, true);

      if (client.getEmulatorService().isVpxGame(game) || client.getEmulatorService().isFpGame(game)) {
        autoFillBtn.setVisible(true);
        propertRenamingRoot.setVisible(true);
        if (propperRenamingController != null) {
          String gameFileName = tableDetails != null  ? tableDetails.getGameFileName() : game.getGameFileName();
          propperRenamingController.setGame(gameFileName);
        }
      }
      else {
        autoFillBtn.setVisible(false);
        propertRenamingRoot.setVisible(false);
      }

      if (client.getEmulatorService().isVpxGame(game)) {
        HighscoreFiles highscoreFiles = client.getGameService().getHighscoreFiles(game.getId());
        scoreDataTab.setDisable(false);
        if (tableDataTabScoreDataController != null) {
          tableDataTabScoreDataController.setGame(game, tableDetails, highscoreFiles, serverSettings);
        }
      }
      else {
        scoreDataTab.setDisable(true);
      }

      this.titleLabel.setText(game.getGameDisplayName());
      databaseIdLabel.setText("(ID: " + game.getId() + ")  ");

      this.initialVpxFileName = game.getGameFileName();

      this.fixVersionBtn.setDisable(!game.isUpdateAvailable() && !StringUtils.isEmpty(game.getVersion()));

      gameVersion.setText(game.getVersion());

      patchVersion.setText(game.getPatchVersion());
      setPatchVersionValue(game.getPatchVersion());

      //---------------------------------------------------------
      boolean isPopper15 = tableDetails != null && tableDetails.isPopper15();
      custom4.setDisable(!isPopper15);
      custom5.setDisable(!isPopper15);
      webDbId.setDisable(!isPopper15);
      webLink.setDisable(!isPopper15);
      tourneyId.setDisable(!isPopper15);
      modCheckbox.setDisable(!isPopper15);

      extrasTab.setDisable(!isPopper15);

      if (tableDetails != null) {
        gameFileName.setDisable(!client.getEmulatorService().isVpxGame(game) && !client.getEmulatorService().isFpGame(game));
      }
      else {
        gameFileName.setDisable(StringUtils.contains(game.getGameFileName(), "/") || StringUtils.contains(game.getGameFileName(), "\\") || !client.getEmulatorService().isVpxGame(game));
      }

      boolean hasNoDetail = tableDetails == null;

      gameName.setDisable(hasNoDetail);
      gameDisplayName.setDisable(hasNoDetail);
      autoFillCheckbox.setDisable(hasNoDetail);

      if (hasNoDetail) {
        gameName.setText(game.getGameName());
        gameFileName.setText(game.getGameFileName());
        gameDisplayName.setText(game.getGameDisplayName());
      }

      metaDataTab.setDisable(hasNoDetail);
      customizationTab.setDisable(hasNoDetail);
      extrasTab.setDisable(hasNoDetail);
      screensTab.setDisable(hasNoDetail);

      initVpsStatus();
      if (Features.STATISTICS_ENABLED && tableStatisticsController != null) {
        tableStatisticsController.setGame(stage, game, tableDetails);
      }

      if (tableScreensController != null) {
        tableScreensController.setGame(game, tableDetails);
      }

      if (pinVolController != null) {
        pinVolController.setData(stage, Arrays.asList(game), false);
      }

      if (tableDataTabCommentsController != null) {
        tableDataTabCommentsController.setGame(game);
      }

      if (tablesSidebarPlaylistsController != null) {
        tablesSidebarPlaylistsController.setGames(Arrays.asList(game));
      }

      setDialogDirty(false);
    }
    catch (Exception e) {
      LOG.error("Failed to initialize Table Data Manager: {}", e.getMessage(), e);
      WidgetFactory.showAlert(Studio.stage, "Error", "Failed to initialize Table Data Manager: " + e.getMessage(), "Please report this bug via Discord or github.");
    }
  }


  private void setVpsTableIdValue(String value) {
    setMappedFieldValue(serverSettings.getMappingVpsTableId(), value);
  }

  private void setVpsVersionIdValue(String value) {
    setMappedFieldValue(serverSettings.getMappingVpsTableVersionId(), value);
  }

  public void setHsFilenameValue(String value) {
    setMappedFieldValue(serverSettings.getMappingHsFileName(), value);
  }

  public void setPatchVersionValue(String value) {
    setMappedFieldValue(serverSettings.getMappingPatchVersion(), value);
  }

  private void setMappedFieldValue(String field, String value) {
    if (field == null) {
      return;
    }

    switch (field) {
      case "WEBGameID": {
        webDbId.setText(value);
        webDbId.setDisable(true);
        webDbId.setTooltip(new Tooltip("This field has been reserved for VPin Studio data."));
        hintWebId.setVisible(true);
        break;
      }
      case "CUSTOM2": {
        custom2.setValue(value);
        custom2.setDisable(true);
        custom2.setTooltip(new Tooltip("This field has been reserved for VPin Studio data."));
        hintCustom2.setVisible(true);
        break;
      }
      case "CUSTOM3": {
        custom3.setValue(value);
        custom3.setDisable(true);
        custom3.setTooltip(new Tooltip("This field has been reserved for VPin Studio data."));
        hintCustom3.setVisible(true);
        break;
      }
      case "CUSTOM4": {
        custom4.setText(value);
        custom4.setDisable(true);
        custom4.setTooltip(new Tooltip("This field has been reserved for VPin Studio data."));
        hintCustom4.setVisible(true);
        break;
      }
      case "CUSTOM5": {
        custom5.setText(value);
        custom5.setDisable(true);
        custom5.setTooltip(new Tooltip("This field has been reserved for VPin Studio data."));
        hintCustom5.setVisible(true);
        break;
      }
    }
  }

  // do not expose tableDetailsBinder directly
  public void setTableDetailProperty(String property, Object value) {
    tableDetailsBinder.setProperty(property, value);
  }

  // Use scarefully, in readonly as modifications are not monitored 
  public TableDetails getTableDetails() {
    return tableDetailsBinder.getBean();
  }


  private void initVpsStatus() {
    openVpsTableBtn.setDisable(StringUtils.isEmpty(game.getExtTableId()));
    openVpsTableVersionBtn.setDisable(StringUtils.isEmpty(game.getExtTableVersionId()));
    copyTableVersionBtn.setDisable(StringUtils.isEmpty(game.getExtTableVersionId()));

    String vpsTableId = game.getExtTableId();
    String vpsTableVersionId = game.getExtTableVersionId();

    VpsTable tableById = null;
    if (!StringUtils.isEmpty(vpsTableId)) {
      tableById = client.getVpsService().getTableById(vpsTableId);
      if (tableById != null) {
        autoCompleteNameField.setText(tableById.getDisplayName());
        if (propperRenamingController != null) {
          propperRenamingController.setVpsTable(tableById);
        }
      }
    }

    tableVersionsCombo.valueProperty().removeListener(this);

    refreshVersionsCombo(tableById);

    if (tableById != null && !StringUtils.isEmpty(vpsTableVersionId)) {
      VpsTableVersion tableVersion = tableById.getTableVersionById(vpsTableVersionId);
      if (tableVersion != null) {
        tableVersionsCombo.setValue(tableVersion);
        propperRenamingController.setVpsTableVersion(tableVersion);
      }

      openVpsTableVersionBtn.setDisable(tableVersion == null);
      copyTableVersionBtn.setDisable(tableVersion == null);
      fixVersionBtn.setDisable(tableVersion == null || StringUtils.equals(tableVersion.getVersion(), gameVersion.getText()));
    }

    tableVersionsCombo.valueProperty().addListener(this);
    setVpsTableIdValue(vpsTableId);
    setVpsVersionIdValue(vpsTableVersionId);
  }

  private void refreshVersionsCombo(VpsTable tableById) {
    tableVersionsCombo.setItems(FXCollections.emptyObservableList());
    if (tableById != null) {
      GameEmulatorRepresentation emulatorRepresentation = client.getEmulatorService().getGameEmulator(game.getEmulatorId());
      String[] tableFormat = emulatorRepresentation.getVpsEmulatorFeatures();
      List<VpsTableVersion> tableFiles = new ArrayList<>(tableById.getTableFilesForFormat(tableFormat));

      if (!tableFiles.isEmpty()) {
        tableFiles.add(0, null);
        tableVersionsCombo.setItems(FXCollections.observableList(tableFiles));
      }
    }
  }

  /**
   * Version selection change
   */
  @Override
  public void changed(ObservableValue<? extends VpsTableVersion> observable, VpsTableVersion oldValue, VpsTableVersion newValue) {
    String mappedVersionId = newValue != null ? newValue.getId() : null;
    String mappedVersion = newValue != null ? newValue.getVersion() : null;

    setVpsVersionIdValue(mappedVersionId);
    game.setExtTableVersionId(mappedVersionId);
    game.setExtVersion(mappedVersion);

    propperRenamingController.setVpsTableVersion(newValue);

    boolean autofill = this.autoFillCheckbox.isSelected();
    if (autofill) {
      this.onAutoFill();
    }

    openVpsTableVersionBtn.setDisable(newValue == null || newValue.getUrls().isEmpty());
    copyTableVersionBtn.setDisable(newValue == null);
    fixVersionBtn.setDisable(mappedVersion == null || StringUtils.equals(gameVersion.getText(), game.getExtVersion()));
  }

  /**
   * Change listener for the vps table name
   *
   * @param value the text field value
   */
  @Override
  public void onChange(String value) {
    this.tableVersionsCombo.valueProperty().removeListener(this);
    List<VpsTable> tables = client.getVpsService().getTables();
    Optional<VpsTable> selectedEntry = tables.stream().filter(t -> t.getDisplayName().equalsIgnoreCase(value)).findFirst();
    if (selectedEntry.isPresent()) {
      VpsTable vpsTable = selectedEntry.get();

      setVpsTableIdValue(vpsTable.getId());
      game.setExtTableId(vpsTable.getId());

      propperRenamingController.setVpsTable(vpsTable);
    }

    openVpsTableVersionBtn.setDisable(true);
    copyTableVersionBtn.setDisable(true);

    refreshVersionsCombo(selectedEntry.orElseGet(null));
    this.tableVersionsCombo.valueProperty().addListener(this);
  }

  public static List<TableStatus> supportedStatuses() {
    return Features.STATUS_EXTENDED ? TABLE_STATUSES_FULL : Features.STATUSES ? TABLE_STATUSES_MINI : Collections.emptyList();
  }
}
