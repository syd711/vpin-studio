package de.mephisto.vpin.ui.launcher;

import de.mephisto.vpin.commons.ServerInstallationUtil;
import de.mephisto.vpin.commons.SystemInfo;
import de.mephisto.vpin.commons.fx.DialogController;
import de.mephisto.vpin.commons.fx.LoadingOverlayController;
import de.mephisto.vpin.commons.utils.PropertiesStore;
import de.mephisto.vpin.commons.utils.Updater;
import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.util.Dialogs;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
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

import static de.mephisto.vpin.ui.Studio.client;

public class InstallationController implements Initializable, DialogController {
  private final static Logger LOG = LoggerFactory.getLogger(InstallationController.class);

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
  private Button visualPinballFolderBtn;

  @FXML
  private Button mameFolderBtn;

  @FXML
  private Button vpxTablesFolderBtn;

  @FXML
  private TextField popperFolderField;

  @FXML
  private TextField visualPinballFolderField;

  @FXML
  private TextField mameFolderField;

  @FXML
  private TextField vpxTablesFolderField;

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
  private File visualPinballInstallationFolder;
  private File vpxTablesFolder;
  private File mameFolder;


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
    LOG.info("Visual Pinball Folder: " + visualPinballInstallationFolder.getAbsolutePath());
    LOG.info("Visual Pinball Tables Folder: " + vpxTablesFolder.getAbsolutePath());
    LOG.info("Mame Folder: " + mameFolder.getAbsolutePath());
    LOG.info("*******************************************************************************************************");

    store.set(SystemInfo.VISUAL_PINBALL_INST_DIR, visualPinballInstallationFolder.getAbsolutePath());
    store.set(SystemInfo.PINUP_SYSTEM_INSTALLATION_DIR_INST_DIR, pinUPSystemInstallationFolder.getAbsolutePath());
    store.set(SystemInfo.VPX_TABLES_DIR, vpxTablesFolder.getAbsolutePath());
    store.set(SystemInfo.MAME_DIR, mameFolder.getAbsolutePath());
    store.set(SystemInfo.USER_DIR, systemInfo.resolveUserFolder(visualPinballInstallationFolder).getAbsolutePath());

    result = true;

