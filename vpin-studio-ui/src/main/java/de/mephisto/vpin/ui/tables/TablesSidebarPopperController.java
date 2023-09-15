package de.mephisto.vpin.ui.tables;

import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.restclient.popper.TableDetails;
import de.mephisto.vpin.restclient.representations.GameRepresentation;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.events.EventManager;
import de.mephisto.vpin.ui.util.Dialogs;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.text.DateFormat;
import java.util.Optional;
import java.util.ResourceBundle;

import static de.mephisto.vpin.ui.Studio.client;
import static de.mephisto.vpin.ui.util.BindingUtil.debouncer;

public class TablesSidebarPopperController implements Initializable, ChangeListener<Number> {
  private final static Logger LOG = LoggerFactory.getLogger(TablesSidebarPopperController.class);

  @FXML
  private Button tableEditBtn;

  @FXML
  private Button autoFillBtn;

  @FXML
  private Button editScreensBtn;

  @FXML
  private Slider volumeSlider;

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
  private TableDetails manifest;

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
      Optional<ButtonType> result = WidgetFactory.showConfirmation(Studio.stage, "Auto Fill Data for \"" + game.get().getGameDisplayName() + "\"?",
          "This fills missing entries with data taken from the table metadata and the Virtual Pinball Spreadsheet.", null, "Continue");
      if (result.isPresent() && result.get().equals(ButtonType.OK)) {
        try {
          client.getPinUPPopperService().autoFillTableDetails(this.game.get().getId());
          EventManager.getInstance().notifyTableChange(this.game.get().getId(), null);
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

  }


  public void setGame(Optional<GameRepresentation> game) {
    this.game = game;
    this.refreshView(game);
  }

  public void refreshView(Optional<GameRepresentation> g) {
    this.tableEditBtn.setDisable(g.isEmpty());
    this.editScreensBtn.setDisable(g.isEmpty());
    volumeSlider.setDisable(g.isEmpty());
    autoFillBtn.setDisable(g.isEmpty());

    if (g.isPresent()) {
      GameRepresentation game = g.get();
      labelLastPlayed.setText(game.getLastPlayed() != null ? DateFormat.getDateInstance().format(game.getLastPlayed()) : "-");
      labelTimesPlayed.setText(String.valueOf(game.getNumberPlays()));

      manifest = Studio.client.getPinUPPopperService().getTableDetails(game.getId());

      volumeSlider.valueProperty().removeListener(this);
      if (manifest.getVolume() != null) {
        volumeSlider.setValue(Integer.parseInt(manifest.getVolume()));
      }
      else {
        volumeSlider.setValue(100);
      }
      volumeSlider.valueProperty().addListener(this);

      gameType.setText(manifest.getGameType() != null ? manifest.getGameType().name() : "-");
      gameName.setText(StringUtils.isEmpty(manifest.getGameName()) ? "-" : manifest.getGameName());
      gameFileName.setText(StringUtils.isEmpty(manifest.getGameFileName()) ? "-" : manifest.getGameFileName());
      gameVersion.setText(StringUtils.isEmpty(manifest.getFileVersion()) ? "-" : manifest.getFileVersion());
      gameDisplayName.setText(StringUtils.isEmpty(manifest.getGameDisplayName()) ? "-" : manifest.getGameDisplayName());
      gameTheme.setText(StringUtils.isEmpty(manifest.getGameTheme()) ? "-" : manifest.getGameTheme());
      dateAdded.setText(manifest.getDateAdded() == null ? "-" : DateFormat.getDateTimeInstance().format(manifest.getDateAdded()));
      gameYear.setText(manifest.getGameYear() == null ? "-" : String.valueOf(manifest.getGameYear()));
      romName.setText(StringUtils.isEmpty(manifest.getRomName()) ? "-" : manifest.getRomName());
      manufacturer.setText(StringUtils.isEmpty(manifest.getManufacturer()) ? "-" : manifest.getManufacturer());
      numberOfPlayers.setText(manifest.getNumberOfPlayers() == null ? "-" : String.valueOf(manifest.getNumberOfPlayers()));
      tags.setText(StringUtils.isEmpty(manifest.getTags()) ? "-" : manifest.getTags());
      category.setText(StringUtils.isEmpty(manifest.getCategory()) ? "-" : manifest.getCategory());
      author.setText(StringUtils.isEmpty(manifest.getAuthor()) ? "-" : manifest.getAuthor());
      launchCustomVar.setText(StringUtils.isEmpty(manifest.getLaunchCustomVar()) ? "-" : manifest.getLaunchCustomVar());
      keepDisplays.setText(StringUtils.isEmpty(manifest.getKeepDisplays()) ? "-" : manifest.getKeepDisplays());
      gameRating.setText(manifest.getGameRating() == null ? "-" : String.valueOf(manifest.getGameRating()));
      dof.setText(StringUtils.isEmpty(manifest.getDof()) ? "-" : manifest.getDof());
      IPDBNum.setText(StringUtils.isEmpty(manifest.getIPDBNum()) ? "-" : manifest.getIPDBNum());
      altRunMode.setText(StringUtils.isEmpty(manifest.getAltRunMode()) ? "-" : manifest.getAltRunMode());
      url.setText(StringUtils.isEmpty(manifest.getUrl()) ? "-" : manifest.getUrl());
      designedBy.setText(StringUtils.isEmpty(manifest.getDesignedBy()) ? "-" : manifest.getDesignedBy());
      notes.setText(StringUtils.isEmpty(manifest.getNotes()) ? "-" : manifest.getNotes());
    }
    else {
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

        if (manifest.getVolume() != null && Integer.parseInt(manifest.getVolume()) == value) {
          return;
        }

        manifest.setVolume(String.valueOf(value));
        LOG.info("Updates volume of " + g.getGameDisplayName() + " to " + value);
        try {
          Studio.client.getPinUPPopperService().saveTableDetails(manifest, g.getId());
        } catch (Exception e) {
          WidgetFactory.showAlert(Studio.stage, e.getMessage());
        }
      }, 500);
    }
  }
}