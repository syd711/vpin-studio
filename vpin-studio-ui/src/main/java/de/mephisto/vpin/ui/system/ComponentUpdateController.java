package de.mephisto.vpin.ui.system;

import de.mephisto.vpin.restclient.components.ComponentActionLogRepresentation;
import de.mephisto.vpin.restclient.components.ComponentRepresentation;
import de.mephisto.vpin.restclient.components.ComponentType;
import de.mephisto.vpin.ui.WaitOverlayController;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.scene.layout.StackPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import static de.mephisto.vpin.ui.Studio.client;

public class ComponentUpdateController implements Initializable {
  private final static Logger LOG = LoggerFactory.getLogger(ComponentUpdateController.class);

  @FXML
  private Button simBtn;

  @FXML
  private Button okBtn;

  @FXML
  private TextArea textArea;

  @FXML
  private StackPane loaderStack;

  @FXML
  private ComboBox<String> artifactCombo;

  private Parent tablesLoadingOverlay;

  private ComponentType type;

  @FXML
  private void onInstallSimulate() {
    run(true);
  }

  @FXML
  private void onOkClick() {
    run(false);
  }

  private void run(boolean simulate) {
    textArea.setText("");
    simBtn.setDisable(true);
    okBtn.setDisable(true);

    try {
      FXMLLoader loader = new FXMLLoader(WaitOverlayController.class.getResource("overlay-wait.fxml"));
      tablesLoadingOverlay = loader.load();
      WaitOverlayController ctrl = loader.getController();
      ctrl.setLoadingMessage("Installing Update...");
    } catch (IOException e) {
      LOG.error("Failed to load loading overlay: " + e.getMessage());
    }

    loaderStack.getChildren().add(tablesLoadingOverlay);

    Platform.runLater(() -> {
      try {
        String artifactName = artifactCombo.getValue();

        if (simulate) {
          ComponentActionLogRepresentation install = client.getComponentService().simulate(type, artifactName);
          processResult(install);
        }
        else {
          ComponentActionLogRepresentation install = client.getComponentService().install(type, artifactName);
          processResult(install);
        }
        loaderStack.getChildren().remove(tablesLoadingOverlay);
      } catch (Exception ex) {
        LOG.error("Failed to run component update: " + ex.getMessage(), ex);
        textArea.setText("Action failed: " + ex.getMessage());
      }

      simBtn.setDisable(false);
      okBtn.setDisable(false);
    });
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    simBtn.setDisable(true);
    okBtn.setDisable(true);
  }

  public void setComponentType(ComponentType type) {
    this.type = type;

    ComponentRepresentation component = client.getComponentService().getComponent(type);
    artifactCombo.setItems(FXCollections.observableList(component.getArtifacts()));
    artifactCombo.valueProperty().addListener((observableValue, s, t1) -> {
      simBtn.setDisable(t1 == null);
      okBtn.setDisable(t1 == null);
    });
  }

  private void processResult(ComponentActionLogRepresentation install) {
    StringBuilder result = new StringBuilder();
    install.getLogs().stream().forEach(l -> result.append(l + "\n"));


    if (install.isSimulated()) {
      if (install.getStatus() != null) {
        result.append("\nSIMULATION FAILED\n");
        result.append(install.getStatus());
      }
      else {
        result.append("\nSIMULATION SUCCESSFUL\n");
      }
    }
    else {
      if (install.getStatus() != null) {
        result.append("\nINSTALLATION FAILED\n");
        result.append(install.getStatus());
      }
      else {
        result.append("\nINSTALLATION SUCCESSFUL\n");
      }
    }

    textArea.setText(result.toString());
  }
}
