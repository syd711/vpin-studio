package de.mephisto.vpin.ui.preferences;

import de.mephisto.vpin.commons.fx.Debouncer;
import de.mephisto.vpin.commons.utils.localsettings.LocalUISettings;
import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.emulators.GameEmulatorRepresentation;
import de.mephisto.vpin.restclient.preferences.UISettings;
import de.mephisto.vpin.restclient.util.OSUtil;
import de.mephisto.vpin.ui.PreferencesController;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.events.EventManager;
import de.mephisto.vpin.ui.util.StudioFolderChooser;
import de.mephisto.vpin.ui.util.SystemUtil;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

import static de.mephisto.vpin.ui.Studio.Features;
import static de.mephisto.vpin.ui.Studio.client;
import static de.mephisto.vpin.ui.Studio.stage;

public class ClientSettingsPreferencesController implements Initializable {
  private final static Logger LOG = LoggerFactory.getLogger(ClientSettingsPreferencesController.class);

  @FXML
  private VBox emulatorList;

  @FXML
  private VBox dropIns;

  @FXML
  private TextField winNetworkShare;

  @FXML
  private Button winNetworkShareTestBtn;

  @FXML
  private Label winNetworkShareStatusLabel;

  @FXML
  private CheckBox uiShowVersion;

  @FXML
  private CheckBox autoEditCheckbox;

  @FXML
  private CheckBox dropInFolderCheckbox;

  @FXML
  private Button dropInFolderButton;

  @FXML
  private TextField dropInTextField;

  @FXML
  private CheckBox sectionAltColor;
  @FXML
  private CheckBox sectionAltSound;
  @FXML
  private CheckBox sectionBackglass;
  @FXML
  private CheckBox sectionDMD;
  @FXML
  private CheckBox sectionHighscore;
  @FXML
  private CheckBox sectionAssets;
  @FXML
  private CheckBox sectionPov;
  @FXML
  private CheckBox sectionPupPack;
  @FXML
  private CheckBox sectionPlaylists;
  @FXML
  private CheckBox sectionScriptDetails;
  @FXML
  private CheckBox sectionTableData;
  @FXML
  private CheckBox sectionVPinMAME;
  @FXML
  private CheckBox sectionVps;

  @FXML
  private CheckBox columnAltColor;
  @FXML
  private CheckBox columnAltSound;
  @FXML
  private CheckBox columnBackglass;
  @FXML
  private CheckBox columnDateAdded;
  @FXML
  private CheckBox columnDateModified;
  @FXML
  private CheckBox columnLauncher;
  @FXML
  private CheckBox columnComment;
  @FXML
  private CheckBox columnHighscore;
  @FXML
  private CheckBox columnEmulator;
  @FXML
  private CheckBox columnPinVol;
  @FXML
  private CheckBox columnIni;
  @FXML
  private CheckBox columnTutorials;
  @FXML
  private CheckBox columnPlaylists;
  @FXML
  private CheckBox columnRating;
  @FXML
  private CheckBox columnPatchVersion;
  @FXML
  private CheckBox columnPov;
  @FXML
  private CheckBox columnRes;
  @FXML
  private CheckBox columnPupPack;
  @FXML
  private CheckBox columnRom;
  @FXML
  private CheckBox columnVersion;
  @FXML
  private CheckBox columnVpsStatus;
  @FXML
  private CheckBox uiHideCustomIcons;

  @FXML
  private VBox networkSettings;

  public static Debouncer debouncer = new Debouncer();
  private String networkShareTestPath;
  private UISettings uiSettings;
  private final boolean supportsNetworkShare = OSUtil.isWindows() || OSUtil.isMac();

  @FXML
  private void onWinShareTest() {
    SystemUtil.publicUrl = winNetworkShare.getText();
    SystemUtil.openFolder(new File(networkShareTestPath));
  }

  @FXML
  private void onDialogReset() {
    Optional<ButtonType> result = WidgetFactory.showConfirmation(stage, "Reset all dialogs?", "All dialog sizes and positions will be resetted.");
    if (result.isPresent() && result.get().equals(ButtonType.OK)) {
      LocalUISettings.reset();
    }
  }

