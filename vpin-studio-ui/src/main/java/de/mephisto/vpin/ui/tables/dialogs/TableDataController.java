package de.mephisto.vpin.ui.tables.dialogs;

import de.mephisto.vpin.commons.fx.DialogController;
import de.mephisto.vpin.commons.utils.FileUtils;
import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.connectors.vps.VPS;
import de.mephisto.vpin.connectors.vps.model.VpsFeatures;
import de.mephisto.vpin.connectors.vps.model.VpsTable;
import de.mephisto.vpin.connectors.vps.model.VpsTableVersion;
import de.mephisto.vpin.connectors.vps.model.VpsUrl;
import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.games.GameDetailsRepresentation;
import de.mephisto.vpin.restclient.games.GameList;
import de.mephisto.vpin.restclient.games.GameListItem;
import de.mephisto.vpin.restclient.games.GameRepresentation;
import de.mephisto.vpin.restclient.highscores.HighscoreFiles;
import de.mephisto.vpin.restclient.popper.GameType;
import de.mephisto.vpin.restclient.popper.PopperScreen;
import de.mephisto.vpin.restclient.popper.TableDetails;
import de.mephisto.vpin.restclient.preferences.ServerSettings;
import de.mephisto.vpin.restclient.preferences.UISettings;
import de.mephisto.vpin.restclient.system.ScoringDB;
import de.mephisto.vpin.restclient.vpx.TableInfo;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.events.EventManager;
import de.mephisto.vpin.ui.tables.TableDialogs;
import de.mephisto.vpin.ui.tables.TableOverviewController;
import de.mephisto.vpin.ui.tables.models.TableStatus;
import de.mephisto.vpin.ui.tables.panels.PropperRenamingController;
import de.mephisto.vpin.ui.tables.vps.VpsTableVersionCell;
import de.mephisto.vpin.ui.util.AutoCompleteTextField;
import de.mephisto.vpin.ui.util.AutoCompleteTextFieldChangeListener;
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
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.*;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;

import static de.mephisto.vpin.ui.Studio.client;

public class TableDataController implements Initializable, DialogController, AutoCompleteTextFieldChangeListener, ChangeListener<VpsTableVersion> {
  private final static Logger LOG = LoggerFactory.getLogger(TableDataController.class);

  private final static TableStatus STATUS_DISABLED = new TableStatus(0, "InActive (Disabled)");
  private final static TableStatus STATUS_NORMAL = new TableStatus(1, "Visible (Normal)");
  private final static TableStatus STATUS_MATURE = new TableStatus(2, "Visible (Mature/Hidden)");
  private final static TableStatus STATUS_WIP = new TableStatus(3, "Work In Progress");

  private final static List<TableStatus> TABLE_STATUSES = new ArrayList<>(Arrays.asList(STATUS_DISABLED, STATUS_NORMAL, STATUS_MATURE));
  public final static List<TableStatus> TABLE_STATUSES_15 = new ArrayList<>(Arrays.asList(STATUS_DISABLED, STATUS_NORMAL, STATUS_MATURE, STATUS_WIP));
  public static int lastTab = 0;


  @FXML
  private TextField gameName;

  @FXML
  private TextField gameFileName;

  @FXML
  private TextField gameVersion;

  @FXML
  private ComboBox<GameType> gameTypeCombo;

  @FXML
  private TextField gameTheme;

  @FXML
  private TextField gameDisplayName;

  @FXML
  private TextField gameYear;

  @FXML
  private TextField manufacturer;

  @FXML
  private Spinner<Integer> numberOfPlayers;

  @FXML
  private TextField tags;

  @FXML
  private TextField category;

  @FXML
  private TextField author;

  @FXML
  private TextField launchCustomVar;

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
  private TextField custom2;

  @FXML
  private TextField custom3;

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
  private Tab extrasTab;

  @FXML
  private Slider volumeSlider;

  @FXML
  private Tab screensTab;

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
  private Button copyTableBtn;

  @FXML
  private Button copyTableVersionBtn;

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
  private Tab statisticsTab;

  @FXML
  private Tab scoreDataTab;

  @FXML
  private VBox detailsRoot;

  @FXML
  private CheckBox autoFillCheckbox;

