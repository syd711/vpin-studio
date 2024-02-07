package de.mephisto.vpin.ui.tables.dialogs;

import de.mephisto.vpin.commons.fx.DialogController;
import de.mephisto.vpin.commons.utils.FileUtils;
import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.games.GameEmulatorRepresentation;
import de.mephisto.vpin.restclient.games.GameList;
import de.mephisto.vpin.restclient.games.GameListItem;
import de.mephisto.vpin.restclient.games.GameRepresentation;
import de.mephisto.vpin.restclient.popper.GameType;
import de.mephisto.vpin.restclient.popper.TableDetails;
import de.mephisto.vpin.restclient.preferences.ServerSettings;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.events.EventManager;
import de.mephisto.vpin.ui.tables.TableDialogs;
import de.mephisto.vpin.ui.tables.TableOverviewController;
import de.mephisto.vpin.ui.tables.TableScanProgressModel;
import de.mephisto.vpin.ui.util.ProgressDialog;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.*;
import java.util.List;

import static de.mephisto.vpin.ui.Studio.client;

public class TableDataController implements Initializable, DialogController {
  private final static Logger LOG = LoggerFactory.getLogger(TableDataController.class);

  private final static TableStatus STATUS_DISABLED = new TableStatus(0, "InActive (Disabled)");
  private final static TableStatus STATUS_NORMAL = new TableStatus(1, "Visible (Normal)");
  private final static TableStatus STATUS_MATURE = new TableStatus(2, "Visible (Mature/Hidden)");
  private final static TableStatus STATUS_WIP = new TableStatus(3, "Work In Progress");

  private final static List<TableStatus> TABLE_STATUSES = new ArrayList<>(Arrays.asList(STATUS_DISABLED, STATUS_NORMAL, STATUS_MATURE));
  public final static List<TableStatus> TABLE_STATUSES_15 = new ArrayList<>(Arrays.asList(STATUS_DISABLED, STATUS_NORMAL, STATUS_MATURE, STATUS_WIP));

  @FXML
  private Label titleLabel;

  @FXML
  private Label databaseIdLabel;

  @FXML
  private TextField highscoreFileName;

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
  private TextField romName;

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
  private ComboBox<TableDataController.TableStatus> statusCombo;

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

  private List<CheckBox> screenCheckboxes = new ArrayList<>();
  private TableOverviewController overviewController;
  private GameRepresentation game;
  private TableDetails tableDetails;
  private String initialVpxFileName = null;

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
      int index = this.tabPane.getSelectionModel().getSelectedIndex();
      Platform.runLater(() -> {
        Stage stage = (Stage) ((Button) e.getSource()).getScene().getWindow();
        stage.close();
      });

      Platform.runLater(() -> {
        TableDialogs.openTableDataDialog(this.overviewController, selection, index);
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
        Stage stage = (Stage) ((Button) e.getSource()).getScene().getWindow();
        stage.close();
      });

