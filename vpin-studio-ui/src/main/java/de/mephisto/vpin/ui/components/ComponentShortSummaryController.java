package de.mephisto.vpin.ui.components;

import de.mephisto.vpin.commons.utils.JFXFuture;
import de.mephisto.vpin.commons.utils.i18n.Messages;
import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.components.ComponentRepresentation;
import de.mephisto.vpin.restclient.components.ComponentType;
import de.mephisto.vpin.restclient.emulators.GameEmulatorRepresentation;
import de.mephisto.vpin.restclient.preferences.PreferenceChangeListener;
import de.mephisto.vpin.ui.NavigationController;
import de.mephisto.vpin.ui.NavigationItem;
import de.mephisto.vpin.ui.NavigationOptions;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.events.EventManager;
import de.mephisto.vpin.ui.events.StudioEventListener;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static de.mephisto.vpin.commons.utils.WidgetFactory.createHelpIcon;
import static de.mephisto.vpin.ui.Studio.client;

/**
 * A single component card of the System Manager overview.
 */
public class ComponentShortSummaryController implements Initializable, StudioEventListener, PreferenceChangeListener {
  private final static Logger LOG = LoggerFactory.getLogger(ComponentShortSummaryController.class);

  /** vpinball release tags: v10.8.1-5436-af26b2d93 */
  private static final Pattern VPX_TAG_PATTERN = Pattern.compile("v?(\\d+\\.\\d+\\.\\d+)(?:-(\\d+))?.*");

  /** github urls: https://github.com/vpinball/pinmame/releases */
  private static final Pattern GITHUB_REPO_PATTERN = Pattern.compile("https?://github\\.com/([^/]+/[^/?#]+).*");

  private static final String[] STATUS_STYLES = {"sm-pill-ok", "sm-pill-warn", "sm-pill-unknown"};
  private static final String[] VERSION_STYLES = {"sm-version-ok", "sm-version-warn"};

  public enum Status {
    UP_TO_DATE, UPDATE_AVAILABLE, UNKNOWN
  }

  @FXML
  private VBox root;

  @FXML
  private Label titleLabel;

  @FXML
  private Label statusLabel;

  @FXML
  private Label installedVersionLabel;

  @FXML
  private Label latestVersionLabel;

  @FXML
  private Hyperlink link;

  @FXML
  private GridPane versionGrid;

  private ComponentRepresentation component;
  private Status status = Status.UNKNOWN;
  private Runnable onChange;

  @FXML
  private void onLink() {
    if (component != null && !StringUtils.isEmpty(component.getUrl())) {
      Studio.browse(component.getUrl());
    }
  }

  @FXML
  private void onManage() {
    if (component != null) {
      NavigationController.navigateTo(NavigationItem.Updates, new NavigationOptions(component.getType()));
    }
  }

  public VBox getRoot() {
    return root;
  }

  @Nullable
  public ComponentRepresentation getComponent() {
    return component;
  }

  public Status getStatus() {
    return status;
  }

  /**
   * Invoked whenever the status or the visibility of the card changes.
   */
  public void setOnChange(Runnable onChange) {
    this.onChange = onChange;
  }

  public void refresh(@NonNull ComponentRepresentation component) {
    this.component = component;

    titleLabel.setText(component.getType().toString());
    link.setText(toRepositoryName(component.getUrl()));
    link.setTooltip(StringUtils.isEmpty(component.getUrl()) ? null : new Tooltip(component.getUrl()));

    String installedVersion = component.getInstalledVersion();
    boolean installedKnown = installedVersion != null && !installedVersion.equals("?");

    installedVersionLabel.setGraphic(null);
    installedVersionLabel.setTooltip(null);
    if (installedKnown) {
      installedVersionLabel.setText(installedVersion);
    }
    else {
      createHelpIcon(installedVersionLabel, Messages.get("dialog.component_version_unmatched_hint"));
      installedVersionLabel.setText("?");
    }
    latestVersionLabel.setText(component.getLatestReleaseVersion() != null ? component.getLatestReleaseVersion() : "?");

    if (component.isVersionDiff()) {
      status = Status.UPDATE_AVAILABLE;
    }
    else if (installedKnown) {
      status = Status.UP_TO_DATE;
    }
    else {
      status = Status.UNKNOWN;
    }
    applyStatus();

    clearEmulatorRows();
    if (component.getType().equals(ComponentType.vpinball)) {
      // uncached, the versions of the emulators change with an installation
      JFXFuture.supplyAsync(() -> client.getEmulatorService().getGameEmulatorsUncached().stream()
              .filter(GameEmulatorRepresentation::isVpxEmulator).toList())
          .thenAcceptLater(emulators -> refreshEmulators(component, emulators));
    }

    if (component.getType().equals(ComponentType.doflinx)) {
      preferencesChanged(PreferenceNames.DOFLINX_SETTINGS, null);
    }
    else if (component.getType().equals(ComponentType.dof)) {
      preferencesChanged(PreferenceNames.DOF_SETTINGS, null);
    }
    fireChange();
  }

