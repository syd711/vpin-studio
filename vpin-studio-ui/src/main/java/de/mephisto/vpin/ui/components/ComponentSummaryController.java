package de.mephisto.vpin.ui.components;

import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.commons.utils.i18n.Messages;
import de.mephisto.vpin.restclient.components.ComponentRepresentation;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.events.EventManager;
import de.mephisto.vpin.ui.util.StudioFolderChooser;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Optional;
import java.util.ResourceBundle;

import static de.mephisto.vpin.ui.Studio.client;

/**
 * The version and installation sections of a component tab.
 */
public class ComponentSummaryController implements Initializable {
  private final static Logger LOG = LoggerFactory.getLogger(ComponentSummaryController.class);

  private static final String[] STATUS_STYLES = {"sm-pill-ok", "sm-pill-warn", "sm-pill-unknown"};
  private static final String[] VERSION_STYLES = {"sm-version-ok", "sm-version-warn"};

  @FXML
  private VBox versionSection;

  @FXML
  private VBox installationSection;

  @FXML
  private HBox statusRow;

  @FXML
  private Label statusLabel;

  @FXML
  private Button setVersionBtn;

  @FXML
  private Button resetVersionBtn;

  @FXML
  private Button ignoreBtn;

  @FXML
  private Label installedVersionLabel;

  @FXML
  private Label latestVersionLabel;

  @FXML
  private Label folderLabel;

  @FXML
  private Button folderBtn;

  @FXML
  private Button folderResetBtn;

  @FXML
  private Label lastModifiedLabel;

  @FXML
  private Label lastCheckLabel;

  @FXML
  private Hyperlink githubLink;

  private ComponentRepresentation component;

  /**
   * Set when another folder than the component folder is selected, e.g. another VPX emulator
   */
  private String overrideFolder;
  private String overrideVersion;

  public VBox getVersionSection() {
    return versionSection;
  }

  public VBox getInstallationSection() {
    return installationSection;
  }

  @FXML
  public void onHyperlink() {
    if (component != null && !StringUtils.isEmpty(component.getUrl())) {
      Studio.browse(component.getUrl());
    }
  }

  @FXML
  public void onVersionSet() {
    Optional<ButtonType> result = WidgetFactory.showConfirmation(Studio.stage, Messages.get("dialog.apply_version"), Messages.get("msg.apply_as_the_current_version_of", latestVersionLabel.getText(), String.valueOf(component.getType())), null, Messages.get("common.apply"));
    if (result.isPresent() && result.get().equals(ButtonType.OK)) {
      try {
        client.getComponentService().setVersion(component.getType(), component.getLatestReleaseVersion());
        EventManager.getInstance().notify3rdPartyVersionUpdate(component.getType());
      }
      catch (Exception e) {
        WidgetFactory.showAlert(Studio.stage, Messages.get("common.error"), Messages.get("msg.failed_to_apply_version", e.getMessage()));
      }
    }
  }

  @FXML
  public void onVersionReset() {
    Optional<ButtonType> result = WidgetFactory.showConfirmation(Studio.stage, Messages.get("dialog.reset_version"), Messages.get("msg.reset_version", latestVersionLabel.getText()), null, Messages.get("common.reset"));
    if (result.isPresent() && result.get().equals(ButtonType.OK)) {
      try {
        client.getComponentService().setVersion(component.getType(), "-");
        EventManager.getInstance().notify3rdPartyVersionUpdate(component.getType());
      }
      catch (Exception e) {
        WidgetFactory.showAlert(Studio.stage, Messages.get("common.error"), Messages.get("msg.failed_to_reset_version", e.getMessage()));
      }
    }
  }

  @FXML
  public void onVersionIgnore() {
    Optional<ButtonType> result = WidgetFactory.showConfirmation(Studio.stage, Messages.get("dialog.ignore_version"), Messages.get("msg.ignore_version", latestVersionLabel.getText()), Messages.get("dialog.the_previous_version_will_be_used_as"), Messages.get("dialog.ignore_version"));
    if (result.isPresent() && result.get().equals(ButtonType.OK)) {
      try {
        client.getComponentService().ignoreVersion(component.getType(), component.getLatestReleaseVersion());
        EventManager.getInstance().notify3rdPartyVersionUpdate(component.getType());
      }
      catch (Exception e) {
        WidgetFactory.showAlert(Studio.stage, Messages.get("common.error"), Messages.get("msg.failed_to_ignore_version", e.getMessage()));
      }
    }
  }

  @FXML
  public void onFolderSelect() {
    StudioFolderChooser chooser = new StudioFolderChooser();
    chooser.setTitle(Messages.get("dialog.select_target_folder"));
    File targetFolder = chooser.showOpenDialog(Studio.stage);
    if (targetFolder != null && targetFolder.exists()) {
      try {
        client.getComponentService().setTargetFolder(component.getType(), targetFolder.getAbsolutePath());
        EventManager.getInstance().notify3rdPartyVersionUpdate(component.getType());
      }
      catch (Exception e) {
        WidgetFactory.showAlert(Studio.stage, Messages.get("common.error"), Messages.get("msg.failed_to_apply_version", e.getMessage()));
      }
    }
  }

