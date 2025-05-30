package de.mephisto.vpin.ui.tables.vps;

import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.connectors.vps.model.*;
import de.mephisto.vpin.restclient.preferences.UISettings;
import de.mephisto.vpin.ui.Studio;
import edu.umd.cs.findbugs.annotations.Nullable;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Paint;

import org.kordamp.ikonli.javafx.FontIcon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class VpsTableColumn extends HBox {
  private final static Logger LOG = LoggerFactory.getLogger(VpsTableColumn.class);

  public VpsTableColumn(@Nullable String vpsTableId, @Nullable String vpsTableVersionId, boolean disabled, @Nullable VPSChanges updates, UISettings uiSettings) {
    super(3);
    try {

      int iconSize = WidgetFactory.DEFAULT_ICON_SIZE;

      this.setAlignment(Pos.CENTER_LEFT);

      Label label = new Label();
      label.getStyleClass().add("default-title");
      VpsTable vpsTable = Studio.client.getVpsService().getTableById(vpsTableId);
      VpsTableVersion vpsTableVersion = null;

      if (vpsTable != null) {
        vpsTableVersion = vpsTable.getTableVersionById(vpsTableVersionId);

        FontIcon checkboxIcon = WidgetFactory.createCheckboxIcon(disabled ? WidgetFactory.DISABLED_COLOR : null);
        checkboxIcon.setIconSize(iconSize);
        label.setGraphic(checkboxIcon);
        label.setTooltip(new Tooltip("VPS Table:\n" + vpsTable.getDisplayName()));
      }
      else {
        label.setText(" - ");
        label.setStyle(disabled ? WidgetFactory.DISABLED_TEXT_STYLE : WidgetFactory.DEFAULT_TEXT_STYLE);
        label.setTooltip(new Tooltip("No VPS table mapped."));
      }
      this.getChildren().add(label);

      label = new Label(" / ");
      label.setStyle(disabled ? WidgetFactory.DISABLED_TEXT_STYLE : WidgetFactory.DEFAULT_TEXT_STYLE);
      this.getChildren().add(label);

      label = new Label();
      if (vpsTableVersion != null) {
        FontIcon checkboxIcon = WidgetFactory.createCheckboxIcon(disabled ? WidgetFactory.DISABLED_COLOR : null);
        checkboxIcon.setIconSize(iconSize);
        label.setGraphic(checkboxIcon);
        label.setTooltip(new Tooltip("VPS Table Version:\n" + vpsTableVersion.toString()));
      }
      else {
        label.setText(" - ");
        label.setStyle(disabled ? WidgetFactory.DISABLED_TEXT_STYLE : WidgetFactory.DEFAULT_TEXT_STYLE);
        label.setTooltip(new Tooltip("No VPS table version mapped."));
      }
      this.getChildren().add(label);

      label = new Label(" / ");
      label.setStyle(disabled ? WidgetFactory.DISABLED_TEXT_STYLE : WidgetFactory.DEFAULT_TEXT_STYLE);
      this.getChildren().add(label);

      label = new Label();

      int changeCounter = 0;
      if (updates != null && !updates.isEmpty() && vpsTable != null) {
        StringBuilder builder = new StringBuilder();
        List<VPSChange> changes = updates.getChanges();
        for (VPSChange change : changes) {
          if (isFiltered(uiSettings, change)) {
            continue;
          }
          changeCounter++;
          String changeValue = change.toString(vpsTable);
          if (changeValue != null) {
            builder.append(changeValue);
            builder.append("\n");
          }
        }

        if (changeCounter > 0) {
          FontIcon updateIcon = WidgetFactory.createUpdateIcon();
          if (disabled) {
            updateIcon.setIconColor(Paint.valueOf(WidgetFactory.DISABLED_COLOR));
            ;
          }
          label.setGraphic(updateIcon);

          String tooltip = "The table or its assets have received updates:\n\n" + builder + "\n\nYou can reset this indicator with the reset action from the context menu.";
          Tooltip tt = new Tooltip(tooltip);
          tt.setStyle("-fx-font-weight: bold;");
          tt.setWrapText(true);
          tt.setMaxWidth(400);
          label.setTooltip(tt);
        }
      }

      if (changeCounter == 0) {
        label.setText(" - ");
        label.setTooltip(new Tooltip("No updates available."));
        label.setStyle(disabled ? WidgetFactory.DISABLED_TEXT_STYLE : WidgetFactory.DEFAULT_TEXT_STYLE);
      }

      this.getChildren().add(label);
    }
    catch (Exception e) {
      LOG.error("Failed to render VPS table container: " + e.getMessage(), e);
      this.getChildren().add(new Label("ERROR"));
    }
  }

  public static boolean isFiltered(UISettings uiSettings, VPSChange change) {
    if (uiSettings != null) {
      if (change.getDiffType() != null && change.getDiffType().equals(VpsDiffTypes.b2s) && !uiSettings.isVpsBackglass()) {
        return true;
      }
      if (change.getDiffType() != null && change.getDiffType().equals(VpsDiffTypes.topper) && !uiSettings.isVpsToppper()) {
        return true;
      }
      if (change.getDiffType() != null && change.getDiffType().equals(VpsDiffTypes.pov) && !uiSettings.isVpsPOV()) {
        return true;
      }
      if (change.getDiffType() != null && change.getDiffType().equals(VpsDiffTypes.rom) && !uiSettings.isVpsRom()) {
        return true;
      }
      if (change.getDiffType() != null && change.getDiffType().equals(VpsDiffTypes.altColor) && !uiSettings.isVpsAltColor()) {
        return true;
      }
      if (change.getDiffType() != null && change.getDiffType().equals(VpsDiffTypes.altSound) && !uiSettings.isVpsAltSound()) {
        return true;
      }
      if (change.getDiffType() != null && change.getDiffType().equals(VpsDiffTypes.pupPack) && !uiSettings.isVpsPUPPack()) {
        return true;
      }
      if (change.getDiffType() != null && change.getDiffType().equals(VpsDiffTypes.sound) && !uiSettings.isVpsSound()) {
        return true;
      }
      if (change.getDiffType() != null && change.getDiffType().equals(VpsDiffTypes.wheel) && !uiSettings.isVpsWheel()) {
        return true;
      }
      if (change.getDiffType() != null && change.getDiffType().equals(VpsDiffTypes.tutorial) && !uiSettings.isVpsTutorial()) {
        return true;
      }
    }
    return false;
  }
}
