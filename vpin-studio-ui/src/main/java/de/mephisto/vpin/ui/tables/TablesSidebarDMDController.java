package de.mephisto.vpin.ui.tables;

import de.mephisto.vpin.restclient.util.FileUtils;
import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.restclient.dmd.DMDPackage;
import de.mephisto.vpin.restclient.games.GameRepresentation;
import de.mephisto.vpin.restclient.textedit.TextFile;
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
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
  private Button dmdPositionBtn;

  @FXML
  private Button reloadBtn;

  @FXML
  private Label lastModifiedLabel;

  @FXML
  private Label bundleSizeLabel;

  @FXML
  private Label dmdFolderLabel;

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
  private void onDmdDevice() {
    if (client.getSystemService().isLocal()) {
      File ini = client.getMameService().getDmdDeviceIni();
      Dialogs.editFile(ini);
    }
    else {
      try {
        boolean b = Dialogs.openTextEditor(new TextFile(VPinFile.DmdDeviceIni), "DmdDevice.ini");
        if (b) {
          client.getMameService().clearCache();
          EventManager.getInstance().notifyTablesChanged();
        }
      }
      catch (Exception e) {
        LOG.error("Failed to open DmdDeviceIni text file: " + e.getMessage(), e);
        WidgetFactory.showAlert(Studio.stage, "Error", "Failed to open DmdDeviceIni file: " + e.getMessage());
      }
    }
  }

  @FXML
  private void onFlexDMDUI() {
    if (this.game.isPresent()) {
      if (!client.getMameService().runFlexSetup()) {
        WidgetFactory.showAlert(Studio.stage, "Did not find FlexDMD UI", "The FlexDMDUI.exe file was not found.");
      }
    }
  }

  @FXML
  private void onDMDPosition() {
    GameRepresentation g = game.get();
    TableDialogs.openDMDPositionDialog(g, null);
  }

  @FXML
  private void onReload() {
    this.reloadBtn.setDisable(true);

    Platform.runLater(() -> {
      new Thread(() -> {
        Studio.client.getGameService().scanGame(this.game.get().getId());
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
      TableDialogs.openDMDUploadDialog(game.get(), null, null, null);
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
    dmdFolderLabel.setText("-");
    lastModifiedLabel.setText("-");

    boolean directb2sAvailable = g.isPresent() && g.get().getDirectB2SPath() != null;
    dmdPositionBtn.setDisable(g.isEmpty() || !directb2sAvailable);

    errorBox.setVisible(false);

    if (g.isPresent()) {
      GameRepresentation game = g.get();
      dmdPackage = Studio.client.getDmdService().getDMDPackage(game.getId());
      boolean packageAvailable = dmdPackage != null;

      dataBox.setVisible(packageAvailable);
      emptyDataBox.setVisible(!packageAvailable);

      uploadBtn.setDisable(StringUtils.isEmpty(game.getRom()));

      if (packageAvailable) {
        dmdFolderLabel.setText(dmdPackage.getName());
        bundleTypeLabel.setText(dmdPackage.getDmdPackageTypes().name());
        if (dmdPackage.getSize() > 0) {
          bundleSizeLabel.setText(FileUtils.readableFileSize(dmdPackage.getSize()));
        }
        if (dmdPackage.getModificationDate() != null) {
          lastModifiedLabel.setText(SimpleDateFormat.getDateTimeInstance().format(dmdPackage.getModificationDate()));
        }

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
  }
}