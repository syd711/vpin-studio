package de.mephisto.vpin.ui.tables;

import de.mephisto.vpin.commons.utils.FileUtils;
import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.restclient.altsound.AltSound;
import de.mephisto.vpin.restclient.altsound.AltSoundFormats;
import de.mephisto.vpin.restclient.games.GameRepresentation;
import de.mephisto.vpin.restclient.validation.ValidationState;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.events.EventManager;
import de.mephisto.vpin.ui.tables.validation.GameValidationTexts;
import de.mephisto.vpin.ui.util.DismissalUtil;
import de.mephisto.vpin.ui.util.FileDragEventHandler;
import de.mephisto.vpin.ui.util.LocalizedValidation;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
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
  private Button deleteBtn;

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
  private Label formatLabel;

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

  @FXML
  private Pane altSoundRoot;

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
      TableDialogs.openAltSoundUploadDialog(tablesSidebarController, game.get(), null);
    }
  }

  @FXML
  private void onDelete() {
    Optional<ButtonType> result = WidgetFactory.showConfirmation(Studio.stage, "Delete ALTSound package for table '" + this.game.get().getGameDisplayName() + "'?");
    if (result.isPresent() && result.get().equals(ButtonType.OK)) {
      Studio.client.getAltSoundService().delete(this.game.get().getId());
      EventManager.getInstance().notifyTableChange(this.game.get().getId(), this.game.get().getRom());
    }
  }

  @FXML
  private void onAltSoundEdit() {
    if (game.isPresent() && game.get().isAltSoundAvailable()) {
      if (altSound.getFormat() == null || altSound.getFormat().equals(AltSoundFormats.altsound)) {
        tablesSidebarController.getTablesController().showAltSoundEditor(this.game.get(), altSound);
      }
      else if (altSound.getFormat().equals(AltSoundFormats.gsound)) {
        if(altSound.getFilesize() == -1) {
          WidgetFactory.showAlert(Studio.stage, "Invalid Configuration", "The table must be played once, so that the necessary configuration files are generated.");
          return;
        }
        tablesSidebarController.getTablesController().showAltSound2Editor(this.game.get(), altSound);
      }
      else {
        WidgetFactory.showAlert(Studio.stage, "Error", "Unknown alt sound format \"" + altSound.getFormat() + "\".");
      }
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
    DismissalUtil.dismissValidation(g, this.validationState);
  }


  @FXML
  private void onReload() {
    this.reloadBtn.setDisable(true);
    tablesSidebarController.getTablesController().closeEditors();

    Platform.runLater(() -> {
      new Thread(() -> {
        Studio.client.getMameService().clearCache();
        this.game.ifPresent(gameRepresentation -> EventManager.getInstance().notifyTableChange(gameRepresentation.getId(), gameRepresentation.getRom()));

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
      Optional<ButtonType> result = WidgetFactory.showConfirmation(Studio.stage, "Restore Backup?", "Revert all changes and restore the original ALT sound backup?", null, "Restore Backup");
      if (result.isPresent() && result.get().equals(ButtonType.OK)) {
        tablesSidebarController.getTablesController().closeEditors();
        Studio.client.getAltSoundService().restoreAltSound(game.get().getId());
        EventManager.getInstance().notifyTableChange(game.get().getId(), game.get().getRom());
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
    deleteBtn.setDisable(true);
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
    deleteBtn.setDisable(true);

    entriesLabel.setText("-");
    bundleSizeLabel.setText("-");
    filesLabel.setText("-");
    lastModifiedLabel.setText("-");
    formatLabel.setText("-");

    errorBox.setVisible(false);

    if (g.isPresent()) {
      GameRepresentation game = g.get();
      boolean altSoundAvailable = game.isAltSoundAvailable();

      dataBox.setVisible(altSoundAvailable);
      emptyDataBox.setVisible(!altSoundAvailable);

      uploadBtn.setDisable(StringUtils.isEmpty(game.getRom()));
      deleteBtn.setDisable(!altSoundAvailable);
      altSoundBtn.setDisable(!altSoundAvailable);
      restoreBtn.setDisable(!altSoundAvailable);
      enabledCheckbox.setDisable(!altSoundAvailable);
      enabledCheckbox.setVisible(altSoundAvailable);

      if (altSoundAvailable) {
        altSound = Studio.client.getAltSoundService().getAltSound(game.getId());
        enabledCheckbox.setSelected(Studio.client.getAltSoundService().isAltSoundEnabled(game.getId()));

        entriesLabel.setText(String.valueOf(altSound.getEntries().size()));
        filesLabel.setText(String.valueOf(altSound.getFiles()));

        long filesize = altSound.getFilesize();
        if (filesize == -1) {
          formatLabel.setText(altSound.getFormat() + " (not played yet)");
        }
        else {
          bundleSizeLabel.setText(FileUtils.readableFileSize(altSound.getFilesize()));
          lastModifiedLabel.setText(SimpleDateFormat.getDateTimeInstance().format(altSound.getModificationDate()));
          formatLabel.setText(altSound.getFormat());
        }

        List<ValidationState> validationStates = altSound.getValidationStates();
        errorBox.setVisible(!validationStates.isEmpty());
        if (!validationStates.isEmpty()) {
          validationState = validationStates.get(0);
          LocalizedValidation validationResult = GameValidationTexts.getValidationResult(game, validationState);
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