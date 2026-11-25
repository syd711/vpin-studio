package de.mephisto.vpin.ui.components.emulators;

import de.mephisto.vpin.commons.utils.JFXFuture;
import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.commons.utils.i18n.Messages;
import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.emulators.GameEmulatorRepresentation;
import de.mephisto.vpin.restclient.emulators.GameEmulatorScript;
import de.mephisto.vpin.restclient.frontend.FrontendType;
import de.mephisto.vpin.restclient.preferences.PreferenceChangeListener;
import de.mephisto.vpin.restclient.preferences.VRSettings;
import de.mephisto.vpin.restclient.system.FolderRepresentation;
import de.mephisto.vpin.restclient.util.FileUtils;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.components.emulators.dialogs.EmulatorDialogs;
import de.mephisto.vpin.ui.events.EventManager;
import de.mephisto.vpin.ui.events.StudioEventListener;
import de.mephisto.vpin.ui.tables.validation.GameEmulatorValidationTexts;
import de.mephisto.vpin.ui.util.FolderChooserDialog;
import de.mephisto.vpin.ui.util.LocalizedValidation;
import de.mephisto.vpin.ui.util.ProgressDialog;
import de.mephisto.vpin.ui.util.SystemUtil;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.Nullable;
import org.kordamp.ikonli.javafx.FontIcon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

import static de.mephisto.vpin.ui.Studio.*;

/**
 * Emulators tab of the System Manager: emulator list on the left, the settings of the selected emulator in the
 * middle and its launch and exit scripts on the right.
 */
public class EmulatorsController implements Initializable, PreferenceChangeListener, StudioEventListener {
  private final static Logger LOG = LoggerFactory.getLogger(EmulatorsController.class);

  private final static String COLOR_OK = "#5CC46A";
  private final static String COLOR_DISABLED = "#7A7A7A";
  private final static String COLOR_ERROR = "#FF6B70";

  @FXML
  private BorderPane emulatorRoot;

  // list
  @FXML
  private Label countLabel;
  @FXML
  private VBox listBox;
  @FXML
  private Label listStatusLabel;
  @FXML
  private Button reloadBtn;
  @FXML
  private Button createBtn;

  // settings
  @FXML
  private Label emulatorNameLabel;
  @FXML
  private Label emulatorIdLabel;
  @FXML
  private Label statusPill;
  @FXML
  private Button duplicateBtn;
  @FXML
  private Button deleteBtn;
  @FXML
  private Button saveBtn;

  @FXML
  private HBox validationBox;
  @FXML
  private Label validationTitle;
  @FXML
  private Label validationText;

  @FXML
  private VBox generalSection;
  @FXML
  private VBox foldersSection;

  @FXML
  private CheckBox enabledCheckbox;
  @FXML
  private TextField safeNameField;
  @FXML
  private TextField nameField;
  @FXML
  private TextField descriptionField;
  @FXML
  private VBox descriptionBox;
  @FXML
  private TextField launchFolderField;
  @FXML
  private TextField gamesFolderField;
  @FXML
  private TextField customField2;
  @FXML
  private Label customField2Label;
  @FXML
  private TextField customField1;
  @FXML
  private Label customField1Label;
  @FXML
  private VBox customField1Box;
  @FXML
  private TextField romsFolderField;
  @FXML
  private VBox romsFolderBox;

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

  // scripts
  @FXML
  private VBox scriptsPanel;
  @FXML
  private TabPane tabPane;
  @FXML
  private Tab startScriptTab;
  @FXML
  private Tab vrStartScriptTab;
  @FXML
  private Tab exitScriptTab;

  private Optional<GameEmulatorRepresentation> emulator = Optional.empty();
  private List<GameEmulatorRepresentation> emulators = new ArrayList<>();

  /**
   * The emulator to select once the running reload is finished, matched by id or, for new emulators, by name
   */
  @Nullable
  private GameEmulatorRepresentation pendingSelection;
  private boolean loading = false;

  private IEmulatorScriptPanel startScriptController;
  private IEmulatorScriptPanel vrStartScriptController;
  private IEmulatorScriptPanel exitScriptController;

  //------------------------------------------- folder actions

  @FXML
  private void onFolderLaunch() {
    openFolder(launchFolderField);
  }

  @FXML
  private void onSelectFolderLaunch() {
    onFolderSelect(launchFolderField);
  }

  @FXML
  private void onFolderGames() {
    openFolder(gamesFolderField);
  }

  @FXML
  private void onSelectFolderGames() {
    onFolderSelect(gamesFolderField);
  }

