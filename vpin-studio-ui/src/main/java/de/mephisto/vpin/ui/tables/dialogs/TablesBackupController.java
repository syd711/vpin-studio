package de.mephisto.vpin.ui.tables.dialogs;

import de.mephisto.vpin.commons.fx.DialogController;
import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.restclient.BackupDescriptor;
import de.mephisto.vpin.restclient.representations.GameRepresentation;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.jobs.JobPoller;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class TablesBackupController implements Initializable, DialogController {

  @FXML
  private Label titleLabel;

  @FXML
  private CheckBox exportRomCheckbox;

  @FXML
  private CheckBox exportPupPackCheckbox;

  @FXML
  private CheckBox exportPopperMedia;

  @FXML
  private CheckBox exportMusic;

  @FXML
  private CheckBox highscoresCheckbox;

  private List<GameRepresentation> games;

  @FXML
  private void onExportClick(ActionEvent e) throws Exception {
    BackupDescriptor descriptor = new BackupDescriptor();
    descriptor.setExportPupPack(this.exportPupPackCheckbox.isSelected());
    descriptor.setExportRom(this.exportRomCheckbox.isSelected());
    descriptor.setExportPopperMedia(this.exportPopperMedia.isSelected());
    descriptor.setExportHighscores(this.highscoresCheckbox.isSelected());
    descriptor.getGameIds().addAll(games.stream().map(GameRepresentation::getId).collect(Collectors.toList()));
    Studio.client.exportArchive(descriptor);

    Stage stage = (Stage) ((Button) e.getSource()).getScene().getWindow();
    stage.close();

    new Thread(() -> {
      try {
        Thread.sleep(1000);
      } catch (InterruptedException ex) {
        //ignore
      }
      Platform.runLater(() -> {
        JobPoller.getInstance().setPolling();
      });
    }).start();

    Platform.runLater(() -> {
      WidgetFactory.showInformation(Studio.stage, "Backup Started", "The backup of " + games.size() + " tables has been started.", "The archived state will update once the backup is finished.");
    });
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

  public void setGames(List<GameRepresentation> games) {
    this.games = games;
    if(games.size() == 1) {
      this.titleLabel.setText("Export of \"" + games.get(0).getGameDisplayName() + "\"");
    }
    else {
      this.titleLabel.setText("Export of " + games.size() + " tables");
    }

    exportRomCheckbox.setSelected(true);
    exportPupPackCheckbox.setSelected(true);
  }
}
