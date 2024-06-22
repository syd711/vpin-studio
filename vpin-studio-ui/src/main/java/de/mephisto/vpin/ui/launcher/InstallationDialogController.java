package de.mephisto.vpin.ui.launcher;

import de.mephisto.vpin.commons.SystemInfo;
import de.mephisto.vpin.commons.fx.DialogController;
import de.mephisto.vpin.commons.utils.PropertiesStore;
import de.mephisto.vpin.ui.Studio;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import org.apache.commons.lang3.StringUtils;
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
  private TextField installationFolderField;

  @FXML
  private RadioButton radioA;

  @FXML
  private RadioButton radioB;

  @FXML
  private RadioButton radioC;

  @FXML
  private VBox validationError;

  private boolean result = false;

  private Stage stage;
  private PropertiesStore store;
  private File installationFolder;

  private SystemInfo systemInfo = new SystemInfo();

  @FXML
  private void onCancel(ActionEvent e) {
    Stage stage = (Stage) ((Button) e.getSource()).getScene().getWindow();
    stage.close();
  }

  @FXML
  private void onInstall(ActionEvent e) {
    LOG.info("********************************* Installation Overview ***********************************************");
    LOG.info("Frontend System Folder: " + installationFolder.getAbsolutePath());
    LOG.info("*******************************************************************************************************");

    if (radioA.isSelected()) {
      store.set(SystemInfo.PINUP_SYSTEM_INSTALLATION_DIR_INST_DIR, installationFolder.getAbsolutePath());
    }
    else if (radioB.isSelected()) {
      store.set(SystemInfo.PINBALLX_INSTALLATION_DIR_INST_DIR, installationFolder.getAbsolutePath());
    }
    else if (radioC.isSelected()) {
      store.set(SystemInfo.STANDALONE_INSTALLATION_DIR_INST_DIR, installationFolder.getAbsolutePath());
    }

    result = true;

    Stage stage = (Stage) ((Button) e.getSource()).getScene().getWindow();
    stage.close();
  }

  @FXML
  private void onPopperFolderBtn() {
    DirectoryChooser directoryChooser = new DirectoryChooser();
    if (installationFolder != null && installationFolder.exists()) {
      if (!installationFolder.isDirectory()) {
        installationFolder = installationFolder.getParentFile();
      }
      directoryChooser.setInitialDirectory(this.installationFolder);
    }
    directoryChooser.setTitle("Select Frontend System Folder");
    File selectedDirectory = directoryChooser.showDialog(stage);

    if (selectedDirectory != null) {
      this.installationFolder = selectedDirectory;
      this.installationFolderField.setText(this.installationFolder.getAbsolutePath());
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

    if (radioA.isSelected()) {
      installationFolder = systemInfo.resolvePinUPSystemInstallationFolder();
      installationFolderField.setText(installationFolder.getAbsolutePath());
    }

    ToggleGroup toggleGroup = new ToggleGroup();
    radioA.setToggleGroup(toggleGroup);
    radioB.setToggleGroup(toggleGroup);
    radioC.setToggleGroup(toggleGroup);

    toggleGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
      @Override
      public void changed(ObservableValue<? extends Toggle> observable, Toggle oldValue, Toggle newValue) {
        if (radioA.isSelected()) {
          installationFolder = systemInfo.resolvePinUPSystemInstallationFolder();
          installationFolderField.setText(installationFolder.getAbsolutePath());
        }

        if (radioB.isSelected()) {
          installationFolder = new File("C:\\PinballX");
          installationFolderField.setText(installationFolder.getAbsolutePath());
          validateFolders();
        }

        if (radioC.isSelected()) {
          installationFolder = new File("C:\\VisualPinball");
          installationFolderField.setText(installationFolder.getAbsolutePath());
          validateFolders();
        }

        validateFolders();
      }
    });

    validateFolders();
  }

  private void validateFolders() {
    installBtn.setDisable(true);
    validationError.setVisible(false);

    if (installationFolder == null) {
      validationError.setVisible(true);
      validationErrorLabel.setText("No frontend installation folder set.");
      return;
    }

    if (!installationFolder.exists()) {
      validationError.setVisible(true);
      validationErrorLabel.setText("Frontend installation folder does not exist.");
      return;
    }

    if (radioA.isSelected()) {
      if (!hasValidExe("PinUpMenu.exe")) {
        validationError.setVisible(true);
        validationErrorLabel.setText("No PinPUP Popper installation found in this folder.");
        return;
      }
    }

    if (radioB.isSelected()) {
      if (!hasValidExe("Game Manager.exe")) {
        validationError.setVisible(true);
        validationErrorLabel.setText("No PinballX installation found in this folder.");
        return;
      }
    }

    if (radioC.isSelected()) {
      if (!hasValidExe("VPinballX.exe") && !hasValidExe("VPinballX64.exe")) {
        validationError.setVisible(true);
        validationErrorLabel.setText("No Visual Pinball installation found in this folder.");
        return;
      }
    }

    installBtn.setDisable(false);
  }

  private boolean hasValidExe(String s) {
    if (!StringUtils.isEmpty(this.installationFolderField.getText())) {
      return new File(this.installationFolderField.getText(), s).exists();
    }
    return false;
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