    Stage stage = (Stage) ((Button) e.getSource()).getScene().getWindow();
    stage.close();
  }

  @FXML
  private void onPopperFolderBtn() {
    DirectoryChooser directoryChooser = new DirectoryChooser();
    if(pinUPSystemInstallationFolder != null && pinUPSystemInstallationFolder.exists()) {
      directoryChooser.setInitialDirectory(this.pinUPSystemInstallationFolder);
    }
    directoryChooser.setTitle("Select PinUP System Folder");
    File selectedDirectory = directoryChooser.showDialog(stage);

    if (selectedDirectory != null) {
      this.pinUPSystemInstallationFolder = selectedDirectory;
      this.visualPinballFolderField.setText(this.pinUPSystemInstallationFolder.getAbsolutePath());
      validateFolders();
    }
  }

  @FXML
  private void onVisualPinballFolderBtn() {
    DirectoryChooser directoryChooser = new DirectoryChooser();
    if(visualPinballInstallationFolder != null && visualPinballInstallationFolder.exists()) {
      directoryChooser.setInitialDirectory(this.visualPinballInstallationFolder);
    }
    directoryChooser.setTitle("Select Visual Pinball Installation Folder");
    File selectedDirectory = directoryChooser.showDialog(stage);

    if (selectedDirectory != null) {
      this.visualPinballInstallationFolder = selectedDirectory;
      this.visualPinballFolderField.setText(this.visualPinballInstallationFolder.getAbsolutePath());
      validateFolders();
    }
  }

  @FXML
  private void onVpxTablesFolderBtn() {
    DirectoryChooser directoryChooser = new DirectoryChooser();
    if(vpxTablesFolder != null && vpxTablesFolder.exists()) {
      directoryChooser.setInitialDirectory(this.vpxTablesFolder);
    }
    directoryChooser.setTitle("Select VPX Tables Folder");
    File selectedDirectory = directoryChooser.showDialog(stage);

    if (selectedDirectory != null) {
      this.vpxTablesFolder = selectedDirectory;
      this.vpxTablesFolderField.setText(this.vpxTablesFolder.getAbsolutePath());
      validateFolders();
    }
  }

  @FXML
  private void onMameFolderBtn() {
    DirectoryChooser directoryChooser = new DirectoryChooser();
    if(mameFolder != null && mameFolder.exists()) {
      directoryChooser.setInitialDirectory(this.mameFolder);
    }

    directoryChooser.setTitle("Select VPin MAME Folder");
    File selectedDirectory = directoryChooser.showDialog(stage);

    if (selectedDirectory != null) {
      this.mameFolder = selectedDirectory;
      this.mameFolderField.setText(this.mameFolder.getAbsolutePath());
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
    popperFolderField.setText(pinUPSystemInstallationFolder.getAbsolutePath());

    visualPinballInstallationFolder = systemInfo.resolveVisualPinballInstallationFolder(pinUPSystemInstallationFolder);
    visualPinballFolderField.setText(visualPinballInstallationFolder.getAbsolutePath());

    vpxTablesFolder = systemInfo.resolveVpxTablesInstallationFolder(visualPinballInstallationFolder);
    vpxTablesFolderField.setText(vpxTablesFolder.getAbsolutePath());

    mameFolder = systemInfo.resolveMameInstallationFolder(visualPinballInstallationFolder);
    mameFolderField.setText(mameFolder.getAbsolutePath());

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

    if (visualPinballInstallationFolder == null) {
      validationError.setVisible(true);
      validationErrorLabel.setText("No Visual Pinball installation folder set.");
      return;
    }

    if (!visualPinballInstallationFolder.exists()) {
      validationError.setVisible(true);
      validationErrorLabel.setText("Visual Pinball installation folder does not exist.");
      return;
    }

    if (vpxTablesFolder == null) {
      validationError.setVisible(true);
      validationErrorLabel.setText("VPX tables folder set.");
      return;
    }

    if (!vpxTablesFolder.exists()) {
      validationError.setVisible(true);
      validationErrorLabel.setText("VPX tables folder does not exist.");
      return;
    }

    if (mameFolder == null) {
      validationError.setVisible(true);
      validationErrorLabel.setText("VPin MAME folder set.");
      return;
    }

    if (!mameFolder.exists()) {
      validationError.setVisible(true);
      validationErrorLabel.setText("VPin MAME folder does not exist.");
      return;
    }

    File[] roms = mameFolder.listFiles((dir, name) -> name.equals("roms"));

    if (roms == null || roms.length == 0) {
      validationError.setVisible(true);
      validationErrorLabel.setText("The VPin MAME folder is invalid, required subfolders have not been found.");
      return;
    }

    File[] userFolder = visualPinballInstallationFolder.listFiles((dir, name) -> name.equals("User"));

    if (userFolder == null || userFolder.length == 0) {
      validationError.setVisible(true);
      validationErrorLabel.setText("The Visual Pinball folder is invalid,\nrequired subfolders have not been found.");
      return;
    }

    File[] popMedia = pinUPSystemInstallationFolder.listFiles((dir, name) -> name.equals("POPMedia"));

    if (popMedia == null || popMedia.length == 0) {
      validationError.setVisible(true);
      validationErrorLabel.setText("The PinUP Popper installation folder is invalid,\nrequired subfolders have not been found.");
      return;
    }

    File[] files = vpxTablesFolder.listFiles((dir, name) -> name.endsWith(".vpx"));
    if (files == null || files.length == 0) {
      validationError.setVisible(true);
      validationErrorLabel.setText("The VPX tables folder \"" + vpxTablesFolder.getAbsolutePath() + "\" contains no VPX files.\nAre you sure it's the right folder?");
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
