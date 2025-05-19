package de.mephisto.vpin.ui.tables.dialogs;

import de.mephisto.vpin.commons.fx.DialogController;
import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.connectors.vps.VPS;
import de.mephisto.vpin.connectors.vps.matcher.VpsMatch;
import de.mephisto.vpin.connectors.vps.model.VpsTable;
import de.mephisto.vpin.connectors.vps.model.VpsTableVersion;
import de.mephisto.vpin.connectors.vps.model.VpsUrl;
import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.emulators.GameEmulatorRepresentation;
import de.mephisto.vpin.restclient.frontend.Frontend;
import de.mephisto.vpin.restclient.frontend.FrontendType;
import de.mephisto.vpin.restclient.frontend.TableDetails;
import de.mephisto.vpin.restclient.frontend.VPinScreen;
import de.mephisto.vpin.restclient.games.*;
import de.mephisto.vpin.restclient.highscores.HighscoreFiles;
import de.mephisto.vpin.restclient.preferences.ServerSettings;
import de.mephisto.vpin.restclient.preferences.UISettings;
import de.mephisto.vpin.restclient.system.ScoringDB;
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
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
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

import static de.mephisto.vpin.ui.Studio.client;

public class TableDataController implements Initializable, DialogController, AutoCompleteTextFieldChangeListener, ChangeListener<VpsTableVersion> {
  private final static Logger LOG = LoggerFactory.getLogger(TableDataController.class);

  private final static TableStatus STATUS_DISABLED = new TableStatus(0, "InActive (Disabled)");
  private final static TableStatus STATUS_NORMAL = new TableStatus(1, "Visible (Normal)");
  private final static TableStatus STATUS_MATURE = new TableStatus(2, "Visible (Mature/Hidden)");
  private final static TableStatus STATUS_WIP = new TableStatus(3, "Work In Progress");

  public final static List<TableStatus> TABLE_STATUSES_FULL = new ArrayList<>(Arrays.asList(STATUS_DISABLED, STATUS_NORMAL, STATUS_MATURE, STATUS_WIP));
  public final static List<TableStatus> TABLE_STATUSES_MINI = new ArrayList<>(Arrays.asList(STATUS_DISABLED, STATUS_NORMAL));

  public static final int MIN_HEIGHT = 712;
  public static int lastTab = 0;


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
  private TextField tags;

  @FXML
  private ComboBox<String> category;

  @FXML
  private TextField author;

  @FXML
  private ComboBox<String> launchCustomVar;

  @FXML
  private Spinner<Integer> gameRating;

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
  private Button prevButton;

  @FXML
  private Button nextButton;

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
  private TableDetails tableDetails;
  private String initialVpxFileName = null;

  private UISettings uiSettings;
  private ServerSettings serverSettings;