  @FXML
  private void onHideReset() {
    Optional<ButtonType> result = WidgetFactory.showConfirmation(stage, "Reset \"Do not show again\" flags?", "All previously hidden dialogs or panels will be shown again.");
    if (result.isPresent() && result.get().equals(ButtonType.OK)) {
      UISettings uiSettings = client.getPreferenceService().getJsonPreference(PreferenceNames.UI_SETTINGS, UISettings.class);

      uiSettings.setHideComponentWarning(false);
      uiSettings.setHideDismissConfirmations(false);
      uiSettings.setHideVPXStartInfo(false);
      uiSettings.setHideRatingSyncInfo(false);
      uiSettings.setHideFrontendLaunchQuestion(false);

      client.getPreferenceService().setJsonPreference(uiSettings);
      EventManager.getInstance().notifyPreferenceChanged(PreferenceType.uiSettings);
    }
  }

  @FXML
  private void onDropInFolderSelection() {
    StudioFolderChooser chooser = new StudioFolderChooser();
    chooser.setTitle("Select Drop-In Folder");
    File targetFolder = chooser.showOpenDialog(stage);

    if (targetFolder != null && targetFolder.exists()) {
      LocalUISettings.saveProperty(LocalUISettings.DROP_IN_FOLDER, targetFolder.getAbsolutePath());
      dropInTextField.setText(LocalUISettings.getString(LocalUISettings.DROP_IN_FOLDER));
    }
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    columnPupPack.managedProperty().bindBidirectional(columnPupPack.visibleProperty());
    sectionPupPack.managedProperty().bindBidirectional(sectionPupPack.visibleProperty());
    sectionAssets.managedProperty().bindBidirectional(sectionAssets.visibleProperty());
    columnRating.managedProperty().bindBidirectional(columnRating.visibleProperty());
    networkSettings.managedProperty().bindBidirectional(networkSettings.visibleProperty());

    dropIns.managedProperty().bindBidirectional(dropIns.visibleProperty());

    dropInFolderCheckbox.setSelected(LocalUISettings.getBoolean(LocalUISettings.DROP_IN_FOLDER_ENABLED));
    dropInFolderCheckbox.selectedProperty().addListener((observableValue, aBoolean, t1) -> {
      LocalUISettings.saveProperty(LocalUISettings.DROP_IN_FOLDER_ENABLED, t1.toString());
      dropInTextField.setDisable(!t1);
      dropInFolderButton.setDisable(!t1);
    });

    dropInTextField.setDisable(!dropInFolderCheckbox.isSelected());
    dropInTextField.setText(LocalUISettings.getString(LocalUISettings.DROP_IN_FOLDER));
    dropInFolderButton.setDisable(!dropInFolderCheckbox.isSelected());

    sectionPlaylists.managedProperty().bindBidirectional(sectionPlaylists.visibleProperty());
    columnPlaylists.managedProperty().bindBidirectional(columnPlaylists.visibleProperty());

    columnPupPack.setVisible(Features.PUPPACKS_ENABLED);
    sectionPupPack.setVisible(Features.PUPPACKS_ENABLED);

    sectionPlaylists.setVisible(Features.PLAYLIST_ENABLED);
    columnPlaylists.setVisible(Features.PLAYLIST_ENABLED);
    columnRating.setVisible(Features.RATINGS);

    sectionAssets.setVisible(Features.MEDIA_ENABLED);
    dropIns.setVisible(Features.DROP_IN_FOLDER);

    networkShareTestPath = client.getFrontendService().getFrontend().getInstallationDirectory();

    uiSettings = client.getPreferenceService().getJsonPreference(PreferenceNames.UI_SETTINGS, UISettings.class);

    uiShowVersion.setSelected(!uiSettings.isHideVersions());
    uiShowVersion.selectedProperty().addListener((observableValue, aBoolean, t1) -> {
      uiSettings.setHideVersions(!t1);
      PreferencesController.markDirty(PreferenceType.uiSettings);
      client.getPreferenceService().setJsonPreference(uiSettings);
    });

    uiHideCustomIcons.setSelected(uiSettings.isHideCustomIcons());

    uiHideCustomIcons.selectedProperty().addListener((observableValue, aBoolean, t1) -> {
      uiSettings.setHideCustomIcons(t1);
      PreferencesController.markDirty(PreferenceType.uiSettings);
      client.getPreferenceService().setJsonPreference(uiSettings);
    });

    autoEditCheckbox.setSelected(uiSettings.isAutoEditTableData());
    autoEditCheckbox.selectedProperty().addListener((observableValue, aBoolean, t1) -> {
      uiSettings.setAutoEditTableData(t1);
      PreferencesController.markDirty(PreferenceType.uiSettings);
      client.getPreferenceService().setJsonPreference(uiSettings);
    });

    winNetworkShare.setText(uiSettings.getWinNetworkShare());
    winNetworkShare.setDisable(!supportsNetworkShare);
    winNetworkShareStatusLabel.setVisible(supportsNetworkShare && !StringUtils.isEmpty(winNetworkShare.getText()));
    winNetworkShare.textProperty().addListener(new ChangeListener<String>() {
      @Override
      public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
        debouncer.debounce("winNetworkShare", () -> {
          uiSettings.setWinNetworkShare(newValue);

          boolean visible = supportsNetworkShare && !StringUtils.isEmpty(newValue);
          winNetworkShareStatusLabel.setVisible(visible);
          refreshNetworkStatusLabel(newValue);
          client.getPreferenceService().setJsonPreference(uiSettings);
        }, 300);
      }
    });
    winNetworkShareTestBtn.setDisable(!supportsNetworkShare);
    refreshNetworkStatusLabel(uiSettings.getWinNetworkShare());

