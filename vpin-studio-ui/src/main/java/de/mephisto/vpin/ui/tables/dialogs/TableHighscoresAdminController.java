package de.mephisto.vpin.ui.tables.dialogs;

import de.mephisto.vpin.commons.fx.DialogController;
import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.restclient.games.GameRepresentation;
import de.mephisto.vpin.restclient.highscores.HighscoreBackup;
import de.mephisto.vpin.restclient.util.ScoreFormatUtil;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.events.EventManager;
import de.mephisto.vpin.ui.tables.TablesSidebarController;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

import static de.mephisto.vpin.commons.utils.WidgetFactory.getScoreFontText;
import static de.mephisto.vpin.ui.Studio.client;

public class TableHighscoresAdminController implements Initializable, DialogController {
  private final static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  @FXML
  private VBox scorePane;

  @FXML
  private Label scoreLabel;

  @FXML
  private Button restoreBtn;

  @FXML
  private Button deleteBtn;

  @FXML
  private ListView<HighscoreBackup> backupList;

  private GameRepresentation game;
  private TablesSidebarController tablesSidebarController;

  @FXML
  private void onRestore(ActionEvent e) {
    Stage stage = (Stage) ((Button) e.getSource()).getScene().getWindow();

    HighscoreBackup selectedItem = backupList.getSelectionModel().getSelectedItem();
    if(selectedItem != null) {
      Optional<ButtonType> result = WidgetFactory.showConfirmation(stage, "Restore selected highscore for table \"" + game.getGameDisplayName() + "\"?");
      if (result.isPresent() && result.get().equals(ButtonType.OK)) {
        try {
          String rom = this.game.getRom();
          if(StringUtils.isEmpty(rom)) {
            rom = this.game.getTableName();
          }
          client.getHigscoreBackupService().restore(game.getId(), selectedItem.getFilename());
          EventManager.getInstance().notifyTableChange(this.game.getId(), rom);
        } catch (Exception ex) {
          LOG.error("Failed to restore highscore: " + ex.getMessage(), ex);
          WidgetFactory.showAlert(Studio.stage, "Error", "Failed to restore highscore backup: " + ex.getMessage() );
        }
      }
    }
  }

  @FXML
  private void onDelete(ActionEvent e) {
    Stage stage = (Stage) ((Button) e.getSource()).getScene().getWindow();

    HighscoreBackup selectedItem = backupList.getSelectionModel().getSelectedItem();
    if (selectedItem != null) {
      Optional<ButtonType> result = WidgetFactory.showConfirmation(stage, "Delete backup from " + selectedItem+ "?");
      if (result.isPresent() && result.get().equals(ButtonType.OK)) {
        String rom = this.game.getRom();
        if(StringUtils.isEmpty(rom)) {
          rom = this.game.getTableName();
        }
        client.getHigscoreBackupService().delete(rom, selectedItem.getFilename());
        EventManager.getInstance().notifyTableChange(this.game.getId(), rom);
        refresh();
      }
    }
  }

  @FXML
  private void onCancel(ActionEvent e) {
    Stage stage = (Stage) ((Button) e.getSource()).getScene().getWindow();
    stage.close();
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    this.deleteBtn.setDisable(true);
    this.restoreBtn.setDisable(true);
    scoreLabel.setFont(getScoreFontText());

    this.backupList.getSelectionModel().selectedItemProperty().addListener((observableValue, highscoreBackup, t1) -> {
      deleteBtn.setDisable(t1 == null);
      restoreBtn.setDisable(t1 == null);

      scoreLabel.setText("");

      if(t1 != null) {
        String raw = ScoreFormatUtil.formatRaw(t1.getRaw());
        scoreLabel.setText(raw);
      }
      else {
        scoreLabel.setText("No score selected.");
      }
    });
  }

  @Override
  public void onDialogCancel() {
  }


  public void setGame(GameRepresentation game) {
    this.game = game;
    refresh();
  }

  private void refresh() {
    List<HighscoreBackup> highscoreBackups = client.getHigscoreBackupService().get(game.getRom());
    backupList.setItems(FXCollections.observableList(highscoreBackups));
    scoreLabel.setText("");

    if(!highscoreBackups.isEmpty()) {
      this.backupList.getSelectionModel().select(0);
    }
    else {
      scoreLabel.setText("No backups found.");
    }
  }

  public void setTableSidebarController(TablesSidebarController tablesSidebarController) {
    this.tablesSidebarController = tablesSidebarController;
  }
}
