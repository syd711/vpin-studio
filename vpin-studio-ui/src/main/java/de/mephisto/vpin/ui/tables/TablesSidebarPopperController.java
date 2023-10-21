package de.mephisto.vpin.ui.tables;

import de.mephisto.vpin.commons.fx.ConfirmationResult;
import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.restclient.popper.TableDetails;
import de.mephisto.vpin.restclient.tables.GameRepresentation;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.events.EventManager;
import de.mephisto.vpin.ui.util.Dialogs;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.text.DateFormat;
import java.util.*;

import static de.mephisto.vpin.ui.Studio.client;
import static de.mephisto.vpin.ui.util.BindingUtil.debouncer;

public class TablesSidebarPopperController implements Initializable, ChangeListener<Number> {
  private final static Logger LOG = LoggerFactory.getLogger(TablesSidebarPopperController.class);

  private final static TableStatus STATUS_DISABLED = new TableStatus(0, "InActive (Disabled)");
  private final static TableStatus STATUS_NORMAL = new TableStatus(1, "Visible (Normal)");
  private final static TableStatus STATUS_MATURE = new TableStatus(2, "Visible (Mature/Hidden)");

  private final static List<TableStatus> TABLE_STATUSES = Arrays.asList(STATUS_DISABLED, STATUS_NORMAL, STATUS_MATURE);

  @FXML
  private ComboBox<String> launcherCombo;

  @FXML
  private ComboBox<TableStatus> statusCombo;

  @FXML
  private Button tableEditBtn;

  @FXML
  private SplitMenuButton autoFillBtn;

  @FXML
  private Button editScreensBtn;

  @FXML
  private Slider volumeSlider;

  @FXML
  private Label emulatorLabel;

  @FXML
  private Label labelLastPlayed;

  @FXML
  private Label labelTimesPlayed;

  @FXML
  private Label gameName;

  @FXML
  private Label gameFileName;

  @FXML
  private Label gameDisplayName;

  @FXML
  private Label gameTheme;

  @FXML
  private Label gameType;

  @FXML
  private Label gameVersion;

  @FXML
  private Label gameYear;

  @FXML
  private Label dateAdded;

  @FXML
  private Label romName;

  @FXML
  private Label manufacturer;

  @FXML
  private Label numberOfPlayers;

  @FXML
  private Label tags;

  @FXML
  private Label category;

  @FXML
  private Label author;

  @FXML
  private Label launchCustomVar;

  @FXML
  private Label keepDisplays;

  @FXML
  private Label gameRating;

  @FXML
  private Label dof;

  @FXML
  private Label IPDBNum;

  @FXML
  private Label altRunMode;

  @FXML
  private Label url;

  @FXML
  private Label designedBy;

  @FXML
  private Label notes;

  private Optional<GameRepresentation> game = Optional.empty();

  private TablesSidebarController tablesSidebarController;
  private TableDetails tableDetails;

  // Add a public no-args constructor
  public TablesSidebarPopperController() {
  }

  @FXML
  private void onTableEdit() {
    if (Studio.client.getPinUPPopperService().isPinUPPopperRunning()) {
      if (Dialogs.openPopperRunningWarning(Studio.stage)) {
        Dialogs.openTableDataDialog(this.game.get());
        this.refreshView(this.game);
      }
      return;
    }

    Dialogs.openTableDataDialog(this.game.get());
    this.refreshView(this.game);
  }

  @FXML
  private void onAutoFill() {
    if (this.game.isPresent()) {
      ConfirmationResult result = WidgetFactory.showAlertOptionWithCheckbox(Studio.stage, "Auto-fill data for \"" + game.get().getGameDisplayName() + "\"?",
          "Cancel", "Continue", "This fills missing entries with data taken from the table metadata and the Virtual Pinball Spreadsheet." , null, "Overwrite existing values");
      if (!result.isApplied()) {
        try {
          boolean checked = result.isChecked();
          client.getPinUPPopperService().autoFillTableDetails(this.game.get().getId(), checked);
          EventManager.getInstance().notifyTableChange(this.game.get().getId(), null);
        } catch (Exception e) {
          WidgetFactory.showAlert(Studio.stage, "Error", e.getMessage());
        }
      }
    }
  }

  @FXML
  private void onAutoFillAll() {
    if (this.game.isPresent()) {
      ConfirmationResult result = WidgetFactory.showAlertOptionWithCheckbox(Studio.stage, "Auto-fill data for all " + client.getGameService().getGamesCached().size() + " tables?",
          "Cancel", "Continue", "This fills missing entries with data taken from the table metadata and the Virtual Pinball Spreadsheet." , null, "Overwrite existing values");
      if (!result.isApplied()) {
        try {
          boolean checked = result.isChecked();
          Dialogs.createProgressDialog(new TableDataAutoFillProgressModel(client.getGameService().getGamesCached(), checked));
          EventManager.getInstance().notifyTablesChanged();
        } catch (Exception e) {
          WidgetFactory.showAlert(Studio.stage, "Error", e.getMessage());
        }
      }
    }
  }

