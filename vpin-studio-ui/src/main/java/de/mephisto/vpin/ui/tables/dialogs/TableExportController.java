package de.mephisto.vpin.ui.tables.dialogs;

import de.mephisto.vpin.commons.fx.DialogController;
import de.mephisto.vpin.commons.fx.OverlayWindowFX;
import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.restclient.ExportDescriptor;
import de.mephisto.vpin.restclient.PopperScreen;
import de.mephisto.vpin.restclient.VpaManifest;
import de.mephisto.vpin.restclient.representations.GameMediaItemRepresentation;
import de.mephisto.vpin.restclient.representations.GameMediaRepresentation;
import de.mephisto.vpin.restclient.representations.GameRepresentation;
import de.mephisto.vpin.ui.Studio;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

import java.io.ByteArrayInputStream;
import java.net.URL;
import java.util.ResourceBundle;

public class TableExportController implements Initializable, DialogController {

  @FXML
  private Label titleLabel;

  @FXML
  private TextField gameName;

  @FXML
  private TextField gameFileName;

  @FXML
  private TextField gameDisplayName;

  @FXML
  private TextField gameYear;

  @FXML
  private TextField romName;

  @FXML
  private TextField romUrl;

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
  private Spinner<Integer> volume;

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
  private ImageView imageView;

  @FXML
  private CheckBox exportRomCheckbox;

  @FXML
  private CheckBox exportPupPackCheckbox;

  @FXML
  private CheckBox exportPopperMedia;

  @FXML
  private CheckBox overwriteCheckbox;

  @FXML
  private TextField notes;

  private boolean result = false;
  private GameRepresentation game;
  private VpaManifest manifest;

  @FXML
  private void onExportClick(ActionEvent e) throws Exception {
    ExportDescriptor descriptor = new ExportDescriptor();
    descriptor.setManifest(manifest);
    descriptor.setGameId(game.getId());
    descriptor.setExportPupPack(this.exportPupPackCheckbox.isSelected());
    descriptor.setExportRom(this.exportRomCheckbox.isSelected());
    descriptor.setExportPopperMedia(this.exportPopperMedia.isSelected());
    descriptor.setOverwrite(this.overwriteCheckbox.isSelected());
    Studio.client.export(descriptor);

    Stage stage = (Stage) ((Button) e.getSource()).getScene().getWindow();
    stage.close();

    WidgetFactory.showInformation(Studio.stage, "Export Started", "The export of '" + game.getGameDisplayName() + "' has been started.", "The archived state will update once the export is finished.");
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

    GameMediaRepresentation gameMedia = game.getGameMedia();
    GameMediaItemRepresentation wheelMedia = gameMedia.getMedia().get(PopperScreen.Wheel.name());
    if (wheelMedia != null) {
      ByteArrayInputStream gameMediaItem = OverlayWindowFX.client.getGameMediaItem(game.getId(), PopperScreen.Wheel);
      Image image = new Image(gameMediaItem);
      imageView.setImage(image);
    }

    exportRomCheckbox.setSelected(true);
    exportPupPackCheckbox.setSelected(true);

    gameName.setText(manifest.getGameName());
    gameName.textProperty().addListener((observable, oldValue, newValue) -> manifest.setGameName(newValue));
    gameFileName.setText(manifest.getGameFileName());
    gameDisplayName.setText(manifest.getGameDisplayName());
    gameDisplayName.textProperty().addListener((observable, oldValue, newValue) -> manifest.setGameDisplayName(newValue));


    gameYear.textProperty().addListener((observable, oldValue, newValue) -> {
      if (!newValue.matches("\\d*")) {
        gameYear.setText(newValue.replaceAll("[^\\d]", ""));
      }

      if (gameYear.getText().length() > 4) {
        String s = gameYear.getText().substring(0, 4);
        gameYear.setText(s);
      }
    });
    if (manifest.getGameYear() > 0) {
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

    romUrl.setText(manifest.getRomUrl());
    romUrl.textProperty().addListener((observable, oldValue, newValue) -> manifest.setRomUrl(newValue));

    manufacturer.setText(manifest.getManufacturer());
    manufacturer.textProperty().addListener((observable, oldValue, newValue) -> manifest.setManufacturer(newValue));

    SpinnerValueFactory.IntegerSpinnerValueFactory factory = new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 4, 0);
    numberOfPlayers.setValueFactory(factory);
    if (manifest.getNumberOfPlayers() > 0) {
      numberOfPlayers.getValueFactory().setValue(manifest.getNumberOfPlayers());
    }
    numberOfPlayers.getValueFactory().valueProperty().addListener((observable, oldValue, newValue) -> manifest.setNumberOfPlayers(Integer.parseInt(String.valueOf(newValue))));

    tags.setText(manifest.getTags());
    tags.textProperty().addListener((observable, oldValue, newValue) -> manifest.setTags(newValue));

    category.setText(manifest.getCategory());
    category.textProperty().addListener((observable, oldValue, newValue) -> manifest.setCategory(newValue));

    author.setText(manifest.getAuthor());
    author.textProperty().addListener((observable, oldValue, newValue) -> manifest.setAuthor(newValue));

    SpinnerValueFactory.IntegerSpinnerValueFactory volumeFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 100, 0);
    volume.setValueFactory(volumeFactory);
    if (manifest.getVolume() > 0) {
      volume.getValueFactory().setValue(manifest.getVolume());
    }
    volume.getValueFactory().valueProperty().addListener((observable, oldValue, newValue) -> manifest.setVolume(Integer.parseInt(String.valueOf(newValue))));

    launchCustomVar.setText(manifest.getLaunchCustomVar());
    launchCustomVar.textProperty().addListener((observable, oldValue, newValue) -> manifest.setLaunchCustomVar(newValue));

    keepDisplays.setText(manifest.getKeepDisplays());
    keepDisplays.textProperty().addListener((observable, oldValue, newValue) -> manifest.setKeepDisplays(newValue));

    SpinnerValueFactory.IntegerSpinnerValueFactory ratingFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 10, 0);
    gameRating.setValueFactory(ratingFactory);
    if (manifest.getGameRating() > 0) {
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

    designedBy.setText(manifest.getUrl());
    designedBy.textProperty().addListener((observable, oldValue, newValue) -> manifest.setDesignedBy(newValue));

    notes.setText(manifest.getNotes());
    notes.textProperty().addListener((observable, oldValue, newValue) -> manifest.setNotes(newValue));
  }
}
