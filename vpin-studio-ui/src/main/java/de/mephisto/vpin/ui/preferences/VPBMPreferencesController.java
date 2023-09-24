package de.mephisto.vpin.ui.preferences;

import de.mephisto.vpin.commons.utils.SystemCommandExecutor;
import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.vpbm.VpbmHosts;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.util.BindingUtil;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.File;
import java.net.URI;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

public class VPBMPreferencesController implements Initializable {
  private final static Logger LOG = LoggerFactory.getLogger(VPBMPreferencesController.class);

  @FXML
  private TextField externalHostText;

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

    VpbmHosts hostIds = Studio.client.getVpbmService().getHostIds();
    if (hostIds != null) {
      this.externalHostText.setText(hostIds.getExternalHostId());
      this.thisHostText.setText(hostIds.getInternalHostId());
    }

    BindingUtil.bindTextField(externalHostText, PreferenceNames.VPBM_EXTERNAL_HOST_IDENTIFIER, "");
    BindingUtil.bindTextField(thisHostText, PreferenceNames.VPBM_INTERNAL_HOST_IDENTIFIER, "");

    new Thread(() -> {
      Platform.runLater(() -> {
        vpbmBtbn.setDisable(!Studio.client.getSystemService().isLocal() && new File("resources", "vpbm").exists());
        versionLabel.setText(Studio.client.getVpbmService().getVersion());

        boolean canUpdate = Studio.client.getSystemService().isLocal() && Studio.client.getVpbmService().isUpdateAvailable();
        updateBtn.setDisable(!canUpdate);

        //current .net check not applicable
//        validationError.setVisible(!Studio.client.getSystemService().isDotNetInstalled());
      });
    }).start();
  }

  @FXML
  private void onVPBMLink() {
    Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
    if (desktop != null && desktop.isSupported(Desktop.Action.BROWSE)) {
      try {
        desktop.browse(new URI("https://github.com/mmattner/vPinBackupManagerApp/"));
      } catch (Exception e) {
        LOG.error("Failed to open link: " + e.getMessage(), e);
      }
    }
  }

  @FXML
  private void onDotNetLink() {
    Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
    if (desktop != null && desktop.isSupported(Desktop.Action.BROWSE)) {
      try {
        desktop.browse(new URI("https://dotnet.microsoft.com/en-us/download/dotnet/thank-you/runtime-desktop-6.0.16-windows-x64-installer?cid=getdotnetcore"));
      } catch (Exception e) {
        LOG.error("Failed to open link: " + e.getMessage(), e);
      }
    }
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
        } catch (InterruptedException e) {
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
      } catch (InterruptedException e) {
        //ignore
      }
      vpbmBtbn.setDisable(false);
    });
  }
}