  private void applyStatus() {
    statusLabel.getStyleClass().removeAll(STATUS_STYLES);
    latestVersionLabel.getStyleClass().removeAll(VERSION_STYLES);
    switch (status) {
      case UP_TO_DATE -> {
        statusLabel.setText(Messages.get("components.overview.status_up_to_date"));
        statusLabel.getStyleClass().add("sm-pill-ok");
        latestVersionLabel.getStyleClass().add("sm-version-ok");
      }
      case UPDATE_AVAILABLE -> {
        statusLabel.setText(Messages.get("components.overview.status_update_available"));
        statusLabel.getStyleClass().add("sm-pill-warn");
        latestVersionLabel.getStyleClass().add("sm-version-warn");
      }
      default -> {
        statusLabel.setText(Messages.get("components.overview.status_unknown"));
        statusLabel.getStyleClass().add("sm-pill-unknown");
      }
    }
  }

  private void clearEmulatorRows() {
    versionGrid.getChildren().removeIf(node -> {
      Integer row = GridPane.getRowIndex(node);
      return row != null && row > 1;
    });
  }

  /**
   * Lists the installed version of every VPX emulator, the component version is only tracked for one folder.
   */
  private void refreshEmulators(ComponentRepresentation component, List<GameEmulatorRepresentation> emulators) {
    clearEmulatorRows();
    int row = 2;
    for (GameEmulatorRepresentation emulator : emulators) {
      Label nameLabel = new Label(emulator.getName());
      nameLabel.getStyleClass().add("sm-hint");

      String version = emulator.getEmulatorVersion();
      Label versionLabel = new Label(StringUtils.isEmpty(version) ? "?" : version);
      versionLabel.getStyleClass().add("sm-text");
      if (!StringUtils.isEmpty(version) && component.getLatestReleaseVersion() != null) {
        versionLabel.getStyleClass().add(isLatestVpxVersion(version, component.getLatestReleaseVersion()) ? "sm-version-ok" : "sm-version-warn");
      }
      if (!StringUtils.isEmpty(emulator.getInstallationDirectory())) {
        versionLabel.setTooltip(new Tooltip(emulator.getInstallationDirectory()));
      }

      versionGrid.add(nameLabel, 0, row);
      versionGrid.add(versionLabel, 1, row);
      row++;
    }
  }

  static String toRepositoryName(@Nullable String url) {
    if (StringUtils.isEmpty(url)) {
      return "-";
    }
    Matcher m = GITHUB_REPO_PATTERN.matcher(url.trim());
    return m.matches() ? m.group(1) : url;
  }

  /**
   * @param exeVersion the file version of the VPX executable, e.g. "10.8.1.5436"
   * @param releaseTag the vpinball release tag, e.g. "v10.8.1-5436-af26b2d93"
   */
  static boolean isLatestVpxVersion(String exeVersion, String releaseTag) {
    Matcher m = VPX_TAG_PATTERN.matcher(releaseTag.trim());
    if (!m.matches()) {
      return false;
    }
    String[] exe = exeVersion.trim().split("\\.");
    String[] tag = m.group(2) != null ? (m.group(1) + "." + m.group(2)).split("\\.") : m.group(1).split("\\.");
    if (exe.length < tag.length) {
      return false;
    }
    for (int i = 0; i < tag.length; i++) {
      if (!exe[i].equals(tag[i])) {
        return false;
      }
    }
    return true;
  }

  private void fireChange() {
    if (onChange != null) {
      onChange.run();
    }
  }

  @Override
  public void preferencesChanged(String key, Object value) {
    if (component == null) {
      return;
    }
    if (key.equals(PreferenceNames.DOFLINX_SETTINGS) && component.getType().equals(ComponentType.doflinx)) {
      JFXFuture.supplyAsync(() -> client.getDofLinxService().isValid())
          .thenAcceptLater(isDofLinxValid -> setCardVisible(isDofLinxValid));
    }
    if (key.equals(PreferenceNames.DOF_SETTINGS) && component.getType().equals(ComponentType.dof)) {
      JFXFuture.supplyAsync(() -> client.getDofService().isValid())
          .thenAcceptLater(isDofValid -> setCardVisible(isDofValid));
    }
  }

  private void setCardVisible(boolean visible) {
    if (root.isVisible() != visible) {
      root.setVisible(visible);
      fireChange();
    }
  }

  @Override
  public void thirdPartyVersionUpdated(@NonNull ComponentType type) {
    if (component != null && component.getType().equals(type)) {
      JFXFuture.supplyAsync(() -> client.getComponentService().getComponent(type))
          .thenAcceptLater(this::refresh);
    }
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    client.getPreferenceService().addListener(this);
    root.managedProperty().bindBidirectional(root.visibleProperty());
    EventManager.getInstance().addListener(this);
  }
}
