package de.mephisto.vpin.ui.tables.dialogs;

import de.mephisto.vpin.commons.fx.DialogController;
import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.restclient.popper.GameType;
import de.mephisto.vpin.restclient.popper.TableDetails;
import de.mephisto.vpin.restclient.tables.GameRepresentation;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.events.EventManager;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
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

    custom2.setText(manifest.getCustom2());
    custom2.textProperty().addListener((observable, oldValue, newValue) -> manifest.setCustom2(newValue));

    custom3.setText(manifest.getCustom3());
    custom3.textProperty().addListener((observable, oldValue, newValue) -> manifest.setCustom3(newValue));

    extrasTab.setDisable(manifest.getSqlVersion() < 64);
    if(extrasTab.isDisable()) {
      return;
    }

    custom4.setText(manifest.getCustom4());
    custom4.textProperty().addListener((observable, oldValue, newValue) -> manifest.setCustom4(newValue));

    custom5.setText(manifest.getCustom5());
    custom5.textProperty().addListener((observable, oldValue, newValue) -> manifest.setCustom5(newValue));

    altRomName.setText(manifest.getRomAlt());
    altRomName.textProperty().addListener((observable, oldValue, newValue) -> manifest.setRomAlt(newValue));

    webDbId.setText(manifest.getWebGameId());
    webDbId.textProperty().addListener((observable, oldValue, newValue) -> manifest.setWebGameId(newValue));

    webLink.setText(manifest.getWebLink2Url());
    webLink.textProperty().addListener((observable, oldValue, newValue) -> manifest.setWebLink2Url(newValue));

    tourneyId.setText(manifest.getTourneyId());
    tourneyId.textProperty().addListener((observable, oldValue, newValue) -> manifest.setTourneyId(newValue));

    modCheckbox.setSelected(manifest.isMod());
    modCheckbox.selectedProperty().addListener((observableValue, aBoolean, t1) -> manifest.setMod(t1));

    gDetails.setText(manifest.getgDetails());
    gDetails.textProperty().addListener((observableValue, oldValue, newValue) -> manifest.setgDetails(newValue));

    gLog.setText(manifest.getgLog());
    gLog.textProperty().addListener((observableValue, oldValue, newValue) -> manifest.setgLog(newValue));

    gPlayLog.setText(manifest.getgPlayLog());
    gPlayLog.textProperty().addListener((observableValue, oldValue, newValue) -> manifest.setgPlayLog(newValue));

    gNotes.setText(manifest.getgNotes());
    gNotes.textProperty().addListener((observableValue, oldValue, newValue) -> manifest.setgNotes(newValue));
  }
}
