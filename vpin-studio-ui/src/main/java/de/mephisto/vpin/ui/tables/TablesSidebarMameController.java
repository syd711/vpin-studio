package de.mephisto.vpin.ui.tables;

import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.restclient.games.GameRepresentation;
import de.mephisto.vpin.restclient.highscores.HighscoreType;
import de.mephisto.vpin.restclient.mame.MameOptions;
import de.mephisto.vpin.restclient.textedit.MonitoredTextFile;
import de.mephisto.vpin.restclient.textedit.VPinFile;
import de.mephisto.vpin.restclient.validation.GameValidationCode;
import de.mephisto.vpin.restclient.validation.ValidationState;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.events.EventManager;
import de.mephisto.vpin.ui.tables.validation.GameValidationTexts;
import de.mephisto.vpin.ui.tables.vbsedit.VBSManager;
import de.mephisto.vpin.ui.util.Dialogs;
import de.mephisto.vpin.ui.util.DismissalUtil;
import de.mephisto.vpin.ui.util.LocalizedValidation;
import de.mephisto.vpin.ui.util.ProgressDialog;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

import static de.mephisto.vpin.ui.Studio.Features;
import static de.mephisto.vpin.ui.Studio.client;

public class TablesSidebarMameController implements Initializable {
  private final static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

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
  private VBox tablesBox;

  @FXML
  private VBox tableListBox;

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
  private Button scriptBtn;

  @FXML
  private Button copyRomAliasBtn;

  @FXML
  private Button copyRomBtn;

  @FXML
  private Label labelRomAlias;

  @FXML
  private Label nvOffsetLabel;

  @FXML
  private Label labelRom;

  @FXML
  private Button nvOffsetBtn;


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
  private void onNvOffset() {
    if (this.game.isPresent()) {
      onNvOffset(this.game.get().getId());
    }
  }

  @FXML
  public void onScriptEdit() {
    VBSManager.getInstance().edit(this.game);
  }

  private void onNvOffset(int gameId) {
    GameRepresentation g = client.getGameService().getGame(gameId);
    if (g == null) {
      return;
    }

    int nvOffset = g.getNvOffset();
    if (nvOffset == 0) {
      nvOffset = 1;
    }
    String value = WidgetFactory.showInputDialog(Studio.stage, "NVOffset", "Enter the new NVOffset value for the table.", null, null, String.valueOf(nvOffset));
    if (value != null) {
      try {
        nvOffset = Integer.parseInt(value);
      }
      catch (NumberFormatException e) {
        WidgetFactory.showAlert(Studio.stage, "Error", "Invalid NVOffset value.", "Please input a numeric value.");
        onNvOffset(gameId);
        return;
      }

      if (nvOffset > 0 && nvOffset == g.getNvOffset()) {
        WidgetFactory.showInformation(Studio.stage, "NVOffset not updated", "NVOffset not updated, because it already has the value \"" + nvOffset + "\".", null);
        onNvOffset(gameId);
        return;
      }

      try {
        int nvOffset1 = client.getVpxService().setNvOffset(g.getId(), nvOffset);
        if (nvOffset1 == -1) {
          WidgetFactory.showAlert(Studio.stage, "Error", "The NVOffset value has not been set. The script analysis failed.", "Use the script editor to set the value manually.");
        }
        else {
          EventManager.getInstance().notifyTableChange(g.getId(), g.getRom(), null);
        }
      }
      catch (Exception e) {
        LOG.error("Failed to set nvoffset value: {}", e.getMessage(), e);
        WidgetFactory.showAlert(Studio.stage, "Error", "Setting NVOffset failed: " + e.getMessage(), "Use the script editor to set the value manually.");
      }
    }
  }

