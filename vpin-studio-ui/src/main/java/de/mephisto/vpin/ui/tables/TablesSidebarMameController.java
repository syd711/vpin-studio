package de.mephisto.vpin.ui.tables;

import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.restclient.games.GameRepresentation;
import de.mephisto.vpin.restclient.highscores.HighscoreType;
import de.mephisto.vpin.restclient.mame.MameOptions;
import de.mephisto.vpin.restclient.textedit.TextFile;
import de.mephisto.vpin.restclient.textedit.VPinFile;
import de.mephisto.vpin.restclient.validation.GameValidationCode;
import de.mephisto.vpin.restclient.validation.ValidationState;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.events.EventManager;
import de.mephisto.vpin.ui.tables.validation.GameValidationTexts;
import de.mephisto.vpin.ui.util.Dialogs;
import de.mephisto.vpin.ui.util.DismissalUtil;
import de.mephisto.vpin.ui.util.LocalizedValidation;
import de.mephisto.vpin.ui.util.ProgressDialog;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.VBox;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import java.util.ResourceBundle;

import static de.mephisto.vpin.ui.Studio.client;

public class TablesSidebarMameController implements Initializable {
  private final static Logger LOG = LoggerFactory.getLogger(TablesSidebarMameController.class);

  public final static java.util.List<SoundMode> SOUND_MODES = Arrays.asList(
      new SoundMode(0, "0 - Standard built-in PinMAME emulation"),
      new SoundMode(1, "1 - Built-in alternate sound file support"),
      new SoundMode(2, "2 - External pinsound"),
      new SoundMode(3, "3 - External pinsound + psrec sound recording"));

  @FXML
  private ScrollPane dataScrollPane;

  @FXML
  private VBox dataBox;

  @FXML
  private VBox emptyDataBox;

  @FXML
  private VBox invalidDataBox;

  @FXML
  private VBox noInputDataBox;

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
  private ComboBox<SoundMode> soundModeCombo;

  @FXML
  private CheckBox forceStereo;

  @FXML
  private Button applyDefaultsBtn;

  @FXML
  private Button mameBtn;

  @FXML
  private Button aliasBtn;

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


  private void onDelete() {
    Optional<ButtonType> result = WidgetFactory.showConfirmation(Studio.stage, "Delete VPin MAME settings for table '" + this.game.get().getGameDisplayName() + "'?");
    String rom = game.get().getRom();
    if (StringUtils.isEmpty(rom)) {
      return;
    }

    if (result.isPresent() && result.get().equals(ButtonType.OK)) {
      new Thread(() -> {
        client.getMameService().deleteSettings(rom);
        Platform.runLater(() -> {
          EventManager.getInstance().notifyTableChange(this.game.get().getId(), this.game.get().getRom());
        });
      }).start();
    }
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
    try {
      if (game.isPresent()) {
        GameRepresentation gameRepresentation = game.get();

        TextFile textFile = new TextFile(VPinFile.VPMAliasTxt);
        textFile.setEmulatorId(gameRepresentation.getEmulatorId());
        boolean b = Dialogs.openTextEditor(textFile, "VPMAlias.txt");
        if (b) {
          EventManager.getInstance().notifyTableChange(gameRepresentation.getId(), gameRepresentation.getRom());
          if (!StringUtils.isEmpty(gameRepresentation.getRomAlias())) {
            EventManager.getInstance().notifyTableChange(gameRepresentation.getId(), gameRepresentation.getRomAlias());
          }
        }
      }
    }
    catch (Exception e) {
      LOG.error("Failed to open alias text file: " + e.getMessage(), e);
      WidgetFactory.showAlert(Studio.stage, "Error", "Failed to open alias text file: " + e.getMessage());
    }
  }

  @FXML
  private void onReload() {
    ProgressDialog.createProgressDialog(new MAMERefreshProgressModel(this.game.get()));
  }

  @FXML
  private void onMameSetup() {
    if (this.game.isPresent()) {
      if (!client.getMameService().runSetup()) {
        WidgetFactory.showAlert(Studio.stage, "Did not find Setup.exe", "The setup.exe file was not found.");
      }
    }
  }

  @FXML
  private void onApplyDefaults() {
    if (options.isExistInRegistry()) {
      onDelete();
    }
    else {
      noInputDataBox.setVisible(false);
      MameOptions defaultOptions = client.getMameService().getOptions(MameOptions.DEFAULT_KEY);

      options.setSkipPinballStartupTest(defaultOptions.isSkipPinballStartupTest());
      options.setUseSound(defaultOptions.isUseSound());
      options.setUseSamples(defaultOptions.isUseSamples());
      options.setCompactDisplay(defaultOptions.isCompactDisplay());
      options.setDoubleDisplaySize(defaultOptions.isDoubleDisplaySize());
      options.setIgnoreRomCrcError(defaultOptions.isIgnoreRomCrcError());
      options.setCabinetMode(defaultOptions.isCabinetMode());
      options.setShowDmd(defaultOptions.isShowDmd());
      options.setUseExternalDmd(defaultOptions.isUseExternalDmd());
      options.setColorizeDmd(defaultOptions.isColorizeDmd());
      options.setSoundMode(defaultOptions.getSoundMode());
      options.setForceStereo(defaultOptions.isForceStereo());

      try {
        client.getMameService().saveOptions(options);
        EventManager.getInstance().notifyTableChange(this.game.get().getId(), this.game.get().getRom());
      }
      catch (Exception e) {
        LOG.error("Failed to save mame settings: " + e.getMessage(), e);
        WidgetFactory.showAlert(Studio.stage, "Error", "Failed to save mame settings: " + e.getMessage());
      }
    }
  }

