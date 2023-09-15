package de.mephisto.vpin.ui.tables;

import de.mephisto.vpin.commons.utils.FileUtils;
import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.restclient.SystemSummary;
import de.mephisto.vpin.restclient.jobs.JobExecutionResult;
import de.mephisto.vpin.restclient.popper.ScreenMode;
import de.mephisto.vpin.restclient.representations.GameRepresentation;
import de.mephisto.vpin.restclient.representations.PupPackRepresentation;
import de.mephisto.vpin.restclient.representations.ValidationState;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.events.EventManager;
import de.mephisto.vpin.ui.tables.drophandler.PupPackFileDropEventHandler;
import de.mephisto.vpin.ui.util.LocalizedValidation;
import de.mephisto.vpin.ui.tables.validation.GameValidationTexts;
import de.mephisto.vpin.ui.util.Dialogs;
import de.mephisto.vpin.ui.util.FileDragEventHandler;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import org.apache.commons.lang3.StringUtils;
import org.kordamp.ikonli.javafx.FontIcon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.File;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class TablesSidebarPUPPackController implements Initializable {
  private final static Logger LOG = LoggerFactory.getLogger(TablesSidebarPUPPackController.class);

  private Optional<GameRepresentation> game = Optional.empty();

  @FXML
  private Button uploadBtn;

  @FXML
  private Button reloadBtn;

  @FXML
  private Button openBtn;

  @FXML
  private Label lastModifiedLabel;

  @FXML
  private Label bundleSizeLabel;

  @FXML
  private VBox emptyDataBox;

  @FXML
  private VBox dataBox;

  @FXML
  private CheckBox scriptOnlyCheckbox;

  @FXML
  private ComboBox<String> optionsCombo;

  @FXML
  private ComboBox<String> txtsCombo;

  @FXML
  private CheckBox enabledCheckbox;

  @FXML
  private FontIcon screenBackglass;

  @FXML
  private FontIcon screenDMD;

  @FXML
  private FontIcon screenFullDMD;

  @FXML
  private FontIcon screenTopper;

  @FXML
  private Tooltip screenBackglassTooltip;

  @FXML
  private Tooltip screenDMDTooltip;

  @FXML
  private Tooltip screenFullDMDTooltip;

  @FXML
  private Tooltip screenTopperTooltip;

  @FXML
  private Button applyBtn;

  @FXML
  private VBox errorBox;

  @FXML
  private Label errorTitle;

  @FXML
  private Label errorText;

  @FXML
  private Button pupPackEditorBtn;

  @FXML
  private Pane pupRoot;

  @FXML
  private VBox screensPanel;

  private TablesSidebarController tablesSidebarController;
  private PupPackRepresentation pupPack;
  private ValidationState validationState;

  // Add a public no-args constructor
  public TablesSidebarPUPPackController() {
  }

  @FXML
  private void onOptionApply() {
    String option = optionsCombo.getValue();
    if (!StringUtils.isEmpty(option)) {
      Optional<ButtonType> result = WidgetFactory.showConfirmation(Studio.stage, "Apply \"" + option + "\"?", "The existing settings will be overwritten.");
      if (result.isPresent() && result.get().equals(ButtonType.OK)) {
        JobExecutionResult jobExecutionResult = null;
        try {
          jobExecutionResult = Studio.client.getPupPackService().option(game.get().getId(), option);
          refreshView(game);

          if (!StringUtils.isEmpty(jobExecutionResult.getError())) {
            WidgetFactory.showAlert(Studio.stage, "Option Execution Failed", jobExecutionResult.getError());
          }

          if (!StringUtils.isEmpty(jobExecutionResult.getMessage())) {
            WidgetFactory.showOutputDialog(Studio.stage, "Option Command Result", option, "The command returned this output:", jobExecutionResult.getMessage());
          }
        } catch (Exception e) {
          LOG.error("Failed to execute PUP command: " + e.getMessage(), e);
          WidgetFactory.showAlert(Studio.stage, "Option Execution Failed", e.getMessage());
        }
      }
    }
  }

  @FXML
  private void onPupPackEnable() {
    if (game.isPresent() && game.get().isPupPackAvailable()) {
      GameRepresentation g = game.get();
      Studio.client.getPupPackService().setPupPackEnabled(g.getId(), enabledCheckbox.isSelected());
      EventManager.getInstance().notifyTableChange(g.getId(), g.getRom());
    }
  }

  @FXML
  private void onReload() {
    this.reloadBtn.setDisable(true);

    Platform.runLater(() -> {
      new Thread(() -> {
        Studio.client.getPupPackService().clearCache();

        this.game.ifPresent(gameRepresentation -> EventManager.getInstance().notifyTableChange(gameRepresentation.getId(), gameRepresentation.getRom()));

        Platform.runLater(() -> {
          this.reloadBtn.setDisable(false);
          this.refreshView(this.game);
        });
      }).start();
    });
  }

  @FXML
  private void onOpen() {
    String value = txtsCombo.getValue();
    if (!StringUtils.isEmpty(value)) {
      File file = new File(pupPack.getPath(), value);
      Dialogs.openFile(file);
    }
  }

  @FXML
  private void onPupPackEditor() {
    Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
    if (desktop != null && desktop.isSupported(Desktop.Action.OPEN)) {
      try {
        SystemSummary systemSummary = Studio.client.getSystemService().getSystemSummary();
        File file = new File(systemSummary.getPinupSystemDirectory(), "PinUpPackEditor.exe");
        if (!file.exists()) {
          WidgetFactory.showAlert(Studio.stage, "Did not find PinUpPackEditor.exe", "The exe file " + file.getAbsolutePath() + " was not found.");
        }
        else {
          desktop.open(file);
        }
      } catch (Exception e) {
        LOG.error("Failed to open PinUpPackEditor: " + e.getMessage(), e);
      }
    }
  }

  @FXML
  private void onDismiss() {
    GameRepresentation g = game.get();
    tablesSidebarController.getTablesController().dismissValidation(g, this.validationState);
  }

  @FXML
  private void onUpload() {
    if (game.isPresent()) {
      GameRepresentation g = game.get();
      if (StringUtils.isEmpty(g.getRom())) {
        WidgetFactory.showAlert(Studio.stage, "No ROM name found for \"" + g.getGameDisplayName() + "\".", "To upload a PUP pack, a ROM name must have been resolved for the table.");
        return;
      }

      Dialogs.openPupPackUploadDialog(tablesSidebarController, game.get(), null);
    }
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    dataBox.managedProperty().bindBidirectional(dataBox.visibleProperty());
    emptyDataBox.managedProperty().bindBidirectional(emptyDataBox.visibleProperty());
    errorBox.managedProperty().bindBidirectional(errorBox.visibleProperty());
    errorBox.setVisible(false);
    dataBox.setVisible(false);
    emptyDataBox.setVisible(true);

    openBtn.setText("View");
    if(Studio.client.getSystemService().isLocal()) {
      openBtn.setText("Edit");
    }

    pupPackEditorBtn.setDisable(!Studio.client.getSystemService().isLocal());

    optionsCombo.valueProperty().addListener((observable, oldValue, newValue) -> applyBtn.setDisable(StringUtils.isEmpty(newValue)));
    txtsCombo.valueProperty().addListener((observable, oldValue, newValue) -> openBtn.setDisable(StringUtils.isEmpty(newValue)));
  }

  public void setGame(Optional<GameRepresentation> game) {
    this.game = game;
    this.refreshView(game);
  }

  public void refreshView(Optional<GameRepresentation> g) {
    this.pupPack = null;
    this.validationState = null;
    reloadBtn.setDisable(g.isEmpty());
    enabledCheckbox.setDisable(g.isEmpty());
    scriptOnlyCheckbox.setSelected(false);

    screensPanel.setVisible(true);

    dataBox.setVisible(false);
    emptyDataBox.setVisible(true);
    uploadBtn.setDisable(true);
    openBtn.setDisable(true);

    txtsCombo.getItems().clear();
    txtsCombo.setItems(FXCollections.emptyObservableList());
    txtsCombo.setDisable(true);

    bundleSizeLabel.setText("-");
    lastModifiedLabel.setText("-");

    optionsCombo.getItems().clear();
    optionsCombo.setItems(FXCollections.emptyObservableList());
    optionsCombo.setDisable(true);
    applyBtn.setDisable(true);

    screenBackglass.setVisible(false);
    screenDMD.setVisible(false);
    screenTopper.setVisible(false);
    screenFullDMD.setVisible(false);

    screenBackglassTooltip.setText("");
    screenDMDTooltip.setText("");
    screenTopperTooltip.setText("");
    screenFullDMDTooltip.setText("");

    errorBox.setVisible(false);

    if (g.isPresent()) {
      GameRepresentation game = g.get();
      pupPack = Studio.client.getPupPackService().getPupPack(game.getId());
      boolean pupPackAvailable = pupPack != null;
      scriptOnlyCheckbox.setSelected(pupPackAvailable && pupPack.isScriptOnly());
      screensPanel.setVisible(pupPackAvailable && !pupPack.isScriptOnly());
      enabledCheckbox.setDisable(!pupPackAvailable || StringUtils.isEmpty(game.getRom()));

      dataBox.setVisible(pupPackAvailable);
      emptyDataBox.setVisible(!pupPackAvailable);

      uploadBtn.setDisable(StringUtils.isEmpty(game.getRom()));
      enabledCheckbox.setSelected(false);

      if (pupPackAvailable) {
        enabledCheckbox.setSelected(pupPack.isEnabled());

        List<String> options = pupPack.getOptions();
        optionsCombo.setItems(FXCollections.observableList(options));
        optionsCombo.setDisable(options.isEmpty());

        List<String> txts = pupPack.getTxtFiles();
        txtsCombo.setItems(FXCollections.observableList(txts));
        txtsCombo.setDisable(txts.isEmpty());

        applyBtn.setDisable(true);
        if (!StringUtils.isEmpty(pupPack.getSelectedOption())) {
          optionsCombo.setValue(pupPack.getSelectedOption());
          applyBtn.setDisable(true);
        }

        if (!pupPack.getScreenBackglassMode().equals(ScreenMode.off)) {
          screenBackglassTooltip.setText(pupPack.getScreenBackglassMode().name());
          screenBackglass.setVisible(true);
        }

        if (!pupPack.getScreenDMDMode().equals(ScreenMode.off)) {
          screenDMDTooltip.setText(pupPack.getScreenDMDMode().name());
          screenDMD.setVisible(true);
        }

        if (!pupPack.getScreenFullDMDMode().equals(ScreenMode.off)) {
          screenFullDMDTooltip.setText(pupPack.getScreenFullDMDMode().name());
          screenFullDMD.setVisible(true);
        }

        if (!pupPack.getScreenTopperMode().equals(ScreenMode.off)) {
          screenTopperTooltip.setText(pupPack.getScreenTopperMode().name());
          screenTopper.setVisible(true);
        }

        bundleSizeLabel.setText(FileUtils.readableFileSize(pupPack.getSize()));
        lastModifiedLabel.setText(SimpleDateFormat.getDateTimeInstance().format(pupPack.getModificationDate()));

        List<ValidationState> validationStates = pupPack.getValidationStates();
        errorBox.setVisible(!validationStates.isEmpty());
        if (!validationStates.isEmpty()) {
          validationState = validationStates.get(0);
          LocalizedValidation validationResult = GameValidationTexts.getValidationResult(game, validationState);
          errorTitle.setText(validationResult.getLabel());
          errorText.setText(validationResult.getText());
        }
      }
    }
  }

  public void setSidebarController(TablesSidebarController tablesSidebarController) {
    this.tablesSidebarController = tablesSidebarController;

    pupRoot.setOnDragOver(new FileDragEventHandler(pupRoot, true, "zip"));
    pupRoot.setOnDragDropped(new PupPackFileDropEventHandler(tablesSidebarController));
  }
}