package de.mephisto.vpin.ui.components;

import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.components.ComponentActionLogRepresentation;
import de.mephisto.vpin.restclient.components.ComponentRepresentation;
import de.mephisto.vpin.restclient.components.ComponentType;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.events.EventManager;
import de.mephisto.vpin.ui.events.StudioEventListener;
import de.mephisto.vpin.ui.util.Dialogs;
import de.mephisto.vpin.ui.util.ProgressResultModel;
import edu.umd.cs.findbugs.annotations.NonNull;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

import static de.mephisto.vpin.ui.Studio.client;

public class ComponentUpdateController implements Initializable, StudioEventListener {
  private final static Logger LOG = LoggerFactory.getLogger(ComponentUpdateController.class);

  @FXML
  private Button installBtn;

  @FXML
  private Button checkBtn;

  @FXML
  private Button simBtn;

  @FXML
  private TextArea textArea;

  @FXML
  private ComboBox<String> artifactCombo;

  private AbstractComponentTab componentTab;
  private ComponentType type;
  private ComponentRepresentation component;

  @FXML
  private void onFetch() {
    ComponentCheckProgressModel model = new ComponentCheckProgressModel("Fetching Latest Releases for " + type, type, "-latest-");
    ProgressResultModel resultModel = Dialogs.createProgressDialog(model);
    if (!resultModel.getResults().isEmpty()) {
      ComponentActionLogRepresentation log = (ComponentActionLogRepresentation) resultModel.getResults().get(0);
      textArea.setText(log.toString());
      EventManager.getInstance().notify3rdPartyVersionUpdate(type);
    }
    else {
      textArea.setText("Check failed. See log for details.");
    }
  }

  @FXML
  private void onInstall() {
    if (client.getPinUPPopperService().isPinUPPopperRunning()) {
      if (Dialogs.openPopperRunningWarning(Studio.stage)) {
        runInstall();
      }
    }
    else {
      runInstall();
    }
  }

  @FXML
  private void onInstallSimulate() {
    run(true);
  }

  @FXML
  private void onCheck() {
    Platform.runLater(() -> {
      try {
        String artifactName = artifactCombo.getValue();
        ComponentCheckProgressModel model = new ComponentCheckProgressModel("Component Check for " + type, type, artifactName);
        ProgressResultModel resultModel = Dialogs.createProgressDialog(model);

        if (!resultModel.getResults().isEmpty()) {
          ComponentActionLogRepresentation log = (ComponentActionLogRepresentation) resultModel.getResults().get(0);
          textArea.setText(log.toString());
        }
        else {
          textArea.setText("Check failed. See log for details.");
        }

        EventManager.getInstance().notify3rdPartyVersionUpdate(type);
      } catch (Exception e) {
        LOG.error("Failed to execute component check: " + e.getMessage(), e);
        WidgetFactory.showAlert(Studio.stage, "Error", "Failed to execute component check: " + e.getMessage());
      }
    });
  }

  private void runInstall() {
    String artifactName = artifactCombo.getValue();
    Optional<ButtonType> result = WidgetFactory.showConfirmation(Studio.stage, "Install Update \"" + artifactName + "\"?", "Existing files will be overwritten.", "Make sure to follow the additional instructions shown below.", "Continue");
    if (result.isPresent() && result.get().equals(ButtonType.OK)) {
      run(false);
    }
  }

  private void run(boolean simulate) {
    textArea.setText("");
    Platform.runLater(() -> {
      try {
        String artifactName = artifactCombo.getValue();
        ComponentInstallProgressModel model = new ComponentInstallProgressModel(type, simulate, artifactName);
        ProgressResultModel resultModel = Dialogs.createProgressDialog(model);

        ComponentActionLogRepresentation log = (ComponentActionLogRepresentation) resultModel.getResults().get(0);
        textArea.setText(log.toString());

        componentTab.postProcessing(simulate);

        EventManager.getInstance().notify3rdPartyVersionUpdate(type);
      } catch (Exception ex) {
        LOG.error("Failed to run component update: " + ex.getMessage(), ex);
        textArea.setText("Action failed: " + ex.getMessage());
      }
    });
  }

  public void setComponent(AbstractComponentTab tab, ComponentRepresentation component) {
    this.componentTab = tab;
    this.type = component.getType();
    this.component = component;

    artifactCombo.valueProperty().addListener((observableValue, s, t1) -> {
      checkBtn.setDisable(t1 == null);
      installBtn.setDisable(t1 == null || !client.getSystemService().isLocal());
      simBtn.setDisable(t1 == null);
    });

    refresh();
  }

  public void refresh() {
    artifactCombo.getItems().clear();
    artifactCombo.setItems(FXCollections.observableList(component.getArtifacts()));

    artifactCombo.setDisable(component.getArtifacts().isEmpty());
    checkBtn.setDisable(component.getArtifacts().isEmpty() || artifactCombo.getValue() == null);
    simBtn.setDisable(component.getArtifacts().isEmpty() || artifactCombo.getValue() == null);
    installBtn.setDisable(component.getArtifacts().isEmpty() || artifactCombo.getValue() == null || !client.getSystemService().isLocal());

    if (component.getArtifacts().size() == 1) {
      artifactCombo.setValue(component.getArtifacts().get(0));
    }

    String systemPreset = client.getSystemPreset();
    if (systemPreset.equals(PreferenceNames.SYSTEM_PRESET_64_BIT)) {
      Optional<String> first = component.getArtifacts().stream().filter(r -> r.contains("x64")).findFirst();
      first.ifPresent(s -> artifactCombo.setValue(s));
    }
    else {
      Optional<String> first = component.getArtifacts().stream().filter(r -> !r.contains("x64")).findFirst();
      first.ifPresent(s -> artifactCombo.setValue(s));
    }
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    EventManager.getInstance().addListener(this);

    simBtn.setDisable(true);
    installBtn.setDisable(true);
    artifactCombo.setDisable(true);
    checkBtn.setDisable(true);
  }

  @Override
  public void thirdPartyVersionUpdated(@NonNull ComponentType type) {
    if (type.equals(this.type)) {
      Platform.runLater(() -> {
        this.component = client.getComponentService().getComponent(type);
        refresh();
      });
    }
  }
}