  @FXML
  private Node vpsPanel;

  @FXML
  private ComboBox<VpsTableVersion> tableVersionsCombo;
  private AutoCompleteTextField autoCompleteNameField;

  private TableOverviewController overviewController;

  private GameRepresentation game;

  private TableDetails tableDetails;
  private String initialVpxFileName = null;

  private UISettings uiSettings;
  private ServerSettings serverSettings;

  private Scene scene;
  private Stage stage;
  private ScoringDB scoringDB;
  private HighscoreFiles highscoreFiles;
  private TableDataTabStatisticsController tableStatisticsController;
  private TableDataTabScreensController tableScreensController;
  private TableDataTabScoreDataController tableDataTabScoreDataController;
  private PropperRenamingController propperRenamingController;
  private Pane propertRenamingRoot;

  @FXML
  private void onAssetManager(ActionEvent e) {
    this.onCancelClick(e);
    Platform.runLater(() -> {
      TableDialogs.openTableAssetsDialog(overviewController, this.game, PopperScreen.BackGlass);
    });
  }

  @FXML
  private void onVpsReset() {
    tableDetails.setMappedValue(serverSettings.getMappingVpsTableVersionId(), null);
    tableDetails.setMappedValue(serverSettings.getMappingVpsTableId(), null);

    String vpsTableMappingField = serverSettings.getMappingVpsTableId();
    String vpsTableVersionMappingField = serverSettings.getMappingVpsTableVersionId();

    setMappedFieldValue(vpsTableMappingField, "");
    setMappedFieldValue(vpsTableVersionMappingField, "");

    autoCompleteNameField.setText("");
    refreshVersionsCombo(null);
    propperRenamingController.setVpsTable(null);
  }

  @FXML
  private void onAutoMatch() {
    String rom = tableDetails.getRomName();
    GameDetailsRepresentation gameDetails = tableDataTabScoreDataController.getGameDetails();
    if (StringUtils.isEmpty(rom) && !StringUtils.isEmpty(gameDetails.getRomName())) {
      rom = gameDetails.getRomName();
    }

    boolean autofill = this.autoFillCheckbox.isSelected();
    TableDetails updatedTableDetails = client.getPinUPPopperService().autoMatch(game.getId(), autofill);
    if (updatedTableDetails != null) {

      String vpsTableMappingField = serverSettings.getMappingVpsTableId();
      String vpsTableVersionMappingField = serverSettings.getMappingVpsTableVersionId();

      String mappedTableId = updatedTableDetails.getMappedValue(vpsTableMappingField);
      String mappedVersion = updatedTableDetails.getMappedValue(vpsTableVersionMappingField);

      setMappedFieldValue(vpsTableMappingField, mappedTableId);
      tableDetails.setMappedValue(vpsTableMappingField, mappedTableId);

      openVpsTableBtn.setDisable(false);
      copyTableBtn.setDisable(false);

      VpsTable vpsTable = client.getVpsService().getTableById(mappedTableId);
      if (vpsTable != null) {
        propperRenamingController.setVpsTable(vpsTable);
        autoCompleteNameField.setText(vpsTable.getDisplayName());

        VpsTableVersion version = vpsTable.getTableVersionById(mappedVersion);
        if (version != null) {
          setMappedFieldValue(vpsTableVersionMappingField, mappedVersion);
          tableDetails.setMappedValue(vpsTableVersionMappingField, mappedVersion);
          propperRenamingController.setVpsTableVersion(version);
        }
        refreshVersionsCombo(vpsTable);
        tableVersionsCombo.setValue(version);
      }


      openVpsTableVersionBtn.setDisable(false);
      copyTableVersionBtn.setDisable(false);
    }
  }

  @FXML
  private void onAutoMatchAll() {
    TableDialogs.openAutoMatchAll();
  }