    List<GameEmulatorRepresentation> gameEmulators = Studio.client.getEmulatorService().getValidatedGameEmulators();
    List<GameEmulatorRepresentation> backglassGameEmulators = Studio.client.getEmulatorService().getBackglassGameEmulators();
    for (GameEmulatorRepresentation gameEmulator : gameEmulators) {
      CheckBox checkBox = new CheckBox(gameEmulator.getName());
      checkBox.setUserData(gameEmulator);
      checkBox.setDisable(gameEmulator.isVpxEmulator() || backglassGameEmulators.contains(gameEmulator));
      if (checkBox.isDisabled()) {
        checkBox.setTooltip(new Tooltip("Emulators with backglasses can not be disabled here."));
      }
      checkBox.setSelected(checkBox.isDisabled() || !uiSettings.getIgnoredEmulatorIds().contains(gameEmulator.getId()));
      checkBox.getStyleClass().add("default-text");
      checkBox.selectedProperty().addListener(new ChangeListener<Boolean>() {
        @Override
        public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
          if (newValue) {
            uiSettings.getIgnoredEmulatorIds().remove(Integer.valueOf(gameEmulator.getId()));
          }
          else {
            if (!uiSettings.getIgnoredEmulatorIds().contains(Integer.valueOf(gameEmulator.getId()))) {
              uiSettings.getIgnoredEmulatorIds().add(Integer.valueOf(gameEmulator.getId()));
            }
          }
          PreferencesController.markDirty(PreferenceType.serverSettings);

          //update the REST client immediately
          client.getGameService().setIgnoredEmulatorIds(uiSettings.getIgnoredEmulatorIds());
          client.getPreferenceService().setJsonPreference(uiSettings);
        }
      });

