package de.mephisto.vpin.ui.tables.vps;

import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.connectors.vps.model.VPSChange;
import de.mephisto.vpin.connectors.vps.model.VPSChanges;
import de.mephisto.vpin.connectors.vps.model.VpsTable;
import de.mephisto.vpin.connectors.vps.model.VpsTableVersion;
import de.mephisto.vpin.restclient.games.GameRepresentation;
import de.mephisto.vpin.ui.Studio;
import edu.umd.cs.findbugs.annotations.Nullable;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import org.kordamp.ikonli.javafx.FontIcon;

import java.util.List;

public class VpsTableColumn extends HBox {

  public VpsTableColumn(@Nullable String vpsTableId, @Nullable String vpsTableVersionId, @Nullable VPSChanges updates) {
    super(3);
    int iconSize = 14;

    this.setAlignment(Pos.CENTER_LEFT);

    Label label = new Label();
    label.getStyleClass().add("default-title");
    VpsTable vpsTable = Studio.client.getVpsService().getTableById(vpsTableId);
    VpsTableVersion vpsTableVersion = null;

    if (vpsTable != null) {
      vpsTableVersion = vpsTable.getTableVersionById(vpsTableVersionId);

      FontIcon checkboxIcon = WidgetFactory.createCheckboxIcon();
      checkboxIcon.setIconSize(iconSize);
      label.setGraphic(checkboxIcon);
      label.setTooltip(new Tooltip("VPS Table:\n" + vpsTable.getDisplayName()));
    }
    else {
      label.setText(" - ");
      label.setStyle("-fx-text-fill: #FFFFFF;");
      label.setTooltip(new Tooltip("No VPS table mapped."));
    }
    this.getChildren().add(label);

    label = new Label(" / ");
    label.setStyle("-fx-text-fill: #FFFFFF;");
    this.getChildren().add(label);

    label = new Label();
    if (vpsTableVersion != null) {
      FontIcon checkboxIcon = WidgetFactory.createCheckboxIcon();
      checkboxIcon.setIconSize(iconSize);
      label.setGraphic(checkboxIcon);
      label.setTooltip(new Tooltip("VPS Table Version:\n" + vpsTableVersion.toString()));
    }
    else {
      label.setText(" - ");
      label.setStyle("-fx-text-fill: #FFFFFF;");
      label.setTooltip(new Tooltip("No VPS table version mapped."));
    }
    this.getChildren().add(label);

    label = new Label(" / ");
    label.setStyle("-fx-text-fill: #FFFFFF;");
    this.getChildren().add(label);

    label = new Label();

    if (updates != null && !updates.isEmpty() && vpsTable != null) {
      FontIcon updateIcon = WidgetFactory.createUpdateIcon();
      label.setGraphic(updateIcon);

      StringBuilder builder = new StringBuilder();
      List<VPSChange> changes = updates.getChanges();
      for (VPSChange change : changes) {
        builder.append(change.toString(vpsTable));
        builder.append("\n");
      }

      String tooltip = "The table or its assets have received updates:\n\n" + builder + "\n\nYou can reset this indicator with the reset action from the context menu.";
      Tooltip tt = new Tooltip(tooltip);
      tt.setStyle("-fx-font-weight: bold;");
      tt.setWrapText(true);
      tt.setMaxWidth(400);
      label.setTooltip(tt);
    }
    else {
      label.setText(" - ");
      label.setTooltip(new Tooltip("No updates available."));
      label.setStyle("-fx-text-fill: #FFFFFF;");
    }
    this.getChildren().add(label);
  }
}
