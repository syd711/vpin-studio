package de.mephisto.vpin.ui.components.emulators;

import de.mephisto.vpin.restclient.emulators.GameEmulatorRepresentation;
import de.mephisto.vpin.restclient.emulators.GameEmulatorScript;
import de.mephisto.vpin.restclient.frontend.FrontendType;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

import static de.mephisto.vpin.ui.Studio.client;

public class EmulatorBatScriptPanelController implements Initializable, IEmulatorScriptPanel {
  private final static Logger LOG = LoggerFactory.getLogger(EmulatorBatScriptPanelController.class);

  @FXML
  private Node root;

  @FXML
  private CheckBox enabledCheckbox;
  @FXML
  private CheckBox waitForExitCheckbox;
  @FXML
  private CheckBox hideWindowCheckbox;

  @FXML
  private TextField workingDirectoryField;
  @FXML
  private TextField executableField;
  @FXML
  private TextField parametersField;

  @FXML
  private VBox infoContainer;

  private Optional<GameEmulatorScript> script;


  public void setData(Optional<GameEmulatorRepresentation> emulator, Optional<GameEmulatorScript> model) {
    FrontendType frontendType = client.getFrontendService().getFrontendType();

    boolean disabledFields = frontendType.equals(FrontendType.PinballX) && emulator.isPresent() && emulator.get().getType().isVpxEmulator();
    infoContainer.setVisible(disabledFields);

    this.script = model;

    enabledCheckbox.setSelected(false);
    enabledCheckbox.setDisable(model.isEmpty() || disabledFields);
    waitForExitCheckbox.setSelected(false);
    waitForExitCheckbox.setDisable(model.isEmpty() || disabledFields);
    hideWindowCheckbox.setSelected(false);
    hideWindowCheckbox.setDisable(model.isEmpty() || disabledFields);

    workingDirectoryField.setText("");
    workingDirectoryField.setDisable(model.isEmpty() || disabledFields);
    executableField.setText("");
    executableField.setDisable(model.isEmpty() || disabledFields);
    parametersField.setText("");
    parametersField.setDisable(model.isEmpty() || disabledFields);

    if (model.isPresent()) {
      GameEmulatorScript script = model.get();
      enabledCheckbox.setSelected(script.isEnabled());
      waitForExitCheckbox.setSelected(script.isWaitForExit());
      hideWindowCheckbox.setSelected(script.isHideWindow());
      workingDirectoryField.setText(script.getWorkingDirectory());
      executableField.setText(script.getExecuteable());
      parametersField.setText(script.getParameters());
    }
  }

  @Override
  public void applyValues() {
    GameEmulatorScript s = this.script.get();
    s.setEnabled(enabledCheckbox.isSelected());
    s.setWaitForExit(waitForExitCheckbox.isSelected());
    s.setHideWindow(hideWindowCheckbox.isSelected());
    s.setWorkingDirectory(workingDirectoryField.getText());
    s.setExecuteable(executableField.getText());
    s.setParameters(parametersField.getText());
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    infoContainer.managedProperty().bindBidirectional(infoContainer.visibleProperty());
  }
}