  private Scene scene;
  private Stage stage;
  private ScoringDB scoringDB;
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
    TableDialogs.openAutoMatchAll();
  }

  @FXML
  private void onAutoFill() {
    try {
      if (tableDetails != null) {
        LOG.info("Auto-fill table version");
        String vpsTableId = this.game.getExtTableId();
        String vpsVersionId = this.game.getExtTableVersionId();

        TableDetails td = TableDialogs.openAutoFillSettingsDialog(this.stage, Arrays.asList(this.game), tableDetails, vpsTableId, vpsVersionId);
        if (td != null) {
          refreshTableDetails(td);
        }
      }
    }
    catch (Exception e) {
      WidgetFactory.showAlert(Studio.stage, "Error", "Auto-fill failed: " + e.getMessage());
    }
  }

  private void refreshTableDetails(TableDetails td) {
    gameTypeCombo.setValue(td.getGameType());
    gameTheme.setValue(td.getGameTheme());
    gameYear.setText("" + td.getGameYear());
    manufacturer.setValue(td.getManufacturer());
    author.setText(td.getAuthor());
    category.setValue(td.getCategory());
    if (td.getNumberOfPlayers() != null) {
      numberOfPlayers.getValueFactory().setValue(td.getNumberOfPlayers());
    }
    if (td.getGameRating() != null) {
      gameRating.getValueFactory().setValue(td.getGameRating());
    }
    IPDBNum.setText(td.getIPDBNum());
    url.setText(td.getUrl());
    designedBy.setText(td.getDesignedBy());
    tags.setText(td.getTags());
    notes.setText(td.getNotes());
    gameVersion.setText(td.getGameVersion());
    gNotes.setText(td.getgNotes());
    gDetails.setText(td.getgDetails());
    gLog.setText(td.getgLog());
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

  @FXML
  private void onNext(ActionEvent e) {
    tableOverviewController.selectNext();
    GameRepresentation selection = tableOverviewController.getSelection();
    if (selection != null && !selection.equals(this.game)) {
      TableDataController.lastTab = this.tabPane.getSelectionModel().getSelectedIndex();
      Platform.runLater(() -> {
        stage.close();
      });

      Platform.runLater(() -> {
        TableDialogs.openTableDataDialog(this.tableOverviewController, selection, TableDataController.lastTab);
      });
    }
  }

  @FXML
  private void onPrevious(ActionEvent e) {
    tableOverviewController.selectPrevious();
    GameRepresentation selection = tableOverviewController.getSelection();
    if (selection != null && !selection.equals(this.game)) {
      int index = this.tabPane.getSelectionModel().getSelectedIndex();
      Platform.runLater(() -> {
        stage.close();
      });

      Platform.runLater(() -> {
        TableDialogs.openTableDataDialog(this.tableOverviewController, selection, index);
      });
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
    Stage stage = (Stage) ((Button) e.getSource()).getScene().getWindow();
    doSave(stage, false);
  }

  @FXML
  private void onSaveClick(ActionEvent e) {
    TableDataController.lastTab = tabPane.getSelectionModel().getSelectedIndex();
    Stage stage = (Stage) ((Button) e.getSource()).getScene().getWindow();
    doSave(stage, true);
  }

  private synchronized void doSave(Stage stage, boolean closeDialog) {
    if (tableDetails != null) {
      String updatedGameFileName = tableDetails.getGameFileName();
      if (game.isVpxGame() && !updatedGameFileName.toLowerCase().endsWith(".vpx")) {
        updatedGameFileName = updatedGameFileName + ".vpx";
      }
      else if (game.isFpGame() && !updatedGameFileName.toLowerCase().endsWith(".fpt")) {
        updatedGameFileName = updatedGameFileName + ".fpt";
      }

      if (!updatedGameFileName.trim().equalsIgnoreCase(initialVpxFileName.trim())) {
        String duplicate = findDuplicate(game.getEmulatorId(), updatedGameFileName);
        if (duplicate != null) {
          WidgetFactory.showAlert(stage, "Error", "Another file with the name \"" + duplicate + "\" already exist. Please chooser another name.");
          return;
        }
      }
    }

    pinVolController.save();
    tableScreensController.save();
    tableDataTabCommentsController.save();
    tableDataTabScoreDataController.save();

    try {
      if (closeDialog) {
        stage.close();
      }

      if (tableDetails != null) {
        tableDetails.setGameVersion(gameVersion.getText());
        tableDetails = client.getFrontendService().saveTableDetails(this.tableDetails, game.getId());
      }
      client.getFrontendService().vpsLink(game.getId(), game.getExtTableId(), game.getExtTableVersionId());

      EventManager.getInstance().notifyTableChange(game.getId(), null);

      if (game.isVpxGame()) {
        tableDataTabScoreDataController.refreshScannedValues();
      }
    }
    catch (Exception ex) {
      LOG.error("Error saving table manifest: " + ex.getMessage(), ex);
      WidgetFactory.showAlert(Studio.stage, "Error", "Error saving table manifest: " + ex.getMessage());
    }
  }

  @FXML
  private void onCancelClick(ActionEvent e) {
    TableDataController.lastTab = tabPane.getSelectionModel().getSelectedIndex();
    Stage stage = (Stage) ((Button) e.getSource()).getScene().getWindow();
    stage.close();

    EventManager.getInstance().notifyTableChange(this.game.getId(), null);
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    openAssetMgrBtn.managedProperty().bindBidirectional(openAssetMgrBtn.visibleProperty());
    patchVersionPanel.managedProperty().bindBidirectional(patchVersionPanel.visibleProperty());
    patchVersionPanel.managedProperty().bindBidirectional(patchVersionPanel.visibleProperty());

    FrontendType frontendType = null;
    try {
      frontendType = client.getFrontendService().getFrontendType();

      if (!frontendType.supportStandardFields()) {
        tabPane.getTabs().remove(metaDataTab);
      }

      if (!frontendType.supportExtendedFields()) {
        tabPane.getTabs().remove(customizationTab);
        tabPane.getTabs().remove(extrasTab);
      }

      if (!frontendType.supportMedias()) {
        buttonsBar.getChildren().remove(openAssetMgrBtn);
      }

      if (!frontendType.supportPlaylists()) {
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
    }
    catch (Exception e) {
      LOG.error("Failed to initialize table data manager: " + e.getMessage(), e);
    }

    if (frontendType != null && frontendType.supportStatistics()) {
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

  private void loadTabs() {
    FrontendType frontendType = client.getFrontendService().getFrontendType();

    try {
      FXMLLoader loader = new FXMLLoader(TableDataTabScreensController.class.getResource("dialog-table-data-tab-screens.fxml"));
      Parent builtInRoot = loader.load();
      tableScreensController = loader.getController();
      screensTab.setContent(builtInRoot);
    }
    catch (IOException e) {
      LOG.error("Failed to load dialog-table-data-tab-screens.fxml: " + e.getMessage(), e);
    }

    try {
      FXMLLoader loader = new FXMLLoader(TableDataTabScoreDataController.class.getResource("dialog-table-data-tab-scoredata.fxml"));
      Parent scoreDataRoot = loader.load();
      tableDataTabScoreDataController = loader.getController();
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
    }
    catch (IOException e) {
      LOG.error("Failed to load propper-renaming.fxml: " + e.getMessage(), e);
    }

    try {
      FXMLLoader loader = new FXMLLoader(TableDataTabCommentsController.class.getResource("dialog-table-data-tab-comments.fxml"));
      Parent commentsRoot = loader.load();
      tableDataTabCommentsController = loader.getController();
      commentsTab.setContent(commentsRoot);
    }
    catch (IOException e) {
      LOG.error("Failed to load dialog-table-data-tab-comments.fxml: " + e.getMessage(), e);
    }

    try {
      if (frontendType.supportPlaylists()) {
        FXMLLoader loader = new FXMLLoader(TablesSidebarPlaylistsController.class.getResource("scene-tables-sidebar-playlists.fxml"));
        Parent playlistsRoot = loader.load();
        tablesSidebarPlaylistsController = loader.getController();
        tablesSidebarPlaylistsController.setTableOverviewController(this.tableOverviewController);
        tablesSidebarPlaylistsController.setDialogMode();
        playlistsTab.setContent(playlistsRoot);
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
    }
    catch (IOException e) {
      LOG.error("Failed to load pinvol settings panel: " + e.getMessage(), e);
    }
  }

  @Override
  public void onDialogCancel() {
    TableDataController.lastTab = tabPane.getSelectionModel().getSelectedIndex();
    EventManager.getInstance().notifyTableChange(this.game.getId(), null);
  }

  public void setGame(@NonNull Stage stage, @Nullable TableOverviewController overviewController, GameRepresentation game, int tab) {
    try {
      this.stage = stage;
      this.game = game;
      this.tableOverviewController = overviewController;

      this.serverSettings = client.getPreferenceService().getJsonPreference(PreferenceNames.SERVER_SETTINGS, ServerSettings.class);
      this.uiSettings = client.getPreferenceService().getJsonPreference(PreferenceNames.UI_SETTINGS, UISettings.class);
      scoringDB = client.getSystemService().getScoringDatabase();
      tableDetails = client.getFrontendService().getTableDetails(game.getId());

      loadTabs();

      boolean patchVersionEnabled = !StringUtils.isEmpty(serverSettings.getMappingPatchVersion());
      patchVersion.setDisable(!patchVersionEnabled);
      patchVersionPanel.setVisible(client.getFrontendService().getFrontendType().supportExtendedFields() && patchVersionEnabled);

      nextButton.setVisible(overviewController != null);
      prevButton.setVisible(overviewController != null);
      openAssetMgrBtn.setVisible(overviewController != null);

      FrontendType frontendType = client.getFrontendService().getFrontendType();
      Frontend frontend = client.getFrontendService().getFrontendCached();


      if (game.isVpxGame() || game.isFpGame()) {
        if (propperRenamingController != null) {
          propperRenamingController.setData(752, tableDetails, uiSettings, gameDisplayName, gameFileName, gameName);
        }
      }
      else {
        autoFillBtn.setVisible(false);
        detailsRoot.getChildren().remove(propertRenamingRoot);
      }

      if (game.isVpxGame()) {
        HighscoreFiles highscoreFiles = client.getGameService().getHighscoreFiles(game.getId());
        if (tableDataTabScoreDataController != null) {
          tableDataTabScoreDataController.setGame(this, game, tableDetails, highscoreFiles, serverSettings);
        }
      }
      else {
        this.tabPane.getTabs().remove(scoreDataTab);
      }


      this.scene = stage.getScene();

      this.titleLabel.setText(game.getGameDisplayName());
      databaseIdLabel.setText("(ID: " + game.getId() + ")  ");

      this.stage.setOnShowing(new EventHandler<WindowEvent>() {
        @Override
        public void handle(WindowEvent event) {
          BorderPane root = (BorderPane) stage.getScene().getRoot();
          root.setPrefHeight(MIN_HEIGHT);
        }
      });

      scene.addEventHandler(KeyEvent.KEY_PRESSED, t -> {
        if (t.getCode() == KeyCode.PAGE_UP) {
          onPrevious(null);
        }
        if (t.getCode() == KeyCode.PAGE_DOWN) {
          onNext(null);
        }
      });


      TableDataController.lastTab = tab;
      tabPane.getSelectionModel().select(tab);
      this.initialVpxFileName = game.getGameFileName();

      this.fixVersionBtn.setDisable(!game.isUpdateAvailable() && !StringUtils.isEmpty(game.getVersion()));
      gameVersion.setText(game.getVersion());
      gameVersion.textProperty().addListener((observable, oldValue, newValue) -> {
        fixVersionBtn.setDisable(!StringUtils.isEmpty(game.getExtVersion()) && newValue.equals(game.getExtVersion()));
      });

      patchVersion.setText(game.getPatchVersion());
      setPatchVersionValue(game.getPatchVersion());
      patchVersion.textProperty().addListener((observable, oldValue, newValue) -> {
        setPatchVersionValue(newValue);
      });

      //---------------------------------------------------------
      if (tableDetails != null) {

        //---------------
        // TAB Details

        autoFillCheckbox.setSelected(uiSettings.isAutoApplyVpsData());
        autoFillCheckbox.selectedProperty().addListener((observable, oldValue, newValue) -> {
          uiSettings.setAutoApplyVpsData(newValue);
          client.getPreferenceService().setJsonPreference(uiSettings);
        });

        gameName.setText(tableDetails.getGameName());
        gameName.textProperty().addListener((observable, oldValue, newValue) -> {
          if (FileUtils.isValidFilename(newValue)) {
            tableDetails.setGameName(newValue);
          }
          else {
            gameName.setText(oldValue);
          }
        });
        gameName.setDisable(frontendType.isStandalone());

        if (game.isVpxGame() || game.isFpGame()) {
          gameFileName.setText(tableDetails.getGameFileName());
          gameFileName.setDisable(isGameFileNameDisabled());
          gameFileName.textProperty().addListener((observable, oldValue, newValue) -> {
            if (oldValue.contains("\\") && !newValue.contains("\\")) {
              newValue = oldValue;
              gameFileName.setText(newValue);
              return;
            }

            if (oldValue.contains("\\")) {
              String oldBase = tableDetails.getGameFileName().substring(0, tableDetails.getGameFileName().indexOf("\\") + 1);
              String newBase = newValue.substring(0, newValue.indexOf("\\") + 1);
              if (!newBase.equals(oldBase)) {
                String name = newValue.substring(newValue.indexOf("\\") + 1);
                gameFileName.setText(oldBase + name);
                return;
              }
            }


            if (FileUtils.isValidFilenameWithPath(newValue)) {
              tableDetails.setGameFileName(newValue);
            }
            else {
              gameFileName.setText(oldValue);
            }
          });
        }
        else {
          gameFileName.setDisable(true);
        }

        gameDisplayName.setText(tableDetails.getGameDisplayName());
        gameDisplayName.textProperty().addListener((observable, oldValue, newValue) -> tableDetails.setGameDisplayName(newValue.trim()));
        gameDisplayName.setDisable(frontendType.isStandalone());

        //---------------
        // TAB Meta Data

        List<TableStatus> statuses = TableDataController.supportedStatuses(frontendType);
        statusCombo.setDisable(statuses.isEmpty());
        statusCombo.setItems(FXCollections.observableList(statuses));
        if (tableDetails.getStatus() >= 0 && tableDetails.getStatus() <= 3 && !statuses.isEmpty()) {
          TableStatus tableStatus = statuses.get(tableDetails.getStatus());
          statusCombo.setValue(tableStatus);
        }
        statusCombo.valueProperty().addListener((observableValue, tableStatus, t1) -> {
          this.tableDetails.setStatus(t1.getValue());
        });

        gameTheme.setItems(FXCollections.observableList(frontend.getFieldLookups().getGameTheme()));
        gameTheme.setValue(tableDetails.getGameTheme());
        gameTheme.valueProperty().addListener((observable, oldValue, newValue) -> tableDetails.setGameTheme(newValue));

        gameYear.textProperty().addListener((observable, oldValue, newValue) -> {
          if (!newValue.matches("\\d*")) {
            gameYear.setText(newValue.replaceAll("[^\\d]", ""));
          }

          if (gameYear.getText().length() > 4) {
            String s = gameYear.getText().substring(0, 4);
            gameYear.setText(s);
          }
        });
        if (tableDetails.getGameYear() != null && tableDetails.getGameYear() > 0) {
          gameYear.setText(String.valueOf(tableDetails.getGameYear()));
        }
        gameYear.textProperty().addListener((observable, oldValue, newValue) -> {
          if (newValue.length() > 0) {
            tableDetails.setGameYear(Integer.parseInt(newValue));
          }
          else {
            tableDetails.setGameYear(0);
          }
        });

        manufacturer.setItems(FXCollections.observableList(frontend.getFieldLookups().getManufacturer()));
        manufacturer.setValue(tableDetails.getManufacturer());
        manufacturer.valueProperty().addListener((observable, oldValue, newValue) -> tableDetails.setManufacturer(newValue));

        gameTypeCombo.setItems(FXCollections.observableList(frontend.getFieldLookups().getGameType()));
        gameTypeCombo.valueProperty().setValue(tableDetails.getGameType());
        gameTypeCombo.valueProperty().addListener((observableValue, gameType, t1) -> tableDetails.setGameType(t1));

        SpinnerValueFactory.IntegerSpinnerValueFactory factory = new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 4, 0);
        numberOfPlayers.setValueFactory(factory);
        if (tableDetails.getNumberOfPlayers() != null) {
          numberOfPlayers.getValueFactory().setValue(tableDetails.getNumberOfPlayers());
        }
        numberOfPlayers.getValueFactory().valueProperty().addListener((observable, oldValue, newValue) -> tableDetails.setNumberOfPlayers(Integer.parseInt(String.valueOf(newValue))));

        tags.setText(tableDetails.getTags());
        tags.textProperty().addListener((observable, oldValue, newValue) -> tableDetails.setTags(newValue));

        category.setItems(FXCollections.observableList(frontend.getFieldLookups().getCategory()));
        category.setValue(tableDetails.getCategory());
        category.valueProperty().addListener((observable, oldValue, newValue) -> tableDetails.setCategory(newValue));

        author.setText(tableDetails.getAuthor());
        author.textProperty().addListener((observable, oldValue, newValue) -> tableDetails.setAuthor(newValue));

        SpinnerValueFactory.IntegerSpinnerValueFactory ratingFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 10, 0);
        gameRating.setValueFactory(ratingFactory);
        if (tableDetails.getGameRating() != null) {
          gameRating.getValueFactory().setValue(tableDetails.getGameRating());
        }
        gameRating.getValueFactory().valueProperty().addListener((observable, oldValue, newValue) -> tableDetails.setGameRating(Integer.parseInt(String.valueOf(newValue))));

        IPDBNum.setText(tableDetails.getIPDBNum());
        idpdBtn.setVisible(tableDetails.getIPDBNum() != null && tableDetails.getIPDBNum().startsWith("http"));
        IPDBNum.textProperty().addListener((observable, oldValue, newValue) -> {
          tableDetails.setIPDBNum(newValue);
          idpdBtn.setVisible(tableDetails.getIPDBNum() != null && tableDetails.getIPDBNum().startsWith("http"));
        });

        url.setText(tableDetails.getUrl());
        url.textProperty().addListener((observable, oldValue, newValue) -> tableDetails.setUrl(newValue));

        designedBy.setText(tableDetails.getDesignedBy());
        designedBy.textProperty().addListener((observable, oldValue, newValue) -> tableDetails.setDesignedBy(newValue));

        notes.setText(tableDetails.getNotes());
        notes.textProperty().addListener((observable, oldValue, newValue) -> tableDetails.setNotes(newValue));

        //---------------
        // TAB Customization

        List<String> launcherList = new ArrayList<>(tableDetails.getLauncherList());
        launcherList.add(0, null);
        launcherCombo.setItems(FXCollections.observableList(launcherList));
        launcherCombo.setValue(tableDetails.getAltLaunchExe());
        launcherCombo.valueProperty().addListener((observableValue, s, t1) -> {
          this.tableDetails.setAltLaunchExe(t1);
        });

        altRunMode.setText(tableDetails.getAltRunMode());
        altRunMode.textProperty().addListener((observable, oldValue, newValue) -> tableDetails.setAltRunMode(newValue));

        launchCustomVar.setItems(FXCollections.observableList(frontend.getFieldLookups().getCustom1()));
        launchCustomVar.setValue(tableDetails.getLaunchCustomVar());
        launchCustomVar.valueProperty().addListener((observable, oldValue, newValue) -> tableDetails.setLaunchCustomVar(newValue));

        custom2.setItems(FXCollections.observableList(frontend.getFieldLookups().getCustom2()));
        custom2.setValue(tableDetails.getCustom2());
        custom2.valueProperty().addListener((observable, oldValue, newValue) -> tableDetails.setCustom2(newValue));

        custom3.setItems(FXCollections.observableList(frontend.getFieldLookups().getCustom3()));
        custom3.setValue(tableDetails.getCustom3());
        custom3.valueProperty().addListener((observable, oldValue, newValue) -> tableDetails.setCustom3(newValue));

        dof.setText(tableDetails.getDof());
        dof.textProperty().addListener((observable, oldValue, newValue) -> tableDetails.setDof(newValue));

        if (tableDetails.getVolume() != null) {
          try {
            volumeSlider.setValue(Integer.parseInt(tableDetails.getVolume()));
          }
          catch (NumberFormatException e) {
            LOG.error("Failed to set valume: " + e.getMessage());
          }
        }
        else {
          volumeSlider.setValue(100);
        }
        volumeSlider.valueProperty().addListener((observableValue, number, t1) -> tableDetails.setVolume(String.valueOf(t1.intValue())));

        if (tableDetails.isPopper15()) {
          custom4.setText(tableDetails.getCustom4());
          custom4.textProperty().addListener((observable, oldValue, newValue) -> tableDetails.setCustom4(newValue));

          custom5.setText(tableDetails.getCustom5());
          custom5.textProperty().addListener((observable, oldValue, newValue) -> tableDetails.setCustom5(newValue));

          webDbId.setText(tableDetails.getWebGameId());
          webDbId.textProperty().addListener((observable, oldValue, newValue) -> tableDetails.setWebGameId(newValue));

          webLink.setText(tableDetails.getWebLink2Url());
          webLink.textProperty().addListener((observable, oldValue, newValue) -> tableDetails.setWebLink2Url(newValue));

          tourneyId.setText(tableDetails.getTourneyId());
          tourneyId.textProperty().addListener((observable, oldValue, newValue) -> tableDetails.setTourneyId(newValue));

          modCheckbox.setSelected(tableDetails.isMod());
          modCheckbox.selectedProperty().addListener((observableValue, aBoolean, t1) -> tableDetails.setMod(t1));
        }
        else {
          custom4.setDisable(true);
          custom5.setDisable(true);
          webDbId.setDisable(true);
          webLink.setDisable(true);
          tourneyId.setDisable(true);
          modCheckbox.setDisable(true);
        }

        //---------------
        // TAB extras
        if (tableDetails.isPopper15()) {
          gDetails.setText(tableDetails.getgDetails());
          gDetails.textProperty().addListener((observableValue, oldValue, newValue) -> tableDetails.setgDetails(newValue));

          gLog.setText(tableDetails.getgLog());
          gLog.textProperty().addListener((observableValue, oldValue, newValue) -> tableDetails.setgLog(newValue));

          gPlayLog.setText(tableDetails.getgPlayLog());
          gPlayLog.textProperty().addListener((observableValue, oldValue, newValue) -> tableDetails.setgPlayLog(newValue));

          gNotes.setText(tableDetails.getgNotes());
          gNotes.textProperty().addListener((observableValue, oldValue, newValue) -> tableDetails.setgNotes(newValue));
        }
        else {
          extrasTab.setDisable(true);
        }
      }
      else {

        this.fixVersionBtn.setDisable(!game.isUpdateAvailable());

        //---------------
        // TAB Details

        autoFillCheckbox.setDisable(true);

        gameName.setText(game.getGameName());
        gameName.setDisable(true);

        gameFileName.setText(game.getGameFileName());
        gameFileName.setDisable(StringUtils.contains(game.getGameFileName(), "/") || StringUtils.contains(game.getGameFileName(), "\\") || !game.isVpxGame());

        gameDisplayName.setText(game.getGameDisplayName());
        gameDisplayName.setDisable(true);

        //---------------
        // TAB Meta Data

        metaDataTab.setDisable(true);
        customizationTab.setDisable(true);
        extrasTab.setDisable(true);
        screensTab.setDisable(true);
      }

      initVpsStatus();
      if (frontendType.supportStatistics() && tableStatisticsController != null) {
        tableStatisticsController.setGame(stage, game, tableDetails);
      }

      if (tableScreensController != null) {
        tableScreensController.setGame(game, tableDetails);
      }

      tabPane.getSelectionModel().select(tab);

      if (pinVolController != null) {
        pinVolController.setData(stage, Arrays.asList(game), false);
      }

      if (tableDataTabCommentsController != null) {
        tableDataTabCommentsController.setGame(game);
      }

      if (tablesSidebarPlaylistsController != null) {
        tablesSidebarPlaylistsController.setGames(Arrays.asList(game));
      }
    }
    catch (Exception e) {
      LOG.error("Failed to initialize Table Data Manager: {}", e.getMessage(), e);
      WidgetFactory.showAlert(Studio.stage, "Error", "Failed to initialize Table Data Manager: " + e.getMessage(), "Please report this bug via Discord or github.");
    }
  }

  private boolean isGameFileNameDisabled() {
    return false; //StringUtils.contains(tableDetails.getGameFileName(), "/") || StringUtils.contains(tableDetails.getGameFileName(), "\\");
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

  private void initVpsStatus() {
    openVpsTableBtn.setDisable(StringUtils.isEmpty(game.getExtTableId()));
    openVpsTableVersionBtn.setDisable(StringUtils.isEmpty(game.getExtTableVersionId()));
    copyTableVersionBtn.setDisable(StringUtils.isEmpty(game.getExtTableVersionId()));
    List<VpsTable> tables = client.getVpsService().getTables();
    TreeSet<String> collect = new TreeSet<>(tables.stream().map(t -> t.getDisplayName()).collect(Collectors.toSet()));
    autoCompleteNameField = new AutoCompleteTextField(null, this.nameField, this, collect);

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
    if (tableById != null) {
      GameEmulatorRepresentation emulatorRepresentation = client.getEmulatorService().getGameEmulator(game.getEmulatorId());
      String[] tableFormat = emulatorRepresentation.getVpsEmulatorFeatures();
      List<VpsTableVersion> tableFiles = new ArrayList<>(tableById.getTableFilesForFormat(tableFormat));

      if (!tableFiles.isEmpty()) {
        tableVersionsCombo.setItems(FXCollections.emptyObservableList());
        tableFiles.add(0, null);
        tableVersionsCombo.setItems(FXCollections.observableList(tableFiles));
      }
      else {
        tableVersionsCombo.setItems(FXCollections.emptyObservableList());
      }
    }
    else {
      tableVersionsCombo.setItems(FXCollections.emptyObservableList());
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

  public static List<TableStatus> supportedStatuses(FrontendType frontendType) {
    return frontendType.supportExtendedStatuses() ? TABLE_STATUSES_FULL : frontendType.supportStatuses() ? TABLE_STATUSES_MINI : Collections.emptyList();
  }
}
