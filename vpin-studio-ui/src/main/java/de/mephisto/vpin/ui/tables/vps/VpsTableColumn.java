package de.mephisto.vpin.ui.tables.vps;

import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.commons.utils.i18n.Messages;
import de.mephisto.vpin.connectors.vps.model.*;
import de.mephisto.vpin.restclient.vps.VpsChangeFilter;
import de.mephisto.vpin.restclient.vps.VpsSettings;
import de.mephisto.vpin.ui.Studio;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Paint;
import org.jspecify.annotations.Nullable;
import org.kordamp.ikonli.javafx.FontIcon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.util.List;

public class VpsTableColumn extends HBox {
  private final static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  public VpsTableColumn(@Nullable String vpsTableId, @Nullable String vpsTableVersionId, boolean disabled, boolean ignoreUpdates, @Nullable VPSChanges updates, VpsSettings vpsSettings) {
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
        label.setTooltip(new Tooltip(Messages.get("dialog.vps_table_colon", vpsTable.getDisplayName())));
      }
      else {
        label.setText(" - ");
        label.setPrefWidth(WidgetFactory.DEFAULT_ICON_SIZE);
        label.setStyle(disabled ? WidgetFactory.DISABLED_TEXT_STYLE : WidgetFactory.DEFAULT_TEXT_STYLE);
        label.setTooltip(new Tooltip(Messages.get("dialog.no_vps_table_mapped")));
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
        label.setTooltip(new Tooltip(Messages.get("dialog.vps_table_version_colon", vpsTableVersion.toString())));
      }
      else {
        label.setText(" - ");
        label.setPrefWidth(WidgetFactory.DEFAULT_ICON_SIZE);
        label.setStyle(disabled ? WidgetFactory.DISABLED_TEXT_STYLE : WidgetFactory.DEFAULT_TEXT_STYLE);
        label.setTooltip(new Tooltip(Messages.get("dialog.no_vps_table_version_mapped")));
      }
      this.getChildren().add(label);

      label = new Label(" / ");
      label.setStyle(disabled ? WidgetFactory.DISABLED_TEXT_STYLE : WidgetFactory.DEFAULT_TEXT_STYLE);
      this.getChildren().add(label);

      label = new Label();

      if (ignoreUpdates) {
        FontIcon ignoredIcon = WidgetFactory.createIcon("mdi2b-bell-cancel-outline");
        label.setGraphic(ignoredIcon);
        label.setTooltip(new Tooltip(Messages.get("dialog.updates_for_this_table_are_ignored")));
      }
      else {
        applyChanges(label, vpsTable, updates, disabled, vpsSettings);
      }

      this.getChildren().add(label);
    }
    catch (Exception e) {
      LOG.error("Failed to render VPS table container: " + e.getMessage(), e);
      this.getChildren().add(new Label(Messages.get("vps.vps_table_column.render_error")));
    }
  }

  private void applyChanges(Label label, VpsTable vpsTable, VPSChanges updates, boolean disabled, VpsSettings vpsSettings) {
    int changeCounter = 0;
    if (updates != null && !updates.isEmpty() && vpsTable != null) {
      StringBuilder builder = new StringBuilder();
      List<VPSChange> changes = updates.getChanges();
      for (VPSChange change : changes) {
        if (isFiltered(vpsSettings, change)) {
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

        String tooltip = Messages.get("dialog.table_or_assets_received_updates", builder.toString());
        Tooltip tt = new Tooltip(tooltip);
        tt.setStyle("-fx-font-weight: bold;");
        tt.setWrapText(true);
        tt.setMaxWidth(400);
        label.setTooltip(tt);
      }
    }

    if (changeCounter == 0) {
      label.setText(" - ");
      label.setTooltip(new Tooltip(Messages.get("dialog.no_updates_available")));
      label.setStyle(disabled ? WidgetFactory.DISABLED_TEXT_STYLE : WidgetFactory.DEFAULT_TEXT_STYLE);
    }
  }

  public static boolean isFiltered(VpsSettings vpsSettings, VPSChange change) {
    return VpsChangeFilter.isFiltered(vpsSettings, change);
  }
}
