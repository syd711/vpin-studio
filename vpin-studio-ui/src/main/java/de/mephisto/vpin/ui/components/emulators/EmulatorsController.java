package de.mephisto.vpin.ui.components.emulators;

import de.mephisto.vpin.commons.utils.JFXFuture;
import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.restclient.emulators.GameEmulatorRepresentation;
import de.mephisto.vpin.restclient.frontend.EmulatorType;
import de.mephisto.vpin.restclient.frontend.FrontendType;
import de.mephisto.vpin.restclient.util.FileUtils;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.util.StudioFolderChooser;
import de.mephisto.vpin.ui.util.SystemUtil;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

import static de.mephisto.vpin.ui.Studio.Features;
import static de.mephisto.vpin.ui.Studio.client;
import static de.mephisto.vpin.ui.Studio.stage;

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
  private TextField safeNameField;

  @FXML
  private TextField nameField;

  @FXML
  private TextField descriptionField;

  @FXML
  private TextField launchFolderField;

  @FXML
  private TextField gamesFolderField;

  @FXML
  private TextField customField2;

  @FXML
  private TextField customField1;

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
  private Button duplicateBtn;

  @FXML
  private Label customField1Label;

  @FXML
  private Label customField2Label;

  @FXML
  private Label descriptionLabel;

  @FXML
  private Label romsFolderLabel;

  @FXML
  private Separator firstSeparator;

  @FXML
  private Button openFolderButtonLaunch;

  @FXML
  private Button openFolderButtonGames;

  @FXML
  private Button openFolderButtonRoms;

  @FXML
  private Button openFolderButtonMedia;

  @FXML
  private Button selectFolderButtonLaunch;

  @FXML
  private Button selectFolderButtonGames;

  @FXML
  private Button selectFolderButtonRoms;

  @FXML
  private Button selectFolderButtonMedia;

  @FXML
  private Pane emuScrollRoot;

  @FXML
  private Pane emuScrollChild;

  @FXML
  private ScrollPane emuScrollPane;

  private Optional<GameEmulatorRepresentation> emulator = Optional.empty();

  private EmulatorsTableController tableController;

  private IEmulatorScriptPanel startScriptController;
  private IEmulatorScriptPanel exitScriptController;

  @FXML
  private void onFolderLaunch() {
    if (!StringUtils.isEmpty(launchFolderField.getText())) {
      File folder = new File(launchFolderField.getText());
      SystemUtil.openFolder(folder);
    }
  }

  @FXML
  private void onSelectFolderLaunch(ActionEvent event) {
    onFolderSelect(event, launchFolderField);
  }

  @FXML
  private void onFolderGames() {
    if (!StringUtils.isEmpty(gamesFolderField.getText())) {
      File folder = new File(gamesFolderField.getText());
      SystemUtil.openFolder(folder);
    }
  }

  @FXML
  private void onSelectFolderGames(ActionEvent event) {
    onFolderSelect(event, gamesFolderField);
  }

  @FXML
  private void onFolderRoms() {
    if (!StringUtils.isEmpty(romsFolderField.getText())) {
      File folder = new File(romsFolderField.getText());
      SystemUtil.openFolder(folder);
    }
  }

  @FXML
  private void onSelectFolderRoms(ActionEvent event) {
    onFolderSelect(event, romsFolderField);
  }

  @FXML
  private void onFolderMedia() {
    if (!StringUtils.isEmpty(customField1.getText())) {
      File folder = new File(customField1.getText());
      SystemUtil.openFolder(folder);
    }
  }

  @FXML
  private void onSelectFolderMedia(ActionEvent event) {
    onFolderSelect(event, customField1);
  }


  @FXML
  private void onSave() {
    saveBtn.setDisable(true);
    if (!FileUtils.isValidFilename(safeNameField.getText())) {
      WidgetFactory.showAlert(stage, "Invalid Name", "The specified name contains invalid characters.");
      return;
    }

    JFXFuture.runAsync(() -> {
      FrontendType frontendType = client.getFrontendService().getFrontendType();

      GameEmulatorRepresentation emu = emulator.get();
      emu.setEnabled(enabledCheckbox.isSelected());
      emu.setSafeName(safeNameField.getText());
      emu.setName(nameField.getText());
      emu.setDescription(descriptionField.getText());
      emu.setGamesDirectory(gamesFolderField.getText());
      emu.setRomDirectory(romsFolderField.getText());
      emu.setInstallationDirectory(launchFolderField.getText());

      if (frontendType.equals(FrontendType.Popper)) {
        emu.setGameExt(customField2.getText());
        emu.setMediaDirectory(customField1.getText());
      }
      else if (frontendType.equals(FrontendType.PinballX)) {
        emu.setExeName(customField2.getText());
        emu.setExeParameters(customField1.getText());
      }

      if (startScriptController != null) {
        startScriptController.applyValues();
        exitScriptController.applyValues();
      }

      client.getEmulatorService().saveGameEmulator(emu);
    }).thenLater(() -> {
      onReload();
      saveBtn.setDisable(false);
    });
  }

  @FXML
  private void onDuplicate() {
    GameEmulatorRepresentation template = emulator.get();
    String s = WidgetFactory.showInputDialog(Studio.stage, "Clone Emulator", "Enter the folder save name for the copy of \"" + emulator.get().getName() + "\".", "You can edit the additional emulator parameters afterwards.", null, "Copy of " + template);
    if (!StringUtils.isEmpty(s)) {
      if (!FileUtils.isValidFilename(s)) {
        WidgetFactory.showAlert(stage, "Invalid Name", "The specified name contains invalid characters.");
        return;
      }

      GameEmulatorRepresentation emu = new GameEmulatorRepresentation();
      emu.setSafeName(s);
      emu.setName(s);
      emu.setType(template.getType());
      emu.setDescription(template.getDescription());
      emu.setMediaDirectory(template.getMediaDirectory());
      emu.setRomDirectory(template.getRomDirectory());
      emu.setGameExt(template.getGameExt());
      emu.setGamesDirectory(template.getGamesDirectory());
      emu.setLaunchScript(template.getLaunchScript());
      emu.setExitScript(template.getExitScript());
      emu.setExeName(template.getExeName());
      emu.setInstallationDirectory(template.getInstallationDirectory());
      emu.setExeParameters(template.getExeParameters());

      GameEmulatorRepresentation gameEmulatorRepresentation = client.getEmulatorService().saveGameEmulator(emu);
      onReload();
      tableController.select(gameEmulatorRepresentation);
    }
  }

  @FXML
  private void onDelete() {
    if (emulator.isPresent()) {
      GameEmulatorRepresentation gameEmulatorRepresentation = emulator.get();
      Optional<ButtonType> result = WidgetFactory.showConfirmation(Studio.stage, "Delete Game Emulator", "Delete Game Emulator \"" + gameEmulatorRepresentation.getName() + "\"?");
      if (result.isPresent() && result.get().equals(ButtonType.OK)) {
        client.getEmulatorService().deleteGameEmulator(gameEmulatorRepresentation.getId());
        client.getGameService().clearCache(gameEmulatorRepresentation.getId());
        onReload();
      }
    }
  }

  @FXML
  private void onCreate() {
    String s = WidgetFactory.showInputDialog(Studio.stage, "New Emulator", "Enter the folder save name of the new emulator.", "You can edit the additional emulator parameters afterwards.", null, "Visual Pinball");
    if (!StringUtils.isEmpty(s)) {
      if (!FileUtils.isValidFilename(s)) {
        WidgetFactory.showAlert(stage, "Invalid Name", "The specified name contains invalid characters.");
        return;
      }

      GameEmulatorRepresentation emu = new GameEmulatorRepresentation();
      emu.setSafeName(s);
      emu.setName(s);
      emu.setType(EmulatorType.OTHER);
      GameEmulatorRepresentation gameEmulatorRepresentation = client.getEmulatorService().saveGameEmulator(emu);
      onReload();

      tableController.select(gameEmulatorRepresentation);
    }
  }

  @FXML
  private void onReload() {
    client.getEmulatorService().clearCache();
    tableController.reload();
  }

  public void setSelection(Optional<GameEmulatorRepresentation> model) {
    this.emulator = model;

    emulatorNameLabel.setText("-");
    emulatorIdLabel.setText("-");

    enabledCheckbox.setSelected(false);
    enabledCheckbox.setDisable(model.isEmpty());
    safeNameField.setText("");
    safeNameField.setDisable(model.isEmpty() || !Features.EMULATORS_CRUD);
    nameField.setText("");
    nameField.setDisable(model.isEmpty());
    descriptionField.setText("");
    descriptionField.setDisable(model.isEmpty());
    launchFolderField.setText("");
    launchFolderField.setDisable(model.isEmpty());
    gamesFolderField.setText("");
    gamesFolderField.setDisable(model.isEmpty());
    customField2.setText("");
    customField2.setDisable(model.isEmpty());
    customField1.setText("");
    customField1.setDisable(model.isEmpty());
    romsFolderField.setText("");
    romsFolderField.setDisable(model.isEmpty());

    duplicateBtn.setDisable(model.isEmpty());
    saveBtn.setDisable(model.isEmpty());
    deleteBtn.setDisable(model.isEmpty());

    if (startScriptController != null) {
      startScriptController.setData(model, model.map(GameEmulatorRepresentation::getLaunchScript));
      exitScriptController.setData(model, model.map(GameEmulatorRepresentation::getExitScript));
    }


    if (model.isPresent()) {
      GameEmulatorRepresentation emulator = model.get();

      emulatorNameLabel.setText(emulator.getName());
      emulatorIdLabel.setText("(ID #" + emulator.getId() + ")");

      enabledCheckbox.setSelected(emulator.isEnabled());
      safeNameField.setText(emulator.getSafeName());
      nameField.setText(emulator.getName());
      descriptionField.setText(emulator.getDescription());
      launchFolderField.setText(emulator.getInstallationDirectory());
      gamesFolderField.setText(emulator.getGamesDirectory());
      romsFolderField.setText(emulator.getRomDirectory());

      FrontendType frontendType = client.getFrontendService().getFrontendType();

      if (frontendType.equals(FrontendType.Popper)) {
        customField2.setText(emulator.getGameExt());
        customField1.setText(emulator.getMediaDirectory());
      }
      else if (frontendType.equals(FrontendType.PinballX)) {
        customField2.setText(emulator.getExeName());
        customField1.setText(emulator.getExeParameters());
      }
    }
  }

  public void onViewActivated() {
    Platform.runLater(() -> {
      refreshTableWidth();

      stage.setWidth(stage.getWidth() - 5);
      stage.setWidth(stage.getWidth() + 5);
    });
  }

  public void onViewDeactivated() {

  }

  private void refreshTableWidth() {
    Number newValue = stage.getWidth();
    if (newValue.intValue() < 1800) {
      tableRoot.setPrefWidth(400);
    }
    else {
      tableRoot.setPrefWidth(-1);
    }
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

  private void loadPinballXFrontend() {
    descriptionField.setVisible(false);
    descriptionLabel.setVisible(false);
    romsFolderLabel.setVisible(false);
    romsFolderField.setVisible(false);
    customField1Label.setText("Parameters:");
    customField2Label.setText("Executable:");

    try {
      FXMLLoader loader = new FXMLLoader(EmulatorBatScriptPanelController.class.getResource("panel-emulator-batscript.fxml"));
      Parent builtInRoot = loader.load();
      startScriptController = loader.getController();
      startScriptTab.setContent(builtInRoot);
    }
    catch (IOException e) {
      LOG.error("Failed to load emulator table: " + e.getMessage(), e);
    }
    try {
      FXMLLoader loader = new FXMLLoader(EmulatorScriptPanelController.class.getResource("panel-emulator-batscript.fxml"));
      Parent builtInRoot = loader.load();
      exitScriptController = loader.getController();
      exitScriptTab.setContent(builtInRoot);
    }
    catch (IOException e) {
      LOG.error("Failed to load emulator table: " + e.getMessage(), e);
    }
  }

  private void onFolderSelect(ActionEvent event, TextField field) {
    Stage stage = (Stage) ((Button) event.getSource()).getScene().getWindow();
    StudioFolderChooser chooser = new StudioFolderChooser();
    chooser.setTitle("Select Folder");

    String value = field.getText();
    if(!StringUtils.isEmpty(value) && new File(value).exists()) {
      chooser.setInitialDirectory(new File(value));
    }

    File targetFolder = chooser.showOpenDialog(stage);

    if (targetFolder != null) {
      field.setText(targetFolder.getAbsolutePath());
    }
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    createBtn.managedProperty().bindBidirectional(createBtn.visibleProperty());
    duplicateBtn.managedProperty().bindBidirectional(duplicateBtn.visibleProperty());
    deleteBtn.managedProperty().bindBidirectional(deleteBtn.visibleProperty());
    firstSeparator.managedProperty().bindBidirectional(firstSeparator.visibleProperty());
    selectFolderButtonLaunch.managedProperty().bindBidirectional(selectFolderButtonLaunch.visibleProperty());
    selectFolderButtonGames.managedProperty().bindBidirectional(selectFolderButtonGames.visibleProperty());
    selectFolderButtonRoms.managedProperty().bindBidirectional(selectFolderButtonRoms.visibleProperty());
    selectFolderButtonMedia.managedProperty().bindBidirectional(selectFolderButtonMedia.visibleProperty());

    openFolderButtonLaunch.managedProperty().bindBidirectional(openFolderButtonLaunch.visibleProperty());
    openFolderButtonLaunch.setVisible(client.getSystemService().isLocal());

    openFolderButtonMedia.managedProperty().bindBidirectional(openFolderButtonMedia.visibleProperty());
    openFolderButtonMedia.setVisible(client.getSystemService().isLocal());

    openFolderButtonRoms.managedProperty().bindBidirectional(openFolderButtonRoms.visibleProperty());
    openFolderButtonRoms.setVisible(client.getSystemService().isLocal());

    openFolderButtonGames.managedProperty().bindBidirectional(openFolderButtonGames.visibleProperty());
    openFolderButtonGames.setVisible(client.getSystemService().isLocal());

    try {
      FXMLLoader loader = new FXMLLoader(EmulatorsTableController.class.getResource("table-emulators.fxml"));
      Parent parent = loader.load();
      tableController = loader.getController();
      tableController.setEmulatorController(this);
      tableRoot.setCenter(parent);
    }
    catch (IOException e) {
      LOG.error("Failed to load emulator table: " + e.getMessage(), e);
    }

    stage.widthProperty().addListener(new ChangeListener<Number>() {
      @Override
      public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
        refreshTableWidth();
      }
    });

    FrontendType frontendType = client.getFrontendService().getFrontendType();
    if (frontendType.equals(FrontendType.Popper)) {
      loadPopperFrontend();
    }
    else if (frontendType.equals(FrontendType.PinballX)) {
      openFolderButtonRoms.setVisible(false);
      selectFolderButtonRoms.setVisible(false);

      openFolderButtonMedia.setVisible(false);
      selectFolderButtonMedia.setVisible(false);
      loadPinballXFrontend();
    }
    else {
      tabPane.setVisible(false);
    }

    createBtn.setVisible(Features.EMULATORS_CRUD);
    deleteBtn.setVisible(Features.EMULATORS_CRUD);
    duplicateBtn.setVisible(Features.EMULATORS_CRUD);
    firstSeparator.setVisible(Features.EMULATORS_CRUD);

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

      refreshTableWidth();
    });
  }
}
