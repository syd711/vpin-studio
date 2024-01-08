package de.mephisto.vpin.ui.tables;

import de.mephisto.vpin.commons.utils.FileUtils;
import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.restclient.dmd.DMDPackage;
import de.mephisto.vpin.restclient.games.GameEmulatorRepresentation;
import de.mephisto.vpin.restclient.games.GameRepresentation;
import de.mephisto.vpin.restclient.validation.ValidationState;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.events.EventManager;
import de.mephisto.vpin.ui.tables.drophandler.PupPackFileDropEventHandler;
import de.mephisto.vpin.ui.tables.validation.GameValidationTexts;
import de.mephisto.vpin.ui.util.DismissalUtil;
import de.mephisto.vpin.ui.util.FileDragEventHandler;
import de.mephisto.vpin.ui.util.LocalizedValidation;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
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

import static de.mephisto.vpin.ui.Studio.client;

public class TablesSidebarDMDController implements Initializable {
  private final static Logger LOG = LoggerFactory.getLogger(TablesSidebarDMDController.class);

  private Optional<GameRepresentation> game = Optional.empty();

  @FXML
  private Button uploadBtn;

  @FXML
  private Button flexDMDUIBtn;

  @FXML
  private Button reloadBtn;

  @FXML
  private Label lastModifiedLabel;

  @FXML
  private Label bundleSizeLabel;

  @FXML
  private Label bundleTypeLabel;

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
  private Pane pupRoot;


  private TablesSidebarController tablesSidebarController;
  private DMDPackage dmdPackage;
  private ValidationState validationState;

  // Add a public no-args constructor
  public TablesSidebarDMDController() {
  }

  @FXML
  private void onFlexDMDUI() {
    if (this.game.isPresent()) {
      GameRepresentation g = this.game.get();

      Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
      if (desktop != null && desktop.isSupported(Desktop.Action.OPEN)) {
        try {
          GameEmulatorRepresentation emulatorRepresentation = client.getPinUPPopperService().getGameEmulator(g.getEmulatorId());
          File file = new File(emulatorRepresentation.getMameDirectory(), "FlexDMDUI.exe");
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
  }

  @FXML
  private void onReload() {
    this.reloadBtn.setDisable(true);

    Platform.runLater(() -> {
      new Thread(() -> {
        this.game.ifPresent(gameRepresentation -> EventManager.getInstance().notifyTableChange(gameRepresentation.getId(), gameRepresentation.getRom()));
      }).start();
    });
  }

  @FXML
  private void onDismiss() {
    GameRepresentation g = game.get();
    DismissalUtil.dismissValidation(g, this.validationState);
  }

  @FXML
  private void onUpload() {
    if (game.isPresent()) {
      TableDialogs.openDMDUploadDialog(tablesSidebarController, game.get(), null);
    }
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    dataBox.managedProperty().bindBidirectional(dataBox.visibleProperty());
    emptyDataBox.managedProperty().bindBidirectional(emptyDataBox.visibleProperty());
    errorBox.managedProperty().bindBidirectional(errorBox.visibleProperty());
    errorBox.setVisible(false);
    dataBox.setVisible(false);
    emptyDataBox.setVisible(true);

    flexDMDUIBtn.setVisible(Studio.client.getSystemService().isLocal());
  }

  public void setGame(Optional<GameRepresentation> game) {
    this.game = game;
    this.refreshView(game);
  }

  public void refreshView(Optional<GameRepresentation> g) {
    this.dmdPackage = null;
    this.validationState = null;
    reloadBtn.setDisable(g.isEmpty());

    dataBox.setVisible(false);
    emptyDataBox.setVisible(true);
    uploadBtn.setDisable(true);
    bundleSizeLabel.setText("-");
    lastModifiedLabel.setText("-");

    errorBox.setVisible(false);

    if (g.isPresent()) {
      GameRepresentation game = g.get();
      dmdPackage = Studio.client.getDmdService().getDMDPackage(game.getId());
      boolean packageAvailable = dmdPackage != null;

      dataBox.setVisible(packageAvailable);
      emptyDataBox.setVisible(!packageAvailable);

      uploadBtn.setDisable(StringUtils.isEmpty(game.getRom()));

      if (packageAvailable) {
        bundleSizeLabel.setText(FileUtils.readableFileSize(dmdPackage.getSize()));
        bundleTypeLabel.setText(dmdPackage.getDmdPackageTypes().name());
        lastModifiedLabel.setText(SimpleDateFormat.getDateTimeInstance().format(dmdPackage.getModificationDate()));

        List<ValidationState> validationStates = dmdPackage.getValidationStates();
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

    pupRoot.setOnDragOver(new FileDragEventHandler(pupRoot, true, "zip"));
    pupRoot.setOnDragDropped(new PupPackFileDropEventHandler(tablesSidebarController));
  }
}