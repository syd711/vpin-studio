package de.mephisto.vpin.ui.tables.dialogs;

import de.mephisto.vpin.commons.fx.DialogController;
import de.mephisto.vpin.commons.utils.JFXFuture;
import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.commons.utils.i18n.Messages;
import de.mephisto.vpin.restclient.altsound.Rom2AltSoundInfo;
import de.mephisto.vpin.restclient.altsound.Rom2AltSoundOptions;
import de.mephisto.vpin.restclient.altsound.Rom2AltSoundVersion;
import de.mephisto.vpin.restclient.games.GameRepresentation;
import de.mephisto.vpin.restclient.games.descriptors.JobDescriptor;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.jobs.JobPoller;
import de.mephisto.vpin.ui.util.ProgressDialog;
import de.mephisto.vpin.ui.util.ProgressResultModel;
import de.mephisto.vpin.ui.util.WaitProgressModel;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Creates an ALT sound pack from the ROM of a table with rom2altsound.
 */
public class Rom2AltSoundDialogController implements Initializable, DialogController {
  private final static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  @FXML
  private Label installedVersionLabel;

  @FXML
  private Hyperlink latestVersionLink;

  @FXML
  private Button updateBtn;

  @FXML
  private Label tableLabel;

  @FXML
  private Label romLabel;

  @FXML
  private Label romFileLabel;

  @FXML
  private Label gameLabel;

  @FXML
  private Label soundBoardLabel;

  @FXML
  private Label romStatusLabel;

  @FXML
  private Label targetFolderLabel;

  @FXML
  private VBox errorBox;

  @FXML
  private Label errorText;

  @FXML
  private RadioButton volumeFactoryRadio;

  @FXML
  private RadioButton volumeReferenceRadio;

  @FXML
  private Spinner<Integer> maxSecsSpinner;

  @FXML
  private CheckBox enableAltSoundCheckbox;

  @FXML
  private Spinner<Integer> loopMaxSecsSpinner;

  @FXML
  private Spinner<Integer> introLoopSecsSpinner;

  @FXML
  private CheckBox mergeTwinsCheckbox;

  @FXML
  private CheckBox dcBlockCheckbox;

  @FXML
  private CheckBox noFactoryCheckbox;

  @FXML
  private CheckBox checkDuckingCheckbox;

  @FXML
  private CheckBox noChipCheckCheckbox;

  @FXML
  private CheckBox bsmtHleCheckbox;

  @FXML
  private Button createBtn;

  private Stage stage;
  private GameRepresentation game;
  @Nullable
  private Rom2AltSoundVersion version;

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    errorBox.managedProperty().bindBidirectional(errorBox.visibleProperty());
    errorBox.setVisible(false);
    createBtn.setDisable(true);
    updateBtn.setDisable(true);

    maxSecsSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 900, Rom2AltSoundOptions.DEFAULT_MAX_SECS));
    loopMaxSecsSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 900, Rom2AltSoundOptions.DEFAULT_LOOP_MAX_SECS));
    introLoopSecsSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 3600, Rom2AltSoundOptions.DEFAULT_INTRO_LOOP_SECS));
  }

  public void setData(@NonNull Stage stage, @NonNull GameRepresentation game, @Nullable Rom2AltSoundVersion version) {
    this.stage = stage;
    this.game = game;
    tableLabel.setText(game.getGameDisplayName());
    romLabel.setText(game.getRom());
    setVersion(version);
    refreshInfo();
  }

  private void setVersion(@Nullable Rom2AltSoundVersion version) {
    this.version = version;
    String notInstalled = Messages.get("tables.rom2altsound.not_installed");
    installedVersionLabel.setText(version != null && version.getInstalledVersion() != null ? version.getInstalledVersion() : notInstalled);
    latestVersionLink.setText(version != null && version.getLatestVersion() != null ? version.getLatestVersion() : "-");
    latestVersionLink.setDisable(version == null || version.getLatestReleaseUrl() == null);

    boolean installed = version != null && version.isInstalled();
    updateBtn.setText(Messages.get(installed ? "tables.rom2altsound.update" : "tables.rom2altsound.install"));
    // a re-install of the same version is allowed, e.g. for a damaged installation, but only offered as update when newer
    updateBtn.setDisable(version == null || !version.isSupported() || version.getLatestVersion() == null || (installed && !version.isUpdateAvailable()));
  }

  private void refreshInfo() {
    createBtn.setDisable(true);
    errorBox.setVisible(false);
    romFileLabel.setText("-");
    gameLabel.setText("-");
    soundBoardLabel.setText(Messages.get("tables.rom2altsound.checking_rom"));
    romStatusLabel.setText("-");
    targetFolderLabel.setText("-");

    int gameId = game.getId();
    JFXFuture.supplyAsync(() -> Studio.client.getAltSoundService().getRom2AltSoundInfo(gameId))
        .thenAcceptLater(this::setInfo)
        .onErrorLater(e -> {
          LOG.error("Failed to check ROM of game {}: {}", gameId, e.getMessage(), e);
          soundBoardLabel.setText("-");
          showError(e.getMessage());
        });
  }

  private void setInfo(@NonNull Rom2AltSoundInfo info) {
    romFileLabel.setText(StringUtils.defaultIfEmpty(info.getRomFile(), "-"));
    targetFolderLabel.setText(StringUtils.defaultIfEmpty(info.getAltSoundFolder(), "-"));

    List<String> gameParts = new ArrayList<>();
    if (info.getDescription() != null) {
      gameParts.add(info.getDescription());
    }
    if (info.getManufacturer() != null) {
      gameParts.add(info.getManufacturer());
    }
    if (info.getYear() != null) {
      gameParts.add(info.getYear());
    }
    gameLabel.setText(gameParts.isEmpty() ? "-" : String.join(", ", gameParts));

    String board = StringUtils.defaultIfEmpty(info.getSoundBoard(), "-");
    if (!info.getSoundInterfaces().isEmpty()) {
      board += " (" + String.join(", ", info.getSoundInterfaces()) + ")";
    }
    soundBoardLabel.setText(board);

    String status = StringUtils.defaultIfEmpty(info.getStatus(), "-");
    if (!info.getIssues().isEmpty()) {
      status += ": " + String.join(", ", info.getIssues());
    }
    romStatusLabel.setText(status);

    if (info.isAltSoundExists()) {
      showError(Messages.get("tables.rom2altsound.altsound_exists"));
    }
    else if (!info.isSupported()) {
      showError(StringUtils.defaultIfEmpty(info.getError(), Messages.get("tables.rom2altsound.not_supported")));
    }
    createBtn.setDisable(!info.isSupported() || info.isAltSoundExists());
  }

  private void showError(@Nullable String msg) {
    errorText.setText(msg);
    errorBox.setVisible(true);
  }

  @FXML
  private void onProjectLink() {
    Studio.browse(Rom2AltSoundVersion.PROJECT_URL);
  }

  @FXML
  private void onReleaseLink() {
    if (version != null) {
      Studio.browse(version.getLatestReleaseUrl());
    }
  }

  @FXML
  private void onUpdate() {
    Rom2AltSoundVersion updated = install(stage);
    if (updated != null) {
      setVersion(updated);
      refreshInfo();
    }
  }

  @FXML
  private void onCancelClick(ActionEvent e) {
    stage.close();
  }

  @FXML
  private void onCreateClick(ActionEvent e) {
    Rom2AltSoundOptions options = new Rom2AltSoundOptions();
    options.setVolume(volumeReferenceRadio.isSelected() ? Rom2AltSoundOptions.VOLUME_REFERENCE : Rom2AltSoundOptions.VOLUME_FACTORY);
    options.setMaxSecs(maxSecsSpinner.getValue());
    options.setEnableAltSound(enableAltSoundCheckbox.isSelected());
    options.setLoopMaxSecs(loopMaxSecsSpinner.getValue());
    options.setIntroLoopSecs(introLoopSecsSpinner.getValue());
    options.setMergeTwins(mergeTwinsCheckbox.isSelected());
    options.setDcBlock(dcBlockCheckbox.isSelected());
    options.setNoFactory(noFactoryCheckbox.isSelected());
    options.setCheckDucking(checkDuckingCheckbox.isSelected());
    options.setNoChipCheck(noChipCheckCheckbox.isSelected());
    options.setBsmtHle(bsmtHleCheckbox.isSelected());

    try {
      JobDescriptor job = Studio.client.getAltSoundService().generateAltSound(game.getId(), options);
      if (job != null && !StringUtils.isEmpty(job.getError())) {
        WidgetFactory.showAlert(stage, Messages.get("common.error"), job.getError());
        return;
      }
      JobPoller.getInstance().setPolling();
      stage.close();
    }
    catch (Exception ex) {
      LOG.error("Failed to start the ALT sound generation: {}", ex.getMessage(), ex);
      WidgetFactory.showAlert(stage, Messages.get("common.error"), ex.getMessage());
    }
  }

  @Override
  public void onDialogCancel() {
  }

  /**
   * Downloads the latest rom2altsound release into the server's resources folder.
   *
   * @return the new version info or null when the installation failed (the error is shown)
   */
  @Nullable
  public static Rom2AltSoundVersion install(@Nullable Stage parent) {
    ProgressResultModel result = ProgressDialog.createProgressDialog(parent, new WaitProgressModel<>(Messages.get("tables.rom2altsound.installing_title"),
        Messages.get("tables.rom2altsound.installing"), () -> {
      try {
        return Studio.client.getAltSoundService().installRom2AltSound();
      }
      catch (Exception e) {
        LOG.error("rom2altsound installation failed: {}", e.getMessage(), e);
        return e;
      }
    }));

    Object value = result.getResults().isEmpty() ? null : result.getResults().getFirst();
    if (value instanceof Rom2AltSoundVersion installed && installed.isInstalled()) {
      return installed;
    }
    String msg = value instanceof Exception ex ? ex.getMessage() : null;
    WidgetFactory.showAlert(parent != null ? parent : Studio.stage, Messages.get("common.error"), Messages.get("tables.rom2altsound.install_failed"), msg);
    return null;
  }
}
