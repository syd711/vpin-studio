package de.mephisto.vpin.ui.tables;

import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.restclient.games.GameEmulatorRepresentation;
import de.mephisto.vpin.restclient.games.GameRepresentation;
import de.mephisto.vpin.restclient.highscores.HighscoreType;
import de.mephisto.vpin.restclient.mame.MameOptions;
import de.mephisto.vpin.restclient.textedit.VPinFile;
import de.mephisto.vpin.restclient.validation.ValidationState;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.events.EventManager;
import de.mephisto.vpin.ui.tables.validation.GameValidationTexts;
import de.mephisto.vpin.ui.util.Dialogs;
import de.mephisto.vpin.ui.util.DismissalUtil;
import de.mephisto.vpin.ui.util.LocalizedValidation;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.VBox;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.File;
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

import static de.mephisto.vpin.ui.Studio.client;

public class TablesSidebarMameController implements Initializable {
  private final static Logger LOG = LoggerFactory.getLogger(TablesSidebarMameController.class);

  @FXML
  private VBox dataBox;

  @FXML
  private VBox emptyDataBox;

  @FXML
  private VBox invalidDataBox;

  @FXML
  private VBox errorBox;

  @FXML
  private Label errorTitle;

  @FXML
  private Label errorText;

  @FXML
  private CheckBox skipPinballStartupTest;

  @FXML
  private CheckBox useSound;

  @FXML
  private CheckBox useSamples;

  @FXML
  private CheckBox compactDisplay;

  @FXML
  private CheckBox doubleDisplaySize;

  @FXML
  private CheckBox ignoreRomCrcError;

  @FXML
  private CheckBox cabinetMode;

  @FXML
  private CheckBox showDmd;

  @FXML
  private CheckBox useExternalDmd;

  @FXML
  private CheckBox colorizeDmd;

  @FXML
  private CheckBox soundMode;

  @FXML
  private Button applyDefaultsBtn;

  @FXML
  private Button mameBtn;

  @FXML
  private Button reloadBtn;

  @FXML
  private Button copyRomAliasBtn;

  @FXML
  private Button copyRomBtn;

  @FXML
  private Label labelRomAlias;

  @FXML
  private Label labelRom;


  private Optional<GameRepresentation> game = Optional.empty();

  private TablesSidebarController tablesSidebarController;
  private MameOptions options;

  private boolean saveDisabled = false;

  // Add a public no-args constructor
  public TablesSidebarMameController() {
  }


  @FXML
  private void onRomAliasCopy() {
    Clipboard clipboard = Clipboard.getSystemClipboard();
    ClipboardContent content = new ClipboardContent();
    content.putString(game.get().getRomAlias());
    clipboard.setContent(content);
  }

  @FXML
  private void onRomCopy() {
    Clipboard clipboard = Clipboard.getSystemClipboard();
    ClipboardContent content = new ClipboardContent();
    content.putString(game.get().getRom());
    clipboard.setContent(content);
  }

  @FXML
  private void onVPMAlias() {
//    if (client.getSystemService().isLocal()) {
//      GameEmulatorRepresentation defaultGameEmulator = client.getPinUPPopperService().getDefaultGameEmulator();
//      File folder = new File(defaultGameEmulator.getMameDirectory());
//      File textFile = new File(folder, "VPMAlias.txt");
//      Dialogs.editFile(textFile);
//    }
//    else {
//
//    }
    boolean b = Dialogs.openTextEditor(VPinFile.VPMAliasTxt);
    if (b) {
      client.getMameService().clearCache();
      EventManager.getInstance().notifyTablesChanged();
    }
  }

  @FXML
  private void onReload() {
    saveDisabled = true;
    this.reloadBtn.setDisable(true);
    client.getMameService().clearCache();

    Platform.runLater(() -> {
      new Thread(() -> {
        this.game.ifPresent(gameRepresentation -> EventManager.getInstance().notifyTableChange(gameRepresentation.getId(), gameRepresentation.getRom()));

        Platform.runLater(() -> {
          this.refreshView(this.game);
          this.reloadBtn.setDisable(false);
          saveDisabled = false;
        });
      }).start();
    });
  }