  @FXML
  private void onFolderRoms() {
    openFolder(romsFolderField);
  }

  @FXML
  private void onSelectFolderRoms() {
    onFolderSelect(romsFolderField);
  }

  @FXML
  private void onFolderMedia() {
    openFolder(customField1);
  }

  @FXML
  private void onSelectFolderMedia() {
    onFolderSelect(customField1);
  }

  private void openFolder(TextField field) {
    if (!StringUtils.isEmpty(field.getText())) {
      SystemUtil.openFolder(new File(field.getText()));
    }
  }

  private void onFolderSelect(TextField field) {
    FolderRepresentation folder = FolderChooserDialog.open(field.getText());
    if (folder != null) {
      field.setText(folder.getPath());
    }
  }

  //------------------------------------------- emulator actions

  @FXML
  private void onSave() {
    if (emulator.isEmpty()) {
      return;
    }
    if (!FileUtils.isValidFilename(safeNameField.getText())) {
      WidgetFactory.showAlert(stage, Messages.get("dialog.invalid_name"), Messages.get("dialog.the_specified_name_contains_invalid_characters"));
      return;
    }

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
    }
    if (exitScriptController != null) {
      exitScriptController.applyValues();
    }
    if (vrStartScriptController != null) {
      vrStartScriptController.applyValues();
      if (emu.isVpxEmulator() && vrStartScriptController.getScript().isPresent()) {
        client.getVRService().saveVrEmulatorLaunchScript(emu.getId(), vrStartScriptController.getScript().get());
      }
    }

