package de.mephisto.vpin.ui.system;

import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.restclient.components.ComponentActionLogRepresentation;
import de.mephisto.vpin.restclient.components.ComponentRepresentation;
import de.mephisto.vpin.restclient.components.ComponentType;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.events.EventManager;
import de.mephisto.vpin.ui.util.Dialogs;
import de.mephisto.vpin.ui.util.ProgressResultModel;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.scene.layout.StackPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.ResourceBundle;

public class ComponentUpdateController implements Initializable {
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
  private StackPane loaderStack;

  @FXML
  private ComboBox<String> artifactCombo;

  private ComponentType type;

  @FXML
  private void onInstall() {
    run(false);
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

        if(!resultModel.getResults().isEmpty()) {
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

  private void run(boolean simulate) {
    textArea.setText("");
    Platform.runLater(() -> {
      try {
        String artifactName = artifactCombo.getValue();
        ComponentInstallProgressModel model = new ComponentInstallProgressModel(type, simulate, artifactName);
        ProgressResultModel resultModel = Dialogs.createProgressDialog(model);

        ComponentActionLogRepresentation log = (ComponentActionLogRepresentation) resultModel.getResults().get(0);
        textArea.setText(log.toString());
      } catch (Exception ex) {
        LOG.error("Failed to run component update: " + ex.getMessage(), ex);
        textArea.setText("Action failed: " + ex.getMessage());
      }
    });
  }

  public void setComponent(ComponentRepresentation component) {
    this.type = component.getType();
    artifactCombo.setItems(FXCollections.observableList(component.getArtifacts()));
    artifactCombo.valueProperty().addListener((observableValue, s, t1) -> {
      checkBtn.setDisable(t1 == null);
      installBtn.setDisable(t1 == null);
      simBtn.setDisable(t1 == null);
    });

    artifactCombo.setDisable(component.getArtifacts().isEmpty());
    checkBtn.setDisable(component.getArtifacts().isEmpty() || artifactCombo.getValue() == null);
    simBtn.setDisable(component.getArtifacts().isEmpty() || artifactCombo.getValue() == null);
    installBtn.setDisable(component.getArtifacts().isEmpty() || artifactCombo.getValue() == null);
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    simBtn.setDisable(true);
    installBtn.setDisable(true);
    artifactCombo.setDisable(true);
    checkBtn.setDisable(true);
  }
}