  @FXML
  private void onMameSetup() {
    if (this.game.isPresent()) {
      GameRepresentation g = this.game.get();
      Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
      if (desktop != null && desktop.isSupported(Desktop.Action.OPEN)) {
        try {
          GameEmulatorRepresentation emulatorRepresentation = client.getPinUPPopperService().getGameEmulator(g.getEmulatorId());
          File file = new File(emulatorRepresentation.getMameDirectory(), "Setup64.exe");
          if (!file.exists()) {
            file = new File(emulatorRepresentation.getMameDirectory(), "Setup.exe");
          }
          if (!file.exists()) {
            WidgetFactory.showAlert(Studio.stage, "Did not find Setup.exe", "The exe file " + file.getAbsolutePath() + " was not found.");
          }
          else {
            desktop.open(file);
          }
        } catch (Exception e) {
          LOG.error("Failed to open Mame Setup: " + e.getMessage());
        }
      }
    }
  }

  @FXML
  private void onApplyDefaults() {
    MameOptions defaultOptions = client.getMameService().getOptions(MameOptions.DEFAULT_KEY);

    saveDisabled = true;
    skipPinballStartupTest.setSelected(defaultOptions.isSkipPinballStartupTest());
    useSound.setSelected(defaultOptions.isUseSound());
    useSamples.setSelected(defaultOptions.isUseSamples());
    compactDisplay.setSelected(defaultOptions.isCompactDisplay());
    doubleDisplaySize.setSelected(defaultOptions.isDoubleDisplaySize());
    ignoreRomCrcError.setSelected(defaultOptions.isIgnoreRomCrcError());
    cabinetMode.setSelected(defaultOptions.isCabinetMode());
    showDmd.setSelected(defaultOptions.isShowDmd());
    useExternalDmd.setSelected(defaultOptions.isUseExternalDmd());
    colorizeDmd.setSelected(defaultOptions.isColorizeDmd());
    soundMode.setSelected(defaultOptions.isSoundMode());

    saveDisabled = false;
    saveOptions();
  }

  @FXML
  private void onDismiss() {
    GameRepresentation g = game.get();
    DismissalUtil.dismissValidation(g, options.getValidationStates().get(0));
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    dataBox.managedProperty().bindBidirectional(dataBox.visibleProperty());
    emptyDataBox.managedProperty().bindBidirectional(emptyDataBox.visibleProperty());
    invalidDataBox.managedProperty().bindBidirectional(invalidDataBox.visibleProperty());
    errorBox.managedProperty().bindBidirectional(errorBox.visibleProperty());
    errorBox.setVisible(false);
    invalidDataBox.setVisible(false);
    mameBtn.setDisable(!client.getSystemService().isLocal());

    skipPinballStartupTest.selectedProperty().addListener((observable, oldValue, newValue) -> saveOptions());
    useSound.selectedProperty().addListener((observable, oldValue, newValue) -> saveOptions());
    useSamples.selectedProperty().addListener((observable, oldValue, newValue) -> saveOptions());
    compactDisplay.selectedProperty().addListener((observable, oldValue, newValue) -> saveOptions());
    doubleDisplaySize.selectedProperty().addListener((observable, oldValue, newValue) -> saveOptions());
    ignoreRomCrcError.selectedProperty().addListener((observable, oldValue, newValue) -> saveOptions());
    cabinetMode.selectedProperty().addListener((observable, oldValue, newValue) -> saveOptions());
    showDmd.selectedProperty().addListener((observable, oldValue, newValue) -> saveOptions());
    useExternalDmd.selectedProperty().addListener((observable, oldValue, newValue) -> saveOptions());
    colorizeDmd.selectedProperty().addListener((observable, oldValue, newValue) -> saveOptions());
    soundMode.selectedProperty().addListener((observable, oldValue, newValue) -> saveOptions());
  }


  public void setGame(Optional<GameRepresentation> game) {
    this.game = game;
    saveDisabled = true;
    this.refreshView(game);
    saveDisabled = false;
  }

