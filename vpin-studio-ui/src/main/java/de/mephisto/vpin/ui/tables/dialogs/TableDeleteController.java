package de.mephisto.vpin.ui.tables.dialogs;

import de.mephisto.vpin.commons.fx.DialogController;
import de.mephisto.vpin.restclient.descriptors.DeleteDescriptor;
import de.mephisto.vpin.restclient.representations.GameRepresentation;
import de.mephisto.vpin.ui.Studio;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class TableDeleteController implements Initializable, DialogController {
  private final static Logger LOG = LoggerFactory.getLogger(TableDeleteController.class);

  @FXML
  private Label titleLabel;

  @FXML
  private Button deleteBtn;

  @FXML
  private CheckBox deleteAllCheckbox;

  @FXML
  private CheckBox vpxFileCheckbox;

  @FXML
  private CheckBox directb2sCheckbox;

  @FXML
  private CheckBox popperCheckbox;

  @FXML
  private CheckBox confirmationCheckbox;

  @FXML
  private CheckBox pupPackCheckbox;

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

  private boolean result = false;
  private List<GameRepresentation> games;

  @FXML
  private void onDeleteClick(ActionEvent e) {
    Stage stage = (Stage) ((Button) e.getSource()).getScene().getWindow();

    DeleteDescriptor descriptor = new DeleteDescriptor();
    descriptor.setDeleteTable(vpxFileCheckbox.isSelected());
    descriptor.setDeleteDirectB2s(directb2sCheckbox.isSelected());
    descriptor.setDeleteFromPopper(popperCheckbox.isSelected());
    descriptor.setDeletePupPack(pupPackCheckbox.isSelected());
    descriptor.setDeleteDMDs(dmdCheckbox.isSelected());
    descriptor.setDeleteHighscores(highscoreCheckbox.isSelected());
    descriptor.setDeleteMusic(musicCheckbox.isSelected());
    descriptor.setDeleteAltSound(altSoundCheckbox.isSelected());
    descriptor.setDeleteAltColor(altColorCheckbox.isSelected());
    descriptor.setDeleteCfg(mameConfigCheckbox.isSelected());
    descriptor.setGameIds(games.stream().map(GameRepresentation::getId).collect(Collectors.toList()));

    Studio.client.getGameService().deleteGame(descriptor);
    result = true;
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
    this.deleteBtn.setDisable(true);
    confirmationCheckbox.selectedProperty().addListener((observable, oldValue, newValue) -> deleteBtn.setDisable(!newValue));

    deleteAllCheckbox.selectedProperty().addListener((observable, oldValue, newValue) -> {
      vpxFileCheckbox.setSelected(newValue);
      directb2sCheckbox.setSelected(newValue);
      popperCheckbox.setSelected(newValue);
      pupPackCheckbox.setSelected(newValue);
      dmdCheckbox.setSelected(newValue);
      musicCheckbox.setSelected(newValue);
      mameConfigCheckbox.setSelected(newValue);
      highscoreCheckbox.setSelected(newValue);
      altSoundCheckbox.setSelected(newValue);
      altColorCheckbox.setSelected(newValue);
    });
  }

  @Override
  public void onDialogCancel() {
    result = false;
  }

  public boolean tableDeleted() {
    return result;
  }

  public void setGames(List<GameRepresentation> selectedGames, List<GameRepresentation> allGames) {
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
    for (GameRepresentation selectedGame : selectedGames) {
      boolean hasNoArchives = Studio.client.getArchiveService().getArchiveDescriptorsForGame(selectedGame.getId()).isEmpty();
      if (hasNoArchives) {
        this.validationContainer.setVisible(true);
        this.validationDescription.setVisible(true);
        return;
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
        List<GameRepresentation> variants = allGames.stream().filter(g -> rom.equals(g.getRom())).collect(Collectors.toList());
        for (GameRepresentation variant : variants) {
          if (!selectedGames.contains(variant)) {
            hasNonSelectedVariant = true;
            this.validationContainer.setVisible(true);
            this.validationTitle.setVisible(true);
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
