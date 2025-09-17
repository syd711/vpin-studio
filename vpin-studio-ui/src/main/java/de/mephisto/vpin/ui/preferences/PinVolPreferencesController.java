package de.mephisto.vpin.ui.preferences;

import de.mephisto.vpin.commons.fx.Debouncer;
import de.mephisto.vpin.commons.utils.JFXFuture;
import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.frontend.Frontend;
import de.mephisto.vpin.restclient.preferences.ServerSettings;
import de.mephisto.vpin.restclient.preferences.UISettings;
import de.mephisto.vpin.restclient.representations.PreferenceEntryRepresentation;
import de.mephisto.vpin.restclient.system.FolderRepresentation;
import de.mephisto.vpin.restclient.util.SystemCommandExecutor;
import de.mephisto.vpin.ui.PreferencesController;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.tables.panels.PinVolSettingsController;
import de.mephisto.vpin.ui.util.FolderChooserDialog;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.*;

import static de.mephisto.vpin.ui.Studio.client;
import static de.mephisto.vpin.ui.Studio.stage;

public class PinVolPreferencesController implements Initializable {
  private final static Logger LOG = LoggerFactory.getLogger(PinVolPreferencesController.class);
  private final Debouncer debouncer = new Debouncer();

  @FXML
  private CheckBox useDefaultFolderCheckBox;

  @FXML
  private GridPane installationPane;

  @FXML
  private TextField installationFolderText;

  @FXML
  private Label pinvolFolderErrorLabel;

  @FXML
  private CheckBox toggleAutoStart;

  @FXML
  private VBox preferenceList;

  @FXML
  private VBox errorContainer;

  @FXML
  private Label pinvolFolderLabel;

  @FXML
  private Button openBtn;

  @FXML
  private Button restartBtn;

  @FXML
  private Button stopBtn;

  @FXML
  private CheckBox columnPinVol;

  @FXML
  private CheckBox muteCheckbox;

  @FXML
  private GridPane adjustSystemVolumePane;

  @FXML
  private Spinner<Integer> volumeSpinner;

  private PinVolSettingsController pinVolController;

  private BooleanProperty notvalid = new SimpleBooleanProperty(false);
  private BooleanProperty running = new SimpleBooleanProperty(false);


  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    errorContainer.managedProperty().bindBidirectional(errorContainer.visibleProperty());

    try {
      FXMLLoader loader = new FXMLLoader(PinVolSettingsController.class.getResource("pinvol-settings.fxml"));
      Parent builtInRoot = loader.load();
      pinVolController = loader.getController();
      pinVolController.setData(stage, Collections.emptyList(), true);
      preferenceList.getChildren().add(builtInRoot);
      builtInRoot.disableProperty().bind(notvalid);
    }
    catch (IOException e) {
      LOG.error("Failed to load pinvol settings panel: " + e.getMessage(), e);
    }

    ServerSettings serverSettings = client.getPreferenceService().getJsonPreference(PreferenceNames.SERVER_SETTINGS, ServerSettings.class);
    UISettings uiSettings = client.getPreferenceService().getJsonPreference(PreferenceNames.UI_SETTINGS, UISettings.class);
    Frontend frontendCached = client.getFrontendService().getFrontend();
    PreferenceEntryRepresentation autostartPreference = client.getPreferenceService().getPreference(PreferenceNames.PINVOL_AUTOSTART_ENABLED);
    PreferenceEntryRepresentation installFolderPreferences = client.getPreferenceService().getPreference(PreferenceNames.PINVOL_FOLDER);

    installationPane.disableProperty().bind(useDefaultFolderCheckBox.selectedProperty());
    useDefaultFolderCheckBox.selectedProperty().addListener((observableValue, integer, selected) -> {
      installationFolderText.setText(selected ? null : "<change me>");
    });

    pinvolFolderErrorLabel.visibleProperty().bind(notvalid);

    openBtn.disableProperty().bind(notvalid.or(new SimpleBooleanProperty(!client.getSystemService().isLocal())));
    restartBtn.disableProperty().bind(notvalid);
    stopBtn.disableProperty().bind(notvalid.or(running.not()));
    toggleAutoStart.disableProperty().bind(notvalid);
    adjustSystemVolumePane.disableProperty().bind(notvalid);

    installationFolderText.textProperty().addListener((observableValue, integer, folder) -> {
      debouncer.debounce("installationFolderText", () -> {
        JFXFuture.runAsync(() -> client.getPreferenceService().setPreference(PreferenceNames.PINVOL_FOLDER, folder))
            .thenLater(() -> refresh(true))
            .onErrorLater(e -> WidgetFactory.showAlert(Studio.stage, "Error", e.getMessage()));
      }, 300);
    });

