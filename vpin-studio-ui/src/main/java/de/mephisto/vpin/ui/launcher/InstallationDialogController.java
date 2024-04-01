package de.mephisto.vpin.ui.launcher;

import de.mephisto.vpin.commons.SystemInfo;
import de.mephisto.vpin.commons.fx.DialogController;
import de.mephisto.vpin.commons.utils.PropertiesStore;
import de.mephisto.vpin.ui.Studio;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

public class InstallationDialogController implements Initializable, DialogController {
  private final static Logger LOG = LoggerFactory.getLogger(InstallationDialogController.class);

  @FXML
  private Label studioLabel;

  @FXML
  private Label versionLabel;

  @FXML
  private Label validationErrorLabel;

  @FXML
  private Button installBtn;

  @FXML
  private Button popperFolderBtn;

  @FXML
  private TextField pinUPSystemFolderField;

  @FXML
  private BorderPane main;

  @FXML
  private ToolBar toolbar;

  @FXML
  private VBox validationError;

  private boolean result = false;

  private Stage stage;
  private PropertiesStore store;
  private File pinUPSystemInstallationFolder;

  private SystemInfo systemInfo = new SystemInfo();

  @FXML
  private void onCancel(ActionEvent e) {
    Stage stage = (Stage) ((Button) e.getSource()).getScene().getWindow();
    stage.close();
  }

  @FXML
  private void onInstall(ActionEvent e) {
    LOG.info("********************************* Installation Overview ***********************************************");
    LOG.info("PinUP System Folder: " + pinUPSystemInstallationFolder.getAbsolutePath());
    LOG.info("*******************************************************************************************************");

    store.set(SystemInfo.PINUP_SYSTEM_INSTALLATION_DIR_INST_DIR, pinUPSystemInstallationFolder.getAbsolutePath());
    result = true;

    Stage stage = (Stage) ((Button) e.getSource()).getScene().getWindow();
    stage.close();
  }

  @FXML
  private void onPopperFolderBtn() {
    DirectoryChooser directoryChooser = new DirectoryChooser();
    if (pinUPSystemInstallationFolder != null && pinUPSystemInstallationFolder.exists()) {
      if(!pinUPSystemInstallationFolder.isDirectory()) {
        pinUPSystemInstallationFolder = pinUPSystemInstallationFolder.getParentFile();
      }
      directoryChooser.setInitialDirectory(this.pinUPSystemInstallationFolder);
    }
    directoryChooser.setTitle("Select PinUP System Folder");
    File selectedDirectory = directoryChooser.showDialog(stage);

    if (selectedDirectory != null) {
      this.pinUPSystemInstallationFolder = selectedDirectory;
      this.pinUPSystemFolderField.setText(this.pinUPSystemInstallationFolder.getAbsolutePath());
      validateFolders();
    }
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    this.installBtn.setDisable(true);
    Font font = Font.font("Impact", FontPosture.findByName("regular"), 28);
    studioLabel.setFont(font);
    versionLabel.setText(Studio.getVersion());

    File propertiesFile = new File(SystemInfo.RESOURCES + "system.properties");
    propertiesFile.getParentFile().mkdirs();
    store = PropertiesStore.create(propertiesFile);

    pinUPSystemInstallationFolder = systemInfo.resolvePinUPSystemInstallationFolder();
    pinUPSystemFolderField.setText(pinUPSystemInstallationFolder.getAbsolutePath());

    validateFolders();
  }

  private void validateFolders() {
    installBtn.setDisable(true);
    validationError.setVisible(false);

    if (pinUPSystemInstallationFolder == null) {
      validationError.setVisible(true);
      validationErrorLabel.setText("No PinUP Popper installation folder set.");
      return;
    }

    if (!pinUPSystemInstallationFolder.exists()) {
      validationError.setVisible(true);
      validationErrorLabel.setText("PinUP Popper installation folder does not exist.");
      return;
    }

    installBtn.setDisable(false);
  }

  @Override
  public void onDialogCancel() {

  }

  public boolean install() {
    return this.result;
  }

  public void setStage(Stage stage) {
    this.stage = stage;
  }
}
