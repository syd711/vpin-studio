package de.mephisto.vpin.ui.components;

import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.restclient.components.ComponentSummary;
import de.mephisto.vpin.restclient.components.ComponentSummaryEntry;
import de.mephisto.vpin.restclient.components.ComponentType;
import de.mephisto.vpin.restclient.textedit.MonitoredTextFile;
import de.mephisto.vpin.restclient.textedit.VPinFile;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.util.Dialogs;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

import static de.mephisto.vpin.ui.Studio.client;

public class TabDOFLinxController extends AbstractComponentTab implements Initializable {
  private final static Logger LOG = LoggerFactory.getLogger(TabDOFLinxController.class);

  @FXML
  private Button configBtn;

  @FXML
  private Button iniBtn;

  @FXML
  private Button restartBtn;

  @FXML
  private Button stopBtn;

  @FXML
  private void onRestart() {
    restartBtn.setDisable(true);
    Platform.runLater(() -> {
      stopBtn.setDisable(!client.getDofLinxService().restart());
      stopBtn.setDisable(false);
      restartBtn.setDisable(false);
      onReload();
    });
  }

  @FXML
  private void onStop() {
    stopBtn.setDisable(!client.getDofLinxService().kill());
    stopBtn.setDisable(true);
    onReload();
  }

  @FXML
  private void onIni() {
    boolean running = client.getDofLinxService().isRunning();
    if (running) {
      Optional<ButtonType> result = WidgetFactory.showConfirmation(Studio.stage, "DOFLink Running", "DOFLinx is currently running.",
          "Shutdown DOFLink and edit DOFLinx.INI file?", "Shutdown and edit");
      if (!result.isPresent() || !result.get().equals(ButtonType.OK)) {
        client.getDofLinxService().kill();
      }
      else {
        return;
      }
    }

    String installFolder = component.getTargetFolder();
    if (!StringUtils.isEmpty(installFolder)) {
      File folder = new File(installFolder);
      if (folder.exists()) {
        try {
          boolean b = Dialogs.openTextEditor(new MonitoredTextFile(VPinFile.DOFLinxINI), "DOFLinx.INI");
          if (b) {
            onReload();
          }
        }
        catch (Exception e) {
          LOG.error("Failed to open DOFLinx.INI file: " + e.getMessage(), e);
          WidgetFactory.showAlert(Studio.stage, "Error", "Failed to open DOFLinx.INI file: " + e.getMessage());
        }
        return;
      }
    }

    WidgetFactory.showAlert(Studio.stage, "Error", "Invalid or no DOFLinx installation folder set.");
  }

  @FXML
  private void onConfigTool() {
    String installFolder = component.getTargetFolder();
    if (!StringUtils.isEmpty(installFolder)) {
      File exe = new File(installFolder, "DOFLinxConfig.exe");
      if (exe.exists()) {
        openFile(exe);
        return;
      }
    }

    WidgetFactory.showAlert(Studio.stage, "Error", "Invalid or no DOFLinx installation folder set.");
  }

  @Override
  protected void refresh() {
    super.refresh();
    onReload();
  }

  @FXML
  private void onReload() {
    try {
      openFolderButton.setDisable(true);
      stopBtn.setDisable(true);
      restartBtn.setDisable(true);
      configBtn.setDisable(true);
      iniBtn.setDisable(true);

      String installFolder = component.getTargetFolder();
      if (!StringUtils.isEmpty(installFolder)) {
        File folder = new File(installFolder);
        openFolderButton.setDisable(!folder.exists());
        iniBtn.setDisable(!folder.exists());

        boolean running = client.getDofLinxService().isRunning();
        stopBtn.setDisable(!running);
        restartBtn.setDisable(!folder.exists());

        File exe = new File(installFolder, "DOFLinxConfig.exe");
        configBtn.setDisable(!exe.exists());
      }

      clearCustomValues();
      ComponentSummary freezySummary = client.getDofLinxService().getDOFLinxSummary();
      List<ComponentSummaryEntry> entries = freezySummary.getEntries();
      for (ComponentSummaryEntry entry : entries) {
        super.addCustomValue(entry);
      }
    }
    catch (Exception e) {
      LOG.error("Failed to refresh DOFLinx: " + e.getMessage());
    }
  }

  @Override
  protected ComponentType getComponentType() {
    return ComponentType.doflinx;
  }


  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    super.initialize();
    onReload();
  }
}
