package de.mephisto.vpin.ui.tables;

import de.mephisto.vpin.commons.utils.FileUtils;
import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.restclient.AltSound;
import de.mephisto.vpin.restclient.representations.GameRepresentation;
import de.mephisto.vpin.restclient.representations.ValidationState;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.events.EventManager;
import de.mephisto.vpin.ui.tables.validation.LocalizedValidation;
import de.mephisto.vpin.ui.tables.validation.ValidationTexts;
import de.mephisto.vpin.ui.util.Dialogs;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class TablesSidebarAltSoundController implements Initializable {
  private final static Logger LOG = LoggerFactory.getLogger(TablesSidebarAltSoundController.class);

  @FXML
  private Button altSoundBtn;

  @FXML
  private Button reloadBtn;

  @FXML
  private Button restoreBtn;

  @FXML
  private Button uploadBtn;

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

  @FXML
  private VBox dataRoot;

  @FXML
  private VBox emptyDataBox;

  @FXML
  private VBox dataBox;

  @FXML
  private VBox errorBox;

  @FXML
  private Label errorTitle;

  @FXML
  private Label errorText;

  private Optional<GameRepresentation> game = Optional.empty();

  private TablesSidebarController tablesSidebarController;
  private AltSound altSound;
  private ValidationState validationState;

  // Add a public no-args constructor
  public TablesSidebarAltSoundController() {
  }

  @FXML
  private void onUpload() {
    if (game.isPresent()) {
      Dialogs.openAltSoundUploadDialog(tablesSidebarController, game.get());
    }
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
      Studio.client.getAltSoundService().setAltSoundEnabled(game.get().getId(), enabledCheckbox.isSelected());
      EventManager.getInstance().notifyTableChange(g.getId(), g.getRom());
    }
  }

  @FXML
  private void onDismiss() {
    GameRepresentation g = game.get();
    tablesSidebarController.getTablesController().dismissValidation(g, this.validationState);
  }


  @FXML
  private void onReload() {
    this.reloadBtn.setDisable(true);

    Platform.runLater(() -> {
      new Thread(() -> {
        Studio.client.getAltSoundService().clearCache();

        Platform.runLater(() -> {
          this.reloadBtn.setDisable(false);
          this.refreshView(this.game);
        });
      }).start();
    });
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
    dataBox.managedProperty().bindBidirectional(dataBox.visibleProperty());
    emptyDataBox.managedProperty().bindBidirectional(emptyDataBox.visibleProperty());
    errorBox.managedProperty().bindBidirectional(errorBox.visibleProperty());
    dataBox.setVisible(false);
    emptyDataBox.setVisible(true);
  }

  public void setGame(Optional<GameRepresentation> game) {
    this.game = game;
    this.refreshView(game);
  }

  public void refreshView(Optional<GameRepresentation> g) {
    this.validationState = null;
    reloadBtn.setDisable(g.isEmpty());

    altSoundBtn.setDisable(true);
    restoreBtn.setDisable(true);
    enabledCheckbox.setVisible(false);
    dataBox.setVisible(false);
    emptyDataBox.setVisible(true);
    uploadBtn.setDisable(true);

    entriesLabel.setText("-");
    bundleSizeLabel.setText("-");
    filesLabel.setText("-");
    lastModifiedLabel.setText("-");

    errorBox.setVisible(false);

    if (g.isPresent()) {
      GameRepresentation game = g.get();
      boolean altSoundAvailable = game.isAltSoundAvailable();
      reloadBtn.setDisable(!altSoundAvailable);

      dataBox.setVisible(altSoundAvailable);
      emptyDataBox.setVisible(!altSoundAvailable);

      uploadBtn.setDisable(StringUtils.isEmpty(game.getRom()));
      altSoundBtn.setDisable(!altSoundAvailable);
      restoreBtn.setDisable(!altSoundAvailable);
      enabledCheckbox.setDisable(!altSoundAvailable);
      enabledCheckbox.setVisible(altSoundAvailable);

      if (altSoundAvailable) {
        altSound = Studio.client.getAltSoundService().getAltSound(game.getId());
        enabledCheckbox.setSelected(Studio.client.getAltSoundService().isAltSoundEnabled(game.getId()));

        entriesLabel.setText(String.valueOf(altSound.getEntries().size()));
        filesLabel.setText(String.valueOf(altSound.getFiles()));
        bundleSizeLabel.setText(FileUtils.readableFileSize(altSound.getFilesize()));
        lastModifiedLabel.setText(SimpleDateFormat.getDateTimeInstance().format(altSound.getModificationDate()));

        List<ValidationState> validationStates = altSound.getValidationStates();
        errorBox.setVisible(!validationStates.isEmpty());
        if (!validationStates.isEmpty()) {
          validationState = validationStates.get(0);
          LocalizedValidation validationResult = ValidationTexts.getValidationResult(game, validationState);
          errorTitle.setText(validationResult.getLabel());
          errorText.setText(validationResult.getText());
        }
      }
    }
  }

  public void setSidebarController(TablesSidebarController tablesSidebarController) {
    this.tablesSidebarController = tablesSidebarController;
  }
}