  @FXML
  private void onAutoFill() {
    try {
      LOG.info("Auto-fill table version " + tableDetails.getMappedValue(serverSettings.getMappingVpsTableVersionId()));
      TableDetails td = client.getPinUPPopperService().autoFillTableDetails(game.getId(), tableDetails);
      if (td != null) {
        gameTypeCombo.setValue(td.getGameType());
        gameTheme.setText(td.getGameTheme());
        gameYear.setText("" + td.getGameYear());
        manufacturer.setText(td.getManufacturer());
        author.setText(td.getAuthor());
        category.setText(td.getCategory());
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
    }
    catch (Exception e) {
      WidgetFactory.showAlert(Studio.stage, "Error", "Auto-fill failed: " + e.getMessage());
    }
  }

  @FXML
  private void onAutoFillAll() {
    TableDialogs.openAutoFillAll();
  }


  @FXML
  private void onCopyTableId() {
    Clipboard clipboard = Clipboard.getSystemClipboard();
    ClipboardContent content = new ClipboardContent();
    String mappingVpsTableId = serverSettings.getMappingVpsTableId();
    String vpsTableUrl = VPS.getVpsTableUrl(tableDetails.getMappedValue(mappingVpsTableId));
    content.putString(vpsTableUrl);
    clipboard.setContent(content);
  }

  @FXML
  private void onCopyTableVersion() {
    Clipboard clipboard = Clipboard.getSystemClipboard();
    ClipboardContent content = new ClipboardContent();
    String mappingVpsTableId = serverSettings.getMappingVpsTableId();
    String mappingVpsTableVersionId = serverSettings.getMappingVpsTableVersionId();
    String vpsTableUrl = VPS.getVpsTableUrl(tableDetails.getMappedValue(mappingVpsTableId) + "#" + tableDetails.getMappedValue(mappingVpsTableVersionId));
    content.putString(vpsTableUrl);
    clipboard.setContent(content);
  }

  @FXML
  private void onVpsTableOpen() {
    Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
    if (desktop != null && desktop.isSupported(Desktop.Action.BROWSE)) {
      try {
        String mappingVpsTableId = serverSettings.getMappingVpsTableId();
        String mappedValue = tableDetails.getMappedValue(mappingVpsTableId);
        if (!StringUtils.isEmpty(mappedValue)) {
          desktop.browse(new URI(VPS.getVpsTableUrl(mappedValue)));
        }
      }
      catch (Exception e) {
        LOG.error("Failed to open link: " + e.getMessage());
      }
    }
  }

  @FXML
  private void onVpsTableVersionOpen() {
    Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
    if (desktop != null && desktop.isSupported(Desktop.Action.BROWSE)) {
      try {
        VpsTableVersion value = this.tableVersionsCombo.getValue();
        if (value != null) {
          VpsUrl vpsUrl = value.getUrls().get(0);
          desktop.browse(new URI(vpsUrl.getUrl()));
        }
      }
      catch (Exception e) {
        LOG.error("Failed to open link: " + e.getMessage());
      }
    }
  }

  @FXML
  private void onVersionFix(ActionEvent e) {
    TableDetails td = client.getPinUPPopperService().getTableDetails(game.getId());
    String gVersion = game.getExtVersion();
    if (StringUtils.isEmpty(gVersion)) {
      VpsTableVersion value = this.tableVersionsCombo.getValue();
      if (value != null) {
        gVersion = value.getVersion();
      }
    }
    td.setGameVersion(gVersion);
    gameVersion.setText(gVersion);
    fixVersionBtn.setDisable(true);
  }

  @FXML
  private void onNext(ActionEvent e) {
    overviewController.selectNext();
    GameRepresentation selection = overviewController.getSelection();
    if (selection != null && !selection.equals(this.game)) {
      TableDataController.lastTab = this.tabPane.getSelectionModel().getSelectedIndex();
      Platform.runLater(() -> {
        stage.close();
      });

      Platform.runLater(() -> {
        TableDialogs.openTableDataDialog(this.overviewController, selection, TableDataController.lastTab);
      });
    }
  }

  @FXML
  private void onPrevious(ActionEvent e) {
    overviewController.selectPrevious();
    GameRepresentation selection = overviewController.getSelection();
    if (selection != null && !selection.equals(this.game)) {
      int index = this.tabPane.getSelectionModel().getSelectedIndex();
      Platform.runLater(() -> {
        stage.close();
      });

      Platform.runLater(() -> {
        TableDialogs.openTableDataDialog(this.overviewController, selection, index);
      });
    }
  }

  @FXML
  private void onWeblinkProperty() {
    String text = this.webLink.getText();
    if (!StringUtils.isEmpty(text) && text.startsWith("http")) {
      Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
      if (desktop != null && desktop.isSupported(Desktop.Action.BROWSE)) {
        try {
          desktop.browse(new URI(text));
        }
        catch (Exception e) {
          LOG.error("Failed to open link: " + e.getMessage());
        }
      }
    }
  }

  @FXML
  private void onUrlProperty() {
    String text = this.url.getText();
    if (!StringUtils.isEmpty(text) && text.startsWith("http")) {
      Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
      if (desktop != null && desktop.isSupported(Desktop.Action.BROWSE)) {
        try {
          desktop.browse(new URI(text));
        }
        catch (Exception e) {
          LOG.error("Failed to open link: " + e.getMessage());
        }
      }
    }
  }

  private String findDuplicate(String updated) {
    GameList importableTables = client.getPinUPPopperService().getImportableTables();
    List<GameListItem> items = importableTables.getItems();
    for (GameListItem item : items) {
      String name = item.getName();
      if (name.equalsIgnoreCase(updated)) {
        return name;
      }
    }

    List<GameRepresentation> gamesCached = client.getGameService().getGamesCached(-1);
    for (GameRepresentation gameRepresentation : gamesCached) {
      if (gameRepresentation.getId() != this.game.getId() && gameRepresentation.getGameFileName().trim().equalsIgnoreCase(updated)) {
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
    String updatedGameFileName = tableDetails.getGameFileName();
    if (game.isVpxGame() && !updatedGameFileName.toLowerCase().endsWith(".vpx")) {
      updatedGameFileName = updatedGameFileName + ".vpx";
    }

    if (!updatedGameFileName.trim().equalsIgnoreCase(initialVpxFileName.trim())) {
      String duplicate = findDuplicate(updatedGameFileName);
      if (duplicate != null) {
        WidgetFactory.showAlert(stage, "Error", "Another VPX file with the name \"" + duplicate + "\" already exist. Please chooser another name.");
        return;
      }
    }

    tableScreensController.save();
    tableDataTabScoreDataController.save();

    try {
      if (closeDialog) {
        stage.close();
      }
      tableDetails = Studio.client.getPinUPPopperService().saveTableDetails(this.tableDetails, game.getId());
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
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    hintCustom2.setVisible(false);
    hintCustom3.setVisible(false);
    hintCustom4.setVisible(false);
    hintCustom5.setVisible(false);
    hintWebId.setVisible(false);

    tableVersionsCombo.setCellFactory(c -> new VpsTableVersionCell());
    tableVersionsCombo.setButtonCell(new VpsTableVersionCell());

    try {
      FXMLLoader loader = new FXMLLoader(TableDataTabStatisticsController.class.getResource("dialog-table-data-tab-statistics.fxml"));
      Parent builtInRoot = loader.load();
      tableStatisticsController = loader.getController();
      statisticsTab.setContent(builtInRoot);
    }
    catch (IOException e) {
      LOG.error("Failed to load dialog-table-data-tab-statistics.fxml: " + e.getMessage(), e);
    }

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
      detailsRoot.getChildren().add(propertRenamingRoot);
    }
    catch (IOException e) {
      LOG.error("Failed to load propper-renaming.fxml: " + e.getMessage(), e);
    }
  }

  @Override
  public void onDialogCancel() {
    TableDataController.lastTab = tabPane.getSelectionModel().getSelectedIndex();
  }

  public void setGame(Stage stage, TableOverviewController overviewController, GameRepresentation game, int tab) {
    this.game = game;
    this.serverSettings = overviewController.getServerSettings();
    this.uiSettings = overviewController.getUISettings();
    scoringDB = client.getSystemService().getScoringDatabase();
    tableDetails = Studio.client.getPinUPPopperService().getTableDetails(game.getId());
    highscoreFiles = client.getGameService().getHighscoreFiles(game.getId());

    if (game.isVpxGame()) {
      tableDataTabScoreDataController.setGame(this, game, tableDetails, highscoreFiles, serverSettings);
      propperRenamingController.setData(752, tableDetails, uiSettings, gameDisplayName, gameFileName, gameName);
    }
    else {
      gameFileName.setDisable(true);
      autoFillBtn.setVisible(false);
      detailsRoot.getChildren().remove(propertRenamingRoot);
      this.tabPane.getTabs().remove(scoreDataTab);
      this.fixVersionBtn.setVisible(false);
      this.vpsPanel.setVisible(false);
    }

    this.stage = stage;
    this.scene = stage.getScene();

    this.titleLabel.setText(game.getGameDisplayName());
    databaseIdLabel.setText("(ID: " + game.getId() + ")  ");

    this.stage.setOnShowing(new EventHandler<WindowEvent>() {
      @Override
      public void handle(WindowEvent event) {
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice defaultScreenDevice = ge.getDefaultScreenDevice();
        GraphicsConfiguration defaultConfiguration = defaultScreenDevice.getDefaultConfiguration();
        if (defaultConfiguration.getBounds().getHeight() < 1080) {
          BorderPane root = (BorderPane) stage.getScene().getRoot();
          root.setPrefHeight(820);
        }
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

    autoFillCheckbox.setSelected(uiSettings.isAutoApplyVpsData());
    autoFillCheckbox.selectedProperty().addListener((observable, oldValue, newValue) -> {
      uiSettings.setAutoApplyVpsData(newValue);
      client.getPreferenceService().setJsonPreference(PreferenceNames.UI_SETTINGS, uiSettings);
    });

    TableDataController.lastTab = tab;
    tabPane.getSelectionModel().select(tab);

    this.overviewController = overviewController;
    this.initialVpxFileName = game.getGameFileName();

    this.fixVersionBtn.setDisable(!game.isUpdateAvailable() && !StringUtils.isEmpty(tableDetails.getGameVersion()));

    gameName.setText(tableDetails.getGameName());
    gameName.textProperty().addListener((observable, oldValue, newValue) -> {
      if (FileUtils.isValidFilename(newValue)) {
        tableDetails.setGameName(newValue);
      }
      else {
        gameName.setText(oldValue);
      }
    });
    gameFileName.setText(tableDetails.getGameFileName());
    gameFileName.setDisable(tableDetails.getGameFileName().contains("/") || tableDetails.getGameFileName().contains("\\") || !game.isVpxGame());
    gameFileName.textProperty().addListener((observable, oldValue, newValue) -> {
      if (FileUtils.isValidFilename(newValue)) {
        tableDetails.setGameFileName(newValue);
      }
      else {
        gameFileName.setText(oldValue);
      }
    });
    gameDisplayName.setText(tableDetails.getGameDisplayName());
    gameDisplayName.textProperty().addListener((observable, oldValue, newValue) -> tableDetails.setGameDisplayName(newValue.trim()));
    gameTheme.setText(tableDetails.getGameTheme());
    gameTheme.textProperty().addListener((observable, oldValue, newValue) -> tableDetails.setGameTheme(newValue));
    gameVersion.setText(tableDetails.getGameVersion());
    gameVersion.textProperty().addListener((observable, oldValue, newValue) -> {
      tableDetails.setGameVersion(newValue);
      fixVersionBtn.setDisable(!StringUtils.isEmpty(game.getExtVersion()) && newValue.equals(game.getExtVersion()));
    });

    gameTypeCombo.setItems(FXCollections.observableList(Arrays.asList(GameType.values())));
    GameType gt = tableDetails.getGameType();
    if (gt != null) {
      gameTypeCombo.valueProperty().setValue(gt);
    }
    gameTypeCombo.valueProperty().addListener((observableValue, gameType, t1) -> tableDetails.setGameType(t1));

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


    manufacturer.setText(tableDetails.getManufacturer());
    manufacturer.textProperty().addListener((observable, oldValue, newValue) -> tableDetails.setManufacturer(newValue));

    SpinnerValueFactory.IntegerSpinnerValueFactory factory = new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 4, 0);
    numberOfPlayers.setValueFactory(factory);
    if (tableDetails.getNumberOfPlayers() != null) {
      numberOfPlayers.getValueFactory().setValue(tableDetails.getNumberOfPlayers());
    }
    numberOfPlayers.getValueFactory().valueProperty().addListener((observable, oldValue, newValue) -> tableDetails.setNumberOfPlayers(Integer.parseInt(String.valueOf(newValue))));

    tags.setText(tableDetails.getTags());
    tags.textProperty().addListener((observable, oldValue, newValue) -> tableDetails.setTags(newValue));

    category.setText(tableDetails.getCategory());
    category.textProperty().addListener((observable, oldValue, newValue) -> tableDetails.setCategory(newValue));

    author.setText(tableDetails.getAuthor());
    author.textProperty().addListener((observable, oldValue, newValue) -> tableDetails.setAuthor(newValue));

    launchCustomVar.setText(tableDetails.getLaunchCustomVar());
    launchCustomVar.textProperty().addListener((observable, oldValue, newValue) -> tableDetails.setLaunchCustomVar(newValue));

    SpinnerValueFactory.IntegerSpinnerValueFactory ratingFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 10, 0);
    gameRating.setValueFactory(ratingFactory);
    if (tableDetails.getGameRating() != null) {
      gameRating.getValueFactory().setValue(tableDetails.getGameRating());
    }

    gameRating.getValueFactory().valueProperty().addListener((observable, oldValue, newValue) -> tableDetails.setGameRating(Integer.parseInt(String.valueOf(newValue))));

    dof.setText(tableDetails.getDof());
    dof.textProperty().addListener((observable, oldValue, newValue) -> tableDetails.setDof(newValue));

    IPDBNum.setText(tableDetails.getIPDBNum());
    IPDBNum.textProperty().addListener((observable, oldValue, newValue) -> tableDetails.setIPDBNum(newValue));

    altRunMode.setText(tableDetails.getAltRunMode());
    altRunMode.textProperty().addListener((observable, oldValue, newValue) -> tableDetails.setAltRunMode(newValue));

    url.setText(tableDetails.getUrl());
    url.textProperty().addListener((observable, oldValue, newValue) -> tableDetails.setUrl(newValue));

    designedBy.setText(tableDetails.getDesignedBy());
    designedBy.textProperty().addListener((observable, oldValue, newValue) -> tableDetails.setDesignedBy(newValue));

    notes.setText(tableDetails.getNotes());
    notes.textProperty().addListener((observable, oldValue, newValue) -> tableDetails.setNotes(newValue));

    custom2.setText(tableDetails.getCustom2());
    custom2.textProperty().addListener((observable, oldValue, newValue) -> tableDetails.setCustom2(newValue));

    custom3.setText(tableDetails.getCustom3());
    custom3.textProperty().addListener((observable, oldValue, newValue) -> tableDetails.setCustom3(newValue));

    extrasTab.setDisable(!tableDetails.isPopper15());

    statusCombo.setItems(FXCollections.observableList(TABLE_STATUSES));
    if (tableDetails.isPopper15()) {
      statusCombo.setItems(FXCollections.observableList(TABLE_STATUSES_15));
    }

    if (tableDetails.getStatus() >= 0 && tableDetails.getStatus() <= 3) {
      TableStatus tableStatus = TABLE_STATUSES_15.get(tableDetails.getStatus());
      statusCombo.setValue(tableStatus);
    }

    statusCombo.valueProperty().addListener((observableValue, tableStatus, t1) -> {
      this.tableDetails.setStatus(t1.getValue());
    });

    List<String> launcherList = new ArrayList<>(tableDetails.getLauncherList());
    launcherList.add(0, null);
    launcherCombo.setItems(FXCollections.observableList(launcherList));
    launcherCombo.setValue(tableDetails.getAltLaunchExe());

    launcherCombo.valueProperty().addListener((observableValue, s, t1) -> {
      this.tableDetails.setAltLaunchExe(t1);
    });

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

    //cancel edit here!!!!!
    if (extrasTab.isDisable()) {
      return;
    }

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

    gDetails.setText(tableDetails.getgDetails());
    gDetails.textProperty().addListener((observableValue, oldValue, newValue) -> tableDetails.setgDetails(newValue));

    gLog.setText(tableDetails.getgLog());
    gLog.textProperty().addListener((observableValue, oldValue, newValue) -> tableDetails.setgLog(newValue));

    gPlayLog.setText(tableDetails.getgPlayLog());
    gPlayLog.textProperty().addListener((observableValue, oldValue, newValue) -> tableDetails.setgPlayLog(newValue));

    gNotes.setText(tableDetails.getgNotes());
    gNotes.textProperty().addListener((observableValue, oldValue, newValue) -> tableDetails.setgNotes(newValue));


    initVpsStatus();
    tableStatisticsController.setGame(game, tableDetails);
    tableScreensController.setGame(game, tableDetails);
    tabPane.getSelectionModel().select(tab);
  }

  public void setMappedFieldValue(String field, String value) {
    switch (field) {
      case "WEBGameID": {
        webDbId.setText(value);
        webDbId.setDisable(true);
        webDbId.setTooltip(new Tooltip("This field has been reserved for VPin Studio data."));
        hintWebId.setVisible(true);
        break;
      }
      case "CUSTOM2": {
        custom2.setText(value);
        custom2.setDisable(true);
        custom2.setTooltip(new Tooltip("This field has been reserved for VPin Studio data."));
        hintCustom2.setVisible(true);
        break;
      }
      case "CUSTOM3": {
        custom3.setText(value);
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
    copyTableBtn.setDisable(StringUtils.isEmpty(game.getExtTableId()));
    copyTableVersionBtn.setDisable(StringUtils.isEmpty(game.getExtTableVersionId()));
    List<VpsTable> tables = client.getVpsService().getTables();
    TreeSet<String> collect = new TreeSet<>(tables.stream().map(t -> t.getDisplayName()).collect(Collectors.toSet()));
    autoCompleteNameField = new AutoCompleteTextField(this.nameField, this, collect);

    //vps mapping
    String vpsTableMappingField = serverSettings.getMappingVpsTableId();
    String vpsTableVersionMappingField = serverSettings.getMappingVpsTableVersionId();

    String vpsTableId = tableDetails.getMappedValue(vpsTableMappingField);
    if (StringUtils.isEmpty(vpsTableId)) {
      vpsTableId = game.getExtTableId();
      tableDetails.setMappedValue(vpsTableMappingField, vpsTableId);
    }

    String vpsTableVersionId = tableDetails.getMappedValue(vpsTableVersionMappingField);
    if (StringUtils.isEmpty(vpsTableVersionId)) {
      vpsTableVersionId = game.getExtTableVersionId();
      tableDetails.setMappedValue(vpsTableVersionMappingField, vpsTableVersionId);
    }

    VpsTable tableById = null;
    if (!StringUtils.isEmpty(vpsTableId)) {
      tableById = client.getVpsService().getTableById(vpsTableId);
      if (tableById != null) {
        autoCompleteNameField.setText(tableById.getDisplayName());
        propperRenamingController.setVpsTable(tableById);
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
    }

    tableVersionsCombo.valueProperty().addListener(this);
    setMappedFieldValue(vpsTableMappingField, vpsTableId);
    setMappedFieldValue(vpsTableVersionMappingField, vpsTableVersionId);
  }

  private void refreshVersionsCombo(VpsTable tableById) {
    if (tableById != null) {
      List<VpsTableVersion> tableFiles = new ArrayList<>(tableById.getTableFilesForFormat(VpsFeatures.VPX));
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
    openVpsTableVersionBtn.setDisable(newValue == null || newValue.getUrls().isEmpty());
    copyTableVersionBtn.setDisable(newValue == null);

    String mappingVpsTableVersionId = serverSettings.getMappingVpsTableVersionId();
    setMappedFieldValue(mappingVpsTableVersionId, newValue != null ? newValue.getId() : null);
    tableDetails.setMappedValue(mappingVpsTableVersionId, newValue != null ? newValue.getId() : null);

    propperRenamingController.setVpsTableVersion(newValue);

    if (this.autoFillCheckbox.isSelected()) {
      onAutoFill();
    }
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

      String mappingVpsTableId = serverSettings.getMappingVpsTableId();
      setMappedFieldValue(mappingVpsTableId, vpsTable.getId());

      propperRenamingController.setVpsTable(vpsTable);
    }

    openVpsTableVersionBtn.setDisable(true);
    copyTableVersionBtn.setDisable(true);

    refreshVersionsCombo(selectedEntry.orElseGet(null));
    this.tableVersionsCombo.valueProperty().addListener(this);
  }
}
