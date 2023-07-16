package de.mephisto.vpin.ui.tables;

import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.restclient.AltColor;
import de.mephisto.vpin.restclient.SystemSummary;
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
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.File;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class TablesSidebarAltColorController implements Initializable {
  private final static Logger LOG = LoggerFactory.getLogger(TablesSidebarAltColorController.class);

  @FXML
  private Button uploadBtn;

  @FXML
  private Button reloadBtn;

  @FXML
  private Button flexDMDUIBtn;

  @FXML
  private Label lastModifiedLabel;

  @FXML
  private Label typeLabel;

  @FXML
  private Label filesLabel;

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

  private AltColor altColor;
  private ValidationState validationState;

  private Optional<GameRepresentation> game = Optional.empty();

  private TablesSidebarController tablesSidebarController;

  // Add a public no-args constructor
  public TablesSidebarAltColorController() {
  }

  @FXML
  private void onUpload() {
    if (game.isPresent()) {
      Dialogs.openAltColorUploadDialog(tablesSidebarController, game.get());
    }
  }

  @FXML
  private void onFlexDMDUI() {
    Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
    if (desktop != null && desktop.isSupported(Desktop.Action.OPEN)) {
      try {
        SystemSummary systemSummary = Studio.client.getSystemService().getSystemSummary();
        File file = new File(systemSummary.getVpinMameDirectory(), "FlexDMDUI.exe");
        if (!file.exists()) {
          WidgetFactory.showAlert(Studio.stage, "Did not find FlexDMD UI", "The exe file " + file.getAbsolutePath() + " was not found.");
        }
        else {
          desktop.open(file);
        }
      } catch (Exception e) {
        LOG.error("Failed to open FlexDMD UI: " + e.getMessage(), e);
      }
    }
  }

  @FXML
  private void onReload() {
    this.reloadBtn.setDisable(true);

    Platform.runLater(() -> {
      new Thread(() -> {
        Studio.client.getAltColorService().clearCache();

        Platform.runLater(() -> {
          this.reloadBtn.setDisable(false);
          EventManager.getInstance().notifyRepositoryUpdate();
        });
      }).start();
    });
  }

  @FXML
  private void onDismiss() {
    GameRepresentation g = game.get();
    tablesSidebarController.getTablesController().dismissValidation(g, this.validationState);
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    dataBox.managedProperty().bindBidirectional(dataBox.visibleProperty());
    emptyDataBox.managedProperty().bindBidirectional(emptyDataBox.visibleProperty());
    errorBox.managedProperty().bindBidirectional(errorBox.visibleProperty());
    dataBox.setVisible(false);
    emptyDataBox.setVisible(true);
    uploadBtn.setDisable(true);

    flexDMDUIBtn.setDisable(!Studio.client.getSystemService().isLocal());
  }

  public void setGame(Optional<GameRepresentation> game) {
    this.game = game;
    this.refreshView(game);
  }

  public void refreshView(Optional<GameRepresentation> g) {
    this.altColor = null;
    this.validationState = null;
    reloadBtn.setDisable(g.isEmpty());

    dataBox.setVisible(false);
    emptyDataBox.setVisible(true);
    uploadBtn.setDisable(true);

    lastModifiedLabel.setText("-");
    typeLabel.setText("-");
    filesLabel.setText("-");

    errorBox.setVisible(false);

    if (g.isPresent()) {
      GameRepresentation game = g.get();
      boolean altColorAvailable = game.isAltColorAvailable();
      reloadBtn.setDisable(!altColorAvailable);

      dataBox.setVisible(altColorAvailable);
      emptyDataBox.setVisible(!altColorAvailable);

      uploadBtn.setDisable(StringUtils.isEmpty(game.getEffectiveRom()));

      if (altColorAvailable) {
        altColor = Studio.client.getAltColorService().getAltColor(game.getId());
        lastModifiedLabel.setText(SimpleDateFormat.getDateTimeInstance().format(altColor.getModificationDate()));
        typeLabel.setText(altColor.getAltColorType().name());

        List<String> files = altColor.getFiles();
        filesLabel.setText(String.join(", ", files));


        List<ValidationState> validationStates = altColor.getValidationStates();
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