  public void refreshView(Optional<GameRepresentation> g) {
    this.options = null;

    invalidDataBox.setVisible(false);
    emptyDataBox.setVisible(g.isEmpty());
    dataBox.setVisible(g.isPresent());

    labelRomAlias.setText("-");
    labelRom.setText("-");
    copyRomAliasBtn.setDisable(true);
    copyRomBtn.setDisable(true);

    skipPinballStartupTest.setSelected(false);
    useSound.setSelected(false);
    useSamples.setSelected(false);
    compactDisplay.setSelected(false);
    doubleDisplaySize.setSelected(false);
    ignoreRomCrcError.setSelected(false);
    cabinetMode.setSelected(false);
    showDmd.setSelected(false);
    useExternalDmd.setSelected(false);
    colorizeDmd.setSelected(false);
    soundMode.setSelected(false);

    this.errorBox.setVisible(false);
    this.applyDefaultsBtn.setDisable(!g.isPresent());

    if (g.isPresent()) {
      GameRepresentation game = g.get();

      if (!StringUtils.isEmpty(game.getRomAlias())) {
        labelRomAlias.setText(game.getRomAlias());
        copyRomAliasBtn.setDisable(false);
      }

      if (!StringUtils.isEmpty(game.getRom())) {
        labelRom.setText(game.getRom());
        copyRomBtn.setDisable(false);
      }

      invalidDataBox.setVisible(HighscoreType.VPReg.name().equals(game.getHighscoreType()));
      if (invalidDataBox.isVisible()) {
        applyDefaultsBtn.setDisable(true);
        dataBox.setVisible(false);
        errorBox.setVisible(false);
        return;
      }

      boolean romSet = !StringUtils.isEmpty(game.getRom());
      setInputDisabled(!romSet);

      if (romSet) {
        options = client.getMameService().getOptions(game.getRom());
        setInputDisabled(options == null || !options.isExistInRegistry());

        if (options != null) {
          skipPinballStartupTest.setSelected(options.isSkipPinballStartupTest());
          useSound.setSelected(options.isUseSound());
          useSamples.setSelected(options.isUseSamples());
          compactDisplay.setSelected(options.isCompactDisplay());
          doubleDisplaySize.setSelected(options.isDoubleDisplaySize());
          ignoreRomCrcError.setSelected(options.isIgnoreRomCrcError());
          cabinetMode.setSelected(options.isCabinetMode());
          showDmd.setSelected(options.isShowDmd());
          useExternalDmd.setSelected(options.isUseExternalDmd());
          colorizeDmd.setSelected(options.isColorizeDmd());
          soundMode.setSelected(options.isSoundMode());

          if (options.getValidationStates() != null && !options.getValidationStates().isEmpty()) {
            ValidationState validationState = options.getValidationStates().get(0);
            LocalizedValidation validationResult = GameValidationTexts.getValidationResult(game, validationState);

            this.errorBox.setVisible(true);
            this.errorTitle.setText(validationResult.getLabel());
            this.errorText.setText(validationResult.getText());
          }
        }
      }
    }
  }

  private void setInputDisabled(boolean b) {
    skipPinballStartupTest.setDisable(b);
    useSound.setDisable(b);
    useSamples.setDisable(b);
    compactDisplay.setDisable(b);
    doubleDisplaySize.setDisable(b);
    ignoreRomCrcError.setDisable(b);
    cabinetMode.setDisable(b);
    showDmd.setDisable(b);
    useExternalDmd.setDisable(b);
    colorizeDmd.setDisable(b);
    soundMode.setDisable(b);
  }

  private void saveOptions() {
    if (saveDisabled) {
      return;
    }

    MameOptions options = new MameOptions();
    options.setRom(game.get().getRom());

    options.setIgnoreRomCrcError(ignoreRomCrcError.isSelected());
    options.setSkipPinballStartupTest(skipPinballStartupTest.isSelected());
    options.setUseSamples(useSamples.isSelected());
    options.setUseSound(useSound.isSelected());
    options.setCompactDisplay(compactDisplay.isSelected());
    options.setDoubleDisplaySize(doubleDisplaySize.isSelected());
    options.setShowDmd(showDmd.isSelected());
    options.setUseExternalDmd(useExternalDmd.isSelected());
    options.setCabinetMode(cabinetMode.isSelected());
    options.setColorizeDmd(colorizeDmd.isSelected());
    options.setSoundMode(soundMode.isSelected());

    try {
      client.getMameService().saveOptions(options);
      EventManager.getInstance().notifyTableChange(this.game.get().getId(), this.game.get().getRom());
    } catch (Exception e) {
      LOG.error("Failed to save mame settings: " + e.getMessage(), e);
      WidgetFactory.showAlert(Studio.stage, "Error", "Failed to save mame settings: " + e.getMessage());
    }
  }

  public void setSidebarController(TablesSidebarController tablesSidebarController) {
    this.tablesSidebarController = tablesSidebarController;
  }
}