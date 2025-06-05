package de.mephisto.vpin.ui.tables.dialogs;

import de.mephisto.vpin.commons.fx.DialogController;
import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.restclient.altcolor.AltColor;
import de.mephisto.vpin.restclient.games.GameRepresentation;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

import static de.mephisto.vpin.commons.utils.WidgetFactory.getScoreFontText;
import static de.mephisto.vpin.ui.Studio.client;

public class TableAltColorAdminController implements Initializable, DialogController {
  private final static Logger LOG = LoggerFactory.getLogger(TableAltColorAdminController.class);

  @FXML
  private VBox scorePane;

  @FXML
  private Label scoreLabel;

  @FXML
  private Button restoreBtn;

  @FXML
  private Button deleteBtn;

  @FXML
  private ListView<String> backupList;

  private GameRepresentation game;
  private TablesSidebarController tablesSidebarController;

  @FXML
  private void onRestore(ActionEvent e) {
    Stage stage = (Stage) ((Button) e.getSource()).getScene().getWindow();

    String selectedItem = backupList.getSelectionModel().getSelectedItem();
    if (selectedItem != null) {
      Optional<ButtonType> result = WidgetFactory.showConfirmation(stage, "Restore selected highscore for table \"" + game.getGameDisplayName() + "\"?", "The existing ALT Color file(s) will be moved to the backup folder.");
      if (result.isPresent() && result.get().equals(ButtonType.OK)) {
        try {
          client.getAltColorService().restore(game.getId(), selectedItem);
          refresh();
          EventManager.getInstance().notifyTableChange(this.game.getId(), game.getRom());
        }
        catch (Exception ex) {
          LOG.error("Failed to restore ALT color: " + ex.getMessage(), ex);
          WidgetFactory.showAlert(Studio.stage, "Error", "Failed to restore ALT color backup: " + ex.getMessage());
        }
      }
    }
  }

  @FXML
  private void onDelete(ActionEvent e) {
    Stage stage = (Stage) ((Button) e.getSource()).getScene().getWindow();

    String selectedItem = backupList.getSelectionModel().getSelectedItem();
    if (selectedItem != null) {
      Optional<ButtonType> result = WidgetFactory.showConfirmation(stage, "Delete backup from " + selectedItem + "?");
      if (result.isPresent() && result.get().equals(ButtonType.OK)) {
        client.getAltColorService().deleteBackup(game.getId(), selectedItem);
        EventManager.getInstance().notifyTableChange(this.game.getId(), game.getRom());
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

      if (t1 != null) {
        scoreLabel.setText(t1);
      }
      else {
        scoreLabel.setText("No backup selected.");
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
    AltColor altColor = client.getAltColorService().getAltColor(this.game.getId());
    List<String> backupList = altColor.getBackedUpFiles();
    this.backupList.setItems(FXCollections.observableList(backupList));
    scoreLabel.setText("");

    if (!backupList.isEmpty()) {
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
