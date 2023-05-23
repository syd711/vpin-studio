package de.mephisto.vpin.ui.preferences;

import de.mephisto.vpin.commons.utils.SystemCommandExecutor;
import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.VpbmHosts;
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
  private Button vpbmBtbn;

  @FXML
  private Button updateBtn;

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    vpbmBtbn.setDisable(!Studio.client.getSystemService().isLocal());
    versionLabel.setText(Studio.client.getVpbmService().getVersion());
    updateBtn.setDisable(true);

    VpbmHosts hostIds = Studio.client.getVpbmService().getHostIds();
    if (hostIds != null) {
      this.externalHostText.setText(hostIds.getExternalHostId());
      this.thisHostText.setText(hostIds.getInternalHostId());
    }

    BindingUtil.bindTextField(externalHostText, PreferenceNames.VPBM_EXTERNAL_HOST_IDENTIFIER, "");
    BindingUtil.bindTextField(thisHostText, PreferenceNames.VPBM_INTERNAL_HOST_IDENTIFIER, "");

    new Thread(() -> {
      Platform.runLater(() -> {
        updateBtn.setDisable(!Studio.client.getVpbmService().isUpdateAvailable());
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
