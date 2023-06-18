package de.mephisto.vpin.ui.tables;

import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.restclient.mame.MameOptions;
import de.mephisto.vpin.restclient.representations.GameRepresentation;
import de.mephisto.vpin.restclient.representations.ValidationState;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.tables.validation.LocalizedValidation;
import de.mephisto.vpin.ui.tables.validation.ValidationTexts;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

public class TablesSidebarMameController implements Initializable {
  private final static Logger LOG = LoggerFactory.getLogger(TablesSidebarMameController.class);

  @FXML
  private VBox dataBox;

  @FXML
  private VBox emptyDataBox;

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
  private Button applyDefaultsBtn;

  private Optional<GameRepresentation> game = Optional.empty();

  private TablesSidebarController tablesSidebarController;
  private MameOptions options;

  private boolean saveDisabled = false;

  // Add a public no-args constructor
  public TablesSidebarMameController() {
  }

  @FXML
  private void onApplyDefaults() {
    MameOptions defaultOptions = Studio.client.getMameService().getOptions(MameOptions.DEFAULT_KEY);

    saveDisabled = true;
    skipPinballStartupTest.setSelected(defaultOptions.isSkipPinballStartupTest());
    useSound.setSelected(defaultOptions.isUseSound());
    useSamples.setSelected(defaultOptions.isUseSamples());
    ignoreRomCrcError.setSelected(defaultOptions.isIgnoreRomCrcError());
    cabinetMode.setSelected(defaultOptions.isCabinetMode());
    showDmd.setSelected(defaultOptions.isShowDmd());
    useExternalDmd.setSelected(defaultOptions.isUseExternalDmd());
    colorizeDmd.setSelected(defaultOptions.isColorizeDmd());

    saveDisabled = false;
    saveOptions();
  }

  @FXML
  private void onDismiss() {
    GameRepresentation g = game.get();
    tablesSidebarController.getTablesController().dismissValidation(g, options.getValidationStates().get(0));
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    dataBox.managedProperty().bindBidirectional(dataBox.visibleProperty());
    emptyDataBox.managedProperty().bindBidirectional(emptyDataBox.visibleProperty());
    errorBox.managedProperty().bindBidirectional(errorBox.visibleProperty());
    errorBox.setVisible(false);

    skipPinballStartupTest.selectedProperty().addListener((observable, oldValue, newValue) -> saveOptions());
    useSound.selectedProperty().addListener((observable, oldValue, newValue) -> saveOptions());
    useSamples.selectedProperty().addListener((observable, oldValue, newValue) -> saveOptions());
    ignoreRomCrcError.selectedProperty().addListener((observable, oldValue, newValue) -> saveOptions());
    cabinetMode.selectedProperty().addListener((observable, oldValue, newValue) -> saveOptions());
    showDmd.selectedProperty().addListener((observable, oldValue, newValue) -> saveOptions());
    useExternalDmd.selectedProperty().addListener((observable, oldValue, newValue) -> saveOptions());
    colorizeDmd.selectedProperty().addListener((observable, oldValue, newValue) -> saveOptions());
  }


  public void setGame(Optional<GameRepresentation> game) {
    this.game = game;
    saveDisabled = true;
    this.refreshView(game);
    saveDisabled = false;
  }

  public void refreshView(Optional<GameRepresentation> g) {
    this.options = null;

    emptyDataBox.setVisible(g.isEmpty());
    dataBox.setVisible(g.isPresent());

    skipPinballStartupTest.setSelected(false);
    useSound.setSelected(false);
    useSamples.setSelected(false);
    ignoreRomCrcError.setSelected(false);
    cabinetMode.setSelected(false);
    showDmd.setSelected(false);
    useExternalDmd.setSelected(false);
    colorizeDmd.setSelected(false);

    this.errorBox.setVisible(false);


    if (g.isPresent()) {
      GameRepresentation game = g.get();
      options = Studio.client.getMameService().getOptions(game.getRom());

      if (options != null) {
        skipPinballStartupTest.setSelected(options.isSkipPinballStartupTest());
        useSound.setSelected(options.isUseSound());
        useSamples.setSelected(options.isUseSamples());
        ignoreRomCrcError.setSelected(options.isIgnoreRomCrcError());
        cabinetMode.setSelected(options.isCabinetMode());
        showDmd.setSelected(options.isShowDmd());
        useExternalDmd.setSelected(options.isUseExternalDmd());
        colorizeDmd.setSelected(options.isColorizeDmd());

        if (options.getValidationStates() != null && !options.getValidationStates().isEmpty()) {
          ValidationState validationState = options.getValidationStates().get(0);
          LocalizedValidation validationResult = ValidationTexts.getValidationResult(game, validationState);

          this.errorBox.setVisible(true);
          this.errorTitle.setText(validationResult.getLabel());
          this.errorText.setText(validationResult.getText());
        }
      }
    }
  }

  private void saveOptions() {
    if(saveDisabled) {
      return;
    }

    MameOptions options = new MameOptions();
    options.setRom(game.get().getRom());

    options.setIgnoreRomCrcError(ignoreRomCrcError.isSelected());
    options.setSkipPinballStartupTest(skipPinballStartupTest.isSelected());
    options.setUseSamples(useSamples.isSelected());
    options.setUseSound(useSound.isSelected());
    options.setShowDmd(showDmd.isSelected());
    options.setUseExternalDmd(useExternalDmd.isSelected());
    options.setCabinetMode(cabinetMode.isSelected());
    options.setColorizeDmd(colorizeDmd.isSelected());

    try {
      Studio.client.getMameService().saveOptions(options);
    } catch (Exception e) {
      LOG.error("Failed to save mame settings: " + e.getMessage(), e);
      WidgetFactory.showAlert(Studio.stage, "Error", "Failed to save mame settings: " + e.getMessage());
    }
  }

  public void setSidebarController(TablesSidebarController tablesSidebarController) {
    this.tablesSidebarController = tablesSidebarController;
  }
}