  @FXML
  private void onScreenEdit() {
    if (Studio.client.getPinUPPopperService().isPinUPPopperRunning()) {
      if (Dialogs.openPopperRunningWarning(Studio.stage)) {
        Dialogs.openPopperScreensDialog(this.game.get());
        this.refreshView(this.game);
      }
      return;
    }

    Dialogs.openPopperScreensDialog(this.game.get());
    this.refreshView(this.game);
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    statusCombo.setItems(FXCollections.observableList(TABLE_STATUSES));
    statusCombo.valueProperty().addListener((observableValue, tableStatus, t1) -> {
      try {
        if (Studio.client.getPinUPPopperService().isPinUPPopperRunning()) {
          if (Dialogs.openPopperRunningWarning(Studio.stage)) {
            this.refreshView(this.game);
          }
          return;
        }

        if (game.isPresent() && tableDetails != null) {
          if (t1 == null || t1.getValue() == tableDetails.getStatus()) {
            return;
          }
          this.tableDetails.setStatus(t1.getValue());
          this.tableDetails = client.getPinUPPopperService().saveTableDetails(tableDetails, game.get().getId());
          EventManager.getInstance().notifyTableChange(this.game.get().getId(), null);
        }
      } catch (Exception e) {
        LOG.error("Failed to save table status: " + e.getMessage(), e);
        WidgetFactory.showAlert(Studio.stage, "Error", "Failed to table status: " + e.getMessage());
      }
    });

    launcherCombo.valueProperty().addListener((observableValue, s, t1) -> {
      try {
        if (Studio.client.getPinUPPopperService().isPinUPPopperRunning()) {
          if (Dialogs.openPopperRunningWarning(Studio.stage)) {
            this.refreshView(this.game);
          }
          return;
        }

        if (game.isPresent() && tableDetails != null) {
          String altLauncher = tableDetails.getAltLaunchExe();
          if (StringUtils.equals(altLauncher, t1)) {
            return;
          }
          this.tableDetails = client.getPinUPPopperService().saveCustomLauncher(this.game.get().getId(), t1);
        }
      } catch (Exception e) {
        LOG.error("Failed to save alt launcher: " + e.getMessage(), e);
        WidgetFactory.showAlert(Studio.stage, "Error", "Failed to save alt launcher: " + e.getMessage());
      }
    });
  }


  public void setGame(Optional<GameRepresentation> game) {
    this.game = game;
    this.tableDetails = null;
    this.launcherCombo.setValue(null);
    this.statusCombo.setValue(null);
    this.refreshView(game);
  }

