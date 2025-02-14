package de.mephisto.vpin.ui.components.emulators;

import de.mephisto.vpin.restclient.frontend.FrontendType;
import de.mephisto.vpin.restclient.games.GameEmulatorRepresentation;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.function.Consumer;

import static de.mephisto.vpin.ui.Studio.client;

public class EmulatorsController implements Initializable {
  private final static Logger LOG = LoggerFactory.getLogger(EmulatorsController.class);

  @FXML
  private BorderPane emulatorRoot;

  @FXML
  private Label emulatorNameLabel;

  @FXML
  private Label emulatorIdLabel;

  @FXML
  private CheckBox enabledCheckbox;

  @FXML
  private TextField nameField;

  @FXML
  private TextField descriptionField;

  @FXML
  private TextField launchFolderField;

  @FXML
  private TextField gamesFolderField;

  @FXML
  private TextField fileExtensionField;

  @FXML
  private TextField mediaFolderField;

  @FXML
  private TextField romsFolderField;

  @FXML
  private TabPane tabPane;

  @FXML
  private Tab startScriptTab;

  @FXML
  private Tab exitScriptTab;

  private Optional<GameEmulatorRepresentation> emulator = Optional.empty();

  private EmulatorsTableController tableController;

  private EmulatorScriptPanelController startScriptController;
  private EmulatorScriptPanelController exitScriptController;


  @FXML
  private void onReload() {
    tableController.reload();
  }

  public void setSelection(Optional<GameEmulatorRepresentation> model) {
//    tableController.setSelection(model);

    emulatorNameLabel.setText("");
    emulatorIdLabel.setText("");

    enabledCheckbox.setSelected(false);
    enabledCheckbox.setDisable(model.isEmpty());
    nameField.setText("");
    nameField.setDisable(model.isEmpty());
    descriptionField.setText("");
    descriptionField.setDisable(model.isEmpty());
    launchFolderField.setText("");
    launchFolderField.setDisable(model.isEmpty());
    gamesFolderField.setText("");
    gamesFolderField.setDisable(model.isEmpty());
    fileExtensionField.setText("");
    fileExtensionField.setDisable(model.isEmpty());
    mediaFolderField.setText("");
    mediaFolderField.setDisable(model.isEmpty());
    romsFolderField.setText("");
    romsFolderField.setDisable(model.isEmpty());

    if (startScriptController != null) {
      startScriptController.setData(model, model.isPresent() ? model.get().getLaunchScript() : "");
      exitScriptController.setData(model, model.isPresent() ? model.get().getExitScript() : "");
    }

    if (model.isPresent()) {
      GameEmulatorRepresentation emulator = model.get();

      emulatorNameLabel.setText(emulator.getName());
      emulatorIdLabel.setText("(ID #" + emulator.getId() + ")");

      enabledCheckbox.setSelected(emulator.isEnabled());
      nameField.setText(emulator.getName());
      descriptionField.setText(emulator.getDescription());
      launchFolderField.setText(emulator.getInstallationDirectory());
      gamesFolderField.setText(emulator.getGamesDirectory());
      fileExtensionField.setText(emulator.getGameExt());
      mediaFolderField.setText(emulator.getMameDirectory());
      romsFolderField.setText(emulator.getRomDirectory());
    }
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {

    try {
      FXMLLoader loader = new FXMLLoader(EmulatorsTableController.class.getResource("table-emulators.fxml"));
      Parent builtInRoot = loader.load();
      tableController = loader.getController();
      tableController.setEmulatorController(this);
      emulatorRoot.setLeft(builtInRoot);
    }
    catch (IOException e) {
      LOG.error("Failed to load emulator table: " + e.getMessage(), e);
    }

    FrontendType frontendType = client.getFrontendService().getFrontendType();
    if (frontendType.equals(FrontendType.Popper)) {
      try {
        FXMLLoader loader = new FXMLLoader(EmulatorScriptPanelController.class.getResource("panel-emulator-script.fxml"));
        Parent builtInRoot = loader.load();
        startScriptController = loader.getController();
        startScriptController.setCallback(new Consumer<String>() {
          @Override
          public void accept(String s) {
            if (emulator.isPresent()) {
              emulator.get().setLaunchScript(s);
            }
          }
        });
        startScriptTab.setContent(builtInRoot);
      }
      catch (IOException e) {
        LOG.error("Failed to load emulator table: " + e.getMessage(), e);
      }
      try {
        FXMLLoader loader = new FXMLLoader(EmulatorScriptPanelController.class.getResource("panel-emulator-script.fxml"));
        Parent builtInRoot = loader.load();
        exitScriptController = loader.getController();
        exitScriptController.setCallback(new Consumer<String>() {
          @Override
          public void accept(String s) {
            if (emulator.isPresent()) {
              emulator.get().setExitScript(s);
            }
          }
        });
        exitScriptTab.setContent(builtInRoot);
      }
      catch (IOException e) {
        LOG.error("Failed to load emulator table: " + e.getMessage(), e);
      }

      emulatorRoot.widthProperty().addListener(new ChangeListener<Number>() {
        @Override
        public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
          tabPane.setPrefWidth(newValue.intValue() - 780);
        }
      });

      emulatorRoot.heightProperty().addListener(new ChangeListener<Number>() {
        @Override
        public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
          tabPane.setPrefHeight(newValue.intValue() - 426);
        }
      });
    }
  }
}