  @FXML
  private void onVPMAlias() {
    try {
      if (game.isPresent()) {
        GameRepresentation gameRepresentation = game.get();

        MonitoredTextFile monitoredTextFile = new MonitoredTextFile(VPinFile.VPMAliasTxt);
        monitoredTextFile.setEmulatorId(gameRepresentation.getEmulatorId());
        boolean b = Dialogs.openTextEditor(monitoredTextFile, "VPMAlias.txt");
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
    try {
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

        client.getMameService().saveOptions(options);
      }
    }
    catch (Exception e) {
      LOG.error("Failed to save mame settings: " + e.getMessage(), e);
      WidgetFactory.showAlert(Studio.stage, "Error", "Failed to save mame settings: " + e.getMessage());
    }
    finally {
      Studio.client.getGameService().reload(game.get().getId());
      EventManager.getInstance().notifyTableChange(this.game.get().getId(), this.game.get().getRom());
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
    tablesBox.managedProperty().bindBidirectional(tablesBox.visibleProperty());
    errorBox.managedProperty().bindBidirectional(errorBox.visibleProperty());
    errorBox.setVisible(false);
    invalidDataBox.setVisible(false);
    tablesBox.setVisible(false);

    mameBtn.managedProperty().bind(mameBtn.visibleProperty());
    mameBtn.setVisible(!Features.IS_STANDALONE);
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

  public void refreshView(Optional<GameRepresentation> gameOptional) {
    this.options = null;

    invalidDataBox.setVisible(false);
    emptyDataBox.setVisible(gameOptional.isEmpty());
    dataBox.setVisible(gameOptional.isPresent());
    dataScrollPane.setVisible(gameOptional.isPresent());

    labelRomAlias.setText("-");
    nvOffsetLabel.setText("-");
    labelRom.setText("-");
    copyRomAliasBtn.setDisable(true);
    scriptBtn.setDisable(true);
    copyRomBtn.setDisable(true);
    aliasBtn.setDisable(gameOptional.isEmpty());
    nvOffsetBtn.setDisable(true);

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
    this.applyDefaultsBtn.setDisable(!gameOptional.isPresent());
    noInputDataBox.setVisible(false);
    tablesBox.setVisible(false);

    if (gameOptional.isPresent()) {
      GameRepresentation game = gameOptional.get();

      nvOffsetBtn.setDisable(!HighscoreType.NVRam.equals(game.getHighscoreType()));
      labelRomAlias.setText(!StringUtils.isEmpty(game.getRomAlias()) ? game.getRomAlias() : "-");
      copyRomAliasBtn.setDisable(false);
      scriptBtn.setDisable(false);

      if (game.getNvOffset() > 0) {
        nvOffsetLabel.setText(String.valueOf(game.getNvOffset()));
      }

      List<GameRepresentation> data = tablesSidebarController.getTableOverviewController().getData();
      String rom = game.getRom();
      List<GameRepresentation> sharedGames = data.stream().filter(g -> !StringUtils.isEmpty(g.getRom()) && g.getRom().equals(rom) && g.getId() != game.getId()).collect(Collectors.toList());
      if (!sharedGames.isEmpty()) {
        tablesBox.setVisible(true);
        tableListBox.getChildren().removeAll(tableListBox.getChildren());
        for (GameRepresentation sharedGame : sharedGames) {
          HBox row = new HBox(3);
          String title = "\"" + sharedGame.getGameDisplayName() + "\"";
          if (sharedGame.getNvOffset() > 0) {
            title += " [NVOffset: " + sharedGame.getNvOffset() + "]";
          }
          Label label = new Label("- " + title);
          label.setPrefWidth(464);
          label.setStyle("");
          label.getStyleClass().add("default-text");
          row.getChildren().add(label);

          Button button = new Button("", WidgetFactory.createIcon("mdi2l-lead-pencil"));
          button.setTooltip(new Tooltip("Edit table data"));
          button.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
              TableDialogs.openTableDataDialog(tablesSidebarController.getTableOverviewController(), sharedGame);
            }
          });
          row.getChildren().add(button);

          if (HighscoreType.NVRam.equals(sharedGame.getHighscoreType())) {
            Button nvOffsetButton = new Button("", WidgetFactory.createIcon("mdi2s-script-text"));
            nvOffsetButton.setTooltip(new Tooltip("Set NVOffset"));
            nvOffsetButton.setOnAction(new EventHandler<ActionEvent>() {
              @Override
              public void handle(ActionEvent event) {
                onNvOffset(sharedGame.getId());
              }
            });

            row.getChildren().add(nvOffsetButton);
          }

          tableListBox.getChildren().add(row);
        }
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