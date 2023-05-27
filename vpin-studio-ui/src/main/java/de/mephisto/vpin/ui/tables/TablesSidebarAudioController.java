package de.mephisto.vpin.ui.tables;

import de.mephisto.vpin.commons.utils.FileUtils;
import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.restclient.AltSound;
import de.mephisto.vpin.restclient.client.VPinStudioClient;
import de.mephisto.vpin.restclient.representations.GameRepresentation;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.util.Dialogs;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Optional;
import java.util.ResourceBundle;

public class TablesSidebarAudioController implements Initializable {
  private final static Logger LOG = LoggerFactory.getLogger(TablesSidebarAudioController.class);

  @FXML
  private Button altSoundBtn;

  @FXML
  private Button restoreBtn;

  @FXML
  private Label entriesLabel;

  @FXML
  private Label filesLabel;

  @FXML
  private Label lastModifiedLabel;

  @FXML
  private Label bundleSizeLabel;

  @FXML
  private CheckBox enabledCheckbox;

  private Optional<GameRepresentation> game = Optional.empty();

  private TablesSidebarController tablesSidebarController;
  private AltSound altSound;

  // Add a public no-args constructor
  public TablesSidebarAudioController() {
  }

  @FXML
  private void onAltSoundEdit() {
    if (game.isPresent() && game.get().isAltSoundAvailable()) {
      Dialogs.openAltSoundEditor(game.get(), altSound);
    }
  }

  @FXML
  private void onAltSoundEnable() {
    if (game.isPresent() && game.get().isAltSoundAvailable()) {
      GameRepresentation g = game.get();
      g.setAltSoundEnabled(enabledCheckbox.isSelected());
      try {
        Studio.client.getGameService().saveGame(g);
      } catch (Exception e) {
        WidgetFactory.showAlert(Studio.stage, e.getMessage());
      }
    }
  }

  @FXML
  private void onRestore() {
    if (game.isPresent() && game.get().isAltSoundAvailable()) {
      Optional<ButtonType> result = WidgetFactory.showConfirmation(Studio.stage, "Restore Backup?", "Revert all changes and restore the original ALT sound backup?", null, "Yes, restore backup");
      if (result.isPresent() && result.get().equals(ButtonType.OK)) {
        Studio.client.getAltSoundService().restoreAltSound(game.get().getId());
      }
    }
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {

  }

  public void setGame(Optional<GameRepresentation> game) {
    this.game = game;
    this.refreshView(game);
  }

  public void refreshView(Optional<GameRepresentation> g) {
    altSoundBtn.setDisable(!game.isPresent() || !game.get().isAltSoundAvailable());
    restoreBtn.setDisable(!game.isPresent() || !game.get().isAltSoundAvailable());
    enabledCheckbox.setDisable(!game.isPresent() || !game.get().isAltSoundAvailable());
    enabledCheckbox.setSelected(false);

    entriesLabel.setText("-");
    bundleSizeLabel.setText("-");
    filesLabel.setText("-");
    lastModifiedLabel.setText("-");

    if (g.isPresent()) {
      GameRepresentation game = g.get();

      if (game.isAltSoundAvailable()) {
        altSound = Studio.client.getAltSoundService().getAltSound(game.getId());
        enabledCheckbox.setSelected(game.isAltSoundEnabled());

        entriesLabel.setText(String.valueOf(altSound.getEntries().size()));
        filesLabel.setText(String.valueOf(altSound.getFiles()));
        bundleSizeLabel.setText(FileUtils.readableFileSize(altSound.getFilesize()));
        lastModifiedLabel.setText(SimpleDateFormat.getDateTimeInstance().format(altSound.getModificationDate()));
      }
    }
  }

  public void setSidebarController(TablesSidebarController tablesSidebarController) {
    this.tablesSidebarController = tablesSidebarController;
  }
}