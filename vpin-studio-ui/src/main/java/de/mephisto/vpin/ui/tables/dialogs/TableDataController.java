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
import de.mephisto.vpin.restclient.games.*;
import de.mephisto.vpin.restclient.highscores.HighscoreFiles;
import de.mephisto.vpin.restclient.highscores.HighscoreType;
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
import de.mephisto.vpin.ui.tables.TableScanProgressModel;
import de.mephisto.vpin.ui.tables.dialogs.models.TableStatus;
import de.mephisto.vpin.ui.tables.vps.VpsTableVersionCell;
import de.mephisto.vpin.ui.util.AutoCompleteTextField;
import de.mephisto.vpin.ui.util.AutoCompleteTextFieldChangeListener;
import de.mephisto.vpin.ui.util.ProgressDialog;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
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
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.File;
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
  public static final String UNPLAYED_STATUS_ICON = "bi-check2-circle";
  public static final String UNSUPPORTED_STATUS_ICON = "mdi2l-list-status";
  public static int lastTab = 0;

  @FXML
  private Label titleLabel;

  @FXML
  private Label databaseIdLabel;

  @FXML
  private ComboBox<String> highscoreFileName;

  @FXML
  private TextField scannedHighscoreFileName;

  @FXML
  private TextField scannedRomName;

  @FXML
  private TextField scannedAltRomName;

  @FXML
  private Button openHsFileBtn;


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
  private ComboBox<String> romName;

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
  private TextField altRomName;

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

  //screens
  @FXML
  private CheckBox useEmuDefaultsCheckbox;

  @FXML
  private CheckBox hideAllCheckbox;

  @FXML
  private CheckBox topperCheckbox;

  @FXML
  private CheckBox dmdCheckbox;

  @FXML
  private CheckBox backglassCheckbox;

  @FXML
  private CheckBox playfieldCheckbox;

  @FXML
  private CheckBox musicCheckbox;

  @FXML
  private CheckBox apronCheckbox;

  @FXML
  private CheckBox wheelbarCheckbox;

  @FXML
  private CheckBox loadingCheckbox;

  @FXML
  private CheckBox otherCheckbox;

  @FXML
  private CheckBox flyerCheckbox;

  @FXML
  private CheckBox helpCheckbox;

  @FXML
  private ComboBox<TableStatus> statusCombo;

  @FXML
  private ComboBox<String> launcherCombo;

  @FXML
  private TabPane tabPane;

  @FXML
  private Button applyAltRomBtn;

  @FXML
  private Button applyRomBtn;

  @FXML
  private Button applyHsBtn;

  @FXML
  private Label hsMappingLabel;

  @FXML
  private Button prevButton;

  @FXML
  private Button nextButton;

  @FXML
  private Button fixVersionBtn;

  @FXML
  private SplitMenuButton autoFillBtn;

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
  private CheckBox autoFillCheckbox;

  @FXML
  private HBox hsFileStatusBox;

  @FXML
  private HBox romStatusBox;

  @FXML
  private ComboBox<VpsTableVersion> tableVersionsCombo;
  private AutoCompleteTextField autoCompleteNameField;

  private List<CheckBox> screenCheckboxes = new ArrayList<>();
  private TableOverviewController overviewController;

  private GameRepresentation game;
  private GameDetailsRepresentation gameDetails;

  private TableDetails tableDetails;
  private String initialVpxFileName = null;

  private UISettings uiSettings;
  private ServerSettings serverSettings;

  private Scene scene;
  private Stage stage;
  private ScoringDB scoringDB;
  private HighscoreFiles highscoreFiles;

  @FXML
  private void onAssetManager(ActionEvent e) {
    this.onCancelClick(e);
    Platform.runLater(() -> {
      TableDialogs.openTableAssetsDialog(overviewController, this.game, PopperScreen.BackGlass);
    });
  }

  @FXML
  private void onAutoMatch() {
    String rom = tableDetails.getRomName();
    if (StringUtils.isEmpty(rom) && !StringUtils.isEmpty(gameDetails.getRomName())) {
      rom = gameDetails.getRomName();
    }

    List<VpsTable> vpsTables = VPS.getInstance().find(game.getGameDisplayName(), rom);
    if (!vpsTables.isEmpty()) {
      VpsTable vpsTable = vpsTables.get(0);
      setMappedFieldValue(serverSettings.getMappingVpsTableId(), vpsTable.getId());
      tableDetails.setMappedValue(serverSettings.getMappingVpsTableId(), vpsTable.getId());

      TableInfo tableInfo = client.getVpxService().getTableInfo(game);
      String tableVersion = null;
      if (tableInfo != null) {
        tableVersion = tableInfo.getTableVersion();
      }

      VpsTableVersion version = VPS.getInstance().findVersion(vpsTable, game.getGameFileName(), game.getGameDisplayName(), tableVersion);
      if (version != null) {
        setMappedFieldValue(serverSettings.getMappingVpsTableVersionId(), version.getId());
        tableDetails.setMappedValue(serverSettings.getMappingVpsTableVersionId(), version.getId());
      }

      autoCompleteNameField.setText(vpsTable.getDisplayName());
      refreshVersionsCombo(vpsTable);
      tableVersionsCombo.setValue(version);

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
      }
    } catch (Exception e) {
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
      } catch (Exception e) {
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
      } catch (Exception e) {
        LOG.error("Failed to open link: " + e.getMessage());
      }
    }
  }

  @FXML
  private void onVersionFix() {
    Optional<ButtonType> result = WidgetFactory.showConfirmation(Studio.stage, "Auto-Fix Table Version?", "This overwrites the existing PinUP Popper table version \""
        + game.getVersion() + "\" with the VPS table version \"" +
        game.getExtVersion() + "\".", "The table update indicator won't be shown afterwards.");
    if (result.isPresent() && result.get().equals(ButtonType.OK)) {
      TableDetails td = client.getPinUPPopperService().getTableDetails(game.getId());
      td.setGameVersion(game.getExtVersion());
      try {
        client.getPinUPPopperService().saveTableDetails(td, game.getId());
        EventManager.getInstance().notifyTableChange(game.getId(), null);
      } catch (Exception ex) {
        LOG.error("Error saving table manifest: " + ex.getMessage(), ex);
        WidgetFactory.showAlert(Studio.stage, "Error", "Error saving table manifest: " + ex.getMessage());
      }
    }
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
  private void onRomApply() {
    romName.setValue(scannedRomName.getText());
  }

  @FXML
  private void onAltRomApply() {
    altRomName.setText(scannedAltRomName.getText());
  }

  @FXML
  private void onHsApply() {
    highscoreFileName.setValue(scannedHighscoreFileName.getText());
  }

  @FXML
  private void onTableScan() {
    ProgressDialog.createProgressDialog(new TableScanProgressModel("Scanning \"" + game.getGameDisplayName() + "\"", Arrays.asList(game)));
    this.game = client.getGame(this.game.getId());
    this.gameDetails = client.getGameService().getGameDetails(this.game.getId());
    refreshScannedValues();
  }

  @FXML
  private void onWeblinkProperty() {
    String text = this.webLink.getText();
    if (!StringUtils.isEmpty(text) && text.startsWith("http")) {
      Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
      if (desktop != null && desktop.isSupported(Desktop.Action.BROWSE)) {
        try {
          desktop.browse(new URI(text));
        } catch (Exception e) {
          LOG.error("Failed to open link: " + e.getMessage());
        }
      }
    }
  }

  @FXML
  private void onEMHighscore() {
    GameEmulatorRepresentation emulatorRepresentation = client.getPinUPPopperService().getGameEmulator(this.game.getEmulatorId());
    File folder = new File(emulatorRepresentation.getUserDirectory());
    try {
      Desktop.getDesktop().open(folder);
    } catch (IOException e) {
      WidgetFactory.showAlert(Studio.stage, "Error", "Failed to open EM highscore file: " + e.getMessage());
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
        } catch (Exception e) {
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

    List<GameRepresentation> gamesCached = client.getGameService().getGamesCached();
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

  private void doSave(Stage stage, boolean closeDialog) {
    String updatedGameFileName = tableDetails.getGameFileName();
    if (!updatedGameFileName.toLowerCase().endsWith(".vpx")) {
      updatedGameFileName = updatedGameFileName + ".vpx";
    }

    if (!updatedGameFileName.trim().equalsIgnoreCase(initialVpxFileName.trim())) {
      String duplicate = findDuplicate(updatedGameFileName);
      if (duplicate != null) {
        WidgetFactory.showAlert(stage, "Error", "Another VPX file with the name \"" + duplicate + "\" already exist. Please chooser another name.");
        return;
      }
    }


    String value = "";
    if (useEmuDefaultsCheckbox.isSelected()) {
      //nothing, empty value for defaults
    }
    else if (hideAllCheckbox.isSelected()) {
      value = "NONE";
    }
    else {
      List<String> result = new ArrayList<>();
      if (topperCheckbox.isSelected()) result.add("" + 0);
      if (dmdCheckbox.isSelected()) result.add("" + 1);
      if (backglassCheckbox.isSelected()) result.add("" + 2);
      if (playfieldCheckbox.isSelected()) result.add("" + 3);
      if (musicCheckbox.isSelected()) result.add("" + 4);
      if (apronCheckbox.isSelected()) result.add("" + 5);
      if (wheelbarCheckbox.isSelected()) result.add("" + 6);
      if (loadingCheckbox.isSelected()) result.add("" + 7);
      if (otherCheckbox.isSelected()) result.add("" + 8);
      if (flyerCheckbox.isSelected()) result.add("" + 9);
      if (helpCheckbox.isSelected()) result.add("" + 10);

      value = String.join(",", result);
    }
    tableDetails.setKeepDisplays(value);

    try {
      if (closeDialog) {
        stage.close();
      }
      tableDetails = Studio.client.getPinUPPopperService().saveTableDetails(this.tableDetails, game.getId());
      EventManager.getInstance().notifyTableChange(game.getId(), null);
    } catch (Exception ex) {
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

    openHsFileBtn.setVisible(client.getSystemService().isLocal());

    //screens
    screenCheckboxes = Arrays.asList(topperCheckbox, dmdCheckbox, backglassCheckbox, playfieldCheckbox, musicCheckbox,
        apronCheckbox, wheelbarCheckbox, loadingCheckbox, otherCheckbox, flyerCheckbox, helpCheckbox);

    useEmuDefaultsCheckbox.selectedProperty().addListener((observable, oldValue, newValue) -> {
      if (newValue) {
        screenCheckboxes.stream().forEach(check -> check.setSelected(false));
        hideAllCheckbox.setSelected(false);
      }
    });

    hideAllCheckbox.selectedProperty().addListener((observable, oldValue, newValue) -> {
      if (newValue) {
        screenCheckboxes.stream().forEach(check -> check.setSelected(false));
        useEmuDefaultsCheckbox.setSelected(false);
      }
    });

    topperCheckbox.selectedProperty().addListener((observable, oldValue, newValue) -> {
      if (newValue) {
        hideAllCheckbox.setSelected(false);
        useEmuDefaultsCheckbox.setSelected(false);
      }
    });
    dmdCheckbox.selectedProperty().addListener((observable, oldValue, newValue) -> {
      if (newValue) {
        hideAllCheckbox.setSelected(false);
        useEmuDefaultsCheckbox.setSelected(false);
      }
    });
    backglassCheckbox.selectedProperty().addListener((observable, oldValue, newValue) -> {
      if (newValue) {
        hideAllCheckbox.setSelected(false);
        useEmuDefaultsCheckbox.setSelected(false);
      }
    });
    playfieldCheckbox.selectedProperty().addListener((observable, oldValue, newValue) -> {
      if (newValue) {
        hideAllCheckbox.setSelected(false);
        useEmuDefaultsCheckbox.setSelected(false);
      }
    });
    musicCheckbox.selectedProperty().addListener((observable, oldValue, newValue) -> {
      if (newValue) {
        hideAllCheckbox.setSelected(false);
        useEmuDefaultsCheckbox.setSelected(false);
      }
    });
    apronCheckbox.selectedProperty().addListener((observable, oldValue, newValue) -> {
      if (newValue) {
        hideAllCheckbox.setSelected(false);
        useEmuDefaultsCheckbox.setSelected(false);
      }
    });
    wheelbarCheckbox.selectedProperty().addListener((observable, oldValue, newValue) -> {
      if (newValue) {
        hideAllCheckbox.setSelected(false);
        useEmuDefaultsCheckbox.setSelected(false);
      }
    });
    loadingCheckbox.selectedProperty().addListener((observable, oldValue, newValue) -> {
      if (newValue) {
        hideAllCheckbox.setSelected(false);
        useEmuDefaultsCheckbox.setSelected(false);
      }
    });
    otherCheckbox.selectedProperty().addListener((observable, oldValue, newValue) -> {
      if (newValue) {
        hideAllCheckbox.setSelected(false);
        useEmuDefaultsCheckbox.setSelected(false);
      }
    });
    flyerCheckbox.selectedProperty().addListener((observable, oldValue, newValue) -> {
      if (newValue) {
        hideAllCheckbox.setSelected(false);
        useEmuDefaultsCheckbox.setSelected(false);
      }
    });
    helpCheckbox.selectedProperty().addListener((observable, oldValue, newValue) -> {
      if (newValue) {
        hideAllCheckbox.setSelected(false);
        useEmuDefaultsCheckbox.setSelected(false);
      }
    });
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
    gameDetails = client.getGameService().getGameDetails(game.getId());
    tableDetails = Studio.client.getPinUPPopperService().getTableDetails(game.getId());
    highscoreFiles = client.getGameService().getHighscoreFiles(game.getId());

    List<String> availableRoms = new ArrayList<>(highscoreFiles.getNvRams());
    availableRoms.addAll(highscoreFiles.getVpRegEntries());
    Collections.sort(availableRoms);
    availableRoms.add(0, null);
    romName.setItems(FXCollections.observableList(availableRoms));

    List<String> availableHsFiles = new ArrayList<>(highscoreFiles.getTextFiles());
    Collections.sort(availableHsFiles);
    availableHsFiles.add(0, null);
    highscoreFileName.setItems(FXCollections.observableList(availableHsFiles));

    refreshStatusIcons();
    refreshScannedValues();

    this.stage = stage;
    this.scene = stage.getScene();

    scene.addEventHandler(KeyEvent.KEY_PRESSED, t -> {
      if (t.getCode() == KeyCode.PAGE_UP) {
        onPrevious(null);
      }
      if (t.getCode() == KeyCode.PAGE_DOWN) {
        onNext(null);
      }
    });

    autoFillCheckbox.setSelected(uiSettings.isAutoFill());
    autoFillCheckbox.selectedProperty().addListener((observable, oldValue, newValue) -> {
      uiSettings.setAutoFill(newValue);
      client.getPreferenceService().setJsonPreference(PreferenceNames.UI_SETTINGS, uiSettings);
    });

    TableDataController.lastTab = tab;
    tabPane.getSelectionModel().select(tab);

    this.overviewController = overviewController;
    this.initialVpxFileName = game.getGameFileName();

    this.fixVersionBtn.setDisable(!game.isUpdateAvailable());

    this.titleLabel.setText(game.getGameDisplayName());


    databaseIdLabel.setText("(ID: " + game.getId() + ")  ");
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
    gameVersion.textProperty().addListener((observable, oldValue, newValue) -> tableDetails.setGameVersion(newValue));

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

    applyRomBtn.setDisable(true);
    romName.setValue(tableDetails.getRomName());
    romName.valueProperty().addListener((observable, oldValue, newValue) -> {
      onRomNameUpdate(newValue);
    });
    romName.getEditor().textProperty().addListener((observable, oldValue, newValue) -> {
      onRomNameUpdate(newValue);
    });
    romName.focusedProperty().addListener(new ChangeListener<Boolean>() {
      @Override
      public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
        onRomNameFocusChange(newValue);
      }
    });

    if (StringUtils.isEmpty(tableDetails.getRomName()) && !StringUtils.isEmpty(gameDetails.getRomName())) {
      romName.setPromptText(gameDetails.getRomName() + " (scanned value)");
      applyRomBtn.setDisable(false);
    }

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

    //displays
    String keepDisplays = tableDetails.getKeepDisplays();
    if (StringUtils.isEmpty(keepDisplays)) {
      useEmuDefaultsCheckbox.setSelected(true);
    }
    else if (keepDisplays.equalsIgnoreCase("NONE")) {
      hideAllCheckbox.setSelected(true);
    }
    else {
      String[] split = keepDisplays.split(",");
      for (String screen : split) {
        if (StringUtils.isEmpty(screen)) {
          continue;
        }

        int id = Integer.parseInt(screen);
        switch (id) {
          case 0: {
            topperCheckbox.setSelected(true);
            break;
          }
          case 1: {
            dmdCheckbox.setSelected(true);
            break;
          }
          case 2: {
            backglassCheckbox.setSelected(true);
            break;
          }
          case 3: {
            playfieldCheckbox.setSelected(true);
            break;
          }
          case 4: {
            musicCheckbox.setSelected(true);
            break;
          }
          case 5: {
            apronCheckbox.setSelected(true);
            break;
          }
          case 6: {
            wheelbarCheckbox.setSelected(true);
            break;
          }
          case 7: {
            loadingCheckbox.setSelected(true);
            break;
          }
          case 8: {
            otherCheckbox.setSelected(true);
            break;
          }
          case 9: {
            flyerCheckbox.setSelected(true);
            break;
          }
          case 10: {
            helpCheckbox.setSelected(true);
            break;
          }
        }
      }
    }

    if (tableDetails.getVolume() != null) {
      try {
        volumeSlider.setValue(Integer.parseInt(tableDetails.getVolume()));
      } catch (NumberFormatException e) {
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

    applyAltRomBtn.setDisable(true);
    altRomName.setText(tableDetails.getRomAlt());
    altRomName.textProperty().addListener((observable, oldValue, newValue) -> {
      onAltRomNameUpdate(newValue);
    });
    altRomName.focusedProperty().addListener((observable, oldValue, newValue) -> onAltRomNameFocusChange(newValue));

    if (StringUtils.isEmpty(tableDetails.getRomAlt()) && !StringUtils.isEmpty(gameDetails.getTableName())) {
      altRomName.setPromptText(gameDetails.getTableName() + " (scanned value)");
      applyAltRomBtn.setDisable(false);
    }

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

    scannedRomName.setText(gameDetails.getRomName());
    applyRomBtn.setDisable(StringUtils.isEmpty(scannedRomName.getText()));

    scannedAltRomName.setText(gameDetails.getTableName());
    applyAltRomBtn.setDisable(StringUtils.isEmpty(scannedAltRomName.getText()));


    String mappingHsField = serverSettings.getMappingHsFileName();
    scannedHighscoreFileName.setText(gameDetails.getHsFileName());
    applyHsBtn.setDisable(StringUtils.isEmpty(scannedHighscoreFileName.getText()));
    hsMappingLabel.setText("The value is mapped to Popper field \"" + mappingHsField + "\"");

    //highscore mapping
    String hsFileName = tableDetails.getMappedValue(mappingHsField);
    highscoreFileName.setValue(hsFileName);

    setMappedFieldValue(mappingHsField, highscoreFileName.getValue());
    if (StringUtils.isEmpty(highscoreFileName.getValue()) && !StringUtils.isEmpty(gameDetails.getHsFileName())) {
      highscoreFileName.setPromptText(gameDetails.getHsFileName() + " (scanned value)");
    }
    highscoreFileName.valueProperty().addListener((observable, oldValue, newValue) -> {
      onHighscoreFilenameUpdate(newValue, mappingHsField);
    });
    highscoreFileName.getEditor().textProperty().addListener((observable, oldValue, newValue) -> {
      onHighscoreFilenameUpdate(newValue, mappingHsField);
    });

    initVpsStatus();
    tabPane.getSelectionModel().select(tab);
  }

  private void onRomNameFocusChange(Boolean newValue) {
    if(!newValue) {
      romName.setPromptText("");
      if (StringUtils.isEmpty(tableDetails.getRomName()) && !StringUtils.isEmpty(gameDetails.getRomName())) {
        romName.setPromptText(gameDetails.getRomName() + " (scanned value)");
      }
    }
  }

  private void onHighscoreFilenameUpdate(String newValue, String mappingHsField) {
    setMappedFieldValue(mappingHsField, newValue);
    refreshStatusIcons();
  }

  private void onAltRomNameUpdate(String newValue) {
    tableDetails.setRomAlt(newValue);
    refreshStatusIcons();
  }

  private void onAltRomNameFocusChange(Boolean newValue) {
    if(!newValue) {
      altRomName.setPromptText("");
      if (StringUtils.isEmpty(tableDetails.getRomAlt()) && !StringUtils.isEmpty(gameDetails.getTableName())) {
        altRomName.setPromptText(gameDetails.getTableName() + " (scanned value)");
      }
    }
  }

  private void onRomNameUpdate(String newValue) {
    tableDetails.setRomName(newValue);
    refreshStatusIcons();
  }

  private void refreshStatusIcons() {
    boolean played = tableDetails.getNumberPlays() != null && tableDetails.getNumberPlays() > 0;
    String hsType = game.getHighscoreType();

    String rom = this.getEffectiveRom();
    String tableName = this.getEffectiveTableName();
    romStatusBox.getChildren().removeAll(romStatusBox.getChildren());
    if (!String.valueOf(hsType).equals(HighscoreType.EM.name()) && !StringUtils.isEmpty(rom)) {
      if (romName.getItems().contains(rom)) {
        Label l = new Label();
        l.setGraphic(WidgetFactory.createCheckIcon());
        l.setTooltip(new Tooltip("A matching highscore entry has been found for this ROM name."));
        romStatusBox.getChildren().add(l);
      }
      else if (StringUtils.isEmpty(getEffectiveHighscoreFilename())) {
        if (played) {
          Label l = new Label();
          l.setGraphic(WidgetFactory.createExclamationIcon());
          l.setTooltip(new Tooltip("Table has been played, but no nv-RAM file or VPReg.stg entry has been found."));
          romStatusBox.getChildren().add(l);
        }
        else {
          Label l = new Label();
          l.setGraphic(WidgetFactory.createIcon(UNPLAYED_STATUS_ICON));
          l.setTooltip(new Tooltip("No nv-RAM file or entry in VPReg.stg has been found, but the table has not been played yet."));
          romStatusBox.getChildren().add(l);
        }
      }
    }

    //check ROM name validity
    if (played
        && !scoringDB.getSupportedNvRams().contains(rom)
        && !scoringDB.getSupportedNvRams().contains(tableName)
        && !highscoreFiles.getVpRegEntries().contains(rom)
        && !highscoreFiles.getVpRegEntries().contains(tableName)) {
      Label l = new Label();
      l.setGraphic(WidgetFactory.createUnsupportedIcon());
      l.setTooltip(new Tooltip("This ROM is currently not supported by the highscore parser."));
      romStatusBox.getChildren().add(l);
    }

    String hsName = this.getEffectiveHighscoreFilename();
    hsFileStatusBox.getChildren().removeAll(hsFileStatusBox.getChildren());
    if (!StringUtils.isEmpty(hsName)) {
      if (highscoreFileName.getItems().contains(hsName)) {
        Label l = new Label();
        l.setGraphic(WidgetFactory.createCheckIcon());
        l.setTooltip(new Tooltip("A matching highscore file has been found."));
        hsFileStatusBox.getChildren().add(l);
      }
      else {
        //txt not found
        Label l = new Label();
        if (played) {
          l.setGraphic(WidgetFactory.createExclamationIcon());
          l.setTooltip(new Tooltip("Table has been played, but text file has been found for this name."));
          hsFileStatusBox.getChildren().add(l);
        }
        else {
          l.setGraphic(WidgetFactory.createIcon(UNPLAYED_STATUS_ICON));
          l.setTooltip(new Tooltip("No text file has been found for this name, but the table has not been played yet."));
          hsFileStatusBox.getChildren().add(l);
        }
      }
    }
    else {
      String altRom = getEffectiveTableName();
      if (!StringUtils.isEmpty(altRom)) {
        if (highscoreFileName.getItems().contains(altRom + ".txt")) {
          Label l = new Label();
          l.setGraphic(WidgetFactory.createCheckIcon());
          l.setTooltip(new Tooltip("A matching highscore file has been found (using \"" + altRom + "\"as fallback."));
          hsFileStatusBox.getChildren().add(l);
        }
      }
    }
  }

  private String getEffectiveRom() {
    String rom = tableDetails.getRomName();
    if (StringUtils.isEmpty(rom)) {
      rom = gameDetails.getRomName();
    }
    return rom;
  }

  private String getEffectiveHighscoreFilename() {
    String hs = tableDetails.getMappedValue(serverSettings.getMappingHsFileName());
    if (StringUtils.isEmpty(hs)) {
      hs = gameDetails.getHsFileName();
    }
    return hs;
  }

  private String getEffectiveTableName() {
    String rom = tableDetails.getRomAlt();
    if (StringUtils.isEmpty(rom)) {
      rom = gameDetails.getTableName();
    }
    return rom;
  }

  private void setMappedFieldValue(String field, String value) {
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
    List<VpsTable> tables = VPS.getInstance().getTables();
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
      tableById = VPS.getInstance().getTableById(vpsTableId);
      if (tableById != null) {
        autoCompleteNameField.setText(tableById.getDisplayName());
      }
    }

    refreshVersionsCombo(tableById);

    if (tableById != null && !StringUtils.isEmpty(vpsTableVersionId)) {
      VpsTableVersion tableVersion = tableById.getVersion(vpsTableVersionId);
      if (tableVersion != null) {
        tableVersionsCombo.setValue(tableVersion);
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
    }
    else {
      tableVersionsCombo.setItems(FXCollections.emptyObservableList());
    }
  }

  private void refreshScannedValues() {
    scannedRomName.setText(gameDetails.getRomName());
    applyRomBtn.setDisable(StringUtils.isEmpty(gameDetails.getRomName()));

    scannedAltRomName.setText(gameDetails.getTableName());
    applyAltRomBtn.setDisable(StringUtils.isEmpty(gameDetails.getTableName()));

    scannedHighscoreFileName.setText(gameDetails.getHsFileName());
    if (StringUtils.isEmpty(gameDetails.getHsFileName()) && !StringUtils.isEmpty(gameDetails.getTableName())) {
      highscoreFileName.setPromptText(gameDetails.getTableName() + " (scanned value)");
    }
    scannedHighscoreFileName.setPromptText("");
    if (StringUtils.isEmpty(gameDetails.getHsFileName()) && !StringUtils.isEmpty(gameDetails.getTableName())) {
      scannedHighscoreFileName.setPromptText(gameDetails.getTableName() + ".txt");
    }
    applyHsBtn.setDisable(StringUtils.isEmpty(gameDetails.getHsFileName()));
  }

  @Override
  public void changed(ObservableValue<? extends VpsTableVersion> observable, VpsTableVersion oldValue, VpsTableVersion newValue) {
    openVpsTableVersionBtn.setDisable(newValue == null || newValue.getUrls().isEmpty());
    copyTableVersionBtn.setDisable(newValue == null);

    String mappingVpsTableVersionId = serverSettings.getMappingVpsTableVersionId();
    setMappedFieldValue(mappingVpsTableVersionId, newValue != null ? newValue.getId() : null);
    tableDetails.setMappedValue(mappingVpsTableVersionId, newValue != null ? newValue.getId() : null);

    if (autoFillCheckbox.isSelected()) {
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
    List<VpsTable> tables = VPS.getInstance().getTables();
    Optional<VpsTable> selectedEntry = tables.stream().filter(t -> t.getDisplayName().equalsIgnoreCase(value)).findFirst();
    if (selectedEntry.isPresent()) {
      VpsTable vpsTable = selectedEntry.get();

      String mappingVpsTableId = serverSettings.getMappingVpsTableId();
      setMappedFieldValue(mappingVpsTableId, vpsTable.getId());
    }

    openVpsTableVersionBtn.setDisable(true);
    copyTableVersionBtn.setDisable(true);

    refreshVersionsCombo(selectedEntry.orElseGet(null));
    this.tableVersionsCombo.valueProperty().addListener(this);
  }
}
