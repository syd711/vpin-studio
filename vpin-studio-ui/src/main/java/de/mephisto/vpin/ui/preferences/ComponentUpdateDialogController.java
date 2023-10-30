package de.mephisto.vpin.ui.preferences;

import de.mephisto.vpin.commons.fx.DialogController;
import de.mephisto.vpin.restclient.components.ComponentRepresentation;
import de.mephisto.vpin.restclient.components.ComponentType;
import de.mephisto.vpin.restclient.components.InstallLogRepresentation;
import de.mephisto.vpin.ui.WaitOverlayController;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import static de.mephisto.vpin.ui.Studio.client;

public class ComponentUpdateDialogController implements Initializable, DialogController {
  private final static Logger LOG = LoggerFactory.getLogger(ComponentUpdateDialogController.class);

  @FXML
  private Button cancelBtn;

  @FXML
  private Label titleLabel;

  @FXML
  private TextArea textArea;

  @FXML
  private StackPane loaderStack;

  @FXML
  private ComboBox artifactCombo;

  private Parent tablesLoadingOverlay;

  private ComponentType type;
  private boolean simulate;

  @FXML
  private void onCancelClick(ActionEvent e) {
    Stage stage = (Stage) ((Button) e.getSource()).getScene().getWindow();
    stage.close();
  }

  @FXML
  private void onOkClick() {
    cancelBtn.setDisable(true);
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
        if (simulate) {
          InstallLogRepresentation install = client.getComponentService().simulate(type);
          processResult(install);
        }
        else {
          InstallLogRepresentation install = client.getComponentService().install(type);
          processResult(install);
        }
        loaderStack.getChildren().remove(tablesLoadingOverlay);
      } catch (Exception ex) {
        LOG.error("Failed to run component update: " + ex.getMessage(), ex);
        textArea.setText("Action failed: " + ex.getMessage());
      }

      cancelBtn.setDisable(false);
    });
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    ComponentRepresentation component = client.getComponentService().getComponent(type);
//    component.
  }

  public void setInstallOptions(ComponentType type, boolean simulate, String label) {
    this.type = type;
    this.simulate = simulate;
    this.titleLabel.setText(label);
  }

  @Override
  public void onDialogCancel() {
  }

  private void processResult(InstallLogRepresentation install) {
    StringBuilder result = new StringBuilder();
    install.getLogs().stream().forEach(l -> result.append(l + "\n"));


    if(install.getStatus() != null) {
      result.append("\nINSTALLATION FAILED\n");
      result.append(install.getStatus());
    }
    else {
      result.append("\nINSTALLATION SUCCESSFUL\n");
    }

    textArea.setText(result.toString());
  }
}
