package de.mephisto.vpin.ui.tables.dialogs;

import de.mephisto.vpin.commons.fx.DialogController;
import de.mephisto.vpin.restclient.games.GameRepresentation;
import de.mephisto.vpin.restclient.highscores.HighscoreType;
import de.mephisto.vpin.restclient.highscores.NVRamList;
import de.mephisto.vpin.ui.util.ProgressDialog;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

import static de.mephisto.vpin.ui.Studio.client;

public class HighscoreResetController implements Initializable, DialogController {

  private List<GameRepresentation> games;

  @FXML
  private Label title;

  @FXML
  private Label multiNVRamLabel;

  @FXML
  private Label singleNVRamLabel;

  @FXML
  private Label singleNVRamNoResetLabel;

  @FXML
  private VBox scoreValueBox;

  @FXML
  private TextField scoreField;

  @FXML
  private Button okBtn;

  private NVRamList nvRamList;

  @Override
  public void onDialogCancel() {

  }


  @FXML
  private void onCancelClick(ActionEvent e) {
    Stage stage = (Stage) ((Button) e.getSource()).getScene().getWindow();
    stage.close();
  }

  @FXML
  private void onSaveClick(ActionEvent e) {
    String value = scoreField.getText();
    long score = 99;
    try {
      score = Long.parseLong(value);
    }
    catch (NumberFormatException ex) {
      //ignore
    }

    HighscoreResetProgressModel highscoreResetProgressModel = new HighscoreResetProgressModel(this.games, score);
    Platform.runLater(() -> {
      Stage stage = (Stage) ((Button) e.getSource()).getScene().getWindow();
      stage.close();
    });

    ProgressDialog.createProgressDialog(highscoreResetProgressModel);
  }

  public void setGames(List<GameRepresentation> games) {
    this.games = games;
    if (games.size() == 1) {
      GameRepresentation game = games.get(0);

      boolean nvRamScore = HighscoreType.NVRam.name().equals(game.getHighscoreType());
      this.multiNVRamLabel.setVisible(false);
      scoreValueBox.setVisible(!nvRamScore);
      singleNVRamLabel.setVisible(nvRamScore);

      if (nvRamScore) {
        boolean resettedRomAvailable = this.nvRamList.getEntries().stream().anyMatch(rom -> rom.equalsIgnoreCase(game.getRom()));
        this.singleNVRamLabel.setVisible(resettedRomAvailable);
        this.singleNVRamNoResetLabel.setVisible(!resettedRomAvailable);
      }
      title.setText("Reset the highscores of \"" + game.getGameDisplayName() + "\"?");
    }
    else {
      multiNVRamLabel.setVisible(true);
      scoreValueBox.setVisible(this.games.stream().anyMatch(g -> !HighscoreType.NVRam.name().equals(g.getHighscoreType())));
      title.setText("Reset the highscores of " + games.size() + " tables?");
    }
    this.scoreField.setText("99");
  }

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    this.nvRamList = client.getNvRamsService().getResettedNVRams();

    this.scoreField.textProperty().addListener(new ChangeListener<String>() {
      @Override
      public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
        if (!newValue.matches("\\d*")) {
          scoreField.setText(newValue.replaceAll("[^\\d]", ""));
        }
      }
    });

    scoreValueBox.managedProperty().bindBidirectional(scoreValueBox.visibleProperty());
    multiNVRamLabel.managedProperty().bindBidirectional(multiNVRamLabel.visibleProperty());
    singleNVRamLabel.managedProperty().bindBidirectional(singleNVRamLabel.visibleProperty());
    singleNVRamNoResetLabel.managedProperty().bindBidirectional(singleNVRamNoResetLabel.visibleProperty());

    multiNVRamLabel.setVisible(false);
    singleNVRamLabel.setVisible(false);
    singleNVRamNoResetLabel.setVisible(false);
  }
}