    String installFolder = installFolderPreferences.getValue();
    if (installFolder == null) {
      useDefaultFolderCheckBox.setSelected(true);
    }
    else {
      useDefaultFolderCheckBox.setSelected(false);
      installationFolderText.setText(installFolder);
    }
    refresh(false);

    boolean volConflict = autostartPreference.getBooleanValue() && frontendCached.isSystemVolumeControlEnabled();
    errorContainer.setVisible(volConflict);

    toggleAutoStart.setSelected(autostartPreference.getBooleanValue());
    toggleAutoStart.selectedProperty().addListener(new ChangeListener<Boolean>() {
      @Override
      public void changed(ObservableValue<? extends Boolean> observableValue, Boolean aBoolean, Boolean t1) {
        client.getPreferenceService().setPreference(PreferenceNames.PINVOL_AUTOSTART_ENABLED, t1);

        boolean volConflict = t1 && frontendCached.isSystemVolumeControlEnabled();
        errorContainer.setVisible(volConflict);
      }
    });

    SpinnerValueFactory.IntegerSpinnerValueFactory factory = new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 100, serverSettings.getVolume());
    volumeSpinner.setValueFactory(factory);
    volumeSpinner.valueProperty().addListener((observable, oldValue, newValue) -> {
      debouncer.debounce("volume", () -> {
        serverSettings.setVolume(newValue);
        client.getPreferenceService().setJsonPreference(serverSettings);
      }, 300);
    });

    columnPinVol.setSelected(uiSettings.isColumnPinVol());
    columnPinVol.selectedProperty().addListener((observableValue, aBoolean, t1) -> {
      uiSettings.setColumnPinVol(t1);
      PreferencesController.markDirty(PreferenceType.uiSettings);
      client.getPreferenceService().setJsonPreference(uiSettings);
    });

    muteCheckbox.setSelected(serverSettings.isInitialMute());
    muteCheckbox.selectedProperty().addListener((observableValue, aBoolean, t1) -> {
      serverSettings.setInitialMute(t1);
      client.getPreferenceService().setJsonPreference(serverSettings);
    });
  }

  private void refresh(boolean clearCache) {
    JFXFuture.supplyAsync(() -> client.getPinVolService().isValid())
      .thenAcceptLater(res -> {
        notvalid.set(!res);
      });

    JFXFuture.supplyAsync(() -> client.getPinVolService().isRunning())
      .thenAcceptLater(res -> {
        running.set(res);
      });

    pinVolController.reload(clearCache);
  }

  @FXML
  private void onFolder(ActionEvent event) {
    FolderRepresentation folder = FolderChooserDialog.open(null);
    if (folder != null) {
      this.installationFolderText.setText(folder.getPath());
    }
  }


  @FXML
  private void onRestart() {
    JFXFuture.supplyAsync(() -> client.getPinVolService().restart())
      .thenAcceptLater(success -> running.set(success));
  }

  @FXML
  private void onStop() {
    JFXFuture.supplyAsync(() -> client.getPinVolService().kill())
      .thenAcceptLater(success -> running.set(!success));
  }

  @FXML
  private void onVolumeApply() {
    client.getPinVolService().setVolume();
  }

  @FXML
  private void onLink() {
    Studio.browse("http://mjrnet.org/pinscape/PinVol.html");
  }

  @FXML
  private void onOpen() {
    if (running.get()) {
      Optional<ButtonType> result = WidgetFactory.showConfirmation(Studio.stage, "PinVol Running", "The \"PinVol.exe\" is currently running. To open the UI, the process will be terminated.",
          "The process has to be restarted afterwards.", "Kill Process");
      if (result.isPresent() && result.get().equals(ButtonType.OK)) {
        JFXFuture.supplyAsync(() -> client.getPinVolService().kill())
          .thenAcceptLater(success -> {
            running.set(!success);
            if (success) {
              openUI();
            }
          });
      }
    } else {
      openUI();
    }
  }

  private void openUI() {
    File file = useDefaultFolderCheckBox.isSelected() ? new File("./resources", "PinVol.exe") : 
          new File(installationFolderText.getText(), "PinVol.exe");
    if (!file.exists()) {
      WidgetFactory.showAlert(Studio.stage, "Did not find PinVol.exe", "The exe file " + file.getAbsolutePath() + " was not found.");
    }
    else {
      try {
        List<String> commands = Arrays.asList(file.getName());
        SystemCommandExecutor executor = new SystemCommandExecutor(commands);
        executor.setDir(file.getParentFile());
        executor.executeCommandAsync();
        LOG.info("Executed PinVol command: " + String.join(" ", commands));
      }
      catch (Exception e) {
        LOG.error("Error opening browser: " + e.getMessage(), e);
        WidgetFactory.showAlert(Studio.stage, "Error", "Error opening browser: " + e.getMessage());
      }
    }
  }
}