  public void refreshView(Optional<GameRepresentation> g) {
    this.tableEditBtn.setDisable(g.isEmpty());
    this.editScreensBtn.setDisable(g.isEmpty());
    volumeSlider.setDisable(g.isEmpty());
    autoFillBtn.setDisable(g.isEmpty());
    launcherCombo.setDisable(g.isEmpty());
    statusCombo.setDisable(g.isEmpty());

    if (g.isPresent()) {
      GameRepresentation game = g.get();
      tableDetails = Studio.client.getPinUPPopperService().getTableDetails(game.getId());

      labelLastPlayed.setText(tableDetails.getLastPlayed() != null ? DateFormat.getDateInstance().format(tableDetails.getLastPlayed()) : "-");
      if (tableDetails.getNumberPlays() != null) {
        labelTimesPlayed.setText(String.valueOf(tableDetails.getNumberPlays()));
      }
      else {
        labelTimesPlayed.setText("0");
      }


      List<String> launcherList = new ArrayList<>(tableDetails.getLauncherList());
      launcherList.add(0, null);
      launcherCombo.setItems(FXCollections.observableList(launcherList));
      launcherCombo.setValue(tableDetails.getAltLaunchExe());

      if (tableDetails.getStatus() >= 0 && tableDetails.getStatus() <= 2) {
        TableStatus tableStatus = TABLE_STATUSES.get(tableDetails.getStatus());
        statusCombo.setValue(tableStatus);
      }

      volumeSlider.valueProperty().removeListener(this);
      if (tableDetails.getVolume() != null) {
        volumeSlider.setValue(Integer.parseInt(tableDetails.getVolume()));
      }
      else {
        volumeSlider.setValue(100);
      }
      volumeSlider.valueProperty().addListener(this);

      emulatorLabel.setText(client.getPinUPPopperService().getGameEmulator(tableDetails.getEmulatorId()).getName());
      gameType.setText(tableDetails.getGameType() != null ? tableDetails.getGameType().name() : "-");
      gameName.setText(StringUtils.isEmpty(tableDetails.getGameName()) ? "-" : tableDetails.getGameName());
      gameFileName.setText(StringUtils.isEmpty(tableDetails.getGameFileName()) ? "-" : tableDetails.getGameFileName());
      gameVersion.setText(StringUtils.isEmpty(tableDetails.getFileVersion()) ? "-" : tableDetails.getFileVersion());
      gameDisplayName.setText(StringUtils.isEmpty(tableDetails.getGameDisplayName()) ? "-" : tableDetails.getGameDisplayName());
      gameTheme.setText(StringUtils.isEmpty(tableDetails.getGameTheme()) ? "-" : tableDetails.getGameTheme());
      dateAdded.setText(tableDetails.getDateAdded() == null ? "-" : DateFormat.getDateTimeInstance().format(tableDetails.getDateAdded()));
      gameYear.setText(tableDetails.getGameYear() == null ? "-" : String.valueOf(tableDetails.getGameYear()));
      romName.setText(StringUtils.isEmpty(tableDetails.getRomName()) ? "-" : tableDetails.getRomName());
      manufacturer.setText(StringUtils.isEmpty(tableDetails.getManufacturer()) ? "-" : tableDetails.getManufacturer());
      numberOfPlayers.setText(tableDetails.getNumberOfPlayers() == null ? "-" : String.valueOf(tableDetails.getNumberOfPlayers()));
      tags.setText(StringUtils.isEmpty(tableDetails.getTags()) ? "-" : tableDetails.getTags());
      category.setText(StringUtils.isEmpty(tableDetails.getCategory()) ? "-" : tableDetails.getCategory());
      author.setText(StringUtils.isEmpty(tableDetails.getAuthor()) ? "-" : tableDetails.getAuthor());
      launchCustomVar.setText(StringUtils.isEmpty(tableDetails.getLaunchCustomVar()) ? "-" : tableDetails.getLaunchCustomVar());
      keepDisplays.setText(StringUtils.isEmpty(tableDetails.getKeepDisplays()) ? "-" : tableDetails.getKeepDisplays());
      gameRating.setText(tableDetails.getGameRating() == null ? "-" : String.valueOf(tableDetails.getGameRating()));
      dof.setText(StringUtils.isEmpty(tableDetails.getDof()) ? "-" : tableDetails.getDof());
      IPDBNum.setText(StringUtils.isEmpty(tableDetails.getIPDBNum()) ? "-" : tableDetails.getIPDBNum());
      altRunMode.setText(StringUtils.isEmpty(tableDetails.getAltRunMode()) ? "-" : tableDetails.getAltRunMode());
      url.setText(StringUtils.isEmpty(tableDetails.getUrl()) ? "-" : tableDetails.getUrl());
      designedBy.setText(StringUtils.isEmpty(tableDetails.getDesignedBy()) ? "-" : tableDetails.getDesignedBy());
      notes.setText(StringUtils.isEmpty(tableDetails.getNotes()) ? "-" : tableDetails.getNotes());
    }
    else {
      launcherCombo.setValue(null);
      volumeSlider.setValue(100);

      labelLastPlayed.setText("-");
      labelTimesPlayed.setText("-");

      dateAdded.setText("-");
      gameName.setText("-");
      gameFileName.setText("-");
      gameDisplayName.setText("-");
      gameYear.setText("-");
      gameType.setText("-");
      gameTheme.setText("-");
      romName.setText("-");
      manufacturer.setText("-");
      numberOfPlayers.setText("-");
      tags.setText("-");
      category.setText("-");
      author.setText("-");
      launchCustomVar.setText("-");
      keepDisplays.setText("-");
      gameRating.setText("-");
      dof.setText("-");
      IPDBNum.setText("-");
      altRunMode.setText("-");
      url.setText("-");
      designedBy.setText("-");
      notes.setText("-");
    }
  }

  public void setSidebarController(TablesSidebarController tablesSidebarController) {
    this.tablesSidebarController = tablesSidebarController;
  }

  @Override
  public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
    if (game.isPresent()) {
      final GameRepresentation g = game.get();
      debouncer.debounce("tableVolume" + g.getId(), () -> {
        int value = newValue.intValue();
        if (value == 0) {
          value = 1;
        }

        if (tableDetails.getVolume() != null && Integer.parseInt(tableDetails.getVolume()) == value) {
          return;
        }

        tableDetails.setVolume(String.valueOf(value));
        LOG.info("Updates volume of " + g.getGameDisplayName() + " to " + value);
        try {
          this.tableDetails = Studio.client.getPinUPPopperService().saveTableDetails(tableDetails, g.getId());
        } catch (Exception e) {
          WidgetFactory.showAlert(Studio.stage, e.getMessage());
        }
      }, 500);
    }
  }

  static class TableStatus {
    private final int value;
    private final String label;

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