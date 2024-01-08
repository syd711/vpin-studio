package de.mephisto.vpin.ui.tables.dialogs;

import de.mephisto.vpin.commons.fx.DialogController;
import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.restclient.popper.GameType;
import de.mephisto.vpin.restclient.popper.TableDetails;
import de.mephisto.vpin.restclient.games.GameRepresentation;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.events.EventManager;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

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

  private List<CheckBox> screenCheckboxes = new ArrayList<>();
  private GameRepresentation game;
  private TableDetails tableDetails;

  @FXML
  private void onSaveClick(ActionEvent e) {
    Stage stage = (Stage) ((Button) e.getSource()).getScene().getWindow();

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
      stage.close();
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

  public void setGame(GameRepresentation game) {
    this.game = game;
    this.titleLabel.setText("Table Data of '" + game.getGameDisplayName() + "'");

    tableDetails = Studio.client.getPinUPPopperService().getTableDetails(game.getId());

    gameName.setText(tableDetails.getGameName());
    gameFileName.setText(tableDetails.getGameFileName());
    gameFileName.textProperty().addListener((observable, oldValue, newValue) -> tableDetails.setGameFileName(newValue));
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

    romName.setText(tableDetails.getRomName());
    romName.textProperty().addListener((observable, oldValue, newValue) -> tableDetails.setRomName(newValue));

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

    altRomName.setText(tableDetails.getRomAlt());
    altRomName.textProperty().addListener((observable, oldValue, newValue) -> tableDetails.setRomAlt(newValue));

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
