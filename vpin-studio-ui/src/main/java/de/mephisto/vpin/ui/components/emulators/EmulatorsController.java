package de.mephisto.vpin.ui.components.emulators;

import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.restclient.emulators.GameEmulatorRepresentation;
import de.mephisto.vpin.restclient.frontend.FrontendType;
import de.mephisto.vpin.ui.Studio;
import javafx.application.Platform;
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

import static de.mephisto.vpin.ui.Studio.client;

public class EmulatorsController implements Initializable {
  private final static Logger LOG = LoggerFactory.getLogger(EmulatorsController.class);

  @FXML
  private BorderPane emulatorRoot;

  @FXML
  private BorderPane tableRoot;

  @FXML
  private Label emulatorNameLabel;

  @FXML
  private Label emulatorIdLabel;

  @FXML
  private Button saveBtn;

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

  @FXML
  private Button deleteBtn;

  @FXML
  private Button createBtn;

  @FXML
  private Separator firstSeparator;


  private Optional<GameEmulatorRepresentation> emulator = Optional.empty();

  private EmulatorsTableController tableController;

  private EmulatorScriptPanelController startScriptController;
  private EmulatorScriptPanelController exitScriptController;

  private boolean saveDisabled = false;

  @FXML
  private void onSave() {
    GameEmulatorRepresentation emu = emulator.get();
    emu.setEnabled(enabledCheckbox.isSelected());
    emu.setName(nameField.getText());
    emu.setDescription(descriptionField.getText());
    emu.setLaunchScript(startScriptController.getData());
    emu.setExitScript(exitScriptController.getData());
    emu.setGamesDirectory(gamesFolderField.getText());
    emu.setRomDirectory(romsFolderField.getText());
    emu.setInstallationDirectory(launchFolderField.getText());
    emu.setGameExt(fileExtensionField.getText());

    client.getEmulatorService().saveGameEmulator(emulator.get());
    Platform.runLater(() -> {
      tableController.reload();
    });
  }

  @FXML
  private void onDelete() {
    if (emulator.isPresent()) {
      GameEmulatorRepresentation gameEmulatorRepresentation = emulator.get();
      Optional<ButtonType> result = WidgetFactory.showConfirmation(Studio.stage, "Delete Game Emulator", "Delete Game Emulator \"" + gameEmulatorRepresentation.getName() + "\"?");
      if (result.isPresent() && result.get().equals(ButtonType.OK)) {
        client.getEmulatorService().deleteGameEmulator(gameEmulatorRepresentation.getId());
        onReload();
      }
    }
  }

  @FXML
  private void onCreate() {

  }

  @FXML
  private void onReload() {
    client.getEmulatorService().clearCache();
    tableController.reload();
  }

  public void setSelection(Optional<GameEmulatorRepresentation> model) {
    saveDisabled = true;
//    tableController.setSelection(model);
    this.emulator = model;

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

    saveDisabled = false;
  }

  public void onViewDeactivated() {

  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    createBtn.managedProperty().bindBidirectional(createBtn.visibleProperty());
    deleteBtn.managedProperty().bindBidirectional(deleteBtn.visibleProperty());
    firstSeparator.managedProperty().bindBidirectional(firstSeparator.visibleProperty());

    try {
      FXMLLoader loader = new FXMLLoader(EmulatorsTableController.class.getResource("table-emulators.fxml"));
      Parent builtInRoot = loader.load();
      tableController = loader.getController();
      tableController.setEmulatorController(this);
      tableRoot.setCenter(builtInRoot);
    }
    catch (IOException e) {
      LOG.error("Failed to load emulator table: " + e.getMessage(), e);
    }

    FrontendType frontendType = client.getFrontendService().getFrontendType();
    if (frontendType.equals(FrontendType.Popper)) {
      loadPopperFrontend();
    }
    else if (frontendType.equals(FrontendType.PinballX) || frontendType.equals(FrontendType.PinballY)) {

    }

    createBtn.setVisible(frontendType.supportEmulatorCreateDelete());
    deleteBtn.setVisible(frontendType.supportEmulatorCreateDelete());
    firstSeparator.setVisible(frontendType.supportEmulatorCreateDelete());

    emulatorRoot.widthProperty().addListener(new ChangeListener<Number>() {
      @Override
      public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
        tabPane.setPrefWidth(newValue.intValue() - tableController.getTableView().getWidth() - 30);
      }
    });

    emulatorRoot.heightProperty().addListener(new ChangeListener<Number>() {
      @Override
      public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
        tabPane.setPrefHeight(newValue.intValue() - 426);
      }
    });

    setSelection(Optional.empty());

    Platform.runLater(() -> {
      tabPane.setPrefWidth(emulatorRoot.widthProperty().intValue() - tableController.getTableView().getWidth() - 30);
      tabPane.setPrefHeight(emulatorRoot.heightProperty().intValue() - 426);
    });
  }

  private void loadPopperFrontend() {
    try {
      FXMLLoader loader = new FXMLLoader(EmulatorScriptPanelController.class.getResource("panel-emulator-script.fxml"));
      Parent builtInRoot = loader.load();
      startScriptController = loader.getController();
      startScriptTab.setContent(builtInRoot);
    }
    catch (IOException e) {
      LOG.error("Failed to load emulator table: " + e.getMessage(), e);
    }


    try {
      FXMLLoader loader = new FXMLLoader(EmulatorScriptPanelController.class.getResource("panel-emulator-script.fxml"));
      Parent builtInRoot = loader.load();
      exitScriptController = loader.getController();
      exitScriptTab.setContent(builtInRoot);
    }
    catch (IOException e) {
      LOG.error("Failed to load emulator table: " + e.getMessage(), e);
    }
  }
}
