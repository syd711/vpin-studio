package de.mephisto.vpin.ui.components;

import de.mephisto.vpin.commons.utils.i18n.Messages;
import de.mephisto.vpin.commons.utils.JFXFuture;
import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.components.ComponentRepresentation;
import de.mephisto.vpin.restclient.components.ComponentType;
import de.mephisto.vpin.restclient.emulators.GameEmulatorRepresentation;
import de.mephisto.vpin.restclient.preferences.PreferenceChangeListener;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.events.EventManager;
import de.mephisto.vpin.ui.events.StudioEventListener;
import org.jspecify.annotations.NonNull;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Font;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static de.mephisto.vpin.commons.utils.WidgetFactory.*;
import static de.mephisto.vpin.ui.Studio.client;

public class ComponentShortSummaryController implements Initializable, StudioEventListener, PreferenceChangeListener {
  private final static Logger LOG = LoggerFactory.getLogger(ComponentShortSummaryController.class);

  /** vpinball release tags: v10.8.1-5436-af26b2d93 */
  private static final Pattern VPX_TAG_PATTERN = Pattern.compile("v?(\\d+\\.\\d+\\.\\d+)(?:-(\\d+))?.*");


  @FXML
  private BorderPane root;

  @FXML
  private Label titleLabel;

  @FXML
  private Label installedVersionLabel;

  @FXML
  private Label latestVersionLabel;

  @FXML
  private Hyperlink link;

  @FXML
  private GridPane emulatorsGrid;

  private ComponentRepresentation component;

  @FXML
  private void onLink(ActionEvent event) {
    Hyperlink link = (Hyperlink) event.getSource();
    String linkText = link.getText();
    Studio.browse(linkText);
  }

  public void refresh(@NonNull ComponentRepresentation component) {
    this.component = component;
    latestVersionLabel.getStyleClass().remove("orange-label");
    latestVersionLabel.getStyleClass().remove("green-label");
    link.setText(component.getUrl());

    if (component.isVersionDiff()) {
      latestVersionLabel.getStyleClass().add("orange-label");
    }
    else if (component.getInstalledVersion() != null && !component.getInstalledVersion().equals("?")) {
      latestVersionLabel.getStyleClass().add("green-label");
    }

    titleLabel.setText(component.getType().toString());

    if (component.getInstalledVersion() != null && !component.getInstalledVersion().equals("?")) {
      installedVersionLabel.setText(component.getInstalledVersion());
    }
    else {
      createHelpIcon(installedVersionLabel, Messages.get("dialog.component_version_unmatched_hint"));
    }
    latestVersionLabel.setText(component.getLatestReleaseVersion() != null ? component.getLatestReleaseVersion() : "?");

    emulatorsGrid.getChildren().clear();
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
  }

  /**
   * Lists the installed version of every VPX emulator, the component version is only tracked for one folder.
   */
  private void refreshEmulators(ComponentRepresentation component, List<GameEmulatorRepresentation> emulators) {
    emulatorsGrid.getChildren().clear();
    int row = 0;
    for (GameEmulatorRepresentation emulator : emulators) {
      Label nameLabel = new Label(Messages.get("components.component_short_summary_panel.emulator_version", emulator.getName()));
      nameLabel.getStyleClass().add("default-text");
      nameLabel.setMinHeight(30);

      String version = emulator.getEmulatorVersion();
      Label versionLabel = new Label(StringUtils.isEmpty(version) ? "?" : version);
      versionLabel.setFont(Font.font(14));
      versionLabel.getStyleClass().add("default-text");
      if (!StringUtils.isEmpty(version) && component.getLatestReleaseVersion() != null) {
        versionLabel.getStyleClass().add(isLatestVpxVersion(version, component.getLatestReleaseVersion()) ? "green-label" : "orange-label");
      }
      if (!StringUtils.isEmpty(emulator.getInstallationDirectory())) {
        versionLabel.setTooltip(new javafx.scene.control.Tooltip(emulator.getInstallationDirectory()));
      }

      emulatorsGrid.add(nameLabel, 0, row);
      emulatorsGrid.add(versionLabel, 2, row);
      row++;
    }
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

  @Override
  public void preferencesChanged(String key, Object value) {
    if (key.equals(PreferenceNames.DOFLINX_SETTINGS) && component.getType().equals(ComponentType.doflinx)) {
      JFXFuture.supplyAsync(() -> client.getDofLinxService().isValid())
          .thenAcceptLater(isDofLinxValid -> root.setVisible(isDofLinxValid));
    }
    if (key.equals(PreferenceNames.DOF_SETTINGS) && component.getType().equals(ComponentType.dof)) {
      JFXFuture.supplyAsync(() -> client.getDofService().isValid())
          .thenAcceptLater(isDofValid -> root.setVisible(isDofValid));
    }
  }

  @Override
  public void thirdPartyVersionUpdated(@NonNull ComponentType type) {
    if (component.getType().equals(type)) {
      JFXFuture.supplyAsync(() -> client.getComponentService().getComponent(type))
          .thenAcceptLater(updatedComponent -> refresh(updatedComponent));
    }
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    client.getPreferenceService().addListener(this);
    root.managedProperty().bindBidirectional(root.visibleProperty());
    EventManager.getInstance().addListener(this);
  }
}