      Platform.runLater(() -> {
        TableDialogs.openTableDataDialog(this.overviewController, selection, index);
      });
    }
  }

  @FXML
  private void onRomApply() {
    romName.setText(scannedRomName.getText());
  }

  @FXML
  private void onAltRomApply() {
    altRomName.setText(scannedAltRomName.getText());
  }

  @FXML
  private void onHsApply() {
    highscoreFileName.setText(scannedHighscoreFileName.getText());
  }

  @FXML
  private void onTableScan() {
    ProgressDialog.createProgressDialog(new TableScanProgressModel("Scanning \"" + game.getGameDisplayName() + "\"", Arrays.asList(game)));
    this.game = client.getGame(this.game.getId());
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
      WidgetFactory.showAlert(Studio.stage, "Error", "Failed to open EM highscore file \"" + game.getHsFileName() + "\": " + e.getMessage());
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
    Stage stage = (Stage) ((Button) e.getSource()).getScene().getWindow();
    doSave(stage, false);
  }

  @FXML
  private void onSaveClick(ActionEvent e) {
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
    Stage stage = (Stage) ((Button) e.getSource()).getScene().getWindow();
    stage.close();
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
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

  }

  public void setGame(TableOverviewController overviewController, GameRepresentation game, int tab) {
    this.overviewController = overviewController;
    this.game = game;
    this.initialVpxFileName = game.getGameFileName();
    ServerSettings serverSettings = client.getPreferenceService().getJsonPreference(PreferenceNames.SERVER_SETTINGS, ServerSettings.class);

    this.fixVersionBtn.setDisable(!game.isUpdateAvailable());

    this.titleLabel.setText(game.getGameDisplayName());

    tableDetails = Studio.client.getPinUPPopperService().getTableDetails(game.getId());
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
    romName.setText(tableDetails.getRomName());
    romName.textProperty().addListener((observable, oldValue, newValue) -> tableDetails.setRomName(newValue));
    if (StringUtils.isEmpty(tableDetails.getRomName()) && !StringUtils.isEmpty(game.getRom())) {
      romName.setPromptText(game.getRom() + " (scanned value)");
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
    altRomName.textProperty().addListener((observable, oldValue, newValue) -> tableDetails.setRomAlt(newValue));
    if (StringUtils.isEmpty(tableDetails.getRomAlt()) && !StringUtils.isEmpty(game.getTableName())) {
      romName.setPromptText(game.getTableName() + " (scanned value)");
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

    scannedRomName.setText(game.getRom());
    scannedRomName.textProperty().addListener((observableValue, oldValue, newValue) -> {
      game.setRom(newValue);
      applyRomBtn.setDisable(StringUtils.isEmpty(newValue));
      if (StringUtils.isEmpty(romName.getText())) {
        romName.setPromptText(newValue + " (scanned value)");
      }
    });
    applyRomBtn.setDisable(StringUtils.isEmpty(scannedRomName.getText()));

    scannedAltRomName.setText(game.getTableName());
    scannedAltRomName.textProperty().addListener((observableValue, oldValue, newValue) -> {
      game.setTableName(newValue);
      applyAltRomBtn.setDisable(StringUtils.isEmpty(newValue));
      if (StringUtils.isEmpty(altRomName.getText())) {
        altRomName.setPromptText(newValue + " (scanned value)");
      }
    });
    applyAltRomBtn.setDisable(StringUtils.isEmpty(scannedAltRomName.getText()));


    String mappingHsField = serverSettings.getMappingHsFileName();
    scannedHighscoreFileName.setText(game.getHsFileName());
    scannedHighscoreFileName.textProperty().addListener((observableValue, oldValue, newValue) -> {
      game.setHsFileName(newValue);
      applyHsBtn.setDisable(StringUtils.isEmpty(newValue));
      if (StringUtils.isEmpty(highscoreFileName.getText())) {
        highscoreFileName.setPromptText(newValue + " (scanned value)");
      }
    });
    applyHsBtn.setDisable(StringUtils.isEmpty(scannedHighscoreFileName.getText()));
    hsMappingLabel.setText("The value is mapped to Popper field \"" + mappingHsField + "\"");

    switch (mappingHsField) {
      case "CUSTOM2": {
        highscoreFileName.setText(tableDetails.getCustom2());
        break;
      }
      case "CUSTOM3": {
        highscoreFileName.setText(tableDetails.getCustom3());
        break;
      }
      case "CUSTOM4": {
        highscoreFileName.setText(tableDetails.getCustom4());
        break;
      }
      case "CUSTOM5": {
        highscoreFileName.setText(tableDetails.getCustom5());
        break;
      }
    }
    refreshHsMappingField(mappingHsField, highscoreFileName.getText());

    if (StringUtils.isEmpty(highscoreFileName.getText()) && !StringUtils.isEmpty(game.getHsFileName())) {
      highscoreFileName.setPromptText(game.getHsFileName() + " (scanned value)");
    }
    highscoreFileName.textProperty().addListener(new ChangeListener<String>() {
      @Override
      public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
        refreshHsMappingField(mappingHsField, newValue);
      }
    });

    tabPane.getSelectionModel().select(tab);
  }

  private void refreshVpsTableIdField(String vpsTableIdField, String value) {

  }

  private void refreshHsMappingField(String mappingHsField, String value) {
    switch (mappingHsField) {
      case "CUSTOM2": {
        custom2.setText(value);
        custom2.setDisable(true);
        break;
      }
      case "CUSTOM3": {
        custom3.setText(value);
        custom3.setDisable(true);
        break;
      }
      case "CUSTOM4": {
        custom4.setText(value);
        custom4.setDisable(true);
        break;
      }
      case "CUSTOM5": {
        custom5.setText(value);
        custom5.setDisable(true);
        break;
      }
    }
  }

  private void refreshScannedValues() {
    scannedRomName.setText(game.getRom());
    applyRomBtn.setDisable(StringUtils.isEmpty(game.getRom()));

    scannedAltRomName.setText(game.getTableName());
    applyAltRomBtn.setDisable(StringUtils.isEmpty(game.getTableName()));

    scannedHighscoreFileName.setText(game.getHsFileName());
    if (StringUtils.isEmpty(game.getHsFileName()) && !StringUtils.isEmpty(game.getTableName())) {
      scannedHighscoreFileName.setPromptText(game.getTableName());
    }
    applyHsBtn.setDisable(StringUtils.isEmpty(game.getHsFileName()));
  }

  public static class TableStatus {
    public final int value;
    public final String label;

    TableStatus(int value, String label) {
      this.value = value;
      this.label = label;
    }

    public int getValue() {
      return value;
    }

    public String getLabel() {
      return label;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (!(o instanceof TableStatus)) return false;

      TableStatus that = (TableStatus) o;

      return value == that.value;
    }

    @Override
    public int hashCode() {
      return value;
    }

    @Override
    public String toString() {
      return label;
    }
  }
}
