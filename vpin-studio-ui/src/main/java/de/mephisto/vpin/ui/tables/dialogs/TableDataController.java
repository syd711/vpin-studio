package de.mephisto.vpin.ui.tables.dialogs;

import de.mephisto.vpin.commons.fx.DialogController;
import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.restclient.popper.GameType;
import de.mephisto.vpin.restclient.popper.TableDetails;
import de.mephisto.vpin.restclient.tables.GameRepresentation;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.events.EventManager;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.Arrays;
import java.util.ResourceBundle;

public class TableDataController implements Initializable, DialogController {
  private final static Logger LOG = LoggerFactory.getLogger(TableDataController.class);

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
  private TextField keepDisplays;

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
  private TextField notes;

  private GameRepresentation game;
  private TableDetails manifest;

  @FXML
  private void onSaveClick(ActionEvent e) {
    Stage stage = (Stage) ((Button) e.getSource()).getScene().getWindow();
    stage.close();

    try {
      manifest = Studio.client.getPinUPPopperService().saveTableDetails(this.manifest, game.getId());
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

  }

  @Override
  public void onDialogCancel() {

  }

  public void setGame(GameRepresentation game) {
    this.game = game;
    this.titleLabel.setText("Table Data of '" + game.getGameDisplayName() + "'");

    manifest = Studio.client.getPinUPPopperService().getTableDetails(game.getId());

    gameName.setText(manifest.getGameName());
    gameFileName.setText(manifest.getGameFileName());
    gameFileName.textProperty().addListener((observable, oldValue, newValue) -> manifest.setGameFileName(newValue));
    gameDisplayName.setText(manifest.getGameDisplayName());
    gameDisplayName.textProperty().addListener((observable, oldValue, newValue) -> manifest.setGameDisplayName(newValue.trim()));
    gameTheme.setText(manifest.getGameTheme());
    gameTheme.textProperty().addListener((observable, oldValue, newValue) -> manifest.setGameTheme(newValue));
    gameVersion.setText(manifest.getGameVersion());
    gameVersion.textProperty().addListener((observable, oldValue, newValue) -> manifest.setGameVersion(newValue));

    gameTypeCombo.setItems(FXCollections.observableList(Arrays.asList(GameType.values())));
    GameType gt = manifest.getGameType();
    if(gt != null) {
      gameTypeCombo.valueProperty().setValue(gt);
    }
    gameTypeCombo.valueProperty().addListener((observableValue, gameType, t1) -> manifest.setGameType(t1));

    gameYear.textProperty().addListener((observable, oldValue, newValue) -> {
      if (!newValue.matches("\\d*")) {
        gameYear.setText(newValue.replaceAll("[^\\d]", ""));
      }

      if (gameYear.getText().length() > 4) {
        String s = gameYear.getText().substring(0, 4);
        gameYear.setText(s);
      }
    });
    if (manifest.getGameYear() != null && manifest.getGameYear() > 0) {
      gameYear.setText(String.valueOf(manifest.getGameYear()));
    }
    gameYear.textProperty().addListener((observable, oldValue, newValue) -> {
      if (newValue.length() > 0) {
        manifest.setGameYear(Integer.parseInt(newValue));
      }
      else {
        manifest.setGameYear(0);
      }
    });

    romName.setText(manifest.getRomName());
    romName.textProperty().addListener((observable, oldValue, newValue) -> manifest.setRomName(newValue));

    manufacturer.setText(manifest.getManufacturer());
    manufacturer.textProperty().addListener((observable, oldValue, newValue) -> manifest.setManufacturer(newValue));

    SpinnerValueFactory.IntegerSpinnerValueFactory factory = new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 4, 0);
    numberOfPlayers.setValueFactory(factory);
    if (manifest.getNumberOfPlayers() != null) {
      numberOfPlayers.getValueFactory().setValue(manifest.getNumberOfPlayers());
    }
    numberOfPlayers.getValueFactory().valueProperty().addListener((observable, oldValue, newValue) -> manifest.setNumberOfPlayers(Integer.parseInt(String.valueOf(newValue))));

    tags.setText(manifest.getTags());
    tags.textProperty().addListener((observable, oldValue, newValue) -> manifest.setTags(newValue));

    category.setText(manifest.getCategory());
    category.textProperty().addListener((observable, oldValue, newValue) -> manifest.setCategory(newValue));

    author.setText(manifest.getAuthor());
    author.textProperty().addListener((observable, oldValue, newValue) -> manifest.setAuthor(newValue));

    launchCustomVar.setText(manifest.getLaunchCustomVar());
    launchCustomVar.textProperty().addListener((observable, oldValue, newValue) -> manifest.setLaunchCustomVar(newValue));

    keepDisplays.setText(manifest.getKeepDisplays());
    keepDisplays.textProperty().addListener((observable, oldValue, newValue) -> manifest.setKeepDisplays(newValue));

    SpinnerValueFactory.IntegerSpinnerValueFactory ratingFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 10, 0);
    gameRating.setValueFactory(ratingFactory);
    if (manifest.getGameRating() != null) {
      gameRating.getValueFactory().setValue(manifest.getGameRating());
    }
    gameRating.getValueFactory().valueProperty().addListener((observable, oldValue, newValue) -> manifest.setGameRating(Integer.parseInt(String.valueOf(newValue))));

    dof.setText(manifest.getDof());
    dof.textProperty().addListener((observable, oldValue, newValue) -> manifest.setDof(newValue));

    IPDBNum.setText(manifest.getIPDBNum());
    IPDBNum.textProperty().addListener((observable, oldValue, newValue) -> manifest.setIPDBNum(newValue));

    altRunMode.setText(manifest.getAltRunMode());
    altRunMode.textProperty().addListener((observable, oldValue, newValue) -> manifest.setAltRunMode(newValue));

    url.setText(manifest.getUrl());
    url.textProperty().addListener((observable, oldValue, newValue) -> manifest.setUrl(newValue));

    designedBy.setText(manifest.getDesignedBy());
    designedBy.textProperty().addListener((observable, oldValue, newValue) -> manifest.setDesignedBy(newValue));

    notes.setText(manifest.getNotes());
    notes.textProperty().addListener((observable, oldValue, newValue) -> manifest.setNotes(newValue));
  }
}
