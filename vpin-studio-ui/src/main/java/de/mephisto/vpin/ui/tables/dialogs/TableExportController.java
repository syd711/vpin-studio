package de.mephisto.vpin.ui.tables.dialogs;

import de.mephisto.vpin.commons.fx.DialogController;
import de.mephisto.vpin.restclient.representations.GameRepresentation;
import de.mephisto.vpin.restclient.representations.VpaManifestRepresentation;
import de.mephisto.vpin.ui.Studio;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.Date;
import java.util.ResourceBundle;

public class TableExportController implements Initializable, DialogController {
  private final static Logger LOG = LoggerFactory.getLogger(TableExportController.class);

  @FXML
  private Label titleLabel;

  @FXML
  private TextField gameName;

  @FXML
  private TextField gameFileName;

  @FXML
  private TextField gameDisplayName;

  @FXML
  private Spinner gameYear;

  @FXML
  private TextField romName;

  @FXML
  private TextField romUrl;

  @FXML
  private TextField manufacturer;

  @FXML
  private Spinner numberOfPlayers;

  @FXML
  private TextField tags;

  @FXML
  private TextField category;

  @FXML
  private TextField author;

  @FXML
  private Spinner volume;

  @FXML
  private TextField launchCustomVar;

  @FXML
  private TextField keepDisplays;

  @FXML
  private Spinner gameRating;

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
  private Button exportBtn;

  @FXML
  private CheckBox exportRomCheckbox;

  @FXML
  private CheckBox exportPupPackCheckbox;

  @FXML
  private TextField notes;

  private boolean result = false;
  private GameRepresentation game;
  private VpaManifestRepresentation manifest;

  @FXML
  private void onExportClick(ActionEvent e) {
    Stage stage = (Stage) ((Button) e.getSource()).getScene().getWindow();
    stage.close();
  }

  @FXML
  private void onCancelClick(ActionEvent e) {
    Stage stage = (Stage) ((Button) e.getSource()).getScene().getWindow();
    stage.close();
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    this.result = false;
  }

  @Override
  public void onDialogCancel() {
    result = false;
  }

  public boolean tableDeleted() {
    return result;
  }

  public void setGame(GameRepresentation game) {
    this.game = game;
    this.titleLabel.setText("Export of '" + game.getGameDisplayName() + "'");

    manifest = Studio.client.getVpaManifest(game.getId());

    exportRomCheckbox.setSelected(true);
    exportPupPackCheckbox.setSelected(true);

    gameName.setText(manifest.getGameName());
    gameFileName.setText(manifest.getGameFileName());
    gameDisplayName.setText(manifest.getGameDisplayName());

    SpinnerValueFactory.IntegerSpinnerValueFactory yearFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(1900, 2500, new Date().getYear());
    gameYear.setValueFactory(yearFactory);
    if(manifest.getGameYear() > 1900) {
      gameYear.getValueFactory().setValue(manifest.getGameYear());
    }
    romName.setText(manifest.getRomName());
    romUrl.setText(manifest.getRomUrl());
    manufacturer.setText(manifest.getManufacturer());

    SpinnerValueFactory.IntegerSpinnerValueFactory factory = new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 4, 1);
    numberOfPlayers.setValueFactory(factory);
    numberOfPlayers.getValueFactory().setValue(manifest.getNumberOfPlayers());
    tags.setText(manifest.getTags());
    category.setText(manifest.getCategory());
    author.setText(manifest.getAuthor());

    SpinnerValueFactory.IntegerSpinnerValueFactory volumeFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 100, 0);
    volume.setValueFactory(volumeFactory);
    volume.getValueFactory().setValue(manifest.getVolume());

    launchCustomVar.setText(manifest.getLaunchCustomVar());
    keepDisplays.setText(manifest.getKeepDisplays());

    SpinnerValueFactory.IntegerSpinnerValueFactory ratingFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 10, 0);
    gameRating.setValueFactory(ratingFactory);
    gameRating.getValueFactory().setValue(manifest.getGameRating());
    dof.setText(manifest.getDof());
    IPDBNum.setText(manifest.getIPDBNum());
    altRunMode.setText(manifest.getAltRunMode());
    url.setText(manifest.getUrl());
    designedBy.setText(manifest.getUrl());
  }
}