  @FXML
  public void onFolderReset() {
    try {
      client.getComponentService().setTargetFolder(component.getType(), null);
      EventManager.getInstance().notify3rdPartyVersionUpdate(component.getType());
    }
    catch (Exception e) {
      WidgetFactory.showAlert(Studio.stage, Messages.get("common.error"), Messages.get("msg.failed_to_apply_version", e.getMessage()));
    }
  }

  /**
   * Shows the given folder and its version instead of the component ones, null resets to the component.
   * The version buttons are hidden then as the component version is only tracked for the component folder.
   */
  protected void setTargetOverride(String folder, String version) {
    this.overrideFolder = folder;
    this.overrideVersion = version;
    refreshComponent(component);
  }

  protected void setComponent(AbstractComponentTab componentTab, ComponentRepresentation component) {
    refreshComponent(component);
  }

  protected void refreshComponent(ComponentRepresentation component) {
    this.component = component;

    latestVersionLabel.getStyleClass().removeAll(VERSION_STYLES);
    statusLabel.getStyleClass().removeAll(STATUS_STYLES);

    installedVersionLabel.setText("?");
    latestVersionLabel.setText("?");
    lastCheckLabel.setText("");
    lastModifiedLabel.setText("?");
    folderLabel.setText("-");
    folderLabel.setTooltip(null);
    folderBtn.setVisible(true);
    folderResetBtn.setVisible(false);
    statusRow.setVisible(false);

    if (component == null) {
      return;
    }

    String installedVersion = component.getInstalledVersion();
    boolean installedKnown = !StringUtils.isEmpty(installedVersion) && !installedVersion.equals("?") && !installedVersion.equals("-");

    setVersionBtn.setVisible(component.isInstalled());
    setVersionBtn.setDisable(installedKnown && installedVersion.equals(component.getLatestReleaseVersion()));

    resetVersionBtn.setVisible(component.isInstalled());
    resetVersionBtn.setDisable(!installedKnown);

    ignoreBtn.setVisible(component.isInstalled() &&
        component.getLatestReleaseVersion() != null && !component.getLatestReleaseVersion().equals("?") && component.getReleases().size() > 1);

    installedVersionLabel.setText(installedVersion != null ? installedVersion : "?");
    latestVersionLabel.setText(component.getLatestReleaseVersion() != null ? component.getLatestReleaseVersion() : "?");

    // same status logic as the overview cards
    statusRow.setVisible(true);
    if (component.isVersionDiff()) {
      statusLabel.setText(Messages.get("components.overview.status_update_available"));
      statusLabel.getStyleClass().add("sm-pill-warn");
      latestVersionLabel.getStyleClass().add("sm-version-warn");
    }
    else if (installedKnown) {
      statusLabel.setText(Messages.get("components.overview.status_up_to_date"));
      statusLabel.getStyleClass().add("sm-pill-ok");
      latestVersionLabel.getStyleClass().add("sm-version-ok");
    }
    else {
      statusLabel.setText(Messages.get("components.overview.status_unknown"));
      statusLabel.getStyleClass().add("sm-pill-unknown");
    }

    DateTimeFormatter formatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT);
    lastCheckLabel.setText(component.getLastCheck() != null ? Messages.get("components.tab.checked", formatter.format(component.getLastCheck())) : "");
    lastModifiedLabel.setText(component.getLastModified() != null ? formatter.format(component.getLastModified()) : "?");

    setFolder(component.getTargetFolder() != null ? component.getTargetFolder() : "?");
    folderBtn.setVisible(true);
    folderResetBtn.setVisible(!StringUtils.isEmpty(component.getTargetFolderOverride()));

    githubLink.setText(ComponentShortSummaryController.toRepositoryName(component.getUrl()));
    githubLink.setTooltip(StringUtils.isEmpty(component.getUrl()) ? null : new Tooltip(component.getUrl()));

    if (overrideFolder != null) {
      setFolder(overrideFolder);
      installedVersionLabel.setText(overrideVersion != null ? overrideVersion : "?");
      // the status is only tracked for the component folder
      statusRow.setVisible(false);
      latestVersionLabel.getStyleClass().removeAll(VERSION_STYLES);
      setVersionBtn.setVisible(false);
      resetVersionBtn.setVisible(false);
      folderBtn.setVisible(false);
      folderResetBtn.setVisible(false);
    }
  }

  private void setFolder(String folder) {
    folderLabel.setText(folder);
    folderLabel.setTooltip(new Tooltip(folder));
  }

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    ignoreBtn.setVisible(false);
    statusRow.managedProperty().bind(statusRow.visibleProperty());
    setVersionBtn.managedProperty().bind(setVersionBtn.visibleProperty());
    resetVersionBtn.managedProperty().bind(resetVersionBtn.visibleProperty());
    ignoreBtn.managedProperty().bind(ignoreBtn.visibleProperty());
    folderBtn.managedProperty().bind(folderBtn.visibleProperty());
    folderResetBtn.managedProperty().bind(folderResetBtn.visibleProperty());
  }
}