    pendingSelection = emu;
    ProgressDialog.createProgressDialog(new EmulatorSaveProgressModel(emu, this));
  }

  @FXML
  private void onDuplicate() {
    if (emulator.isEmpty()) {
      return;
    }
    GameEmulatorRepresentation template = emulator.get();
    String s = WidgetFactory.showInputDialog(Studio.stage, Messages.get("dialog.clone_emulator"), Messages.get("msg.enter_the_folder_save_name_for_the", template.getName()), Messages.get("dialog.you_can_edit_the_additional_emulator_parameters"), null, Messages.get("msg.copy_of", template.getName()));
    if (!StringUtils.isEmpty(s)) {
      if (!FileUtils.isValidFilename(s)) {
        WidgetFactory.showAlert(stage, Messages.get("dialog.invalid_name"), Messages.get("dialog.the_specified_name_contains_invalid_characters"));
        return;
      }

      GameEmulatorRepresentation emu = new GameEmulatorRepresentation();
      emu.setSafeName(s);
      emu.setName(s);
      emu.setId(-1);
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

      GameEmulatorRepresentation saved = client.getEmulatorService().saveGameEmulator(emu);
      select(saved);
      onReload();
    }
  }

  @FXML
  private void onDelete() {
    if (emulator.isPresent()) {
      GameEmulatorRepresentation emu = emulator.get();
      Optional<ButtonType> result = WidgetFactory.showConfirmation(Studio.stage, Messages.get("dialog.delete_game_emulator"), Messages.get("msg.delete_game_emulator", emu.getName()));
      if (result.isPresent() && result.get().equals(ButtonType.OK)) {
        pendingSelection = null;
        ProgressDialog.createProgressDialog(new EmulatorDeletionProgressModel(emu, this));
      }
    }
  }

  @FXML
  private void onCreate() {
    GameEmulatorRepresentation emulatorRepresentation = EmulatorDialogs.openNewEmulatorDialog();
    if (emulatorRepresentation != null) {
      ProgressDialog.createProgressDialog(new EmulatorCreateProgressModel(emulatorRepresentation, this));
    }
  }

  @FXML
  public void onReload() {
    if (pendingSelection == null) {
      pendingSelection = emulator.orElse(null);
    }
    loading = true;
    reloadBtn.setDisable(true);
    if (emulators.isEmpty()) {
      listStatusLabel.setText(Messages.get("components.emulators.emulators.loading_emulators"));
      listStatusLabel.setVisible(true);
    }

    JFXFuture
        .supplyAsync(() -> {
          client.getEmulatorService().clearCache();
          return client.getEmulatorService().getValidatedGameEmulators();
        })
        .thenAcceptLater(result -> {
          loading = false;
          reloadBtn.setDisable(false);
          emulators = new ArrayList<>(result);
          emulators.sort(Comparator.comparing(e -> StringUtils.defaultString(e.getName()).toLowerCase()));

          GameEmulatorRepresentation toSelect = pendingSelection;
          pendingSelection = null;
          Optional<GameEmulatorRepresentation> match = find(toSelect);
          if (match.isEmpty() && !emulators.isEmpty()) {
            match = Optional.of(emulators.getFirst());
          }
          showEmulator(match);
        })
        .onErrorLater(e -> {
          loading = false;
          reloadBtn.setDisable(false);
          LOG.error("Emulator refresh failed: {}", e.getMessage(), e);
          listStatusLabel.setText(e.getMessage());
          listStatusLabel.setVisible(true);
        });
  }

  //------------------------------------------- selection

  /**
   * Selects the given emulator, used by the navigation and the progress models.
   * When the emulator list is still loading, the selection is applied afterwards.
   */
  public void select(GameEmulatorRepresentation gameEmulator) {
    Optional<GameEmulatorRepresentation> match = find(gameEmulator);
    if (match.isPresent() && !loading) {
      showEmulator(match);
    }
    else {
      pendingSelection = gameEmulator;
    }
  }

  public void setSelection(Optional<GameEmulatorRepresentation> model) {
    if (model.isPresent()) {
      select(model.get());
    }
    else {
      showEmulator(Optional.empty());
    }
  }

  private Optional<GameEmulatorRepresentation> find(@Nullable GameEmulatorRepresentation emu) {
    if (emu == null) {
      return Optional.empty();
    }
    if (emu.getId() >= 0) {
      Optional<GameEmulatorRepresentation> byId = emulators.stream().filter(e -> e.getId() == emu.getId()).findFirst();
      if (byId.isPresent()) {
        return byId;
      }
    }
    return emulators.stream().filter(e -> StringUtils.equals(e.getSafeName(), emu.getSafeName()) || StringUtils.equals(e.getName(), emu.getName())).findFirst();
  }

  private void showEmulator(Optional<GameEmulatorRepresentation> model) {
    this.emulator = model;
    refreshList();
    refreshDetails(model);
  }

  //------------------------------------------- list

  private void refreshList() {
    listBox.getChildren().clear();
    countLabel.setText(emulators.isEmpty() ? "" : String.valueOf(emulators.size()));
    for (GameEmulatorRepresentation emu : emulators) {
      listBox.getChildren().add(createListRow(emu));
    }
    if (emulators.isEmpty() && !loading) {
      listStatusLabel.setText(Messages.get("components.emulators.no_emulators"));
    }
    listStatusLabel.setVisible(emulators.isEmpty());
  }

  private HBox createListRow(GameEmulatorRepresentation emu) {
    boolean selected = emulator.isPresent() && emulator.get().getId() == emu.getId();
    boolean valid = emu.getValidationStates().isEmpty();

    HBox row = new HBox(10);
    row.setAlignment(Pos.CENTER_LEFT);
    row.setPadding(new Insets(7, 10, 7, 10));
    row.getStyleClass().add("sm-list-row");
    if (selected) {
      row.getStyleClass().add("sm-list-row-selected");
    }

    Node status;
    if (!valid) {
      FontIcon icon = new FontIcon("mdi2a-alert-circle");
      icon.setIconSize(16);
      icon.setIconColor(javafx.scene.paint.Paint.valueOf(emu.isEnabled() ? COLOR_ERROR : COLOR_DISABLED));
      status = icon;
    }
    else {
      Region dot = new Region();
      dot.setMinSize(8, 8);
      dot.setMaxSize(8, 8);
      dot.setStyle("-fx-background-radius: 4; -fx-background-color: " + (emu.isEnabled() ? COLOR_OK : COLOR_DISABLED) + ";");
      StackPane box = new StackPane(dot);
      box.setMinWidth(16);
      status = box;
    }

    Label name = new Label(emu.getName());
    name.setStyle("-fx-font-size: 14px; -fx-text-fill: " + (emu.isEnabled() ? "white" : COLOR_DISABLED) + ";" + (selected ? " -fx-font-weight: bold;" : ""));
    Label meta = new Label(getListMeta(emu));
    meta.getStyleClass().add("sm-small");
    VBox texts = new VBox(1, name, meta);
    texts.setMinWidth(0);
    HBox.setHgrow(texts, Priority.ALWAYS);

    row.getChildren().addAll(status, texts);

    if (emu.getType() != null && !StringUtils.isEmpty(emu.getType().shortName())) {
      Label type = new Label(emu.getType().shortName());
      type.getStyleClass().addAll("sm-pill", "sm-pill-unknown");
      type.setMinWidth(Region.USE_PREF_SIZE);
      row.getChildren().add(type);
    }

    if (!valid) {
      LocalizedValidation validation = GameEmulatorValidationTexts.validate(emu);
      if (validation != null) {
        Tooltip.install(row, new Tooltip(validation.getLabel()));
      }
    }

    row.setOnMouseClicked(e -> showEmulator(Optional.of(emu)));
    return row;
  }

  private String getListMeta(GameEmulatorRepresentation emu) {
    List<String> parts = new ArrayList<>();
    if (!emu.isEnabled()) {
      parts.add(Messages.get("components.emulators.status_disabled"));
    }
    if (!StringUtils.isEmpty(emu.getGameExt())) {
      parts.add("." + emu.getGameExt());
    }
    if (!StringUtils.isEmpty(emu.getGamesDirectory())) {
      parts.add(emu.getGamesDirectory());
    }
    return String.join(" · ", parts);
  }

  //------------------------------------------- details

  private void refreshDetails(Optional<GameEmulatorRepresentation> model) {
    int tabIndex = tabPane.getSelectionModel().getSelectedIndex();
    boolean empty = model.isEmpty();

    emulatorNameLabel.setText(Messages.get("components.emulators.no_selection"));
    emulatorIdLabel.setText("");
    statusPill.setVisible(false);
    validationBox.setVisible(false);

    enabledCheckbox.setSelected(false);
    safeNameField.setText("");
    nameField.setText("");
    descriptionField.setText("");
    launchFolderField.setText("");
    gamesFolderField.setText("");
    customField2.setText("");
    customField1.setText("");
    romsFolderField.setText("");

    generalSection.setDisable(empty);
    foldersSection.setDisable(empty);
    safeNameField.setDisable(empty || !Features.EMULATORS_CRUD);
    duplicateBtn.setDisable(empty);
    deleteBtn.setDisable(empty);
    saveBtn.setDisable(empty);

    boolean vrEnabled = false;
    if (model.isPresent()) {
      GameEmulatorRepresentation emu = model.get();

      emulatorNameLabel.setText(emu.getName());
      List<String> meta = new ArrayList<>();
      if (emu.getType() != null && !StringUtils.isEmpty(emu.getType().shortName())) {
        meta.add(emu.getType().shortName());
      }
      meta.add(Messages.get("components.emulators.id", emu.getId()));
      if (!StringUtils.isEmpty(emu.getEmulatorVersion())) {
        meta.add(emu.getEmulatorVersion());
      }
      emulatorIdLabel.setText(String.join(" · ", meta));

      boolean valid = emu.getValidationStates().isEmpty();
      statusPill.getStyleClass().removeAll("sm-pill-ok", "sm-pill-warn", "sm-pill-unknown", "sm-pill-error");
      if (!emu.isEnabled()) {
        statusPill.setText(Messages.get("components.emulators.status_disabled"));
        statusPill.getStyleClass().add("sm-pill-unknown");
      }
      else if (!valid) {
        statusPill.setText(Messages.get("components.emulators.status_invalid"));
        statusPill.getStyleClass().add("sm-pill-error");
      }
      else {
        statusPill.setText(Messages.get("components.emulators.status_active"));
        statusPill.getStyleClass().add("sm-pill-ok");
      }
      statusPill.setVisible(true);

      LocalizedValidation validation = GameEmulatorValidationTexts.validate(emu);
      if (validation != null) {
        validationTitle.setText(validation.getLabel());
        validationText.setText(validation.getText());
        validationText.setVisible(!StringUtils.isEmpty(validation.getText()));
        validationBox.setVisible(true);
      }

      enabledCheckbox.setSelected(emu.isEnabled());
      safeNameField.setText(emu.getSafeName());
      nameField.setText(emu.getName());
      descriptionField.setText(emu.getDescription());
      launchFolderField.setText(emu.getInstallationDirectory());
      gamesFolderField.setText(emu.getGamesDirectory());
      romsFolderField.setText(emu.getRomDirectory());

      FrontendType frontendType = client.getFrontendService().getFrontendType();
      if (frontendType.equals(FrontendType.Popper)) {
        customField2.setText(emu.getGameExt());
        customField1.setText(emu.getMediaDirectory());
      }
      else if (frontendType.equals(FrontendType.PinballX)) {
        customField2.setText(emu.getExeName());
        customField1.setText(emu.getExeParameters());
      }

      if (emu.isVpxEmulator()) {
        client.getPreferenceService().clearCache(PreferenceNames.VR_SETTINGS);
        VRSettings vrSettings = client.getPreferenceService().getJsonPreference(PreferenceNames.VR_SETTINGS, VRSettings.class);
        vrEnabled = vrSettings.isEnabled();

        if (vrStartScriptController != null) {
          GameEmulatorScript vrLaunchScript = client.getVRService().getVrLaunchScript(emu.getId());
          vrStartScriptController.setData(model, Optional.of(vrLaunchScript != null ? vrLaunchScript : new GameEmulatorScript()));
        }
      }
    }

    if (startScriptController != null) {
      startScriptController.setData(model, model.map(GameEmulatorRepresentation::getLaunchScript));
      startScriptTab.setDisable(empty);
      startScriptController.setDisabled(empty);
    }
    if (vrStartScriptController != null) {
      vrStartScriptTab.setDisable(empty || !vrEnabled);
      vrStartScriptController.setDisabled(empty || !vrEnabled);
    }
    if (exitScriptController != null) {
      exitScriptController.setData(model, model.map(GameEmulatorRepresentation::getExitScript));
      exitScriptTab.setDisable(empty);
      exitScriptController.setDisabled(empty);
    }

    // keep the selected script tab unless it got disabled
    if (tabIndex >= 0 && !tabPane.getTabs().get(tabIndex).isDisable()) {
      tabPane.getSelectionModel().select(tabIndex);
    }
    else {
      tabPane.getSelectionModel().select(0);
    }
  }

  //------------------------------------------- view lifecycle

  public void onViewActivated() {
    client.getPreferenceService().notifyPreferenceChange(PreferenceNames.VR_SETTINGS, null);
  }

  public void onViewDeactivated() {
  }

  private IEmulatorScriptPanel loadScriptPanel(String fxml, Tab tab) {
    try {
      FXMLLoader loader = new FXMLLoader(EmulatorsController.class.getResource(fxml));
      loader.setResources(Messages.getBundle());
      Parent root = loader.load();
      tab.setContent(root);
      return loader.getController();
    }
    catch (IOException e) {
      LOG.error("Failed to load emulator script panel {}: {}", fxml, e.getMessage(), e);
      return null;
    }
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    for (Node node : List.of(createBtn, duplicateBtn, deleteBtn, statusPill, validationBox, validationText, descriptionBox, romsFolderBox,
        selectFolderButtonMedia, openFolderButtonMedia, scriptsPanel)) {
      node.managedProperty().bind(node.visibleProperty());
    }

    FrontendType frontendType = client.getFrontendService().getFrontendType();
    if (frontendType.equals(FrontendType.Popper)) {
      startScriptController = loadScriptPanel("panel-emulator-script.fxml", startScriptTab);
      vrStartScriptController = loadScriptPanel("panel-emulator-script.fxml", vrStartScriptTab);
      exitScriptController = loadScriptPanel("panel-emulator-script.fxml", exitScriptTab);
    }
    else if (frontendType.equals(FrontendType.PinballX)) {
      descriptionBox.setVisible(false);
      romsFolderBox.setVisible(false);

      // PinballX has executable and parameters instead of file extension and media folder
      customField2Label.setText(Messages.get("components.emulators.executable"));
      customField1Label.setText(Messages.get("components.emulators.parameters"));
      selectFolderButtonMedia.setVisible(false);
      openFolderButtonMedia.setVisible(false);
      foldersSection.getChildren().remove(customField1Box);
      generalSection.getChildren().add(customField1Box);

      startScriptController = loadScriptPanel("panel-emulator-batscript.fxml", startScriptTab);
      vrStartScriptController = loadScriptPanel("panel-emulator-batscript.fxml", vrStartScriptTab);
      exitScriptController = loadScriptPanel("panel-emulator-batscript.fxml", exitScriptTab);
    }
    else {
      scriptsPanel.setVisible(false);
    }

    createBtn.setVisible(Features.EMULATORS_CRUD);
    deleteBtn.setVisible(Features.EMULATORS_CRUD);
    duplicateBtn.setVisible(Features.EMULATORS_CRUD);

    showEmulator(Optional.empty());
    onReload();

    client.getPreferenceService().addListener(this);
    EventManager.getInstance().addListener(this);
  }

  @Override
  public void vrModeEnabled(boolean b) {
    Platform.runLater(() -> refreshDetails(this.emulator));
  }

  @Override
  public void preferencesChanged(String key, Object value) {
    if (PreferenceNames.VR_SETTINGS.equals(key)) {
      Platform.runLater(() -> refreshDetails(this.emulator));
    }
  }
}
