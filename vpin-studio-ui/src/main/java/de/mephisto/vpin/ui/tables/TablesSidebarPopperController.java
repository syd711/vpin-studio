package de.mephisto.vpin.ui.tables;

import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.restclient.TableManifest;
import de.mephisto.vpin.restclient.VPinStudioClient;
import de.mephisto.vpin.restclient.representations.GameRepresentation;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.util.Dialogs;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.text.DateFormat;
import java.util.Optional;
import java.util.ResourceBundle;

import static de.mephisto.vpin.ui.util.BindingUtil.debouncer;

public class TablesSidebarPopperController implements Initializable, ChangeListener<Number> {
  private final static Logger LOG = LoggerFactory.getLogger(TablesSidebarPopperController.class);

  @FXML
  private Button tableEditBtn;

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
  private Label gameYear;

  @FXML
  private Label romName;

  @FXML
  private Label romUrl;

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


  private VPinStudioClient client;

  private Optional<GameRepresentation> game = Optional.empty();

  private TablesSidebarController tablesSidebarController;
  private TableManifest manifest;

  // Add a public no-args constructor
  public TablesSidebarPopperController() {
  }

  @FXML
  private void onTableEdit() {
    Dialogs.openTableDataDialog(this.game.get());
    this.refreshView(this.game);
  }

  @FXML
  private void onScreenEdit() {
    Dialogs.openPopperScreensDialog(this.game.get());
    this.refreshView(this.game);
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    client = Studio.client;
  }


  public void setGame(Optional<GameRepresentation> game) {
    this.game = game;
    this.refreshView(game);
  }

  public void refreshView(Optional<GameRepresentation> g) {
    this.tableEditBtn.setDisable(g.isEmpty());
    volumeSlider.setDisable(g.isEmpty());

    if (g.isPresent()) {
      GameRepresentation game = g.get();
      labelLastPlayed.setText(game.getLastPlayed() != null ? DateFormat.getDateInstance().format(game.getLastPlayed()) : "-");
      labelTimesPlayed.setText(String.valueOf(game.getNumberPlays()));

      manifest = client.getTableManifest(game.getId());

      volumeSlider.valueProperty().removeListener(this);
      if (manifest.getVolume() != null) {
        volumeSlider.setValue(Integer.parseInt(manifest.getVolume()));
      }
      else {
        volumeSlider.setValue(100);
      }
      volumeSlider.valueProperty().addListener(this);

      gameName.setText(StringUtils.isEmpty(manifest.getGameName()) ? "-" : manifest.getGameName());
      gameFileName.setText(StringUtils.isEmpty(manifest.getGameFileName()) ? "-" : manifest.getGameFileName());
      gameDisplayName.setText(StringUtils.isEmpty(manifest.getGameDisplayName()) ? "-" : manifest.getGameDisplayName());
      gameYear.setText(manifest.getGameYear() == 0 ? "-" : String.valueOf(manifest.getGameYear()));
      romName.setText(StringUtils.isEmpty(manifest.getRomName()) ? "-" : manifest.getRomName());
      romUrl.setText(StringUtils.isEmpty(manifest.getRomUrl()) ? "-" : manifest.getRomUrl());
      manufacturer.setText(StringUtils.isEmpty(manifest.getManufacturer()) ? "-" : manifest.getManufacturer());
      numberOfPlayers.setText(manifest.getNumberOfPlayers() == 0 ? "-" : String.valueOf(manifest.getNumberOfPlayers()));
      tags.setText(StringUtils.isEmpty(manifest.getTags()) ? "-" : manifest.getTags());
      category.setText(StringUtils.isEmpty(manifest.getCategory()) ? "-" : manifest.getCategory());
      author.setText(StringUtils.isEmpty(manifest.getAuthor()) ? "-" : manifest.getAuthor());
      launchCustomVar.setText(StringUtils.isEmpty(manifest.getLaunchCustomVar()) ? "-" : manifest.getLaunchCustomVar());
      keepDisplays.setText(StringUtils.isEmpty(manifest.getKeepDisplays()) ? "-" : manifest.getKeepDisplays());
      gameRating.setText(manifest.getGameRating() > 0 ? String.valueOf(manifest.getGameRating()) : "-");
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

      gameName.setText("-");
      gameFileName.setText("-");
      gameDisplayName.setText("-");
      gameYear.setText("-");
      romName.setText("-");
      romUrl.setText("-");
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
          client.saveTableManifest(manifest);
        } catch (Exception e) {
          WidgetFactory.showAlert(Studio.stage, e.getMessage());
        }
      }, 500);
    }
  }
}