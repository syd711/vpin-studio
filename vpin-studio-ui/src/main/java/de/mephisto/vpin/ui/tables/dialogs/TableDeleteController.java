package de.mephisto.vpin.ui.tables.dialogs;

import de.mephisto.vpin.commons.fx.DialogController;
import de.mephisto.vpin.commons.utils.localsettings.LocalUISettings;
import de.mephisto.vpin.restclient.games.descriptors.DeleteDescriptor;
import de.mephisto.vpin.restclient.games.GameRepresentation;
import de.mephisto.vpin.ui.events.EventManager;
import de.mephisto.vpin.ui.tables.TableDeleteProgressModel;
import de.mephisto.vpin.ui.tables.TableOverviewController;
import de.mephisto.vpin.ui.util.ProgressDialog;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

import static de.mephisto.vpin.ui.Studio.Features;
import static de.mephisto.vpin.ui.Studio.client;

public class TableDeleteController implements Initializable, DialogController {
  private final static Logger LOG = LoggerFactory.getLogger(TableDeleteController.class);

  @FXML
  private Label titleLabel;

  @FXML
  private Button deleteBtn;

  @FXML
  private CheckBox deleteAllCheckbox;

  @FXML
  private CheckBox keepAssetsCheckbox;

  @FXML
  private CheckBox vpxFileCheckbox;

  @FXML
  private CheckBox directb2sCheckbox;

  @FXML
  private CheckBox vbsCheckbox;

  @FXML
  private CheckBox bamCfgCheckbox;

  @FXML
  private CheckBox iniCheckbox;

  @FXML
  private CheckBox resCheckbox;

  @FXML
  private CheckBox povCheckbox;

  @FXML
  private CheckBox frontendCheckbox;

  @FXML
  private CheckBox confirmationCheckbox;

  @FXML
  private CheckBox pupPackCheckbox;

  @FXML
  private CheckBox pinVolCheckbox;

  @FXML
  private CheckBox musicCheckbox;

  @FXML
  private CheckBox mameConfigCheckbox;

  @FXML
  private CheckBox highscoreCheckbox;

  @FXML
  private CheckBox altSoundCheckbox;

  @FXML
  private CheckBox altColorCheckbox;

  @FXML
  private CheckBox dmdCheckbox;

  @FXML
  private Pane validationContainer;

  @FXML
  private Label validationTitle;

  @FXML
  private Label validationDescription;

  @FXML
  private VBox frontendSelectionField;

  private List<GameRepresentation> games;

  private TableOverviewController tableOverviewController;

  @FXML
  private void onDeleteClick(ActionEvent e) {
    Stage stage = (Stage) ((Button) e.getSource()).getScene().getWindow();

    DeleteDescriptor descriptor = new DeleteDescriptor();
    descriptor.setDeleteTable(vpxFileCheckbox.isSelected());
    descriptor.setDeleteDirectB2s(directb2sCheckbox.isSelected());
    descriptor.setDeleteFromFrontend(frontendCheckbox.isSelected());
    descriptor.setDeletePupPack(pupPackCheckbox.isSelected());
    descriptor.setDeleteDMDs(dmdCheckbox.isSelected());
    descriptor.setDeleteHighscores(highscoreCheckbox.isSelected());
    descriptor.setDeleteMusic(musicCheckbox.isSelected());
    descriptor.setDeleteAltSound(altSoundCheckbox.isSelected());
    descriptor.setDeleteAltColor(altColorCheckbox.isSelected());
    descriptor.setDeleteCfg(mameConfigCheckbox.isSelected());
    descriptor.setDeletePov(povCheckbox.isSelected());
    descriptor.setDeleteIni(iniCheckbox.isSelected());
    descriptor.setDeleteRes(resCheckbox.isSelected());
    descriptor.setDeleteVbs(vbsCheckbox.isSelected());
    descriptor.setDeletePinVol(pinVolCheckbox.isSelected());
    descriptor.setDeleteBAMCfg(bamCfgCheckbox.isSelected());
    descriptor.setKeepAssets(keepAssetsCheckbox.isSelected());
    descriptor.setGameIds(games.stream().map(GameRepresentation::getId).collect(Collectors.toList()));

    LocalUISettings.saveJsonProperty(this.getClass().getSimpleName(), descriptor);

    Platform.runLater(() -> {
      ProgressDialog.createProgressDialog(new TableDeleteProgressModel(tableOverviewController, descriptor));
      for (GameRepresentation game : games) {
        EventManager.getInstance().notifyTableChange(game.getId(), game.getRom());
      }
    });

    stage.close();
  }

