package de.mephisto.vpin.ui.components;

import de.mephisto.vpin.commons.utils.JFXFuture;
import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.restclient.components.ComponentRepresentation;
import de.mephisto.vpin.restclient.components.ComponentType;
import de.mephisto.vpin.restclient.emulators.GameEmulatorRepresentation;
import de.mephisto.vpin.restclient.textedit.TextEditorFile;
import de.mephisto.vpin.restclient.textedit.TextEditorFileTypes;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.util.Dialogs;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.ResourceBundle;

import static de.mephisto.vpin.ui.Studio.client;
import de.mephisto.vpin.commons.utils.i18n.Messages;

public class TabVpxController extends AbstractComponentTab implements Initializable {
  private final static Logger LOG = LoggerFactory.getLogger(TabVpxController.class);

  @FXML
  private Button openBtn;

  @FXML
  private Separator emulatorSeparator;

  @FXML
  private Label emulatorLabel;

  @FXML
  private ComboBox<VpxTarget> emulatorCombo;

  /**
   * An installation target of the VPX updates: a VPX emulator or the component folder if no emulator uses it.
   */
  public record VpxTarget(@Nullable GameEmulatorRepresentation emulator, String folder) {
    @Override
    public String toString() {
      if (emulator == null) {
        return Messages.get("components.vpx.component_folder", folder);
      }
      return emulator.getName() + (StringUtils.isEmpty(emulator.getEmulatorVersion()) ? "" : " (" + emulator.getEmulatorVersion() + ")");
    }
  }

  @FXML
  private void onOpen() {
    try {
      // the ini depends on the version and folder of the selected emulator
      TextEditorFile file = new TextEditorFile(TextEditorFileTypes.VPinballXIni);
      VpxTarget target = emulatorCombo.getValue();
      String title = "VPinballX.ini";
      if (target != null && target.emulator() != null) {
        file.setEmulatorId(target.emulator().getId());
        title += " - " + target.emulator().getName();
      }
      Dialogs.openTextEditor(file, title);
    }
    catch (Exception e) {
      LOG.error("Failed to open file: " + e.getMessage(), e);
      WidgetFactory.showAlert(Studio.stage, Messages.get("common.error"), Messages.get("msg.failed_to_open_file", e.getMessage()));
    }
  }

  @Override
  protected ComponentType getComponentType() {
    return ComponentType.vpinball;
  }

  @Override
  protected String getTargetFolder() {
    VpxTarget target = emulatorCombo.getValue();
    return target != null ? target.folder() : super.getTargetFolder();
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    super.initialize();
    componentUpdateController.setLocalInstallOnly(false);

    emulatorSeparator.managedProperty().bind(emulatorSeparator.visibleProperty());
    emulatorLabel.managedProperty().bind(emulatorLabel.visibleProperty());
    emulatorCombo.managedProperty().bind(emulatorCombo.visibleProperty());
    emulatorLabel.visibleProperty().bind(emulatorCombo.visibleProperty());
    emulatorSeparator.visibleProperty().bind(emulatorCombo.visibleProperty());
    emulatorCombo.setVisible(false);

    emulatorCombo.valueProperty().addListener((obs, oldValue, newValue) -> applyTarget(newValue));
  }

  @Override
  protected void refreshTab(ComponentRepresentation component) {
    openFolderButton.setDisable(!component.isInstalled());
    openBtn.setDisable(!component.isInstalled() || client.getVpxService().getVpxFile() == null);

    // uncached, the versions of the emulators change with an installation
    JFXFuture.supplyAsync(() -> client.getEmulatorService().getGameEmulatorsUncached().stream()
            .filter(GameEmulatorRepresentation::isVpxEmulator).toList())
        .thenAcceptLater(emulators -> refreshTargets(component, emulators));
  }

  private void refreshTargets(ComponentRepresentation component, List<GameEmulatorRepresentation> emulators) {
    List<VpxTarget> targets = new ArrayList<>();
    for (GameEmulatorRepresentation emulator : emulators) {
      if (!StringUtils.isEmpty(emulator.getInstallationDirectory())) {
        targets.add(new VpxTarget(emulator, emulator.getInstallationDirectory()));
      }
    }

    String componentFolder = component.getTargetFolder();
    boolean usedByEmulator = targets.stream().anyMatch(t -> isSameFolder(t.folder(), componentFolder));
    if (!usedByEmulator && !StringUtils.isEmpty(componentFolder)) {
      targets.addFirst(new VpxTarget(null, componentFolder));
    }

    // keep the selection over refreshes, the component folder otherwise
    VpxTarget selection = emulatorCombo.getValue();
    VpxTarget newSelection = null;
    if (selection != null) {
      newSelection = targets.stream().filter(t -> isSameTarget(t, selection)).findFirst().orElse(null);
    }
    if (newSelection == null) {
      newSelection = targets.stream().filter(t -> isSameFolder(t.folder(), componentFolder)).findFirst()
          .orElse(targets.isEmpty() ? null : targets.getFirst());
    }

    emulatorCombo.setItems(FXCollections.observableList(targets));
    emulatorCombo.setVisible(targets.size() > 1);
    if (Objects.equals(emulatorCombo.getValue(), newSelection)) {
      applyTarget(newSelection);
    }
    else {
      emulatorCombo.setValue(newSelection);
    }
  }

  private void applyTarget(@Nullable VpxTarget target) {
    if (component == null) {
      return;
    }
    boolean isComponentFolder = target == null || isSameFolder(target.folder(), component.getTargetFolder());
    componentUpdateController.setTargetFolder(isComponentFolder ? null : target.folder());
    if (isComponentFolder) {
      componentSummaryController.setTargetOverride(null, null);
    }
    else {
      componentSummaryController.setTargetOverride(target.folder(), target.emulator() != null ? target.emulator().getEmulatorVersion() : null);
    }
  }

  private static boolean isSameTarget(VpxTarget a, VpxTarget b) {
    if (a.emulator() != null && b.emulator() != null) {
      return a.emulator().getId() == b.emulator().getId();
    }
    return a.emulator() == null && b.emulator() == null && isSameFolder(a.folder(), b.folder());
  }

  static boolean isSameFolder(@Nullable String a, @Nullable String b) {
    if (a == null || b == null) {
      return false;
    }
    return normalize(a).equalsIgnoreCase(normalize(b));
  }

  private static String normalize(String folder) {
    String f = folder.trim().replace('/', '\\');
    while (f.endsWith("\\")) {
      f = f.substring(0, f.length() - 1);
    }
    return f;
  }
}
