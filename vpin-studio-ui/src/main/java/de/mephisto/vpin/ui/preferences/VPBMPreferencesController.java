package de.mephisto.vpin.ui.preferences;

import de.mephisto.vpin.commons.fx.Debouncer;
import de.mephisto.vpin.commons.utils.SystemCommandExecutor;
import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.preferences.BackupSettings;
import de.mephisto.vpin.ui.Studio;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

import static de.mephisto.vpin.ui.Studio.client;

public class VPBMPreferencesController implements Initializable {
  private final static Logger LOG = LoggerFactory.getLogger(VPBMPreferencesController.class);
  public static Debouncer debouncer = new Debouncer();

  @FXML
  private TextField externalHostText1;
  @FXML
  private TextField externalHostText2;
  @FXML
  private TextField externalHostText3;

  @FXML
  private TextField thisHostText;

  @FXML
  private Label versionLabel;

  @FXML
  private Label validationError;

  @FXML
  private Button vpbmBtbn;

  @FXML
  private Button updateBtn;

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    validationError.setVisible(false);
    updateBtn.setDisable(true);
    versionLabel.setText("Version: ???");

    BackupSettings backupSettings = client.getPreferenceService().getJsonPreference(PreferenceNames.BACKUP_SETTINGS, BackupSettings.class);
    if (backupSettings.getVpbmExternalHostId1() != null) {
      this.externalHostText1.setText(backupSettings.getVpbmExternalHostId1());
    }
    if (backupSettings.getVpbmExternalHostId2() != null) {
      this.externalHostText2.setText(backupSettings.getVpbmExternalHostId2());
    }
    if (backupSettings.getVpbmExternalHostId3() != null) {
      this.externalHostText3.setText(backupSettings.getVpbmExternalHostId3());
    }

    if (backupSettings.getVpbmInternalHostId() != null) {
      this.thisHostText.setText(backupSettings.getVpbmInternalHostId());
    }

    externalHostText1.textProperty().addListener((observableValue, s, t1) -> debouncer.debounce("value", () -> {
      backupSettings.setVpbmExternalHostId1(t1);
      client.getPreferenceService().setJsonPreference(PreferenceNames.BACKUP_SETTINGS, backupSettings);
    }, 500));
    externalHostText2.textProperty().addListener((observableValue, s, t1) -> debouncer.debounce("value", () -> {
      backupSettings.setVpbmExternalHostId2(t1);
      client.getPreferenceService().setJsonPreference(PreferenceNames.BACKUP_SETTINGS, backupSettings);
    }, 500));
    externalHostText3.textProperty().addListener((observableValue, s, t1) -> debouncer.debounce("value", () -> {
      backupSettings.setVpbmExternalHostId3(t1);
      client.getPreferenceService().setJsonPreference(PreferenceNames.BACKUP_SETTINGS, backupSettings);
    }, 500));

    new Thread(() -> {
      Platform.runLater(() -> {
        vpbmBtbn.setDisable(!Studio.client.getSystemService().isLocal() && new File("resources", "vpbm").exists());
        versionLabel.setText(Studio.client.getVpbmService().getVersion());

        boolean canUpdate = Studio.client.getSystemService().isLocal() && Studio.client.getVpbmService().isUpdateAvailable();
        updateBtn.setDisable(!canUpdate);
      });
    }).start();
  }

  @FXML
  private void onVPBMLink() {
    Studio.browse("https://github.com/mmattner/vPinBackupManagerApp/");
  }

  @FXML
  private void onDotNetLink() {
    Studio.browse("https://dotnet.microsoft.com/en-us/download/dotnet/thank-you/runtime-desktop-6.0.16-windows-x64-installer?cid=getdotnetcore");
  }

  @FXML
  private void onUpdate() {
    updateBtn.setDisable(true);
    vpbmBtbn.setDisable(true);

    updateBtn.setText("Installing Update...");
    new Thread(() -> {
      Studio.client.getVpbmService().update();
      Platform.runLater(() -> {
        try {
          vpbmBtbn.setDisable(true);
          updateBtn.setText("Install Update");
          updateBtn.setDisable(true);
          Thread.sleep(5000);

          versionLabel.setText(Studio.client.getVpbmService().getVersion());
        }
        catch (InterruptedException e) {
          LOG.error("Failed to execute VPBM update: " + e.getMessage(), e);
        }
        finally {
          vpbmBtbn.setDisable(false);
        }
      });
    }).start();
  }

  @FXML
  private void onVPBM() {
    Platform.runLater(() -> {
      vpbmBtbn.setDisable(true);
    });

    new Thread(() -> {
      List<String> commands = Arrays.asList("vPinBackupManager.exe");
      LOG.info("Executing vpbm: " + String.join(" ", commands));
      File dir = new File("./resources/", "vpbm");
      SystemCommandExecutor executor = new SystemCommandExecutor(commands);
      executor.setDir(dir);
      executor.executeCommandAsync();
    }).start();

    Platform.runLater(() -> {
      try {
        Thread.sleep(2000);
      }
      catch (InterruptedException e) {
        //ignore
      }
      vpbmBtbn.setDisable(false);
    });
  }
}