  @FXML
  private void onDismiss() {
    GameRepresentation g = game.get();
    DismissalUtil.dismissValidation(g, game.get().getValidationState());
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    dataBox.managedProperty().bindBidirectional(dataBox.visibleProperty());
    emptyDataBox.managedProperty().bindBidirectional(emptyDataBox.visibleProperty());
    noInputDataBox.managedProperty().bindBidirectional(noInputDataBox.visibleProperty());
    invalidDataBox.managedProperty().bindBidirectional(invalidDataBox.visibleProperty());
    errorBox.managedProperty().bindBidirectional(errorBox.visibleProperty());
    errorBox.setVisible(false);
    invalidDataBox.setVisible(false);
    mameBtn.setDisable(!client.getSystemService().isLocal());

    noInputDataBox.setVisible(false);

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
    soundModeCombo.setItems(FXCollections.observableList(SOUND_MODES));
    soundModeCombo.valueProperty().addListener((observable, oldValue, newValue) -> saveOptions());
    forceStereo.selectedProperty().addListener((observable, oldValue, newValue) -> saveOptions());
  }


  public void setGame(Optional<GameRepresentation> game) {
    this.game = game;
    saveDisabled = true;
    this.reloadBtn.setDisable(game.isEmpty());
    this.refreshView(game);
    saveDisabled = false;
  }

  public void refreshView(Optional<GameRepresentation> g) {
    this.options = null;

    invalidDataBox.setVisible(false);
    emptyDataBox.setVisible(g.isEmpty());
    dataBox.setVisible(g.isPresent());
    dataScrollPane.setVisible(g.isPresent());

    labelRomAlias.setText("-");
    labelRom.setText("-");
    copyRomAliasBtn.setDisable(true);
    copyRomBtn.setDisable(true);
    aliasBtn.setDisable(g.isEmpty());

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
    soundModeCombo.setValue(SOUND_MODES.get(0));
    forceStereo.setSelected(false);

    this.errorBox.setVisible(false);
    this.applyDefaultsBtn.setDisable(!g.isPresent());
    noInputDataBox.setVisible(false);

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
        noInputDataBox.setVisible(options == null || !options.isExistInRegistry());

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
          soundModeCombo.setValue(SOUND_MODES.get(options.getSoundMode()));
          forceStereo.setSelected(options.isForceStereo());

          if (game.getValidationState() != null) {
            ValidationState validationState = game.getValidationState();
            if (validationState.getCode() == GameValidationCode.CODE_ALT_COLOR_COLORIZE_DMD_ENABLED ||
                validationState.getCode() == GameValidationCode.CODE_FORCE_STEREO ||
                validationState.getCode() == GameValidationCode.CODE_ALT_SOUND_NOT_ENABLED ||
                validationState.getCode() == GameValidationCode.CODE_ALT_COLOR_EXTERNAL_DMD_NOT_ENABLED) {
              LocalizedValidation validationResult = GameValidationTexts.getValidationResult(game, validationState);

              this.errorBox.setVisible(true);
              this.errorTitle.setText(validationResult.getLabel());
              this.errorText.setText(validationResult.getText());
            }
          }
        }
      }
      else {
        applyDefaultsBtn.setDisable(true);
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
    soundModeCombo.setDisable(b);
    forceStereo.setDisable(b);

    applyDefaultsBtn.setText(b ? "Override Defaults" : "Apply Defaults");

    Tooltip tooltip = new Tooltip(b ? "Uses the default VPin MAME settings and applies them for the ROM of the selected game." : "Delete the VPin MAME settings for this table so that the system defaults are used.");
    applyDefaultsBtn.setTooltip(tooltip);
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
    options.setSoundMode(soundModeCombo.getValue().getId());
    options.setForceStereo(forceStereo.isSelected());

    try {
      client.getMameService().saveOptions(options);
      EventManager.getInstance().notifyTableChange(this.game.get().getId(), this.game.get().getRom());
    }
    catch (Exception e) {
      LOG.error("Failed to save mame settings: " + e.getMessage(), e);
      WidgetFactory.showAlert(Studio.stage, "Error", "Failed to save mame settings: " + e.getMessage());
    }
  }

  public void setSidebarController(TablesSidebarController tablesSidebarController) {
    this.tablesSidebarController = tablesSidebarController;
  }

  public static class SoundMode {
    private int id;
    private String label;

    public SoundMode(int id, String label) {
      this.id = id;
      this.label = label;
    }

    public int getId() {
      return id;
    }

    public String getLabel() {
      return label;
    }

    @Override
    public String toString() {
      return label;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
      SoundMode soundMode = (SoundMode) o;
      return id == soundMode.id;
    }

    @Override
    public int hashCode() {
      return Objects.hashCode(id);
    }
  }
}