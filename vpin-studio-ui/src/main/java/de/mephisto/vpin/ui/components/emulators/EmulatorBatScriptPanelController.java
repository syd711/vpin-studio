package de.mephisto.vpin.ui.components.emulators;

import de.mephisto.vpin.restclient.emulators.GameEmulatorRepresentation;
import de.mephisto.vpin.restclient.emulators.GameEmulatorScript;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

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

  private Optional<GameEmulatorScript> script;


  public void setData(Optional<GameEmulatorScript> model) {
    this.script = model;

    enabledCheckbox.setSelected(false);
    enabledCheckbox.setDisable(model.isEmpty());
    waitForExitCheckbox.setSelected(false);
    waitForExitCheckbox.setDisable(model.isEmpty());
    hideWindowCheckbox.setSelected(false);
    hideWindowCheckbox.setDisable(model.isEmpty());

    workingDirectoryField.setText("");
    workingDirectoryField.setDisable(model.isEmpty());
    executableField.setText("");
    executableField.setDisable(model.isEmpty());
    parametersField.setText("");
    parametersField.setDisable(model.isEmpty());

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
  }
}