      emulatorList.getChildren().add(checkBox);
    }

    // --- Sections ----
    sectionAltColor.setSelected(uiSettings.isSectionAltColor());
    sectionAltColor.selectedProperty().addListener((observableValue, aBoolean, t1) -> {
      uiSettings.setSectionAltColor(t1);
      PreferencesController.markDirty(PreferenceType.uiSettings);
      client.getPreferenceService().setJsonPreference(uiSettings);
    });

    sectionAltSound.setSelected(uiSettings.isSectionAltSound());
    sectionAltSound.selectedProperty().addListener((observableValue, aBoolean, t1) -> {
      uiSettings.setSectionAltSound(t1);
      PreferencesController.markDirty(PreferenceType.uiSettings);
      client.getPreferenceService().setJsonPreference(uiSettings);
    });

    sectionBackglass.setSelected(uiSettings.isSectionBackglass());
    sectionBackglass.selectedProperty().addListener((observableValue, aBoolean, t1) -> {
      uiSettings.setSectionBackglass(t1);
      PreferencesController.markDirty(PreferenceType.uiSettings);
      client.getPreferenceService().setJsonPreference(uiSettings);
    });

    sectionDMD.setSelected(uiSettings.isSectionDMD());
    sectionDMD.selectedProperty().addListener((observableValue, aBoolean, t1) -> {
      uiSettings.setSectionDMD(t1);
      PreferencesController.markDirty(PreferenceType.uiSettings);
      client.getPreferenceService().setJsonPreference(uiSettings);
    });

    sectionHighscore.setSelected(uiSettings.isSectionHighscore());
    sectionHighscore.selectedProperty().addListener((observableValue, aBoolean, t1) -> {
      uiSettings.setSectionHighscore(t1);
      PreferencesController.markDirty(PreferenceType.uiSettings);
      client.getPreferenceService().setJsonPreference(uiSettings);
    });

    sectionAssets.setSelected(uiSettings.isSectionAssets());
    sectionAssets.selectedProperty().addListener((observableValue, aBoolean, t1) -> {
      uiSettings.setSectionAssets(t1);
      PreferencesController.markDirty(PreferenceType.uiSettings);
      client.getPreferenceService().setJsonPreference(uiSettings);
    });

    sectionPov.setSelected(uiSettings.isSectionPov());
    sectionPov.selectedProperty().addListener((observableValue, aBoolean, t1) -> {
      uiSettings.setSectionPov(t1);
      PreferencesController.markDirty(PreferenceType.uiSettings);
      client.getPreferenceService().setJsonPreference(uiSettings);
    });

    sectionPupPack.setSelected(uiSettings.isSectionPupPack());
    sectionPupPack.selectedProperty().addListener((observableValue, aBoolean, t1) -> {
      uiSettings.setSectionPupPack(t1);
      PreferencesController.markDirty(PreferenceType.uiSettings);
      client.getPreferenceService().setJsonPreference(uiSettings);
    });

    sectionPlaylists.setSelected(uiSettings.isSectionPlaylists());
    sectionPlaylists.selectedProperty().addListener((observableValue, aBoolean, t1) -> {
      uiSettings.setSectionPlaylists(t1);
      PreferencesController.markDirty(PreferenceType.uiSettings);
      client.getPreferenceService().setJsonPreference(uiSettings);
    });

    sectionScriptDetails.setSelected(uiSettings.isSectionScriptDetails());
    sectionScriptDetails.selectedProperty().addListener((observableValue, aBoolean, t1) -> {
      uiSettings.setSectionScriptDetails(t1);
      PreferencesController.markDirty(PreferenceType.uiSettings);
      client.getPreferenceService().setJsonPreference(uiSettings);
    });

    sectionTableData.setSelected(uiSettings.isSectionTableData());
    sectionTableData.selectedProperty().addListener((observableValue, aBoolean, t1) -> {
      uiSettings.setSectionTableData(t1);
      PreferencesController.markDirty(PreferenceType.uiSettings);
      client.getPreferenceService().setJsonPreference(uiSettings);
    });

    sectionVps.setSelected(uiSettings.isSectionVps());
    sectionVps.selectedProperty().addListener((observableValue, aBoolean, t1) -> {
      uiSettings.setSectionVps(t1);
      PreferencesController.markDirty(PreferenceType.uiSettings);
      client.getPreferenceService().setJsonPreference(uiSettings);
    });

    sectionVPinMAME.setSelected(uiSettings.isSectionVPinMAME());
    sectionVPinMAME.selectedProperty().addListener((observableValue, aBoolean, t1) -> {
      uiSettings.setSectionVPinMAME(t1);
      PreferencesController.markDirty(PreferenceType.uiSettings);
      client.getPreferenceService().setJsonPreference(uiSettings);
    });


    // --- Columns ----
    columnAltColor.setSelected(uiSettings.isColumnAltColor());
    columnAltColor.selectedProperty().addListener((observableValue, aBoolean, t1) -> {
      uiSettings.setColumnAltColor(t1);
      PreferencesController.markDirty(PreferenceType.uiSettings);
      client.getPreferenceService().setJsonPreference(uiSettings);
    });

    columnAltSound.setSelected(uiSettings.isColumnAltSound());
    columnAltSound.selectedProperty().addListener((observableValue, aBoolean, t1) -> {
      uiSettings.setColumnAltSound(t1);
      PreferencesController.markDirty(PreferenceType.uiSettings);
      client.getPreferenceService().setJsonPreference(uiSettings);
    });

    columnBackglass.setSelected(uiSettings.isColumnBackglass());
    columnBackglass.selectedProperty().addListener((observableValue, aBoolean, t1) -> {
      uiSettings.setColumnBackglass(t1);
      PreferencesController.markDirty(PreferenceType.uiSettings);
      client.getPreferenceService().setJsonPreference(uiSettings);
    });

    columnDateAdded.setSelected(uiSettings.isColumnDateAdded());
    columnDateAdded.selectedProperty().addListener((observableValue, aBoolean, t1) -> {
      uiSettings.setColumnDateAdded(t1);
      PreferencesController.markDirty(PreferenceType.uiSettings);
      client.getPreferenceService().setJsonPreference(uiSettings);
    });

    columnLauncher.setSelected(uiSettings.isColumnLauncher());
    columnLauncher.selectedProperty().addListener((observableValue, aBoolean, t1) -> {
      uiSettings.setColumnLauncher(t1);
      PreferencesController.markDirty(PreferenceType.uiSettings);
      client.getPreferenceService().setJsonPreference(uiSettings);
    });

    columnComment.setSelected(uiSettings.isColumnComment());
    columnComment.selectedProperty().addListener((observableValue, aBoolean, t1) -> {
      uiSettings.setColumnComment(t1);
      PreferencesController.markDirty(PreferenceType.uiSettings);
      client.getPreferenceService().setJsonPreference(uiSettings);
    });

    columnDateModified.setSelected(uiSettings.isColumnDateModified());
    columnDateModified.selectedProperty().addListener((observableValue, aBoolean, t1) -> {
      uiSettings.setColumnDateModified(t1);
      PreferencesController.markDirty(PreferenceType.uiSettings);
      client.getPreferenceService().setJsonPreference(uiSettings);
    });

    columnHighscore.setSelected(uiSettings.isColumnHighscore());
    columnHighscore.selectedProperty().addListener((observableValue, aBoolean, t1) -> {
      uiSettings.setColumnHighscore(t1);
      PreferencesController.markDirty(PreferenceType.uiSettings);
      client.getPreferenceService().setJsonPreference(uiSettings);
    });

    columnEmulator.setSelected(uiSettings.isColumnEmulator());
    columnEmulator.selectedProperty().addListener((observableValue, aBoolean, t1) -> {
      uiSettings.setColumnEmulator(t1);
      PreferencesController.markDirty(PreferenceType.uiSettings);
      client.getPreferenceService().setJsonPreference(uiSettings);
    });

    columnIni.setSelected(uiSettings.isColumnIni());
    columnIni.selectedProperty().addListener((observableValue, aBoolean, t1) -> {
      uiSettings.setColumnIni(t1);
      PreferencesController.markDirty(PreferenceType.uiSettings);
      client.getPreferenceService().setJsonPreference(uiSettings);
    });

    columnTutorials.setSelected(uiSettings.isColumnTutorial());
    columnTutorials.selectedProperty().addListener((observableValue, aBoolean, t1) -> {
      uiSettings.setColumnTutorial(t1);
      PreferencesController.markDirty(PreferenceType.uiSettings);
      client.getPreferenceService().setJsonPreference(uiSettings);
    });

    columnPlaylists.setSelected(uiSettings.isColumnPlaylists());
    columnPlaylists.selectedProperty().addListener((observableValue, aBoolean, t1) -> {
      uiSettings.setColumnPlaylists(t1);
      PreferencesController.markDirty(PreferenceType.uiSettings);
      client.getPreferenceService().setJsonPreference(uiSettings);
    });

    columnPatchVersion.setSelected(uiSettings.isColumnPatchVersion());
    columnPatchVersion.selectedProperty().addListener((observableValue, aBoolean, t1) -> {
      uiSettings.setColumnPatchVersion(t1);
      PreferencesController.markDirty(PreferenceType.uiSettings);
      client.getPreferenceService().setJsonPreference(uiSettings);
    });

    columnPov.setSelected(uiSettings.isColumnPov());
    columnPov.selectedProperty().addListener((observableValue, aBoolean, t1) -> {
      uiSettings.setColumnPov(t1);
      PreferencesController.markDirty(PreferenceType.uiSettings);
      client.getPreferenceService().setJsonPreference(uiSettings);
    });

    columnPinVol.setSelected(uiSettings.isColumnPinVol());
    columnPinVol.selectedProperty().addListener((observableValue, aBoolean, t1) -> {
      uiSettings.setColumnPinVol(t1);
      PreferencesController.markDirty(PreferenceType.uiSettings);
      client.getPreferenceService().setJsonPreference(uiSettings);
    });

    columnRating.setSelected(uiSettings.isColumnRating());
    columnRating.selectedProperty().addListener((observableValue, aBoolean, t1) -> {
      uiSettings.setColumnRating(t1);
      PreferencesController.markDirty(PreferenceType.uiSettings);
      client.getPreferenceService().setJsonPreference(uiSettings);
    });

    columnRes.setSelected(uiSettings.isColumnRes());
    columnRes.selectedProperty().addListener((observableValue, aBoolean, t1) -> {
      uiSettings.setColumnRes(t1);
      PreferencesController.markDirty(PreferenceType.uiSettings);
      client.getPreferenceService().setJsonPreference(uiSettings);
    });

    columnPupPack.setSelected(uiSettings.isColumnPupPack());
    columnPupPack.selectedProperty().addListener((observableValue, aBoolean, t1) -> {
      uiSettings.setColumnPupPack(t1);
      PreferencesController.markDirty(PreferenceType.uiSettings);
      client.getPreferenceService().setJsonPreference(uiSettings);
    });

    columnRom.setSelected(uiSettings.isColumnRom());
    columnRom.selectedProperty().addListener((observableValue, aBoolean, t1) -> {
      uiSettings.setColumnRom(t1);
      PreferencesController.markDirty(PreferenceType.uiSettings);
      client.getPreferenceService().setJsonPreference(uiSettings);
    });

    columnVersion.setSelected(uiSettings.isColumnVersion());
    columnVersion.selectedProperty().addListener((observableValue, aBoolean, t1) -> {
      uiSettings.setColumnVersion(t1);
      PreferencesController.markDirty(PreferenceType.uiSettings);
      client.getPreferenceService().setJsonPreference(uiSettings);
    });

    columnVpsStatus.setSelected(uiSettings.isColumnVpsStatus());
    columnVpsStatus.selectedProperty().addListener((observableValue, aBoolean, t1) -> {
      uiSettings.setColumnVpsStatus(t1);
      PreferencesController.markDirty(PreferenceType.uiSettings);
      client.getPreferenceService().setJsonPreference(uiSettings);
    });

  }

  private void refreshNetworkStatusLabel(String newValue) {
    winNetworkShareTestBtn.setDisable(true);
    Platform.runLater(() -> {
      String path = SystemUtil.resolveNetworkPath(newValue, networkShareTestPath);
      if (StringUtils.isEmpty(newValue) || !supportsNetworkShare) {
        winNetworkShareStatusLabel.setVisible(false);
        return;
      }

      String startsWith = OSUtil.isWindows() ? "\\\\" : OSUtil.isMac() ? "smb://" : null;
      if (startsWith == null) {
        winNetworkShareStatusLabel.setText("Network path is not supported on this OS.");
      }
      else if (!newValue.startsWith(startsWith)) {
        winNetworkShareStatusLabel.setText("Network path must begin with " + startsWith + ".");
      }
      else if (path == null) {
        winNetworkShareStatusLabel.setText("No matching path with VPX installation found, using test folder \"" + networkShareTestPath + "\"");
      }
      else {
        winNetworkShareStatusLabel.setText("Test Folder: " + path);
        winNetworkShareTestBtn.setDisable(false);
      }
    });
  }
}