  @FXML
  private void onCancelClick(ActionEvent e) {
    Stage stage = (Stage) ((Button) e.getSource()).getScene().getWindow();
    stage.close();
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    pupPackCheckbox.managedProperty().bindBidirectional(pupPackCheckbox.visibleProperty());
    validationContainer.managedProperty().bindBidirectional(validationContainer.visibleProperty());
    validationDescription.managedProperty().bindBidirectional(validationDescription.visibleProperty());
    validationTitle.managedProperty().bindBidirectional(validationTitle.visibleProperty());

    this.frontendSelectionField.setVisible(!Features.IS_STANDALONE);
    this.pupPackCheckbox.setVisible(Features.PUPPACKS_ENABLED);

    this.deleteBtn.setDisable(true);

    confirmationCheckbox.selectedProperty().addListener((observable, oldValue, newValue) -> deleteBtn.setDisable(!newValue));

    deleteAllCheckbox.selectedProperty().addListener((observable, oldValue, newValue) -> {
      vpxFileCheckbox.setSelected(newValue);
      directb2sCheckbox.setSelected(newValue);
      frontendCheckbox.setSelected(newValue);
      pupPackCheckbox.setSelected(newValue);
      dmdCheckbox.setSelected(newValue);
      musicCheckbox.setSelected(newValue);
      mameConfigCheckbox.setSelected(newValue);
      highscoreCheckbox.setSelected(newValue);
      altSoundCheckbox.setSelected(newValue);
      altColorCheckbox.setSelected(newValue);
      vbsCheckbox.setSelected(newValue);
      iniCheckbox.setSelected(newValue);
      resCheckbox.setSelected(newValue);
      povCheckbox.setSelected(newValue);
      pinVolCheckbox.setSelected(newValue);
      bamCfgCheckbox.setSelected(newValue);
    });

    frontendCheckbox.selectedProperty().addListener(new ChangeListener<Boolean>() {
      @Override
      public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
        keepAssetsCheckbox.setDisable(!newValue);
      }
    });

    DeleteDescriptor savedSettings = LocalUISettings.getJsonProperty(this.getClass().getSimpleName(), DeleteDescriptor.class, new DeleteDescriptor());
    vpxFileCheckbox.setSelected(savedSettings.isDeleteTable());
    directb2sCheckbox.setSelected(savedSettings.isDeleteDirectB2s());
    frontendCheckbox.setSelected(savedSettings.isDeleteFromFrontend());
    pupPackCheckbox.setSelected(savedSettings.isDeletePupPack());
    dmdCheckbox.setSelected(savedSettings.isDeleteDMDs());
    musicCheckbox.setSelected(savedSettings.isDeleteMusic());
    mameConfigCheckbox.setSelected(savedSettings.isDeleteCfg());
    highscoreCheckbox.setSelected(savedSettings.isDeleteHighscores());
    altSoundCheckbox.setSelected(savedSettings.isDeleteAltSound());
    altColorCheckbox.setSelected(savedSettings.isDeleteAltColor());
    vbsCheckbox.setSelected(savedSettings.isDeleteVbs());
    iniCheckbox.setSelected(savedSettings.isDeleteIni());
    resCheckbox.setSelected(savedSettings.isDeleteRes());
    povCheckbox.setSelected(savedSettings.isDeletePov());
    pinVolCheckbox.setSelected(savedSettings.isDeletePinVol());
    bamCfgCheckbox.setSelected(savedSettings.isDeleteBAMCfg());
  }

  @Override
  public void onDialogCancel() {

  }

  public void setGames(TableOverviewController tableOverviewController, List<GameRepresentation> selectedGames, List<GameRepresentation> allGames) {
    this.tableOverviewController = tableOverviewController;
    this.games = selectedGames;
    if (selectedGames.size() == 1) {
      this.titleLabel.setText("Delete \"" + selectedGames.get(0).getGameDisplayName() + "\"?");
    }
    else {
      this.titleLabel.setText("Delete " + selectedGames.size() + " Tables?");
    }

    this.validationContainer.setVisible(false);
    this.validationTitle.setVisible(false);
    this.validationDescription.setVisible(false);

    refreshVariantsCheck(selectedGames, allGames);
    refreshArchivesCheck(selectedGames, allGames);
  }

  private void refreshArchivesCheck(List<GameRepresentation> selectedGames, List<GameRepresentation> allGames) {
    if (Features.BACKUPS_ENABLED) {
      for (GameRepresentation selectedGame : selectedGames) {
        boolean hasNoArchives = client.getBackupService().getBackupsForGame(selectedGame.getId()).isEmpty();
        if (hasNoArchives) {
          this.validationContainer.setVisible(true);
          this.validationDescription.setVisible(true);
          return;
        }
      }
    }
  }

  private void refreshVariantsCheck(List<GameRepresentation> selectedGames, List<GameRepresentation> allGames) {
    boolean hasNonSelectedVariant = false;
    for (GameRepresentation selectedGame : selectedGames) {
      if (hasNonSelectedVariant) {
        break;
      }

      if (!StringUtils.isEmpty(selectedGame.getRom())) {
        String rom = selectedGame.getRom();
        List<GameRepresentation> variants = allGames.stream().filter(g -> rom.equalsIgnoreCase(g.getRom())).collect(Collectors.toList());
        for (GameRepresentation variant : variants) {
          if (!selectedGames.contains(variant)) {
            hasNonSelectedVariant = true;
            this.validationContainer.setVisible(true);
            this.validationTitle.setVisible(true);
            break;
          }
        }
      }
    }

    this.deleteAllCheckbox.setDisable(hasNonSelectedVariant);
    pupPackCheckbox.setDisable(hasNonSelectedVariant);
    dmdCheckbox.setDisable(hasNonSelectedVariant);
    musicCheckbox.setDisable(hasNonSelectedVariant);
    mameConfigCheckbox.setDisable(hasNonSelectedVariant);
    highscoreCheckbox.setDisable(hasNonSelectedVariant);
    altSoundCheckbox.setDisable(hasNonSelectedVariant);
    altColorCheckbox.setDisable(hasNonSelectedVariant);
  }
}
